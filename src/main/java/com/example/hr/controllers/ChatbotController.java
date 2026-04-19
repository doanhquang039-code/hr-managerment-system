package com.example.hr.controllers;

import com.example.hr.dto.ChatbotChatRequest;
import com.example.hr.dto.ChatbotChatResponse;
import com.example.hr.dto.ChatbotRateRequest;
import com.example.hr.models.ChatbotMessage;
import com.example.hr.models.User;
import com.example.hr.repository.ChatbotMessageRepository;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/user1/chatbot")
@PreAuthorize("isAuthenticated()")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private AuthUserHelper authUserHelper;

    @Autowired
    private ChatbotMessageRepository chatbotMessageRepository;

    @GetMapping
    public String page(Authentication auth, Model model) {
        User user = authUserHelper.getCurrentUser(auth);
        if (user == null) {
            return "redirect:/login?error=user_not_found";
        }
        List<ChatbotMessage> rows = new ArrayList<>(chatbotMessageRepository.findTop40ByUserOrderByCreatedAtDesc(user));
        Collections.reverse(rows);
        model.addAttribute("history", rows);
        model.addAttribute("currentUser", user);
        return "user1/chatbot";
    }

    @PostMapping(value = "/api/message", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ChatbotChatResponse> postMessage(@RequestBody ChatbotChatRequest body, Authentication auth) {
        User user = authUserHelper.getCurrentUser(auth);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        ChatbotChatResponse out = chatbotService.chat(user, body != null ? body.getMessage() : null,
                body != null ? body.getSessionId() : null);
        return ResponseEntity.ok(out);
    }

    @PostMapping(value = "/api/rate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> rate(@RequestBody ChatbotRateRequest body, Authentication auth) {
        User user = authUserHelper.getCurrentUser(auth);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        if (body == null || body.getRating() == null) {
            return ResponseEntity.badRequest().build();
        }
        boolean ok = chatbotService.rateMessage(user, body.getMessageId(), body.getRating());
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
