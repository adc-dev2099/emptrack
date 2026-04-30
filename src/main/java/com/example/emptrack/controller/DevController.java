package com.example.emptrack.controller;

import com.example.emptrack.preset.EmpTrackerPreset;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DevController {

    private final EmpTrackerPreset preset;

    public DevController(EmpTrackerPreset preset) {
        this.preset = preset;
    }

    @GetMapping("/dev/reseed")
    public String reseed() {
        return preset.reseedDatabase();
    }

    @GetMapping("/dev/bcrypt")
    public String bcrypt() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .encode("admin123");
    }
}