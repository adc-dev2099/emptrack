package com.example.emptrack.controller;

import com.example.emptrack.dto.request.EmployeeRequestDTO;
import com.example.emptrack.dto.response.EmployeeResponseDTO;
import com.example.emptrack.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("${api.employees.base}")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    // ADD
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> addEmployee(@RequestBody EmployeeRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAll());
    }

    // GET BY ID
    @GetMapping("${api.employees.get.by.id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    // GET BY EMPLOYEE ID
    @GetMapping("${api.employees.get.by.employee.id}")
    public ResponseEntity<EmployeeResponseDTO> getByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeService.getByEmployeeId(employeeId));
    }

    // SEARCH BY NAME
    @GetMapping("${api.employees.search}")
    public ResponseEntity<List<EmployeeResponseDTO>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(employeeService.searchByName(name));
    }

    // COMBINED SEARCH + FILTER
    @GetMapping("${api.employees.search.filter}")
    public ResponseEntity<Page<EmployeeResponseDTO>> searchAndFilter(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(employeeService.searchAndFilter(name, departmentId, active, minAge, maxAge, page, size));
    }

    // UPDATE
    @PutMapping("${api.employees.update}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(@PathVariable Long id,
                                                              @RequestBody EmployeeRequestDTO request) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    // DELETE
    @DeleteMapping("${api.employees.delete}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.ok("Employee deleted successfully.");
    }

    @PatchMapping("${api.employees.activate}")
    public ResponseEntity<EmployeeResponseDTO> activateEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.activate(id));
    }

    // ───── Reports (Paginated) ─────

    @GetMapping("${api.employees.report.department}")
    public ResponseEntity<Page<EmployeeResponseDTO>> getByDepartmentPaged(
            @PathVariable Long departmentId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(employeeService.getByDepartmentPaged(departmentId, active, page, size));
    }

    @GetMapping("${api.employees.report.age}")
    public ResponseEntity<Page<EmployeeResponseDTO>> getAllOrderedByAgePaged(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(defaultValue = "asc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(employeeService.getAllOrderedByAgePaged(active, minAge, maxAge, sort, page, size));
    }

    // ───── Calculations ─────

    @GetMapping("${api.employees.stats.salary}")
    public ResponseEntity<BigDecimal> getAverageSalary(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge) {
        return ResponseEntity.ok(employeeService.getAverageSalary(departmentId, active, minAge, maxAge));
    }

    @GetMapping("${api.employees.stats.age}")
    public ResponseEntity<Double> getAverageAge(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge) {
        return ResponseEntity.ok(employeeService.getAverageAge(departmentId, active, minAge, maxAge));
    }
}
