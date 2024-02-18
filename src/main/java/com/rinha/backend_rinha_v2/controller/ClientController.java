package com.rinha.backend_rinha_v2.controller;

import com.rinha.backend_rinha_v2.controller.dto.ExtractResponse;
import com.rinha.backend_rinha_v2.controller.dto.TransactionRequest;
import com.rinha.backend_rinha_v2.controller.dto.TransactionResponse;
import com.rinha.backend_rinha_v2.service.ClientService;
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
    Mono<ResponseEntity<TransactionResponse>> transaction(@RequestBody TransactionRequest transactionRequest,
                                                          @PathVariable String id) {
        return Mono.just(id)
                .flatMap(anStringId -> {
                    var parsedId = parseInt(anStringId);
                    if (parsedId == null) {
                        return Mono.just(ResponseEntity.unprocessableEntity().build());
                    }
                    return clientService.validateAndRunTransaction(parsedId, transactionRequest)
                            .map(client -> ResponseEntity.ok(new TransactionResponse(client.limitCents(), client.amount())))
                            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                            .onErrorResume(ResponseStatusException.class, ex -> Mono.just(ResponseEntity.status(ex.getStatusCode()).build()));
                });
    }

    private Integer parseInt(String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @GetMapping("{id}/extrato")
    Mono<ResponseEntity<ExtractResponse>> extract(@PathVariable Integer id) {
        return clientService.extract(id)
                .map(client -> ResponseEntity.ok(ExtractResponse.newExtractResponse(client)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
