package com.splitpay.conciliation_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TransactionEventListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventListener.class);
    private final com.splitpay.conciliation_service.service.ConciliationService conciliationService;

    public TransactionEventListener(com.splitpay.conciliation_service.service.ConciliationService conciliationService) {
        this.conciliationService = conciliationService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TRANSACTION_CREATED)
    public void handleTransactionCreated(Map<String, Object> payload) {
        log.info("Received transaction.created event: {}", payload);
        conciliationService.processConciliation(payload);
    }
}
