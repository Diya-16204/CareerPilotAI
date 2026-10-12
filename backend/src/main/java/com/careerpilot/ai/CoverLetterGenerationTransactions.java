package com.careerpilot.ai;

import com.careerpilot.auth.UserAccountRepository;
import com.careerpilot.jobs.Job;
import com.careerpilot.jobs.JobRepository;
import com.careerpilot.resume.ResumeRepository;
import com.careerpilot.resume.ResumeVersion;
import com.careerpilot.resume.ResumeVersionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Loads immutable AI context and persists results in separate short transactions. */
@Service
public class CoverLetterGenerationTransactions {
    private final JobRepository jobs;
    private final ResumeVersionRepository versions;
    private final ResumeRepository resumes;
    private final UserAccountRepository users;
    private final CoverLetterRepository letters;

    public CoverLetterGenerationTransactions(JobRepository jobs, ResumeVersionRepository versions,
                                             ResumeRepository resumes, UserAccountRepository users,
                                             CoverLetterRepository letters) {
        this.jobs = jobs; this.versions = versions; this.resumes = resumes; this.users = users; this.letters = letters;
    }

    @Transactional(readOnly = true)
    public Context load(UUID owner, CoverLetterDtos.GenerateRequest request) {
        ResumeVersion version = versions.findById(request.resumeVersionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume version not found."));
        resumes.findByIdAndUser_Id(version.getResumeId(), owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume version not found."));
        Job job = jobs.findVisibleById(request.jobId(), owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found."));
        String candidate = users.findById(owner).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in again to continue.")).getDisplayName();
        List<String> skills = version.getSkillsText().isBlank() ? List.of() : Arrays.stream(version.getSkillsText().split(","))
                .map(String::trim).filter(value -> !value.isBlank()).distinct().toList();
        return new Context(owner, job.getId(), version.getId(), candidate, job.getTitle(), job.getCompany(),
                job.getDescription(), version.getExtractedText(), skills);
    }

    @Transactional
    public CoverLetterDtos.Response persist(Context context, AiProvider.Draft draft) {
        Job job = jobs.findVisibleById(context.jobId(), context.ownerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found."));
        ResumeVersion version = versions.findById(context.resumeVersionId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume version not found."));
        resumes.findByIdAndUser_Id(version.getResumeId(), context.ownerId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume version not found."));
        var saved = letters.saveAndFlush(new CoverLetter(users.getReferenceById(context.ownerId()), job, version,
                job.getTitle() + " · " + job.getCompany(), draft.body(), draft.source()));
        return new CoverLetterDtos.Response(saved.getId(), saved.getJobId(), saved.getTitle(), saved.getBody(),
                saved.getGenerationSource(), saved.getCreatedAt(), saved.getUpdatedAt());
    }

    public record Context(UUID ownerId, UUID jobId, UUID resumeVersionId, String candidateName,
                          String jobTitle, String company, String jobDescription,
                          String resumeText, List<String> verifiedSkills) {}
}
