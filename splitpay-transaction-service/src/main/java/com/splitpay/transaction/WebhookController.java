package com.splitpay.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/webhooks")
@CrossOrigin(origins = "*")
public class WebhookController {

    @PutMapping("/adquirente")
    public ResponseEntity<?> processarRetornoAdquirente(@RequestBody Map<String, Object> payload) {
        // Exemplo simplificado onde aceitamos o webhook e retornamos confirmação
        String rawTransactionId = (String) payload.getOrDefault("transaction_id", "txn_unknown");
        String protocoloRoc = "ROC2026" + UUID.randomUUID().toString().substring(0, 8);

        // Na prática, atualizaríamos o banco de dados (A Transaction retida sendo confirmada)
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Retorno processado com sucesso",
            "transaction_id", rawTransactionId,
            "roc_gerado", protocoloRoc
        ));
    }
}
