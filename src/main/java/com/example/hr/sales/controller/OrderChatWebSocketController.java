package com.example.hr.sales.controller;

import com.example.hr.models.User;
import com.example.hr.sales.entity.OrderChat;
import com.example.hr.sales.service.SalesService;
import com.example.hr.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OrderChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final SalesService salesService;
    private final UserRepository userRepository;

    @MessageMapping("/chat/send")
    public void handleChatMessage(ChatPayload payload) {
        log.info("Received order chat message: {}", payload);
        try {
            if (payload.getOrderId() == null || payload.getMessage() == null || payload.getMessage().isBlank()) {
                log.warn("Invalid chat message payload: {}", payload);
                return;
            }

            User sender = userRepository.findByUsername(payload.getSenderUsername()).orElse(null);
            if (sender == null) {
                log.warn("Sender username '{}' not found, ignoring message.", payload.getSenderUsername());
                return;
            }

            // Lưu tin nhắn chat vào database
            OrderChat chat = salesService.saveOrderChat(payload.getOrderId(), sender, payload.getMessage());

            // Chuẩn bị payload để broadcast
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", payload.getOrderId());
            response.put("senderUsername", sender.getUsername());
            response.put("senderFullName", sender.getFullName());
            response.put("message", chat.getMessage());
            response.put("timestamp", chat.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            // Broadcast tới all clients subscribe vào order đó
            String destination = "/topic/order-chat/" + payload.getOrderId();
            messagingTemplate.convertAndSend(destination, response);
            log.info("Broadcasted order chat to destination: {}", destination);

        } catch (Exception e) {
            log.error("Error handling chat message: ", e);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatPayload {
        private Integer orderId;
        private String senderUsername;
        private String message;
    }
}
