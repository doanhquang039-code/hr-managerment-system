package com.example.hr.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LanguageController {

    /**
     * Đổi ngôn ngữ và redirect về trang trước.
     * Dùng khi JS không available.
     */
    @GetMapping("/change-lang")
    public String changeLang(@RequestParam String lang,
                              @RequestParam(required = false) String redirect,
                              HttpServletRequest request) {
        // LocaleChangeInterceptor đã xử lý việc set locale qua ?lang=
        // Chỉ cần redirect về trang trước
        String referer = request.getHeader("Referer");
        if (redirect != null && !redirect.isBlank()) {
            return "redirect:" + redirect;
        }
        if (referer != null && !referer.isBlank()) {
            // Thêm ?lang= vào referer URL
            String separator = referer.contains("?") ? "&" : "?";
            return "redirect:" + referer + separator + "lang=" + lang;
        }
        return "redirect:/home";
    }
}
