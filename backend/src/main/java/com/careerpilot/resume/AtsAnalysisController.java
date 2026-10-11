package com.careerpilot.resume;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import static com.careerpilot.resume.AtsDtos.*;

@RestController
@RequestMapping("/api/v1/resumes/{resumeId}/versions/{versionId}/ats-analyses")
public class AtsAnalysisController {
    private final AtsAnalysisService service;
    public AtsAnalysisController(AtsAnalysisService service){this.service=service;}
    @PostMapping public AnalysisResponse analyze(@PathVariable UUID resumeId,@PathVariable UUID versionId,@Valid @RequestBody(required=false) AnalyzeRequest request,@AuthenticationPrincipal UUID owner){return service.analyze(owner,resumeId,versionId,request);}
    @GetMapping public List<AnalysisResponse> history(@PathVariable UUID resumeId,@PathVariable UUID versionId,@AuthenticationPrincipal UUID owner){return service.history(owner,resumeId,versionId);}
}
