package com.splitpay.transaction.service;

import com.splitpay.transaction.TaxRule;
import com.splitpay.transaction.TaxRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class IvaDualTaxServiceTest {

    private IvaDualTaxService taxService;
    private TaxRuleRepository taxRuleRepository;

    @BeforeEach
    void setUp() {
        taxRuleRepository = Mockito.mock(TaxRuleRepository.class);
        taxService = new IvaDualTaxService(taxRuleRepository);
    }

    @Test
    void shouldReturnBaseAliquotaFor2026() {
        when(taxRuleRepository.findBySegmentoAndFase("geral", "2026_teste"))
                .thenReturn(Optional.of(new TaxRule(1L, "geral", "2026_teste", new BigDecimal("0.005"), new BigDecimal("0.005"))));

        BigDecimal base = taxService.getBaseAliquota("2026_teste");
        assertThat(base).isEqualByComparingTo("0.01");
    }

    @Test
    void shouldCalculateRatesFor2026() {
        when(taxRuleRepository.findBySegmentoAndFase("outro", "2026_teste"))
                .thenReturn(Optional.empty());
        when(taxRuleRepository.findBySegmentoAndFase("geral", "2026_teste"))
                .thenReturn(Optional.of(new TaxRule(1L, "geral", "2026_teste", new BigDecimal("0.005"), new BigDecimal("0.005"))));

        Map<String, BigDecimal> rates = taxService.calculateRates("outro", "2026_teste");
        assertThat(rates.get("ibs")).isEqualByComparingTo("0.005");
        assertThat(rates.get("cbs")).isEqualByComparingTo("0.005");
        assertThat(rates.get("total")).isEqualByComparingTo("0.01");
    }

    @Test
    void shouldApplyRuleForSaude() {
        when(taxRuleRepository.findBySegmentoAndFase("saude", "2029_pleno"))
                .thenReturn(Optional.of(new TaxRule(2L, "saude", "2029_pleno", new BigDecimal("0.054"), new BigDecimal("0.054"))));

        Map<String, BigDecimal> rates = taxService.calculateRates("saude", "2029_pleno");
        
        assertThat(rates.get("ibs")).isEqualByComparingTo("0.054");
        assertThat(rates.get("cbs")).isEqualByComparingTo("0.054");
        assertThat(rates.get("total")).isEqualByComparingTo("0.108");
    }
}

