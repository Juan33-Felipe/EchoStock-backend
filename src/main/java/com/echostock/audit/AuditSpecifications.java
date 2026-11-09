package com.echostock.audit;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/** Criterios dinámicos para la consulta de supervisión. */
final class AuditSpecifications {

    private AuditSpecifications() {
    }

    /** {@code from} inclusivo, {@code to} exclusivo. */
    static Specification<Audit> matching(AuditFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.sku() != null && !filter.sku().isBlank()) {
                predicates.add(cb.equal(root.get("sku"), filter.sku().trim().toUpperCase(Locale.ROOT)));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("recordedAt"), filter.from()));
            }
            if (filter.to() != null) {
                predicates.add(cb.lessThan(root.get("recordedAt"), filter.to()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
