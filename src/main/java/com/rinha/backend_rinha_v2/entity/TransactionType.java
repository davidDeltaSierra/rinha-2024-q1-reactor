package com.rinha.backend_rinha_v2.entity;

import com.fasterxml.jackson.annotation.JsonAlias;

public enum TransactionType {
    @JsonAlias("c")
    C,
    @JsonAlias("d")
    D
}
