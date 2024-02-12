package com.rinha.backend_rinha_v2.entity;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record Transaction(
        Integer amount,
        TransactionType type,
        String description,
        LocalDateTime createdAt
) {
}
