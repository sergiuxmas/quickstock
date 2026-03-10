package com.quickstock.core.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayMigrationRunner {
    // Intentionally empty: rely on Spring Boot Flyway auto-configuration
    // to run migrations before Hibernate validates schema.
}
