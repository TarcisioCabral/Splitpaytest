package com.splitpay.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import com.splitpay.transaction.dto.ProcessTransactionRequest;
import com.splitpay.transaction.service.TransactionService;

@RestController
@RequestMapping("/v1/split")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/process")
    public ResponseEntity<?> processTransaction(@Valid @RequestBody ProcessTransactionRequest payload) {
        Map<String, Object> response = transactionService.processTransaction(payload);
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/stream/{nfeKey}")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamConciliationUpdates(@PathVariable String nfeKey) {
        return transactionService.streamConciliationUpdates(nfeKey);
    }

    @GetMapping("/stream/dashboard")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamDashboardUpdates() {
        return transactionService.streamDashboardUpdates();
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Transaction>> getRecentTransactions() {
        return ResponseEntity.ok(transactionService.getRecentTransactions());
    }
}
