package com.example.hr.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LanguageController {

    /**
     * Äá»•i ngÃ´n ngá»¯ vÃ  redirect vá» trang trÆ°á»›c.
     * DÃ¹ng khi JS khÃ´ng available.
     */
    @GetMapping("/change-lang")
    public String changeLang(@RequestParam String lang,
                              @RequestParam(required = false) String redirect,
                              HttpServletRequest request) {
        // LocaleChangeInterceptor Ä‘Ã£ xá»­ lÃ½ viá»‡c set locale qua ?lang=
        // Chá»‰ cáº§n redirect vá» trang trÆ°á»›c
        String referer = request.getHeader("Referer");
        if (redirect != null && !redirect.isBlank()) {
            return "redirect:" + redirect;
        }
        if (referer != null && !referer.isBlank()) {
            // ThÃªm ?lang= vÃ o referer URL
            String separator = referer.contains("?") ? "&" : "?";
            return "redirect:" + referer + separator + "lang=" + lang;
        }
        return "redirect:/home";
    }
}


