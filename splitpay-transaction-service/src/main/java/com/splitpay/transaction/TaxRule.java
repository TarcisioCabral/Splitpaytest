package com.splitpay.transaction;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tax_rules")
public class TaxRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String segmento; // e.g., "alimentacao", "saude", "educacao", "geral"

    @Column(nullable = false)
    private String fase; // e.g., "2026_teste", "2027_cbs", "2028_transicao", "2029_pleno"

    @Column(name = "ibs_rate", nullable = false)
    private BigDecimal ibsRate;

    @Column(name = "cbs_rate", nullable = false)
    private BigDecimal cbsRate;
}
