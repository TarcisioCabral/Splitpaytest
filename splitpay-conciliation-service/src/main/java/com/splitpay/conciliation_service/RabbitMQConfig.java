package com.splitpay.conciliation_service;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_TRANSACTION_CREATED = "transaction.created";
    public static final String QUEUE_CONCILIATION_COMPLETED = "conciliation.completed";

    @Bean
    public Queue transactionCreatedQueue() {
        return new Queue(QUEUE_TRANSACTION_CREATED, true); // durable = true
    }

    @Bean
    public Queue conciliationCompletedQueue() {
        return new Queue(QUEUE_CONCILIATION_COMPLETED, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
