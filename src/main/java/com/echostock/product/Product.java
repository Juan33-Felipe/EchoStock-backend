package com.echostock.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Producto del almacén con su ubicación y la cantidad registrada en el sistema. */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String sku;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "location_code", nullable = false, length = 20)
    private String locationCode;

    @Column(nullable = false, length = 30)
    private String unit;

    @Column(name = "system_quantity", nullable = false)
    private int systemQuantity;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected Product() {
        // JPA
    }

    public Product(String sku, String name, String locationCode, String unit, int systemQuantity) {
        this.sku = sku;
        this.name = name;
        this.locationCode = locationCode;
        this.unit = unit;
        this.systemQuantity = systemQuantity;
        this.updatedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }

    /** Ajuste de stock aprobado por un supervisor. */
    public void adjustSystemQuantity(int newQuantity, Instant when) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        this.systemQuantity = newQuantity;
        this.updatedAt = when;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public String getUnit() {
        return unit;
    }

    public int getSystemQuantity() {
        return systemQuantity;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }
}
