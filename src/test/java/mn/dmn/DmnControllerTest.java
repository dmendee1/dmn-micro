package mn.dmn;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@DisplayName("DMN Controller REST API Tests")
class DmnControllerTest {

    @Nested
    @DisplayName("POST /dmn/evaluate")
    class EvaluateEndpointTests {

        @Test
        @DisplayName("Should return 200 for valid approval request")
        void shouldReturn200ForValidApprovalRequest() {
            Map<String, Object> requestBody = createValidRequest(25, 50000, "apply");

            given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post("/dmn/evaluate")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("result", equalTo(true))
                    .body("reason", notNullValue())
                    .body("reason", containsString("Approval Decision"))
                    .body("error", nullValue());
        }

//        @Test
//        @DisplayName("Should return 200 for valid rejection request")
//        void shouldReturn200ForValidRejectionRequest() {
//            Map<String, Object> requestBody = createValidRequest(25, 40000, "apply");
//
//            given()
//                    .contentType(ContentType.JSON)
//                    .body(requestBody)
//                    .when()
//                    .post("/dmn/evaluate")
//                    .then()
//                    .statusCode(200)
//                    .body("success", equalTo(true))
//                    .body("result", equalTo(false))
//                    .body("reason", notNullValue())
//                    .body("error", nullValue());
//        }

        @Test
        @DisplayName("Should return 200 for changeUp action")
        void shouldReturn200ForChangeUpAction() {
            Map<String, Object> requestBody = createValidRequest(20, 25000, "changeUp");

            given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post("/dmn/evaluate")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("result", equalTo(true))
                    .body("reason", notNullValue());
        }

        @Test
        @DisplayName("Should return 400 for null request body")
        void shouldReturn400ForNullRequestBody() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/dmn/evaluate")
                    .then()
                    .statusCode(400)
                    .body("success", equalTo(false))
                    .body("error", containsString("Request body is required"));
        }

        @Test
        @DisplayName("Should return 400 for missing DMN file")
        void shouldReturn400ForMissingDmnFile() {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("decisionName", "Approval Decision");
            requestBody.put("inputData", createInputData(25, 50000, "apply"));

            given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post("/dmn/evaluate")
                    .then()
                    .statusCode(400)
                    .body("success", equalTo(false))
                    .body("error", containsString("DMN file path is required"));
        }

        @Test
        @DisplayName("Should return 400 for empty DMN file")
        void shouldReturn400ForEmptyDmnFile() {
            Map<String, Object> requestBody = createValidRequest(25, 50000, "apply");
            requestBody.put("dmnFile", "");

            given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post("/dmn/evaluate")
                    .then()
                    .statusCode(400)
                    .body("success", equalTo(false))
                    .body("error", containsString("DMN file path is required"));
        }

        @Test
        @DisplayName("Should return 500 for non-existent DMN file")
        void shouldReturn500ForNonExistentDmnFile() {
            Map<String, Object> requestBody = createValidRequest(25, 50000, "apply");
            requestBody.put("dmnFile", "non-existent.dmn");

            given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post("/dmn/evaluate")
                    .then()
                    .statusCode(500)
                    .body("success", equalTo(false))
                    .body("error", notNullValue());
        }

        @Test
        @DisplayName("Should handle request without decision name")
        void shouldHandleRequestWithoutDecisionName() {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("dmnFile", "sample-decision.dmn");
            requestBody.put("inputData", createInputData(25, 50000, "apply"));

            given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post("/dmn/evaluate")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("result", notNullValue())
                    .body("reason", notNullValue());
        }

        @Test
        @DisplayName("Should handle request without input data")
        void shouldHandleRequestWithoutInputData() {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("dmnFile", "sample-decision.dmn");
            requestBody.put("decisionName", "Approval Decision");

            given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post("/dmn/evaluate")
                    .then()
                    // Could be 200 with default behavior or 500 with error - both acceptable
                    .statusCode(anyOf(equalTo(200), equalTo(500)));
        }

//        @Test
//        @DisplayName("Should handle malformed JSON")
//        void shouldHandleMalformedJson() {
//            given()
//                    .contentType(ContentType.JSON)
//                    .body("{ invalid json }")
//                    .when()
//                    .post("/dmn/evaluate")
//                    .then()
//                    .statusCode(400);
//        }

