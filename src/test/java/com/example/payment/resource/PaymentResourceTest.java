package com.example.payment.resource;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;

@QuarkusTest
class PaymentResourceTest {

    @Test
    void shouldCreateAndRetrievePayment() {

        String key = "test-" + UUID.randomUUID();

        String paymentId = given()
                .contentType("application/json")
                .header("Idempotency-Key", key)
                .body(
                        """
                                    {
                                      "amount": 100.50,
                                      "currency": "USD"
                                    }
                                """
                )
                .when()
                .post("/payments")
                .then()
                .statusCode(201)
                .body("status", equalTo("PENDING"))
                .body("currency", equalTo("USD"))
                .extract()
                .path("paymentId");

        given()
                .when()
                .get("/payments/" + paymentId)
                .then()
                .statusCode(200)
                .body("paymentId", equalTo(paymentId))
                .body("status", equalTo("PENDING"));

    }

    @Test
    void shouldReturnExistingPaymentForTheSameKeyAndPayload() {
        String key = "test-" + UUID.randomUUID();

        String payload = """
                {
                  "amount": 100.50,
                  "currency": "USD"
                }
                """;

        String firstPaymentId =
                given()
                        .contentType("application/json")
                        .header("Idempotency-Key", key)
                        .body(payload)
                        .when()
                        .post("/payments")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("paymentId");

        String secondPaymentId =
                given()
                        .contentType("application/json")
                        .header("Idempotency-Key", key)
                        .body(payload)
                        .when()
                        .post("/payments")
                        .then()
                        .statusCode(200)
                        .extract()
                        .path("paymentId");

        assertEquals(firstPaymentId, secondPaymentId);
    }

    @Test
    void shouldRejectDifferentPayloadForSameKey() {

        String key = "test-" + UUID.randomUUID();

        given()
                .contentType("application/json")
                .header("Idempotency-Key", key)
                .body(
                        """
                                {"amount":100.50,"currency":"USD"}
                                """
                )
                .when()
                .post("/payments")
                .then()
                .statusCode(201);

        given()
                .contentType("application/json")
                .header("Idempotency-Key", key)
                .body(
                        """
                                {"amount":200.00,"currency":"USD"}
                                """
                )
                .when()
                .post("/payments")
                .then()
                .statusCode(409);
    }

    @Test
    void shouldReturn404ForUnknownPayment() {

        given()
                .when()
                .get("/payments/" + UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("code", equalTo("PAYMENT_NOT_FOUND"));
    }

    @Test
    void shouldCreateDifferentPaymentsForDifferentKeys() {

        String payload = """
                {"amount":100.50,"currency":"USD"}
                """;

        String firstId =
                given()
                        .contentType("application/json")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .body(payload)
                        .when()
                        .post("/payments")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("paymentId");

        String secondId =
                given()
                        .contentType("application/json")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .body(payload)
                        .when()
                        .post("/payments")
                        .then()
                        .statusCode(201)
                        .extract()
                        .path("paymentId");

        assertNotEquals(firstId, secondId);
    }

    @Test
    void shouldHandleConcurrentRequestWithSameKey() throws Exception {

        String key = "concurrent-" + UUID.randomUUID();

        String payload = """
                {"amount":300.00,"currency":"USD"}
                """;

        int requestCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch countDownLatch = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<Response>> futures = new ArrayList<>();

            for (int index = 0; index < requestCount; index++) {
                futures.add(executor.submit(
                        () -> {
                            countDownLatch.countDown();
                            start.await();

                            return given()
                                    .contentType("application/json")
                                    .header("Idempotency-Key", key)
                                    .body(payload)
                                    .when()
                                    .post("/payments");

                        }
                ));
            }

            assertTrue(countDownLatch.await(10, TimeUnit.SECONDS));

            // Release all requests at approximately the same time
            start.countDown();

            List<Response> responses = new ArrayList<>();

            for (Future<Response> future : futures) {
                responses.add(
                        future.get(20, TimeUnit.SECONDS)
                );
            }

            long createdCount = responses.stream().filter(r -> r.statusCode() == 201).count();
            long reusedCount = responses.stream().filter(r -> r.statusCode() == 200).count();
            Set<String> paymentIds = responses.stream().map(r -> r.jsonPath().getString("paymentId")).collect(Collectors.toSet());

            assertEquals(1, createdCount);
            assertEquals(requestCount - 1, reusedCount);
            assertEquals(1, paymentIds.size());

        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

}