package com.echostock.product.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VoiceQueryNormalizerTest {

    @Nested
    @DisplayName("normalize")
    class Normalize {

        @ParameterizedTest(name = "\"{0}\" → \"{1}\"")
        @CsvSource(delimiter = '|', value = {
                // Ejemplos de la tabla del contrato
                "cajas tipo a            | cajas tipo a",
                "c j a cero cero uno     | cja 0 0 1",
                "a 1 3                   | a 1 3",
                "a uno tres              | a 1 3",
                // Minúsculas, tildes, eñes y puntuación
                "Cajas Tipo A            | cajas tipo a",
                "Cartón Retráctil        | carton retractil",
                "Señalización            | senalizacion",
                "CJA-001                 | cja 001",
                "  cajas,   tipo-A.      | cajas tipo a",
        })
        void normalizesTranscriptions(String raw, String expected) {
            assertThat(VoiceQueryNormalizer.normalize(raw)).isEqualTo(expected);
        }

        @Test
        void nullAndBlankBecomeEmpty() {
            assertThat(VoiceQueryNormalizer.normalize(null)).isEmpty();
            assertThat(VoiceQueryNormalizer.normalize("   -- ")).isEmpty();
        }
    }

    @Nested
    @DisplayName("números en palabras")
    class Numbers {

        @ParameterizedTest(name = "\"{0}\" → \"{1}\"")
        @CsvSource(delimiter = '|', value = {
                "cero                    | 0",
                "uno                     | 1",
                "quince                  | 15",
                "dieciséis               | 16",
                "veinte                  | 20",
                "veintidós               | 22",
                "treinta                 | 30",
                "treinta y dos           | 32",
                "noventa y nueve         | 99",
                "cien                    | 100",
                "ciento dos              | 102",
                "ciento treinta y dos    | 132",
                "quinientos              | 500",
                "novecientos noventa     | 990",
                "mil                     | 1000",
                "mil doscientos          | 1200",
                "dos mil quinientos      | 2500",
                "veinte mil              | 20000",
                "tres mil ciento uno     | 3101",
        })
        void convertsValidGrammar(String raw, String expected) {
            assertThat(VoiceQueryNormalizer.normalize(raw)).isEqualTo(expected);
        }

        @ParameterizedTest(name = "\"{0}\" → \"{1}\"")
        @CsvSource(delimiter = '|', value = {
                "cero cero uno           | 0 0 1",
                "uno tres                | 1 3",
                "dos tres cuatro         | 2 3 4",
                "cien dos                | 100 2",
                "veinte y dos            | 20 y 2",
                "treinta y               | 30 y",
                "treinta y diez          | 30 y 10",
        })
        void consecutiveOrInvalidWordsStaySeparate(String raw, String expected) {
            assertThat(VoiceQueryNormalizer.normalize(raw)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("letras sueltas")
    class SingleLetters {

        @Test
        void joinsConsecutiveSingleLetters() {
            assertThat(VoiceQueryNormalizer.normalize("c j a")).isEqualTo("cja");
            assertThat(VoiceQueryNormalizer.normalize("f l m dos")).isEqualTo("flm 2");
        }

        @Test
        void keepsIsolatedLetter() {
            assertThat(VoiceQueryNormalizer.normalize("tipo a")).isEqualTo("tipo a");
            assertThat(VoiceQueryNormalizer.normalize("a 1 b 2")).isEqualTo("a 1 b 2");
        }
    }

    @Nested
    @DisplayName("nombres de letras")
    class LetterNames {

        @ParameterizedTest(name = "\"{0}\" → \"{1}\"")
        @CsvSource(delimiter = '|', value = {
                "se jota a cero cero uno         | cja 0 0 1",
                "ce jota a cero cero uno         | cja 0 0 1",
                "efe ele eme cero cero dos       | flm 0 0 2",
                "be de cero uno                  | bd 0 1",
                "de a uno                        | da 1",
                "pe ele te uno                   | plt 1",
                "hache cota ka erre ese uve ve   | hjkrsvv",
                "equis ye zeta ge cu ene         | xyzgqn",
                "doble ve uno                    | w 1",
                "doble uve                       | w",
                "uve doble                       | w",
                "guantes de nitrilo talla eme    | guantes de nitrilo talla m",
                "cajas de carton doble canal     | cajas de carton doble canal",
                "cajas de archivo                | cajas de archivo",
        })
        void mapsLetterNames(String raw, String expected) {
            assertThat(VoiceQueryNormalizer.normalize(raw)).isEqualTo(expected);
        }

        @Test
        void spokenLetterNamesMatchSkuCompact() {
            List<String> code = VoiceQueryNormalizer.codeTokens(VoiceQueryNormalizer.tokens("se jota a cero cero uno"));
            assertThat(VoiceQueryNormalizer.compact(code)).isEqualTo("cja001");
        }
    }

    @Nested
    @DisplayName("vista de código")
    class CodeView {

        @ParameterizedTest(name = "\"{0}\" → compacto \"{1}\"")
        @CsvSource(delimiter = '|', value = {
                "c j a cero cero uno               | cja001",
                "sku c j a cero cero uno           | cja001",
                "código c j a guion cero cero uno  | cja001",
                "referencia CJA-001                | cja001",
                "producto número c j a 0 0 1       | cja001",
        })
        void dropsFillerWordsBeforeCompacting(String raw, String expected) {
            List<String> code = VoiceQueryNormalizer.codeTokens(VoiceQueryNormalizer.tokens(raw));
            assertThat(VoiceQueryNormalizer.compact(code)).isEqualTo(expected);
        }

        @ParameterizedTest(name = "\"{0}\" → clave \"{1}\"")
        @CsvSource(delimiter = ';', value = {
                "pasillo a uno tres ; a|1|3",
                "a 1 3 ; a|1|3",
                "a 01 03 ; a|1|3",
                "rack b estante 3 nivel 2 ; b|3|2",
        })
        void segmentKeyOfSpokenLocation(String raw, String expected) {
            List<String> code = VoiceQueryNormalizer.codeTokens(VoiceQueryNormalizer.tokens(raw));
            assertThat(VoiceQueryNormalizer.segmentKey(code)).isEqualTo(expected);
        }

        @Test
        void segmentKeyOfStoredCodes() {
            assertThat(VoiceQueryNormalizer.segmentKey("A-01-03")).isEqualTo("a|1|3");
            assertThat(VoiceQueryNormalizer.segmentKey("CJA-001")).isEqualTo("cja|1");
            assertThat(VoiceQueryNormalizer.segmentKey("B-10-00")).isEqualTo("b|10|0");
        }

        @Test
        void compactOfStoredCodes() {
            assertThat(VoiceQueryNormalizer.compact("CJA-001")).isEqualTo("cja001");
            assertThat(VoiceQueryNormalizer.compact("A-01-03")).isEqualTo("a0103");
        }
    }
}
