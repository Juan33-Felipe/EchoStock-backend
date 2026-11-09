package com.echostock.common.error;

import org.springframework.http.HttpStatus;

/** Operación incompatible con el estado actual del recurso (409). */
public class ConflictException extends ApiException {

    public ConflictException(String detail) {
        super(HttpStatus.CONFLICT, ProblemTypes.CONFLICT, "Conflicto de estado", detail);
    }
}
