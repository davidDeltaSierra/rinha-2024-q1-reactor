package com.rinha.backend_rinha_v2.entity;

import lombok.Builder;
import org.springframework.data.annotation.Id;

import java.util.List;

@Builder
public record Client(
        @Id
        Integer id,
        Integer limit,
        Integer amount,
        List<Transaction> transactions) {
}
