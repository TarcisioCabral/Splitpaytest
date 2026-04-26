package com.splitpay.transaction.service;

import com.splitpay.transaction.Transaction;
import com.splitpay.transaction.TransactionRepository;
import com.splitpay.transaction.SseNotificationService;
import com.splitpay.transaction.dto.ProcessTransactionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final RabbitTemplate rabbitTemplate;
    private final SseNotificationService sseNotificationService;
    private final IvaDualTaxService ivaDualTaxService;
    private final CurrencyService currencyService;

    public Map<String, Object> processTransaction(ProcessTransactionRequest payload) {
        String nfeKey = payload.nfeKey();
        BigDecimal originalAmount = payload.valorBruto();
        String adquirente = payload.adquirente();
        String segmento = payload.segmento();
        String fase = payload.fase();
        String currency = payload.currency() != null ? payload.currency() : "BRL";

        // Multi-currency support: convert to BRL if necessary
        BigDecimal exchangeRate = currencyService.getExchangeRate(currency);
        BigDecimal valorBruto = currencyService.convertToBrl(originalAmount, currency);

        // Dynamic calc for IBS / CBS based on phase and segment
        Map<String, BigDecimal> rates = ivaDualTaxService.calculateRates(segmento, fase);
        BigDecimal ibs = valorBruto.multiply(rates.get("ibs"));
        BigDecimal cbs = valorBruto.multiply(rates.get("cbs"));
        BigDecimal liquido = valorBruto.subtract(ibs).subtract(cbs);

        Transaction tx = new Transaction();
        tx.setNfeKey(nfeKey);
        tx.setValorBruto(valorBruto);
        tx.setOriginalAmount(originalAmount);
        tx.setCurrency(currency);
        tx.setExchangeRate(exchangeRate);
        tx.setIbsRetido(ibs);
        tx.setCbsRetido(cbs);
        tx.setLiquido(liquido);
        tx.setAdquirente(adquirente);
        tx.setSegmento(segmento);
        tx.setFase(fase);
        
        transactionRepository.save(tx);

        // Broadcast to dashboard
        sseNotificationService.broadcastToDashboard(tx);

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

        return Map.of(
            "transaction_id", tx.getId(),
            "nfe_key", nfeKey,
            "status", "PENDING",
            "message", "Transação enviada para conciliação.",
            "timestamp", tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : java.time.LocalDateTime.now().toString()
        );
    }

    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamConciliationUpdates(String nfeKey) {
        return sseNotificationService.createEmitter(nfeKey);
    }

    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamDashboardUpdates() {
        return sseNotificationService.createDashboardEmitter();
    }

    public List<Transaction> getRecentTransactions() {
        return transactionRepository.findAll();
    }
}
