package com.technicel.dto;

import com.technicel.entity.Role;

public class UserResponse {

    private Long id;
    private String technicianCode;
    private String firstName;
    private String lastName;
    private String nip;
    private Role role;
    private boolean active;

    public UserResponse(
            Long id,
            String technicianCode,
            String firstName,
            String lastName,
            String nip,
            Role role,
            boolean active
    ) {
        this.id = id;
        this.technicianCode = technicianCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nip = nip;
        this.role = role;
        this.active = active;
    }

    public Long getId() {
        return id;
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

    public String getNip() {
        return nip;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }
}
