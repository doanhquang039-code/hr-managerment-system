package com.example.hr.repository;

import com.example.hr.models.Notification;
import com.example.hr.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(User user);

    List<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    long countByUserAndReadFalse(User user);

    Optional<Notification> findByIdAndUser(Integer id, User user);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.user = :user")
    void markAllReadByUser(@Param("user") User user);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.id = :id AND n.user = :user")
    int markReadByIdAndUser(@Param("id") Integer id, @Param("user") User user);
}


