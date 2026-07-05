package com.example.hr.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * Dev profile configuration
 * Provides dummy templates to avoid connection in dev mode
 */
@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "false", matchIfMissing = true)
public class DevKafkaConfig {

    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<String, Object>(new DummyProducerFactory()) {
            @Override
            public CompletableFuture<SendResult<String, Object>> send(String topic, Object data) {
                return CompletableFuture.completedFuture(null);
            }
        };
    }

    @Bean
    @Primary
    public KafkaProperties kafkaProperties() {
        return new KafkaProperties();
    }

    private static class DummyProducerFactory implements ProducerFactory<String, Object> {
        @Override
        public org.apache.kafka.clients.producer.Producer<String, Object> createProducer() {
            return null;
        }
    }
}
