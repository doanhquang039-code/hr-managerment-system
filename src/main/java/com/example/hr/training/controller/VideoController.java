package com.example.hr.training.controller;

import com.example.hr.training.dto.VideoUploadStatusDto;
import com.example.hr.training.entity.TrainingVideo;
import com.example.hr.models.User;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.training.service.VideoFileValidator.VideoValidationException;
import com.example.hr.training.service.VideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Admin controller – quản lý video đào tạo (upload, sửa, xóa, publish).
 *
 * <p><b>Luồng upload mới (không timeout):</b></p>
 * <ol>
 *   <li>Admin POST file → {@link #handleUploadAsync} validate magic bytes + lưu file tạm → trả về jobId ngay</li>
 *   <li>@Async thread chạy ngầm: upload Cloudinary → lưu DB</li>
 *   <li>Frontend polling {@link #getUploadStatus} mỗi 3 giây để lấy progress</li>
 * </ol>
 */
@Controller
@RequestMapping("/admin/videos")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class VideoController {

    private static final Logger log = LoggerFactory.getLogger(VideoController.class);

    private final VideoService videoService;
    private final AuthUserHelper authUserHelper;

    public VideoController(VideoService videoService, AuthUserHelper authUserHelper) {
        this.videoService = videoService;
        this.authUserHelper = authUserHelper;
    }

    // ----------------------------------------------------------------
    // Danh sách video
    // ----------------------------------------------------------------

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String category,
                       Model model) {
        List<TrainingVideo> videos = videoService.searchVideos(keyword, category);
        model.addAttribute("videos", videos);
        model.addAttribute("categories", videoService.findDistinctCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("totalVideos", videoService.findAll().size());
        model.addAttribute("publishedCount", videoService.findPublished().size());
        return "admin/video-list";
    }

    // ----------------------------------------------------------------
    // Upload form
    // ----------------------------------------------------------------

    @GetMapping("/upload")
    public String uploadForm(Model model) {
        model.addAttribute("categories", videoService.findDistinctCategories());
        return "admin/video-upload";
    }

    // ----------------------------------------------------------------
    // POST Upload — trả về JSON ngay lập tức với jobId
    // ----------------------------------------------------------------

    /**
     * Nhận file từ form, validate magic bytes ngay, lưu tạm,
     * khởi động @Async thread và trả về JSON { jobId } trong < 1 giây.
     *
     * <p>Frontend dùng jobId để polling {@link #getUploadStatus}.</p>
     */
    @PostMapping(value = "/upload", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleUploadAsync(
            @RequestParam("file") MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tags,
            Authentication auth) {

        log.info(">>> VIDEO UPLOAD POST | file={} size={}", file.getOriginalFilename(), file.getSize());

        // --- Validate cơ bản ---
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Vui lòng chọn file video!"));
        }
        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Tiêu đề không được để trống!"));
        }

        try {
            User uploader = authUserHelper.getCurrentUser(auth);

            // submitUploadAsync: validate Tika + lưu file tạm + tạo job → trả jobId
            String jobId = videoService.submitUploadAsync(
                    file, title.trim(), description, category, tags, uploader);

            // Khởi động @Async thread (không block HTTP thread)
            videoService.processUploadJobAsync(jobId);

            log.info(">>> Job {} đã được submit bởi {}", jobId,
                    uploader != null ? uploader.getUsername() : "unknown");

            return ResponseEntity.ok(Map.of(
                    "jobId",   jobId,
                    "message", "File đã được nhận. Đang upload trong nền..."
            ));

        } catch (VideoValidationException e) {
            // File bị từ chối ngay (giả mạo MIME, extension sai, v.v.)
            log.warn(">>> File bị từ chối: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (IOException e) {
            log.error(">>> IOException khi lưu file tạm: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Lỗi lưu file tạm: " + e.getMessage()));

        } catch (Exception e) {
            log.error(">>> Exception không mong đợi: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Lỗi hệ thống: " + e.getClass().getSimpleName()));
        }
    }

    // ----------------------------------------------------------------
    // GET Upload Status — client polling mỗi 3 giây
    // ----------------------------------------------------------------

    /**
     * Trả về JSON trạng thái của upload job.
     * Client gọi endpoint này mỗi 3 giây để cập nhật progress bar.
     *
     * <p>Response format:</p>
     * <pre>
     * {
     *   "jobId":      "abc-123",
     *   "status":     "PROCESSING",   // PENDING | PROCESSING | DONE | FAILED
     *   "progress":   45,             // 0-100
     *   "message":    "Đang upload lên Cloudinary (45%)...",
     *   "videoId":    null,           // có giá trị khi DONE
     *   "errorDetail": null           // có giá trị khi FAILED
     * }
     * </pre>
     */
    @GetMapping(value = "/upload-status/{jobId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<VideoUploadStatusDto> getUploadStatus(@PathVariable String jobId) {
        VideoUploadStatusDto status = videoService.getJobStatus(jobId);
        return ResponseEntity.ok(status);
    }

    // ----------------------------------------------------------------
    // Edit
    // ----------------------------------------------------------------

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        TrainingVideo video = videoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Video không tồn tại"));
        model.addAttribute("video", video);
        model.addAttribute("categories", videoService.findDistinctCategories());
        return "admin/video-edit";
    }

    @PostMapping("/edit/{id}")
    public String saveEdit(@PathVariable Integer id,
                            @RequestParam String title,
                            @RequestParam(required = false) String description,
                            @RequestParam(required = false) String category,
                            @RequestParam(required = false) String tags,
                            @RequestParam(required = false) Boolean isPublished,
                            RedirectAttributes ra) {
        videoService.updateMetadata(id, title, description, category, tags, isPublished);
        ra.addFlashAttribute("success", "Cập nhật video thành công!");
        return "redirect:/admin/videos";
    }

    // ----------------------------------------------------------------
    // Toggle publish
    // ----------------------------------------------------------------

    @GetMapping("/toggle/{id}")
    public String togglePublish(@PathVariable Integer id, RedirectAttributes ra) {
        TrainingVideo v = videoService.togglePublish(id);
        ra.addFlashAttribute("success",
                "Video đã " + (Boolean.TRUE.equals(v.getIsPublished()) ? "xuất bản" : "ẩn") + "!");
        return "redirect:/admin/videos";
    }

    // ----------------------------------------------------------------
    // Delete
    // ----------------------------------------------------------------

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            videoService.deleteVideo(id);
            ra.addFlashAttribute("success", "Đã xóa video!");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Lỗi xóa: " + e.getMessage());
        }
        return "redirect:/admin/videos";
    }
}
