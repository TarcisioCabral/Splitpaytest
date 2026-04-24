package com.splitpay.transaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcessarRetornoAdquirenteRequest(
    @JsonProperty("transaction_id")
    String transactionId
) {}
