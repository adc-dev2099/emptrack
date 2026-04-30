package com.example.emptrack.controller;

import com.example.emptrack.dto.request.DepartmentRequestDTO;
import com.example.emptrack.dto.response.DepartmentResponseDTO;
import com.example.emptrack.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.departments.base}")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;

    // ADD
    @PostMapping
    public ResponseEntity<DepartmentResponseDTO> addDepartment(@RequestBody DepartmentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.addDepartment(request));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<DepartmentResponseDTO>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    // GET BY ID
    @GetMapping("${api.departments.get.by.id}")
    public ResponseEntity<DepartmentResponseDTO> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    // UPDATE
    @PutMapping("${api.departments.update}")
    public ResponseEntity<DepartmentResponseDTO> updateDepartment(@PathVariable Long id,
                                                                  @RequestBody DepartmentRequestDTO request) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }

    // REACTIVATE
    @PatchMapping("${api.departments.activate}")
    public ResponseEntity<DepartmentResponseDTO> activateDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.activateDepartment(id));
    }

    // DEACTIVATE
    @DeleteMapping("${api.departments.delete}")
    public ResponseEntity<String> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok("Department deactivated successfully.");
    }
}
