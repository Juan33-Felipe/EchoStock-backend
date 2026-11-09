package com.echostock.audit;

import com.echostock.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/** Auditoría de inventario registrada desde el reloj (conteo físico de un producto). */
@Entity
@Table(name = "audits")
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** UUID generado en el reloj: clave de idempotencia (restricción única en BD). */
    @Column(name = "client_audit_id", nullable = false, unique = true, length = 36, updatable = false)
    private String clientAuditId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @Column(nullable = false, length = 20, updatable = false)
    private String sku;

    @Column(name = "operator_code", nullable = false, length = 32, updatable = false)
    private String operatorCode;

    @Column(name = "device_id", nullable = false, length = 64, updatable = false)
    private String deviceId;

    /** Lo que el reloj leyó al operario (puede estar desactualizado si se auditó offline). */
    @Column(name = "system_quantity", nullable = false, updatable = false)
    private int systemQuantity;

    @Column(name = "counted_quantity", nullable = false, updatable = false)
    private int countedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16, updatable = false)
    private AuditResult result;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AuditStatus status;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    protected Audit() {
        // JPA
    }

    public Audit(String clientAuditId, Product product, String operatorCode, String deviceId, int systemQuantity,
            int countedQuantity, Instant recordedAt, Instant receivedAt) {
        this.clientAuditId = clientAuditId;
        this.product = product;
        this.sku = product.getSku();
        this.operatorCode = operatorCode;
        this.deviceId = deviceId;
        this.systemQuantity = systemQuantity;
        this.countedQuantity = countedQuantity;
        this.result = AuditResult.of(systemQuantity, countedQuantity);
        this.status = result == AuditResult.MATCH ? AuditStatus.CONFIRMED : AuditStatus.PENDING_REVIEW;
        this.recordedAt = recordedAt;
        this.receivedAt = receivedAt;
    }

    /** {@code countedQuantity - systemQuantity}. */
    public int getDifference() {
        return countedQuantity - systemQuantity;
    }

    /** Aprueba la discrepancia y aplica el conteo al stock del producto. */
    void approve(Instant when) {
        requirePendingReview("aprobar");
        product.adjustSystemQuantity(countedQuantity, when);
        status = AuditStatus.APPROVED;
        reviewedAt = when;
    }

    /** Descarta la discrepancia sin tocar el stock. */
    void reject(Instant when) {
        requirePendingReview("rechazar");
        status = AuditStatus.REJECTED;
        reviewedAt = when;
    }

    private void requirePendingReview(String action) {
        if (status != AuditStatus.PENDING_REVIEW) {
            throw new IllegalAuditTransitionException(
                    "Solo se pueden %s auditorías en estado PENDING_REVIEW; la auditoría %d está en %s"
                            .formatted(action, id, status));
        }
    }

    public Long getId() {
        return id;
    }

    public String getClientAuditId() {
        return clientAuditId;
    }

    public Product getProduct() {
        return product;
    }

    public String getSku() {
        return sku;
    }

    public String getOperatorCode() {
        return operatorCode;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public int getSystemQuantity() {
        return systemQuantity;
    }

    public int getCountedQuantity() {
        return countedQuantity;
    }

    public AuditResult getResult() {
        return result;
    }

    public AuditStatus getStatus() {
        return status;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }
}
