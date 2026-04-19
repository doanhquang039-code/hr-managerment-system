package com.example.hr.repository;

import com.example.hr.models.ChatbotMessage;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {

    List<ChatbotMessage> findByUser(User user);

    List<ChatbotMessage> findTop40ByUserOrderByCreatedAtDesc(User user);

    Optional<ChatbotMessage> findByIdAndUser(int id, User user);
}