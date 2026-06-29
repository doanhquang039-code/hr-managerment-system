package com.example.hr.config;

import com.example.hr.scheduler.PendingOrderCancellationJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Quartz Scheduler cho các tác vụ tự động Marketplace.
 */
@Configuration
public class QuartzMarketplaceConfig {

    private static final String JOB_IDENTITY = "pendingOrderCancellationJob";
    private static final String TRIGGER_IDENTITY = "pendingOrderCancellationTrigger";
    private static final String GROUP = "marketplaceGroup";

    @Bean
    public JobDetail pendingOrderCancellationJobDetail() {
        return JobBuilder.newJob(PendingOrderCancellationJob.class)
                .withIdentity(JOB_IDENTITY, GROUP)
                .storeDurably()
                .withDescription("Job tự động quét và hủy đơn hàng quá 24h chưa thanh toán")
                .build();
    }

    @Bean
    public Trigger pendingOrderCancellationTrigger(JobDetail pendingOrderCancellationJobDetail) {
        // Chạy định kỳ mỗi 15 phút
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMinutes(15)
                .repeatForever();

        return TriggerBuilder.newTrigger()
                .forJob(pendingOrderCancellationJobDetail)
                .withIdentity(TRIGGER_IDENTITY, GROUP)
                .withSchedule(scheduleBuilder)
                .build();
    }
}
