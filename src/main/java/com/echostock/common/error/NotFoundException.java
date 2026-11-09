package com.echostock.common.error;

import org.springframework.http.HttpStatus;

/** Recurso inexistente (404). */
public class NotFoundException extends ApiException {

    public NotFoundException(String detail) {
        super(HttpStatus.NOT_FOUND, ProblemTypes.NOT_FOUND, "Recurso no encontrado", detail);
    }
}
