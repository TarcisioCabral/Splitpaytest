package com.splitpay.conciliation_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TransactionEventListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventListener.class);
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    public TransactionEventListener(org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TRANSACTION_CREATED)
    public void handleTransactionCreated(Map<String, Object> payload) {
        log.info("Received transaction.created event: {}", payload);
        
        // Simular o processo de conciliação assíncrona
        String nfeKey = (String) payload.get("nfe_key");
        Object valorBruto = payload.get("valor_bruto");
        
        log.info("Processando conciliação para NFe: {}, Valor Bruto: {}", nfeKey, valorBruto);
        
        // TODO: Inserir lógica real de salvar no banco a conciliação, notificar via webhook, etc.
        try {
            Thread.sleep(1000); // Simulando tempo de processamento
            log.info("Conciliação finalizada para NFe: {}", nfeKey);
            
            // Publish success event
            rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_CONCILIATION_COMPLETED, Map.of(
                "nfe_key", nfeKey,
                "status", "COMPLETED",
                "message", "Conciliação concluída com sucesso.",
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
