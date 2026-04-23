package com.splitpay.transaction.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

@Service
public class IvaDualTaxService {

    /**
     * Obtém a alíquota base total dependendo da fase de implementação da Reforma Tributária.
     */
    public BigDecimal getBaseAliquota(String faseAlvo) {
        if ("2026_teste".equals(faseAlvo)) return new BigDecimal("0.01");
        if ("2027_cbs".equals(faseAlvo)) return new BigDecimal("0.135");
        if ("2028_transicao".equals(faseAlvo)) return new BigDecimal("0.22");
        // default 2029_pleno
        return new BigDecimal("0.27");
    }

    /**
     * Calcula as alíquotas dinâmicas de IBS e CBS baseadas no segmento (com reduções)
     * e na fase de transição (faseAlvo).
     */
    public Map<String, BigDecimal> calculateRates(String segmento, String faseAlvo) {
        BigDecimal totalAliquota = getBaseAliquota(faseAlvo);
        
        BigDecimal ibs;
        BigDecimal cbs;
        
        if ("2026_teste".equals(faseAlvo)) {
            ibs = new BigDecimal("0.005");
            cbs = new BigDecimal("0.005");
        } else if ("2027_cbs".equals(faseAlvo)) {
            ibs = BigDecimal.ZERO;
            cbs = new BigDecimal("0.135");
        } else {
            // Proporção de exemplo para as fases mais avançadas
            ibs = totalAliquota.divide(new BigDecimal("2"));
            cbs = totalAliquota.subtract(ibs);
        }

        // Aplicação de redutores por segmento conforme a emenda constitucional
        BigDecimal reducer = BigDecimal.ONE;
        if ("alimentacao".equalsIgnoreCase(segmento) || "saude".equalsIgnoreCase(segmento)) {
            reducer = new BigDecimal("0.4"); // Redução de 60%
        } else if ("educacao".equalsIgnoreCase(segmento)) {
            reducer = new BigDecimal("0.3"); // Redução de 70%
        }

        ibs = ibs.multiply(reducer);
        cbs = cbs.multiply(reducer);
        totalAliquota = ibs.add(cbs);

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("ibs", ibs);
        result.put("cbs", cbs);
        result.put("total", totalAliquota);
        return result;
    }
}
