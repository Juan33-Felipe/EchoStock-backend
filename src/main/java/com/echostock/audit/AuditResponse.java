package com.echostock.audit;

import java.time.Instant;

/** {@code Audit} completo del contrato: campos del cliente + {@code id, status, receivedAt, difference}. */
public record AuditResponse(
        Long id,
        String clientAuditId,
        String sku,
        String operatorCode,
        String deviceId,
        int systemQuantity,
        int countedQuantity,
        AuditResult result,
        AuditStatus status,
        Instant recordedAt,
        Instant receivedAt,
        int difference) {

    public static AuditResponse from(Audit audit) {
        return new AuditResponse(audit.getId(), audit.getClientAuditId(), audit.getSku(), audit.getOperatorCode(),
                audit.getDeviceId(), audit.getSystemQuantity(), audit.getCountedQuantity(), audit.getResult(),
                audit.getStatus(), audit.getRecordedAt(), audit.getReceivedAt(), audit.getDifference());
    }
}
