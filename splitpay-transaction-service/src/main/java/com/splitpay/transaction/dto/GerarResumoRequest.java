package com.splitpay.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record GerarResumoRequest(
    @NotNull
    @DecimalMin("0.00")
    BigDecimal faturamentoBruto,
    
    Boolean aplicarCreditos,
    
    @DecimalMin("0.00")
    BigDecimal creditosExteriores,

    String fase,
    String segmento
) {}
