package com.rinha.backend_rinha_v2.service;

import com.rinha.backend_rinha_v2.controller.dto.TransactionRequest;
import com.rinha.backend_rinha_v2.entity.Client;
import com.rinha.backend_rinha_v2.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public Mono<Client> transaction(Integer id, TransactionRequest transactionRequest) {
        return reactiveMongoTemplate.findById(id, Client.class)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(client -> {
                    var amount = client.amount() - transactionRequest.amount();
                    if (client.limit() < abs(amount)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY));
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
                    return reactiveMongoTemplate.save(
                            Client.builder()
                                    .id(client.id())
                                    .limit(client.limit())
                                    .amount(amount)
                                    .transactions(transactions.subList(0, min(10, transactions.size())))
                                    .build()
                    );
                });
    }

    public Mono<Client> extract(Integer id) {
        return reactiveMongoTemplate.findById(id, Client.class)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }
}