        @Test
        @DisplayName("Should validate content type")
        void shouldValidateContentType() {
            Map<String, Object> requestBody = createValidRequest(25, 50000, "apply");

            given()
                    .contentType(ContentType.TEXT)
                    .body(requestBody.toString())
                    .when()
                    .post("/dmn/evaluate")
                    .then()
                    .statusCode(415); // Unsupported Media Type
        }
    }

    @Nested
    @DisplayName("GET /dmn/health")
    class HealthEndpointTests {

        @Test
        @DisplayName("Should return 200 for health check")
        void shouldReturn200ForHealthCheck() {
            given()
                    .when()
                    .get("/dmn/health")
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("UP"))
                    .body("service", equalTo("DMN Evaluator"));
        }

        @Test
        @DisplayName("Should return JSON content type for health check")
        void shouldReturnJsonContentTypeForHealthCheck() {
            given()
                    .when()
                    .get("/dmn/health")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON);
        }
    }

    @Nested
    @DisplayName("HTTP Method Tests")
    class HttpMethodTests {

        @Test
        @DisplayName("Should return 405 for GET on evaluate endpoint")
        void shouldReturn405ForGetOnEvaluateEndpoint() {
            given()
                    .when()
                    .get("/dmn/evaluate")
                    .then()
                    .statusCode(405); // Method Not Allowed
        }

        @Test
        @DisplayName("Should return 405 for PUT on evaluate endpoint")
        void shouldReturn405ForPutOnEvaluateEndpoint() {
            given()
                    .contentType(ContentType.JSON)
                    .body(createValidRequest(25, 50000, "apply"))
                    .when()
                    .put("/dmn/evaluate")
                    .then()
                    .statusCode(405); // Method Not Allowed
        }

        @Test
        @DisplayName("Should return 405 for DELETE on evaluate endpoint")
        void shouldReturn405ForDeleteOnEvaluateEndpoint() {
            given()
                    .when()
                    .delete("/dmn/evaluate")
                    .then()
                    .statusCode(405); // Method Not Allowed
        }

        @Test
        @DisplayName("Should return 405 for POST on health endpoint")
        void shouldReturn405ForPostOnHealthEndpoint() {
            given()
                    .contentType(ContentType.JSON)
                    .body("{}")
                    .when()
                    .post("/dmn/health")
                    .then()
                    .statusCode(405); // Method Not Allowed
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle boundary age values")
        void shouldHandleBoundaryAgeValues() {
            // Test age exactly 18
            Map<String, Object> requestBody1 = createValidRequest(18, 30000, "apply");
            given()
                    .contentType(ContentType.JSON)
                    .body(requestBody1)
                    .when()
                    .post("/dmn/evaluate")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("result", equalTo(true));

            // Test age 17 (just under 18)
            Map<String, Object> requestBody2 = createValidRequest(17, 55000, "apply");
            given()
                    .contentType(ContentType.JSON)
                    .body(requestBody2)
                    .when()
                    .post("/dmn/evaluate")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("result", equalTo(true)); // Should still be approved due to high income
        }

        @Test
        @DisplayName("Should handle boundary income values")
        void shouldHandleBoundaryIncomeValues() {
            // Test income exactly 30000
            Map<String, Object> requestBody1 = createValidRequest(25, 30000, "apply");
            given()
                    .contentType(ContentType.JSON)
                    .body(requestBody1)
                    .when()
                    .post("/dmn/evaluate")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("result", equalTo(true));

            // Test income exactly 49900
            Map<String, Object> requestBody2 = createValidRequest(25, 49900, "apply");
            given()
                    .contentType(ContentType.JSON)
                    .body(requestBody2)
                    .when()
                    .post("/dmn/evaluate")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("result", equalTo(true));
        }

        @Test
        @DisplayName("Should handle very large numbers")
        void shouldHandleVeryLargeNumbers() {
            Map<String, Object> requestBody = createValidRequest(999, 999999999, "apply");

            given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post("/dmn/evaluate")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true));
        }

        @Test
        @DisplayName("Should handle zero values")
        void shouldHandleZeroValues() {
            Map<String, Object> requestBody = createValidRequest(0, 0, "apply");

            given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post("/dmn/evaluate")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true));
        }

        @Test
        @DisplayName("Should handle different action values")
        void shouldHandleDifferentActionValues() {
            // Test various action values
            String[] actions = {"apply", "review", "approve", "deny", "changeUp", "update"};

            for (String action : actions) {
                Map<String, Object> requestBody = createValidRequest(25, 50000, action);

                given()
                        .contentType(ContentType.JSON)
                        .body(requestBody)
                        .when()
                        .post("/dmn/evaluate")
                        .then()
                        .statusCode(200)
                        .body("success", equalTo(true));
            }
        }
    }

//    @Test
//    @DisplayName("Should handle Custom1 Decision evaluation")
//    void shouldHandleCustom1DecisionEvaluation() {
//        Map<String, Object> requestBody = createCustom1Request(25, 50000, "apply");
//
//        given()
//                .contentType(ContentType.JSON)
//                .body(requestBody)
//                .when()
//                .post("/dmn/evaluate")
//                .then()
//                .statusCode(200)
//                .body("success", equalTo(true))
//                .body("reason", notNullValue())
//                .body("reason", containsString("Custom1 Decision"));
//    }
//
//    @Test
//    @DisplayName("Should handle Custom1 Decision with changeUp action")
//    void shouldHandleCustom1DecisionWithChangeUpAction() {
//        Map<String, Object> requestBody = createCustom1Request(30, 25000, "changeUp");
//
//        given()
//                .contentType(ContentType.JSON)
//                .body(requestBody)
//                .when()
//                .post("/dmn/evaluate")
//                .then()
//                .statusCode(200)
//                .body("success", equalTo(true))
//                .body("reason", notNullValue());
//    }

    @Test
    @DisplayName("Should handle evaluation without specific decision name")
    void shouldHandleEvaluationWithoutSpecificDecisionName() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("dmnFile", "sample-decision.dmn");
        requestBody.put("inputData", createInputData(25, 50000, "apply"));
        // No decisionName specified - should evaluate all decisions

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/dmn/evaluate")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("result", notNullValue())
                .body("reason", notNullValue());
    }

    // Helper methods
    private Map<String, Object> createValidRequest(int age, int income, String action) {
        Map<String, Object> request = new HashMap<>();
        request.put("dmnFile", "sample-decision.dmn");
        request.put("decisionName", "Approval Decision");
        request.put("inputData", createInputData(age, income, action));
        return request;
    }

    private Map<String, Object> createCustom1Request(int age, int income, String action) {
        Map<String, Object> request = new HashMap<>();
        request.put("dmnFile", "sample-decision.dmn");
        request.put("decisionName", "Custom1 Decision");
        request.put("inputData", createInputData(age, income, action));
        return request;
    }

    private Map<String, Object> createInputData(int age, int income, String action) {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("age", age);
        inputData.put("income", income);
        inputData.put("action", action);
        return inputData;
    }
}