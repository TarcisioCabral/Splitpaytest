package com.splitpay.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@Slf4j
public class NotificationListener {

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotification(Map<String, Object> message) {
        log.info("Receiving notification event: {}", message);
        
        String type = (String) message.getOrDefault("type", "unknown");
        
        if ("CONCILIATION_FAILED".equals(type)) {
            sendEmail("admin@splitpay.com", "Falha na Conciliação", 
                "A NF-e " + message.get("nfeKey") + " falhou na conciliação: " + message.get("reason"));
        } else if ("HIGH_VALUE_TRANSACTION".equals(type)) {
            sendWhatsApp("+5511999999999", "Alerta de Transação Alta: R$ " + message.get("amount"));
        } else if ("CONCILIATION_COMPLETED".equals(type)) {
            log.info("Conciliation completed for NF-e {}. Triggering webhooks...", message.get("nfeKey"));
            // Webhook dispatch logic will go here
        }
    }

    private void sendEmail(String to, String subject, String body) {
        log.info("[MOCK EMAIL] To: {}, Subject: {}, Body: {}", to, subject, body);
    }

    private void sendWhatsApp(String to, String message) {
        log.info("[MOCK WHATSAPP] To: {}, Message: {}", to, message);
    }
}
