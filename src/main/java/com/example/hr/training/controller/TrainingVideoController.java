package com.example.hr.training.controller;

import com.example.hr.training.entity.TrainingVideo;
import com.example.hr.training.service.VideoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User-facing controller â€” xem thÆ° viá»‡n video Ä‘Ã o táº¡o.
 */
@Controller
@RequestMapping("/videos")
public class TrainingVideoController {

    private final VideoService videoService;

    public TrainingVideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    /** ThÆ° viá»‡n video â€” táº¥t cáº£ má»i ngÆ°á»i Ä‘á»u xem Ä‘Æ°á»£c */
    @GetMapping
    public String library(@RequestParam(required = false) String keyword,
                           @RequestParam(required = false) String category,
                           Model model) {
        List<TrainingVideo> videos = videoService.searchVideos(keyword, category);
        model.addAttribute("videos", videos);
        model.addAttribute("categories", videoService.findDistinctCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        return "videos/library";
    }

    /** Xem chi tiáº¿t + player */
    @GetMapping("/{id}")
    public String watch(@PathVariable Integer id, Model model) {
        TrainingVideo video = videoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Video khÃ´ng tá»“n táº¡i"));

        if (!Boolean.TRUE.equals(video.getIsPublished())) {
            return "redirect:/videos";
        }

        // TÄƒng lÆ°á»£t xem
        videoService.incrementView(id);

        // Video liÃªn quan (cÃ¹ng category)
        List<TrainingVideo> related = videoService.searchVideos(null, video.getCategory())
                .stream()
                .filter(v -> !v.getId().equals(id))
                .limit(6)
                .toList();

        model.addAttribute("video", video);
        model.addAttribute("related", related);
        return "videos/watch";
    }
}

