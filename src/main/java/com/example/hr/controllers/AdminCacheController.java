package com.example.hr.controllers;

import com.example.hr.service.CacheService;
import com.example.hr.service.CloudStorageFacade;
import com.example.hr.service.EmailFacade;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@Controller
@RequestMapping("/admin/cache")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCacheController {

    private final CacheService cacheService;
    private final EmailFacade emailFacade;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CloudStorageFacade cloudStorageFacade;

    public AdminCacheController(CacheService cacheService,
                                 EmailFacade emailFacade,
                                 RedisTemplate<String, Object> redisTemplate,
                                 CloudStorageFacade cloudStorageFacade) {
        this.cacheService = cacheService;
        this.emailFacade = emailFacade;
        this.redisTemplate = redisTemplate;
        this.cloudStorageFacade = cloudStorageFacade;
    }

    @GetMapping
    public String dashboard(Model model) {
        // Thống kê cache keys
        try {
            Set<String> allKeys = cacheService.keys("*");
            long totalKeys = allKeys != null ? allKeys.size() : 0;

            model.addAttribute("totalKeys", totalKeys);
            model.addAttribute("dashboardKeys", cacheService.countKeys("dashboard*"));
            model.addAttribute("userKeys",      cacheService.countKeys("users*"));
            model.addAttribute("deptKeys",      cacheService.countKeys("departments*"));
            model.addAttribute("videoKeys",     cacheService.countKeys("videoLibrary*"));
            model.addAttribute("kpiKeys",       cacheService.countKeys("kpiGoals*"));
            model.addAttribute("redisOnline", true);
        } catch (Exception e) {
            model.addAttribute("redisOnline", false);
            model.addAttribute("redisError", e.getMessage());
        }

        model.addAttribute("emailProvider", emailFacade.getProvider());
        model.addAttribute("cloudServices", cloudStorageFacade.getHealthStatus());
        model.addAttribute("enabledCloudServices", cloudStorageFacade.getEnabledServices());
        return "admin/cache-dashboard";
    }

    @PostMapping("/evict")
    public String evict(@RequestParam String cacheName, RedirectAttributes ra) {
        try {
            switch (cacheName) {
                case "dashboard"    -> cacheService.evictDashboard();
                case "users"        -> cacheService.evictUsers();
                case "departments"  -> cacheService.evictDepartments();
                case "all"          -> cacheService.evictAll();
                default             -> cacheService.delete(cacheName);
            }
            ra.addFlashAttribute("success", "Đã xóa cache: " + cacheName);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi xóa cache: " + e.getMessage());
        }
        return "redirect:/admin/cache";
    }

    @PostMapping("/evict-all")
    public String evictAll(RedirectAttributes ra) {
        try {
            cacheService.evictAll();
            ra.addFlashAttribute("success", "Đã xóa toàn bộ cache!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/cache";
    }
}
