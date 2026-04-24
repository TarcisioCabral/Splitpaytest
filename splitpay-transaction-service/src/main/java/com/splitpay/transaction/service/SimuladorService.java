package com.splitpay.transaction.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import com.splitpay.transaction.dto.CalcularMargemRequest;

@Service
public class SimuladorService {

    @Autowired
    private IvaDualTaxService ivaDualTaxService;

    public Map<String, Object> calcularMargem(CalcularMargemRequest payload) {
        BigDecimal precoAtual = payload.precoAtual();
        BigDecimal custo = payload.custo();
        String faseAlvo = payload.faseAlvo() != null ? payload.faseAlvo() : "2029_pleno";
        String segmento = payload.segmento() != null ? payload.segmento() : "geral";

        Map<String, BigDecimal> rates = ivaDualTaxService.calculateRates(segmento, faseAlvo);
        BigDecimal aliquota = rates.get("total");

        // Cálculo da margem atual e projetada
        BigDecimal margemAtualValor = precoAtual.subtract(custo);
        BigDecimal margemAtualPct = margemAtualValor.divide(precoAtual, 4, RoundingMode.HALF_UP);

        // Preço ideal: Custo + Margem alvo / (1 - aliquota)
        BigDecimal fatorCusto = BigDecimal.ONE.subtract(aliquota);
        BigDecimal alvoMargemComCusto = margemAtualValor.add(custo);
        
        BigDecimal precoIdeal = alvoMargemComCusto.divide(fatorCusto, 2, RoundingMode.HALF_UP);
        BigDecimal impactoTributos = precoIdeal.multiply(aliquota).setScale(2, RoundingMode.HALF_UP);

        return Map.of(
                "preco_ideal", precoIdeal,
                "margem_atual", margemAtualPct,
                "margem_projetada", margemAtualPct, // A ideia é manter a mesma margem
                "impacto_tributos", impactoTributos,
                "aliquota", aliquota
        );
    }
}
