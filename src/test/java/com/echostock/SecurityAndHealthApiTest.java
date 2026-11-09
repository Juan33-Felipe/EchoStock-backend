package com.echostock;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

class SecurityAndHealthApiTest extends ApiIntegrationTest {

    @Test
    void health_withoutKey_isUp() throws Exception {
        mvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"status\":\"UP\"}", true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/products", "/api/v1/products/CJA-001", "/api/v1/products/search?q=cajas",
            "/api/v1/audits"})
    void get_withoutKey_is401Problem(String path) throws Exception {
        mvc.perform(get(path))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:echostock:problem:unauthorized"))
                .andExpect(jsonPath("$.title").isString())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").isString());
    }

    @Test
    void wrongKey_is401() throws Exception {
        mvc.perform(get("/api/v1/products/CJA-001").header(API_KEY_HEADER, "otra-clave"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("La cabecera X-Api-Key no es válida"));
    }

    @Test
    void post_withoutKey_is401() throws Exception {
        mvc.perform(post("/api/v1/audits").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/audits/batch").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/audits/1/approve")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/audits/1/reject")).andExpect(status().isUnauthorized());
    }

    @Test
    void unknownApiRoute_withKey_is404Problem() throws Exception {
        mvc.perform(get("/api/v1/nada").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void corsPreflight_isAllowedWithoutKey() throws Exception {
        mvc.perform(options("/api/v1/products")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "X-Api-Key"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void openApiDocs_arePublic() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("EchoStock API"))
                .andExpect(jsonPath("$.paths['/api/v1/products/search']").exists());
    }
}
