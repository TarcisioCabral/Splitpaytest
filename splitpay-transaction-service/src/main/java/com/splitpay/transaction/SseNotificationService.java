package com.splitpay.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SseNotificationService.class);
    
    // Key: nfeKey, Value: SseEmitter
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String nfeKey) {
        // Set timeout to 5 minutes (300_000 ms)
        SseEmitter emitter = new SseEmitter(300_000L);
        
        emitter.onCompletion(() -> {
            log.info("SSE Emitter completed for NFE: {}", nfeKey);
            emitters.remove(nfeKey);
        });
        
        emitter.onTimeout(() -> {
            log.warn("SSE Emitter timed out for NFE: {}", nfeKey);
            emitter.complete();
            emitters.remove(nfeKey);
        });
        
        emitter.onError((e) -> {
            log.error("SSE Emitter error for NFE: {}", nfeKey, e);
            emitters.remove(nfeKey);
        });
        
        emitters.put(nfeKey, emitter);
        
        // Send initial dummy event to establish connection
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected successfully. Waiting for conciliation..."));
        } catch (IOException e) {
            emitter.completeWithError(e);
            emitters.remove(nfeKey);
        }
        
        return emitter;
    }

    public void sendNotification(String nfeKey, Object data) {
        SseEmitter emitter = emitters.get(nfeKey);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("COMPLETED").data(data));
                emitter.complete();
            } catch (IOException e) {
                log.error("Error sending SSE notification for NFE: {}", nfeKey, e);
                emitters.remove(nfeKey);
            }
        } else {
            log.warn("No active SSE Emitter found for NFE: {}", nfeKey);
        }
    }
}
