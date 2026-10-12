package com.careerpilot.autoapply;

import com.careerpilot.ai.CoverLetterService;
import com.careerpilot.auth.UserAccount;
import com.careerpilot.auth.UserAccountRepository;
import com.careerpilot.jobs.Job;
import com.careerpilot.jobs.JobRepository;
import com.careerpilot.resume.Resume;
import com.careerpilot.resume.ResumeRepository;
import com.careerpilot.resume.ResumeStorage;
import com.careerpilot.resume.ResumeVersion;
import com.careerpilot.resume.ResumeVersionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.careerpilot.autoapply.ApplicationPrepDtos.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AutoApplySessionServiceTest {
    @Test
    void submittedSessionCannotBeEdited() {
        ApplicationPrepSessionRepository sessions = mock(ApplicationPrepSessionRepository.class);
        UUID owner = UUID.randomUUID(), sessionId = UUID.randomUUID();
        ApplicationPrepSession session = mock(ApplicationPrepSession.class);
        when(sessions.findByIdAndOwner_Id(sessionId, owner)).thenReturn(Optional.of(session));
        when(session.getStatus()).thenReturn(ApplicationPrepStatus.SUBMITTED);
        AutoApplySessionService service = new AutoApplySessionService(sessions, mock(ConfirmedApplicationAnswerRepository.class),
                mock(JobRepository.class), mock(ResumeVersionRepository.class), mock(ResumeRepository.class), mock(UserAccountRepository.class),
                mock(AutoApplyPreparationAgent.class), mock(AutoApplySessionResponseReader.class), mock(CoverLetterService.class),
                mock(ResumeStorage.class), new ObjectMapper(), mock(AutoApplyApplicationService.class));

        assertThatThrownBy(() -> service.updateDraft(owner, sessionId, new DraftUpdateRequest("Edit after submission")))
                .isInstanceOf(ResponseStatusException.class);
        verify(session, never()).updateDraft(anyString(), anyString(), any());
    }

    @Test
    void createSavesPreparationAndReturnsTheFetchedJobTitleAndStatus() throws Exception {
        ApplicationPrepSessionRepository sessions = mock(ApplicationPrepSessionRepository.class);
        ConfirmedApplicationAnswerRepository reusable = mock(ConfirmedApplicationAnswerRepository.class);
        JobRepository jobs = mock(JobRepository.class);
        ResumeVersionRepository versions = mock(ResumeVersionRepository.class);
        ResumeRepository resumes = mock(ResumeRepository.class);
        UserAccountRepository users = mock(UserAccountRepository.class);
        AutoApplyPreparationAgent agent = mock(AutoApplyPreparationAgent.class);
        AutoApplySessionResponseReader responseReader = mock(AutoApplySessionResponseReader.class);
        UUID owner = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID resumeId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        Job job = mock(Job.class);
        ResumeVersion version = mock(ResumeVersion.class);
        Resume resume = mock(Resume.class);
        UserAccount user = mock(UserAccount.class);
        ApplicationPrepSession session = mock(ApplicationPrepSession.class);
        when(jobs.findVisibleById(jobId, owner)).thenReturn(Optional.of(job));
        when(versions.findById(versionId)).thenReturn(Optional.of(version));
        when(version.getResumeId()).thenReturn(resumeId);
        when(resumes.findByIdAndUser_Id(resumeId, owner)).thenReturn(Optional.of(resume));
        when(users.findById(owner)).thenReturn(Optional.of(user));
        when(agent.writeAnswers(anyList())).thenReturn("[]");
        when(agent.prepare(eq(owner), eq(jobId), eq(versionId), any(), anyList()))
                .thenReturn(new AutoApplyPreparationAgent.Prepared(null, "Draft", "LOCAL_TEMPLATE", "resume.pdf", true,
                        List.of(), List.of()));
        when(agent.writeMissing(anyList())).thenReturn("[]");
        when(session.getId()).thenReturn(sessionId);
        when(session.getJobId()).thenReturn(jobId);
        when(session.getResumeVersionId()).thenReturn(versionId);
        when(session.getAnswersJson()).thenReturn("[]");
        when(sessions.saveAndFlush(any(ApplicationPrepSession.class))).thenReturn(session);
        when(sessions.findByIdAndOwner_Id(sessionId, owner)).thenReturn(Optional.of(session));
        SessionResponse expected = new SessionResponse(sessionId, jobId, "Junior Java Engineer", "Northstar Labs", "Remote",
                versionId, "resume.pdf", null, null, ApplicationPrepStatus.READY_FOR_REVIEW, "Draft", "LOCAL_TEMPLATE",
                List.of(), List.of(), List.of(), false, null, "Drafts are ready for your review.", null, null);
        when(responseReader.get(owner, sessionId)).thenReturn(expected);

        AutoApplySessionService service = new AutoApplySessionService(sessions, reusable, jobs, versions, resumes, users,
                agent, responseReader, mock(CoverLetterService.class), mock(ResumeStorage.class), new ObjectMapper(), mock(AutoApplyApplicationService.class));
        SessionResponse actual = service.create(owner, new CreateRequest(jobId, versionId, null, "professional", List.of()));

        assertThat(actual.jobTitle()).isEqualTo("Junior Java Engineer");
        assertThat(actual.status()).isEqualTo(ApplicationPrepStatus.READY_FOR_REVIEW);
        verify(sessions, times(2)).saveAndFlush(any(ApplicationPrepSession.class));
        verify(responseReader).get(owner, sessionId);
    }
}
