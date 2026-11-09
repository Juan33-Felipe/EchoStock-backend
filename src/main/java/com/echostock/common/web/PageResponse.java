package com.echostock.common.web;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/**
 * Formato de paginación del contrato:
 * {@code { "content": [...], "page": 0, "size": 20, "totalElements": 42, "totalPages": 3 }}.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(page.getContent().stream().map(mapper).toList(), page.getNumber(),
                page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
