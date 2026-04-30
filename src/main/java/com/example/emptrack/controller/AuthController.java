package com.example.emptrack.controller;

import com.example.emptrack.dto.request.LoginRequestDTO;
import com.example.emptrack.dto.response.LoginResponseDTO;
import com.example.emptrack.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.auth.base}")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("${api.auth.login}")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("${api.auth.logout}")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok(authService.logout());
    }
}
