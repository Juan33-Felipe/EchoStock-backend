package com.echostock.audit;

/** Resultado del conteo físico frente a lo que indicaba el sistema. */
public enum AuditResult {
    /** {@code countedQuantity == systemQuantity}. */
    MATCH,
    /** {@code countedQuantity != systemQuantity}. */
    DISCREPANCY;

    public static AuditResult of(int systemQuantity, int countedQuantity) {
        return systemQuantity == countedQuantity ? MATCH : DISCREPANCY;
    }
}
