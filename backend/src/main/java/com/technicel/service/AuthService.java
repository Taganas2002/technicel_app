package com.technicel.service;

import com.technicel.dto.LoginRequest;
import com.technicel.dto.LoginResponse;
import com.technicel.entity.User;
import com.technicel.mapper.UserMapper;
import com.technicel.repository.UserRepository;
import com.technicel.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByNip(request.getNip())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if (!user.isActive()) {
            throw new DisabledException("Account is inactive");
        }

        String token = jwtService.generateToken(user.getId(), user.getNip(), user.getRole());
        return userMapper.toLoginResponse(user, token);
    }
}
