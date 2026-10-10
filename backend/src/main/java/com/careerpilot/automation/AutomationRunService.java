package com.careerpilot.automation;

import com.careerpilot.auth.JwtService;
import com.careerpilot.auth.UserAccount;
import com.careerpilot.auth.UserAccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.careerpilot.portal.SimulatedPortalDtos;
import com.careerpilot.portal.SimulatedPortalService;
import com.careerpilot.resume.ResumeRepository;
import com.careerpilot.resume.ResumeStorage;
import com.careerpilot.resume.ResumeVersion;
import com.careerpilot.resume.ResumeVersionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.careerpilot.automation.AutomationRunDtos.*;

@Service
public class AutomationRunService {
    private final AutomationRunRepository runs;
    private final ResumeVersionRepository versions;
    private final ResumeRepository resumes;
    private final ResumeStorage storage;
    private final UserAccountRepository users;
    private final SimulatedPortalService portals;
    private final PortalAutomationRunner runner;
    private final JwtService jwt;
    private final TaskExecutor executor;
    private final TransactionTemplate transactions;
    private final ObjectMapper mapper;
    private final String frontendUrl;
    private final String apiBaseUrl;

    public AutomationRunService(AutomationRunRepository runs,ResumeVersionRepository versions,ResumeRepository resumes,
                                ResumeStorage storage,UserAccountRepository users,SimulatedPortalService portals,
                                PortalAutomationRunner runner,JwtService jwt,@Qualifier("portalAutomationExecutor") TaskExecutor portalAutomationExecutor,
                                org.springframework.transaction.PlatformTransactionManager transactionManager,ObjectMapper mapper,
                                @Value("${app.automation.frontend-url:http://127.0.0.1:4173}") String frontendUrl,
                                @Value("${app.automation.api-base-url:http://localhost:8080/api/v1}") String apiBaseUrl) {
        this.runs=runs;this.versions=versions;this.resumes=resumes;this.storage=storage;this.users=users;
        this.portals=portals;this.runner=runner;this.jwt=jwt;this.executor=portalAutomationExecutor;
        this.transactions=new TransactionTemplate(transactionManager);this.mapper=mapper;
        this.frontendUrl=localUrl(frontendUrl);this.apiBaseUrl=localUrl(apiBaseUrl);
    }

