package com.technicel.repository;

import com.technicel.entity.Role;
import com.technicel.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByNip(String nip);

    Optional<User> findByIdAndRole(Long id, Role role);

    boolean existsByNip(String nip);

    boolean existsByTechnicianCode(String technicianCode);

    boolean existsByNipAndIdNot(String nip, Long id);

    boolean existsByTechnicianCodeAndIdNot(String technicianCode, Long id);

    Page<User> findByRole(Role role, Pageable pageable);
}
