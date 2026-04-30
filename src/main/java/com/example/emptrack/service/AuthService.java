package com.example.emptrack.service;

import com.example.emptrack.dto.request.LoginRequestDTO;
import com.example.emptrack.dto.response.LoginResponseDTO;
import com.example.emptrack.model.User;
import com.example.emptrack.repository.UserRepository;
import com.example.emptrack.config.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginResponseDTO login(LoginRequestDTO request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("auth.user.not.found"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new RuntimeException("auth.password.incorrect");
        }

        String token = jwtTokenUtil.generateToken(user.getUsername());

        return new LoginResponseDTO(
                token,
                "Login successful",
                user.getRole().name()
        );
    }

    public String logout() {
        return "Logged out successfully";
    }
}