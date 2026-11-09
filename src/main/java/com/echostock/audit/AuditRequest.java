package com.echostock.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Campos de {@code Audit} que envía el reloj.
 *
 * <p>{@code result} es opcional: si llega, el servidor comprueba que cuadra con las cantidades
 * (si no, 422); si falta, se calcula.
 */
public record AuditRequest(
        @Schema(example = "6f1c3e2a-7d0b-4c55-9a8e-1f2b3c4d5e6f")
        @NotBlank
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "debe ser un UUID")
        String clientAuditId,

        @Schema(example = "CJA-001")
        @NotBlank @Size(max = 20)
        String sku,

        @Schema(example = "OP-07")
        @NotBlank @Size(max = 32)
        String operatorCode,

        @Schema(example = "wear-emulator-01")
        @NotBlank @Size(max = 64)
        String deviceId,

        @Schema(example = "50")
        @NotNull @PositiveOrZero
        Integer systemQuantity,

        @Schema(example = "48")
        @NotNull @PositiveOrZero
        Integer countedQuantity,

        @Schema(example = "DISCREPANCY", allowableValues = {"MATCH", "DISCREPANCY"})
        @Pattern(regexp = "MATCH|DISCREPANCY", message = "debe ser MATCH o DISCREPANCY")
        String result,

        @Schema(example = "2026-11-09T18:31:12Z")
        @NotNull
        Instant recordedAt) {
}
