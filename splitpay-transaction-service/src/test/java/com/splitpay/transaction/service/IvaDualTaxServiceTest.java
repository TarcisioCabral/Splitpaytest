package com.splitpay.transaction.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IvaDualTaxServiceTest {

    private IvaDualTaxService taxService;

    @BeforeEach
    void setUp() {
        taxService = new IvaDualTaxService();
    }

    @Test
    void shouldReturnBaseAliquotaFor2026() {
        BigDecimal base = taxService.getBaseAliquota("2026_teste");
        assertThat(base).isEqualByComparingTo("0.01");
    }

    @Test
    void shouldReturnBaseAliquotaFor2027() {
        BigDecimal base = taxService.getBaseAliquota("2027_cbs");
        assertThat(base).isEqualByComparingTo("0.135");
    }

    @Test
    void shouldReturnBaseAliquotaFor2028() {
        BigDecimal base = taxService.getBaseAliquota("2028_transicao");
        assertThat(base).isEqualByComparingTo("0.22");
    }

    @Test
    void shouldReturnBaseAliquotaFor2029() {
        BigDecimal base = taxService.getBaseAliquota("2029_pleno");
        assertThat(base).isEqualByComparingTo("0.27");
    }

    @Test
    void shouldCalculateRatesFor2026() {
        Map<String, BigDecimal> rates = taxService.calculateRates("outro", "2026_teste");
        assertThat(rates.get("ibs")).isEqualByComparingTo("0.005");
        assertThat(rates.get("cbs")).isEqualByComparingTo("0.005");
        assertThat(rates.get("total")).isEqualByComparingTo("0.01");
    }

    @Test
    void shouldCalculateRatesFor2027() {
        Map<String, BigDecimal> rates = taxService.calculateRates("outro", "2027_cbs");
        assertThat(rates.get("ibs")).isEqualByComparingTo("0.0");
        assertThat(rates.get("cbs")).isEqualByComparingTo("0.135");
        assertThat(rates.get("total")).isEqualByComparingTo("0.135");
    }

    @Test
    void shouldApplyReducerForSaudeAndAlimentacao() {
        Map<String, BigDecimal> rates = taxService.calculateRates("saude", "2029_pleno");
        
        // Base for 2029 is 0.27
        // IBS and CBS base without reducer = 0.135
        // Reducer is 0.4
        // IBS = 0.135 * 0.4 = 0.054
        // CBS = 0.135 * 0.4 = 0.054
        // Total = 0.108
        
        assertThat(rates.get("ibs")).isEqualByComparingTo("0.054");
        assertThat(rates.get("cbs")).isEqualByComparingTo("0.054");
        assertThat(rates.get("total")).isEqualByComparingTo("0.108");
    }

    @Test
    void shouldApplyReducerForEducacao() {
        Map<String, BigDecimal> rates = taxService.calculateRates("educacao", "2028_transicao");
        
        // Base for 2028 is 0.22
        // IBS and CBS base without reducer = 0.11
        // Reducer is 0.3
        // IBS = 0.11 * 0.3 = 0.033
        // CBS = 0.11 * 0.3 = 0.033
        // Total = 0.066
        
        assertThat(rates.get("ibs")).isEqualByComparingTo("0.033");
        assertThat(rates.get("cbs")).isEqualByComparingTo("0.033");
        assertThat(rates.get("total")).isEqualByComparingTo("0.066");
    }
}
