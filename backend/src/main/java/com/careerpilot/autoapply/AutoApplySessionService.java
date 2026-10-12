package com.careerpilot.autoapply;

import com.careerpilot.ai.CoverLetterDtos;
import com.careerpilot.ai.CoverLetterService;
import com.careerpilot.auth.UserAccount;
import com.careerpilot.auth.UserAccountRepository;
import com.careerpilot.jobs.Job;
import com.careerpilot.jobs.JobRepository;
import com.careerpilot.resume.ResumeRepository;
import com.careerpilot.resume.ResumeStorage;
import com.careerpilot.resume.ResumeVersion;
import com.careerpilot.resume.ResumeVersionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.careerpilot.autoapply.ApplicationPrepDtos.*;

@Service
public class AutoApplySessionService {
    private static final TypeReference<List<AnswerResponse>> ANSWERS = new TypeReference<>() {};
    private final ApplicationPrepSessionRepository sessions;
    private final ConfirmedApplicationAnswerRepository reusableAnswers;
    private final JobRepository jobs;
    private final ResumeVersionRepository versions;
    private final ResumeRepository resumes;
    private final UserAccountRepository users;
    private final AutoApplyPreparationAgent agent;
    private final AutoApplySessionResponseReader responseReader;
    private final CoverLetterService coverLetters;
    private final ResumeStorage storage;
    private final ObjectMapper mapper;
    private final AutoApplyApplicationService applications;

    public AutoApplySessionService(ApplicationPrepSessionRepository sessions,
                                   ConfirmedApplicationAnswerRepository reusableAnswers,
                                   JobRepository jobs, ResumeVersionRepository versions, ResumeRepository resumes,
                                   UserAccountRepository users, AutoApplyPreparationAgent agent,
                                   AutoApplySessionResponseReader responseReader,
                                   CoverLetterService coverLetters, ResumeStorage storage, ObjectMapper mapper,
                                   AutoApplyApplicationService applications) {
        this.sessions = sessions; this.reusableAnswers = reusableAnswers; this.jobs = jobs;
        this.versions = versions; this.resumes = resumes; this.users = users;
        this.agent = agent; this.responseReader = responseReader; this.coverLetters = coverLetters;
        this.storage = storage; this.mapper = mapper;
        this.applications = applications;
    }

