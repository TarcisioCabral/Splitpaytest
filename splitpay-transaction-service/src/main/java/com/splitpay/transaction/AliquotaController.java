package com.splitpay.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/v1/aliquotas")
@CrossOrigin(origins = "*")
public class AliquotaController {

    @GetMapping("/{segmento}")
    public ResponseEntity<?> getAliquotas(@PathVariable String segmento) {
        BigDecimal ibs = new BigDecimal("0.005");
        BigDecimal cbs = new BigDecimal("0.005");
        
        // Mocked logic for segment reductions in test phase 2026
        if ("alimentacao".equalsIgnoreCase(segmento) || "saude".equalsIgnoreCase(segmento)) {
            ibs = ibs.multiply(new BigDecimal("0.4"));
            cbs = cbs.multiply(new BigDecimal("0.4"));
        } else if ("educacao".equalsIgnoreCase(segmento)) {
            ibs = ibs.multiply(new BigDecimal("0.3"));
            cbs = cbs.multiply(new BigDecimal("0.3"));
        }

        BigDecimal total = ibs.add(cbs);

        return ResponseEntity.ok(Map.of(
                "segmento", segmento,
                "fase_atual", "2026_teste",
                "ibs", ibs,
                "cbs", cbs,
                "total", total,
                "cache_ttl_s", 3600,
                "fonte", "comite_gestor_ibs"
        ));
    }
}
