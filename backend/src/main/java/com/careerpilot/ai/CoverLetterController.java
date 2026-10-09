package com.careerpilot.ai;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import static com.careerpilot.ai.CoverLetterDtos.*;

@RestController @Validated @RequestMapping("/api/v1/cover-letters")
public class CoverLetterController {
    private final CoverLetterService service;public CoverLetterController(CoverLetterService service){this.service=service;}
    @PostMapping("/draft") @ResponseStatus(HttpStatus.CREATED) public Response generate(@AuthenticationPrincipal UUID owner,@Valid @RequestBody GenerateRequest request){return service.generate(owner,request);}
    @GetMapping public com.careerpilot.jobs.JobDtos.PageResponse<Response> list(@AuthenticationPrincipal UUID owner,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return com.careerpilot.jobs.JobDtos.map(service.list(owner,PageRequest.of(Math.max(0,page),Math.min(100,Math.max(1,size)),Sort.by(Sort.Direction.DESC,"updatedAt"))),x->x);}
    @GetMapping("/{id}") public Response get(@PathVariable UUID id,@AuthenticationPrincipal UUID owner){return service.get(id,owner);}
    @PutMapping("/{id}") public Response update(@PathVariable UUID id,@AuthenticationPrincipal UUID owner,@Valid @RequestBody UpdateRequest request){return service.update(id,owner,request);}
}
