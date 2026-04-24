package com.splitpay.transaction;

import com.splitpay.transaction.service.DeclaracaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import jakarta.validation.Valid;
import com.splitpay.transaction.dto.GerarResumoRequest;

@RestController
@RequestMapping("/v1/declaracao")
public class DeclaracaoController {

    @Autowired
    private DeclaracaoService declaracaoService;

    // Simula a obtenção de dados já guardados pelo ROC
    @GetMapping("/init")
    public ResponseEntity<?> getDadosFaturamento() {
        Map<String, Object> response = declaracaoService.getDadosFaturamento();
        return ResponseEntity.ok(response);
    }

    // Simula a auditoria e recomendação inteligente
    @PostMapping("/validar")
    public ResponseEntity<?> validarECalcularRecomendacao() {
        Map<String, Object> response = declaracaoService.validarECalcularRecomendacao();
        return ResponseEntity.ok(response);
    }

    // Calcula o valor final resumido com/sem aplicação dos créditos
    @PostMapping("/resumo")
    public ResponseEntity<?> gerarResumo(@Valid @RequestBody GerarResumoRequest payload) {
        Map<String, Object> response = declaracaoService.gerarResumo(payload);
        return ResponseEntity.ok(response);
    }
}
