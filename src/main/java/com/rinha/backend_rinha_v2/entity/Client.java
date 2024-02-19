package com.rinha.backend_rinha_v2.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;

@Data
public class Client {
    @Id
    private Integer id;
    private Integer limitCents;
    private Integer amount;
    private TransactionCollection transactions;

    @PersistenceCreator
    public Client(Integer id, Integer limitCents, Integer amount, TransactionCollection transactions) {
        this.id = id;
        this.limitCents = limitCents;
        this.amount = amount;
        this.transactions = transactions;
    }
}
