package com.rinha.backend_rinha_v2.service;

import com.rinha.backend_rinha_v2.controller.dto.TransactionRequest;
import com.rinha.backend_rinha_v2.entity.Client;
import com.rinha.backend_rinha_v2.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.function.Function;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.util.Optional.ofNullable;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static reactor.util.retry.Retry.backoff;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    private Mono<Client> findByIdBlockDocument(Integer id, Function<Client, Mono<Client>> action) {
        return reactiveMongoTemplate.findAndModify(
                        query(where("id").is(id)
                                .orOperator(
                                        where("locked").is(false),
                                        where("locked").isNull()
                                )),
                        new Update().set("locked", true),
                        Client.class
                )
                .switchIfEmpty(Mono.error(new RuntimeException("Register locked")))
                .retryWhen(backoff(10, Duration.ofMillis(100)))
                .flatMap(client ->
                        action.apply(client)
                                .flatMap(it -> reactiveMongoTemplate.save(it.withLocked(false)))
                                .onErrorResume(
                                        throwable -> reactiveMongoTemplate.save(client.withLocked(false))
                                                .then(Mono.error(throwable))
                                )
                );
    }

    public Mono<Client> transaction(Integer id, TransactionRequest transactionRequest) {
        return reactiveMongoTemplate.exists(query(where("id").is(id)), Client.class)
                .filter(find -> find)
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND)))
                .flatMap(exists -> findByIdBlockDocument(id, client -> {
                    var amount = client.amount() - transactionRequest.amount();
                    if (client.limit() < abs(amount)) {
                        return Mono.error(new ResponseStatusException(UNPROCESSABLE_ENTITY));
                    }
                    var transaction = Transaction.builder()
                            .amount(transactionRequest.amount())
                            .type(transactionRequest.type())
                            .description(transactionRequest.description())
                            .createdAt(LocalDateTime.now())
                            .build();
                    var transactions = ofNullable(client.transactions())
                            .orElseGet(() -> new ArrayList<>(1));
                    if (!transactions.isEmpty()) {
                        transactions.sort((o1, o2) -> o2.createdAt().compareTo(o1.createdAt()));
                    }
                    transactions.addFirst(transaction);
                    return Mono.just(
                            Client.builder()
                                    .id(client.id())
                                    .limit(client.limit())
                                    .amount(amount)
                                    .transactions(transactions.subList(0, min(10, transactions.size())))
                                    .build()
                    );
                }));
    }

    public Mono<Client> extract(Integer id) {
        return reactiveMongoTemplate.findById(id, Client.class)
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND)));
    }
}
