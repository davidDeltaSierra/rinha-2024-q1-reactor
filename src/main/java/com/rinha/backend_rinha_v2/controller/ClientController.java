package com.rinha.backend_rinha_v2.controller;

import com.rinha.backend_rinha_v2.controller.dto.ExtractResponse;
import com.rinha.backend_rinha_v2.controller.dto.TransactionRequest;
import com.rinha.backend_rinha_v2.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("clientes")
@RequiredArgsConstructor
class ClientController {
    private final ClientService clientService;

    @PostMapping(value = "{id}/transacoes", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<?>> transaction(@RequestBody TransactionRequest transactionRequest,
                                        @PathVariable String id) {
        return Mono.just(id)
                .flatMap(anStringId -> {
                    var parsedId = parseInt(anStringId);
                    if (parsedId == null) {
                        return Mono.just(ResponseEntity.unprocessableEntity().build());
                    }
                    return clientService.validateAndRunTransaction(parsedId, transactionRequest)
                            .map(client -> ResponseEntity.ok(Map.of("limite", client.getLimitCents(), "saldo", client.getAmount())))
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
