package com.example.hr.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class GoogleDriveConfig {

    private static final Logger log = LoggerFactory.getLogger(GoogleDriveConfig.class);

    // Đọc nội dung JSON từ env variable
    @Value("${google.drive.service-account-json:}")
    private String serviceAccountJson;

    // Vẫn giữ path để dùng khi chạy local (fallback)
    @Value("${google.drive.service-account-path:google-drive-service-account.json}")
    private String serviceAccountPath;

    @Bean
    @ConditionalOnProperty(name = "google.drive.enabled", havingValue = "true")
    public Drive googleDriveService() throws Exception {
        InputStream serviceAccount;

        // Ưu tiên đọc từ JSON string (dùng khi deploy)
        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            log.info("Google Drive: loading credentials from JSON env variable");
            serviceAccount = new ByteArrayInputStream(
                serviceAccountJson.getBytes(StandardCharsets.UTF_8)
            );
        } else {
            // Fallback: đọc từ file (dùng khi chạy local)
            log.info("Google Drive: loading credentials from file: {}", serviceAccountPath);
            InputStream classpathStream = getClass().getClassLoader()
                    .getResourceAsStream(serviceAccountPath);
            if (classpathStream != null) {
                serviceAccount = classpathStream;
            } else {
                serviceAccount = new FileInputStream(serviceAccountPath);
            }
        }

        GoogleCredentials credentials = ServiceAccountCredentials
                .fromStream(serviceAccount)
                .createScoped(List.of(DriveScopes.DRIVE));

        Drive drive = new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("HRMS Document Manager")
                .build();

        log.info("Google Drive service initialized successfully");
        return drive;
    }
}