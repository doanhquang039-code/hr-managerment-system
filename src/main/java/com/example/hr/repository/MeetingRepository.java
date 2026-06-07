package com.example.hr.repository;

import com.example.hr.models.Meeting;
import com.example.hr.models.User;
import com.example.hr.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Integer> {

    List<Meeting> findByOrganizerOrderByScheduledTimeDesc(User organizer);

    List<Meeting> findByParticipantOrderByScheduledTimeDesc(User participant);

    List<Meeting> findByDepartmentOrderByScheduledTimeDesc(Department department);

    List<Meeting> findByStatusOrderByScheduledTimeDesc(String status);

    @Query("SELECT m FROM Meeting m WHERE m.organizer = :user OR m.participant = :user ORDER BY m.scheduledTime DESC")
    List<Meeting> findByUserInvolved(@Param("user") User user);

    @Query("SELECT m FROM Meeting m WHERE m.scheduledTime BETWEEN :startDate AND :endDate ORDER BY m.scheduledTime")
    List<Meeting> findByScheduledTimeBetween(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT m FROM Meeting m WHERE m.organizer = :organizer AND m.scheduledTime BETWEEN :startDate AND :endDate")
    List<Meeting> findUpcomingMeetings(@Param("organizer") User organizer, 
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    List<Meeting> findByMeetingTypeAndOrganizerOrderByScheduledTimeDesc(String meetingType, User organizer);

    @Query("SELECT COUNT(m) FROM Meeting m WHERE m.organizer = :organizer AND m.status = :status")
    long countByOrganizerAndStatus(@Param("organizer") User organizer, @Param("status") String status);
}


