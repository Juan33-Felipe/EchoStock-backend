package com.echostock.audit;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Cuerpo de {@code POST /audits/batch}. Cada elemento se valida por separado: uno inválido se
 * devuelve en {@code rejected} sin afectar al resto.
 */
public record AuditBatchRequest(
        @NotNull(message = "es obligatorio")
        @Size(max = AuditBatchRequest.MAX_AUDITS, message = "admite como máximo " + AuditBatchRequest.MAX_AUDITS
                + " auditorías por petición")
        List<AuditRequest> audits) {

    public static final int MAX_AUDITS = 200;
}
