package com.example.hr.service;

import com.example.hr.models.User;
import com.example.hr.department.entity.Department;
import com.example.hr.recruitment.entity.JobPosition;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.department.repository.DepartmentRepository;
import com.example.hr.recruitment.repository.JobPositionRepository;
import com.example.hr.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkOperationService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final JobPositionRepository jobPositionRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Import users from Excel file
     */
    @Transactional
    public BulkImportResult importUsersFromExcel(MultipartFile file) {
        BulkImportResult result = new BulkImportResult();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
            
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                try {
                    User user = parseUserFromRow(row);
                    userRepository.save(user);
                    result.addSuccess(user.getUsername());
                } catch (Exception e) {
                    result.addError(row.getRowNum(), e.getMessage());
                    log.error("Error importing user at row {}", row.getRowNum(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error importing users from Excel", e);
            throw new RuntimeException("Failed to import users: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Export users to Excel file
     */
    public ByteArrayOutputStream exportUsersToExcel(List<User> users) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Users");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Username", "Employee Code", "Full Name", "Email", "Phone",
                "Gender", "Date of Birth", "Address", "Hire Date",
                "Department", "Position", "Role", "Status"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                
                // Style header
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(user.getUsername());
                row.createCell(1).setCellValue(user.getEmployeeCode());
                row.createCell(2).setCellValue(user.getFullName());
                row.createCell(3).setCellValue(user.getEmail());
                row.createCell(4).setCellValue(user.getPhoneNumber());
                row.createCell(5).setCellValue(user.getGender());
                row.createCell(6).setCellValue(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : "");
                row.createCell(7).setCellValue(user.getAddress());
                row.createCell(8).setCellValue(user.getHireDate() != null ? user.getHireDate().toString() : "");
                row.createCell(9).setCellValue(user.getDepartment() != null ? user.getDepartment().getName() : "");
                row.createCell(10).setCellValue(user.getPosition() != null ? user.getPosition().getTitle() : "");
                row.createCell(11).setCellValue(user.getRole().name());
                row.createCell(12).setCellValue(user.getStatus().name());
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out;
            
        } catch (Exception e) {
            log.error("Error exporting users to Excel", e);
            throw new RuntimeException("Failed to export users: " + e.getMessage());
        }
    }

    /**
     * Bulk update users
     */
    @Transactional
    public BulkUpdateResult bulkUpdateUsers(List<Integer> userIds, BulkUpdateRequest request) {
        BulkUpdateResult result = new BulkUpdateResult();
        
        for (Integer userId : userIds) {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found: " + userId));
                
                // Update fields if provided
                if (request.departmentId() != null) {
                    Department department = departmentRepository.findById(request.departmentId())
                            .orElseThrow(() -> new RuntimeException("Department not found"));
                    user.setDepartment(department);
                }
                
                if (request.positionId() != null) {
                    JobPosition position = jobPositionRepository.findById(request.positionId())
                            .orElseThrow(() -> new RuntimeException("Position not found"));
                    user.setPosition(position);
                }
                
                if (request.status() != null) {
                    user.setStatus(request.status());
                }
                
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                result.addSuccess(userId);
                
            } catch (Exception e) {
                result.addError(userId, e.getMessage());
                log.error("Error updating user {}", userId, e);
            }
        }
        
        return result;
    }

    /**
     * Bulk delete users
     */
    @Transactional
    public BulkDeleteResult bulkDeleteUsers(List<Integer> userIds) {
        BulkDeleteResult result = new BulkDeleteResult();
        
        for (Integer userId : userIds) {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found: " + userId));
                
                // Soft delete by setting status to INACTIVE
                user.setStatus(UserStatus.INACTIVE);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                
                result.addSuccess(userId);
                
            } catch (Exception e) {
                result.addError(userId, e.getMessage());
                log.error("Error deleting user {}", userId, e);
            }
        }
        
        return result;
    }

    /**
     * Bulk password reset
     */
    @Transactional
    public BulkPasswordResetResult bulkPasswordReset(List<Integer> userIds, String defaultPassword) {
        BulkPasswordResetResult result = new BulkPasswordResetResult();
        
        String encodedPassword = passwordEncoder.encode(defaultPassword);
        
        for (Integer userId : userIds) {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found: " + userId));
                
                user.setPassword(encodedPassword);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                
                result.addSuccess(userId, user.getEmail());
                
            } catch (Exception e) {
                result.addError(userId, e.getMessage());
                log.error("Error resetting password for user {}", userId, e);
            }
        }
        
        return result;
    }

    /**
     * Parse user from Excel row
     */
    private User parseUserFromRow(Row row) {
        User user = new User();
        
        user.setUsername(getCellValueAsString(row.getCell(0)));
        user.setEmployeeCode(getCellValueAsString(row.getCell(1)));
        user.setFullName(getCellValueAsString(row.getCell(2)));
        user.setEmail(getCellValueAsString(row.getCell(3)));
        user.setPhoneNumber(getCellValueAsString(row.getCell(4)));
        user.setGender(getCellValueAsString(row.getCell(5)));
        
        // Parse date of birth
        String dobStr = getCellValueAsString(row.getCell(6));
        if (dobStr != null && !dobStr.isEmpty()) {
            user.setDateOfBirth(LocalDate.parse(dobStr));
        }
        
        user.setAddress(getCellValueAsString(row.getCell(7)));
        
        // Parse hire date
        String hireDateStr = getCellValueAsString(row.getCell(8));
        if (hireDateStr != null && !hireDateStr.isEmpty()) {
            user.setHireDate(LocalDate.parse(hireDateStr));
        }
        
        // Set default password
        user.setPassword(passwordEncoder.encode("123456"));
        
        // Set timestamps
        user.setCreatedAt(LocalDateTime.now());
        user.setStatus(UserStatus.ACTIVE);
        
        return user;
    }

    /**
     * Get cell value as string
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    // Result classes
    public static class BulkImportResult {
        private final List<String> successfulImports = new ArrayList<>();
        private final Map<Integer, String> errors = new HashMap<>();
        
        public void addSuccess(String username) {
            successfulImports.add(username);
        }
        
        public void addError(int row, String error) {
            errors.put(row, error);
        }
        
        public int getSuccessCount() {
            return successfulImports.size();
        }
        
        public int getErrorCount() {
            return errors.size();
        }
        
        public List<String> getSuccessfulImports() {
            return successfulImports;
        }
        
        public Map<Integer, String> getErrors() {
            return errors;
        }
    }

    public static class BulkUpdateResult {
        private final List<Integer> successfulUpdates = new ArrayList<>();
        private final Map<Integer, String> errors = new HashMap<>();
        
        public void addSuccess(Integer userId) {
            successfulUpdates.add(userId);
        }
        
        public void addError(Integer userId, String error) {
            errors.put(userId, error);
        }
        
        public int getSuccessCount() {
            return successfulUpdates.size();
        }
        
        public int getErrorCount() {
            return errors.size();
        }
    }

    public static class BulkDeleteResult {
        private final List<Integer> successfulDeletes = new ArrayList<>();
        private final Map<Integer, String> errors = new HashMap<>();
        
        public void addSuccess(Integer userId) {
            successfulDeletes.add(userId);
        }
        
        public void addError(Integer userId, String error) {
            errors.put(userId, error);
        }
        
        public int getSuccessCount() {
            return successfulDeletes.size();
        }
        
        public int getErrorCount() {
            return errors.size();
        }
    }

    public static class BulkPasswordResetResult {
        private final Map<Integer, String> successfulResets = new HashMap<>();
        private final Map<Integer, String> errors = new HashMap<>();
        
        public void addSuccess(Integer userId, String email) {
            successfulResets.put(userId, email);
        }
        
        public void addError(Integer userId, String error) {
            errors.put(userId, error);
        }
        
        public int getSuccessCount() {
            return successfulResets.size();
        }
        
        public int getErrorCount() {
            return errors.size();
        }
        
        public Map<Integer, String> getSuccessfulResets() {
            return successfulResets;
        }
    }

    public record BulkUpdateRequest(
            Integer departmentId,
            Integer positionId,
            UserStatus status
    ) {}
}


