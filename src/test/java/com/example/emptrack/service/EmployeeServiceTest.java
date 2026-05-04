package com.example.emptrack.service;

import com.example.emptrack.dto.request.EmployeeRequestDTO;
import com.example.emptrack.dto.response.EmployeeResponseDTO;
import com.example.emptrack.exception.ResourceNotFoundException;
import com.example.emptrack.model.Department;
import com.example.emptrack.model.Employee;
import com.example.emptrack.repository.DepartmentRepository;
import com.example.emptrack.repository.EmployeeRepository;
import com.example.emptrack.service.EmployeeService;
import com.example.emptrack.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private MessageUtil messageUtil;

    @InjectMocks
    private EmployeeService employeeService;

    private Department sampleDepartment;
    private Employee sampleEmployee;

    @BeforeEach
    void setUp() {
        sampleDepartment = new Department();
        sampleDepartment.setId(1L);
        sampleDepartment.setName("Engineering");
        sampleDepartment.setActive(true);

        sampleEmployee = new Employee();
        sampleEmployee.setId(1L);
        sampleEmployee.setEmployeeId(100001L);
        sampleEmployee.setFirstName("Juan");
        sampleEmployee.setLastName("Cruz");
        sampleEmployee.setDateOfBirth(LocalDate.now().minusYears(25));
        sampleEmployee.setDepartment(sampleDepartment);
        sampleEmployee.setSalary(new BigDecimal("50000.00"));
        sampleEmployee.setActive(true);
    }

    // ───── create ─────

    @Test
    void create_success() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO(
                "Juan", "Cruz",
                LocalDate.now().minusYears(25),
                1L,
                new BigDecimal("50000.00")
        );

        when(employeeRepository.findByEmployeeId(anyLong())).thenReturn(Optional.empty());
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDepartment));
        when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);

        EmployeeResponseDTO result = employeeService.create(dto);

        assertNotNull(result);
        assertEquals("Juan", result.getFirstName());
        assertEquals("Cruz", result.getLastName());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void create_throwsIllegalArgument_whenFirstNameIsNull() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO(
                null, "Cruz",
                LocalDate.now().minusYears(25),
                1L,
                new BigDecimal("50000.00")
        );

        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void create_throwsIllegalArgument_whenLastNameIsBlank() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO(
                "Juan", "   ",
                LocalDate.now().minusYears(25),
                1L,
                new BigDecimal("50000.00")
        );

        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void create_throwsIllegalArgument_whenDateOfBirthIsNull() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO(
                "Juan", "Cruz",
                null,
                1L,
                new BigDecimal("50000.00")
        );

        when(messageUtil.get("employee.dob.required")).thenReturn("Date of birth is required.");

        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void create_throwsIllegalArgument_whenEmployeeIsUnder18() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO(
                "Juan", "Cruz",
                LocalDate.now().minusYears(17),
                1L,
                new BigDecimal("50000.00")
        );

        when(messageUtil.get("employee.age.minimum")).thenReturn("Employee must be at least 18 years old.");

        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void create_throwsIllegalArgument_whenSalaryIsZero() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO(
                "Juan", "Cruz",
                LocalDate.now().minusYears(25),
                1L,
                BigDecimal.ZERO
        );

        when(messageUtil.get("employee.salary.invalid")).thenReturn("Salary must be greater than zero.");

        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void create_throwsIllegalArgument_whenSalaryIsNull() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO(
                "Juan", "Cruz",
                LocalDate.now().minusYears(25),
                1L,
                null
        );

        when(messageUtil.get("employee.salary.invalid")).thenReturn("Salary must be greater than zero.");

        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void create_throwsResourceNotFound_whenDepartmentDoesNotExist() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO(
                "Juan", "Cruz",
                LocalDate.now().minusYears(25),
                99L,
                new BigDecimal("50000.00")
        );

        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());
        when(messageUtil.get("department.not.found.generic")).thenReturn("Department not found.");

        assertThrows(ResourceNotFoundException.class, () -> employeeService.create(dto));
        verify(employeeRepository, never()).save(any());
    }

    // ───── getById ─────

    @Test
    void getById_returnsCorrectEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));

        EmployeeResponseDTO result = employeeService.getById(1L);

        assertNotNull(result);
        assertEquals("Juan", result.getFirstName());
        assertEquals("Cruz", result.getLastName());
    }

    @Test
    void getById_throwsResourceNotFound_whenEmployeeDoesNotExist() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        when(messageUtil.get(eq("employee.not.found"), any())).thenReturn("Employee not found with id: 99");

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getById(99L));
    }

    // ───── getAll ─────

    @Test
    void getAll_returnsAllEmployees() {
        Employee emp2 = new Employee();
        emp2.setId(2L);
        emp2.setEmployeeId(100002L);
        emp2.setFirstName("Maria");
        emp2.setLastName("Santos");
        emp2.setDateOfBirth(LocalDate.now().minusYears(30));
        emp2.setDepartment(sampleDepartment);
        emp2.setSalary(new BigDecimal("60000.00"));
        emp2.setActive(true);

        when(employeeRepository.findAll()).thenReturn(Arrays.asList(sampleEmployee, emp2));

        List<EmployeeResponseDTO> result = employeeService.getAll();

        assertEquals(2, result.size());
        assertEquals("Juan", result.get(0).getFirstName());
        assertEquals("Maria", result.get(1).getFirstName());
    }

    @Test
    void getAll_returnsEmptyList_whenNoEmployeesExist() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        List<EmployeeResponseDTO> result = employeeService.getAll();

        assertTrue(result.isEmpty());
    }

    // ───── getByEmployeeId ─────

    @Test
    void getByEmployeeId_returnsCorrectEmployee() {
        when(employeeRepository.findByEmployeeId(100001L)).thenReturn(Optional.of(sampleEmployee));

        EmployeeResponseDTO result = employeeService.getByEmployeeId(100001L);

        assertNotNull(result);
        assertEquals(100001L, result.getEmployeeId());
    }

    @Test
    void getByEmployeeId_throwsResourceNotFound_whenNotFound() {
        when(employeeRepository.findByEmployeeId(999999L)).thenReturn(Optional.empty());
        when(messageUtil.get(eq("employee.not.found.employee.id"), any()))
                .thenReturn("Employee not found with employeeId: 999999");

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getByEmployeeId(999999L));
    }

    // ───── update ─────

    @Test
    void update_success_withAllFields() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO(
                "Pedro", "Reyes",
                LocalDate.now().minusYears(28),
                1L,
                new BigDecimal("70000.00")
        );

        Employee updated = new Employee();
        updated.setId(1L);
        updated.setEmployeeId(100001L);
        updated.setFirstName("Pedro");
        updated.setLastName("Reyes");
        updated.setDateOfBirth(LocalDate.now().minusYears(28));
        updated.setDepartment(sampleDepartment);
        updated.setSalary(new BigDecimal("70000.00"));
        updated.setActive(true);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDepartment));
        when(employeeRepository.save(any(Employee.class))).thenReturn(updated);

        EmployeeResponseDTO result = employeeService.update(1L, dto);

        assertEquals("Pedro", result.getFirstName());
        assertEquals("Reyes", result.getLastName());
        assertEquals(new BigDecimal("70000.00"), result.getSalary());
    }

    @Test
    void update_throwsResourceNotFound_whenEmployeeDoesNotExist() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO(
                "Pedro", "Reyes",
                LocalDate.now().minusYears(28),
                1L,
                new BigDecimal("70000.00")
        );

        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        when(messageUtil.get(eq("employee.not.found"), any())).thenReturn("Employee not found with id: 99");

        assertThrows(ResourceNotFoundException.class, () -> employeeService.update(99L, dto));
    }

    @Test
    void update_throwsIllegalArgument_whenAgeBelow18OnUpdate() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO(
                null, null,
                LocalDate.now().minusYears(16),
                null,
                null
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(messageUtil.get("employee.age.minimum")).thenReturn("Employee must be at least 18 years old.");

        assertThrows(IllegalArgumentException.class, () -> employeeService.update(1L, dto));
    }

    // ───── delete (soft delete) ─────

    @Test
    void delete_setsEmployeeInactive() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));

        employeeService.delete(1L);

        verify(employeeRepository).save(argThat(emp -> !emp.isActive()));
    }

    @Test
    void delete_throwsResourceNotFound_whenEmployeeDoesNotExist() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        when(messageUtil.get(eq("employee.not.found"), any())).thenReturn("Employee not found with id: 99");

        assertThrows(ResourceNotFoundException.class, () -> employeeService.delete(99L));
    }

    // ───── activate ─────

    @Test
    void activate_success_whenDepartmentIsActive() {
        sampleEmployee.setActive(false);

        Employee activated = new Employee();
        activated.setId(1L);
        activated.setEmployeeId(100001L);
        activated.setFirstName("Juan");
        activated.setLastName("Cruz");
        activated.setDateOfBirth(LocalDate.now().minusYears(25));
        activated.setDepartment(sampleDepartment);
        activated.setSalary(new BigDecimal("50000.00"));
        activated.setActive(true);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(activated);

        EmployeeResponseDTO result = employeeService.activate(1L);

        assertTrue(result.getActive());
    }

    @Test
    void activate_throwsIllegalArgument_whenDepartmentIsInactive() {
        sampleDepartment.setActive(false);
        sampleEmployee.setActive(false);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(messageUtil.get(eq("employee.department.inactive"), any()))
                .thenReturn("Cannot reactivate employee because their department 'Engineering' is inactive.");

        assertThrows(IllegalArgumentException.class, () -> employeeService.activate(1L));
    }

    @Test
    void activate_throwsResourceNotFound_whenEmployeeDoesNotExist() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        when(messageUtil.get(eq("employee.not.found"), any())).thenReturn("Employee not found with id: 99");

        assertThrows(ResourceNotFoundException.class, () -> employeeService.activate(99L));
    }

    // ───── searchByName ─────

    @Test
    void searchByName_returnsMatchingEmployees() {
        when(employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("juan", "juan"))
                .thenReturn(List.of(sampleEmployee));

        List<EmployeeResponseDTO> result = employeeService.searchByName("juan");

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getFirstName());
    }

    @Test
    void searchByName_throwsIllegalArgument_whenNameIsBlank() {
        when(messageUtil.get("employee.name.search.required")).thenReturn("Name search term is required.");

        assertThrows(IllegalArgumentException.class, () -> employeeService.searchByName("  "));
    }

    @Test
    void searchByName_throwsResourceNotFound_whenNoMatchFound() {
        when(employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("xyz", "xyz"))
                .thenReturn(Collections.emptyList());
        when(messageUtil.get(eq("employee.name.not.found"), any()))
                .thenReturn("No employees found with name: xyz");

        assertThrows(ResourceNotFoundException.class, () -> employeeService.searchByName("xyz"));
    }

    // ───── getAverageSalary ─────

    @Test
    void getAverageSalary_returnsCorrectAverage() {
        Employee emp2 = new Employee();
        emp2.setId(2L);
        emp2.setFirstName("Maria");
        emp2.setLastName("Santos");
        emp2.setDateOfBirth(LocalDate.now().minusYears(30));
        emp2.setDepartment(sampleDepartment);
        emp2.setSalary(new BigDecimal("70000.00"));
        emp2.setActive(true);

        when(employeeRepository.findForStats(null, null, null, null))
                .thenReturn(Arrays.asList(sampleEmployee, emp2));

        BigDecimal avg = employeeService.getAverageSalary(null, null, null, null);

        // (50000 + 70000) / 2 = 60000.00
        assertEquals(new BigDecimal("60000.00"), avg);
    }

    @Test
    void getAverageSalary_returnsZero_whenNoEmployees() {
        when(employeeRepository.findForStats(null, null, null, null))
                .thenReturn(Collections.emptyList());

        BigDecimal avg = employeeService.getAverageSalary(null, null, null, null);

        assertEquals(BigDecimal.ZERO, avg);
    }

    // ───── getAverageAge ─────

    @Test
    void getAverageAge_returnsZero_whenNoEmployees() {
        when(employeeRepository.findForStats(null, null, null, null))
                .thenReturn(Collections.emptyList());

        double avg = employeeService.getAverageAge(null, null, null, null);

        assertEquals(0.0, avg);
    }

    @Test
    void getAverageAge_returnsCorrectAverage() {
        // emp1 is 25 years old (from setUp)
        Employee emp2 = new Employee();
        emp2.setId(2L);
        emp2.setFirstName("Maria");
        emp2.setLastName("Santos");
        emp2.setDateOfBirth(LocalDate.now().minusYears(35));
        emp2.setDepartment(sampleDepartment);
        emp2.setSalary(new BigDecimal("60000.00"));
        emp2.setActive(true);

        when(employeeRepository.findForStats(null, null, null, null))
                .thenReturn(Arrays.asList(sampleEmployee, emp2));

        double avg = employeeService.getAverageAge(null, null, null, null);

        // (25 + 35) / 2 = 30.0
        assertEquals(30.0, avg);
    }
}