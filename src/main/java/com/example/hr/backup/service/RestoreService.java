package com.example.hr.backup.service;

import com.example.hr.backup.entity.BackupHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestoreService {

    private final BackupService backupService;

    @Value("${app.backup.directory:./backups}")
    private String backupDirectory;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    /**
     * Restore from backup
     */
    public boolean restoreFromBackup(Integer backupId) {
        try {
            log.info("Starting restore from backup ID: {}", backupId);

            BackupHistory backup = backupService.getBackupById(backupId);
            File backupFile = new File(backup.getBackupPath());

            if (!backupFile.exists()) {
                throw new RuntimeException("Backup file not found: " + backup.getBackupPath());
            }

            boolean success = false;

            if ("DATABASE".equals(backup.getBackupType())) {
                // Restore SQL file directly
                success = restoreSQLFile(backup.getBackupPath());
            } else if ("FULL".equals(backup.getBackupType())) {
                // Extract ZIP and restore
                success = restoreFromZip(backup.getBackupPath());
            }

            if (success) {
                log.info("Restore completed successfully from backup: {}", backup.getBackupName());
            } else {
                log.error("Restore failed from backup: {}", backup.getBackupName());
            }

            return success;

        } catch (Exception e) {
            log.error("Error restoring from backup", e);
            throw new RuntimeException("Failed to restore from backup: " + e.getMessage());
        }
    }

    /**
     * Restore from ZIP backup
     */
    private boolean restoreFromZip(String zipFilePath) {
        try {
            String tempDir = backupDirectory + "/temp_restore";
            Path tempPath = Paths.get(tempDir);
            
            // Create temp directory
            if (!Files.exists(tempPath)) {
                Files.createDirectories(tempPath);
            }

            // Extract ZIP file
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    String fileName = entry.getName();
                    File extractedFile = new File(tempDir + "/" + fileName);

                    // Create parent directories
                    extractedFile.getParentFile().mkdirs();

                    // Extract file
                    try (FileOutputStream fos = new FileOutputStream(extractedFile)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }

                    // If it's a SQL file, restore it
                    if (fileName.endsWith(".sql")) {
                        restoreSQLFile(extractedFile.getAbsolutePath());
                    }

                    zis.closeEntry();
                }
            }

            // Clean up temp directory
            deleteDirectory(tempPath);

            return true;

        } catch (Exception e) {
            log.error("Error restoring from ZIP", e);
            return false;
        }
    }

    /**
     * Restore SQL file to database
     */
    private boolean restoreSQLFile(String sqlFilePath) {
        try {
            String dbName = extractDatabaseName(databaseUrl);
            String host = extractHost(databaseUrl);

            // Build mysql command
            String command = String.format(
                "mysql -h %s -u %s -p%s %s < %s",
                host, databaseUsername, databasePassword, dbName, sqlFilePath
            );

            Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                // Read error stream
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.error("MySQL restore error: {}", line);
                    }
                }
            }

            return exitCode == 0;

        } catch (Exception e) {
            log.error("Error restoring SQL file", e);
            return false;
        }
    }

    /**
     * Validate backup file before restore
     */
    public boolean validateBackup(Integer backupId) {
        try {
            BackupHistory backup = backupService.getBackupById(backupId);
            File backupFile = new File(backup.getBackupPath());

            if (!backupFile.exists()) {
                log.error("Backup file not found: {}", backup.getBackupPath());
                return false;
            }

            if (backupFile.length() == 0) {
                log.error("Backup file is empty: {}", backup.getBackupPath());
                return false;
            }

            // Additional validation for ZIP files
            if (backup.getBackupPath().endsWith(".zip")) {
                return validateZipFile(backup.getBackupPath());
            }

            return true;

        } catch (Exception e) {
            log.error("Error validating backup", e);
            return false;
        }
    }

    /**
     * Validate ZIP file integrity
     */
    private boolean validateZipFile(String zipFilePath) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            boolean hasSQLFile = false;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".sql")) {
                    hasSQLFile = true;
                }
                zis.closeEntry();
            }

            return hasSQLFile;

        } catch (Exception e) {
            log.error("Error validating ZIP file", e);
            return false;
        }
    }

    /**
     * Extract database name from JDBC URL
     */
    private String extractDatabaseName(String jdbcUrl) {
        String[] parts = jdbcUrl.split("/");
        String dbPart = parts[parts.length - 1];
        return dbPart.split("\\?")[0];
    }

    /**
     * Extract host from JDBC URL
     */
    private String extractHost(String jdbcUrl) {
        String[] parts = jdbcUrl.split("//");
        if (parts.length > 1) {
            String hostPart = parts[1].split("/")[0];
            return hostPart.split(":")[0];
        }
        return "localhost";
    }

    /**
     * Delete directory recursively
     */
    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.error("Error deleting file: {}", p, e);
                        }
                    });
        }
    }
}


