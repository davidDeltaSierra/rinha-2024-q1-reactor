package com.rinha.backend_rinha_v2.controller;

import com.rinha.backend_rinha_v2.controller.dto.ExtractResponse;
import com.rinha.backend_rinha_v2.controller.dto.TransactionRequest;
import com.rinha.backend_rinha_v2.controller.dto.TransactionResponse;
import com.rinha.backend_rinha_v2.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("clientes")
@RequiredArgsConstructor
class ClientController {
    private final ClientService clientService;

    @PostMapping("{id}/transacoes")
    Mono<TransactionResponse> transaction(@Valid @RequestBody TransactionRequest transactionRequest,
                                          @PathVariable Integer id) {
        return clientService.transaction(id, transactionRequest)
                .map(client -> new TransactionResponse(client.limit(), client.amount()));
    }

    @GetMapping("{id}/extrato")
    Mono<ExtractResponse> extract(@PathVariable Integer id) {
        return clientService.extract(id)
                .map(ExtractResponse::newExtractResponse);
    }
}
