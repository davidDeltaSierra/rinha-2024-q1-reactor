package com.rinha.backend_rinha_v2.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rinha.backend_rinha_v2.entity.TransactionType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.util.Objects;

import static java.util.Objects.nonNull;

public record TransactionRequest(
        @JsonProperty("valor")
        @Positive
        @NotNull
        Double amount,
        @JsonProperty("tipo")
        @NotNull
        String type,
        @JsonProperty("descricao")
        @NotEmpty
        @Length(min = 1, max = 10)
        String description
) {
    @AssertTrue
    boolean isAmountIntegerValue() {
        return Objects.nonNull(amount) && amount % 1 == 0;
    }

    @AssertTrue
    boolean isTransactionType() {
        return nonNull(TransactionType.from(type));
    }

    public TransactionType TransactionType() {
        return TransactionType.from(type);
    }
}
