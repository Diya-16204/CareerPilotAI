package com.careerpilot.jobs;

import com.careerpilot.auth.UserAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@Service
public class JobService {
    private final JobRepository jobs; private final SavedJobRepository saved; private final UserAccountRepository users;
    public JobService(JobRepository jobs,SavedJobRepository saved,UserAccountRepository users){this.jobs=jobs;this.saved=saved;this.users=users;}

    @Transactional(readOnly=true)
    public Page<JobDtos.JobResponse> search(String q,String type,Pageable pageable,UUID userId){
        String query=q==null||q.isBlank()?"":q.trim(); String employment=type==null||type.isBlank()?null:type.trim().toUpperCase();
        return jobs.search(userId,query,employment,pageable).map(job->dto(job,userId));
    }
    @Transactional(readOnly=true)
    public Page<JobDtos.JobResponse> savedJobs(UUID userId,Pageable pageable){
        return saved.findJobsSavedByUser(userId,pageable).map(job->dto(job,true));
    }
    @Transactional(readOnly=true)
    public JobDtos.JobResponse find(UUID id,UUID userId){return dto(jobs.findVisibleById(id,userId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Job not found.")),userId);}
    @Transactional
    public void save(UUID jobId,UUID userId){
        Job job=jobs.findVisibleById(jobId,userId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Job not found."));
        if(!saved.existsByUser_IdAndJob_Id(userId,jobId)) saved.save(new SavedJob(users.getReferenceById(userId),job));
    }
    @Transactional
    public void unsave(UUID jobId,UUID userId){saved.deleteByUser_IdAndJob_Id(userId,jobId);}
    @Transactional
    public JobDtos.JobResponse importJob(UUID owner,JobDtos.ImportRequest request){
        String title=request.title().trim(),company=request.company().trim();
        if(jobs.existsByOwner_IdAndTitleIgnoreCaseAndCompanyIgnoreCase(owner,title,company))throw new ResponseStatusException(HttpStatus.CONFLICT,"This imported job is already in your job list.");
        Job job=jobs.saveAndFlush(new Job(users.getReferenceById(owner),title,company,request.location().trim(),request.employmentType().toUpperCase(),request.description().trim(),request.skills()));
        return dto(job,owner);
    }
    private JobDtos.JobResponse dto(Job job,UUID userId){return new JobDtos.JobResponse(job.getId(),job.getTitle(),job.getCompany(),job.getLocation(),job.getEmploymentType(),job.getDescription(),job.getSkills(),job.getSource(),job.isSample(),userId!=null&&saved.existsByUser_IdAndJob_Id(userId,job.getId()));}
    private JobDtos.JobResponse dto(Job job,boolean isSaved){return new JobDtos.JobResponse(job.getId(),job.getTitle(),job.getCompany(),job.getLocation(),job.getEmploymentType(),job.getDescription(),job.getSkills(),job.getSource(),job.isSample(),isSaved);}
}
