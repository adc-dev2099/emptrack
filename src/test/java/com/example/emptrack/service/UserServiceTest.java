package com.example.emptrack.service;

import com.example.emptrack.dto.request.UserRequestDTO;
import com.example.emptrack.dto.response.UserResponseDTO;
import com.example.emptrack.exception.DuplicateEntryException;
import com.example.emptrack.exception.ResourceNotFoundException;
import com.example.emptrack.model.User;
import com.example.emptrack.repository.UserRepository;
import com.example.emptrack.service.UserService;
import com.example.emptrack.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private MessageUtil messageUtil;

    @InjectMocks
    private UserService userService;

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

    // ───── addUser ─────

    @Test
    void addUser_success() {
        UserRequestDTO request = new UserRequestDTO(
                "Admin", "User", "admin.user", "password123", "ADMIN"
        );

        when(userRepository.findByUsername("admin.user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponseDTO result = userService.addUser(request);

        assertNotNull(result);
        assertEquals("admin.user", result.getUsername());
        assertEquals("ADMIN", result.getRole());
        assertTrue(result.isActive());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addUser_defaultsRoleToUser_whenRoleIsNull() {
        UserRequestDTO request = new UserRequestDTO(
                "Regular", "User", "regular.user", "password123", null
        );

        User regularUser = new User();
        regularUser.setId(2L);
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setUsername("regular.user");
        regularUser.setPassword("hashed_password");
        regularUser.setRole(User.Role.USER);
        regularUser.setActive(true);

        when(userRepository.findByUsername("regular.user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(regularUser);

        UserResponseDTO result = userService.addUser(request);

        assertEquals("USER", result.getRole());
    }

    @Test
    void addUser_throwsIllegalArgument_whenFirstNameIsNull() {
        UserRequestDTO request = new UserRequestDTO(
                null, "User", "admin.user", "password123", "ADMIN"
        );

        assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_throwsIllegalArgument_whenLastNameIsBlank() {
        UserRequestDTO request = new UserRequestDTO(
                "Admin", "   ", "admin.user", "password123", "ADMIN"
        );

        assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_throwsIllegalArgument_whenUsernameHasSpaces() {
        UserRequestDTO request = new UserRequestDTO(
                "Admin", "User", "admin user", "password123", "ADMIN"
        );

        assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_throwsIllegalArgument_whenPasswordIsNull() {
        UserRequestDTO request = new UserRequestDTO(
                "Admin", "User", "admin.user", null, "ADMIN"
        );

        when(messageUtil.get("user.password.required")).thenReturn("Password is required.");

        assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_throwsIllegalArgument_whenPasswordTooShort() {
        UserRequestDTO request = new UserRequestDTO(
                "Admin", "User", "admin.user", "short", "ADMIN"
        );

        when(messageUtil.get("user.password.minimum")).thenReturn("Password must be at least 8 characters.");

        assertThrows(IllegalArgumentException.class, () -> userService.addUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_throwsDuplicateEntryException_whenUsernameAlreadyExists() {
        UserRequestDTO request = new UserRequestDTO(
                "Admin", "User", "admin.user", "password123", "ADMIN"
        );

        when(userRepository.findByUsername("admin.user")).thenReturn(Optional.of(sampleUser));
        when(messageUtil.get(eq("user.duplicate.username"), any()))
                .thenReturn("Username already exists: admin.user");

        assertThrows(DuplicateEntryException.class, () -> userService.addUser(request));
        verify(userRepository, never()).save(any());
    }

    // ───── getAllUsers ─────

    @Test
    void getAllUsers_returnsAllUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setFirstName("Regular");
        user2.setLastName("User");
        user2.setUsername("regular.user");
        user2.setPassword("hashed");
        user2.setRole(User.Role.USER);
        user2.setActive(true);

        when(userRepository.findAll()).thenReturn(Arrays.asList(sampleUser, user2));

        List<UserResponseDTO> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("admin.user", result.get(0).getUsername());
        assertEquals("regular.user", result.get(1).getUsername());
    }

    @Test
    void getAllUsers_returnsEmptyList_whenNoUsersExist() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserResponseDTO> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
    }

    // ───── getUserById ─────

    @Test
    void getUserById_returnsCorrectUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserResponseDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("admin.user", result.getUsername());
        assertEquals("ADMIN", result.getRole());
    }

    @Test
    void getUserById_throwsResourceNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(messageUtil.get(eq("user.not.found"), any())).thenReturn("User not found with id: 99");

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }

    // ───── searchByUsername ─────

    @Test
    void searchByUsername_returnsMatchingUsers() {
        when(userRepository.findByUsernameContainingIgnoreCase("admin"))
                .thenReturn(List.of(sampleUser));

        List<UserResponseDTO> result = userService.searchByUsername("admin");

        assertEquals(1, result.size());
        assertEquals("admin.user", result.get(0).getUsername());
    }

    @Test
    void searchByUsername_throwsIllegalArgument_whenUsernameIsBlank() {
        when(messageUtil.get("user.username.search.required")).thenReturn("Username search term is required.");

        assertThrows(IllegalArgumentException.class, () -> userService.searchByUsername("  "));
    }

    @Test
    void searchByUsername_throwsResourceNotFound_whenNoMatch() {
        when(userRepository.findByUsernameContainingIgnoreCase("xyz"))
                .thenReturn(Collections.emptyList());
        when(messageUtil.get(eq("user.username.not.found"), any()))
                .thenReturn("No users found with username: xyz");

        assertThrows(ResourceNotFoundException.class, () -> userService.searchByUsername("xyz"));
    }

    // ───── filterByRole ─────

    @Test
    void filterByRole_returnsUsersWithMatchingRole() {
        when(userRepository.findByRole(User.Role.ADMIN)).thenReturn(List.of(sampleUser));

        List<UserResponseDTO> result = userService.filterByRole("ADMIN");

        assertEquals(1, result.size());
        assertEquals("ADMIN", result.get(0).getRole());
    }

    @Test
    void filterByRole_isCaseInsensitive() {
        when(userRepository.findByRole(User.Role.ADMIN)).thenReturn(List.of(sampleUser));

        List<UserResponseDTO> result = userService.filterByRole("admin");

        assertEquals(1, result.size());
    }

    @Test
    void filterByRole_throwsIllegalArgument_whenRoleIsBlank() {
        when(messageUtil.get("user.role.required")).thenReturn("Role is required.");

        assertThrows(IllegalArgumentException.class, () -> userService.filterByRole("  "));
    }

    @Test
    void filterByRole_throwsIllegalArgument_whenRoleIsInvalid() {
        when(messageUtil.get(eq("user.role.invalid"), any()))
                .thenReturn("Invalid role: MANAGER. Must be ADMIN or USER.");

        assertThrows(IllegalArgumentException.class, () -> userService.filterByRole("MANAGER"));
    }

    // ───── updateUser ─────

    @Test
    void updateUser_success_withNewUsername() {
        UserRequestDTO request = new UserRequestDTO(
                null, null, "new.username", null, null
        );

        User updated = new User();
        updated.setId(1L);
        updated.setFirstName("Admin");
        updated.setLastName("User");
        updated.setUsername("new.username");
        updated.setPassword("hashed_password");
        updated.setRole(User.Role.ADMIN);
        updated.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.findByUsername("new.username")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(updated);

        UserResponseDTO result = userService.updateUser(1L, request);

        assertEquals("new.username", result.getUsername());
    }

    @Test
    void updateUser_throwsResourceNotFound_whenUserDoesNotExist() {
        UserRequestDTO request = new UserRequestDTO(null, null, "new.user", null, null);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(messageUtil.get(eq("user.not.found"), any())).thenReturn("User not found with id: 99");

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(99L, request));
    }

    @Test
    void updateUser_throwsDuplicateEntryException_whenNewUsernameBelongsToAnotherUser() {
        UserRequestDTO request = new UserRequestDTO(null, null, "other.user", null, null);

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("other.user");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.findByUsername("other.user")).thenReturn(Optional.of(otherUser));
        when(messageUtil.get(eq("user.duplicate.username"), any()))
                .thenReturn("Username already exists: other.user");

        assertThrows(DuplicateEntryException.class, () -> userService.updateUser(1L, request));
    }

    @Test
    void updateUser_allowsSameUsernameForSameUser() {
        // Updating user 1 with the same username they already have — should not throw
        UserRequestDTO request = new UserRequestDTO(null, null, "admin.user", null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.findByUsername("admin.user")).thenReturn(Optional.of(sampleUser)); // same id
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        assertDoesNotThrow(() -> userService.updateUser(1L, request));
    }

    @Test
    void updateUser_throwsIllegalArgument_whenNewPasswordTooShort() {
        UserRequestDTO request = new UserRequestDTO(null, null, null, "short", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(messageUtil.get("user.password.minimum")).thenReturn("Password must be at least 8 characters.");

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1L, request));
    }

    // ───── deleteUser (soft delete) ─────

    @Test
    void deleteUser_setsUserInactive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        userService.deleteUser(1L);

        verify(userRepository).save(argThat(user -> !user.isActive()));
    }

    @Test
    void deleteUser_throwsResourceNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(messageUtil.get(eq("user.not.found"), any())).thenReturn("User not found with id: 99");

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));
    }

    // ───── activateUser ─────

    @Test
    void activateUser_success_whenUserIsInactive() {
        sampleUser.setActive(false);

        User activated = new User();
        activated.setId(1L);
        activated.setFirstName("Admin");
        activated.setLastName("User");
        activated.setUsername("admin.user");
        activated.setPassword("hashed_password");
        activated.setRole(User.Role.ADMIN);
        activated.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(activated);

        UserResponseDTO result = userService.activateUser(1L);

        assertTrue(result.isActive());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void activateUser_throwsIllegalArgument_whenUserIsAlreadyActive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(messageUtil.get(eq("user.already.active"), any()))
                .thenReturn("User is already active.");

        assertThrows(IllegalArgumentException.class, () -> userService.activateUser(1L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void activateUser_throwsResourceNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(messageUtil.get(eq("user.not.found"), any())).thenReturn("User not found with id: 99");

        assertThrows(ResourceNotFoundException.class, () -> userService.activateUser(99L));
    }
}