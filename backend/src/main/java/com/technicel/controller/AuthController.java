package com.technicel.controller;

import com.technicel.dto.LoginRequest;
import com.technicel.dto.LoginResponse;
import com.technicel.dto.UserResponse;
import com.technicel.mapper.UserMapper;
import com.technicel.security.CurrentUserService;
import com.technicel.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;
    private final UserMapper userMapper;

    public AuthController(
            AuthService authService,
            CurrentUserService currentUserService,
            UserMapper userMapper
    ) {
        this.authService = authService;
        this.currentUserService = currentUserService;
        this.userMapper = userMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(userMapper.toResponse(currentUserService.requireCurrentUser()));
    }
}
