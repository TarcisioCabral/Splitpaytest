package com.splitpay.transaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyService {

    private final RestTemplate restTemplate;
    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/BRL";
    
    private final Map<String, BigDecimal> cachedRates = new ConcurrentHashMap<>();
    private LocalDateTime lastUpdate;

    /**
     * Converte um valor de uma moeda estrangeira para BRL.
     */
    public BigDecimal convertToBrl(BigDecimal amount, String fromCurrency) {
        if ("BRL".equalsIgnoreCase(fromCurrency)) return amount;
        
        BigDecimal rate = getExchangeRate(fromCurrency);
        // A API retorna taxas em relação ao BRL (ex: 1 BRL = 0.19 USD)
        // Para converter USD -> BRL, dividimos pelo rate.
        return amount.divide(rate, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getExchangeRate(String currency) {
        updateRatesIfNeeded();
        return cachedRates.getOrDefault(currency.toUpperCase(), BigDecimal.ONE);
    }

    private synchronized void updateRatesIfNeeded() {
        if (lastUpdate == null || lastUpdate.isBefore(LocalDateTime.now().minusHours(1))) {
            try {
                log.info("Updating exchange rates from API...");
                Map<String, Object> response = restTemplate.getForObject(API_URL, Map.class);
                if (response != null && response.containsKey("rates")) {
                    Map<String, Double> rates = (Map<String, Double>) response.get("rates");
                    rates.forEach((k, v) -> cachedRates.put(k, BigDecimal.valueOf(v)));
                    lastUpdate = LocalDateTime.now();
                    log.info("Exchange rates updated successfully.");
                }
            } catch (Exception e) {
                log.error("Failed to update exchange rates: {}", e.getMessage());
                // Fallback to minimal defaults if cache is empty
                if (cachedRates.isEmpty()) {
                    cachedRates.put("USD", new BigDecimal("0.19"));
                    cachedRates.put("EUR", new BigDecimal("0.18"));
                }
            }
        }
    }
}
