package com.splitpay.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/bulk")
@RequiredArgsConstructor
@Slf4j
public class BulkController {

    private final RabbitTemplate rabbitTemplate;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadBulk(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Arquivo vazio");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int count = 0;
            // Pula o cabeçalho se existir
            reader.readLine(); 
            
            while ((line = reader.readLine()) != null) {
                String nfeKey = line.trim();
                if (!nfeKey.isEmpty()) {
                    Map<String, Object> message = new HashMap<>();
                    message.put("nfe_key", nfeKey);
                    message.put("valor_bruto", 100.0); // Valor mock para bulk
                    message.put("timestamp", java.time.LocalDateTime.now().toString());
                    
                    rabbitTemplate.convertAndSend("transaction.created", message);
                    count++;
                }
            }
            
            log.info("Processamento em lote iniciado: {} NF-e enviadas para fila.", count);
            return ResponseEntity.accepted().body(Map.of(
                "message", "Upload em lote recebido. Processando " + count + " notas fiscais.",
                "status", "PROCESSING"
            ));
        } catch (Exception e) {
            log.error("Erro ao processar arquivo bulk", e);
            return ResponseEntity.internalServerError().body("Erro ao processar arquivo: " + e.getMessage());
        }
    }
}
