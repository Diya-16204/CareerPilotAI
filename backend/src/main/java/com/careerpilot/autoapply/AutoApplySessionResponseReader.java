package com.careerpilot.autoapply;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static com.careerpilot.autoapply.ApplicationPrepDtos.*;

/** Loads and maps session responses while the fetched job and resume associations are available. */
@Service
public class AutoApplySessionResponseReader {
    private static final TypeReference<List<AnswerResponse>> ANSWERS = new TypeReference<>() {};
    private static final TypeReference<List<String>> MISSING = new TypeReference<>() {};
    private static final TypeReference<List<ApplicationFieldRequest>> FIELDS = new TypeReference<>() {};
    private final ApplicationPrepSessionRepository sessions;
    private final ObjectMapper mapper;

    public AutoApplySessionResponseReader(ApplicationPrepSessionRepository sessions, ObjectMapper mapper) {
        this.sessions = sessions;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> list(UUID owner) {
        return sessions.findTop50ByOwner_IdOrderByUpdatedAtDesc(owner).stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public SessionResponse get(UUID owner, UUID id) {
        ApplicationPrepSession session = sessions.findByIdAndOwner_Id(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Preparation session not found."));
        return map(session);
    }

    private SessionResponse map(ApplicationPrepSession session) {
        try {
            return new SessionResponse(session.getId(), session.getJobId(), session.getJobTitle(), session.getCompany(),
                    session.getLocation(), session.getResumeVersionId(), session.getResumeFileName(), session.getCoverLetterId(),
                    session.getApplicationUrl(), session.getStatus(), session.getCoverLetterDraft(), session.getCoverLetterSource(),
                    mapper.readValue(session.getAnswersJson(), ANSWERS), mapper.readValue(session.getMissingInformationJson(), MISSING),
                    mapper.readValue(session.getApplicationFieldsJson(), FIELDS),
                    session.getApprovedContentHash() != null, session.getApprovedAt(), session.getResultMessage(),
                    session.getCreatedAt(), session.getUpdatedAt());
        } catch (Exception ex) {
            throw new IllegalStateException("Could not read the saved preparation session.", ex);
        }
    }
}
