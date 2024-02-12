package com.rinha.backend_rinha_v2.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TransactionResponse(
        @JsonProperty("limite")
        Integer limit,
        @JsonProperty("saldo")
        Integer amount
) {
}
