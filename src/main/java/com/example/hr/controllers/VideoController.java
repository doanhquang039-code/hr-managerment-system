package com.example.hr.controllers;

import com.example.hr.models.TrainingVideo;
import com.example.hr.models.User;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.VideoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

/**
 * Admin controller — quản lý video đào tạo (upload, sửa, xóa, publish).
 */
@Controller
@RequestMapping("/admin/videos")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class VideoController {

    private final VideoService videoService;
    private final AuthUserHelper authUserHelper;

    public VideoController(VideoService videoService, AuthUserHelper authUserHelper) {
        this.videoService = videoService;
        this.authUserHelper = authUserHelper;
    }

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

    @GetMapping("/upload")
    public String uploadForm(Model model) {
        model.addAttribute("categories", videoService.findDistinctCategories());
        return "admin/video-upload";
    }

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file,
                                @RequestParam String title,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) String category,
                                @RequestParam(required = false) String tags,
                                Authentication auth,
                                RedirectAttributes ra) {
        System.out.println(">>> VIDEO UPLOAD POST received! File: " + file.getOriginalFilename() + " Size: " + file.getSize());
        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "Vui lòng chọn file video!");
            return "redirect:/admin/videos/upload";
        }

        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("video/")) {
            ra.addFlashAttribute("error", "Chỉ chấp nhận file video (mp4, mov, avi...)!");
            return "redirect:/admin/videos/upload";
        }

        if (title == null || title.isBlank()) {
            ra.addFlashAttribute("error", "Tiêu đề không được để trống!");
            return "redirect:/admin/videos/upload";
        }

        try {
            User uploader = authUserHelper.getCurrentUser(auth);
            System.out.println(">>> Uploader: " + (uploader != null ? uploader.getUsername() : "NULL"));
            System.out.println(">>> ContentType: " + file.getContentType() + " | Size: " + file.getSize());
            videoService.uploadVideo(file, title.trim(), description, category, tags, uploader);
            ra.addFlashAttribute("success", "Upload video \"" + title + "\" thành công!");
            return "redirect:/admin/videos";
        } catch (IOException e) {
            System.err.println(">>> IOException khi upload: " + e.getMessage());
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi upload Cloudinary: " + e.getMessage());
            return "redirect:/admin/videos/upload";
        } catch (Exception e) {
            System.err.println(">>> Exception khi upload: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return "redirect:/admin/videos/upload";
        }
    }

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

    @GetMapping("/toggle/{id}")
    public String togglePublish(@PathVariable Integer id, RedirectAttributes ra) {
        TrainingVideo v = videoService.togglePublish(id);
        ra.addFlashAttribute("success",
                "Video đã " + (Boolean.TRUE.equals(v.getIsPublished()) ? "xuất bản" : "ẩn") + "!");
        return "redirect:/admin/videos";
    }

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
