package com.splitpay.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import com.splitpay.transaction.dto.ProcessTransactionRequest;

@RestController
@RequestMapping("/v1/split")
@RequiredArgsConstructor

public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    private final SseNotificationService sseNotificationService;

    @PostMapping("/process")
    public ResponseEntity<?> processTransaction(@Valid @RequestBody ProcessTransactionRequest payload) {
        String nfeKey = payload.nfeKey();
        BigDecimal valorBruto = payload.valorBruto();
        String adquirente = payload.adquirente();
        String segmento = payload.segmento();
        String fase = payload.fase();

        // Simple mock calc for IBS / CBS (0.5% each for 2026 phase)
        BigDecimal ibs = valorBruto.multiply(new BigDecimal("0.005"));
        BigDecimal cbs = valorBruto.multiply(new BigDecimal("0.005"));
        BigDecimal liquido = valorBruto.subtract(ibs).subtract(cbs);

        Transaction tx = new Transaction();
        tx.setNfeKey(nfeKey);
        tx.setValorBruto(valorBruto);
        tx.setIbsRetido(ibs);
        tx.setCbsRetido(cbs);
        tx.setLiquido(liquido);
        tx.setAdquirente(adquirente);
        tx.setSegmento(segmento);
        tx.setFase(fase);
        
        transactionRepository.save(tx);

        // Publish event to RabbitMQ
        rabbitTemplate.convertAndSend("transaction.created", Map.of(
            "transaction_id", tx.getId(),
            "nfe_key", nfeKey,
            "valor_bruto", valorBruto,
            "ibs_retido", ibs,
            "cbs_retido", cbs,
            "liquido", liquido,
            "adquirente", adquirente,
            "segmento", segmento,
            "fase", fase,
            "timestamp", tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : java.time.LocalDateTime.now().toString()
        ));

        // Return 202 Accepted, indicating processing has started but is not complete.
        return ResponseEntity.accepted().body(Map.of(
            "transaction_id", tx.getId(),
            "nfe_key", nfeKey,
            "status", "PENDING",
            "message", "Transação enviada para conciliação.",
            "timestamp", tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : java.time.LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/stream/{nfeKey}")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamConciliationUpdates(@PathVariable String nfeKey) {
        return sseNotificationService.createEmitter(nfeKey);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Transaction>> getRecentTransactions() {
        return ResponseEntity.ok(transactionRepository.findAll());
    }
}
