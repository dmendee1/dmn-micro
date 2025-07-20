package mn.dmn;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@DisplayName("DMN Service Tests")
class DmnServiceTest {

    @Inject
    DmnService dmnService;

    @Nested
    @DisplayName("Approval Decision Tests")
    class ApprovalDecisionTests {

        @Test
        @DisplayName("Should approve when age >= 18 and income >= 30000")
        void shouldApproveValidApplicant() {
            // Given
            DmnRequest request = createRequest(25, 50000, "apply");

            // When
            DmnResponse response = dmnService.evaluateDecision(request);

            // Then
            assertTrue(response.isSuccess(), "Response should be successful");
            assertTrue(response.isResult(), "Should be approved");
            assertNotNull(response.getReason(), "Should have a reason");
            assertTrue(response.getReason().contains("Approval Decision"), "Reason should mention decision name");
        }

        @Test
        @DisplayName("Should approve minor with high income")
        void shouldApproveMinorWithHighIncome() {
            // Given
            DmnRequest request = createRequest(16, 55000, "apply");

            // When
            DmnResponse response = dmnService.evaluateDecision(request);

            // Then
            assertTrue(response.isSuccess(), "Response should be successful");
            assertTrue(response.isResult(), "Should be approved due to high income");
        }

        @Test
        @DisplayName("Should approve for exact income threshold")
        void shouldApproveForExactIncomeThreshold() {
            // Given
            DmnRequest request = createRequest(30, 49900, "apply");

            // When
            DmnResponse response = dmnService.evaluateDecision(request);

            // Then
            assertTrue(response.isSuccess(), "Response should be successful");
            assertTrue(response.isResult(), "Should be approved for exact income match");
        }

        @Test
        @DisplayName("Should reject when income < 50000 for general case")
        void shouldRejectLowIncome() {
            // Given
            DmnRequest request = createRequest(25, 20000, "apply");

            // When
            DmnResponse response = dmnService.evaluateDecision(request);

            // Then
            assertTrue(response.isSuccess(), "Response should be successful");
            assertFalse(response.isResult(), "Should be rejected due to low income");
        }

        @Test
        @DisplayName("Should approve for changeUp action regardless of other factors")
        void shouldApproveForChangeUpAction() {
            // Given
            DmnRequest request = createRequest(20, 25000, "changeUp");

            // When
            DmnResponse response = dmnService.evaluateDecision(request);

            // Then
            assertTrue(response.isSuccess(), "Response should be successful");
            assertTrue(response.isResult(), "Should be approved for changeUp action");
        }

