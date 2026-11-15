package com.technicel.security;

import com.technicel.entity.Role;

public record UserPrincipal(Long id, String nip, Role role, boolean active) {
}
