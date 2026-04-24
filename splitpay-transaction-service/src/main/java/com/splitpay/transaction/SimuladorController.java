package com.splitpay.transaction;

import com.splitpay.transaction.service.IvaDualTaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import jakarta.validation.Valid;
import com.splitpay.transaction.dto.CalcularMargemRequest;

@RestController
@RequestMapping("/v1/simulador")

public class SimuladorController {

    @Autowired
    private IvaDualTaxService ivaDualTaxService;

    @PostMapping("/margem")
    public ResponseEntity<?> calcularMargem(@Valid @RequestBody CalcularMargemRequest payload) {
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

        return ResponseEntity.ok(Map.of(
                "preco_ideal", precoIdeal,
                "margem_atual", margemAtualPct,
                "margem_projetada", margemAtualPct, // A ideia é manter a mesma margem
                "impacto_tributos", impactoTributos,
                "aliquota", aliquota
        ));
    }
}
