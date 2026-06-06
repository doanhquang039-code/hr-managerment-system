package com.example.hr.user.controller;

import com.example.hr.models.User;
import com.example.hr.repository.DepartmentRepository;
import com.example.hr.repository.JobPositionRepository;
import com.example.hr.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    private static final String UPLOAD_DIR = "public/test1/";

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private JobPositionRepository positionRepository;

    @GetMapping
    public String listUsers(@RequestParam(name = "keyword", required = false) String keyword,
                            @RequestParam(name = "deptId", required = false) Integer deptId,
                            @RequestParam(name = "positionId", required = false) Integer positionId,
                            @RequestParam(name = "role", required = false) String role,
                            @RequestParam(name = "sortBy", defaultValue = "fullName") String sortBy,
                            @RequestParam(defaultValue = "asc") String sortDir,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
        
        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? 
                org.springframework.data.domain.Sort.by(sortBy).ascending() : 
                org.springframework.data.domain.Sort.by(sortBy).descending();
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        org.springframework.data.domain.Page<User> userPage = userService.findAdminUsers(keyword, deptId, positionId, role, pageable);
        
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("userPage", userPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedDeptId", deptId);
        model.addAttribute("selectedPositionId", positionId);
        model.addAttribute("selectedRole", role);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("positions", positionRepository.findByActiveTrue());
        return "admin/user-list";
    }

    @GetMapping("/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=employees_full_report.xlsx");

        List<User> users = userService.getActiveUsers();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Full Report");
        org.apache.poi.ss.usermodel.Drawing<?> drawing = sheet.createDrawingPatriarch();

        String[] headers = {"ID", "User", "Pass", "Name", "Email", "Role", "Dept", "Pos", "Status", "Photo"};
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
            row.createCell(6).setCellValue(user.getDepartment() != null ? user.getDepartment().getDepartmentName() : "N/A");
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
                row.createCell(9).setCellValue("Khong co anh");
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

        List<User> users = userService.getActiveUsers();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        document.add(new Paragraph("REPORT: ALL EMPLOYEES DATA WITH PHOTOS"));

        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        String[] headers = {"ID", "User", "Pass", "Name", "Email", "Role", "Dept", "Pos", "Stat", "Photo"};
        for (String h : headers) {
            table.addCell(new Phrase(h));
        }

        for (User user : users) {
            table.addCell(String.valueOf(user.getId()));
            table.addCell(user.getUsername());
            table.addCell("****");
            table.addCell(user.getFullName());
            table.addCell(user.getEmail());
            table.addCell(user.getRole().toString());
            table.addCell(user.getDepartment() != null ? user.getDepartment().getDepartmentName() : "");
            table.addCell(user.getPosition() != null ? user.getPosition().getPositionName() : "");
            table.addCell(user.getStatus().toString());

            Path imgPath = Paths.get(UPLOAD_DIR + user.getProfileImage());
            if (Files.exists(imgPath)) {
                com.lowagie.text.Image pdfImg = com.lowagie.text.Image.getInstance(imgPath.toAbsolutePath().toString());
                pdfImg.scaleToFit(40, 40);
                table.addCell(new PdfPCell(pdfImg, false));
            } else {
                table.addCell("Khong");
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
                           @RequestParam(value = "cccd", required = false) String cccd,
                           @RequestParam(value = "hireDate", required = false) String hireDate,
                           Authentication authentication) throws IOException {
        userService.saveAdminUser(user, file, departmentId, positionId, phoneNumber, gender, dateOfBirth, address, employeeCode, cccd, hireDate, authentication);
        return "redirect:/admin/users";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("positions", positionRepository.findByActiveTrue());
        return "admin/user-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id) {
        userService.softDeleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/user/list")
    public String listAllUsers(@RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "id") String sortBy,
                               @RequestParam(defaultValue = "asc") String direction,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        org.springframework.data.domain.Page<User> userPage = userService.searchUsers(keyword, pageable);

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("userPage", userPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("sortDir", direction);
        model.addAttribute("reverseSortDir", direction.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword);
        return "user_list";
    }
}
