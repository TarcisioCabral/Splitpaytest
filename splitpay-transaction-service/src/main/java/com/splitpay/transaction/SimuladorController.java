package com.splitpay.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@RestController
@RequestMapping("/v1/simulador")
@CrossOrigin(origins = "*")
public class SimuladorController {

    @PostMapping("/margem")
    public ResponseEntity<?> calcularMargem(@RequestBody Map<String, Object> payload) {
        BigDecimal precoAtual = new BigDecimal(payload.get("preco_atual").toString());
        BigDecimal custo = new BigDecimal(payload.get("custo").toString());
        String faseAlvo = (String) payload.getOrDefault("fase_alvo", "2029_pleno");
        String segmento = (String) payload.getOrDefault("segmento", "geral");

        // Alíquota simplificada para o cálculo (ex: 27% para 2029_pleno)
        BigDecimal aliquota = new BigDecimal("0.27");
        if ("2026_teste".equals(faseAlvo)) aliquota = new BigDecimal("0.01");
        else if ("2027_cbs".equals(faseAlvo)) aliquota = new BigDecimal("0.135");
        else if ("2028_transicao".equals(faseAlvo)) aliquota = new BigDecimal("0.22");

        // Redutores por segmento
        if ("alimentacao".equals(segmento) || "saude".equals(segmento)) {
            aliquota = aliquota.multiply(new BigDecimal("0.4")); // Redução de 60%
        } else if ("educacao".equals(segmento)) {
            aliquota = aliquota.multiply(new BigDecimal("0.3")); // Redução de 70%
        }

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
                "impacto_tributos", impactoTributos
        ));
    }
}
