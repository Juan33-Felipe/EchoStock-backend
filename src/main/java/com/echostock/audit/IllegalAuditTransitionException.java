package com.echostock.audit;

import com.echostock.common.error.ConflictException;

/** Aprobar o rechazar una auditoría que no está en {@code PENDING_REVIEW} (409). */
public class IllegalAuditTransitionException extends ConflictException {

    public IllegalAuditTransitionException(String detail) {
        super(detail);
    }
}
