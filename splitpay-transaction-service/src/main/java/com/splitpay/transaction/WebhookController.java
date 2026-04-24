package com.splitpay.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import jakarta.validation.Valid;
import com.splitpay.transaction.dto.ProcessarRetornoAdquirenteRequest;

@RestController
@RequestMapping("/v1/webhooks")

public class WebhookController {

    @PutMapping("/adquirente")
    public ResponseEntity<?> processarRetornoAdquirente(@Valid @RequestBody ProcessarRetornoAdquirenteRequest payload) {
        // Exemplo simplificado onde aceitamos o webhook e retornamos confirmação
        String rawTransactionId = payload.transactionId() != null ? payload.transactionId() : "txn_unknown";
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
