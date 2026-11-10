package com.echostock.product;

import com.echostock.common.web.PageResponse;
import com.echostock.product.search.SearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Productos")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/search")
    @Operation(summary = "Búsqueda tolerante a voz (RF-001)",
            description = "q puede ser nombre, SKU o ubicación tal como lo transcribió el reconocedor. "
                    + "Ranking: SKU exacto > ubicación exacta > nombre por similitud. Lista vacía si no hay nada.")
    public SearchResponse search(
            @Parameter(example = "c j a cero cero uno") @RequestParam @NotBlank @Size(max = 200) String q,
            @RequestParam(defaultValue = "5") @Min(1) @Max(10) int limit) {
        return service.search(q, limit);
    }

    @GetMapping("/{sku}")
    @Operation(summary = "Producto por SKU")
    public ProductResponse get(@Parameter(example = "CJA-001") @PathVariable String sku) {
        return ProductResponse.from(service.getBySku(sku));
    }

    @GetMapping
    @Operation(summary = "Listado paginado de productos, ordenado por SKU")
    public PageResponse<ProductResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return PageResponse.of(service.list(PageRequest.of(page, size, Sort.by("sku"))), ProductResponse::from);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un nuevo producto en el catálogo")
    public ProductResponse create(@Valid @RequestBody ProductCreateRequest request) {
        return ProductResponse.from(service.create(request));
    }

    @DeleteMapping("/{sku}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un producto por su SKU")
    public void delete(@Parameter(example = "CJA-001") @PathVariable String sku) {
        service.deleteBySku(sku);
    }
}
