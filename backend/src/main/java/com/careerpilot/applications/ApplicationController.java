package com.careerpilot.applications;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import static com.careerpilot.applications.ApplicationDtos.*;

@RestController @Validated @RequestMapping("/api/v1")
public class ApplicationController {
    private final ApplicationService service;
    public ApplicationController(ApplicationService service){this.service=service;}
    @PostMapping("/applications") @ResponseStatus(HttpStatus.CREATED)
    public Response create(@Valid @RequestBody CreateRequest request,@AuthenticationPrincipal UUID userId){return service.create(request,userId);}
    @GetMapping("/applications")
    public com.careerpilot.jobs.JobDtos.PageResponse<Response> list(@AuthenticationPrincipal UUID userId,@RequestParam(defaultValue="0") @Min(0) int page,@RequestParam(defaultValue="20") @Min(1) @Max(100) int size){
        return com.careerpilot.jobs.JobDtos.map(service.list(userId,PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"updatedAt"))),x->x);
    }
    @PatchMapping("/applications/{id}/status")
    public Response status(@PathVariable UUID id,@Valid @RequestBody StatusRequest request,@AuthenticationPrincipal UUID userId){return service.updateStatus(id,userId,request);}
    @GetMapping("/analytics/summary")
    public AnalyticsSummary summary(@AuthenticationPrincipal UUID userId){return service.summary(userId);}
}
