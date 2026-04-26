package com.splitpay.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TaxRuleRepository extends JpaRepository<TaxRule, Long> {
    Optional<TaxRule> findBySegmentoAndFase(String segmento, String fase);
}
