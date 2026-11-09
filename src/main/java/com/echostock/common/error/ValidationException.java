package com.echostock.common.error;

import org.springframework.http.HttpStatus;

/** Petición bien formada pero semánticamente inválida (422). */
public class ValidationException extends ApiException {

    public ValidationException(String detail) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, ProblemTypes.VALIDATION, "Datos no válidos", detail);
    }
}
