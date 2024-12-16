package com.example.security.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// MANANGER와 ADMIN만 접근 가능
@RestController
@RequestMapping("/api/v1/management")
@RequiredArgsConstructor
public class ManagementController {
    @GetMapping
    public String get() {
        return "GET:: management controller";
    }

    @PostMapping
    public String post() {
        return "POST:: management controller";
    }

    @PutMapping
    public String put() {
        return "PUT:: management controller";
    }

    @DeleteMapping
    public String delete() {
        return "DELETE:: management controller";
    }
}
