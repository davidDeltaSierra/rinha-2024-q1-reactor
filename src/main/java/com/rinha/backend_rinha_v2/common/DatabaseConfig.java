package com.rinha.backend_rinha_v2.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
class DatabaseConfig implements ApplicationRunner {
    private final ConnectionFactory connectionFactory;

    @Bean
    R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory connectionFactory,
                                                  ObjectMapper objectMapper) {
        return R2dbcCustomConversions.of(
                DialectResolver.getDialect(connectionFactory),
                new TransactionCollectionWriteConverter(objectMapper),
                new TransactionCollectionReadingConverter(objectMapper)
        );
    }

    @Bean
    TransactionalOperator readCommitted(ReactiveTransactionManager transactionManager) {
        return TransactionalOperator.create(
                transactionManager,
                new TransactionDefinition() {
                    @Override
                    public int getIsolationLevel() {
                        return TransactionDefinition.ISOLATION_READ_COMMITTED;
                    }
                }
        );
    }

    @Override
    public void run(ApplicationArguments args) {
        Mono.from(connectionFactory.create()).subscribe();
    }
}
