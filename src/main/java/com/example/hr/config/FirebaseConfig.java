package com.example.hr.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    // Đọc nội dung JSON từ env variable
    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    // Vẫn giữ path để dùng khi chạy local (fallback)
    @Value("${firebase.service-account-path:firebase-service-account.json}")
    private String serviceAccountPath;

    @Value("${firebase.database-url:}")
    private String databaseUrl;

    @Bean
    @ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        InputStream serviceAccount;

        // Ưu tiên đọc từ JSON string (dùng khi deploy)
        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            log.info("Firebase: loading credentials from JSON env variable");
            serviceAccount = new ByteArrayInputStream(
                serviceAccountJson.getBytes(StandardCharsets.UTF_8)
            );
        } else {
            // Fallback: đọc từ file (dùng khi chạy local)
            log.info("Firebase: loading credentials from file: {}", serviceAccountPath);
            InputStream classpathStream = getClass().getClassLoader()
                    .getResourceAsStream(serviceAccountPath);
            if (classpathStream != null) {
                serviceAccount = classpathStream;
            } else {
                serviceAccount = new FileInputStream(serviceAccountPath);
            }
        }

        FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount));

        if (databaseUrl != null && !databaseUrl.isBlank()) {
            optionsBuilder.setDatabaseUrl(databaseUrl);
        }

        FirebaseApp app = FirebaseApp.initializeApp(optionsBuilder.build());
        log.info("Firebase initialized successfully");
        return app;
    }
}