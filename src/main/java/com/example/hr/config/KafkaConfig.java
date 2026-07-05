package com.example.hr.config;


import com.example.hr.payroll.entity.Payroll;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka Configuration
 * Tạo các topic cho HR Management System
 */
//@Configuration
//@EnableKafka
public class KafkaConfig {

    @Value("${kafka.topics.attendance}")
    private String attendanceTopic;

    @Value("${kafka.topics.leave-requests}")
    private String leaveRequestsTopic;

    @Value("${kafka.topics.payroll}")
    private String payrollTopic;

    @Value("${kafka.topics.notifications}")
    private String notificationsTopic;

    @Value("${kafka.topics.performance-reviews}")
    private String performanceReviewsTopic;

    @Value("${kafka.topics.recruitment}")
    private String recruitmentTopic;

    @Value("${kafka.topics.training}")
    private String trainingTopic;

    @Value("${kafka.topics.employee-lifecycle}")
    private String employeeLifecycleTopic;

    @Value("${kafka.topics.audit-events}")
    private String auditEventsTopic;

    @Value("${kafka.topics.health-insights}")
    private String healthInsightsTopic;

    @Bean
    public NewTopic attendanceTopic() {
        return TopicBuilder.name(attendanceTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic attendanceDeadLetterTopic() {
        return deadLetterTopic(attendanceTopic, 3);
    }

    @Bean
    public NewTopic leaveRequestsTopic() {
        return TopicBuilder.name(leaveRequestsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic leaveRequestsDeadLetterTopic() {
        return deadLetterTopic(leaveRequestsTopic, 3);
    }

    @Bean
    public NewTopic payrollTopic() {
        return TopicBuilder.name(payrollTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic payrollDeadLetterTopic() {
        return deadLetterTopic(payrollTopic, 3);
    }

    @Bean
    public NewTopic notificationsTopic() {
        return TopicBuilder.name(notificationsTopic)
                .partitions(5)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationsDeadLetterTopic() {
        return deadLetterTopic(notificationsTopic, 5);
    }

    @Bean
    public NewTopic performanceReviewsTopic() {
        return TopicBuilder.name(performanceReviewsTopic)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic performanceReviewsDeadLetterTopic() {
        return deadLetterTopic(performanceReviewsTopic, 2);
    }

    @Bean
    public NewTopic recruitmentTopic() {
        return TopicBuilder.name(recruitmentTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic recruitmentDeadLetterTopic() {
        return deadLetterTopic(recruitmentTopic, 3);
    }

    @Bean
    public NewTopic trainingTopic() {
        return TopicBuilder.name(trainingTopic)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic trainingDeadLetterTopic() {
        return deadLetterTopic(trainingTopic, 2);
    }

    @Bean
    public NewTopic employeeLifecycleTopic() {
        return TopicBuilder.name(employeeLifecycleTopic)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic employeeLifecycleDeadLetterTopic() {
        return deadLetterTopic(employeeLifecycleTopic, 2);
    }

    @Bean
    public NewTopic auditEventsTopic() {
        return TopicBuilder.name(auditEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic auditEventsDeadLetterTopic() {
        return deadLetterTopic(auditEventsTopic, 3);
    }

    @Bean
    public NewTopic healthInsightsTopic() {
        return TopicBuilder.name(healthInsightsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic healthInsightsDeadLetterTopic() {
        return deadLetterTopic(healthInsightsTopic, 3);
    }

    private NewTopic deadLetterTopic(String topicName, int partitions) {
        return TopicBuilder.name(topicName + ".DLT")
                .partitions(partitions)
                .replicas(1)
                .build();
    }
}
