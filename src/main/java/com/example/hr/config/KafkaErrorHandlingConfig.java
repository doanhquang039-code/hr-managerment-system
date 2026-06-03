package com.example.hr.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
@Slf4j
public class KafkaErrorHandlingConfig {

    private static final long RETRY_INTERVAL_MS = 1_000L;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long MAX_RETRY_INTERVAL_MS = 10_000L;

    @Bean
    public CommonErrorHandler kafkaDefaultErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition())
        );
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(MAX_RETRY_ATTEMPTS);
        backOff.setInitialInterval(RETRY_INTERVAL_MS);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(MAX_RETRY_INTERVAL_MS);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("Kafka retry {}/{} for topic={}, partition={}, offset={} because {}",
                        deliveryAttempt,
                        MAX_RETRY_ATTEMPTS,
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        ex.getClass().getSimpleName())
        );
        return errorHandler;
    }
}
