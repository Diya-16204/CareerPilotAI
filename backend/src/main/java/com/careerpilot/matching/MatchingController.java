package com.careerpilot.matching;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import static com.careerpilot.matching.MatchDtos.*;

@RestController @RequestMapping("/api/v1/matches")
public class MatchingController {
    private final MatchingService service;
    public MatchingController(MatchingService service){this.service=service;}
    @PostMapping public Response create(@Valid @RequestBody Request request,@AuthenticationPrincipal UUID owner){return service.match(owner,request);}
}
