package com.example.emptrack.repository;

import com.example.emptrack.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    // Exact match — for duplicate check
    Optional<Department> findByName(String name);

    // Search by name (partial match)
    List<Department> findByNameContainingIgnoreCase(String name);

    // Get all active departments
    List<Department> findByActive(boolean active);
}
