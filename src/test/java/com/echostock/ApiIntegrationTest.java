package com.echostock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base de los tests de integración: contexto completo con H2 en memoria (perfil {@code test}),
 * migraciones Flyway reales y MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class ApiIntegrationTest {

    protected static final String API_KEY_HEADER = "X-Api-Key";
    protected static final String API_KEY = "echostock-dev-key";

    @Autowired
    protected MockMvc mvc;
}
