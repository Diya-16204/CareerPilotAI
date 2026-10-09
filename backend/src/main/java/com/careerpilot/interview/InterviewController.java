package com.careerpilot.interview;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import static com.careerpilot.interview.InterviewDtos.*;

@RestController @Validated @RequestMapping("/api/v1/interviews")
public class InterviewController {
    private final InterviewService service;public InterviewController(InterviewService service){this.service=service;}
    @GetMapping("/questions") public java.util.List<QuestionResponse> questions(@RequestParam(required=false) String topic,@RequestParam(required=false) String difficulty){return service.questions(topic,difficulty);}
    @PostMapping("/sessions") public SessionResponse start(@AuthenticationPrincipal UUID owner,@Valid @RequestBody StartRequest request){return service.start(owner,request);}
    @GetMapping("/sessions") public com.careerpilot.jobs.JobDtos.PageResponse<SessionResponse> history(@AuthenticationPrincipal UUID owner,@RequestParam(defaultValue="0") @Min(0) int page,@RequestParam(defaultValue="20") @Min(1) @Max(100) int size){return com.careerpilot.jobs.JobDtos.map(service.history(owner,PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"startedAt"))),x->x);}
    @GetMapping("/sessions/{id}") public SessionDetail get(@PathVariable UUID id,@AuthenticationPrincipal UUID owner){return service.get(id,owner);}
    @GetMapping("/sessions/{id}/next") public QuestionResponse next(@PathVariable UUID id,@AuthenticationPrincipal UUID owner){return service.next(id,owner);}
    @PostMapping("/sessions/{id}/answers") public AnswerResponse answer(@PathVariable UUID id,@AuthenticationPrincipal UUID owner,@Valid @RequestBody AnswerRequest request){return service.answer(id,owner,request);}
}
