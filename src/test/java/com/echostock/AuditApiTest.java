package com.echostock;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class AuditApiTest extends ApiIntegrationTest {

    @Autowired
    private ObjectMapper json;

    private static Map<String, Object> audit(String id, String sku, int system, int counted, String result) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("clientAuditId", id);
        body.put("sku", sku);
        body.put("operatorCode", "OP-07");
        body.put("deviceId", "wear-emulator-01");
        body.put("systemQuantity", system);
        body.put("countedQuantity", counted);
        body.put("result", result);
        body.put("recordedAt", "2026-11-09T18:31:12Z");
        return body;
    }

    private ResultActions postAudit(Object body) throws Exception {
        return mvc.perform(post("/api/v1/audits").header(API_KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)));
    }

    private ResultActions postBatch(Object body) throws Exception {
        return mvc.perform(post("/api/v1/audits/batch").header(API_KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)));
    }

    private long createAudit(String sku, int system, int counted) throws Exception {
        String body = postAudit(audit(UUID.randomUUID().toString(), sku, system, counted, null))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("id").asLong();
    }

    // --- POST /audits -----------------------------------------------------------------------------

    @Test
    void create_thenResend_is201Then200WithSameAudit() throws Exception {
        String id = UUID.randomUUID().toString();
        Map<String, Object> body = audit(id, "CJA-001", 50, 48, "DISCREPANCY");

        String first = postAudit(body)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/audits/")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.clientAuditId").value(id))
                .andExpect(jsonPath("$.sku").value("CJA-001"))
                .andExpect(jsonPath("$.operatorCode").value("OP-07"))
                .andExpect(jsonPath("$.deviceId").value("wear-emulator-01"))
                .andExpect(jsonPath("$.systemQuantity").value(50))
                .andExpect(jsonPath("$.countedQuantity").value(48))
                .andExpect(jsonPath("$.result").value("DISCREPANCY"))
                .andExpect(jsonPath("$.status").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$.difference").value(-2))
                .andExpect(jsonPath("$.recordedAt").value("2026-11-09T18:31:12Z"))
                .andExpect(jsonPath("$.receivedAt", endsWith("Z")))
                .andReturn().getResponse().getContentAsString();

        String second = postAudit(body)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertJsonEquals(first, second);
        mvc.perform(get("/api/v1/audits?sku=CJA-001&size=100").header(API_KEY_HEADER, API_KEY))
                .andExpect(jsonPath("$.content[?(@.clientAuditId == '" + id + "')]", hasSize(1)));
    }

    @Test
    void create_match_isConfirmed() throws Exception {
        postAudit(audit(UUID.randomUUID().toString(), "CJA-001", 50, 50, "MATCH"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("MATCH"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.difference").value(0));
    }

    @Test
    void create_unknownSku_is404() throws Exception {
        postAudit(audit(UUID.randomUUID().toString(), "NOPE-999", 1, 1, "MATCH"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void create_negativeCount_is422() throws Exception {
        postAudit(audit(UUID.randomUUID().toString(), "CJA-001", 50, -1, "DISCREPANCY"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:echostock:problem:validation"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.detail", containsString("countedQuantity")));
    }

    @Test
    void create_incoherentResult_is422() throws Exception {
        postAudit(audit(UUID.randomUUID().toString(), "CJA-001", 50, 48, "MATCH"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("result incoherente")));
    }

    @Test
    void create_missingFields_is422() throws Exception {
        postAudit(Map.of("sku", "CJA-001"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("clientAuditId")))
                .andExpect(jsonPath("$.detail", containsString("recordedAt")));
    }

    @Test
    void create_unknownResultValue_is422() throws Exception {
        postAudit(audit(UUID.randomUUID().toString(), "CJA-001", 50, 50, "OK"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("result")));
    }

    @Test
    void create_malformedJson_is400() throws Exception {
        mvc.perform(post("/api/v1/audits").header(API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    // --- POST /audits/batch ---------------------------------------------------------------------

    @Test
    void batch_acceptsDuplicatesAndRejectsPerItem() throws Exception {
        String existing = UUID.randomUUID().toString();
        postAudit(audit(existing, "CJA-001", 50, 50, "MATCH")).andExpect(status().isCreated());

        String fresh = UUID.randomUUID().toString();
        String repeatedInBatch = UUID.randomUUID().toString();
        String badSku = UUID.randomUUID().toString();
        String invalid = UUID.randomUUID().toString();

        postBatch(Map.of("audits", List.of(
                audit(fresh, "FLM-001", 64, 60, "DISCREPANCY"),
                audit(existing, "CJA-001", 50, 50, "MATCH"),
                audit(repeatedInBatch, "PLT-001", 36, 36, "MATCH"),
                audit(repeatedInBatch, "PLT-001", 36, 36, "MATCH"),
                audit(badSku, "NOPE-999", 1, 1, "MATCH"),
                audit(invalid, "CJA-001", 50, -5, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted", contains(fresh, repeatedInBatch)))
                .andExpect(jsonPath("$.duplicates", contains(existing, repeatedInBatch)))
                .andExpect(jsonPath("$.rejected", hasSize(2)))
                .andExpect(jsonPath("$.rejected[0].clientAuditId").value(badSku))
                .andExpect(jsonPath("$.rejected[0].reason").value("SKU no encontrado"))
                .andExpect(jsonPath("$.rejected[1].clientAuditId").value(invalid))
                .andExpect(jsonPath("$.rejected[1].reason", containsString("countedQuantity")));
    }

    @Test
    void batch_resendingWholeBatch_marksAllAsDuplicates() throws Exception {
        String a = UUID.randomUUID().toString();
        String b = UUID.randomUUID().toString();
        Map<String, Object> body = Map.of("audits", List.of(
                audit(a, "GNT-001", 400, 400, "MATCH"), audit(b, "GNT-002", 350, 349, "DISCREPANCY")));

        postBatch(body).andExpect(jsonPath("$.accepted", contains(a, b)));
        postBatch(body)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted", empty()))
                .andExpect(jsonPath("$.duplicates", contains(a, b)))
                .andExpect(jsonPath("$.rejected", empty()));
    }

    @Test
    void batch_emptyList_isOkWithEmptyLists() throws Exception {
        postBatch(Map.of("audits", List.of()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted", empty()))
                .andExpect(jsonPath("$.duplicates", empty()))
                .andExpect(jsonPath("$.rejected", empty()));
    }

    @Test
    void batch_over200_is422() throws Exception {
        List<Map<String, Object>> audits = new ArrayList<>();
        for (int i = 0; i < 201; i++) {
            audits.add(audit(UUID.randomUUID().toString(), "CJA-001", 50, 50, "MATCH"));
        }
        postBatch(Map.of("audits", audits))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("200")));
    }

    @Test
    void batch_exactly200_isAccepted() throws Exception {
        List<Map<String, Object>> audits = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            audits.add(audit(UUID.randomUUID().toString(), "ESQ-001", 900, 900, "MATCH"));
        }
        postBatch(Map.of("audits", audits))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted", hasSize(200)));
    }

    @Test
    void batch_missingAudits_is422() throws Exception {
        postBatch(Map.of()).andExpect(status().isUnprocessableEntity());
    }

    // --- GET /audits ----------------------------------------------------------------------------

    @Test
    void list_filtersAndSortsByRecordedAtDesc() throws Exception {
        Map<String, Object> older = audit(UUID.randomUUID().toString(), "BAT-001", 12, 10, null);
        older.put("recordedAt", "2026-01-01T08:00:00Z");
        Map<String, Object> newer = audit(UUID.randomUUID().toString(), "BAT-001", 12, 12, null);
        newer.put("recordedAt", "2026-01-02T08:00:00Z");
        postAudit(older).andExpect(status().isCreated());
        postAudit(newer).andExpect(status().isCreated());

        mvc.perform(get("/api/v1/audits?sku=BAT-001").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].recordedAt").value("2026-01-02T08:00:00Z"))
                .andExpect(jsonPath("$.content[1].recordedAt").value("2026-01-01T08:00:00Z"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));

        mvc.perform(get("/api/v1/audits?sku=BAT-001&status=PENDING_REVIEW").header(API_KEY_HEADER, API_KEY))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].difference").value(-2));

        mvc.perform(get("/api/v1/audits").param("sku", "BAT-001")
                        .param("from", "2026-01-02T00:00:00Z").param("to", "2026-01-03T00:00:00Z")
                        .header(API_KEY_HEADER, API_KEY))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"));
    }

    @Test
    void list_byStatus_onlyReturnsThatStatus() throws Exception {
        createAudit("CNT-001", 240, 239);
        mvc.perform(get("/api/v1/audits?status=PENDING_REVIEW&size=100").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].status", everyItem(is("PENDING_REVIEW"))));
    }

    @Test
    void list_invalidStatus_is400() throws Exception {
        mvc.perform(get("/api/v1/audits?status=FOO").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void list_invalidDate_is400() throws Exception {
        mvc.perform(get("/api/v1/audits?from=ayer").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_fromAfterTo_is422() throws Exception {
        mvc.perform(get("/api/v1/audits?from=2026-02-01T00:00:00Z&to=2026-01-01T00:00:00Z")
                        .header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void getById() throws Exception {
        long id = createAudit("CJA-001", 50, 50);
        mvc.perform(get("/api/v1/audits/" + id).header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
        mvc.perform(get("/api/v1/audits/999999").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isNotFound());
    }

    // --- approve / reject -----------------------------------------------------------------------

    @Test
    void approve_appliesCountedQuantityToProduct() throws Exception {
        long id = createAudit("CJA-002", 120, 118);

        mvc.perform(post("/api/v1/audits/" + id + "/approve").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.countedQuantity").value(118));

        mvc.perform(get("/api/v1/products/CJA-002").header(API_KEY_HEADER, API_KEY))
                .andExpect(jsonPath("$.systemQuantity").value(118));

        // Ya no está PENDING_REVIEW
        mvc.perform(post("/api/v1/audits/" + id + "/approve").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:echostock:problem:conflict"))
                .andExpect(jsonPath("$.status").value(409));
        mvc.perform(post("/api/v1/audits/" + id + "/reject").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isConflict());
    }

    @Test
    void reject_keepsStock() throws Exception {
        long id = createAudit("FLM-003", 25, 20);

        mvc.perform(post("/api/v1/audits/" + id + "/reject").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        mvc.perform(get("/api/v1/products/FLM-003").header(API_KEY_HEADER, API_KEY))
                .andExpect(jsonPath("$.systemQuantity").value(25));
        mvc.perform(post("/api/v1/audits/" + id + "/approve").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isConflict());
    }

    @Test
    void approve_confirmedAudit_is409() throws Exception {
        long id = createAudit("CJA-001", 50, 50);

        mvc.perform(post("/api/v1/audits/" + id + "/approve").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isConflict());
    }

    @Test
    void approve_unknownAudit_is404() throws Exception {
        mvc.perform(post("/api/v1/audits/999999/approve").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.instance").value("/api/v1/audits/999999/approve"));
    }

    private void assertJsonEquals(String expected, String actual) throws Exception {
        JsonNode a = json.readTree(expected);
        JsonNode b = json.readTree(actual);
        org.assertj.core.api.Assertions.assertThat(b).isEqualTo(a);
    }
}
