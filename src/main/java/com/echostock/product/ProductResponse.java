package com.echostock.product;

import java.time.Instant;

/** Representación {@code Product} del contrato. */
public record ProductResponse(String sku, String name, String locationCode, String unit, int systemQuantity,
                              Instant updatedAt) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getSku(), product.getName(), product.getLocationCode(),
                product.getUnit(), product.getSystemQuantity(), product.getUpdatedAt());
    }
}
