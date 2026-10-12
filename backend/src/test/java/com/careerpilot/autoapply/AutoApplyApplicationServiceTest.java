package com.careerpilot.autoapply;

import com.careerpilot.applications.ApplicationService;
import com.careerpilot.resume.ResumeStorage;
import com.careerpilot.resume.ResumeVersion;
import com.careerpilot.resume.ResumeVersionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.careerpilot.autoapply.ApplicationPrepDtos.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AutoApplyApplicationServiceTest {
    @Test
    void savesCapturedFieldsAndReturnsThePersistedSessionRepresentation() {
        var sessions = mock(ApplicationPrepSessionRepository.class);
        var submissions = mock(AutoApplyDemoSubmissionRepository.class);
        var versions = mock(ResumeVersionRepository.class);
        var reader = mock(AutoApplySessionResponseReader.class);
        UUID owner = UUID.randomUUID(), id = UUID.randomUUID();
        var session = mock(ApplicationPrepSession.class);
        var fields = List.of(new ApplicationFieldRequest("work_email", "Work email", "EMAIL", true, List.of(), "person@example.com"));
        var expected = mock(SessionResponse.class);
        when(sessions.findByIdAndOwner_Id(id, owner)).thenReturn(Optional.of(session));
        when(sessions.saveAndFlush(session)).thenReturn(session);
        when(reader.get(owner, id)).thenReturn(expected);

        var service = new AutoApplyApplicationService(sessions, submissions, versions, mock(ResumeStorage.class), new ObjectMapper(), reader, mock(ApplicationService.class));
        assertThat(service.saveFields(owner, id, fields)).isSameAs(expected);
        verify(session).updateApplicationFields("[{\"key\":\"work_email\",\"label\":\"Work email\",\"type\":\"EMAIL\",\"required\":true,\"options\":[],\"value\":\"person@example.com\"}]");
        verify(reader).get(owner, id);
    }

    @Test
    void rejectsInvalidRequiredFieldsBeforeSavingOrSubmitting() {
        UUID owner = UUID.randomUUID(), id = UUID.randomUUID();
        var sessions = mock(ApplicationPrepSessionRepository.class);
        var submissions = mock(AutoApplyDemoSubmissionRepository.class);
        var session = mock(ApplicationPrepSession.class);
        when(sessions.lockByIdAndOwner_Id(id, owner)).thenReturn(Optional.of(session));
        when(session.getStatus()).thenReturn(ApplicationPrepStatus.APPROVED);
        when(submissions.findBySessionIdAndOwnerId(id, owner)).thenReturn(Optional.empty());
        var service = new AutoApplyApplicationService(sessions, submissions,
                mock(ResumeVersionRepository.class), mock(ResumeStorage.class), new ObjectMapper(), mock(AutoApplySessionResponseReader.class), mock(ApplicationService.class));
        var invalid = List.of(new ApplicationFieldRequest("email", "Email", "EMAIL", true, List.of(), "not-an-email"));
        assertThatThrownBy(() -> service.submitDemo(owner, id, new DemoSubmissionRequest(true, invalid)))
                .isInstanceOf(ResponseStatusException.class);
        verify(submissions, never()).saveAndFlush(any());
    }

    @Test
    void demoSubmissionIsPersistedAndRepeatedRequestReturnsSameReference() throws Exception {
        var sessions = mock(ApplicationPrepSessionRepository.class);
        var submissions = mock(AutoApplyDemoSubmissionRepository.class);
        var versions = mock(ResumeVersionRepository.class);
        var storage = mock(ResumeStorage.class);
        var applicationService = mock(ApplicationService.class);
        UUID owner = UUID.randomUUID(), id = UUID.randomUUID(), versionId = UUID.randomUUID(), reference = UUID.randomUUID();
        var session = mock(ApplicationPrepSession.class);
        var version = mock(ResumeVersion.class);
        var saved = mock(AutoApplyDemoSubmission.class);
        var now = Instant.parse("2026-02-01T00:00:00Z");
        var fields = List.of(new ApplicationFieldRequest("email", "Email", "EMAIL", true, List.of(), "person@example.com"));
        when(sessions.lockByIdAndOwner_Id(id, owner)).thenReturn(Optional.of(session));
        when(submissions.findBySessionIdAndOwnerId(id, owner)).thenReturn(Optional.empty()).thenReturn(Optional.of(saved));
        when(session.getStatus()).thenReturn(ApplicationPrepStatus.APPROVED);
        when(session.getCoverLetterDraft()).thenReturn("Reviewed letter");
        when(session.getResumeVersionId()).thenReturn(versionId);
        when(session.getJobTitle()).thenReturn("Software Engineer");
        when(session.getCompany()).thenReturn("Demo Company");
        when(session.getJobId()).thenReturn(UUID.randomUUID());
        when(versions.findById(versionId)).thenReturn(Optional.of(version));
        when(version.getStorageKey()).thenReturn("resume-key");
        when(version.getOriginalFileName()).thenReturn("resume.pdf");
        when(storage.resolve("resume-key")).thenReturn(Path.of("resume.pdf"));
        when(submissions.saveAndFlush(any(AutoApplyDemoSubmission.class))).thenReturn(saved);
        when(saved.getId()).thenReturn(reference);
        when(saved.getJobTitle()).thenReturn("Software Engineer");
        when(saved.getCompany()).thenReturn("Demo Company");
        when(saved.getSubmittedAt()).thenReturn(now);

        var service = new AutoApplyApplicationService(sessions, submissions, versions, storage, new ObjectMapper(), mock(AutoApplySessionResponseReader.class), applicationService);
        var first = service.submitDemo(owner, id, new DemoSubmissionRequest(true, fields));
        var second = service.submitDemo(owner, id, new DemoSubmissionRequest(true, fields));

        assertThat(first.reference()).isEqualTo(reference);
        assertThat(first.jobTitle()).isEqualTo("Software Engineer");
        assertThat(first.alreadySubmitted()).isFalse();
        assertThat(second.reference()).isEqualTo(reference);
        assertThat(second.alreadySubmitted()).isTrue();
        verify(session).submitted(org.mockito.ArgumentMatchers.contains("No real employer was contacted"));
        verify(submissions, times(1)).saveAndFlush(any(AutoApplyDemoSubmission.class));
        verify(applicationService, times(2)).recordAutoApplyDemo(owner, session.getJobId(), "Software Engineer", "Demo Company");
    }

    @Test
    void sessionLookupIsScopedToTheAuthenticatedOwner() {
        UUID owner = UUID.randomUUID(), id = UUID.randomUUID();
        var sessions = mock(ApplicationPrepSessionRepository.class);
        when(sessions.findByIdAndOwner_Id(id, owner)).thenReturn(Optional.empty());
        var service = new AutoApplyApplicationService(sessions, mock(AutoApplyDemoSubmissionRepository.class), mock(ResumeVersionRepository.class),
                mock(ResumeStorage.class), new ObjectMapper(), mock(AutoApplySessionResponseReader.class), mock(ApplicationService.class));
        assertThatThrownBy(() -> service.saveFields(owner, id, List.of())).isInstanceOf(ResponseStatusException.class);
        verify(sessions).findByIdAndOwner_Id(id, owner);
    }
}
