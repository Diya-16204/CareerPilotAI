package com.careerpilot.automation;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

import static com.careerpilot.automation.AutomationRunDtos.*;

@RestController
@Validated
@RequestMapping("/api/v1/automation-runs")
public class AutomationRunController {
    private final AutomationRunService service;
    public AutomationRunController(AutomationRunService service){this.service=service;}

    @PostMapping @ResponseStatus(HttpStatus.ACCEPTED)
    public RunResponse start(@AuthenticationPrincipal UUID owner,@RequestHeader("Idempotency-Key") UUID idempotencyKey,
                             @Valid @RequestBody StartRequest request){return service.start(owner,idempotencyKey,request);}

    @GetMapping
    public com.careerpilot.jobs.JobDtos.PageResponse<RunResponse> list(@AuthenticationPrincipal UUID owner,
            @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){
        return com.careerpilot.jobs.JobDtos.map(service.list(owner,PageRequest.of(Math.max(0,page),Math.min(100,Math.max(1,size)),Sort.by(Sort.Direction.DESC,"createdAt"))),x->x);
    }

    @GetMapping("/{id}")
    public RunResponse get(@AuthenticationPrincipal UUID owner,@PathVariable UUID id){return service.get(owner,id);}
}
