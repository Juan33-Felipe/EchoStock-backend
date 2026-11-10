package com.echostock.product;

import com.echostock.audit.AuditRepository;
import com.echostock.common.error.NotFoundException;
import com.echostock.product.search.ProductSearchScorer;
import com.echostock.product.search.ProductSearchScorer.Searchable;
import com.echostock.product.search.SearchResponse;
import java.text.Normalizer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private final AuditRepository audits;

    public ProductService(ProductRepository products, AuditRepository audits) {
        this.products = products;
        this.audits = audits;
    }

    public Product getBySku(String sku) {
        return products.findBySku(sku.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new NotFoundException("No existe ningún producto con SKU '%s'".formatted(sku)));
    }

    public Page<Product> list(Pageable pageable) {
        return products.findAll(pageable);
    }

    @Transactional
    public Product create(ProductCreateRequest req) {
        String name = req.name().trim();
        String locationCode = (req.locationCode() == null || req.locationCode().isBlank())
                ? "A-01-01"
                : req.locationCode().trim().toUpperCase(Locale.ROOT);
        String unit = (req.unit() == null || req.unit().isBlank())
                ? "unidades"
                : req.unit().trim().toLowerCase(Locale.ROOT);
        int quantity = Math.max(0, req.systemQuantity());
        String sku = (req.sku() == null || req.sku().isBlank())
                ? generateSku(name)
                : req.sku().trim().toUpperCase(Locale.ROOT);

        return products.findBySku(sku)
                .map(existing -> {
                    existing.adjustSystemQuantity(quantity, Instant.now().truncatedTo(ChronoUnit.SECONDS));
                    return products.save(existing);
                })
                .orElseGet(() -> products.save(new Product(sku, name, locationCode, unit, quantity)));
    }

    @Transactional
    public void deleteBySku(String sku) {
        Product product = getBySku(sku);
        audits.deleteByProduct(product);
        products.delete(product);
    }

    private String generateSku(String name) {
        String normalized = Normalizer.normalize(name.toUpperCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^A-Z0-9]", "");
        String consonants = normalized.replaceAll("[AEIOU0-9]", "");
        String prefix = (consonants.length() >= 3 ? consonants : (normalized + "PRD")).substring(0, 3);
        for (int i = 1; i <= 999; i++) {
            String candidate = "%s-%03d".formatted(prefix, i);
            if (products.findBySku(candidate).isEmpty()) {
                return candidate;
            }
        }
        return "%s-%d".formatted(prefix, System.currentTimeMillis() % 1000);
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
