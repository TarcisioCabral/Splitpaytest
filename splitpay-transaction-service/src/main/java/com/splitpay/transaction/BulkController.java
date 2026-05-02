package com.splitpay.transaction;

import com.splitpay.transaction.dto.ProcessTransactionRequest;
import com.splitpay.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/v1/bulk")
@RequiredArgsConstructor
@Slf4j
public class BulkController {

    private final TransactionService transactionService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadBulk(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Arquivo vazio");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int count = 0;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                String nfeKey = line.trim();
                if (nfeKey.isEmpty()) continue;

                // Detect header: if first line contains letters and isn't a long numeric key, skip it
                if (isFirstLine) {
                    isFirstLine = false;
                    if (nfeKey.matches(".*[a-zA-Z].*") && nfeKey.length() < 40) {
                        log.info("Cabeçalho detectado e pulado: {}", nfeKey);
                        continue;
                    }
                }

                // Create a transaction request with mock values for required fields
                // but use the real service to calculate taxes and persist
                ProcessTransactionRequest request = new ProcessTransactionRequest(
                    nfeKey,
                    new BigDecimal(100.0 + (Math.random() * 100)), // Random mock value between 100 and 200
                    "ADQ_BULK",
                    "GERAL",
                    "2026_teste",
                    "BRL"
                );
                
                transactionService.processTransaction(request);
                count++;
            }
            
            log.info("Processamento em lote finalizado: {} transações criadas e enviadas para conciliação.", count);
            return ResponseEntity.accepted().body(Map.of(
                "message", "Upload em lote processado com sucesso. " + count + " transações criadas.",
                "status", "SUCCESS",
                "count", count
            ));
        } catch (Exception e) {
            log.error("Erro ao processar arquivo bulk", e);
            return ResponseEntity.internalServerError().body("Erro ao processar arquivo: " + e.getMessage());
        }
    }
}
