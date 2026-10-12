package com.careerpilot.applications;

import com.careerpilot.auth.UserAccountRepository;
import com.careerpilot.interview.InterviewAnswerRepository;
import com.careerpilot.interview.InterviewSessionRepository;
import com.careerpilot.jobs.JobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ApplicationServiceTest {
    private final ApplicationRepository applications=mock(ApplicationRepository.class);
    private final ApplicationStatusHistoryRepository history=mock(ApplicationStatusHistoryRepository.class);
    private final UserAccountRepository users=mock(UserAccountRepository.class);
    private final JobRepository jobs=mock(JobRepository.class);
    private final InterviewSessionRepository sessions=mock(InterviewSessionRepository.class);
    private final InterviewAnswerRepository answers=mock(InterviewAnswerRepository.class);
    private final ApplicationService service=new ApplicationService(applications,history,users,jobs,sessions,answers);

    @Test void cannotCreateApplicationForAnotherUsersPrivateJob(){
        UUID owner=UUID.randomUUID(),jobId=UUID.randomUUID();
        when(applications.existsByUser_IdAndJob_Id(owner,jobId)).thenReturn(false);
        when(jobs.findVisibleById(jobId,owner)).thenReturn(Optional.empty());

        assertThatThrownBy(()->service.create(new ApplicationDtos.CreateRequest(jobId,"Engineer","Example",null,null),owner))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("404 NOT_FOUND");
        verify(jobs).findVisibleById(jobId,owner);
        verify(jobs,never()).findById(jobId);
        verify(applications,never()).saveAndFlush(any());
    }

    @Test void createsAppliedTrackerEntryForCompletedAutoApplyDemo() {
        UUID owner = UUID.randomUUID(), jobId = UUID.randomUUID();
        when(applications.findByUser_IdAndJob_Id(owner, jobId)).thenReturn(Optional.empty());
        when(users.getReferenceById(owner)).thenReturn(mock(com.careerpilot.auth.UserAccount.class));
        when(jobs.getReferenceById(jobId)).thenReturn(mock(com.careerpilot.jobs.Job.class));
        when(applications.saveAndFlush(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.recordAutoApplyDemo(owner, jobId, "Software Engineer", "Demo Co");

        var captor = org.mockito.ArgumentCaptor.forClass(Application.class);
        verify(applications).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        verify(history).save(any(ApplicationStatusHistory.class));
    }

    @Test void advancesExistingSavedTrackerEntryToAppliedForCompletedAutoApplyDemo() {
        UUID owner = UUID.randomUUID(), jobId = UUID.randomUUID();
        var existing = mock(Application.class);
        when(applications.findByUser_IdAndJob_Id(owner, jobId)).thenReturn(Optional.of(existing));
        when(existing.getStatus()).thenReturn(ApplicationStatus.SAVED);
        when(applications.saveAndFlush(existing)).thenReturn(existing);

        service.recordAutoApplyDemo(owner, jobId, "Software Engineer", "Demo Co");

        verify(existing).setStatus(ApplicationStatus.APPLIED);
        verify(applications).saveAndFlush(existing);
        verify(history).save(any(ApplicationStatusHistory.class));
    }
}
