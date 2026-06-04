package com.example.hr.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

/**
 * Dev profile Kafka configuration
 * Provides dummy KafkaTemplate to avoid Kafka connection in dev mode
 */
@Configuration
@Profile("dev")
public class DevKafkaConfig {

    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate() {
        // Return dummy KafkaTemplate that does nothing
        return new KafkaTemplate<String, Object>(new DummyProducerFactory()) {
            @Override
            public CompletableFuture<SendResult<String, Object>> send(String topic, Object data) {
                // Do nothing in dev mode
                return CompletableFuture.completedFuture(null);
            }
        };
    }

    @Bean
    @Primary
    public KafkaProperties kafkaProperties() {
        return new KafkaProperties();
    }
    
    /**
     * Dummy ProducerFactory for dev mode
     */
    private static class DummyProducerFactory implements ProducerFactory<String, Object> {
        @Override
        public org.apache.kafka.clients.producer.Producer<String, Object> createProducer() {
            return null;
        }
    }
}
