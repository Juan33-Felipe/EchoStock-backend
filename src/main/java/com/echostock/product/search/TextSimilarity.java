package com.echostock.product.search;

import java.util.HashSet;
import java.util.Set;

/** Medidas de similitud de texto, implementadas en Java para no depender de la base de datos. */
public final class TextSimilarity {

    private TextSimilarity() {
    }

    /** Distancia de edición de Levenshtein (inserción, borrado y sustitución con coste 1). */
    public static int levenshtein(String a, String b) {
        if (a.equals(b)) {
            return 0;
        }
        if (a.isEmpty()) {
            return b.length();
        }
        if (b.isEmpty()) {
            return a.length();
        }
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[b.length()];
    }

    /** Similitud 0..1 basada en Levenshtein: {@code 1 - distancia / longitudMáxima}. */
    public static double levenshteinRatio(String a, String b) {
        int max = Math.max(a.length(), b.length());
        return max == 0 ? 1.0 : 1.0 - (double) levenshtein(a, b) / max;
    }

    /** Coeficiente de Dice sobre trigramas de caracteres (con relleno de espacios). */
    public static double trigramDice(String a, String b) {
        Set<String> ta = trigrams(a);
        Set<String> tb = trigrams(b);
        if (ta.isEmpty() && tb.isEmpty()) {
            return 1.0;
        }
        if (ta.isEmpty() || tb.isEmpty()) {
            return 0.0;
        }
        Set<String> common = new HashSet<>(ta);
        common.retainAll(tb);
        return 2.0 * common.size() / (ta.size() + tb.size());
    }

    static Set<String> trigrams(String text) {
        Set<String> grams = new HashSet<>();
        if (text.isBlank()) {
            return grams;
        }
        String padded = "  " + text.trim() + " ";
        for (int i = 0; i + 3 <= padded.length(); i++) {
            grams.add(padded.substring(i, i + 3));
        }
        return grams;
    }
}
