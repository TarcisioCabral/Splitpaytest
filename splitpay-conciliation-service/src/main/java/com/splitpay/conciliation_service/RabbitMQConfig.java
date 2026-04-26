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
    public static final String DLX_EXCHANGE = "splitpay.dlx";
    public static final String QUEUE_CONCILIATION_DLQ = "conciliation.failed.dlq";

    @Bean
    public org.springframework.amqp.core.TopicExchange deadLetterExchange() {
        return new org.springframework.amqp.core.TopicExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(QUEUE_CONCILIATION_DLQ, true);
    }

    @Bean
    public org.springframework.amqp.core.Binding deadLetterBinding() {
        return org.springframework.amqp.core.BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("conciliation.#");
    }

    @Bean
    public Queue transactionCreatedQueue() {
        return org.springframework.amqp.core.QueueBuilder.durable(QUEUE_TRANSACTION_CREATED)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "conciliation.failed")
                .build();
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
