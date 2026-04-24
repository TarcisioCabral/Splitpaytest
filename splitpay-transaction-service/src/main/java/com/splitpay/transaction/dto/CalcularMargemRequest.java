package com.splitpay.transaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CalcularMargemRequest(
    @NotNull
    @DecimalMin("0.01")
    @JsonProperty("preco_atual")
    BigDecimal precoAtual,
    
    @NotNull
    @DecimalMin("0.00")
    BigDecimal custo,
    
    @JsonProperty("fase_alvo")
    String faseAlvo,
    
    String segmento
) {}
