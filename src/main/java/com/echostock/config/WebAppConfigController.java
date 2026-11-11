package com.echostock.config;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone la configuración pública de arranque para la versión Web / PWA del smartwatch
 * servida desde el mismo backend ({@code /web-config.js}), permitiendo que el cliente web
 * se autentique automáticamente contra {@code /api/v1} sin exponer secretos en el repositorio.
 */
@Hidden
@RestController
public class WebAppConfigController {

    private static final MediaType APPLICATION_JAVASCRIPT =
            MediaType.parseMediaType("application/javascript;charset=UTF-8");

    private final EchoStockProperties properties;

    public WebAppConfigController(EchoStockProperties properties) {
        this.properties = properties;
    }

    @GetMapping(value = "/web-config.js", produces = "application/javascript;charset=UTF-8")
    public ResponseEntity<String> webConfigJs() {
        String escapedKey = new String(JsonStringEncoder.getInstance().quoteAsString(properties.apiKey()));
        String body = "window.__ECHOSTOCK_SERVER_CONFIG__ = {"
                + "\"apiBaseUrl\":\"/api/v1\","
                + "\"apiKey\":\"" + escapedKey + "\","
                + "\"operatorCode\":\"OP-07\""
                + "};\n";
        return ResponseEntity.ok()
                .contentType(APPLICATION_JAVASCRIPT)
                .cacheControl(CacheControl.noStore())
                .body(body);
    }
}
