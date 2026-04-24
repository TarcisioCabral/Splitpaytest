package com.splitpay.transaction;

import com.splitpay.transaction.service.IvaDualTaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/v1/aliquotas")

public class AliquotaController {

    @Autowired
    private IvaDualTaxService ivaDualTaxService;

    @GetMapping("/{segmento}")
    public ResponseEntity<?> getAliquotas(@PathVariable String segmento) {
        Map<String, BigDecimal> rates = ivaDualTaxService.calculateRates(segmento, "2026_teste");

        return ResponseEntity.ok(Map.of(
                "segmento", segmento,
                "fase_atual", "2026_teste",
                "ibs", rates.get("ibs"),
                "cbs", rates.get("cbs"),
                "total", rates.get("total"),
                "cache_ttl_s", 3600,
                "fonte", "comite_gestor_ibs"
        ));
    }
}
