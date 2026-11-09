package com.echostock.product.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.echostock.product.search.ProductSearchScorer.Hit;
import com.echostock.product.search.ProductSearchScorer.MatchedBy;
import com.echostock.product.search.ProductSearchScorer.Searchable;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProductSearchScorerTest {

    /** Subconjunto representativo del catálogo inicial. */
    private static final List<Searchable> CATALOG = List.of(
            new Searchable("CJA-001", "Cajas tipo A", "A-01-03"),
            new Searchable("CJA-002", "Cajas tipo B", "A-01-04"),
            new Searchable("CJA-003", "Cajas de cartón doble canal", "A-02-01"),
            new Searchable("CJA-005", "Cajas de archivo", "A-02-05"),
            new Searchable("SPR-001", "Separadores de cartón para pallet", "A-01-03"),
            new Searchable("FLM-001", "Rollos de film estirable", "B-03-01"),
            new Searchable("FLM-002", "Rollos de film de burbujas", "B-03-02"),
            new Searchable("PLT-001", "Pallets europeos de madera", "C-01-01"),
            new Searchable("TRN-001", "Tornillos hexagonales M8", "D-02-01"),
            new Searchable("GNT-001", "Guantes de nitrilo talla M", "E-01-01"),
            new Searchable("GNT-003", "Guantes anticorte", "E-01-03"));

    private static List<Hit<Searchable>> search(String query) {
        return ProductSearchScorer.rank(query, CATALOG, Function.identity(), 10);
    }

    private static List<String> skus(List<Hit<Searchable>> hits) {
        return hits.stream().map(h -> h.item().sku()).toList();
    }

    // --- Tabla de ejemplos del contrato -------------------------------------------------------

    @Test
    void contract_nameCajasTipoA_findsCja001First() {
        List<Hit<Searchable>> hits = search("cajas tipo a");

        assertThat(hits).isNotEmpty();
        assertThat(hits.get(0).item().sku()).isEqualTo("CJA-001");
        assertThat(hits.get(0).matchedBy()).isEqualTo(MatchedBy.NAME);
        assertThat(hits.get(0).score()).isGreaterThan(hits.get(1).score());
    }

    @Test
    void contract_spelledSku_findsCja001AsExactSku() {
        List<Hit<Searchable>> hits = search("c j a cero cero uno");

        assertThat(hits.get(0).item().sku()).isEqualTo("CJA-001");
        assertThat(hits.get(0).matchedBy()).isEqualTo(MatchedBy.SKU);
        assertThat(hits.get(0).score()).isEqualTo(1.0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"a 1 3", "a uno tres", "pasillo A uno tres", "A-01-03", "a 01 03", "a0103"})
    void contract_spokenLocation_findsProductsInA0103(String query) {
        List<Hit<Searchable>> hits = search(query);

        List<Hit<Searchable>> byLocation = hits.stream().filter(h -> h.matchedBy() == MatchedBy.LOCATION).toList();
        assertThat(skus(byLocation)).containsExactly("CJA-001", "SPR-001");
        assertThat(skus(hits).subList(0, 2)).containsExactly("CJA-001", "SPR-001");
        assertThat(byLocation).allSatisfy(h -> assertThat(h.score()).isEqualTo(0.9));
    }

    // --- Variantes de SKU ------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"CJA-001", "cja001", "sku c j a cero cero uno", "código c j a guion cero cero uno",
            "C J A 0 0 1"})
    void skuVariants_areExactSkuMatches(String query) {
        Hit<Searchable> top = search(query).get(0);

        assertThat(top.item().sku()).isEqualTo("CJA-001");
        assertThat(top.matchedBy()).isEqualTo(MatchedBy.SKU);
        assertThat(top.score()).isEqualTo(1.0);
    }

    @Test
    void skuWithoutLeadingZeros_matchesBySegments() {
        Hit<Searchable> top = search("c j a uno").get(0);

        assertThat(top.item().sku()).isEqualTo("CJA-001");
        assertThat(top.matchedBy()).isEqualTo(MatchedBy.SKU);
        assertThat(top.score()).isEqualTo(ProductSearchScorer.SKU_SEGMENTS);
    }

    @Test
    void skuPrefix_listsFamily() {
        List<Hit<Searchable>> hits = search("f l m");

        assertThat(skus(hits)).startsWith("FLM-001", "FLM-002");
        assertThat(hits.get(0).matchedBy()).isEqualTo(MatchedBy.SKU);
        assertThat(hits.get(0).score()).isLessThan(ProductSearchScorer.LOCATION_EXACT);
    }

    // --- Ranking ---------------------------------------------------------------------------------

    @Test
    void ranking_exactSkuBeatsExactLocationBeatsName() {
        assertThat(ProductSearchScorer.SKU_EXACT).isGreaterThan(ProductSearchScorer.SKU_SEGMENTS);
        assertThat(ProductSearchScorer.SKU_SEGMENTS).isGreaterThan(ProductSearchScorer.LOCATION_EXACT);
        assertThat(ProductSearchScorer.LOCATION_EXACT).isGreaterThan(ProductSearchScorer.NAME_MAX);

        // Un nombre idéntico nunca supera a una ubicación exacta.
        var name = ProductSearchScorer.score(ProductSearchScorer.analyze("cajas tipo a"), CATALOG.get(0));
        assertThat(name).get().extracting(ProductSearchScorer.Match::score)
                .isEqualTo(ProductSearchScorer.NAME_MAX);
    }

    @Test
    void locationPrefix_listsShelf() {
        List<Hit<Searchable>> hits = search("pasillo a uno");

        assertThat(hits).extracting(h -> h.item().sku()).contains("CJA-001", "CJA-002", "SPR-001");
        assertThat(hits.get(0).matchedBy()).isEqualTo(MatchedBy.LOCATION);
        assertThat(hits.get(0).score()).isEqualTo(ProductSearchScorer.LOCATION_PREFIX);
    }

    @Test
    void scoresAreSortedDescendingAndWithinRange() {
        List<Hit<Searchable>> hits = search("cajas de carton");

        assertThat(hits).isNotEmpty();
        assertThat(hits).allSatisfy(h -> assertThat(h.score()).isBetween(0.0, 1.0));
        for (int i = 1; i < hits.size(); i++) {
            assertThat(hits.get(i - 1).score()).isGreaterThanOrEqualTo(hits.get(i).score());
        }
        assertThat(hits.get(0).item().sku()).isEqualTo("CJA-003");
    }

    // --- Tolerancia en nombres -------------------------------------------------------------------

    @Test
    void name_toleratesSingularAccentsAndTypos() {
        assertThat(search("caja tipo a").get(0).item().sku()).isEqualTo("CJA-001");
        assertThat(search("rollo de film estirable").get(0).item().sku()).isEqualTo("FLM-001");
        assertThat(search("guantes anti corte").get(0).item().sku()).isEqualTo("GNT-003");
        assertThat(search("tornilos hexagonales").get(0).item().sku()).isEqualTo("TRN-001");
        assertThat(search("PALLETS EUROPEOS").get(0).item().sku()).isEqualTo("PLT-001");
    }

    @Test
    void name_distinguishesSiblingsByLastLetter() {
        assertThat(search("cajas tipo b").get(0).item().sku()).isEqualTo("CJA-002");
    }

    @Test
    void unrelatedOrEmptyQueries_returnNothing() {
        assertThat(search("helicoptero")).isEmpty();
        assertThat(search("")).isEmpty();
        assertThat(search("   ")).isEmpty();
    }

    @Test
    void respectsLimit() {
        assertThat(ProductSearchScorer.rank("cajas", CATALOG, Function.identity(), 2)).hasSize(2);
    }

    @Test
    void analyze_exposesNormalizedText() {
        assertThat(ProductSearchScorer.analyze("C J A cero cero uno").normalized()).isEqualTo("cja 0 0 1");
    }

    // --- Similitud ------------------------------------------------------------------------------

    @Test
    void textSimilarity_basics() {
        assertThat(TextSimilarity.levenshtein("kitten", "sitting")).isEqualTo(3);
        assertThat(TextSimilarity.levenshtein("", "abc")).isEqualTo(3);
        assertThat(TextSimilarity.levenshteinRatio("caja", "cajas")).isEqualTo(0.8);
        assertThat(TextSimilarity.trigramDice("cajas", "cajas")).isEqualTo(1.0);
        assertThat(TextSimilarity.trigramDice("cajas", "film")).isEqualTo(0.0);
        assertThat(ProductSearchScorer.tokenSimilarity("caj", "cajas")).isGreaterThanOrEqualTo(0.9);
    }
}
