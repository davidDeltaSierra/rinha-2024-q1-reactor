package com.rinha.backend_rinha_v2.entity;

import lombok.Builder;
import lombok.With;
import org.springframework.data.annotation.Id;

import java.util.List;

@With
@Builder
public record Client(
        @Id
        Integer id,
        Integer limit,
        Integer amount,
        List<Transaction> transactions,
        Boolean locked) {
}
