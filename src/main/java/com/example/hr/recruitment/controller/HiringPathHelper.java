package com.example.hr.recruitment.controller;

import jakarta.servlet.http.HttpServletRequest;

public class HiringPathHelper {
    public static String getRedirectPrefix(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/admin")) {
            return "/admin";
        } else if (uri.startsWith("/manager")) {
            return "/manager";
        }
        return "/hiring";
    }
}
