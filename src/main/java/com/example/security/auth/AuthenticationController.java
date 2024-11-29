package com.example.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register (
      @RequestBody RegisterRequest request
    ) {
        return service.register(request);
    }

    // 인증
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate (
            @RequestBody AuthenticationRequest request
    ) {
        return service.authenticate(request);
    }

    // 재발급
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken (
        HttpServletRequest request
    ) {
        return service.refreshToken(request);
    }
}
