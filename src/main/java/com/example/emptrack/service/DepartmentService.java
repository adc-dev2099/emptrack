package com.example.emptrack.service;

import com.example.emptrack.dto.request.DepartmentRequestDTO;
import com.example.emptrack.dto.response.DepartmentResponseDTO;
import com.example.emptrack.model.Department;
import com.example.emptrack.exception.DuplicateEntryException;
import com.example.emptrack.exception.ResourceNotFoundException;
import com.example.emptrack.repository.DepartmentRepository;
import com.example.emptrack.repository.EmployeeRepository;
import com.example.emptrack.util.MessageUtil;
import com.example.emptrack.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final MessageUtil messageUtil;

    // ADD
    public DepartmentResponseDTO addDepartment(DepartmentRequestDTO request) {

        ValidationUtil.validateDepartmentName(request.getName());

        if (departmentRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateEntryException(messageUtil.get("department.duplicate", request.getName()));
        }

        Department department = new Department();
        department.setName(request.getName());

        return mapToResponse(departmentRepository.save(department));
    }

    // GET ALL — only return active departments
    public List<DepartmentResponseDTO> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        List<DepartmentResponseDTO> result = new ArrayList<>();
        for (Department dept : departments) {
            result.add(mapToResponse(dept));
        }
        return result;
    }

    // GET BY ID
    public DepartmentResponseDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtil.get("department.not.found", id)));
        return mapToResponse(department);
    }

    // SEARCH BY NAME
    public List<DepartmentResponseDTO> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(messageUtil.get("department.name.search.required"));
        }
        List<Department> departments = departmentRepository.findByNameContainingIgnoreCase(name);

        if (departments.isEmpty()) {
            throw new ResourceNotFoundException(messageUtil.get("department.name.not.found", name));
        }

        List<DepartmentResponseDTO> result = new ArrayList<>();
        for (Department dept : departments) {
            result.add(mapToResponse(dept));
        }
        return result;
    }

    // UPDATE
    public DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtil.get("department.not.found", id)));

        ValidationUtil.validateDepartmentName(request.getName());

        if (departmentRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateEntryException(messageUtil.get("department.name.duplicate", request.getName()));
        }

        department.setName(request.getName());
        return mapToResponse(departmentRepository.save(department));
    }

    // REACTIVATE
    public DepartmentResponseDTO activateDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtil.get("department.not.found", id)));
        department.setActive(true);
        return mapToResponse(departmentRepository.save(department));
    }

    // DELETE — soft delete with employee check
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtil.get("department.not.found", id)));

        if (employeeRepository.existsByDepartmentIdAndActive(id, true)) {
            throw new IllegalArgumentException(messageUtil.get("department.has.active.employees"));
        }

        department.setActive(false);
        departmentRepository.save(department);
    }

    // MAPPER
    private DepartmentResponseDTO mapToResponse(Department department) {
        DepartmentResponseDTO response = new DepartmentResponseDTO();
        response.setId(department.getId());
        response.setName(department.getName());
        response.setActive(department.isActive());
        return response;
    }
}