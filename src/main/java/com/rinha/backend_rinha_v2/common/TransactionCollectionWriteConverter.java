package com.rinha.backend_rinha_v2.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rinha.backend_rinha_v2.entity.TransactionCollection;
import io.r2dbc.postgresql.codec.Json;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import static java.util.Objects.isNull;

@WritingConverter
@RequiredArgsConstructor
class TransactionCollectionWriteConverter implements Converter<TransactionCollection, Json> {
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public Json convert(@Nullable TransactionCollection source) {
        if (isNull(source)) {
            return null;
        }
        return Json.of(objectMapper.writeValueAsBytes(source));
    }
}
