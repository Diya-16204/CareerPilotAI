package com.careerpilot.portal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/simulated-portals")
public class SimulatedPortalController {
    private final SimulatedPortalService service;
    public SimulatedPortalController(SimulatedPortalService service){this.service=service;}

    @GetMapping("/{portal}/jobs")
    public List<SimulatedPortalDtos.PortalJob> jobs(@PathVariable String portal){return service.jobs(portal);}

    @PostMapping("/{portal}/submissions") @ResponseStatus(HttpStatus.CREATED)
    public SimulatedPortalDtos.SubmissionResponse submit(@AuthenticationPrincipal UUID userId,@PathVariable String portal,@RequestHeader("Idempotency-Key") UUID idempotencyKey,@RequestBody SimulatedPortalDtos.SubmissionRequest request){return service.submit(userId,portal,idempotencyKey,request);}

    @GetMapping("/submissions/{confirmationId}")
    public SimulatedPortalDtos.VerificationResponse verify(@AuthenticationPrincipal UUID userId,@PathVariable UUID confirmationId){return service.verify(userId,confirmationId);}
}
