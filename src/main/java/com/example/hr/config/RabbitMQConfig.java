package com.example.hr.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration
 * Cấu hình RabbitMQ cho message queue
 */
//@Configuration
//@EnableRabbit
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.email}")
    private String emailQueue;

    @Value("${rabbitmq.queue.sms}")
    private String smsQueue;

    @Value("${rabbitmq.queue.reports}")
    private String reportsQueue;

    @Value("${rabbitmq.exchange.hr}")
    private String hrExchange;

    @Bean
    public Queue emailQueue() {
        return new Queue(emailQueue, true);
    }

    @Bean
    public Queue smsQueue() {
        return new Queue(smsQueue, true);
    }

    @Bean
    public Queue reportsQueue() {
        return new Queue(reportsQueue, true);
    }

    @Bean
    public TopicExchange hrExchange() {
        return new TopicExchange(hrExchange);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange hrExchange) {
        return BindingBuilder.bind(emailQueue).to(hrExchange).with("hr.email.#");
    }

    @Bean
    public Binding smsBinding(Queue smsQueue, TopicExchange hrExchange) {
        return BindingBuilder.bind(smsQueue).to(hrExchange).with("hr.sms.#");
    }

    @Bean
    public Binding reportsBinding(Queue reportsQueue, TopicExchange hrExchange) {
        return BindingBuilder.bind(reportsQueue).to(hrExchange).with("hr.reports.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
