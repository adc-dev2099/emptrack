package com.example.emptrack.repository;

import com.example.emptrack.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Search by employeeId
    Optional<Employee> findByEmployeeId(Long employeeId);

    // Search by name (case-insensitive, partial match)
    List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    // List all active employees by department
    List<Employee> findByDepartmentIdAndActive(Long departmentId, boolean active);

    // List all active employees ordered by date of birth (age)
    List<Employee> findByActiveOrderByDateOfBirthAsc(boolean active);

    // Get all active employees only
    List<Employee> findByActive(boolean active);

    // Check if department has active employees
    boolean existsByDepartmentIdAndActive(Long departmentId, boolean active);

    // Paginated report by department (with optional active filter)
    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId " +
            "AND (:active IS NULL OR e.active = :active)")
    Page<Employee> findByDepartmentIdAndActivePaged(
            @Param("departmentId") Long departmentId,
            @Param("active") Boolean active,
            Pageable pageable);

    // Paginated report by age (with optional active filter)
    @Query("SELECT e FROM Employee e WHERE (:active IS NULL OR e.active = :active) " +
            "AND (:minDob IS NULL OR e.dateOfBirth <= :minDob) " +
            "AND (:maxDob IS NULL OR e.dateOfBirth >= :maxDob)")
    Page<Employee> findAllActiveOrderByDateOfBirthAscPaged(
            @Param("active") Boolean active,
            @Param("minDob") LocalDate minDob,
            @Param("maxDob") LocalDate maxDob,
            Pageable pageable);

    // Combined search + filter
    @Query("SELECT e FROM Employee e WHERE " +
            "(:name IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR CAST(e.employeeId AS string) LIKE CONCAT('%', :name, '%')) " +
            "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
            "AND (:active IS NULL OR e.active = :active) " +
            "AND (:minDob IS NULL OR e.dateOfBirth <= :minDob) " +
            "AND (:maxDob IS NULL OR e.dateOfBirth >= :maxDob)")
    Page<Employee> searchAndFilterPageable(
            @Param("name") String name,
            @Param("departmentId") Long departmentId,
            @Param("active") Boolean active,
            @Param("minDob") LocalDate minDob,
            @Param("maxDob") LocalDate maxDob,
            Pageable pageable);

    // Query for averages
    @Query("SELECT e FROM Employee e WHERE (:active IS NULL OR e.active = :active) " +
            "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
            "AND (:minDob IS NULL OR e.dateOfBirth <= :minDob) " +
            "AND (:maxDob IS NULL OR e.dateOfBirth >= :maxDob)")
    List<Employee> findForStats(
            @Param("departmentId") Long departmentId,
            @Param("active") Boolean active,
            @Param("minDob") LocalDate minDob,
            @Param("maxDob") LocalDate maxDob);
}