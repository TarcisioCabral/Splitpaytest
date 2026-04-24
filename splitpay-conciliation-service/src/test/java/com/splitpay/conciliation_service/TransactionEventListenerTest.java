package com.splitpay.conciliation_service;

import com.splitpay.conciliation_service.service.ConciliationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class TransactionEventListenerTest {

    private ConciliationService conciliationService;
    private TransactionEventListener listener;

    @BeforeEach
    void setUp() {
        conciliationService = Mockito.mock(ConciliationService.class);
        listener = new TransactionEventListener(conciliationService);
    }

    @Test
    void handleTransactionCreated_shouldInvokeProcessConciliation() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("nfe_key", "99999");
        payload.put("valor_bruto", 100.0);

        listener.handleTransactionCreated(payload);

        verify(conciliationService).processConciliation(eq(payload));
    }
}
