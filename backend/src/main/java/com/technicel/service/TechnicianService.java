package com.technicel.service;

import com.technicel.dto.CreateTechnicianRequest;
import com.technicel.dto.PageResponse;
import com.technicel.dto.UpdateTechnicianRequest;
import com.technicel.dto.UpdateTechnicianStatusRequest;
import com.technicel.dto.UserResponse;
import com.technicel.entity.Role;
import com.technicel.entity.User;
import com.technicel.exception.DuplicateResourceException;
import com.technicel.mapper.UserMapper;
import com.technicel.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class TechnicianService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public TechnicianService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse create(CreateTechnicianRequest request) {
        String code = StringUtils.hasText(request.getTechnicianCode())
                ? request.getTechnicianCode().trim()
                : nextTechnicianCode();
        String nip = request.getNip().trim();
        assertUniqueCode(code, null);
        assertUniqueNip(nip, null);

        User user = new User();
        user.setTechnicianCode(code);
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setNip(nip);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.TECHNICIAN);
        user.setActive(request.getActive() == null || request.getActive());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(Pageable pageable) {
        Page<UserResponse> page = userRepository.findByRole(Role.TECHNICIAN, pageable)
                .map(userMapper::toResponse);
        return new PageResponse<>(page);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return userMapper.toResponse(requireTechnician(id));
    }

    @Transactional
    public UserResponse update(Long id, UpdateTechnicianRequest request) {
        User user = requireTechnician(id);
        String code = request.getTechnicianCode().trim();
        String nip = request.getNip().trim();
        assertUniqueCode(code, id);
        assertUniqueNip(nip, id);

        user.setTechnicianCode(code);
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setNip(nip);
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateStatus(Long id, UpdateTechnicianStatusRequest request) {
        User user = requireTechnician(id);
        user.setActive(request.getActive());
        return userMapper.toResponse(userRepository.save(user));
    }

    private String nextTechnicianCode() {
        long next = userRepository.count() + 1;
        String code = "T" + next;
        while (userRepository.existsByTechnicianCode(code)) {
            next += 1;
            code = "T" + next;
        }
        return code;
    }

    private User requireTechnician(Long id) {
        return userRepository.findByIdAndRole(id, Role.TECHNICIAN)
                .orElseThrow(() -> new EntityNotFoundException("Technician was not found"));
    }

    private void assertUniqueCode(String technicianCode, Long currentId) {
        boolean taken = currentId == null
                ? userRepository.existsByTechnicianCode(technicianCode)
                : userRepository.existsByTechnicianCodeAndIdNot(technicianCode, currentId);
        if (taken) {
            throw new DuplicateResourceException("Technician code is already in use");
        }
    }

    private void assertUniqueNip(String nip, Long currentId) {
        boolean taken = currentId == null
                ? userRepository.existsByNip(nip)
                : userRepository.existsByNipAndIdNot(nip, currentId);
        if (taken) {
            throw new DuplicateResourceException("NIP is already in use");
        }
    }
}
