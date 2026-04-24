package com.splitpay.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ConciliationEventListenerTest {

    private SseNotificationService sseNotificationService;
    private ConciliationEventListener listener;

    @BeforeEach
    void setUp() {
        sseNotificationService = Mockito.mock(SseNotificationService.class);
        listener = new ConciliationEventListener(sseNotificationService);
    }

    @Test
    void handleConciliationCompleted_shouldSendNotificationWhenNfeKeyIsPresent() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("nfe_key", "12345");
        payload.put("status", "SUCCESS");

        listener.handleConciliationCompleted(payload);

        verify(sseNotificationService).sendNotification(eq("12345"), eq(payload));
    }

    @Test
    void handleConciliationCompleted_shouldNotSendNotificationWhenNfeKeyIsMissing() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "SUCCESS");

        listener.handleConciliationCompleted(payload);

        verify(sseNotificationService, never()).sendNotification(any(), any());
    }
}
