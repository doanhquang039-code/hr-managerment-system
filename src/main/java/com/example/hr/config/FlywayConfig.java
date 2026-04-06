package com.example.hr.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void initFlyway() {
        System.out.println("=== Starting Flyway Migration ===");
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .baselineOnMigrate(true)
                    .locations("classpath:db/migration")
                    .load();
            
            // Delete and recreate migration history (cleanup failed migrations)
            System.out.println("Attempting Flyway repair...");
            try {
                flyway.repair();
            } catch (Exception repairError) {
                System.out.println("Repair not available or failed (non-critical): " + repairError.getMessage());
            }

            // Then migrate
            System.out.println("Executing migrations...");
            flyway.migrate();
            System.out.println("=== Flyway Migration Completed Successfully ===");
        } catch (Exception e) {
            System.err.println("Error during Flyway initialization: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Flyway migration failed", e);
        }
    }
}