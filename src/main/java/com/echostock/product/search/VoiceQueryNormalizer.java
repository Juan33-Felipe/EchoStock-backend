package com.echostock.product.search;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Normalización de transcripciones de voz (RF-001). Funciones puras, sin estado.
 * Las reglas son idénticas a las del reloj (EchoStock-WearApp) para que ambos lados
 * interpreten igual lo que dice el operario.
 *
 * <ol>
 *   <li>Minúsculas y sin tildes ({@code "Cartón"} → {@code "carton"}, {@code "ñ"} → {@code "n"}).</li>
 *   <li>Todo lo que no sea letra o dígito (guiones, puntos, barras...) se trata como separador.</li>
 *   <li>Números en palabras → dígitos, solo con gramática válida: {@code "treinta y dos"} → {@code 32},
 *       {@code "ciento dos"} → {@code 102}, {@code "dos mil quinientos"} → {@code 2500}.
 *       Palabras numéricas sueltas consecutivas siguen separadas:
 *       {@code "cero cero uno"} → {@code "0 0 1"}.</li>
 *   <li>Nombres de letras → letras, tal como los transcribe el reconocedor de voz
 *       ({@code "se jota a"} → {@code "c j a"}). {@code "de"} solo se convierte en {@code d} dentro de un
 *       deletreo (junto a una letra, un nombre de letra o un número); si no, se mantiene como palabra
 *       ({@code "guantes de nitrilo"}).</li>
 *   <li>Letras sueltas consecutivas se unen ({@code "c j a"} → {@code "cja"}).</li>
 * </ol>
 *
 * <p>Para comparar códigos (SKU y ubicación) se descartan antes las palabras de relleno
 * ({@link #CODE_FILLER}) y se usan una forma compacta ({@code "cja001"}) y una clave de segmentos
 * sin ceros a la izquierda ({@code "A-01-03"} → {@code "a|1|3"}).
 */
public final class VoiceQueryNormalizer {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private static final Pattern RUNS = Pattern.compile("[a-z]+|[0-9]+");

    /** Palabras que el operario dice antes de un código y que se ignoran al comparar SKU/ubicación. */
    public static final Set<String> CODE_FILLER = Set.of(
            "pasillo", "ubicacion", "estante", "nivel", "rack", "sku", "codigo",
            "referencia", "ref", "producto", "numero", "guion");

    private static final Map<String, Integer> UNITS = Map.ofEntries(
            Map.entry("cero", 0), Map.entry("uno", 1), Map.entry("dos", 2), Map.entry("tres", 3),
            Map.entry("cuatro", 4), Map.entry("cinco", 5), Map.entry("seis", 6), Map.entry("siete", 7),
            Map.entry("ocho", 8), Map.entry("nueve", 9), Map.entry("diez", 10), Map.entry("once", 11),
            Map.entry("doce", 12), Map.entry("trece", 13), Map.entry("catorce", 14), Map.entry("quince", 15),
            Map.entry("dieciseis", 16), Map.entry("diecisiete", 17), Map.entry("dieciocho", 18),
            Map.entry("diecinueve", 19), Map.entry("veinte", 20), Map.entry("veintiuno", 21),
            Map.entry("veintidos", 22), Map.entry("veintitres", 23), Map.entry("veinticuatro", 24),
            Map.entry("veinticinco", 25), Map.entry("veintiseis", 26), Map.entry("veintisiete", 27),
            Map.entry("veintiocho", 28), Map.entry("veintinueve", 29));

    private static final Map<String, Integer> TENS = Map.of(
            "treinta", 30, "cuarenta", 40, "cincuenta", 50, "sesenta", 60,
            "setenta", 70, "ochenta", 80, "noventa", 90);

    private static final Map<String, Integer> HUNDREDS = Map.of(
            "ciento", 100, "doscientos", 200, "trescientos", 300, "cuatrocientos", 400,
            "quinientos", 500, "seiscientos", 600, "setecientos", 700, "ochocientos", 800,
            "novecientos", 900);

    private static final String HUNDRED_ALONE = "cien";
    private static final String THOUSAND = "mil";

    /** Nombres de letras en español que se convierten siempre (sin ambigüedad en este dominio). */
    private static final Map<String, String> LETTER_NAMES = Map.ofEntries(
            Map.entry("be", "b"), Map.entry("ce", "c"), Map.entry("se", "c"), Map.entry("efe", "f"),
            Map.entry("ge", "g"), Map.entry("hache", "h"), Map.entry("jota", "j"), Map.entry("cota", "j"),
            Map.entry("ka", "k"), Map.entry("ele", "l"), Map.entry("eme", "m"), Map.entry("ene", "n"),
            Map.entry("pe", "p"), Map.entry("cu", "q"), Map.entry("erre", "r"), Map.entry("ese", "s"),
            Map.entry("te", "t"), Map.entry("uve", "v"), Map.entry("ve", "v"), Map.entry("equis", "x"),
            Map.entry("ye", "y"), Map.entry("zeta", "z"));

    /** "de" solo es la letra d dentro de un deletreo. */
    private static final String LETTER_DE = "de";
    private static final String DOUBLE = "doble";

    /** Resultado de reconocer una expresión numérica: valor y número de tokens consumidos. */
    private record Parsed(int value, int consumed) {
    }

    private VoiceQueryNormalizer() {
    }

    /**
     * Normaliza una transcripción. Ejemplos: {@code "C J A cero cero uno"} → {@code "cja 0 0 1"},
     * {@code "Cajas tipo A"} → {@code "cajas tipo a"}, {@code "treinta y dos"} → {@code "32"}.
     */
    public static String normalize(String raw) {
        return String.join(" ", tokens(raw));
    }

    /** Tokens normalizados (ver {@link #normalize(String)}). */
    public static List<String> tokens(String raw) {
        if (raw == null) {
            return List.of();
        }
        String folded = foldCase(raw);
        if (folded.isEmpty()) {
            return List.of();
        }
        return joinSingleLetters(wordsToNumbers(letterNamesToLetters(Arrays.asList(folded.split(" ")))));
    }

    /** Minúsculas, sin tildes y con cualquier carácter no alfanumérico convertido en un espacio. */
    static String foldCase(String raw) {
        String lower = raw.toLowerCase(Locale.ROOT);
        String noAccents = DIACRITICS.matcher(Normalizer.normalize(lower, Normalizer.Form.NFD)).replaceAll("");
        return NON_ALNUM.matcher(noAccents).replaceAll(" ").trim();
    }

    /**
     * Tokens útiles para comparar códigos: los normalizados sin palabras de relleno
     * ({@code "pasillo a uno tres"} → {@code [a, 1, 3]}).
     */
    public static List<String> codeTokens(List<String> normalizedTokens) {
        return normalizedTokens.stream().filter(t -> !CODE_FILLER.contains(t)).toList();
    }

    /** Forma compacta de un código: solo letras y dígitos ({@code "CJA-001"} → {@code "cja001"}). */
    public static String compact(String code) {
        return NON_ALNUM.matcher(foldCase(code == null ? "" : code)).replaceAll("");
    }

    /** Forma compacta de una lista de tokens ya normalizados. */
    public static String compact(List<String> tokens) {
        return String.join("", tokens);
    }

    /**
     * Clave de segmentos de un código: grupos de letras y de dígitos, estos sin ceros a la izquierda,
     * unidos por {@code |}. {@code "A-01-03"} → {@code "a|1|3"}, {@code "CJA-001"} → {@code "cja|1"}.
     */
    public static String segmentKey(String code) {
        return String.join("|", segmentsOf(List.of(foldCase(code == null ? "" : code).split(" "))));
    }

    /** Clave de segmentos (ver {@link #segmentKey(String)}) de tokens ya normalizados. */
    public static String segmentKey(List<String> tokens) {
        return String.join("|", segmentsOf(tokens));
    }

    /** Segmentos alfabéticos/numéricos, los numéricos sin ceros a la izquierda. */
    public static List<String> segmentsOf(List<String> tokens) {
        List<String> segments = new ArrayList<>();
        for (String token : tokens) {
            var matcher = RUNS.matcher(token);
            while (matcher.find()) {
                String run = matcher.group();
                segments.add(Character.isDigit(run.charAt(0)) ? stripLeadingZeros(run) : run);
            }
        }
        return segments;
    }

    private static String stripLeadingZeros(String digits) {
        int i = 0;
        while (i < digits.length() - 1 && digits.charAt(i) == '0') {
            i++;
        }
        return digits.substring(i);
    }

    /** Sustituye cada expresión numérica en palabras por su valor en dígitos. */
    static List<String> wordsToNumbers(List<String> tokens) {
        List<String> out = new ArrayList<>(tokens.size());
        int i = 0;
        while (i < tokens.size()) {
            Parsed parsed = parseNumber(tokens, i);
            if (parsed != null) {
                out.add(String.valueOf(parsed.value()));
                i += parsed.consumed();
            } else {
                out.add(tokens.get(i));
                i++;
            }
        }
        return out;
    }

    /** number := [below1000] "mil" [below1000] | below1000 (el cero solo vale aislado). */
    private static Parsed parseNumber(List<String> tokens, int start) {
        if (start >= tokens.size()) {
            return null;
        }
        if ("cero".equals(tokens.get(start))) {
            return new Parsed(0, 1);
        }
        int pos = start;
        int value = 0;
        Parsed head = parseBelow1000(tokens, pos);
        boolean thousand = false;
        if (head != null && pos + head.consumed() < tokens.size()
                && THOUSAND.equals(tokens.get(pos + head.consumed()))) {
            value = head.value() * 1000;
            pos += head.consumed() + 1;
            thousand = true;
        } else if (THOUSAND.equals(tokens.get(pos))) {
            value = 1000;
            pos += 1;
            thousand = true;
        }
        if (!thousand) {
            return head;
        }
        Parsed tail = parseBelow1000(tokens, pos);
        if (tail != null) {
            value += tail.value();
            pos += tail.consumed();
        }
        return new Parsed(value, pos - start);
    }

    /** below1000 := "cien" | centenas [below100] | below100 (valores 1..999). */
    private static Parsed parseBelow1000(List<String> tokens, int start) {
        if (start >= tokens.size()) {
            return null;
        }
        String token = tokens.get(start);
        if (HUNDRED_ALONE.equals(token)) {
            return new Parsed(100, 1);
        }
        Integer hundreds = HUNDREDS.get(token);
        if (hundreds != null) {
            Parsed rest = parseBelow100(tokens, start + 1);
            return rest == null ? new Parsed(hundreds, 1) : new Parsed(hundreds + rest.value(), 1 + rest.consumed());
        }
        return parseBelow100(tokens, start);
    }

    /** below100 := uno..veintinueve | decenas ["y" uno..nueve] (valores 1..99). */
    private static Parsed parseBelow100(List<String> tokens, int start) {
        if (start >= tokens.size()) {
            return null;
        }
        String token = tokens.get(start);
        Integer small = UNITS.get(token);
        if (small != null) {
            return small == 0 ? null : new Parsed(small, 1);
        }
        Integer tens = TENS.get(token);
        if (tens == null) {
            return null;
        }
        if (start + 2 < tokens.size() && "y".equals(tokens.get(start + 1))) {
            Integer unit = UNITS.get(tokens.get(start + 2));
            if (unit != null && unit >= 1 && unit <= 9) {
                return new Parsed(tens + unit, 3);
            }
        }
        return new Parsed(tens, 1);
    }

    /**
     * Convierte nombres de letras en letras: {@code "se jota a"} → {@code [c, j, a]},
     * {@code "doble ve"}/{@code "doble uve"}/{@code "uve doble"} → {@code w}. {@code "de"} → {@code d} solo
     * si algún token vecino (en la transcripción original) es una letra suelta, un nombre de letra o un
     * número (en dígitos o en palabras).
     */
    static List<String> letterNamesToLetters(List<String> tokens) {
        List<String> out = new ArrayList<>(tokens.size());
        int i = 0;
        while (i < tokens.size()) {
            String token = tokens.get(i);
            String next = i + 1 < tokens.size() ? tokens.get(i + 1) : null;
            if (DOUBLE.equals(token) && ("ve".equals(next) || "uve".equals(next))) {
                out.add("w");
                i += 2;
            } else if ("uve".equals(token) && DOUBLE.equals(next)) {
                out.add("w");
                i += 2;
            } else if (LETTER_NAMES.containsKey(token)) {
                out.add(LETTER_NAMES.get(token));
                i++;
            } else if (LETTER_DE.equals(token) && isInsideSpelling(tokens, i)) {
                out.add("d");
                i++;
            } else {
                out.add(token);
                i++;
            }
        }
        return out;
    }

    private static boolean isInsideSpelling(List<String> tokens, int index) {
        return (index > 0 && isSpellingToken(tokens.get(index - 1)))
                || (index + 1 < tokens.size() && isSpellingToken(tokens.get(index + 1)));
    }

    private static boolean isSpellingToken(String token) {
        return (token.length() == 1 && Character.isLetter(token.charAt(0)))
                || LETTER_NAMES.containsKey(token)
                || token.chars().allMatch(Character::isDigit)
                || UNITS.containsKey(token) || TENS.containsKey(token) || HUNDREDS.containsKey(token)
                || HUNDRED_ALONE.equals(token) || THOUSAND.equals(token);
    }

    /** Une letras sueltas consecutivas: {@code [c, j, a, 0]} → {@code [cja, 0]}. */
    static List<String> joinSingleLetters(List<String> tokens) {
        List<String> out = new ArrayList<>(tokens.size());
        StringBuilder run = new StringBuilder();
        for (String token : tokens) {
            if (token.length() == 1 && Character.isLetter(token.charAt(0))) {
                run.append(token);
            } else {
                if (!run.isEmpty()) {
                    out.add(run.toString());
                    run.setLength(0);
                }
                out.add(token);
            }
        }
        if (!run.isEmpty()) {
            out.add(run.toString());
        }
        return out;
    }
}
