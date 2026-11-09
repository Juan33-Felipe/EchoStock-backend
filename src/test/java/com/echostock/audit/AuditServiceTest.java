package com.echostock.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.echostock.common.error.ConflictException;
import com.echostock.common.error.NotFoundException;
import com.echostock.common.error.ValidationException;
import com.echostock.product.Product;
import com.echostock.product.ProductRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;

class AuditServiceTest {

    private static final jakarta.validation.ValidatorFactory VALIDATOR_FACTORY =
            Validation.buildDefaultValidatorFactory();
    private static final Instant NOW = Instant.parse("2026-11-09T18:35:00Z");
    private static final Instant RECORDED = Instant.parse("2026-11-09T18:31:12Z");

    private AuditRepository audits;
    private ProductRepository products;
    private AuditService service;
    private Product cajasA;

    @BeforeEach
    void setUp() {
        audits = mock(AuditRepository.class);
        products = mock(ProductRepository.class);
        Validator validator = VALIDATOR_FACTORY.getValidator();
        service = new AuditService(audits, products, validator, Clock.fixed(NOW, ZoneOffset.UTC),
                mock(PlatformTransactionManager.class));
        cajasA = new Product("CJA-001", "Cajas tipo A", "A-01-03", "cajas", 50);
        when(products.findBySku("CJA-001")).thenReturn(Optional.of(cajasA));
        when(audits.findByClientAuditId(any())).thenReturn(Optional.empty());
        when(audits.saveAndFlush(any(Audit.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterAll
    static void closeValidator() {
        VALIDATOR_FACTORY.close();
    }

    private static AuditRequest request(String id, int system, int counted, String result) {
        return new AuditRequest(id, "CJA-001", "OP-07", "wear-emulator-01", system, counted, result, RECORDED);
    }

    @Test
    void discrepancy_isPendingReviewWithDifference() {
        AuditService.Registration reg = service.register(request(UUID.randomUUID().toString(), 50, 48, "DISCREPANCY"));

        assertThat(reg.created()).isTrue();
        Audit audit = reg.audit();
        assertThat(audit.getResult()).isEqualTo(AuditResult.DISCREPANCY);
        assertThat(audit.getStatus()).isEqualTo(AuditStatus.PENDING_REVIEW);
        assertThat(audit.getDifference()).isEqualTo(-2);
        assertThat(audit.getReceivedAt()).isEqualTo(NOW);
        assertThat(audit.getRecordedAt()).isEqualTo(RECORDED);
        assertThat(audit.getSku()).isEqualTo("CJA-001");
    }

    @Test
    void match_isConfirmed() {
        Audit audit = service.register(request(UUID.randomUUID().toString(), 50, 50, "MATCH")).audit();

        assertThat(audit.getResult()).isEqualTo(AuditResult.MATCH);
        assertThat(audit.getStatus()).isEqualTo(AuditStatus.CONFIRMED);
        assertThat(audit.getDifference()).isZero();
    }

    @Test
    void missingResult_isComputedByServer() {
        Audit audit = service.register(request(UUID.randomUUID().toString(), 50, 53, null)).audit();

        assertThat(audit.getResult()).isEqualTo(AuditResult.DISCREPANCY);
        assertThat(audit.getDifference()).isEqualTo(3);
    }

    @Test
    void inconsistentResult_is422() {
        assertThatThrownBy(() -> service.register(request(UUID.randomUUID().toString(), 50, 48, "MATCH")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("result incoherente");
        verify(audits, never()).saveAndFlush(any());
    }

    @Test
    void negativeCount_is422() {
        assertThatThrownBy(() -> service.register(request(UUID.randomUUID().toString(), 50, -1, null)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("countedQuantity");
    }

    @Test
    void invalidUuid_is422() {
        assertThatThrownBy(() -> service.register(request("not-a-uuid", 50, 50, null)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("clientAuditId");
    }

    @Test
    void unknownSku_is404() {
        AuditRequest req = new AuditRequest(UUID.randomUUID().toString(), "NOPE-1", "OP-07", "w", 1, 1, null,
                RECORDED);

        assertThatThrownBy(() -> service.register(req)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void sameClientAuditId_returnsExistingWithoutSaving() {
        String id = UUID.randomUUID().toString();
        Audit existing = new Audit(id, cajasA, "OP-07", "w", 50, 48, RECORDED, NOW);
        when(audits.findByClientAuditId(id)).thenReturn(Optional.of(existing));

        AuditService.Registration reg = service.register(request(id, 50, 48, "DISCREPANCY"));

        assertThat(reg.created()).isFalse();
        assertThat(reg.audit()).isSameAs(existing);
        verify(audits, never()).saveAndFlush(any());
    }

    @Test
    void clientAuditId_isCaseInsensitive() {
        String id = UUID.randomUUID().toString();
        Audit existing = new Audit(id, cajasA, "OP-07", "w", 50, 48, RECORDED, NOW);
        when(audits.findByClientAuditId(id)).thenReturn(Optional.of(existing));

        assertThat(service.register(request(id.toUpperCase(), 50, 48, null)).created()).isFalse();
    }

    @Test
    void concurrentDuplicate_resolvesToExisting() {
        String id = UUID.randomUUID().toString();
        Audit winner = new Audit(id, cajasA, "OP-07", "w", 50, 48, RECORDED, NOW);
        when(audits.findByClientAuditId(id)).thenReturn(Optional.empty(), Optional.of(winner));
        when(audits.saveAndFlush(any(Audit.class))).thenThrow(new DataIntegrityViolationException("uk"));

        AuditService.Registration reg = service.register(request(id, 50, 48, null));

        assertThat(reg.created()).isFalse();
        assertThat(reg.audit()).isSameAs(winner);
    }

    @Test
    void approve_appliesCountToStock() {
        Audit audit = new Audit(UUID.randomUUID().toString(), cajasA, "OP-07", "w", 50, 48, RECORDED, NOW);
        when(audits.findById(7L)).thenReturn(Optional.of(audit));

        service.approve(7L);

        assertThat(audit.getStatus()).isEqualTo(AuditStatus.APPROVED);
        assertThat(audit.getReviewedAt()).isEqualTo(NOW);
        assertThat(cajasA.getSystemQuantity()).isEqualTo(48);
        assertThat(cajasA.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void reject_keepsStock() {
        Audit audit = new Audit(UUID.randomUUID().toString(), cajasA, "OP-07", "w", 50, 48, RECORDED, NOW);
        when(audits.findById(7L)).thenReturn(Optional.of(audit));

        service.reject(7L);

        assertThat(audit.getStatus()).isEqualTo(AuditStatus.REJECTED);
        assertThat(cajasA.getSystemQuantity()).isEqualTo(50);
    }

    @Test
    void approveOrReject_nonPending_is409() {
        Audit confirmed = new Audit(UUID.randomUUID().toString(), cajasA, "OP-07", "w", 50, 50, RECORDED, NOW);
        when(audits.findById(1L)).thenReturn(Optional.of(confirmed));

        assertThatThrownBy(() -> service.approve(1L)).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> service.reject(1L)).isInstanceOf(ConflictException.class);
        assertThat(cajasA.getSystemQuantity()).isEqualTo(50);
    }

    @Test
    void approve_twice_is409() {
        Audit audit = new Audit(UUID.randomUUID().toString(), cajasA, "OP-07", "w", 50, 48, RECORDED, NOW);
        when(audits.findById(2L)).thenReturn(Optional.of(audit));

        service.approve(2L);
        assertThatThrownBy(() -> service.approve(2L)).isInstanceOf(ConflictException.class);
    }

    @Test
    void unknownAudit_is404() {
        when(audits.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approve(99L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void search_fromAfterTo_is422() {
        AuditFilter filter = new AuditFilter(null, null, NOW, RECORDED);

        assertThatThrownBy(() -> service.search(filter, org.springframework.data.domain.Pageable.unpaged()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void batch_classifiesEachAuditIndependently() {
        AuditBatchService batch = new AuditBatchService(service);
        String ok = UUID.randomUUID().toString();
        String dup = UUID.randomUUID().toString();
        String badSku = UUID.randomUUID().toString();
        String incoherent = UUID.randomUUID().toString();
        when(audits.findByClientAuditId(dup))
                .thenReturn(Optional.of(new Audit(dup, cajasA, "OP-07", "w", 50, 50, RECORDED, NOW)));

        AuditBatchResponse response = batch.registerAll(List.of(
                request(ok, 50, 50, "MATCH"),
                request(dup, 50, 50, "MATCH"),
                new AuditRequest(badSku, "NOPE-1", "OP-07", "w", 1, 1, null, RECORDED),
                request(incoherent, 50, 49, "MATCH")));

        assertThat(response.accepted()).containsExactly(ok);
        assertThat(response.duplicates()).containsExactly(dup);
        assertThat(response.rejected()).extracting(AuditBatchResponse.Rejection::clientAuditId)
                .containsExactly(badSku, incoherent);
        assertThat(response.rejected().get(0).reason()).isEqualTo("SKU no encontrado");
        assertThat(response.rejected().get(1).reason()).contains("result incoherente");
    }

    @Test
    void batch_nullItem_isRejectedNotFatal() {
        AuditBatchService batch = new AuditBatchService(service);
        String ok = UUID.randomUUID().toString();
        List<AuditRequest> items = new java.util.ArrayList<>();
        items.add(null);
        items.add(request(ok, 50, 50, null));

        AuditBatchResponse response = batch.registerAll(items);

        assertThat(response.accepted()).containsExactly(ok);
        assertThat(response.rejected()).hasSize(1);
        assertThat(response.rejected().get(0).clientAuditId()).isNull();
    }
}