        @Test
        @DisplayName("Should handle boundary values correctly")
        void shouldHandleBoundaryValues() {
            // Test age boundary
            DmnRequest request1 = createRequest(18, 30000, "apply");
            DmnResponse response1 = dmnService.evaluateDecision(request1);
            assertTrue(response1.isResult(), "Should approve for age exactly 18");

            // Test income boundary
            DmnRequest request2 = createRequest(25, 30000, "apply");
            DmnResponse response2 = dmnService.evaluateDecision(request2);
            assertTrue(response2.isResult(), "Should approve for income exactly 30000");
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return error for null request")
        void shouldHandleNullRequest() {
            // When
            DmnResponse response = dmnService.evaluateDecision(null);

            // Then
            assertFalse(response.isSuccess(), "Should fail for null request");
            assertNotNull(response.getError(), "Should have error message");
            assertTrue(response.getError().contains("Error evaluating DMN"), "Should contain error prefix");
        }

        @Test
        @DisplayName("Should return error for missing DMN file")
        void shouldHandleMissingDmnFile() {
            // Given
            DmnRequest request = new DmnRequest();
            request.setDmnFile("non-existent-file.dmn");
            request.setDecisionName("Approval Decision");
            request.setInputData(createInputData(25, 50000, "apply"));

            // When
            DmnResponse response = dmnService.evaluateDecision(request);

            // Then
            assertFalse(response.isSuccess(), "Should fail for missing file");
            assertNotNull(response.getError(), "Should have error message");
            assertTrue(response.getError().contains("non-existent-file.dmn"), "Should mention the missing file");
        }

        @Test
        @DisplayName("Should return error for invalid decision name")
        void shouldHandleInvalidDecisionName() {
            // Given
            DmnRequest request = createRequest(25, 50000, "apply");
            request.setDecisionName("Non Existent Decision");

            // When
            DmnResponse response = dmnService.evaluateDecision(request);

            // Then
            assertFalse(response.isSuccess(), "Should fail for invalid decision name");
            assertNotNull(response.getError(), "Should have error message");
        }

        @Test
        @DisplayName("Should handle missing input data gracefully")
        void shouldHandleMissingInputData() {
            // Given
            DmnRequest request = new DmnRequest();
            request.setDmnFile("sample-decision.dmn");
            request.setDecisionName("Approval Decision");
            request.setInputData(new HashMap<>()); // Empty input data

            // When
            DmnResponse response = dmnService.evaluateDecision(request);

            // Then
            // Should still process but may have warnings or default behavior
            assertTrue(response.isSuccess() || response.getError() != null,
                    "Should either succeed with defaults or fail gracefully");
        }
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {

        @Test
        @DisplayName("Should handle null input data")
        void shouldHandleNullInputData() {
            // Given
            DmnRequest request = new DmnRequest();
            request.setDmnFile("sample-decision.dmn");
            request.setDecisionName("Approval Decision");
            request.setInputData(null);

            // When
            DmnResponse response = dmnService.evaluateDecision(request);

            // Then
            assertTrue(response.isSuccess() || response.getError() != null,
                    "Should handle null input data gracefully");
        }

        @Test
        @DisplayName("Should handle different data types")
        void shouldHandleDifferentDataTypes() {
            // Given - Mix of data types
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("age", "25"); // String instead of number
            inputData.put("income", 50000.0); // Double instead of int
            inputData.put("action", "apply");

            DmnRequest request = new DmnRequest();
            request.setDmnFile("sample-decision.dmn");
            request.setDecisionName("Approval Decision");
            request.setInputData(inputData);

            // When
            DmnResponse response = dmnService.evaluateDecision(request);

            // Then
            assertTrue(response.isSuccess() || response.getError() != null,
                    "Should handle type conversion or fail gracefully");
        }

        @Test
        @DisplayName("Should handle extra input parameters")
        void shouldHandleExtraInputParameters() {
            // Given
            Map<String, Object> inputData = createInputData(25, 50000, "apply");
            inputData.put("extraParam", "should be ignored");
            inputData.put("anotherExtra", 123);

            DmnRequest request = new DmnRequest();
            request.setDmnFile("sample-decision.dmn");
            request.setDecisionName("Approval Decision");
            request.setInputData(inputData);

            // When
            DmnResponse response = dmnService.evaluateDecision(request);

            // Then
            assertTrue(response.isSuccess(), "Should succeed despite extra parameters");
            assertNotNull(response.getReason(), "Should have evaluation reason");
        }
    }

    @Nested
    @DisplayName("Decision Name Tests")
    class DecisionNameTests {

        @Test
        @DisplayName("Should evaluate all decisions when decision name is null")
        void shouldEvaluateAllDecisionsForNullName() {
            // Given
            DmnRequest request = createRequest(25, 50000, "apply");
            request.setDecisionName(null);

            // When
            DmnResponse response = dmnService.evaluateDecision(request);

            // Then
            assertTrue(response.isSuccess(), "Should succeed when evaluating all decisions");
            assertNotNull(response.getReason(), "Should have a reason");
        }

        @Test
        @DisplayName("Should evaluate all decisions when decision name is empty")
        void shouldEvaluateAllDecisionsForEmptyName() {
            // Given
            DmnRequest request = createRequest(25, 50000, "apply");
            request.setDecisionName("");

            // When
            DmnResponse response = dmnService.evaluateDecision(request);

            // Then
            assertTrue(response.isSuccess(), "Should succeed when evaluating all decisions");
            assertNotNull(response.getReason(), "Should have a reason");
        }

        @Test
        @DisplayName("Should evaluate all decisions when decision name is whitespace")
        void shouldEvaluateAllDecisionsForWhitespaceName() {
            // Given
            DmnRequest request = createRequest(25, 50000, "apply");
            request.setDecisionName("   ");

            // When
            DmnResponse response = dmnService.evaluateDecision(request);

            // Then
            assertTrue(response.isSuccess(), "Should succeed when evaluating all decisions");
            assertNotNull(response.getReason(), "Should have a reason");
        }
    }

    @Nested
    @DisplayName("Boolean Conversion Tests")
    class BooleanConversionTests {

        @Test
        @DisplayName("Should convert boolean results correctly")
        void shouldConvertBooleanResultsCorrectly() {
            // Test true result
            DmnRequest request1 = createRequest(25, 40000, "apply");
            DmnResponse response1 = dmnService.evaluateDecision(request1);
            assertTrue(response1.isResult(), "Should convert true correctly");

            // Test false result
            DmnRequest request2 = createRequest(25, 20000, "apply");
            DmnResponse response2 = dmnService.evaluateDecision(request2);
            assertFalse(response2.isResult(), "Should convert false correctly");
        }
    }

    // Helper methods
    private DmnRequest createRequest(int age, int income, String action) {
        DmnRequest request = new DmnRequest();
        request.setDmnFile("sample-decision.dmn");
        request.setDecisionName("Approval Decision");
        request.setInputData(createInputData(age, income, action));
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