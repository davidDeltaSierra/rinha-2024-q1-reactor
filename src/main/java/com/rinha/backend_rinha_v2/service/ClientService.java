package com.rinha.backend_rinha_v2.service;

import com.rinha.backend_rinha_v2.controller.dto.TransactionRequest;
import com.rinha.backend_rinha_v2.entity.Client;
import com.rinha.backend_rinha_v2.entity.Transaction;
import com.rinha.backend_rinha_v2.entity.TransactionCollection;
import com.rinha.backend_rinha_v2.entity.TransactionType;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.util.Optional.ofNullable;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final TransactionalOperator readCommitted;
    private final Validator validator;

    public Mono<Client> validateAndRunTransaction(Integer id, TransactionRequest transactionRequest) {
        return Mono.fromCallable(() -> {
            var constraintViolations = validator.validate(transactionRequest);
            if (!constraintViolations.isEmpty()) {
                throw new ResponseStatusException(UNPROCESSABLE_ENTITY);
            }
            return transactionRequest;
        }).flatMap(tr -> transaction(id, tr));
    }

    private Mono<Client> transaction(int id, TransactionRequest transactionRequest) {
        return r2dbcEntityTemplate.getDatabaseClient()
                .sql("SELECT * FROM client WHERE id=$1 FOR UPDATE")
                .bind(0, id)
                .map((row, metadata) -> r2dbcEntityTemplate.getConverter().read(Client.class, row, metadata))
                .first()
                .flatMap(client -> {
                    var amount = calculateAmount(client, transactionRequest);
                    var transaction = Transaction.builder()
                            .amount(transactionRequest.amount().intValue())
                            .type(transactionRequest.TransactionType())
                            .description(transactionRequest.description())
                            .createdAt(LocalDateTime.now())
                            .build();
                    var transactions = ofNullable(client.transactions())
                            .map(TransactionCollection::transactions)
                            .orElseGet(() -> new ArrayList<>(1));
                    if (!transactions.isEmpty()) {
                        transactions.sort((o1, o2) -> o2.createdAt().compareTo(o1.createdAt()));
                    }
                    transactions.addFirst(transaction);
                    return r2dbcEntityTemplate.update(
                            Client.builder()
                                    .id(client.id())
                                    .limitCents(client.limitCents())
                                    .amount(amount)
                                    .transactions(new TransactionCollection(transactions.subList(0, min(10, transactions.size()))))
                                    .build()
                    );
                })
                .as(readCommitted::transactional);
    }

    private int calculateAmount(Client client, TransactionRequest transactionRequest) {
        if (transactionRequest.TransactionType().equals(TransactionType.d)) {
            var amount = client.amount() - transactionRequest.amount().intValue();
            if (client.limitCents() < abs(amount)) {
                throw new ResponseStatusException(UNPROCESSABLE_ENTITY);
            }
            return amount;
        }
        return client.amount() + transactionRequest.amount().intValue();
    }

    public Mono<Client> extract(Integer id) {
        return r2dbcEntityTemplate.selectOne(query(where("id").is(id)), Client.class);
    }
}
