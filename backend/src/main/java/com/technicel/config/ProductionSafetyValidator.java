package com.technicel.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductionSafetyValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductionSafetyValidator.class);
    private static final String DEFAULT_JWT_SECRET = "change-me-to-a-long-random-secret";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private final AppProperties appProperties;

    public ProductionSafetyValidator(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        String secret = appProperties.getJwt().getSecret();
        if (secret == null || secret.isBlank() || DEFAULT_JWT_SECRET.equals(secret) || secret.length() < 32) {
            throw new IllegalStateException(
                    "Production requires JWT_SECRET to be a unique value at least 32 characters long"
            );
        }
        String adminPassword = appProperties.getAdmin().getPassword();
        if (adminPassword == null || adminPassword.isBlank() || DEFAULT_ADMIN_PASSWORD.equals(adminPassword)) {
            throw new IllegalStateException("Production requires ADMIN_PASSWORD to be set to a strong value");
        }
        log.info("Production safety checks passed");
    }
}