    public SessionResponse create(UUID owner, CreateRequest request) {
        validateQuestions(request.questions());
        String url = validateUrl(request.applicationUrl());
        Job job = jobs.findVisibleById(request.jobId(), owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found."));
        ResumeVersion version = versions.findById(request.resumeVersionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume version not found."));
        resumes.findByIdAndUser_Id(version.getResumeId(), owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume version not found."));
        UserAccount user = users.findById(owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in again to continue."));
        ApplicationPrepSession session = new ApplicationPrepSession(user, job, version, url);
        try {
            List<AnswerResponse> initial = (request.questions() == null ? List.<QuestionRequest>of() : request.questions()).stream()
                    .map(q -> new AnswerResponse(q.key(), q.label(), q.required(), q.consequential(), "", "USER_REQUIRED",
                            "Answer this field yourself or prepare a draft, then confirm it.", "", false, null)).toList();
            session.updateAnswers(agent.writeAnswers(initial), "[]", ApplicationPrepStatus.PREPARING);
        } catch (Exception ex) { throw new IllegalStateException("Could not initialize application questions.", ex); }
        session = sessions.saveAndFlush(session);
        return prepare(owner, session.getId(), request.tone(), request.questions());
    }

    public SessionResponse retryPreparation(UUID owner, UUID id, String tone, List<QuestionRequest> questions) {
        ApplicationPrepSession session = owned(owner, id);
        if (session.getStatus() != ApplicationPrepStatus.FAILED && session.getStatus() != ApplicationPrepStatus.NEEDS_INFORMATION)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This preparation session cannot be retried in its current state.");
        if (questions != null) validateQuestions(questions);
        return prepare(owner, id, tone, questions);
    }

    private SessionResponse prepare(UUID owner, UUID id, String tone, List<QuestionRequest> requestedQuestions) {
        ApplicationPrepSession session = owned(owner, id);
        try {
            List<QuestionRequest> questions = requestedQuestions == null ? decodeQuestions(session.getAnswersJson()) : requestedQuestions;
            List<AnswerResponse> previouslyConfirmed = readAnswers(session.getAnswersJson()).stream()
                    .filter(AnswerResponse::confirmed)
                    .toList();
            AutoApplyPreparationAgent.Prepared prepared = agent.prepare(owner, session.getJobId(), session.getResumeVersionId(), tone, questions);
            List<AnswerResponse> mergedAnswers = prepared.answers().stream().map(answer -> previouslyConfirmed.stream()
                    .filter(previous -> previous.key().equals(answer.key()) && previous.label().equals(answer.label()))
                    .findFirst()
                    .map(previous -> new AnswerResponse(answer.key(), answer.label(), answer.required(), answer.consequential(),
                            previous.confirmedAnswer(), "USER_CONFIRMED", "Previously confirmed by you for this application.",
                            previous.confirmedAnswer(), true, answer.reusableAnswer()))
                    .orElse(answer)).toList();
            List<String> missing = prepared.missingInformation().stream()
                    .filter(item -> !item.startsWith("Answer and confirm: ")).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
            mergedAnswers.stream().filter(answer -> answer.required() && !answer.confirmed())
                    .forEach(answer -> missing.add("Answer and confirm: " + answer.label()));
            ApplicationPrepStatus status = missing.isEmpty() && prepared.coverLetterDraft() != null
                    ? ApplicationPrepStatus.READY_FOR_REVIEW : ApplicationPrepStatus.NEEDS_INFORMATION;
            session.prepared(prepared.coverLetterId(), prepared.coverLetterDraft(), prepared.coverLetterSource(),
                    agent.writeAnswers(mergedAnswers), agent.writeMissing(missing), status,
                    status == ApplicationPrepStatus.READY_FOR_REVIEW ? "Drafts are ready for your review." : "Review missing information, confirm required answers, and make sure the selected resume file is available.");
        } catch (Exception ex) {
            String message = ex instanceof ResponseStatusException response && response.getReason() != null
                    ? response.getReason() : "Preparation could not finish. You can review the job/resume and retry.";
            session.fail(safe(message, 1000));
        }
        sessions.saveAndFlush(session);
        // The preparation may call AI/storage outside a transaction, so the saved entity
        // is detached. The response reader reloads job/resume with a fetch graph and maps
        // them inside its own read-only transaction.
        return responseReader.get(owner, id);
    }

    public List<SessionResponse> list(UUID owner) {
        return responseReader.list(owner);
    }

    public SessionResponse get(UUID owner, UUID id) { return responseReader.get(owner, id); }

    @Transactional
    public SessionResponse updateAnswer(UUID owner, UUID id, String key, AnswerUpdateRequest request) {
        ApplicationPrepSession session = owned(owner, id);
        ensureNotSubmitted(session);
        List<AnswerResponse> answers = readAnswers(session.getAnswersJson());
        int index = findAnswer(answers, key);
        AnswerResponse previous = answers.get(index);
        String answer = request.answer().trim();
        boolean sensitive = previous.consequential() || AutoApplyPreparationAgent.isSensitive(previous.label());
        if (request.rememberForSimilarQuestions() && sensitive)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sensitive or consequential answers cannot be saved for reuse.");
        boolean confirmed = request.confirm();
        String confirmedAnswer = confirmed ? answer : "";
        String source = confirmed ? "USER_CONFIRMED" : "USER_DRAFT";
        answers.set(index, new AnswerResponse(previous.key(), previous.label(), previous.required(), sensitive,
                answer, source, confirmed ? "Confirmed by you for this application." : "Review and confirm this answer before it can be approved.",
                confirmedAnswer, confirmed, previous.reusableAnswer()));
        if (confirmed && request.rememberForSimilarQuestions()) {
            UserAccount user = users.findById(owner).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in again to continue."));
            String hash = AutoApplyPreparationAgent.questionHash(previous.label());
            Job job = jobs.findVisibleById(session.getJobId(), owner)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found."));
            ConfirmedApplicationAnswer known = reusableAnswers.findByOwner_IdAndJob_IdAndQuestionHash(owner, job.getId(), hash)
                    .orElseGet(() -> new ConfirmedApplicationAnswer(user, job, hash, previous.label(), answer));
            known.update(previous.label(), answer);
            reusableAnswers.saveAndFlush(known);
        }
        List<String> missing = refreshedMissing(session, answers);
        ApplicationPrepStatus next = missing.isEmpty() ? ApplicationPrepStatus.READY_FOR_REVIEW : ApplicationPrepStatus.NEEDS_INFORMATION;
        session.updateAnswers(writeAnswers(answers), writeMissing(missing), next);
        return response(sessions.saveAndFlush(session));
    }

    @Transactional
    public SessionResponse updateDraft(UUID owner, UUID id, DraftUpdateRequest request) {
        ApplicationPrepSession session = owned(owner, id);
        ensureNotSubmitted(session);
        List<String> missing = readMissing(session.getMissingInformationJson());
        ApplicationPrepStatus next = missing.isEmpty() ? ApplicationPrepStatus.READY_FOR_REVIEW : ApplicationPrepStatus.NEEDS_INFORMATION;
        session.updateDraft(request.coverLetterDraft().trim(), writeMissing(missing), next);
        UUID coverLetterId = session.getCoverLetterId();
        if (coverLetterId != null) coverLetters.update(coverLetterId, owner, new CoverLetterDtos.UpdateRequest(request.coverLetterDraft().trim()));
        return response(sessions.saveAndFlush(session));
    }

    @Transactional
    public SessionResponse approve(UUID owner, UUID id, ApprovalRequest request) {
        ApplicationPrepSession session = owned(owner, id);
        if (!request.consent()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Explicit approval is required.");
        if (session.getStatus() != ApplicationPrepStatus.READY_FOR_REVIEW)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Resolve all required information before approval.");
        applications.assertRequiredFieldsComplete(session);
        try { storage.resolve(versions.findById(session.getResumeVersionId()).orElseThrow().getStorageKey()); }
        catch (Exception ex) { throw new ResponseStatusException(HttpStatus.CONFLICT, "Resume file is unavailable. Re-upload this resume version before approval."); }
        if (session.getCoverLetterDraft() == null || session.getCoverLetterDraft().isBlank())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Generate or enter a cover letter before approval.");
        session.approve(fingerprint(session));
        return response(sessions.saveAndFlush(session));
    }

    @Transactional
    public SessionResponse continueManually(UUID owner, UUID id) {
        ApplicationPrepSession session = owned(owner, id);
        if (session.getStatus() != ApplicationPrepStatus.APPROVED || session.getApprovedContentHash() == null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Approve the current reviewed draft first.");
        if (!session.getApprovedContentHash().equals(fingerprint(session))) {
            session.updateAnswers(session.getAnswersJson(), session.getMissingInformationJson(), ApplicationPrepStatus.NEEDS_INFORMATION);
            return response(sessions.saveAndFlush(session));
        }
        session.requireManualAction(session.getApplicationUrl() == null
                ? "No supported employer adapter is configured. Open the job listing, apply manually, and verify every answer before sending."
                : "No supported employer adapter is configured. Open the application link and submit manually after reviewing each field.");
        return response(sessions.saveAndFlush(session));
    }

    private ApplicationPrepSession owned(UUID owner, UUID id) {
        return sessions.findByIdAndOwner_Id(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Preparation session not found."));
    }
    private void ensureNotSubmitted(ApplicationPrepSession session) {
        if (session.getStatus() == ApplicationPrepStatus.SUBMITTED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A submitted application session is read-only.");
    }

    private SessionResponse response(ApplicationPrepSession session) {
        try {
            return new SessionResponse(session.getId(), session.getJobId(), session.getJobTitle(), session.getCompany(),
                    session.getLocation(), session.getResumeVersionId(), session.getResumeFileName(), session.getCoverLetterId(),
                    session.getApplicationUrl(), session.getStatus(), session.getCoverLetterDraft(), session.getCoverLetterSource(),
                    readAnswers(session.getAnswersJson()), readMissing(session.getMissingInformationJson()),
                    mapper.readValue(session.getApplicationFieldsJson(), new TypeReference<List<ApplicationFieldRequest>>() {}),
                    session.getApprovedContentHash() != null, session.getApprovedAt(), session.getResultMessage(),
                    session.getCreatedAt(), session.getUpdatedAt());
        } catch (Exception ex) { throw new IllegalStateException("Could not read the saved preparation session.", ex); }
    }

    private List<AnswerResponse> readAnswers(String json) {
        try { return mapper.readValue(json, ANSWERS); }
        catch (Exception ex) { throw new IllegalStateException("Saved application answers could not be read.", ex); }
    }
    private List<String> readMissing(String json) {
        try { return mapper.readValue(json, new TypeReference<>() {}); }
        catch (Exception ex) { throw new IllegalStateException("Saved missing-information list could not be read.", ex); }
    }
    private List<QuestionRequest> decodeQuestions(String answersJson) {
        return readAnswers(answersJson).stream().map(a -> new QuestionRequest(a.key(), a.label(), a.required(), a.consequential())).toList();
    }
    private List<String> refreshedMissing(ApplicationPrepSession session, List<AnswerResponse> answers) {
        List<String> missing = new ArrayList<>(readMissing(session.getMissingInformationJson()).stream()
                .filter(item -> !item.startsWith("Answer and confirm: ")).toList());
        answers.stream().filter(a -> a.required() && (!a.confirmed() || a.confirmedAnswer().isBlank()))
                .forEach(a -> missing.add("Answer and confirm: " + a.label()));
        return List.copyOf(missing);
    }
    private int findAnswer(List<AnswerResponse> answers, String key) {
        for (int i = 0; i < answers.size(); i++) if (answers.get(i).key().equals(key)) return i;
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application question not found.");
    }
    private void validateQuestions(List<QuestionRequest> questions) {
        if (questions == null) return;
        if (questions.size() > 20) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At most 20 application questions can be prepared at once.");
        if (questions.stream().map(QuestionRequest::key).distinct().count() != questions.size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question keys must be unique.");
    }
    private String validateUrl(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            URI uri = URI.create(value.trim());
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null || uri.getUserInfo() != null ||
                    host.equalsIgnoreCase("localhost") || host.endsWith(".local") || host.matches("(?i)\\d{1,3}(\\.\\d{1,3}){3}"))
                throw new IllegalArgumentException();
            return uri.toString();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application links must be public HTTPS URLs without embedded credentials.");
        }
    }
    private String fingerprint(ApplicationPrepSession session) {
        try {
            String content = session.getJobId() + "\n" + session.getResumeVersionId() + "\n" +
                    (session.getApplicationUrl() == null ? "" : session.getApplicationUrl()) + "\n" +
                    (session.getCoverLetterDraft() == null ? "" : session.getCoverLetterDraft()) + "\n" +
                    session.getAnswersJson() + "\n" + session.getApplicationFieldsJson();
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) { throw new IllegalStateException("Could not fingerprint the reviewed application.", ex); }
    }
    private String safe(String value, int max) { return value.length() <= max ? value : value.substring(0, max); }
    private String writeAnswers(List<AnswerResponse> answers) {
        try { return agent.writeAnswers(answers); }
        catch (Exception ex) { throw new IllegalStateException("Could not save application answers.", ex); }
    }
    private String writeMissing(List<String> missing) {
        try { return agent.writeMissing(missing); }
        catch (Exception ex) { throw new IllegalStateException("Could not save missing application information.", ex); }
    }
}
