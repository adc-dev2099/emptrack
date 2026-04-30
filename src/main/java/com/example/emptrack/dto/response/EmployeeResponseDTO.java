package com.example.emptrack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDTO {

    private Long id;
    private Long employeeId;
    private String firstName;
    private String lastName;
    private String fullName;
    private Integer age;
    private LocalDate dateOfBirth;
    private String departmentName;
    private BigDecimal salary;
    private Boolean active;
}