package com.quickstock.core.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayMigrationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlywayMigrationRunner.class);

    @Bean
    ApplicationRunner runFlywayMigrations(DataSource dataSource) {
        return args -> {
            MigrateResult result = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .load()
                    .migrate();

            LOGGER.info("Flyway migration complete. Migrations executed: {}, target schema version: {}",
                    result.migrationsExecuted,
                    result.targetSchemaVersion);
        };
    }
}
