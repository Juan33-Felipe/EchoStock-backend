package com.echostock.audit;

import java.util.List;

/**
 * Resultado de una sincronización offline. El reloj borra de su cola los {@code accepted} y
 * {@code duplicates}; los {@code rejected} se marcan como fallidos y no se reintentan.
 * Los ids se devuelven tal como los envió el reloj.
 */
public record AuditBatchResponse(List<String> accepted, List<String> duplicates, List<Rejection> rejected) {

    public record Rejection(String clientAuditId, String reason) {
    }
}
