package com.echostock.config;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propiedades {@code echostock.*}.
 *
 * @param apiKey clave exigida en la cabecera {@code X-Api-Key} para las rutas {@code /api/**}
 * @param cors   configuración CORS (abierta en desarrollo)
 */
@Validated
@ConfigurationProperties(prefix = "echostock")
public record EchoStockProperties(@NotBlank String apiKey, Cors cors) {

    public EchoStockProperties {
        if (cors == null) {
            cors = new Cors(List.of("*"));
        }
    }

    public record Cors(List<String> allowedOriginPatterns) {

        public Cors {
            allowedOriginPatterns = allowedOriginPatterns == null || allowedOriginPatterns.isEmpty()
                    ? List.of("*")
                    : List.copyOf(allowedOriginPatterns);
        }
    }
}
