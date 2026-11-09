package com.echostock;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ProductApiTest extends ApiIntegrationTest {

    @Test
    void getBySku_returnsContractProduct() throws Exception {
        mvc.perform(get("/api/v1/products/CJA-001").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sku").value("CJA-001"))
                .andExpect(jsonPath("$.name").value("Cajas tipo A"))
                .andExpect(jsonPath("$.locationCode").value("A-01-03"))
                .andExpect(jsonPath("$.unit").value("cajas"))
                .andExpect(jsonPath("$.systemQuantity").value(50))
                .andExpect(jsonPath("$.updatedAt").isString());
    }

    @Test
    void getBySku_isCaseInsensitive() throws Exception {
        mvc.perform(get("/api/v1/products/cja-001").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("CJA-001"));
    }

    @Test
    void getBySku_unknown_is404Problem() throws Exception {
        mvc.perform(get("/api/v1/products/NOPE-999").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:echostock:problem:not-found"))
                .andExpect(jsonPath("$.title").isString())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").isString());
    }

    @Test
    void list_isPaginatedAndSortedBySku() throws Exception {
        mvc.perform(get("/api/v1/products?page=0&size=5").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[0].sku").value("ARD-001"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(32))
                .andExpect(jsonPath("$.totalPages").value(7));
    }

    @Test
    void list_defaultsToPageZeroSize20() throws Exception {
        mvc.perform(get("/api/v1/products").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.content", hasSize(20)));
    }

    @Test
    void list_invalidSize_is400() throws Exception {
        mvc.perform(get("/api/v1/products?size=0").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void search_spelledSku() throws Exception {
        mvc.perform(get("/api/v1/products/search").param("q", "c j a cero cero uno").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("c j a cero cero uno"))
                .andExpect(jsonPath("$.normalized").value("cja 0 0 1"))
                .andExpect(jsonPath("$.results[0].product.sku").value("CJA-001"))
                .andExpect(jsonPath("$.results[0].score").value(1.0))
                .andExpect(jsonPath("$.results[0].matchedBy").value("SKU"));
    }

    @Test
    void search_byName() throws Exception {
        mvc.perform(get("/api/v1/products/search").param("q", "cajas tipo a").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].product.sku").value("CJA-001"))
                .andExpect(jsonPath("$.results[0].matchedBy").value("NAME"))
                .andExpect(jsonPath("$.results", hasSize(lessThanOrEqualTo(5))))
                .andExpect(jsonPath("$.results[*].score", everyItem(lessThanOrEqualTo(1.0))))
                .andExpect(jsonPath("$.results[*].score", everyItem(greaterThanOrEqualTo(0.0))));
    }

    @Test
    void search_byLocation() throws Exception {
        mvc.perform(get("/api/v1/products/search").param("q", "a uno tres").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].matchedBy").value("LOCATION"))
                .andExpect(jsonPath("$.results[0].product.locationCode").value("A-01-03"))
                .andExpect(jsonPath("$.results[1].product.locationCode").value("A-01-03"))
                .andExpect(jsonPath("$.results[*].product.sku", hasItems("CJA-001", "SPR-001")));
    }

    @Test
    void search_respectsLimit() throws Exception {
        mvc.perform(get("/api/v1/products/search?q=cajas&limit=2").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(2)));
    }

    @Test
    void search_noMatches_isEmptyListNot404() throws Exception {
        mvc.perform(get("/api/v1/products/search?q=helicoptero").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(0)));
    }

    @Test
    void search_limitOutOfRange_is400() throws Exception {
        mvc.perform(get("/api/v1/products/search?q=cajas&limit=11").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void search_missingQuery_is400() throws Exception {
        mvc.perform(get("/api/v1/products/search").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Falta el parámetro obligatorio 'q'"));
    }
}
