package com.example.hr.controllers;

import com.example.hr.dto.ChatbotChatRequest;
import com.example.hr.dto.ChatbotChatResponse;
import com.example.hr.dto.ChatbotRateRequest;
import com.example.hr.models.User;
import com.example.hr.service.AuthUserHelper;
import com.example.hr.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@PreAuthorize("isAuthenticated()")
public class GlobalChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private AuthUserHelper authUserHelper;

    @PostMapping(value = "/message", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatbotChatResponse> message(@RequestBody ChatbotChatRequest body, Authentication auth) {
        User user = authUserHelper.getCurrentUser(auth);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        ChatbotChatResponse out = chatbotService.chat(
                user,
                body != null ? body.getMessage() : null,
                body != null ? body.getSessionId() : null
        );
        return ResponseEntity.ok(out);
    }

    @PostMapping(value = "/rate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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


