package com.example.hr.controllers;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.hr.enums.UserStatus;
import com.example.hr.models.User;
import com.example.hr.repository.*;
import com.example.hr.service.CloudinaryService;

import jakarta.servlet.http.HttpServletResponse;

// CHỈ IMPORT NHỮNG THỨ KHÔNG GÂY XUNG ĐỘT
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfWriter;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private JobPositionRepository positionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CloudinaryService cloudinaryService;

    private final String UPLOAD_DIR = "public/test1/";

    @GetMapping
    public String listUsers(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
        List<User> users;
        if (keyword != null && !keyword.isEmpty()) {
            users = userRepository.findByFullNameContainingAndStatus(keyword, UserStatus.ACTIVE);
        } else {
            users = userRepository.findByStatus(UserStatus.ACTIVE);
        }
        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        return "admin/user-list";
    }

    @GetMapping("/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=employees_full_report.xlsx");

        List<User> users = userRepository.findByStatus(UserStatus.ACTIVE);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Full Report");

        // Sử dụng Full Path để tránh lỗi "defined by single-type-import"
        org.apache.poi.ss.usermodel.Drawing<?> drawing = sheet.createDrawingPatriarch();

        String[] headers = { "ID", "User", "Pass", "Name", "Email", "Role", "Dept", "Pos", "Status", "Photo" };
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        int rowIdx = 1;
        for (User user : users) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx);
            row.setHeightInPoints(60);

            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getUsername());
            row.createCell(2).setCellValue("********");
            row.createCell(3).setCellValue(user.getFullName());
            row.createCell(4).setCellValue(user.getEmail());
            row.createCell(5).setCellValue(user.getRole().toString());
            row.createCell(6)
                    .setCellValue(user.getDepartment() != null ? user.getDepartment().getDepartmentName() : "N/A");
            row.createCell(7).setCellValue(user.getPosition() != null ? user.getPosition().getPositionName() : "N/A");
            row.createCell(8).setCellValue(user.getStatus().toString());

            Path path = Paths.get(UPLOAD_DIR + user.getProfileImage());
            if (Files.exists(path)) {
                try (InputStream is = new FileInputStream(path.toFile())) {
                    byte[] bytes = org.apache.poi.util.IOUtils.toByteArray(is);
                    int picIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);

                    org.apache.poi.ss.usermodel.CreationHelper helper = workbook.getCreationHelper();
                    org.apache.poi.ss.usermodel.ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setCol1(9);
                    anchor.setRow1(rowIdx);

                    org.apache.poi.ss.usermodel.Picture pict = drawing.createPicture(anchor, picIdx);
                    pict.resize(0.5, 0.5);
                }
            } else {
                row.createCell(9).setCellValue("Không có ảnh");
            }
            rowIdx++;
        }
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/export/pdf")
    public void exportToPDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=employees_full_report.pdf");

        List<User> users = userRepository.findByStatus(UserStatus.ACTIVE);
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        document.add(new Paragraph("REPORT: ALL EMPLOYEES DATA WITH PHOTOS"));

        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        String[] headers = { "ID", "User", "Pass", "Name", "Email", "Role", "Dept", "Pos", "Stat", "Photo" };
        for (String h : headers)
            table.addCell(new Phrase(h));

        for (User u : users) {
            table.addCell(String.valueOf(u.getId()));
            table.addCell(u.getUsername());
            table.addCell("****");
            table.addCell(u.getFullName());
            table.addCell(u.getEmail());
            table.addCell(u.getRole().toString());
            table.addCell(u.getDepartment() != null ? u.getDepartment().getDepartmentName() : "");
            table.addCell(u.getPosition() != null ? u.getPosition().getPositionName() : "");
            table.addCell(u.getStatus().toString());

            Path imgPath = Paths.get(UPLOAD_DIR + u.getProfileImage());
            if (Files.exists(imgPath)) {
                com.lowagie.text.Image pdfImg = com.lowagie.text.Image.getInstance(imgPath.toAbsolutePath().toString());
                pdfImg.scaleToFit(40, 40);
                table.addCell(new PdfPCell(pdfImg, false));
            } else {
                table.addCell("Không");
            }
        }
        document.add(table);
        document.close();
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("positions", positionRepository.findByActiveTrue());
        return "admin/user-form";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute("user") User user,
                           @RequestParam("image") MultipartFile file,
                           @RequestParam(value = "departmentId", required = false) Integer departmentId,
                           @RequestParam(value = "positionId", required = false) Integer positionId,
                           @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
                           @RequestParam(value = "gender", required = false) String gender,
                           @RequestParam(value = "dateOfBirth", required = false) String dateOfBirth,
                           @RequestParam(value = "address", required = false) String address,
                           @RequestParam(value = "employeeCode", required = false) String employeeCode,
                           @RequestParam(value = "hireDate", required = false) String hireDate)
            throws IOException {

        // Resolve department & position từ ID
        if (departmentId != null) {
            departmentRepository.findById(departmentId).ifPresent(user::setDepartment);
        } else {
            user.setDepartment(null);
        }
        if (positionId != null) {
            positionRepository.findById(positionId).ifPresent(user::setPosition);
        } else {
            user.setPosition(null);
        }

        // Gán các fields mới
        user.setPhoneNumber(phoneNumber);
        user.setGender(gender);
        user.setAddress(address);
        user.setEmployeeCode(employeeCode != null && !employeeCode.isBlank() ? employeeCode : null);
        if (dateOfBirth != null && !dateOfBirth.isBlank()) {
            try { user.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth)); } catch (Exception ignored) {}
        }
        if (hireDate != null && !hireDate.isBlank()) {
            try { user.setHireDate(java.time.LocalDate.parse(hireDate)); } catch (Exception ignored) {}
        }

        // Xử lý ảnh — upload lên Cloudinary
        if (!file.isEmpty()) {
            String ct = file.getContentType();
            if (ct != null && ct.startsWith("image/")) {
                try {
                    java.util.Map<?, ?> result = cloudinaryService.uploadAvatar(file, "hr_avatars");
                    user.setProfileImage(result.get("secure_url").toString());
                } catch (Exception e) {
                    // fallback: giữ ảnh cũ
                    if (user.getId() != null) {
                        userRepository.findById(user.getId())
                                .ifPresent(existing -> user.setProfileImage(existing.getProfileImage()));
                    }
                }
            }
        } else if (user.getId() != null) {
            // Giữ ảnh cũ
            userRepository.findById(user.getId())
                    .ifPresent(existing -> user.setProfileImage(existing.getProfileImage()));
        }

        // Xử lý password: chỉ encode khi tạo mới hoặc khi user nhập password mới
        if (user.getId() == null) {
            // Tạo mới: bắt buộc có password
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            String rawPwd = user.getPassword();
            if (rawPwd != null && !rawPwd.isBlank()) {
                // Đổi password mới
                user.setPassword(passwordEncoder.encode(rawPwd));
            } else {
                // Giữ password cũ
                userRepository.findById(user.getId())
                        .ifPresent(existing -> user.setPassword(existing.getPassword()));
            }
        }

        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("positions", positionRepository.findByActiveTrue());
        return "admin/user-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        return "redirect:/admin/users";
    }
    @GetMapping("/user/list")
public String listUsers(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String direction,
        Model model) {
    
    Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
    List<User> users;

    if (keyword != null && !keyword.isEmpty()) {
        users = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, sort);
    } else {
        users = userRepository.findAll(sort);
    }

    model.addAttribute("users", users);
    model.addAttribute("keyword", keyword);
    return "user_list";
}
}