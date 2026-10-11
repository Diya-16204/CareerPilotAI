package com.careerpilot.matching;

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

class MatchingServiceOwnershipTest {
    private final ResumeVersionRepository versions=mock(ResumeVersionRepository.class);
    private final ResumeRepository resumes=mock(ResumeRepository.class);
    private final JobRepository jobs=mock(JobRepository.class);
    private final ResumeJobMatchRepository matches=mock(ResumeJobMatchRepository.class);
    private final UserAccountRepository users=mock(UserAccountRepository.class);
    private final SkillRepository skills=mock(SkillRepository.class);
    private final MatchSkillRepository matchSkills=mock(MatchSkillRepository.class);
    private final MatchingService service=new MatchingService(versions,resumes,jobs,matches,users,skills,matchSkills);

    @Test void cannotMatchResumeAgainstAnotherUsersPrivateJob(){
        UUID owner=UUID.randomUUID(),jobId=UUID.randomUUID(),resumeId=UUID.randomUUID(),versionId=UUID.randomUUID();
        ResumeVersion version=mock(ResumeVersion.class);
        when(version.getResumeId()).thenReturn(resumeId);
        when(versions.findById(versionId)).thenReturn(Optional.of(version));
        when(resumes.findByIdAndUser_Id(resumeId,owner)).thenReturn(Optional.of(mock(com.careerpilot.resume.Resume.class)));
        when(jobs.findVisibleById(jobId,owner)).thenReturn(Optional.empty());

        assertThatThrownBy(()->service.match(owner,new MatchDtos.Request(versionId,jobId)))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("404 NOT_FOUND");
        verify(jobs).findVisibleById(jobId,owner);
        verify(jobs,never()).findById(jobId);
        verifyNoInteractions(matches,skills,matchSkills);
    }
}
