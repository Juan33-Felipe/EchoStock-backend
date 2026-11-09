package com.echostock.audit;

import com.echostock.common.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.time.Instant;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audits")
@Tag(name = "Auditorías")
public class AuditController {

    private final AuditService service;
    private final AuditBatchService batchService;

    public AuditController(AuditService service, AuditBatchService batchService) {
        this.service = service;
        this.batchService = batchService;
    }

    @PostMapping
    @Operation(summary = "Registrar auditoría en tiempo real (RF-003/004/005)",
            description = "Idempotente por clientAuditId: 201 la primera vez, 200 con la existente si se reenvía.")
    @ApiResponse(responseCode = "201", description = "Auditoría creada")
    @ApiResponse(responseCode = "200", description = "Ya recibida antes (idempotente)")
    @ApiResponse(responseCode = "404", description = "SKU inexistente")
    @ApiResponse(responseCode = "422", description = "Datos no válidos o result incoherente")
    public ResponseEntity<AuditResponse> create(@RequestBody AuditRequest request) {
        AuditService.Registration registration = service.register(request);
        AuditResponse body = AuditResponse.from(registration.audit());
        if (!registration.created()) {
            return ResponseEntity.ok(body);
        }
        return ResponseEntity.created(URI.create("/api/v1/audits/" + body.id())).body(body);
    }

    @PostMapping("/batch")
    @Operation(summary = "Sincronización offline (RF-006)",
            description = "Hasta 200 auditorías. Cada una se acepta, se marca como duplicada o se rechaza por separado.")
    public AuditBatchResponse batch(@Valid @RequestBody AuditBatchRequest request) {
        return batchService.registerAll(request.audits());
    }

    @GetMapping
    @Operation(summary = "Consulta para supervisión",
            description = "Paginado, ordenado por recordedAt descendente. from (inclusivo) y to (exclusivo) "
                    + "filtran por recordedAt en ISO-8601.")
    public PageResponse<AuditResponse> list(
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) AuditStatus status,
            @Parameter(example = "2026-11-09T00:00:00Z")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(example = "2026-11-10T00:00:00Z")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        Sort sort = Sort.by(Sort.Order.desc("recordedAt"), Sort.Order.desc("id"));
        return PageResponse.of(service.search(new AuditFilter(sku, status, from, to), PageRequest.of(page, size, sort)),
                AuditResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Auditoría por id")
    public AuditResponse get(@PathVariable long id) {
        return AuditResponse.from(service.get(id));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Aprobar discrepancia",
            description = "Solo PENDING_REVIEW (si no, 409). Fija product.systemQuantity = countedQuantity.")
    public AuditResponse approve(@PathVariable long id) {
        return AuditResponse.from(service.approve(id));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Rechazar discrepancia", description = "Solo PENDING_REVIEW (si no, 409). El stock no cambia.")
    public AuditResponse reject(@PathVariable long id) {
        return AuditResponse.from(service.reject(id));
    }
}
