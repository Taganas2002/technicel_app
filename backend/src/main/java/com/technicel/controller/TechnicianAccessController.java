package com.technicel.controller;

import com.technicel.dto.UserResponse;
import com.technicel.entity.User;
import com.technicel.mapper.UserMapper;
import com.technicel.repository.UserRepository;
import com.technicel.security.CurrentUserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/technicians")
public class TechnicianAccessController {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public TechnicianAccessController(
            CurrentUserService currentUserService,
            UserRepository userRepository,
            UserMapper userMapper
    ) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getTechnician(@PathVariable Long id) {
        currentUserService.assertCanAccessTechnician(id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Technician was not found"));
        return ResponseEntity.ok(userMapper.toResponse(user));
    }
}
