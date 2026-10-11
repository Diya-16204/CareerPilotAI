package com.careerpilot.ai;

import com.careerpilot.auth.UserAccountRepository;
import com.careerpilot.jobs.JobRepository;
import com.careerpilot.resume.ResumeRepository;
import com.careerpilot.resume.ResumeVersion;
import com.careerpilot.resume.ResumeVersionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CoverLetterServiceTest {
    private final CoverLetterRepository letters=mock(CoverLetterRepository.class);
    private final JobRepository jobs=mock(JobRepository.class);
    private final ResumeVersionRepository versions=mock(ResumeVersionRepository.class);
    private final ResumeRepository resumes=mock(ResumeRepository.class);
    private final UserAccountRepository users=mock(UserAccountRepository.class);
    private final AiProvider provider=mock(AiProvider.class);
    private final CoverLetterService service=new CoverLetterService(letters,jobs,versions,resumes,users,provider);

    @Test void cannotGenerateLetterForAnotherUsersPrivateJob(){
        UUID owner=UUID.randomUUID(),jobId=UUID.randomUUID(),resumeId=UUID.randomUUID(),versionId=UUID.randomUUID();
        ResumeVersion version=mock(ResumeVersion.class);
        when(version.getResumeId()).thenReturn(resumeId);
        when(versions.findById(versionId)).thenReturn(Optional.of(version));
        when(resumes.findByIdAndUser_Id(resumeId,owner)).thenReturn(Optional.of(mock(com.careerpilot.resume.Resume.class)));
        when(jobs.findVisibleById(jobId,owner)).thenReturn(Optional.empty());

        assertThatThrownBy(()->service.generate(owner,new CoverLetterDtos.GenerateRequest(jobId,versionId,"professional")))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("404 NOT_FOUND");
        verify(jobs).findVisibleById(jobId,owner);
        verify(jobs,never()).findById(jobId);
        verifyNoInteractions(provider);
        verify(letters,never()).saveAndFlush(any());
    }
}
