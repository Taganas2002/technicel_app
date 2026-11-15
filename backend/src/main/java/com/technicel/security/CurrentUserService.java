package com.technicel.security;

import com.technicel.entity.Role;
import com.technicel.entity.User;
import com.technicel.entity.WorkList;
import com.technicel.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserPrincipal requirePrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("Authenticated user is required");
        }
        return principal;
    }

    public User requireCurrentUser() {
        UserPrincipal principal = requirePrincipal();
        return userRepository.findById(principal.id())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user was not found"));
    }

    public void assertCanAccessTechnician(Long technicianId) {
        UserPrincipal principal = requirePrincipal();
        if (principal.role() == Role.ADMIN) {
            return;
        }
        if (!principal.id().equals(technicianId)) {
            throw new AccessDeniedException("You cannot access another technician's data");
        }
    }

    public User requireTechnician() {
        User user = requireCurrentUser();
        if (user.getRole() != Role.TECHNICIAN) {
            throw new AccessDeniedException("Only technicians can perform this action");
        }
        return user;
    }

    public void assertCanAccessWorkList(WorkList workList) {
        assertCanAccessTechnician(workList.getTechnician().getId());
    }
}
