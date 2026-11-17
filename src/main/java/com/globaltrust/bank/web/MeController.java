package com.globaltrust.bank.web;

import com.globaltrust.bank.dto.MeResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class MeController {

    @GetMapping
    public MeResponse me(Authentication auth) {
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();
        return new MeResponse(auth.getName(), roles);
    }
}
