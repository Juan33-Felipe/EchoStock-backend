package com.echostock.product.search;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Puntuación y ranking de productos frente a una transcripción de voz (RF-001). Lógica pura.
 *
 * <p>Ranking del contrato: coincidencia exacta de SKU &gt; ubicación exacta &gt; nombre por similitud.
 * Las bandas de puntuación garantizan ese orden:
 *
 * <table>
 *   <caption>Bandas de puntuación</caption>
 *   <tr><th>Coincidencia</th><th>score</th><th>matchedBy</th></tr>
 *   <tr><td>SKU exacto (forma compacta, {@code cja001})</td><td>1.00</td><td>SKU</td></tr>
 *   <tr><td>SKU por segmentos, sin ceros a la izquierda ({@code cja|1})</td><td>0.97</td><td>SKU</td></tr>
 *   <tr><td>Ubicación exacta ({@code a|1|3} o {@code a0103})</td><td>0.90</td><td>LOCATION</td></tr>
 *   <tr><td>Prefijo de SKU ({@code cja} → {@code CJA-*})</td><td>0.60–0.80</td><td>SKU</td></tr>
 *   <tr><td>Prefijo de ubicación ({@code a 1} → {@code A-01-*})</td><td>0.70</td><td>LOCATION</td></tr>
 *   <tr><td>Nombre por similitud</td><td>0.34–0.85</td><td>NAME</td></tr>
 * </table>
 *
 * <p>La similitud de nombre combina el solapamiento de tokens (cada token dicho se empareja con el
 * token del nombre más parecido por Levenshtein, tolerando plurales y erratas del reconocedor) y la
 * similitud de trigramas de la frase completa.
 */
public final class ProductSearchScorer {

    public enum MatchedBy { SKU, LOCATION, NAME }

    /** Vista mínima de un producto para puntuar. */
    public record Searchable(String sku, String name, String locationCode) {
    }

    public record Match(double score, MatchedBy matchedBy) {
    }

    public record Hit<T>(T item, double score, MatchedBy matchedBy) {
    }

    /** Consulta ya analizada (se calcula una vez y se compara con todo el catálogo). */
    public record Query(String raw, String normalized, String codeCompact, String codeKey,
                        int codeSegmentCount, List<String> nameTokens) {
    }

    static final double SKU_EXACT = 1.0;
    static final double SKU_SEGMENTS = 0.97;
    static final double LOCATION_EXACT = 0.90;
    static final double LOCATION_PREFIX = 0.70;
    static final double SKU_PREFIX_BASE = 0.60;
    static final double SKU_PREFIX_SPAN = 0.20;
    static final double NAME_MAX = 0.85;
    /** Similitud mínima de nombre (0..1) para considerar un producto relevante. */
    static final double NAME_THRESHOLD = 0.40;
    /** Similitud mínima entre dos tokens para considerarlos el mismo (p. ej. "caja" ≈ "cajas"). */
    static final double TOKEN_THRESHOLD = 0.70;
    static final int SKU_PREFIX_MIN_LENGTH = 3;

    private static final Set<String> NAME_STOPWORDS = Set.of(
            "de", "del", "el", "la", "los", "las", "para", "con", "por", "en", "un", "una", "y");

    private ProductSearchScorer() {
    }

    public static Query analyze(String raw) {
        List<String> tokens = VoiceQueryNormalizer.tokens(raw);
        List<String> code = VoiceQueryNormalizer.codeTokens(tokens);
        List<String> segments = VoiceQueryNormalizer.segmentsOf(code);
        List<String> nameTokens = code.stream().filter(t -> !NAME_STOPWORDS.contains(t)).toList();
        return new Query(raw, String.join(" ", tokens), VoiceQueryNormalizer.compact(code),
                String.join("|", segments), segments.size(), nameTokens);
    }

    /** Ordena {@code items} por relevancia y devuelve como mucho {@code limit} resultados. */
    public static <T> List<Hit<T>> rank(String rawQuery, Collection<T> items, Function<T, Searchable> view,
            int limit) {
        Query query = analyze(rawQuery);
        return items.stream()
                .map(item -> {
                    Searchable s = view.apply(item);
                    return score(query, s).map(m -> new RankedHit<>(new Hit<>(item, m.score(), m.matchedBy()),
                            s.sku()));
                })
                .flatMap(Optional::stream)
                .sorted(Comparator.<RankedHit<T>>comparingDouble(h -> -h.hit().score())
                        .thenComparing(h -> h.hit().matchedBy())
                        .thenComparing(RankedHit::sku))
                .limit(limit)
                .map(RankedHit::hit)
                .toList();
    }

