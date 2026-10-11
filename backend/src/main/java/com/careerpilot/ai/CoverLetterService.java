package com.careerpilot.ai;

import com.careerpilot.auth.UserAccountRepository;
import com.careerpilot.jobs.Job;
import com.careerpilot.jobs.JobRepository;
import com.careerpilot.resume.ResumeRepository;
import com.careerpilot.resume.ResumeVersion;
import com.careerpilot.resume.ResumeVersionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static com.careerpilot.ai.CoverLetterDtos.*;

@Service
public class CoverLetterService {
    private final CoverLetterRepository letters;private final JobRepository jobs;private final ResumeVersionRepository versions;private final ResumeRepository resumes;private final UserAccountRepository users;private final AiProvider provider;
    public CoverLetterService(CoverLetterRepository letters,JobRepository jobs,ResumeVersionRepository versions,ResumeRepository resumes,UserAccountRepository users,AiProvider provider){this.letters=letters;this.jobs=jobs;this.versions=versions;this.resumes=resumes;this.users=users;this.provider=provider;}
    @Transactional
    public Response generate(UUID owner,GenerateRequest request){
        ResumeVersion version=versions.findById(request.resumeVersionId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Resume version not found."));
        if(resumes.findByIdAndUser_Id(version.getResumeId(),owner).isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Resume version not found.");
        Job job=jobs.findVisibleById(request.jobId(),owner).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Job not found."));
        List<String> verified=version.getSkillsText().isBlank()?List.of():Arrays.stream(version.getSkillsText().split(",")).map(String::trim).filter(s->!s.isBlank()).distinct().toList();
        String candidate=users.findById(owner).orElseThrow().getDisplayName();
        AiProvider.Draft draft=provider.generateCoverLetter(candidate,job.getTitle(),job.getCompany(),job.getDescription(),version.getExtractedText(),verified,request.tone());
        CoverLetter saved=letters.saveAndFlush(new CoverLetter(users.getReferenceById(owner),job,version,job.getTitle()+" · "+job.getCompany(),draft.body(),draft.source()));return dto(saved);
    }
    @Transactional(readOnly=true) public Page<Response> list(UUID owner,Pageable pageable){return letters.findAllByUser_IdOrderByUpdatedAtDesc(owner,pageable).map(this::dto);}
    @Transactional public Response update(UUID id,UUID owner,UpdateRequest request){CoverLetter letter=owned(id,owner);letter.update(request.body().trim());letters.saveAndFlush(letter);return dto(letter);}
    @Transactional(readOnly=true) public Response get(UUID id,UUID owner){return dto(owned(id,owner));}
    private CoverLetter owned(UUID id,UUID owner){return letters.findByIdAndUser_Id(id,owner).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Cover letter not found."));}
    private Response dto(CoverLetter letter){return new Response(letter.getId(),letter.getJobId(),letter.getTitle(),letter.getBody(),letter.getGenerationSource(),letter.getCreatedAt(),letter.getUpdatedAt());}
}
