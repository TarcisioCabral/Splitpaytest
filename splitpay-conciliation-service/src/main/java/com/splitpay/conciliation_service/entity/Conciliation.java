package com.splitpay.conciliation_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "conciliations")
@Data
@NoArgsConstructor
public class Conciliation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nfeKey;

    @Column(nullable = false)
    private BigDecimal valorBruto;

    @Column(nullable = false)
    private String status; // "PENDING", "COMPLETED", "FAILED"

    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
