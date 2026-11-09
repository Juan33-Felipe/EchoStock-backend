package com.echostock.audit;

import com.echostock.common.error.NotFoundException;
import com.echostock.common.error.ValidationException;
import com.echostock.product.Product;
import com.echostock.product.ProductRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Registro y revisión de auditorías.
 *
 * <p>Idempotencia: {@code clientAuditId} tiene restricción única. Reenviar el mismo id devuelve la
 * auditoría existente sin crear otra, incluso si dos peticiones llegan a la vez (la que pierde la
 * carrera choca con la restricción y se resuelve leyendo la ganadora).
 */
@Service
public class AuditService {

    /** Resultado de registrar una auditoría: {@code created = false} si ya existía. */
    public record Registration(Audit audit, boolean created) {
    }

    private final AuditRepository audits;
    private final ProductRepository products;
    private final Validator validator;
    private final Clock clock;
    private final TransactionTemplate tx;

    public AuditService(AuditRepository audits, ProductRepository products, Validator validator, Clock clock,
            PlatformTransactionManager transactionManager) {
        this.audits = audits;
        this.products = products;
        this.validator = validator;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactionManager);
    }

    /**
     * Registra una auditoría (o devuelve la existente con el mismo {@code clientAuditId}).
     *
     * @throws ValidationException si los datos no son válidos o {@code result} no cuadra (422)
     * @throws NotFoundException   si el SKU no existe (404)
     */
    public Registration register(AuditRequest request) {
        validate(request);
        String clientAuditId = canonicalId(request.clientAuditId());
        try {
            return tx.execute(status -> registerInTransaction(clientAuditId, request));
        } catch (DataIntegrityViolationException race) {
            // Otra petición con el mismo clientAuditId se guardó entre la comprobación y el insert.
            return findByClientAuditId(clientAuditId)
                    .map(existing -> new Registration(existing, false))
                    .orElseThrow(() -> race);
        }
    }

    private Registration registerInTransaction(String clientAuditId, AuditRequest request) {
        Optional<Audit> existing = audits.findByClientAuditId(clientAuditId);
        if (existing.isPresent()) {
            return new Registration(existing.get(), false);
        }
        String sku = request.sku().trim().toUpperCase(Locale.ROOT);
        Product product = products.findBySku(sku)
                .orElseThrow(() -> new NotFoundException("No existe ningún producto con SKU '%s'".formatted(sku)));

        AuditResult expected = AuditResult.of(request.systemQuantity(), request.countedQuantity());
        if (request.result() != null && !request.result().equals(expected.name())) {
            throw new ValidationException(
                    "result incoherente: con systemQuantity=%d y countedQuantity=%d el resultado es %s, no %s"
                            .formatted(request.systemQuantity(), request.countedQuantity(), expected,
                                    request.result()));
        }

        Audit audit = new Audit(clientAuditId, product, request.operatorCode().trim(), request.deviceId().trim(),
                request.systemQuantity(), request.countedQuantity(),
                request.recordedAt().truncatedTo(ChronoUnit.MICROS), now());
        return new Registration(audits.saveAndFlush(audit), true);
    }

    private Optional<Audit> findByClientAuditId(String clientAuditId) {
        return tx.execute(status -> audits.findByClientAuditId(clientAuditId));
    }

    @Transactional(readOnly = true)
    public Audit get(long id) {
        return audits.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe ninguna auditoría con id %d".formatted(id)));
    }

    @Transactional(readOnly = true)
    public Page<Audit> search(AuditFilter filter, Pageable pageable) {
        if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to())) {
            throw new ValidationException("'from' no puede ser posterior a 'to'");
        }
        return audits.findAll(AuditSpecifications.matching(filter), pageable);
    }

    /** {@code PENDING_REVIEW → APPROVED}: fija {@code product.systemQuantity = countedQuantity}. */
    @Transactional
    public Audit approve(long id) {
        Audit audit = get(id);
        audit.approve(now());
        return audit;
    }

    /** {@code PENDING_REVIEW → REJECTED}: el stock no cambia. */
    @Transactional
    public Audit reject(long id) {
        Audit audit = get(id);
        audit.reject(now());
        return audit;
    }

    /** Valida con Bean Validation y lanza 422 con todos los errores ordenados por campo. */
    void validate(AuditRequest request) {
        if (request == null) {
            throw new ValidationException("La auditoría no puede estar vacía");
        }
        Set<ConstraintViolation<AuditRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations.stream()
                    .sorted(Comparator.comparing((ConstraintViolation<AuditRequest> v) -> v.getPropertyPath().toString())
                            .thenComparing(ConstraintViolation::getMessage))
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; ")));
        }
    }

    /** UUID en minúsculas para que la idempotencia no dependa de mayúsculas/minúsculas. */
    static String canonicalId(String clientAuditId) {
        return UUID.fromString(clientAuditId.trim()).toString();
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.SECONDS);
    }
}