    @Transactional
    public RunResponse start(UUID owner,UUID idempotencyKey,StartRequest request) {
        if(idempotencyKey==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"An idempotency key is required.");
        String fingerprint=fingerprint(request);
        var prior=runs.findByOwnerUserIdAndIdempotencyKey(owner,idempotencyKey);
        if(prior.isPresent()) {
            if(!fingerprint.equals(prior.get().getRequestFingerprint()))
                throw new ResponseStatusException(HttpStatus.CONFLICT,"This idempotency key was already used with different submission details.");
            return dto(prior.get());
        }
        String portalKey=request.portalKey().toUpperCase(java.util.Locale.ROOT);
        SimulatedPortalDtos.PortalJob job=portals.jobs(portalKey).stream().filter(candidate->candidate.key().equals(request.jobKey())).findFirst()
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Simulated job not found."));
        validatePortalFields(portalKey,job,request);
        ResumeVersion version=versions.findById(request.resumeVersionId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Resume version not found."));
        if(resumes.findByIdAndUser_Id(version.getResumeId(),owner).isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Resume version not found.");
        Path resumeFile;
        try{resumeFile=storage.resolve(version.getStorageKey());}
        catch(IOException ex){throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Resume file is unavailable.");}
        UserAccount account=users.findById(owner).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found."));
        AutomationRun run=runs.saveAndFlush(new AutomationRun(owner,portalKey,job.key(),idempotencyKey,fingerprint));
        PortalAutomationRunner.RunContext context=new PortalAutomationRunner.RunContext(run.getId(),portalKey,job.key(),job.title(),idempotencyKey,
                jwt.issue(owner),frontendUrl,apiBaseUrl,account.getDisplayName(),account.getEmail(),trimToNull(request.education()),
                trimToNull(request.experience()),trimToNull(request.coverLetter()),Map.copyOf(request.answers()),resumeFile,version.getOriginalFileName());
        UUID runId=run.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
            @Override public void afterCommit(){executor.execute(()->execute(runId,owner,context));}
        });
        return dto(run);
    }

    @Transactional(readOnly=true)
    public Page<RunResponse> list(UUID owner,Pageable pageable){return runs.findAllByOwnerUserIdOrderByCreatedAtDesc(owner,pageable).map(AutomationRunService::dto);}

    @Transactional(readOnly=true)
    public RunResponse get(UUID owner,UUID runId){return dto(owned(owner,runId));}

    private void execute(UUID runId,UUID owner,PortalAutomationRunner.RunContext context) {
        try {
            transition(runId,()->runs.findById(runId).orElseThrow().start());
            PortalAutomationRunner.RunnerResult result=runner.run(context);
            if(!result.independentlyVerified()||!context.portalKey().equals(result.portalKey())||!context.jobKey().equals(result.jobKey())) {
                transition(runId,()->runs.findById(runId).orElseThrow().finish(AutomationRunStatus.UNVERIFIED,null,"The browser result did not match the expected simulated application."));
                return;
            }
            SimulatedPortalDtos.VerificationResponse verified=portals.verify(owner,result.confirmationId());
            if(!"PERSISTED".equals(verified.status())||!context.portalKey().equals(verified.portalKey())||!context.jobKey().equals(verified.jobKey())) {
                transition(runId,()->runs.findById(runId).orElseThrow().finish(AutomationRunStatus.UNVERIFIED,null,"The confirmation did not match a persisted owner-scoped record."));
                return;
            }
            transition(runId,()->runs.findById(runId).orElseThrow().finish(AutomationRunStatus.VERIFIED_SUCCESS,result.confirmationId(),"Submission persisted and independently verified."));
        } catch(ProcessPortalAutomationRunner.AutomationExecutionException ex) {
            transition(runId,()->runs.findById(runId).orElseThrow().finish(ex.outcomeMayBeUnknown()?AutomationRunStatus.UNVERIFIED:AutomationRunStatus.FAILED,null,
                    ex.outcomeMayBeUnknown()?"The browser run ended before persistence could be independently confirmed.":"The browser workflow failed before submission."));
        } catch(Exception ex) {
            transition(runId,()->runs.findById(runId).orElseThrow().finish(AutomationRunStatus.UNVERIFIED,null,"The browser outcome could not be independently verified."));
        }
    }

    private void transition(UUID id,Runnable update){transactions.executeWithoutResult(status->{update.run();runs.flush();});}

    private String fingerprint(StartRequest request) {
        try {
            var canonical=Map.of("portalKey",request.portalKey(),"jobKey",request.jobKey(),"resumeVersionId",request.resumeVersionId(),
                    "education",request.education()==null?"":request.education().trim(),"experience",request.experience()==null?"":request.experience().trim(),
                    "coverLetter",request.coverLetter()==null?"":request.coverLetter().trim(),"answers",new java.util.TreeMap<>(request.answers()),"consent",Boolean.TRUE.equals(request.consent()));
            byte[] digest=MessageDigest.getInstance("SHA-256").digest(mapper.writeValueAsBytes(canonical));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException|IOException ex) { throw new IllegalStateException("Could not fingerprint the automation request.",ex); }
    }

    private AutomationRun owned(UUID owner,UUID id){return runs.findByIdAndOwnerUserId(id,owner).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Automation run not found."));}

    private void validatePortalFields(String portal,SimulatedPortalDtos.PortalJob job,StartRequest request) {
        if(!Boolean.TRUE.equals(request.consent()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Explicit consent is required before an automation run can submit.");
        Set<String> required=new HashSet<>(job.questions());
        if(request.answers()==null||!request.answers().keySet().equals(required)||request.answers().values().stream().anyMatch(a->a==null||a.isBlank()||a.length()>2000))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Provide an approved answer for every screening question and no extra answers.");
        if(portal.equals("PORTAL_A")&&(request.coverLetter()==null||request.coverLetter().isBlank()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A cover letter is required for this simulated role.");
        if(portal.equals("PORTAL_B")&&(request.education()==null||request.education().isBlank()||request.experience()==null||request.experience().isBlank()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Education and experience are required for this simulated role.");
    }

    private static RunResponse dto(AutomationRun run){return new RunResponse(run.getId(),run.getPortalKey(),run.getJobKey(),run.getStatus(),run.getConfirmationId(),run.getResultMessage(),run.getCreatedAt(),run.getStartedAt(),run.getCompletedAt());}
    private String localUrl(String value){
        URI uri=URI.create(value);String host=uri.getHost();
        if(!"http".equals(uri.getScheme())||host==null||!(host.equalsIgnoreCase("localhost")||host.equals("127.0.0.1")||host.equals("::1")))
            throw new IllegalArgumentException("Automation URLs must use loopback HTTP endpoints.");
        return value;
    }
    private String trimToNull(String value){return value==null||value.isBlank()?null:value.trim();}
}
