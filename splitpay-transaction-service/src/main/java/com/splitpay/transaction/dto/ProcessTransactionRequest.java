package com.splitpay.transaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProcessTransactionRequest(
    @NotBlank
    @JsonProperty("nfe_key")
    String nfeKey,
    
    @NotNull
    @DecimalMin("0.01")
    @JsonProperty("valor_bruto")
    BigDecimal valorBruto,
    
    @NotBlank
    String adquirente,
    
    String segmento,
    
    String fase
) {}
