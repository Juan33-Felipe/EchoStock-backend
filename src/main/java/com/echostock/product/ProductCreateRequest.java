package com.echostock.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Petición para registrar un nuevo producto en el catálogo. */
public record ProductCreateRequest(
        @Size(max = 20) String sku,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 20) String locationCode,
        @Size(max = 30) String unit,
        @Min(0) int systemQuantity
) {
}
