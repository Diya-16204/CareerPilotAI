package com.careerpilot.jobs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @Validated @RequestMapping("/api/v1/jobs")
public class JobController {
    private final JobService service;
    public JobController(JobService service){this.service=service;}

    @GetMapping
    public JobDtos.PageResponse<JobDtos.JobResponse> search(@RequestParam(required=false) String q,@RequestParam(required=false) String employmentType,
            @RequestParam(defaultValue="0") @Min(0) int page,@RequestParam(defaultValue="20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue="createdAt,desc") String sort,@AuthenticationPrincipal UUID userId){
        String[] sortParts=sort.split(",",2); String field=switch(sortParts[0]){case "title","company","location","createdAt"->sortParts[0];default->throw new IllegalArgumentException("Unsupported sort field.");};
        Sort.Direction direction=sortParts.length>1&&sortParts[1].equalsIgnoreCase("asc")?Sort.Direction.ASC:Sort.Direction.DESC;
        return JobDtos.map(service.search(q,employmentType,PageRequest.of(page,size,Sort.by(direction,field)),userId),x->x);
    }
    @GetMapping("/{id}") public JobDtos.JobResponse get(@PathVariable UUID id,@AuthenticationPrincipal UUID userId){return service.find(id,userId);}
    @PostMapping("/import") @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public JobDtos.JobResponse importJob(@AuthenticationPrincipal UUID userId,@jakarta.validation.Valid @RequestBody JobDtos.ImportRequest request){return service.importJob(userId,request);}
    @PutMapping("/{id}/saved") public java.util.Map<String,Boolean> save(@PathVariable UUID id,@AuthenticationPrincipal UUID userId){service.save(id,userId);return java.util.Map.of("saved",true);}
    @DeleteMapping("/{id}/saved") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void unsave(@PathVariable UUID id,@AuthenticationPrincipal UUID userId){service.unsave(id,userId);}
}
