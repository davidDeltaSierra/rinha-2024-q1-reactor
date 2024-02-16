package com.rinha.backend_rinha_v2.entity;

public enum TransactionType {
    c, d;

    public static TransactionType from(String source) {
        try {
            return TransactionType.valueOf(source);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
