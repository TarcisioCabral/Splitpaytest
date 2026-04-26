package com.splitpay.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SseNotificationService.class);
    
    // Key: nfeKey, Value: SseEmitter
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // Global emitters for the dashboard
    private final List<SseEmitter> dashboardEmitters = new CopyOnWriteArrayList<>();

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

    public SseEmitter createDashboardEmitter() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Dashboard should stay connected
        
        emitter.onCompletion(() -> dashboardEmitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            dashboardEmitters.remove(emitter);
        });
        emitter.onError((e) -> dashboardEmitters.remove(emitter));
        
        dashboardEmitters.add(emitter);
        
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Dashboard stream connected."));
        } catch (IOException e) {
            emitter.completeWithError(e);
            dashboardEmitters.remove(emitter);
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

    public void broadcastToDashboard(Object data) {
        log.info("Broadcasting update to {} dashboard emitters", dashboardEmitters.size());
        for (SseEmitter emitter : dashboardEmitters) {
            try {
                emitter.send(SseEmitter.event().name("TRANSACTION_UPDATE").data(data));
            } catch (IOException e) {
                log.warn("Failed to send broadcast to an emitter, removing it.");
                dashboardEmitters.remove(emitter);
            }
        }
    }
}
