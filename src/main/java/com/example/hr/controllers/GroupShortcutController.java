package com.example.hr.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GroupShortcutController {

    @GetMapping("/group")
    public String groupAlias() {
        return "redirect:/groups";
    }

    @GetMapping("/group/members")
    public String groupMembersAlias() {
        return "redirect:/groups/members";
    }

}
