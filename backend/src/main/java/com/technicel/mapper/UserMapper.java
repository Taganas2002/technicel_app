package com.technicel.mapper;

import com.technicel.dto.LoginResponse;
import com.technicel.dto.UserResponse;
import com.technicel.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getTechnicianCode(),
                user.getFirstName(),
                user.getLastName(),
                user.getNip(),
                user.getRole(),
                user.isActive()
        );
    }

    public LoginResponse toLoginResponse(User user, String accessToken) {
        return new LoginResponse(
                accessToken,
                user.getId(),
                user.getTechnicianCode(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }
}
