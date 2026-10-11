package com.careerpilot.resume;

import com.careerpilot.auth.UserAccountRepository;
import com.careerpilot.jobs.Job;
import com.careerpilot.jobs.JobRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;
import static com.careerpilot.resume.AtsDtos.*;

@Service
public class AtsAnalysisService {
    private final ResumeRepository resumes;
    private final ResumeVersionRepository versions;
    private final AtsAnalysisRepository analyses;
    private final JobRepository jobs;
    private final UserAccountRepository users;
    private final AtsAnalyzer analyzer;
    private final ObjectMapper mapper;

    public AtsAnalysisService(ResumeRepository resumes, ResumeVersionRepository versions, AtsAnalysisRepository analyses,
                              JobRepository jobs, UserAccountRepository users, AtsAnalyzer analyzer, ObjectMapper mapper) {
        this.resumes=resumes;this.versions=versions;this.analyses=analyses;this.jobs=jobs;this.users=users;this.analyzer=analyzer;this.mapper=mapper;
    }

    @Transactional
    public AnalysisResponse analyze(UUID ownerId,UUID resumeId,UUID versionId,AnalyzeRequest request) {
        resumes.findByIdAndUser_Id(resumeId,ownerId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Resume not found."));
        ResumeVersion version=versions.findByIdAndResume_Id(versionId,resumeId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Resume version not found."));
        Job job=null;
        String description=request==null?null:request.jobDescription();
        List<String> jobSkills=List.of();
        if(request!=null&&request.jobId()!=null){
            job=jobs.findVisibleById(request.jobId(),ownerId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Job not found."));
            jobSkills=job.getSkills();
            if(description==null||description.isBlank())description=job.getDescription();
        }
        Result result;
        try { result=analyzer.analyze(version.getExtractedText(),version.getExtractedEmail(),version.getExtractedPhone(),version.getSkillsText(),description,jobSkills); }
        catch(IllegalArgumentException exception){throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,exception.getMessage());}
        try {
            AtsAnalysis saved=analyses.saveAndFlush(new AtsAnalysis(users.getReferenceById(ownerId),version,job,result.overallScore(),result.analysisType(),AtsAnalyzer.FORMULA_VERSION,mapper.writeValueAsString(result)));
            return response(saved,result);
        } catch(JsonProcessingException exception){throw new IllegalStateException("Could not serialize ATS analysis result.",exception);}
    }

    @Transactional(readOnly=true)
    public List<AnalysisResponse> history(UUID ownerId,UUID resumeId,UUID versionId){
        resumes.findByIdAndUser_Id(resumeId,ownerId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Resume not found."));
        versions.findByIdAndResume_Id(versionId,resumeId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Resume version not found."));
        return analyses.findTop20ByUser_IdAndResumeVersion_IdOrderByCreatedAtDesc(ownerId,versionId).stream().map(item->response(item,decode(item.getResultJson()))).toList();
    }

    private Result decode(String json){try{return mapper.readValue(json,Result.class);}catch(JsonProcessingException exception){throw new IllegalStateException("Stored ATS analysis is unreadable.",exception);}}
    private AnalysisResponse response(AtsAnalysis item,Result result){return new AnalysisResponse(item.getId(),item.getResumeVersionId(),item.getJobId(),item.getJobTitle(),item.getScore(),item.getAnalysisType(),result,item.getCreatedAt());}
}
