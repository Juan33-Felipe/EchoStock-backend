package com.echostock.audit;

import java.time.Instant;

/** Filtros opcionales de {@code GET /audits}. {@code from}/{@code to} se aplican sobre {@code recordedAt}. */
public record AuditFilter(String sku, AuditStatus status, Instant from, Instant to) {
}
