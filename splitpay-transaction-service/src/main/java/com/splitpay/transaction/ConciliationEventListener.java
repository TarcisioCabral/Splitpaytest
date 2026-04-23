package com.splitpay.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConciliationEventListener {

    private static final Logger log = LoggerFactory.getLogger(ConciliationEventListener.class);
    private final SseNotificationService sseNotificationService;

    public ConciliationEventListener(SseNotificationService sseNotificationService) {
        this.sseNotificationService = sseNotificationService;
    }

    @RabbitListener(queues = "conciliation.completed")
    public void handleConciliationCompleted(Map<String, Object> payload) {
        log.info("Received conciliation.completed event: {}", payload);
        String nfeKey = (String) payload.get("nfe_key");
        if (nfeKey != null) {
            sseNotificationService.sendNotification(nfeKey, payload);
        } else {
            log.warn("Received conciliation event without nfe_key: {}", payload);
        }
    }
}
