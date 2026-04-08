package com.splitpay.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/split")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow frontend to call directly for testing
public class TransactionController {

    private final TransactionRepository transactionRepository;

    @PostMapping("/process")
    public ResponseEntity<?> processTransaction(@RequestBody Map<String, Object> payload) {
        String nfeKey = (String) payload.get("nfe_key");
        BigDecimal valorBruto = new BigDecimal(payload.get("valor_bruto").toString());
        String adquirente = (String) payload.get("adquirente");
        String segmento = (String) payload.get("segmento");
        String fase = (String) payload.get("fase");

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

        return ResponseEntity.ok(Map.of(
            "transaction_id", tx.getId(),
            "ibs_retido", ibs,
            "cbs_retido", cbs,
            "liquido", liquido,
            "roc_confirmado", true,
            "timestamp", tx.getCreatedAt()
        ));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Transaction>> getRecentTransactions() {
        return ResponseEntity.ok(transactionRepository.findAll());
    }
}
