package com.careerpilot.autoapply;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

import static com.careerpilot.autoapply.ApplicationPrepDtos.*;

@RestController
@RequestMapping("/api/v1/auto-apply/sessions")
public class AutoApplySessionController {
    private final AutoApplySessionService service;
    private final AutoApplyApplicationService applications;
    public AutoApplySessionController(AutoApplySessionService service, AutoApplyApplicationService applications) {
        this.service = service; this.applications = applications;
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse create(@AuthenticationPrincipal UUID owner, @Valid @RequestBody CreateRequest request) {
        return service.create(owner, request);
    }
    @GetMapping public List<SessionResponse> list(@AuthenticationPrincipal UUID owner) { return service.list(owner); }
    @GetMapping("/{id}") public SessionResponse get(@AuthenticationPrincipal UUID owner, @PathVariable UUID id) { return service.get(owner, id); }
    @PutMapping("/{id}/fields") public SessionResponse fields(@AuthenticationPrincipal UUID owner, @PathVariable UUID id,
            @Valid @RequestBody List<@Valid ApplicationFieldRequest> fields) { return applications.saveFields(owner, id, fields); }
    @PostMapping("/{id}/demo-submissions") @ResponseStatus(HttpStatus.CREATED)
    public DemoSubmissionResponse submitDemo(@AuthenticationPrincipal UUID owner, @PathVariable UUID id,
            @Valid @RequestBody DemoSubmissionRequest request) { return applications.submitDemo(owner, id, request); }
    @GetMapping("/{id}/demo-submissions")
    public DemoSubmissionResponse demoSubmission(@AuthenticationPrincipal UUID owner, @PathVariable UUID id) {
        return applications.getDemoSubmission(owner, id);
    }
    @PostMapping("/{id}/prepare") public SessionResponse retry(@AuthenticationPrincipal UUID owner, @PathVariable UUID id,
            @RequestParam(required = false) String tone, @RequestBody(required = false) List<@Valid QuestionRequest> questions) {
        return service.retryPreparation(owner, id, tone, questions);
    }
    @PutMapping("/{id}/answers/{key}") public SessionResponse answer(@AuthenticationPrincipal UUID owner, @PathVariable UUID id,
            @PathVariable String key, @Valid @RequestBody AnswerUpdateRequest request) {
        return service.updateAnswer(owner, id, key, request);
    }
    @PatchMapping("/{id}/draft") public SessionResponse draft(@AuthenticationPrincipal UUID owner, @PathVariable UUID id,
            @Valid @RequestBody DraftUpdateRequest request) { return service.updateDraft(owner, id, request); }
    @PostMapping("/{id}/approve") public SessionResponse approve(@AuthenticationPrincipal UUID owner, @PathVariable UUID id,
            @Valid @RequestBody ApprovalRequest request) { return service.approve(owner, id, request); }
    @PostMapping("/{id}/continue-manually") public SessionResponse manual(@AuthenticationPrincipal UUID owner, @PathVariable UUID id) {
        return service.continueManually(owner, id);
    }
}
