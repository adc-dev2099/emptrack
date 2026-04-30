package com.example.emptrack.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
// Abstract
public abstract class Person {
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    // Concrete shared method (Inheritance)
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    // Abstract method (Abstraction + Polymorphism)
    // Each subclass must define how they identify themselves
    public abstract String getIdentifier();
}
