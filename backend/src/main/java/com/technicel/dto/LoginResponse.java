package com.technicel.dto;

import com.technicel.entity.Role;

public class LoginResponse {

    private String accessToken;
    private Long userId;
    private String technicianCode;
    private String firstName;
    private String lastName;
    private Role role;

    public LoginResponse(
            String accessToken,
            Long userId,
            String technicianCode,
            String firstName,
            String lastName,
            Role role
    ) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.technicianCode = technicianCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTechnicianCode() {
        return technicianCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Role getRole() {
        return role;
    }
}
