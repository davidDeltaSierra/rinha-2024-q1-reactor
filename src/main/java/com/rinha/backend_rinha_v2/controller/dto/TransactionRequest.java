package com.rinha.backend_rinha_v2.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rinha.backend_rinha_v2.entity.TransactionType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionRequest(
        @NotNull
        @Positive
        @JsonProperty("valor")
        Integer amount,
        @NotNull
        @JsonProperty("tipo")
        TransactionType type,
        @NotEmpty
        @JsonProperty("descricao")
        String description
) {
}
