package com.splitpay.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import com.splitpay.transaction.dto.GerarResumoRequest;

@Service
@RequiredArgsConstructor
public class DeclaracaoService {

    private final IvaDualTaxService ivaDualTaxService;

    public Map<String, Object> getDadosFaturamento() {
        return Map.of(
                "faturamentoBruto", new BigDecimal("0.00"),
                "baseCredito", new BigDecimal("0.00"));
    }

    public Map<String, Object> validarECalcularRecomendacao() {
        // Exemplo: AI encontra 12.450 em créditos de sublicenciamento
        return Map.of(
                "status", "conforme",
                "divergencias", 0,
                "creditosRecomendados", new BigDecimal("12450.00"));
    }

    public Map<String, Object> gerarResumo(GerarResumoRequest payload) {
        BigDecimal faturamentoBruto = payload.faturamentoBruto() != null ? payload.faturamentoBruto()
                : new BigDecimal("1250000.00");
        boolean aplicarCreditos = payload.aplicarCreditos() != null ? payload.aplicarCreditos() : false;
        BigDecimal creditosAplcados = payload.creditosExteriores() != null ? payload.creditosExteriores()
                : new BigDecimal("0.00");
        String fase = payload.fase() != null ? payload.fase() : "2029_pleno";
        String segmento = payload.segmento() != null ? payload.segmento() : "servicos";

        // Tributação Mensal presumida usando IvaDualTaxService
        Map<String, BigDecimal> rates = ivaDualTaxService.calculateRates(segmento, fase);
        BigDecimal ibs = faturamentoBruto.multiply(rates.get("ibs")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal cbs = faturamentoBruto.multiply(rates.get("cbs")).setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalCalculado = ibs.add(cbs);

        if (aplicarCreditos) {
            totalCalculado = totalCalculado.subtract(creditosAplcados);
        }

        return Map.of(
                "ibsApurado", ibs,
                "cbsApurado", cbs,
                "creditosAplicados", aplicarCreditos ? creditosAplcados : BigDecimal.ZERO,
                "valorLiquidoAPagar", totalCalculado);
    }
}
