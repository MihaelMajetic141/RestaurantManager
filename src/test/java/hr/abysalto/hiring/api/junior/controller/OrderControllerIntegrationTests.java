package hr.abysalto.hiring.api.junior.controller;

import com.fasterxml.jackson.databind.JsonNode;
import hr.abysalto.hiring.api.junior.data.dto.request.LoginRequest;
import hr.abysalto.hiring.api.junior.data.dto.request.RegistrationRequest;
import hr.abysalto.hiring.api.junior.data.dto.request.AddressRequest;
import hr.abysalto.hiring.api.junior.data.dto.request.OrderItemRequest;
import hr.abysalto.hiring.api.junior.data.dto.request.OrderRequest;
import hr.abysalto.hiring.api.junior.data.dto.response.JwtResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIntegrationTests {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    private String token;
    private static boolean dataInitialized = false;
    private static boolean testUserRegistered = false;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/order";
    }

    private String authUrl() {
        return "http://localhost:" + port + "/api/auth";
    }

    private String initDataUrl() {
        return "http://localhost:" + port + "/init-data";
    }

    @BeforeEach
    void setUp() {
        if (!dataInitialized) {
            ResponseEntity<Void> initResp = restTemplate.postForEntity(initDataUrl() + "/", null, Void.class);
            assertThat(initResp.getStatusCode()).isIn(HttpStatus.NO_CONTENT, HttpStatus.OK);
            dataInitialized = true;
        }
        ensureTestUserAndLogin();
    }

    private void ensureTestUserAndLogin() {
        if (!testUserRegistered) {
            RegistrationRequest reg = new RegistrationRequest("testuser", "testuser@example.com", "testpassword");
            HttpEntity<RegistrationRequest> regEntity = new HttpEntity<>(reg, new HttpHeaders());
            ResponseEntity<String> regResp = restTemplate.postForEntity(authUrl() + "/register", regEntity, String.class);
            assertThat(regResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(regResp.getBody()).isEqualTo("User registered successfully!");
            testUserRegistered = true;
        }
        LoginRequest loginRequest = new LoginRequest("testuser", "testpassword");
        HttpEntity<LoginRequest> loginEntity = new HttpEntity<>(loginRequest, new HttpHeaders());
        ResponseEntity<JwtResponse> loginResponse = restTemplate.postForEntity(
                authUrl() + "/login", loginEntity, JwtResponse.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        this.token = loginResponse.getBody().getAccessToken();
        assertThat(this.token).isNotEmpty();
    }

    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    private OrderRequest buildOrderRequest(Long buyerId, String orderStatus, String paymentOption) {
        AddressRequest address = new AddressRequest("Test City", "Test Street", "1");
        OrderItemRequest orderItem = new OrderItemRequest(null, 1L, (short) 2);
        return new OrderRequest(
                buyerId,
                orderStatus,
                "2025-02-04T12:00:00",
                paymentOption,
                address,
                "+1234567890",
                "Order note",
                List.of(orderItem),
                "USD",
                null
        );
    }

    @Test
    void createAndGetAllOrders() {
        HttpHeaders authHeaders = getAuthHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        OrderRequest orderRequest = buildOrderRequest(1L, "WAITING_FOR_CONFIRMATION", "CASH");
        HttpEntity<OrderRequest> createEntity = new HttpEntity<>(orderRequest, authHeaders);

        ResponseEntity<JsonNode> createResponse = restTemplate.postForEntity(
                baseUrl() + "/create", createEntity, JsonNode.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.has("id")).isTrue();
        Long createdId = created.get("id").asLong();

        HttpEntity<Void> getAllRequest = new HttpEntity<>(authHeaders);
        ResponseEntity<List<LinkedHashMap<String, Object>>> getAllResponse = restTemplate.exchange(
                baseUrl() + "/getAll", HttpMethod.GET, getAllRequest,
                new ParameterizedTypeReference<>() {});
        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<LinkedHashMap<String, Object>> allBody = getAllResponse.getBody();
        assertThat(allBody).isNotNull();
        assertThat(allBody.size()).isGreaterThanOrEqualTo(1);

        boolean found = false;
        for (LinkedHashMap<String, Object> orderMap : allBody) {
            Object idObj = orderMap.get("id");
            if (idObj != null && Long.valueOf(idObj.toString()).equals(createdId)) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
    }

    @Test
    void createAndGetOrder() {
        HttpHeaders authHeaders = getAuthHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        OrderRequest orderRequest = buildOrderRequest(2L, "PREPARING", "CARD_UPFRONT");
        HttpEntity<OrderRequest> createEntity = new HttpEntity<>(orderRequest, authHeaders);

        ResponseEntity<JsonNode> createResp = restTemplate.postForEntity(
                baseUrl() + "/create", createEntity, JsonNode.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode created = createResp.getBody();
        assertThat(created).isNotNull();
        Long createdId = created.get("id").asLong();

        HttpEntity<Void> getEntity = new HttpEntity<>(authHeaders);
        ResponseEntity<JsonNode> getResp = restTemplate.exchange(
                baseUrl() + "/get/" + createdId, HttpMethod.GET, getEntity, JsonNode.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = getResp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("orderStatus").asText()).isEqualTo("PREPARING");
        assertThat(body.get("paymentOption").asText()).isEqualTo("CARD_UPFRONT");
    }

    @Test
    void updateOrder() {
        HttpHeaders authHeaders = getAuthHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        OrderRequest orderRequest = buildOrderRequest(3L, "WAITING_FOR_CONFIRMATION", "CASH");
        HttpEntity<OrderRequest> createEntity = new HttpEntity<>(orderRequest, authHeaders);

        ResponseEntity<JsonNode> createResponse = restTemplate.postForEntity(
                baseUrl() + "/create", createEntity, JsonNode.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long createdId = createResponse.getBody().get("id").asLong();

        OrderRequest updateRequest = buildOrderRequest(3L, "DONE", "CARD_ON_DELIVERY");
        updateRequest.setOrderNote("Updated note");
        HttpEntity<OrderRequest> updateEntity = new HttpEntity<>(updateRequest, authHeaders);

        ResponseEntity<JsonNode> updateResponse = restTemplate.exchange(
                baseUrl() + "/update/" + createdId, HttpMethod.PUT, updateEntity, JsonNode.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().get("orderStatus").asText()).isEqualTo("DONE");
        assertThat(updateResponse.getBody().get("orderNote").asText()).isEqualTo("Updated note");

        HttpEntity<Void> getEntity = new HttpEntity<>(authHeaders);
        ResponseEntity<JsonNode> getResponse = restTemplate.exchange(
                baseUrl() + "/get/" + createdId, HttpMethod.GET, getEntity, JsonNode.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().get("orderStatus").asText()).isEqualTo("DONE");
    }

    @Test
    void patchOrder_mergePatch_updatesField() {
        HttpHeaders authHeaders = getAuthHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        OrderRequest orderRequest = buildOrderRequest(4L, "WAITING_FOR_CONFIRMATION", "CASH");
        HttpEntity<OrderRequest> createEntity = new HttpEntity<>(orderRequest, authHeaders);

        ResponseEntity<JsonNode> createResponse = restTemplate.postForEntity(
                baseUrl() + "/create", createEntity, JsonNode.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long createdId = createResponse.getBody().get("id").asLong();

        String patchJson = "{\"orderNote\":\"Patched note\"}";
        HttpHeaders patchHeaders = new HttpHeaders(authHeaders);
        patchHeaders.setContentType(MediaType.valueOf("application/merge-patch+json"));
        HttpEntity<String> patchReq = new HttpEntity<>(patchJson, patchHeaders);

        ResponseEntity<JsonNode> patchResp = restTemplate.exchange(
                baseUrl() + "/patch/" + createdId, HttpMethod.PATCH, patchReq, JsonNode.class);

        assertThat(patchResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode patched = patchResp.getBody();
        assertThat(patched).isNotNull();
        assertThat(patched.get("orderNote").asText()).isEqualTo("Patched note");
    }

    @Test
    void deleteOrder_returnsNoContent() {
        HttpHeaders authHeaders = getAuthHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);

        OrderRequest orderRequest = buildOrderRequest(5L, "WAITING_FOR_CONFIRMATION", "CASH");
        HttpEntity<OrderRequest> createEntity = new HttpEntity<>(orderRequest, authHeaders);

        ResponseEntity<JsonNode> createResp = restTemplate.postForEntity(
                baseUrl() + "/create", createEntity, JsonNode.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long createdId = createResp.getBody().get("id").asLong();

        HttpEntity<Void> deleteEntity = new HttpEntity<>(authHeaders);
        ResponseEntity<Void> deleteResp = restTemplate.exchange(
                baseUrl() + "/delete/" + createdId, HttpMethod.DELETE, deleteEntity, Void.class);
        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        HttpEntity<Void> getEntity = new HttpEntity<>(authHeaders);
        ResponseEntity<JsonNode> getResp = restTemplate.exchange(
                baseUrl() + "/get/" + createdId, HttpMethod.GET, getEntity, JsonNode.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
