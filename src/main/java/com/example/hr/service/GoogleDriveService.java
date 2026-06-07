package com.example.hr.service;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Google Drive Service â€” sync tÃ i liá»‡u nhÃ¢n viÃªn, há»£p Ä‘á»“ng, bÃ¡o cÃ¡o.
 * Chá»‰ active khi google.drive.enabled=true.
 */
@ConditionalOnBean(Drive.class)
public class GoogleDriveService {

    private static final Logger log = LoggerFactory.getLogger(GoogleDriveService.class);

    private final Drive driveService;

    @Value("${google.drive.folder-id:}")
    private String rootFolderId;

    public GoogleDriveService(Drive driveService) {
        this.driveService = driveService;
    }

    // ==================== UPLOAD ====================

    /**
     * Upload file lÃªn Google Drive.
     * @return fileId trÃªn Drive
     */
    public String uploadFile(byte[] data, String fileName,
                              String mimeType, String folderId) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        if (folderId != null && !folderId.isBlank()) {
            fileMetadata.setParents(Collections.singletonList(folderId));
        } else if (rootFolderId != null && !rootFolderId.isBlank()) {
            fileMetadata.setParents(Collections.singletonList(rootFolderId));
        }

        InputStreamContent content = new InputStreamContent(mimeType,
                new ByteArrayInputStream(data));

        File uploaded = driveService.files().create(fileMetadata, content)
                .setFields("id, name, webViewLink, webContentLink")
                .execute();

        log.info("Uploaded to Drive: {} (id={})", fileName, uploaded.getId());
        return uploaded.getId();
    }

    /**
     * Upload document nhÃ¢n viÃªn vÃ o subfolder theo userId.
     */
    public String uploadEmployeeDocument(byte[] data, String fileName,
                                          String mimeType, Integer userId) throws IOException {
        // Táº¡o/láº¥y subfolder cho nhÃ¢n viÃªn
        String userFolderId = getOrCreateFolder("Employee-" + userId, rootFolderId);
        return uploadFile(data, fileName, mimeType, userFolderId);
    }

    /**
     * Upload bÃ¡o cÃ¡o vÃ o folder Reports.
     */
    public String uploadReport(byte[] data, String fileName, String mimeType) throws IOException {
        String reportsFolderId = getOrCreateFolder("Reports", rootFolderId);
        return uploadFile(data, fileName, mimeType, reportsFolderId);
    }

    /**
     * Upload há»£p Ä‘á»“ng vÃ o folder Contracts.
     */
    public String uploadContract(byte[] data, String fileName, Integer userId) throws IOException {
        String contractsFolderId = getOrCreateFolder("Contracts", rootFolderId);
        String userFolderId = getOrCreateFolder("Employee-" + userId, contractsFolderId);
        return uploadFile(data, fileName, "application/pdf", userFolderId);
    }

    // ==================== SHARE / PERMISSIONS ====================

    /**
     * Share file vá»›i email cá»¥ thá»ƒ (view only).
     */
    public void shareWithUser(String fileId, String email) throws IOException {
        Permission permission = new Permission()
                .setType("user")
                .setRole("reader")
                .setEmailAddress(email);
        driveService.permissions().create(fileId, permission)
                .setSendNotificationEmail(false)
                .execute();
        log.info("Shared Drive file {} with {}", fileId, email);
    }

    /**
     * Táº¡o public link (anyone with link can view).
     */
    public String makePublicLink(String fileId) throws IOException {
        Permission permission = new Permission()
                .setType("anyone")
                .setRole("reader");
        driveService.permissions().create(fileId, permission).execute();

        File file = driveService.files().get(fileId)
                .setFields("webViewLink")
                .execute();
        return file.getWebViewLink();
    }

    // ==================== LIST / GET ====================

    /**
     * Láº¥y web view link cá»§a file.
     */
    public String getWebViewLink(String fileId) throws IOException {
        File file = driveService.files().get(fileId)
                .setFields("webViewLink")
                .execute();
        return file.getWebViewLink();
    }

    /**
     * Liá»‡t kÃª files trong folder.
     */
    public List<java.util.Map<String, String>> listFiles(String folderId) throws IOException {
        String query = "'" + folderId + "' in parents and trashed=false";
        FileList result = driveService.files().list()
                .setQ(query)
                .setFields("files(id, name, mimeType, webViewLink, createdTime, size)")
                .execute();

        return result.getFiles().stream()
                .map(f -> java.util.Map.of(
                        "id", f.getId(),
                        "name", f.getName(),
                        "mimeType", f.getMimeType() != null ? f.getMimeType() : "",
                        "link", f.getWebViewLink() != null ? f.getWebViewLink() : "",
                        "size", f.getSize() != null ? f.getSize().toString() : "0"
                ))
                .collect(Collectors.toList());
    }

    // ==================== DELETE ====================

    /**
     * XÃ³a file trÃªn Drive.
     */
    public void deleteFile(String fileId) throws IOException {
        driveService.files().delete(fileId).execute();
        log.info("Deleted Drive file: {}", fileId);
    }

    // ==================== HEALTH ====================

    public boolean isHealthy() {
        try {
            driveService.about().get().setFields("user").execute();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== PRIVATE ====================

    /**
     * Láº¥y folder ID náº¿u Ä‘Ã£ tá»“n táº¡i, hoáº·c táº¡o má»›i.
     */
    private String getOrCreateFolder(String folderName, String parentId) throws IOException {
        // TÃ¬m folder Ä‘Ã£ tá»“n táº¡i
        String query = String.format(
                "name='%s' and mimeType='application/vnd.google-apps.folder' and trashed=false",
                folderName);
        if (parentId != null && !parentId.isBlank()) {
            query += " and '" + parentId + "' in parents";
        }

        FileList result = driveService.files().list()
                .setQ(query)
                .setFields("files(id)")
                .execute();

        if (!result.getFiles().isEmpty()) {
            return result.getFiles().get(0).getId();
        }

        // Táº¡o folder má»›i
        File folderMetadata = new File();
        folderMetadata.setName(folderName);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");
        if (parentId != null && !parentId.isBlank()) {
            folderMetadata.setParents(Collections.singletonList(parentId));
        }

        File folder = driveService.files().create(folderMetadata)
                .setFields("id")
                .execute();
        log.info("Created Drive folder: {} (id={})", folderName, folder.getId());
        return folder.getId();
    }
}


