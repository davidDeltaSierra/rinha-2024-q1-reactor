package com.rinha.backend_rinha_v2.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rinha.backend_rinha_v2.entity.TransactionCollection;
import io.r2dbc.postgresql.codec.Json;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import static java.util.Objects.isNull;

@ReadingConverter
@RequiredArgsConstructor
class TransactionCollectionReadingConverter implements Converter<Json, TransactionCollection> {
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public TransactionCollection convert(@Nullable Json source) {
        if (isNull(source)) {
            return null;
        }
        return objectMapper.readValue(source.asArray(), TransactionCollection.class);
    }
}
