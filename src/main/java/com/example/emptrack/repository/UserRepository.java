package com.example.emptrack.repository;

import com.example.emptrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // For Login
    Optional<User> findByUsername(String username);

    // Search by username
    List<User> findByUsernameContainingIgnoreCase(String username);

    // Filter by role
    List<User> findByRole(User.Role role);

    // Combined search + filter
    @Query("SELECT u FROM User u WHERE " +
            "(:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
            "AND (:role IS NULL OR u.role = :role)")
    List<User> searchAndFilter(
            @Param("username") String username,
            @Param("role") User.Role role);
}