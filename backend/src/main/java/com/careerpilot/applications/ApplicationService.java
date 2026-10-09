package com.careerpilot.applications;

import com.careerpilot.auth.UserAccountRepository;
import com.careerpilot.jobs.Job;
import com.careerpilot.jobs.JobRepository;
import com.careerpilot.interview.InterviewAnswerRepository;
import com.careerpilot.interview.InterviewSessionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

import static com.careerpilot.applications.ApplicationDtos.*;

@Service
public class ApplicationService {
    private final ApplicationRepository applications; private final ApplicationStatusHistoryRepository history;
    private final UserAccountRepository users; private final JobRepository jobs;private final InterviewSessionRepository interviewSessions;private final InterviewAnswerRepository interviewAnswers;
    public ApplicationService(ApplicationRepository applications,ApplicationStatusHistoryRepository history,UserAccountRepository users,JobRepository jobs,InterviewSessionRepository interviewSessions,InterviewAnswerRepository interviewAnswers){this.applications=applications;this.history=history;this.users=users;this.jobs=jobs;this.interviewSessions=interviewSessions;this.interviewAnswers=interviewAnswers;}

    @Transactional
    public Response create(CreateRequest request,UUID userId){
        if(request.jobId()!=null&&applications.existsByUser_IdAndJobId(userId,request.jobId()))throw new ResponseStatusException(HttpStatus.CONFLICT,"This job is already in your application tracker.");
        Job job=request.jobId()==null?null:jobs.findById(request.jobId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Job not found."));
        String title=request.title().trim(),company=request.company().trim();
        ApplicationStatus status=request.status()==null?ApplicationStatus.APPLIED:request.status();
        Application saved=applications.saveAndFlush(new Application(users.getReferenceById(userId),job,title,company,status));
        history.save(new ApplicationStatusHistory(saved,null,status,"Application created"));
        return dto(saved);
    }

    @Transactional(readOnly=true)
    public Page<Response> list(UUID userId,Pageable pageable){return applications.findAllByUser_IdOrderByUpdatedAtDesc(userId,pageable).map(this::dto);}

    @Transactional
    public Response updateStatus(UUID id,UUID userId,StatusRequest request){
        Application app=applications.findById(id).filter(item->item.getUserId().equals(userId)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Application not found."));
        ApplicationStatus previous=app.getStatus(); app.setStatus(request.status());
        history.save(new ApplicationStatusHistory(app,previous,request.status(),request.note()));
        applications.saveAndFlush(app);
        return dto(app);
    }

    @Transactional(readOnly=true)
    public AnalyticsSummary summary(UUID userId){
        Map<ApplicationStatus,Long> byStatus=new EnumMap<>(ApplicationStatus.class);
        for(ApplicationStatus status:ApplicationStatus.values())byStatus.put(status,applications.countByUser_IdAndStatus(userId,status));
        List<Activity> recent=applications.findTop5ByUser_IdOrderByUpdatedAtDesc(userId).stream().map(a->new Activity(a.getId(),a.getTitle()+" · "+a.getCompany()+" · "+a.getStatus().name().toLowerCase(Locale.ROOT),a.getUpdatedAt())).toList();
        return new AnalyticsSummary(applications.countByUser_Id(userId),byStatus.get(ApplicationStatus.INTERVIEW),byStatus.get(ApplicationStatus.OFFER),interviewSessions.countByUser_Id(userId),interviewAnswers.countByOwner(userId),byStatus,recent);
    }
    private Response dto(Application application){return new Response(application.getId(),application.getTitle(),application.getCompany(),application.getStatus(),application.getAppliedAt(),application.getUpdatedAt());}
}
