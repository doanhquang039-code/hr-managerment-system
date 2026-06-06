package com.example.hr.service;

import com.example.hr.models.BackupHistory;
import com.example.hr.models.User;
import com.example.hr.repository.BackupHistoryRepository;
import com.example.hr.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackupService {

    private final BackupHistoryRepository backupHistoryRepository;
    private final UserRepository userRepository;

    @Value("${app.backup.directory:./backups}")
    private String backupDirectory;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /**
     * Create a full system backup
     */
    @Transactional
    public BackupHistory createFullBackup(Integer createdByUserId) {
        try {
            log.info("Starting full system backup...");
            
            // Fetch user
            User createdByUser = userRepository.findById(createdByUserId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + createdByUserId));
            
            // Create backup directory if not exists
            Path backupPath = Paths.get(backupDirectory);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }

            // Generate backup filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = "backup_full_" + timestamp + ".zip";
            String backupFilePath = backupDirectory + "/" + backupFileName;

            // Create backup history record
            BackupHistory backup = new BackupHistory();
            backup.setBackupType("FULL");
            backup.setBackupName(backupFileName);
            backup.setBackupPath(backupFilePath);
            backup.setCreatedBy(createdByUser);
            backup.setCreatedAt(LocalDateTime.now());
            backup.setStatus("IN_PROGRESS");

            backup = backupHistoryRepository.save(backup);

            // Perform database backup
            boolean dbBackupSuccess = backupDatabase(backupFilePath);

            if (dbBackupSuccess) {
                // Calculate backup size
                File backupFile = new File(backupFilePath);
                long fileSize = backupFile.length();
                backup.setFileSize(fileSize);
                backup.setStatus("COMPLETED");
                backup.setCompletedAt(LocalDateTime.now());
                
                log.info("Backup completed successfully: {}", backupFileName);
            } else {
                backup.setStatus("FAILED");
                backup.setErrorMessage("Database backup failed");
                log.error("Backup failed: {}", backupFileName);
            }

            return backupHistoryRepository.save(backup);

        } catch (Exception e) {
            log.error("Error creating backup", e);
            throw new RuntimeException("Failed to create backup: " + e.getMessage());
        }
    }

    /**
     * Create a database-only backup
     */
    @Transactional
    public BackupHistory createDatabaseBackup(Integer createdByUserId) {
        try {
            log.info("Starting database backup...");
            
            // Fetch user
            User createdByUser = userRepository.findById(createdByUserId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + createdByUserId));
            
            Path backupPath = Paths.get(backupDirectory);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = "backup_db_" + timestamp + ".sql";
            String backupFilePath = backupDirectory + "/" + backupFileName;

            BackupHistory backup = new BackupHistory();
            backup.setBackupType("DATABASE");
            backup.setBackupName(backupFileName);
            backup.setBackupPath(backupFilePath);
            backup.setCreatedBy(createdByUser);
            backup.setCreatedAt(LocalDateTime.now());
            backup.setStatus("IN_PROGRESS");

            backup = backupHistoryRepository.save(backup);

            // Execute mysqldump command
            boolean success = executeMySQLDump(backupFilePath);

            if (success) {
                File backupFile = new File(backupFilePath);
                backup.setFileSize(backupFile.length());
                backup.setStatus("COMPLETED");
                backup.setCompletedAt(LocalDateTime.now());
            } else {
                backup.setStatus("FAILED");
                backup.setErrorMessage("MySQL dump failed");
            }

            return backupHistoryRepository.save(backup);

        } catch (Exception e) {
            log.error("Error creating database backup", e);
            throw new RuntimeException("Failed to create database backup: " + e.getMessage());
        }
    }

    /**
     * Get all backup history
     */
    public List<BackupHistory> getAllBackups() {
        return backupHistoryRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get backup by ID
     */
    public BackupHistory getBackupById(Integer id) {
        return backupHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Backup not found with id: " + id));
    }

    /**
     * Delete backup
     */
    @Transactional
    public void deleteBackup(Integer id) {
        BackupHistory backup = getBackupById(id);
        
        // Delete physical file
        try {
            Path filePath = Paths.get(backup.getBackupPath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Error deleting backup file", e);
        }

        // Delete database record
        backupHistoryRepository.delete(backup);
    }

    /**
     * Download backup file
     */
    public File getBackupFile(Integer id) {
        BackupHistory backup = getBackupById(id);
        File file = new File(backup.getBackupPath());
        
        if (!file.exists()) {
            throw new RuntimeException("Backup file not found: " + backup.getBackupPath());
        }
        
        return file;
    }

    /**
     * Execute MySQL dump command
     */
    private boolean executeMySQLDump(String outputPath) {
        try {
            // Extract database name from URL
            String dbName = extractDatabaseName(databaseUrl);
            String host = extractHost(databaseUrl);
            
            // Build mysqldump command
            String command = String.format(
                "mysqldump -h %s -u %s -p%s %s > %s",
                host, databaseUsername, databasePassword, dbName, outputPath
            );

            Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
            int exitCode = process.waitFor();

            return exitCode == 0;

        } catch (Exception e) {
            log.error("Error executing mysqldump", e);
            return false;
        }
    }

    /**
     * Backup database to ZIP file
     */
    private boolean backupDatabase(String zipFilePath) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String sqlFileName = "database_" + timestamp + ".sql";
            String tempSqlPath = backupDirectory + "/" + sqlFileName;

            // Execute mysqldump
            boolean dumpSuccess = executeMySQLDump(tempSqlPath);
            
            if (!dumpSuccess) {
                return false;
            }

            // Create ZIP file
            try (FileOutputStream fos = new FileOutputStream(zipFilePath);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                
                File sqlFile = new File(tempSqlPath);
                try (FileInputStream fis = new FileInputStream(sqlFile)) {
                    ZipEntry zipEntry = new ZipEntry(sqlFileName);
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    
                    zos.closeEntry();
                }

                // Delete temp SQL file
                Files.deleteIfExists(Paths.get(tempSqlPath));
            }

            return true;

        } catch (Exception e) {
            log.error("Error backing up database", e);
            return false;
        }
    }

    /**
     * Extract database name from JDBC URL
     */
    private String extractDatabaseName(String jdbcUrl) {
        // jdbc:mysql://localhost:3306/hr_management?useSSL=false
        String[] parts = jdbcUrl.split("/");
        String dbPart = parts[parts.length - 1];
        return dbPart.split("\\?")[0];
    }

    /**
     * Extract host from JDBC URL
     */
    private String extractHost(String jdbcUrl) {
        // jdbc:mysql://localhost:3306/hr_management
        String[] parts = jdbcUrl.split("//");
        if (parts.length > 1) {
            String hostPart = parts[1].split("/")[0];
            return hostPart.split(":")[0];
        }
        return "localhost";
    }

    /**
     * Get backup statistics
     */
    public BackupStatistics getBackupStatistics() {
        List<BackupHistory> allBackups = getAllBackups();
        
        long totalBackups = allBackups.size();
        long completedBackups = allBackups.stream()
                .filter(b -> "COMPLETED".equals(b.getStatus()))
                .count();
        long failedBackups = allBackups.stream()
                .filter(b -> "FAILED".equals(b.getStatus()))
                .count();
        long totalSize = allBackups.stream()
                .filter(b -> b.getFileSize() != null)
                .mapToLong(BackupHistory::getFileSize)
                .sum();

        BackupHistory latestBackup = allBackups.isEmpty() ? null : allBackups.get(0);

        return new BackupStatistics(
                totalBackups,
                completedBackups,
                failedBackups,
                totalSize,
                latestBackup
        );
    }

    /**
     * Backup statistics DTO
     */
    public record BackupStatistics(
            long totalBackups,
            long completedBackups,
            long failedBackups,
            long totalSize,
            BackupHistory latestBackup
    ) {}
}
