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
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.Math.abs;
import static java.util.Objects.nonNull;
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
                .flatMap(client -> Mono.zip(createTransaction(transactionRequest, client), calculateAmount(client, transactionRequest)))
                .flatMap(tuple -> {
                    tuple.getT1().setAmount(tuple.getT2());
                    return r2dbcEntityTemplate.update(tuple.getT1());
                })
                .as(readCommitted::transactional);
    }

    private Mono<Client> createTransaction(TransactionRequest transactionRequest, Client client) {
        return Mono.fromSupplier(() -> {
            var transaction = Transaction.builder()
                    .amount(transactionRequest.amount().intValue())
                    .type(transactionRequest.TransactionType())
                    .description(transactionRequest.description())
                    .createdAt(LocalDateTime.now())
                    .build();
            if (nonNull(client.getTransactions())) {
                client.getTransactions()
                        .transactions()
                        .addFirst(transaction);
                client.getTransactions()
                        .transactions()
                        .sort((o1, o2) -> o2.createdAt().compareTo(o1.createdAt()));
                if (client.getTransactions().transactions().size() > 10) {
                    client.getTransactions().transactions().removeLast();
                }
            } else {
                client.setTransactions(new TransactionCollection(List.of(transaction)));
            }
            return client;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Integer> calculateAmount(Client client, TransactionRequest transactionRequest) {
        return Mono.fromSupplier(() -> {
            if (transactionRequest.TransactionType().equals(TransactionType.d)) {
                var amount = client.getAmount() - transactionRequest.amount().intValue();
                if (client.getLimitCents() < abs(amount)) {
                    throw new ResponseStatusException(UNPROCESSABLE_ENTITY);
                }
                return amount;
            }
            return client.getAmount() + transactionRequest.amount().intValue();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Client> extract(Integer id) {
        return r2dbcEntityTemplate.selectOne(query(where("id").is(id)), Client.class);
    }
}
