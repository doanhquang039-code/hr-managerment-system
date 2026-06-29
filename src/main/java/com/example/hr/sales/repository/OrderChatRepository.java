package com.example.hr.sales.repository;

import com.example.hr.sales.entity.OrderChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderChatRepository extends JpaRepository<OrderChat, Integer> {
    List<OrderChat> findByOrderIdOrderByTimestampAsc(Integer orderId);
}
