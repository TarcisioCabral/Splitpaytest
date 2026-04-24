package com.splitpay.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import jakarta.validation.Valid;
import com.splitpay.transaction.dto.GerarResumoRequest;

@RestController
@RequestMapping("/v1/declaracao")

public class DeclaracaoController {

    // Simula a obtenção de dados já guardados pelo ROC
    @GetMapping("/init")
    public ResponseEntity<?> getDadosFaturamento() {
        return ResponseEntity.ok(Map.of(
                "faturamentoBruto", new BigDecimal("1250000.00"),
                "baseCredito", new BigDecimal("850000.00")
        ));
    }

    // Simula a auditoria e recomendação inteligente
    @PostMapping("/validar")
    public ResponseEntity<?> validarECalcularRecomendacao() {
        // Exemplo: AI encontra 12.450 em créditos de sublicenciamento
        return ResponseEntity.ok(Map.of(
                "status", "conforme",
                "divergencias", 0,
                "creditosRecomendados", new BigDecimal("12450.00")
        ));
    }

    // Calcula o valor final resumido com/sem aplicação dos créditos
    @PostMapping("/resumo")
    public ResponseEntity<?> gerarResumo(@Valid @RequestBody GerarResumoRequest payload) {
        BigDecimal faturamentoBruto = payload.faturamentoBruto() != null ? payload.faturamentoBruto() : new BigDecimal("1250000.00");
        boolean aplicarCreditos = payload.aplicarCreditos() != null ? payload.aplicarCreditos() : false;
        BigDecimal creditosAplcados = payload.creditosExteriores() != null ? payload.creditosExteriores() : new BigDecimal("0.00");

        // Tributação Mensal presumida para o exemplo: IBS ~8.8% e CBS ~14.5% 
        BigDecimal ibs = faturamentoBruto.multiply(new BigDecimal("0.088")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal cbs = faturamentoBruto.multiply(new BigDecimal("0.145")).setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal totalCalculado = ibs.add(cbs);
        
        if (aplicarCreditos) {
            totalCalculado = totalCalculado.subtract(creditosAplcados);
        }

        return ResponseEntity.ok(Map.of(
                "ibsApurado", ibs,
                "cbsApurado", cbs,
                "creditosAplicados", aplicarCreditos ? creditosAplcados : BigDecimal.ZERO,
                "valorLiquidoAPagar", totalCalculado
        ));
    }
}
