package com.example.hr.scheduler;


import com.example.hr.attendance.entity.Attendance;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Attendance Reminder Job
 * Nhắc nhở chấm công
 */
@Slf4j
@Component
public class AttendanceReminderJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Starting attendance reminder job at {}", LocalDateTime.now());
        
        try {
            // TODO: Implement attendance reminder logic
            // 1. Get employees who haven't checked in
            // 2. Send reminder notifications
            // 3. Log activity
            
            log.info("Attendance reminders sent successfully");
            
        } catch (Exception e) {
            log.error("Error sending attendance reminders", e);
            throw new JobExecutionException(e);
        }
    }
}
