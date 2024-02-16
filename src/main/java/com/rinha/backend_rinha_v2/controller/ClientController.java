package com.rinha.backend_rinha_v2.controller;

import com.rinha.backend_rinha_v2.controller.dto.ExtractResponse;
import com.rinha.backend_rinha_v2.controller.dto.TransactionRequest;
import com.rinha.backend_rinha_v2.controller.dto.TransactionResponse;
import com.rinha.backend_rinha_v2.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("clientes")
@RequiredArgsConstructor
class ClientController {
    private final ClientService clientService;

    @PostMapping("{id}/transacoes")
    Mono<ResponseEntity<TransactionResponse>> transaction(@Valid @RequestBody TransactionRequest transactionRequest,
                                                          @PathVariable Integer id) {
        return clientService.transaction(id, transactionRequest)
                .map(client -> ResponseEntity.ok(new TransactionResponse(client.limitCents(), client.amount())))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(ResponseStatusException.class, ex -> Mono.just(ResponseEntity.status(ex.getStatusCode()).build()));
    }

    @GetMapping("{id}/extrato")
    Mono<ResponseEntity<ExtractResponse>> extract(@PathVariable Integer id) {
        return clientService.extract(id)
                .map(client -> ResponseEntity.ok(ExtractResponse.newExtractResponse(client)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
