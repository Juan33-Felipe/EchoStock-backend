package com.echostock.common.error;

import java.net.URI;

/**
 * Identificadores {@code type} de los errores RFC 7807 que devuelve la API.
 */
public final class ProblemTypes {

    private static final String BASE = "urn:echostock:problem:";

    public static final URI BAD_REQUEST = URI.create(BASE + "bad-request");
    public static final URI UNAUTHORIZED = URI.create(BASE + "unauthorized");
    public static final URI NOT_FOUND = URI.create(BASE + "not-found");
    public static final URI CONFLICT = URI.create(BASE + "conflict");
    public static final URI VALIDATION = URI.create(BASE + "validation");
    public static final URI INTERNAL = URI.create(BASE + "internal-error");

    private ProblemTypes() {
    }

    /** Tipo genérico para errores HTTP del framework (405, 406, 415...). */
    public static URI forStatus(int status) {
        return URI.create(BASE + "http-" + status);
    }
}
