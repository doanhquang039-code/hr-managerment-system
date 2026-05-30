package com.example.hr.controllers;

import com.example.hr.enums.TaskType;
import com.example.hr.enums.UserStatus;
import com.example.hr.models.Task;
import com.example.hr.repository.TaskRepository;
import com.example.hr.repository.UserRepository;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin/tasks")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class TaskController {

    @Autowired private TaskRepository taskRepository;
    @Autowired private UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @GetMapping
    public String list(@RequestParam(name = "keyword", required = false) String keyword,
                       @RequestParam(name = "taskType", required = false) String taskType,
                       @RequestParam(name = "extraShift", required = false) Boolean extraShift,
                       @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                       @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                       @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
                       @RequestParam(name = "direction", defaultValue = "desc") String direction,
                       @RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       Model model) {
        TaskType type = parseTaskType(taskType);
        String normalizedSortBy = normalizeSortBy(sortBy);
        String normalizedDirection = normalizeDirection(direction);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, buildSort(normalizedSortBy, normalizedDirection));
        
        String normalizedKeyword = keyword != null && !keyword.isBlank() ? keyword.trim() : null;
        org.springframework.data.domain.Page<Task> taskPage = taskRepository.searchTasks(normalizedKeyword, type, extraShift, startDate, endDate, pageable);

        model.addAttribute("tasks", taskPage.getContent());
        model.addAttribute("taskPage", taskPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", taskPage.getTotalPages());
        model.addAttribute("totalItems", taskPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedTaskType", taskType);
        model.addAttribute("selectedExtraShift", extraShift);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sortBy", normalizedSortBy);
        model.addAttribute("direction", normalizedDirection);
        model.addAttribute("reverseSortDir", "asc".equalsIgnoreCase(normalizedDirection) ? "desc" : "asc");
        model.addAttribute("taskTypes", TaskType.values());
        return "admin/task-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("taskTypes", TaskType.values());
        return "admin/task-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
        model.addAttribute("task", task);
        model.addAttribute("taskTypes", TaskType.values());
        return "admin/task-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("task") Task task) {
        if (task.getEndDate() != null && task.getStartDate() != null && task.getEndDate().isBefore(task.getStartDate())) {
            LocalDate start = task.getStartDate();
            task.setStartDate(task.getEndDate());
            task.setEndDate(start);
        }
        if (task.getId() != null) {
            taskRepository.findById(task.getId()).ifPresent(existing -> task.setCreatedAt(existing.getCreatedAt()));
            task.setUpdatedAt(LocalDateTime.now());
        }
        taskRepository.save(task);
        return "redirect:/admin/tasks";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        taskRepository.deleteById(id);
        return "redirect:/admin/tasks";
    }

    @GetMapping("/assign/{id}")
    public String showAssignForm(@PathVariable Integer id, Model model) {
        Task task = taskRepository.findById(id).orElseThrow();
        model.addAttribute("task", task);
        model.addAttribute("users", userRepository.findByStatus(UserStatus.ACTIVE));
        return "admin/task-assign";
    }

    @GetMapping("/export/excel")
    public void exportExcel(@RequestParam(name = "keyword", required = false) String keyword,
                            @RequestParam(name = "taskType", required = false) String taskType,
                            @RequestParam(name = "extraShift", required = false) Boolean extraShift,
                            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
                            @RequestParam(name = "direction", defaultValue = "desc") String direction,
                            HttpServletResponse response) throws IOException {
        String normalizedKeyword = keyword != null && !keyword.isBlank() ? keyword.trim() : null;
        List<Task> tasks = taskRepository.searchTasks(normalizedKeyword, parseTaskType(taskType), extraShift, startDate, endDate,
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE, buildSort(normalizeSortBy(sortBy), normalizeDirection(direction)))).getContent();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tasks");
            String[] headers = {"ID", "Ten cong viec", "Loai", "Ngay bat dau", "Ngay ket thuc", "So nguoi toi da", "Luong co ban", "Tang ca", "Mo ta"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);

            int rowIndex = 1;
            for (Task task : tasks) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(task.getId() != null ? task.getId() : 0);
                row.createCell(1).setCellValue(nullToBlank(task.getTaskName()));
                row.createCell(2).setCellValue(task.getTaskType() != null ? task.getTaskType().getDisplayName() : "");
                row.createCell(3).setCellValue(formatDate(task.getStartDate()));
                row.createCell(4).setCellValue(formatDate(task.getEndDate()));
                row.createCell(5).setCellValue(task.getMaxParticipants() != null ? task.getMaxParticipants() : 0);
                row.createCell(6).setCellValue(task.getBaseReward() != null ? task.getBaseReward().doubleValue() : 0);
                row.createCell(7).setCellValue(Boolean.TRUE.equals(task.getIsExtraShift()) ? "Co" : "Khong");
                row.createCell(8).setCellValue(nullToBlank(task.getDescription()));
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            workbook.write(response.getOutputStream());
        }
    }

    @GetMapping("/export/pdf")
    public void exportPdf(@RequestParam(name = "keyword", required = false) String keyword,
                          @RequestParam(name = "taskType", required = false) String taskType,
                          @RequestParam(name = "extraShift", required = false) Boolean extraShift,
                          @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                          @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                          @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
                          @RequestParam(name = "direction", defaultValue = "desc") String direction,
                          HttpServletResponse response) throws IOException {
        String normalizedKeyword = keyword != null && !keyword.isBlank() ? keyword.trim() : null;
        List<Task> tasks = taskRepository.searchTasks(normalizedKeyword, parseTaskType(taskType), extraShift, startDate, endDate, org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE, buildSort(normalizeSortBy(sortBy), normalizeDirection(direction)))).getContent();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.pdf");

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
        document.add(new Paragraph("TASK REPORT"));

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        String[] headers = {"ID", "Task", "Type", "Start", "End", "Max", "Reward", "OT"};
        for (String header : headers) table.addCell(new Phrase(header));
        for (Task task : tasks) {
            table.addCell(String.valueOf(task.getId()));
            table.addCell(nullToBlank(task.getTaskName()));
            table.addCell(task.getTaskType() != null ? task.getTaskType().getDisplayName() : "");
            table.addCell(formatDate(task.getStartDate()));
            table.addCell(formatDate(task.getEndDate()));
            table.addCell(String.valueOf(task.getMaxParticipants() != null ? task.getMaxParticipants() : 0));
            table.addCell(task.getBaseReward() != null ? task.getBaseReward().toPlainString() : "0");
            table.addCell(Boolean.TRUE.equals(task.getIsExtraShift()) ? "Yes" : "No");
        }

        document.add(table);
        document.close();
    }

    private TaskType parseTaskType(String taskType) {
        if (taskType == null || taskType.isBlank()) return null;
        try {
            return TaskType.valueOf(taskType);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Sort buildSort(String sortBy, String direction) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(sortDirection, sortBy);
    }

    private String normalizeSortBy(String sortBy) {
        return switch (sortBy == null ? "" : sortBy) {
            case "taskName", "taskType", "startDate", "endDate", "baseReward", "maxParticipants", "createdAt" -> sortBy;
            default -> "id";
        };
    }

    private String normalizeDirection(String direction) {
        return "asc".equalsIgnoreCase(direction) ? "asc" : "desc";
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "";
    }

    private String nullToBlank(String value) {
        return value != null ? value : "";
    }
}
