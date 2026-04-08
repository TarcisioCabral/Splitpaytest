package com.splitpay.conciliation_service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/v1/conciliacao")
@CrossOrigin(origins = "*")
public class ConciliationController {

    @GetMapping("/{nfeKey}")
    public ResponseEntity<?> getConciliacaoStatus(@PathVariable String nfeKey) {
        // Mock temporário para retornar o status da conciliação esperado pelo componente API Docs
        
        return ResponseEntity.ok(Map.of(
                "nfe_key", nfeKey,
                "status", "conciliado",
                "nfe_valor", new BigDecimal("1000.00"),
                "split_retornado", new BigDecimal("10.00"),
                "divergencia", new BigDecimal("0.00"),
                "adquirente_confirmou", true
        ));
    }
}
