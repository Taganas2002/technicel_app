package com.technicel.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ProductionSafetyValidatorTest {

    @Test
    void rejectsDefaultJwtSecret() {
        AppProperties properties = validProperties();
        properties.getJwt().setSecret("change-me-to-a-long-random-secret");

        assertThrows(IllegalStateException.class, () -> new ProductionSafetyValidator(properties).run(null));
    }

    @Test
    void rejectsShortJwtSecret() {
        AppProperties properties = validProperties();
        properties.getJwt().setSecret("too-short");

        assertThrows(IllegalStateException.class, () -> new ProductionSafetyValidator(properties).run(null));
    }

    @Test
    void rejectsDefaultAdminPassword() {
        AppProperties properties = validProperties();
        properties.getAdmin().setPassword("admin123");

        assertThrows(IllegalStateException.class, () -> new ProductionSafetyValidator(properties).run(null));
    }

    @Test
    void acceptsStrongProductionSecrets() {
        assertDoesNotThrow(() -> new ProductionSafetyValidator(validProperties()).run(null));
    }

    private static AppProperties validProperties() {
        AppProperties properties = new AppProperties();
        properties.getJwt().setSecret("a-unique-production-jwt-secret-32plus");
        properties.getAdmin().setPassword("not-the-local-admin-password");
        return properties;
    }
}
