package com.echostock.product.search;

import com.echostock.product.ProductResponse;
import com.echostock.product.search.ProductSearchScorer.MatchedBy;
import java.util.List;

/** Respuesta de {@code GET /products/search}. */
public record SearchResponse(String query, String normalized, List<Result> results) {

    public record Result(ProductResponse product, double score, MatchedBy matchedBy) {
    }
}
