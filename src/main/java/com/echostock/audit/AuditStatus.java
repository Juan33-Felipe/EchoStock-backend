package com.echostock.audit;

/** Ciclo de vida de una auditoría en el servidor. */
public enum AuditStatus {
    /** El conteo coincide con el sistema; no requiere revisión. */
    CONFIRMED,
    /** Discrepancia pendiente de que un supervisor la apruebe o la rechace. */
    PENDING_REVIEW,
    /** El supervisor aplicó el ajuste: {@code product.systemQuantity = countedQuantity}. */
    APPROVED,
    /** El supervisor descartó la discrepancia; el stock no cambia. */
    REJECTED
}
