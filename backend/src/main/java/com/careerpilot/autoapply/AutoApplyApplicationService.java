package com.careerpilot.autoapply;

import com.careerpilot.resume.ResumeStorage;
import com.careerpilot.resume.ResumeVersionRepository;
import com.careerpilot.applications.ApplicationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.careerpilot.autoapply.ApplicationPrepDtos.*;

@Service
public class AutoApplyApplicationService {
    private static final TypeReference<List<ApplicationFieldRequest>> FIELDS = new TypeReference<>() {};
    private static final HashSet<String> FIELD_TYPES = new HashSet<>(List.of("TEXT", "EMAIL", "PHONE", "DATE", "TEXTAREA", "SELECT", "RADIO", "CHECKBOX", "NUMBER", "URL"));
    private final ApplicationPrepSessionRepository sessions;
    private final AutoApplyDemoSubmissionRepository submissions;
    private final ResumeVersionRepository versions;
    private final ResumeStorage storage;
    private final ObjectMapper mapper;
    private final AutoApplySessionResponseReader responseReader;
    private final ApplicationService applicationService;

    public AutoApplyApplicationService(ApplicationPrepSessionRepository sessions,
                                       AutoApplyDemoSubmissionRepository submissions,
                                       ResumeVersionRepository versions, ResumeStorage storage,
                                       ObjectMapper mapper, AutoApplySessionResponseReader responseReader,
                                       ApplicationService applicationService) {
        this.sessions = sessions;
        this.submissions = submissions;
        this.versions = versions;
        this.storage = storage;
        this.mapper = mapper;
        this.responseReader = responseReader;
        this.applicationService = applicationService;
    }

    @Transactional
    public SessionResponse saveFields(UUID owner, UUID id, List<ApplicationFieldRequest> fields) {
        ApplicationPrepSession session = owned(owner, id, false);
        if (session.getStatus() == ApplicationPrepStatus.SUBMITTED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A submitted application cannot be edited.");
        List<ApplicationFieldRequest> safe = validateFields(fields, false);
        session.updateApplicationFields(write(safe));
        sessions.saveAndFlush(session);
        return responseReader.get(owner, id);
    }

    public void assertRequiredFieldsComplete(ApplicationPrepSession session) {
        List<ApplicationFieldRequest> fields;
        try { fields = mapper.readValue(session.getApplicationFieldsJson(), FIELDS); }
        catch (Exception ex) { throw new IllegalStateException("Saved application fields could not be read.", ex); }
        List<String> missing = fields.stream().filter(field -> field.required() && !valid(field))
                .map(ApplicationFieldRequest::label).toList();
        if (!missing.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Complete required application fields: " + String.join(", ", missing));
    }

    @Transactional
    public DemoSubmissionResponse submitDemo(UUID owner, UUID id, DemoSubmissionRequest request) {
        ApplicationPrepSession session = owned(owner, id, true);
        var previous = submissions.findBySessionIdAndOwnerId(id, owner);
        if (previous.isPresent()) {
            applicationService.recordAutoApplyDemo(owner, session.getJobId(), session.getJobTitle(), session.getCompany());
            return response(previous.get(), true);
        }
        if (request == null || !request.consent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confirm the fictional demo submission before continuing.");
        if (session.getStatus() != ApplicationPrepStatus.APPROVED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review and approve this exact application before submitting it to the demo portal.");
        List<ApplicationFieldRequest> fields = validateFields(request.fields(), true);
        List<String> missing = fields.stream().filter(field -> field.required() && !valid(field))
                .map(ApplicationFieldRequest::label).toList();
        if (!missing.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Complete required application fields: " + String.join(", ", missing));
        var version = versions.findById(session.getResumeVersionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "The selected resume version is unavailable."));
        try { storage.resolve(version.getStorageKey()); }
        catch (IOException ex) { throw new ResponseStatusException(HttpStatus.CONFLICT, "The selected resume file is unavailable. Re-upload it before applying."); }

        String fieldsJson = write(fields);
        session.updateApplicationFields(fieldsJson);
        session.submitted("Application recorded by the CareerPilot fictional demo portal. No real employer was contacted.");
        sessions.saveAndFlush(session);
        AutoApplyDemoSubmission saved = submissions.saveAndFlush(new AutoApplyDemoSubmission(owner, id,
                session.getJobTitle(), session.getCompany(), version.getOriginalFileName(),
                session.getCoverLetterDraft() == null ? "" : session.getCoverLetterDraft(), session.getApplicationUrl(), fieldsJson));
        applicationService.recordAutoApplyDemo(owner, session.getJobId(), session.getJobTitle(), session.getCompany());
        return response(saved, false);
    }

    @Transactional(readOnly = true)
    public DemoSubmissionResponse getDemoSubmission(UUID owner, UUID id) {
        owned(owner, id, false);
        AutoApplyDemoSubmission saved = submissions.findBySessionIdAndOwnerId(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No demo submission exists for this session."));
        return response(saved, true);
    }

    private ApplicationPrepSession owned(UUID owner, UUID id, boolean lock) {
        return (lock ? sessions.lockByIdAndOwner_Id(id, owner) : sessions.findByIdAndOwner_Id(id, owner))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Preparation session not found."));
    }

    private List<ApplicationFieldRequest> validateFields(List<ApplicationFieldRequest> fields, boolean submission) {
        if (fields == null) {
            if (submission) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application fields are required.");
            return List.of();
        }
        if (fields.size() > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At most 100 application fields are supported.");
        HashSet<String> keys = new HashSet<>();
        for (ApplicationFieldRequest field : fields) {
            if (field == null || field.key() == null || field.key().isBlank() || field.label() == null || field.label().isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Every application field needs a key and label.");
            if (!keys.add(field.key())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application field keys must be unique.");
            String type = field.type() == null ? "" : field.type().toUpperCase(Locale.ROOT);
            if (!FIELD_TYPES.contains(type)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An application field uses an unsupported input type.");
            if ((type.equals("SELECT") || type.equals("RADIO")) && field.options() != null && !field.options().isEmpty() &&
                    field.value() != null && !field.value().isBlank() && !field.options().contains(field.value()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose one of the available options for " + field.label() + ".");
            if (submission && field.required() && !valid(field))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Complete required application field: " + field.label());
        }
        return List.copyOf(fields);
    }

    private boolean valid(ApplicationFieldRequest field) {
        String value = field.value() == null ? "" : field.value().trim();
        if (value.isEmpty()) return false;
        return switch (field.type().toUpperCase(Locale.ROOT)) {
            case "EMAIL" -> value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
            case "DATE" -> isDate(value);
            case "NUMBER" -> value.matches("-?\\d+(\\.\\d+)?");
            case "URL" -> value.matches("https?://\\S+");
            case "SELECT", "RADIO" -> field.options() == null || field.options().isEmpty() || field.options().contains(value);
            case "CHECKBOX" -> value.equalsIgnoreCase("true");
            default -> true;
        };
    }

    private boolean isDate(String value) {
        try { LocalDate.parse(value); return true; }
        catch (DateTimeParseException ex) { return false; }
    }

    private String write(List<ApplicationFieldRequest> fields) {
        try { return mapper.writeValueAsString(fields); }
        catch (Exception ex) { throw new IllegalStateException("Application fields could not be saved.", ex); }
    }

    private DemoSubmissionResponse response(AutoApplyDemoSubmission submission, boolean alreadySubmitted) {
        return new DemoSubmissionResponse(submission.getId(), submission.getJobTitle(), submission.getCompany(),
                submission.getSubmittedAt(), alreadySubmitted);
    }
}
