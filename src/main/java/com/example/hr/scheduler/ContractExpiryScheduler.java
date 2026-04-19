package com.example.hr.scheduler;

import com.example.hr.service.ContractExpiryReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class ContractExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(ContractExpiryScheduler.class);
    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");

    @Autowired
    private ContractExpiryReminderService contractExpiryReminderService;

    /** Chạy mỗi ngày lúc 08:00 (giờ Việt Nam). */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Ho_Chi_Minh")
    public void runContractExpiryReminders() {
        try {
            LocalDate today = LocalDate.now(VN);
            contractExpiryReminderService.sendDueReminders(today);
            log.debug("Contract expiry reminders evaluated for {}", today);
        } catch (Exception e) {
            log.error("Contract expiry reminder job failed", e);
        }
    }
}