    private record RankedHit<T>(Hit<T> hit, String sku) {
    }

    /** Mejor coincidencia de un producto con la consulta, o vacío si no es relevante. */
    public static Optional<Match> score(Query query, Searchable product) {
        if (query.normalized().isEmpty()) {
            return Optional.empty();
        }
        Match best = null;

        String skuCompact = VoiceQueryNormalizer.compact(product.sku());
        String skuKey = VoiceQueryNormalizer.segmentKey(product.sku());
        String locationCompact = VoiceQueryNormalizer.compact(product.locationCode());
        String locationKey = VoiceQueryNormalizer.segmentKey(product.locationCode());
        String qCompact = query.codeCompact();
        String qKey = query.codeKey();

        if (!qCompact.isEmpty()) {
            if (qCompact.equals(skuCompact)) {
                best = better(best, new Match(SKU_EXACT, MatchedBy.SKU));
            } else if (qKey.equals(skuKey)) {
                best = better(best, new Match(SKU_SEGMENTS, MatchedBy.SKU));
            } else if (qCompact.length() >= SKU_PREFIX_MIN_LENGTH && skuCompact.startsWith(qCompact)) {
                double coverage = (double) qCompact.length() / skuCompact.length();
                best = better(best, new Match(SKU_PREFIX_BASE + SKU_PREFIX_SPAN * coverage, MatchedBy.SKU));
            }

            if (qKey.equals(locationKey) || qCompact.equals(locationCompact)) {
                best = better(best, new Match(LOCATION_EXACT, MatchedBy.LOCATION));
            } else if (query.codeSegmentCount() >= 2 && startsWithLetter(qKey)
                    && locationKey.startsWith(qKey + "|")) {
                best = better(best, new Match(LOCATION_PREFIX, MatchedBy.LOCATION));
            }
        }

        double similarity = nameSimilarity(query.nameTokens(), nameTokensOf(product.name()));
        if (similarity >= NAME_THRESHOLD) {
            best = better(best, new Match(NAME_MAX * similarity, MatchedBy.NAME));
        }
        return Optional.ofNullable(best).map(m -> new Match(round(m.score()), m.matchedBy()));
    }

    static List<String> nameTokensOf(String name) {
        return VoiceQueryNormalizer.tokens(name).stream().filter(t -> !NAME_STOPWORDS.contains(t)).toList();
    }

    /**
     * Similitud 0..1 entre los tokens dichos y los del nombre:
     * 55 % cobertura de la consulta, 15 % cobertura del nombre, 30 % trigramas de la frase.
     */
    static double nameSimilarity(List<String> queryTokens, List<String> nameTokens) {
        if (queryTokens.isEmpty() || nameTokens.isEmpty()) {
            return 0.0;
        }
        double queryCoverage = queryTokens.stream()
                .mapToDouble(q -> bestTokenSimilarity(q, nameTokens))
                .map(s -> s >= TOKEN_THRESHOLD ? s : 0.0)
                .average().orElse(0.0);
        double nameCoverage = (double) nameTokens.stream()
                .filter(n -> bestTokenSimilarity(n, queryTokens) >= TOKEN_THRESHOLD)
                .count() / nameTokens.size();
        double trigram = TextSimilarity.trigramDice(String.join(" ", queryTokens), String.join(" ", nameTokens));
        return 0.55 * queryCoverage + 0.15 * nameCoverage + 0.30 * trigram;
    }

    private static double bestTokenSimilarity(String token, List<String> candidates) {
        double best = 0.0;
        for (String candidate : candidates) {
            best = Math.max(best, tokenSimilarity(token, candidate));
            if (best == 1.0) {
                break;
            }
        }
        return best;
    }

    /** Igualdad = 1; prefijo de al menos 3 letras ("caj" → "cajas") ≥ 0.9; si no, ratio de Levenshtein. */
    static double tokenSimilarity(String a, String b) {
        if (a.equals(b)) {
            return 1.0;
        }
        double ratio = TextSimilarity.levenshteinRatio(a, b);
        String shorter = a.length() <= b.length() ? a : b;
        String longer = shorter == a ? b : a;
        if (shorter.length() >= 3 && longer.startsWith(shorter)) {
            return Math.max(0.9, ratio);
        }
        return ratio;
    }

    private static boolean startsWithLetter(String key) {
        return !key.isEmpty() && Character.isLetter(key.charAt(0));
    }

    private static Match better(Match current, Match candidate) {
        return current == null || candidate.score() > current.score() ? candidate : current;
    }

    private static double round(double score) {
        return Math.round(score * 10_000.0) / 10_000.0;
    }
}
