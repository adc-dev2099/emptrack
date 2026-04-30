package com.example.emptrack.service;

import com.example.emptrack.dto.request.EmployeeRequestDTO;
import com.example.emptrack.dto.response.EmployeeResponseDTO;
import com.example.emptrack.model.Department;
import com.example.emptrack.model.Employee;
import com.example.emptrack.exception.ResourceNotFoundException;
import com.example.emptrack.repository.DepartmentRepository;
import com.example.emptrack.repository.EmployeeRepository;
import com.example.emptrack.util.MessageUtil;
import com.example.emptrack.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final MessageUtil messageUtil;

    // ───── CRUD ─────

    public EmployeeResponseDTO create(EmployeeRequestDTO dto) {

        ValidationUtil.validateName(dto.getFirstName(), "First name");

        ValidationUtil.validateName(dto.getLastName(), "Last name");

        if (dto.getDateOfBirth() == null) {
            throw new IllegalArgumentException(messageUtil.get("employee.dob.required"));
        }

        // Age validation — must be at least 18
        int age = calculateAge(dto.getDateOfBirth());
        if (age < 18) {
            throw new IllegalArgumentException(messageUtil.get("employee.age.minimum"));
        }

        if (dto.getSalary() == null || dto.getSalary().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(messageUtil.get("employee.salary.invalid"));
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(messageUtil.get("department.not.found.generic")));

        Employee employee = new Employee();
        employee.setEmployeeId(generateEmployeeId());
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setDepartment(department);
        employee.setSalary(dto.getSalary());
        employee.setActive(true);

        return mapToResponseDTO(employeeRepository.save(employee));
    }

    public EmployeeResponseDTO getById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtil.get("employee.not.found", id)));
        return mapToResponseDTO(employee);
    }

    public List<EmployeeResponseDTO> getAll() {
        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeResponseDTO> result = new ArrayList<>();
        for (Employee emp : employees) {
            result.add(mapToResponseDTO(emp));
        }
        return result;
    }

    public EmployeeResponseDTO update(Long id, EmployeeRequestDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtil.get("employee.not.found", id)));

        if (dto.getFirstName() != null && !dto.getFirstName().trim().isEmpty()) {
            ValidationUtil.validateName(dto.getFirstName(), "First name");
            employee.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null && !dto.getLastName().trim().isEmpty()) {
            ValidationUtil.validateName(dto.getLastName(), "Last name");
            employee.setLastName(dto.getLastName());
        }
        if (dto.getDateOfBirth() != null) {
            int age = calculateAge(dto.getDateOfBirth());
            if (age < 18) throw new IllegalArgumentException(messageUtil.get("employee.age.minimum"));
            employee.setDateOfBirth(dto.getDateOfBirth());
        }
        if (dto.getSalary() != null && dto.getSalary().compareTo(BigDecimal.ZERO) > 0) {
            employee.setSalary(dto.getSalary());
        }
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(messageUtil.get("department.not.found.generic")));
            employee.setDepartment(department);
        }
        return mapToResponseDTO(employeeRepository.save(employee));
    }

    // Deactivate
    public void delete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtil.get("employee.not.found", id)));
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    // Reactivate
    public EmployeeResponseDTO activate(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtil.get("employee.not.found", id)));

        if (!employee.getDepartment().isActive())
            throw new IllegalArgumentException(
                    messageUtil.get("employee.department.inactive", employee.getDepartment().getName()));

        employee.setActive(true);
        return mapToResponseDTO(employeeRepository.save(employee));
    }
    // ───── Search ─────

    public EmployeeResponseDTO getByEmployeeId(Long employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtil.get("employee.not.found.employee.id", employeeId)));
        return mapToResponseDTO(employee);
    }

    // ADD THIS
    public List<EmployeeResponseDTO> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(messageUtil.get("employee.name.search.required"));
        }
        List<Employee> employees = employeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);

        if (employees.isEmpty()) {
            throw new ResourceNotFoundException(messageUtil.get("employee.name.not.found", name));

        }

        List<EmployeeResponseDTO> result = new ArrayList<>();
        for (Employee emp : employees) {
            result.add(mapToResponseDTO(emp));
        }
        return result;
    }
    // ───── Combined Search + Filter ─────

    public Page<EmployeeResponseDTO> searchAndFilter(
            String name, Long departmentId, Boolean active,
            Integer minAge, Integer maxAge,
            int page, int size) {

        String cleanName = (name == null || name.trim().isEmpty()) ? null : name.trim();

        // Convert age to dateOfBirth range
        // minAge=30 → show employees aged 30 and above → dateOfBirth <= today minus 30 years
        LocalDate minDob = (minAge != null) ? LocalDate.now().minusYears(minAge) : null;

        // maxAge=31 → show employees aged 31 and below → dateOfBirth >= today minus 31 years minus 1 day + 1 day
        // simplified: dateOfBirth >= today minus (maxAge + 1) years + 1 day
        LocalDate maxDob = (maxAge != null) ? LocalDate.now().minusYears(maxAge + 1).plusDays(1) : null;
        return employeeRepository.searchAndFilterPageable(
                        cleanName, departmentId, active, minDob, maxDob, PageRequest.of(page, size))
                .map(this::mapToResponseDTO);
    }

    // ───── Filter ─────

    public List<EmployeeResponseDTO> filterByDepartment(Long departmentId) {
        if (departmentId == null) {
            throw new IllegalArgumentException(messageUtil.get("department.not.found.generic"));
        }
        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtil.get("department.not.found", departmentId)));
        List<Employee> employees = employeeRepository.findByDepartmentIdAndActive(departmentId, true);
        List<EmployeeResponseDTO> result = new ArrayList<>();
        for (Employee emp : employees) {
            result.add(mapToResponseDTO(emp));
        }
        return result;
    }

    // ───── Reports (Paginated) ─────

    public Page<EmployeeResponseDTO> getByDepartmentPaged(Long departmentId, Boolean active, int page, int size) {
        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtil.get("department.not.found", departmentId)));
        return employeeRepository
                .findByDepartmentIdAndActivePaged(departmentId, active, PageRequest.of(page, size))
                .map(this::mapToResponseDTO);
    }

    public Page<EmployeeResponseDTO> getAllOrderedByAgePaged(Boolean active, Integer minAge, Integer maxAge, String sort, int page, int size) {
        LocalDate minDob = (minAge != null) ? LocalDate.now().minusYears(minAge) : null;
        LocalDate maxDob = (maxAge != null) ? LocalDate.now().minusYears(maxAge + 1).plusDays(1) : null;

        // 'asc' = youngest first = dateOfBirth DESC (most recent birthdate first)
        // 'desc' = oldest first = dateOfBirth ASC (earliest birthdate first)
        Sort.Direction direction = "asc".equals(sort) ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest  = PageRequest.of(page, size, Sort.by(direction, "dateOfBirth"));

        return employeeRepository
                .findAllActiveOrderByDateOfBirthAscPaged(active, minDob, maxDob, pageRequest)
                .map(this::mapToResponseDTO);
    }

    // ───── Calculations ─────

    public BigDecimal getAverageSalary(Long departmentId, Boolean active, Integer minAge, Integer maxAge) {
        LocalDate minDob = (minAge != null) ? LocalDate.now().minusYears(minAge) : null;
        LocalDate maxDob = (maxAge != null) ? LocalDate.now().minusYears(maxAge + 1).plusDays(1) : null;
        List<Employee> employees = employeeRepository.findForStats(departmentId, active, minDob, maxDob);
        if (employees.isEmpty()) return BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (Employee emp : employees) total = total.add(emp.getSalary());
        return total.divide(BigDecimal.valueOf(employees.size()), 2, RoundingMode.HALF_UP);
    }

    public double getAverageAge(Long departmentId, Boolean active, Integer minAge, Integer maxAge) {
        LocalDate minDob = (minAge != null) ? LocalDate.now().minusYears(minAge) : null;
        LocalDate maxDob = (maxAge != null) ? LocalDate.now().minusYears(maxAge + 1).plusDays(1) : null;
        List<Employee> employees = employeeRepository.findForStats(departmentId, active, minDob, maxDob);
        if (employees.isEmpty()) return 0.0;
        int totalAge = 0;
        for (Employee emp : employees) totalAge += emp.getAge();
        return (double) totalAge / employees.size();
    }

    // ───── Helpers ─────

    private int calculateAge(LocalDate dateOfBirth) {
        return java.time.Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    // ───── Generates the EmpID ─────
    private Long generateEmployeeId() {
        Long employeeId;
        do {
            employeeId = (long) (Math.random() * 900000) + 100000;
        } while (employeeRepository.findByEmployeeId(employeeId).isPresent());
        return employeeId;
    }

    private EmployeeResponseDTO mapToResponseDTO(Employee employee) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(employee.getId());
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setFullName(employee.getFullName());
        dto.setDateOfBirth(employee.getDateOfBirth());
        dto.setAge(employee.getAge());
        dto.setDepartmentName(employee.getDepartment() != null
                ? employee.getDepartment().getName() : "Unassigned");
        dto.setSalary(employee.getSalary());
        dto.setActive(employee.isActive());
        return dto;
    }
}
