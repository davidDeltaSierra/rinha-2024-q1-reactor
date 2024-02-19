package com.rinha.backend_rinha_v2.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rinha.backend_rinha_v2.entity.Client;
import com.rinha.backend_rinha_v2.entity.TransactionType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Objects.nonNull;

@Builder
public record ExtractResponse(
        @JsonProperty("saldo")
        Amount amount,
        @JsonProperty("ultimas_transacoes")
        List<Transaction> transactions
) {
    public record Amount(
            @JsonProperty("total")
            Integer amount,
            @JsonProperty("limite")
            Integer limit,
            @JsonProperty("data_extrato")
            LocalDateTime date
    ) {
    }

    public record Transaction(
            @JsonProperty("valor")
            Integer amount,
            @JsonProperty("tipo")
            TransactionType type,
            @JsonProperty("descricao")
            String description,
            @JsonProperty("realizada_em")
            LocalDateTime createdAt
    ) {
    }

    public static ExtractResponse newExtractResponse(Client client) {
        return new ExtractResponse(
                new Amount(client.getAmount(), client.getLimitCents(), LocalDateTime.now()),
                nonNull(client.getTransactions()) && nonNull(client.getTransactions().transactions())
                        ? client.getTransactions().transactions().stream()
                        .map(t -> new Transaction(t.amount(), t.type(), t.description(), t.createdAt()))
                        .toList()
                        : List.of()
        );
    }
}
