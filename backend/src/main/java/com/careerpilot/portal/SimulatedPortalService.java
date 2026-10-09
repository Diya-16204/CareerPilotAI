package com.careerpilot.portal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SimulatedPortalService {
    private final SimulatedPortalSubmissionRepository submissions;
    private final ObjectMapper mapper;
    private static final Map<String,List<SimulatedPortalDtos.PortalJob>> JOBS=Map.of(
        "PORTAL_A",List.of(new SimulatedPortalDtos.PortalJob("standard-product-analyst","Product Analyst","Product","Remote","Help teams understand customer needs and improve product decisions.",List.of("Clear communication","Data-informed decisions"),List.of("Why are you interested in product analysis?"))),
        "PORTAL_B",List.of(new SimulatedPortalDtos.PortalJob("wizard-software-engineer","Software Engineer","Engineering","New York, NY","Build accessible, reliable tools with a collaborative engineering team.",List.of("Java or TypeScript","Testing","Team collaboration"),List.of("What is your highest completed education?","Describe a project you are proud of."))),
        "PORTAL_C",List.of(new SimulatedPortalDtos.PortalJob("specific-ux-researcher","UX Researcher","Design","Remote","Plan research and turn participant feedback into product insights.",List.of("Interview planning","Synthesis","Accessibility"),List.of("How would you recruit participants for a new feature?","What accessibility checks do you include in research?")))
    );
    public SimulatedPortalService(SimulatedPortalSubmissionRepository submissions,ObjectMapper mapper){this.submissions=submissions;this.mapper=mapper;}

    public List<SimulatedPortalDtos.PortalJob> jobs(String portal){return JOBS.getOrDefault(normalize(portal),List.of());}

    @Transactional
    public SimulatedPortalDtos.SubmissionResponse submit(UUID ownerUserId,String portal,UUID idempotencyKey,SimulatedPortalDtos.SubmissionRequest request){
        String key=normalize(portal);
        var job=jobs(key).stream().filter(item->item.key().equals(request.jobKey())).findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Simulated job not found."));
        validate(request,key);
        byte[] resume;
        try{resume=Base64.getDecoder().decode(request.resumeBase64());}catch(IllegalArgumentException ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Resume attachment must be valid base64.");}
        if(resume.length==0||resume.length>5*1024*1024)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Resume must be between 1 byte and 5 MB.");
        String lowerName=request.resumeFileName().toLowerCase();
        boolean pdf=lowerName.endsWith(".pdf")&&resume.length>=5&&resume[0]=='%'&&resume[1]=='P'&&resume[2]=='D'&&resume[3]=='F'&&resume[4]=='-';
        boolean docx=lowerName.endsWith(".docx")&&resume.length>=4&&resume[0]=='P'&&resume[1]=='K'&&resume[2]==3&&resume[3]==4;
        if(!pdf&&!docx)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Resume attachment must be a PDF or DOCX file with a matching file signature.");
        String answers;
        try{answers=mapper.writeValueAsString(request.answers());}catch(JsonProcessingException ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Answers could not be saved.");}
        var existing=submissions.findByOwnerUserIdAndPortalKeyAndIdempotencyKey(ownerUserId,key,idempotencyKey);
        if(existing.isPresent()){
            var prior=existing.get();
            if(!prior.sameSubmission(request.applicantEmail(),request.applicantName(),request.jobKey(),resume,answers))throw new ResponseStatusException(HttpStatus.CONFLICT,"Idempotency key was already used for another submission.");
            return response(prior,true);
        }
        var saved=submissions.saveAndFlush(new SimulatedPortalSubmission(ownerUserId,key,job.key(),job.title(),request.applicantName().trim(),request.applicantEmail().trim(),request.resumeFileName().trim(),resume,answers,idempotencyKey));
        return response(saved,false);
    }

    @Transactional(readOnly=true)
    public SimulatedPortalDtos.VerificationResponse verify(UUID ownerUserId,UUID confirmationId){
        var saved=submissions.findByConfirmationIdAndOwnerUserId(confirmationId,ownerUserId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"No simulated submission has that confirmation ID."));
        return new SimulatedPortalDtos.VerificationResponse(saved.getConfirmationId(),saved.getPortalKey(),saved.getJobKey(),saved.getJobTitle(),saved.getCreatedAt(),"PERSISTED");
    }
    private void validate(SimulatedPortalDtos.SubmissionRequest request,String portal){
        if(request.consent()==null||!request.consent())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Explicit consent is required to submit this simulated application.");
        if(request.applicantName()==null||request.applicantName().isBlank()||request.applicantName().length()>120)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Applicant name is required (maximum 120 characters).");
        if(request.applicantEmail()==null||!request.applicantEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")||request.applicantEmail().length()>254)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A valid email address is required.");
        if(request.resumeFileName()==null||request.resumeFileName().isBlank()||request.resumeFileName().length()>255||request.resumeBase64()==null||request.resumeBase64().length()>7_000_000)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A resume attachment up to 5 MB is required.");
        if(request.answers()==null||request.answers().size()!=jobs(portal).get(0).questions().size()||request.answers().values().stream().anyMatch(value->value==null||value.isBlank()||value.length()>2000))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Answer every screening question with text up to 2,000 characters.");
    }
    private String normalize(String portal){String normalized=portal==null?"":portal.trim().toUpperCase();if(!JOBS.containsKey(normalized))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Unknown simulated portal.");return normalized;}
    private SimulatedPortalDtos.SubmissionResponse response(SimulatedPortalSubmission saved,boolean reused){return new SimulatedPortalDtos.SubmissionResponse(saved.getConfirmationId(),saved.getPortalKey(),saved.getJobKey(),saved.getJobTitle(),saved.getCreatedAt(),reused);}
}
