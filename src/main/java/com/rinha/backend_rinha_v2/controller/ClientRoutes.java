package com.rinha.backend_rinha_v2.controller;

import com.rinha.backend_rinha_v2.controller.dto.ExtractResponse;
import com.rinha.backend_rinha_v2.controller.dto.TransactionRequest;
import com.rinha.backend_rinha_v2.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Component
@RequiredArgsConstructor
class ClientRoutes {
    private final ClientService clientService;

    @Bean
    RouterFunction<ServerResponse> validateAndRunTransaction() {
        return route(POST("/clientes/{id}/transacoes"), request ->
                request.bodyToMono(TransactionRequest.class)
                        .flatMap(
                                transactionRequest -> clientService.validateAndRunTransaction(Integer.parseInt(request.pathVariable("id")), transactionRequest)
                                        .flatMap(client -> ServerResponse.ok().bodyValue(Map.of("limite", client.getLimitCents(), "saldo", client.getAmount())))
                                        .switchIfEmpty(ServerResponse.notFound().build())
                                        .onErrorResume(ResponseStatusException.class, ex -> ServerResponse.status(ex.getStatusCode()).build())
                        )

        );
    }

    @Bean
    RouterFunction<ServerResponse> extract() {
        return route(GET("/clientes/{id}/extrato"), request ->
                clientService.extract(Integer.parseInt(request.pathVariable("id")))
                        .flatMap(client -> ServerResponse.ok().bodyValue(ExtractResponse.newExtractResponse(client)))
                        .switchIfEmpty(ServerResponse.notFound().build())
        );
    }
}
