package com.careerpilot.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import static com.careerpilot.user.ProfileDtos.*;

@RestController @RequestMapping("/api/v1/users/me")
public class ProfileController {
    private final ProfileService service;public ProfileController(ProfileService service){this.service=service;}
    @GetMapping("/profile") public Response get(@AuthenticationPrincipal UUID owner){return service.get(owner);}
    @PatchMapping("/profile") public Response update(@AuthenticationPrincipal UUID owner,@Valid @RequestBody UpdateRequest request){return service.update(owner,request);}
    @PostMapping("/password") @ResponseStatus(HttpStatus.NO_CONTENT) public void password(@AuthenticationPrincipal UUID owner,@Valid @RequestBody PasswordRequest request){service.changePassword(owner,request);}
}
