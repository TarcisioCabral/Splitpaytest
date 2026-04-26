package com.splitpay.transaction;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nfe_key", unique = true, nullable = false)
    private String nfeKey;

    @Column(name = "valor_bruto", nullable = false)
    private BigDecimal valorBruto;

    @Column(name = "ibs_retido")
    private BigDecimal ibsRetido;

    @Column(name = "cbs_retido")
    private BigDecimal cbsRetido;

    private BigDecimal liquido;

    private String adquirente;
    private String segmento;
    private String fase;
    
    @Column(name = "currency")
    private String currency = "BRL";

    @Column(name = "original_amount")
    private BigDecimal originalAmount;

    @Column(name = "exchange_rate")
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
