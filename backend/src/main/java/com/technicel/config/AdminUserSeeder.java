package com.technicel.config;

import com.technicel.entity.Role;
import com.technicel.entity.User;
import com.technicel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public AdminUserSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AppProperties appProperties
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        AppProperties.Admin admin = appProperties.getAdmin();
        if (userRepository.existsByNip(admin.getNip())) {
            return;
        }

        User user = new User();
        user.setTechnicianCode(admin.getTechnicianCode());
        user.setFirstName(admin.getFirstName());
        user.setLastName(admin.getLastName());
        user.setNip(admin.getNip());
        user.setPassword(passwordEncoder.encode(admin.getPassword()));
        user.setRole(Role.ADMIN);
        user.setActive(true);
        userRepository.save(user);
        log.info("Seeded local admin user with NIP {}", admin.getNip());
    }
}
