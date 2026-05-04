package com.example.emptrack.service;

import com.example.emptrack.dto.request.DepartmentRequestDTO;
import com.example.emptrack.dto.response.DepartmentResponseDTO;
import com.example.emptrack.exception.DuplicateEntryException;
import com.example.emptrack.exception.ResourceNotFoundException;
import com.example.emptrack.model.Department;
import com.example.emptrack.repository.DepartmentRepository;
import com.example.emptrack.repository.EmployeeRepository;
import com.example.emptrack.service.DepartmentService;
import com.example.emptrack.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private MessageUtil messageUtil;

    @InjectMocks
    private DepartmentService departmentService;

    private Department sampleDepartment;

    @BeforeEach
    void setUp() {
        sampleDepartment = new Department();
        sampleDepartment.setId(1L);
        sampleDepartment.setName("Engineering");
        sampleDepartment.setActive(true);
    }

    // ───── addDepartment ─────

    @Test
    void addDepartment_success() {
        DepartmentRequestDTO request = new DepartmentRequestDTO("Engineering");

        when(departmentRepository.findByName("Engineering")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenReturn(sampleDepartment);

        DepartmentResponseDTO result = departmentService.addDepartment(request);

        assertNotNull(result);
        assertEquals("Engineering", result.getName());
        assertTrue(result.isActive());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void addDepartment_throwsWhenNameIsNull() {
        DepartmentRequestDTO request = new DepartmentRequestDTO(null);

        assertThrows(IllegalArgumentException.class,
                () -> departmentService.addDepartment(request));

        verify(departmentRepository, never()).save(any());
    }

    @Test
    void addDepartment_throwsWhenNameIsBlank() {
        DepartmentRequestDTO request = new DepartmentRequestDTO("   ");

        assertThrows(IllegalArgumentException.class,
                () -> departmentService.addDepartment(request));

        verify(departmentRepository, never()).save(any());
    }

    @Test
    void addDepartment_throwsWhenNameExceedsMaxLength() {
        String longName = "A".repeat(101);
        DepartmentRequestDTO request = new DepartmentRequestDTO(longName);

        assertThrows(IllegalArgumentException.class,
                () -> departmentService.addDepartment(request));

        verify(departmentRepository, never()).save(any());
    }

    @Test
    void addDepartment_throwsDuplicateEntryException_whenNameAlreadyExists() {
        DepartmentRequestDTO request = new DepartmentRequestDTO("Engineering");

        when(departmentRepository.findByName("Engineering")).thenReturn(Optional.of(sampleDepartment));
        when(messageUtil.get(eq("department.duplicate"), any())).thenReturn("Department already exists: Engineering");

        assertThrows(DuplicateEntryException.class,
                () -> departmentService.addDepartment(request));

        verify(departmentRepository, never()).save(any());
    }

    // ───── getAllDepartments ─────

    @Test
    void getAllDepartments_returnsAllDepartments() {
        Department dept2 = new Department();
        dept2.setId(2L);
        dept2.setName("HR");
        dept2.setActive(true);

        when(departmentRepository.findAll()).thenReturn(Arrays.asList(sampleDepartment, dept2));

        List<DepartmentResponseDTO> result = departmentService.getAllDepartments();

        assertEquals(2, result.size());
        assertEquals("Engineering", result.get(0).getName());
        assertEquals("HR", result.get(1).getName());
    }

    @Test
    void getAllDepartments_returnsEmptyList_whenNoneExist() {
        when(departmentRepository.findAll()).thenReturn(Collections.emptyList());

        List<DepartmentResponseDTO> result = departmentService.getAllDepartments();

        assertTrue(result.isEmpty());
    }

    // ───── getDepartmentById ─────

    @Test
    void getDepartmentById_returnsCorrectDepartment() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDepartment));

        DepartmentResponseDTO result = departmentService.getDepartmentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Engineering", result.getName());
    }

    @Test
    void getDepartmentById_throwsResourceNotFoundException_whenNotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());
        when(messageUtil.get(eq("department.not.found"), any())).thenReturn("Department not found with id: 99");

        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.getDepartmentById(99L));
    }

    // ───── searchByName ─────

    @Test
    void searchByName_returnsMatchingDepartments() {
        when(departmentRepository.findByNameContainingIgnoreCase("eng"))
                .thenReturn(List.of(sampleDepartment));

        List<DepartmentResponseDTO> result = departmentService.searchByName("eng");

        assertEquals(1, result.size());
        assertEquals("Engineering", result.get(0).getName());
    }

    @Test
    void searchByName_throwsIllegalArgument_whenNameIsBlank() {
        when(messageUtil.get("department.name.search.required")).thenReturn("Department name search term is required.");

        assertThrows(IllegalArgumentException.class,
                () -> departmentService.searchByName("  "));
    }

    @Test
    void searchByName_throwsResourceNotFound_whenNoMatch() {
        when(departmentRepository.findByNameContainingIgnoreCase("xyz"))
                .thenReturn(Collections.emptyList());
        when(messageUtil.get(eq("department.name.not.found"), any())).thenReturn("No departments found with name: xyz");

        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.searchByName("xyz"));
    }

    // ───── updateDepartment ─────

    @Test
    void updateDepartment_success() {
        DepartmentRequestDTO request = new DepartmentRequestDTO("Finance");

        Department updated = new Department();
        updated.setId(1L);
        updated.setName("Finance");
        updated.setActive(true);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDepartment));
        when(departmentRepository.findByName("Finance")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenReturn(updated);

        DepartmentResponseDTO result = departmentService.updateDepartment(1L, request);

        assertEquals("Finance", result.getName());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void updateDepartment_throwsResourceNotFound_whenDepartmentDoesNotExist() {
        DepartmentRequestDTO request = new DepartmentRequestDTO("Finance");

        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());
        when(messageUtil.get(eq("department.not.found"), any())).thenReturn("Department not found with id: 99");

        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.updateDepartment(99L, request));
    }

    @Test
    void updateDepartment_throwsDuplicateEntryException_whenNameTaken() {
        DepartmentRequestDTO request = new DepartmentRequestDTO("HR");

        Department existingHr = new Department();
        existingHr.setId(2L);
        existingHr.setName("HR");

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDepartment));
        when(departmentRepository.findByName("HR")).thenReturn(Optional.of(existingHr));
        when(messageUtil.get(eq("department.name.duplicate"), any())).thenReturn("Department name already exists: HR");

        assertThrows(DuplicateEntryException.class,
                () -> departmentService.updateDepartment(1L, request));
    }

    // ───── activateDepartment ─────

    @Test
    void activateDepartment_success() {
        sampleDepartment.setActive(false);

        Department activated = new Department();
        activated.setId(1L);
        activated.setName("Engineering");
        activated.setActive(true);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDepartment));
        when(departmentRepository.save(any(Department.class))).thenReturn(activated);

        DepartmentResponseDTO result = departmentService.activateDepartment(1L);

        assertTrue(result.isActive());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void activateDepartment_throwsResourceNotFound_whenDepartmentDoesNotExist() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());
        when(messageUtil.get(eq("department.not.found"), any())).thenReturn("Department not found with id: 99");

        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.activateDepartment(99L));
    }

    // ───── deleteDepartment (soft delete) ─────

    @Test
    void deleteDepartment_success_whenNoActiveEmployees() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDepartment));
        when(employeeRepository.existsByDepartmentIdAndActive(1L, true)).thenReturn(false);

        departmentService.deleteDepartment(1L);

        verify(departmentRepository).save(argThat(dept -> !dept.isActive()));
    }

    @Test
    void deleteDepartment_throwsIllegalArgument_whenDepartmentHasActiveEmployees() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDepartment));
        when(employeeRepository.existsByDepartmentIdAndActive(1L, true)).thenReturn(true);
        when(messageUtil.get("department.has.active.employees"))
                .thenReturn("Cannot deactivate department because it has active employees.");

        assertThrows(IllegalArgumentException.class,
                () -> departmentService.deleteDepartment(1L));

        verify(departmentRepository, never()).save(any());
    }

    @Test
    void deleteDepartment_throwsResourceNotFound_whenDepartmentDoesNotExist() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());
        when(messageUtil.get(eq("department.not.found"), any())).thenReturn("Department not found with id: 99");

        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.deleteDepartment(99L));
    }
}