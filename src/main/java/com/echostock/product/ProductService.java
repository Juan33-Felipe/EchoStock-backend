package com.echostock.product;

import com.echostock.common.error.NotFoundException;
import com.echostock.product.search.ProductSearchScorer;
import com.echostock.product.search.ProductSearchScorer.Searchable;
import com.echostock.product.search.SearchResponse;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    public Product getBySku(String sku) {
        return products.findBySku(sku.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new NotFoundException("No existe ningún producto con SKU '%s'".formatted(sku)));
    }

    public Page<Product> list(Pageable pageable) {
        return products.findAll(pageable);
    }

    /**
     * Búsqueda tolerante a voz. El catálogo de un almacén de este tamaño cabe de sobra en memoria,
     * así que se puntúa en Java sin funciones difusas específicas de ninguna base de datos.
     */
    public SearchResponse search(String query, int limit) {
        List<Product> catalog = products.findAll();
        List<SearchResponse.Result> results = ProductSearchScorer
                .rank(query, catalog, p -> new Searchable(p.getSku(), p.getName(), p.getLocationCode()), limit)
                .stream()
                .map(hit -> new SearchResponse.Result(ProductResponse.from(hit.item()), hit.score(), hit.matchedBy()))
                .toList();
        return new SearchResponse(query, ProductSearchScorer.analyze(query).normalized(), results);
    }
}
