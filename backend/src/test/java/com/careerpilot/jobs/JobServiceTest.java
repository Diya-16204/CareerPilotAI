package com.careerpilot.jobs;

import com.careerpilot.auth.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobServiceTest {
    private final JobRepository jobs = mock(JobRepository.class);
    private final SavedJobRepository saved = mock(SavedJobRepository.class);
    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final JobService service = new JobService(jobs, saved, users);

    @Test
    void savedJobsReturnsOnlyRepositorySavedRowsAndMarksThemSaved() {
        UUID owner = UUID.randomUUID();
        var pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"));
        var job = new Job(null, "Junior Java Engineer", "Northstar Labs", "Remote", "FULL_TIME", "Build APIs", List.of("Java"));
        when(saved.findJobsSavedByUser(owner, pageable)).thenReturn(new PageImpl<>(List.of(job), pageable, 1));

        var result = service.savedJobs(owner, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("Junior Java Engineer");
        assertThat(result.getContent().getFirst().saved()).isTrue();
        verify(saved).findJobsSavedByUser(owner, pageable);
    }
}
