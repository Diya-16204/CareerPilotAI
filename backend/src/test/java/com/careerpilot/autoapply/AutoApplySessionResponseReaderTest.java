package com.careerpilot.autoapply;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AutoApplySessionResponseReaderTest {
    @Test
    void responseLoadsAndMapsTheJobAndResumeFieldsInsideReadOnlyTransaction() throws NoSuchMethodException {
        ApplicationPrepSessionRepository repository = mock(ApplicationPrepSessionRepository.class);
        ApplicationPrepSession session = mock(ApplicationPrepSession.class);
        UUID owner = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        when(repository.findByIdAndOwner_Id(sessionId, owner)).thenReturn(Optional.of(session));
        when(session.getId()).thenReturn(sessionId);
        when(session.getJobId()).thenReturn(jobId);
        when(session.getJobTitle()).thenReturn("Junior Java Engineer");
        when(session.getCompany()).thenReturn("Northstar Labs");
        when(session.getLocation()).thenReturn("Remote");
        when(session.getResumeVersionId()).thenReturn(versionId);
        when(session.getResumeFileName()).thenReturn("resume.pdf");
        when(session.getStatus()).thenReturn(ApplicationPrepStatus.READY_FOR_REVIEW);
        when(session.getAnswersJson()).thenReturn("[]");
        when(session.getMissingInformationJson()).thenReturn("[]");
        when(session.getApplicationFieldsJson()).thenReturn("[{\"key\":\"email\",\"label\":\"Email address\",\"type\":\"EMAIL\",\"required\":true,\"options\":[],\"value\":\"candidate@example.com\"}]");
        when(session.getCreatedAt()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
        when(session.getUpdatedAt()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));

        AutoApplySessionResponseReader reader = new AutoApplySessionResponseReader(repository, new ObjectMapper());
        var response = reader.get(owner, sessionId);

        assertThat(response.jobTitle()).isEqualTo("Junior Java Engineer");
        assertThat(response.company()).isEqualTo("Northstar Labs");
        assertThat(response.resumeFileName()).isEqualTo("resume.pdf");
        assertThat(response.status()).isEqualTo(ApplicationPrepStatus.READY_FOR_REVIEW);
        assertThat(response.applicationFields()).singleElement().satisfies(field -> {
            assertThat(field.label()).isEqualTo("Email address");
            assertThat(field.value()).isEqualTo("candidate@example.com");
        });
        assertThat(AutoApplySessionResponseReader.class.getMethod("get", UUID.class, UUID.class)
                .getAnnotation(Transactional.class).readOnly()).isTrue();
        verify(repository).findByIdAndOwner_Id(sessionId, owner);
    }

    @Test
    void listUsesFetchGraphRepositoryQueryAndMapsEachSession() {
        ApplicationPrepSessionRepository repository = mock(ApplicationPrepSessionRepository.class);
        ApplicationPrepSession session = mock(ApplicationPrepSession.class);
        UUID owner = UUID.randomUUID();
        when(repository.findTop50ByOwner_IdOrderByUpdatedAtDesc(owner)).thenReturn(List.of(session));
        when(session.getAnswersJson()).thenReturn("[]");
        when(session.getMissingInformationJson()).thenReturn("[]");
        when(session.getApplicationFieldsJson()).thenReturn("[]");
        when(session.getStatus()).thenReturn(ApplicationPrepStatus.NEEDS_INFORMATION);

        var response = new AutoApplySessionResponseReader(repository, new ObjectMapper()).list(owner);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().status()).isEqualTo(ApplicationPrepStatus.NEEDS_INFORMATION);
        verify(repository).findTop50ByOwner_IdOrderByUpdatedAtDesc(owner);
    }
}
