package com.echostock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada del servidor central EchoStock (ERP/WMS simplificado).
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class EchoStockApplication {

    public static void main(String[] args) {
        SpringApplication.run(EchoStockApplication.class, args);
    }
}
