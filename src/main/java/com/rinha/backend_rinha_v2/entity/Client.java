package com.rinha.backend_rinha_v2.entity;

import lombok.Builder;
import lombok.With;
import org.springframework.data.annotation.Id;

@With
@Builder
public record Client(
        @Id
        Integer id,
        Integer limitCents,
        Integer amount,
        TransactionCollection transactions) {
}
