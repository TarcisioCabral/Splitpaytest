package com.splitpay.conciliation_service.service;

import com.splitpay.conciliation_service.entity.Conciliation;
import com.splitpay.conciliation_service.repository.ConciliationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.splitpay.conciliation_service.RabbitMQConfig;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConciliationService {

    private final ConciliationRepository conciliationRepository;
    private final RabbitTemplate rabbitTemplate;

    public void processConciliation(Map<String, Object> payload) {
        String nfeKey = (String) payload.get("nfe_key");
        Object valorBrutoObj = payload.get("valor_bruto");
        
        BigDecimal valorBruto;
        if (valorBrutoObj instanceof Number) {
            valorBruto = new BigDecimal(valorBrutoObj.toString());
        } else if (valorBrutoObj instanceof String) {
            valorBruto = new BigDecimal((String) valorBrutoObj);
        } else {
            valorBruto = BigDecimal.ZERO;
        }

        log.info("Iniciando processo de conciliação para NFe: {}, Valor: {}", nfeKey, valorBruto);

        Optional<Conciliation> existing = conciliationRepository.findByNfeKey(nfeKey);
        Conciliation conciliation = existing.orElse(new Conciliation());

        conciliation.setNfeKey(nfeKey);
        conciliation.setValorBruto(valorBruto);
        conciliation.setStatus("PROCESSING");
        conciliation.setMessage("Processamento de cruzamento NF-e vs Split iniciado.");
        conciliation = conciliationRepository.save(conciliation);

        try {
            // Lógica real de cruzamento de dados seria implementada aqui
            Thread.sleep(500); 

            conciliation.setStatus("COMPLETED");
            conciliation.setMessage("Conciliação concluída com sucesso.");
            log.info("Conciliação concluída com sucesso para NFe: {}", nfeKey);

            rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_CONCILIATION_COMPLETED, Map.of(
                "nfe_key", nfeKey,
                "status", "COMPLETED",
                "message", "Conciliação concluída com sucesso.",
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            conciliation.setStatus("FAILED");
            conciliation.setMessage("Erro na conciliação: " + e.getMessage());
            log.error("Erro na conciliação da NFe {}", nfeKey, e);
        } finally {
            conciliationRepository.save(conciliation);
        }
    }
}
