package com.splitpay.transaction.service;

import com.splitpay.transaction.TaxRule;
import com.splitpay.transaction.TaxRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class IvaDualTaxService {

    private final TaxRuleRepository taxRuleRepository;

    /**
     * Calcula as alíquotas dinâmicas de IBS e CBS baseadas no segmento e na fase.
     * Busca as regras no banco de dados para permitir customização sem alteração de código.
     */
    public Map<String, BigDecimal> calculateRates(String segmento, String fase) {
        // Tenta buscar regra específica para o segmento e fase
        TaxRule rule = taxRuleRepository.findBySegmentoAndFase(segmento.toLowerCase(), fase)
                .orElseGet(() -> {
                    // Se não achar para o segmento, tenta o 'geral' para aquela fase
                    return taxRuleRepository.findBySegmentoAndFase("geral", fase)
                            .orElse(TaxRule.builder()
                                    .ibsRate(new BigDecimal("0.135"))
                                    .cbsRate(new BigDecimal("0.135"))
                                    .build());
                });

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("ibs", rule.getIbsRate());
        result.put("cbs", rule.getCbsRate());
        result.put("total", rule.getIbsRate().add(rule.getCbsRate()));
        return result;
    }

    /**
     * @deprecated Use calculateRates(segmento, fase) which handles everything dynamically.
     */
    @Deprecated
    public BigDecimal getBaseAliquota(String faseAlvo) {
        return calculateRates("geral", faseAlvo).get("total");
    }
}

