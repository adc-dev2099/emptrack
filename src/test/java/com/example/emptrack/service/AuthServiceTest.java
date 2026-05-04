package com.example.emptrack.service;

import com.example.emptrack.config.JwtTokenUtil;
import com.example.emptrack.dto.request.LoginRequestDTO;
import com.example.emptrack.dto.response.LoginResponseDTO;
import com.example.emptrack.model.User;
import com.example.emptrack.repository.UserRepository;
import com.example.emptrack.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setFirstName("Admin");
        sampleUser.setLastName("User");
        sampleUser.setUsername("admin.user");
        sampleUser.setPassword("hashed_password");
        sampleUser.setRole(User.Role.ADMIN);
        sampleUser.setActive(true);
    }

    // ───── login ─────

    @Test
    void login_success_returnsTokenAndRole() {
        LoginRequestDTO request = new LoginRequestDTO("admin.user", "password123");

        when(userRepository.findByUsername("admin.user")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(jwtTokenUtil.generateToken("admin.user")).thenReturn("mock.jwt.token");

        LoginResponseDTO result = authService.login(request);

        assertNotNull(result);
        assertEquals("mock.jwt.token", result.getToken());
        assertEquals("ADMIN", result.getRole());
        assertEquals("Login successful", result.getMessage());
    }

    @Test
    void login_throwsRuntimeException_whenUserNotFound() {
        LoginRequestDTO request = new LoginRequestDTO("unknown.user", "password123");

        when(userRepository.findByUsername("unknown.user")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("auth.user.not.found", ex.getMessage());
        verify(jwtTokenUtil, never()).generateToken(any());
    }

    @Test
    void login_throwsRuntimeException_whenPasswordIsWrong() {
        LoginRequestDTO request = new LoginRequestDTO("admin.user", "wrongpassword");

        when(userRepository.findByUsername("admin.user")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongpassword", "hashed_password")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("auth.password.incorrect", ex.getMessage());
        verify(jwtTokenUtil, never()).generateToken(any());
    }

    @Test
    void login_returnsUserRole_forRegularUser() {
        sampleUser.setRole(User.Role.USER);

        LoginRequestDTO request = new LoginRequestDTO("admin.user", "password123");

        when(userRepository.findByUsername("admin.user")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(jwtTokenUtil.generateToken("admin.user")).thenReturn("mock.jwt.token");

        LoginResponseDTO result = authService.login(request);

        assertEquals("USER", result.getRole());
    }

    // ───── logout ─────

    @Test
    void logout_returnsSuccessMessage() {
        String result = authService.logout();

        assertEquals("Logged out successfully", result);
    }
}