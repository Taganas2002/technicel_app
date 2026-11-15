package com.technicel.controller;

import com.technicel.dto.CreateTechnicianRequest;
import com.technicel.dto.PageResponse;
import com.technicel.dto.UpdateTechnicianRequest;
import com.technicel.dto.UpdateTechnicianStatusRequest;
import com.technicel.dto.UserResponse;
import com.technicel.service.TechnicianService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/technicians")
public class AdminTechnicianController {

    private final TechnicianService technicianService;

    public AdminTechnicianController(TechnicianService technicianService) {
        this.technicianService = technicianService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateTechnicianRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(technicianService.create(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> list(
            @PageableDefault(size = 20, sort = "technicianCode") Pageable pageable
    ) {
        return ResponseEntity.ok(technicianService.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(technicianService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTechnicianRequest request
    ) {
        return ResponseEntity.ok(technicianService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTechnicianStatusRequest request
    ) {
        return ResponseEntity.ok(technicianService.updateStatus(id, request));
    }
}
