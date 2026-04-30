package com.example.emptrack.controller;

import com.example.emptrack.preset.EmpTrackerPreset;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.dev.base}")
@RequiredArgsConstructor
@Profile("dev")
public class PresetController {
    private final EmpTrackerPreset seeder;

    @PostMapping("${api.dev.seed}")
    public ResponseEntity<String> seed() {
        return ResponseEntity.ok(seeder.reseedDatabase());
    }
}
