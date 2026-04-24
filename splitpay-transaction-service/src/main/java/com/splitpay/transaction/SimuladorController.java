package com.splitpay.transaction;

import com.splitpay.transaction.service.SimuladorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import jakarta.validation.Valid;
import com.splitpay.transaction.dto.CalcularMargemRequest;

@RestController
@RequestMapping("/v1/simulador")
public class SimuladorController {

    @Autowired
    private SimuladorService simuladorService;

    @PostMapping("/margem")
    public ResponseEntity<?> calcularMargem(@Valid @RequestBody CalcularMargemRequest payload) {
        Map<String, Object> response = simuladorService.calcularMargem(payload);
        return ResponseEntity.ok(response);
    }
}
