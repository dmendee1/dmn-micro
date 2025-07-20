# Quarkus DMN Evaluator Service

A Quarkus-based REST API service that reads DMN (Decision Model and Notation) files and evaluates business decisions, returning boolean results with reasoning.

## Features

- Read DMN files from classpath or file system
- REST API for decision evaluation
- Returns boolean result with detailed reasoning
- Error handling and validation
- Health check endpoint
- CORS support for frontend integration

## Project Structure

```
src/
├── main/
│   ├── java/com/example/
│   │   ├── controller/DmnController.java
│   │   ├── service/DmnService.java
│   │   └── dto/
│   │       ├── DmnRequest.java
│   │       └── DmnResponse.java
│   └── resources/
│       ├── application.properties
│       └── sample-decision.dmn
└── pom.xml
```

## API Endpoints

### POST /dmn/evaluate
Evaluates a DMN decision and returns true/false with reasoning.

**Request Body:**
```json
{
  "dmnFile": "sample-decision.dmn",
  "decisionName": "Approval Decision",
  "inputData": {
    "age": 25,
    "income": 50000
  }
}
```

**Response:**
```json
{
  "result": true,
  "reason": "Decision 'Approval Decision' evaluated - Result: true",
  "success": true,
  "error": null
}
```

### GET /dmn/health
Health check endpoint.

**Response:**
```json
{
  "status": "UP",
  "service": "DMN Evaluator"
}
```

## Running the Application

### Development Mode
```bash
./mvnw compile quarkus:dev
```

### Production Mode
```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Native Executable
```bash
./mvnw package -Pnative
./target/quarkus-dmn-evaluator-1.0.0-SNAPSHOT-runner
```

## Usage Examples

## Usage Examples

### Example 1: Basic Approval Decision Evaluation
```bash
curl -X POST http://localhost:8080/dmn/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "dmnFile": "sample-decision.dmn",
    "decisionName": "Approval Decision",
    "inputData": {
      "age": 25,
      "income": 50000,
      "action": "apply"
    }
  }'
```

### Example 2: Approval Decision with Different Input
```bash
curl -X POST http://localhost:8080/dmn/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "dmnFile": "sample-decision.dmn",
    "decisionName": "Approval Decision",
    "inputData": {
      "age": 16,
      "income": 20000,
      "action": "review"
    }
  }'
```

### Example 3: Special Action Test for Approval Decision
```bash
curl -X POST http://localhost:8080/dmn/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "dmnFile": "sample-decision.dmn",
    "decisionName": "Approval Decision",
    "inputData": {
      "age": 30,
      "income": 25000,
      "action": "changeUp"
    }
  }'
```

### Example 4: Custom1 Decision Evaluation
```bash
curl -X POST http://localhost:8080/dmn/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "dmnFile": "sample-decision.dmn",
    "decisionName": "Custom1 Decision",
    "inputData": {
      "age": 25,
      "income": 50000,
      "action": "apply"
    }
  }'
```

### Example 5: Custom1 Decision with Age 18 Special Case
```bash
curl -X POST http://localhost:8080/dmn/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "dmnFile": "sample-decision.dmn",
    "decisionName": "Custom1 Decision",
    "inputData": {
      "age": 18,
      "income": 15000,
      "action": "review"
    }
  }'
```

### Example 6: Evaluate All Decisions
```bash
curl -X POST http://localhost:8080/dmn/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "dmnFile": "sample-decision.dmn",
    "inputData": {
      "age": 25,
      "income": 50000,
      "action": "apply"
    }
  }'
```

## Running Tests

### Unit Tests
```bash
./mvnw test
```

### Specific Test Classes
```bash
# Test DMN Service
./mvnw test -Dtest=DmnServiceTest

# Test REST Controller
./mvnw test -Dtest=DmnControllerTest

# Test DTOs
./mvnw test -Dtest=DmnDtoTest

# Integration Tests
./mvnw test -Dtest=DmnIntegrationTest
```

### Test Coverage by Category
```bash
# Service layer tests
./mvnw test -Dtest="DmnServiceTest"

# REST API tests
./mvnw test -Dtest="DmnControllerTest"

# End-to-end integration tests
./mvnw test -Dtest="DmnIntegrationTest"

# All tests
./mvnw test
```

### Test Scenarios Covered

#### Service Tests (`DmnServiceTest`)
- ✅ Approval Decision logic validation
- ✅ Custom1 Decision logic validation  
- ✅ Multiple decision evaluation
- ✅ Error handling for invalid inputs
- ✅ Null and empty value handling
- ✅ Boolean conversion testing
- ✅ Boundary value testing

#### Controller Tests (`DmnControllerTest`)
- ✅ HTTP status code validation
- ✅ Request/response JSON structure
- ✅ Content-Type validation
- ✅ HTTP method validation (GET, POST, PUT, DELETE)
- ✅ Error response handling
- ✅ Edge case testing

#### Integration Tests (`DmnIntegrationTest`)
- ✅ End-to-end approval workflows
- ✅ End-to-end Custom1 Decision workflows
- ✅ Multi-decision evaluation
- ✅ Error scenario handling
- ✅ Performance and load testing
- ✅ Concurrent request handling

#### DTO Tests (`DmnDtoTest`)
- ✅ Request/Response object creation
- ✅ Getter/setter validation
- ✅ Constructor testing
- ✅ Null value handling
- ✅ Data integrity testing

## Test Results Expected

### Successful Approval Decision Cases:
- Age ≥ 18 AND Income ≥ 30,000 → `true`
- Age < 18 AND Income ≥ 50,000 → `true`
- Income = 49,900 (exact match) → `true`
- Action = "changeUp" (any age/income) → `true`

### Rejection Cases:
- Income < 50,000 (general rule) → `false`

### Custom1 Decision Cases:
- Returns complex multi-output structure
- Age = 18 with Income ≥ 10,000 → special handling
- Action = "changeUp" → approval
- Default case → falsed '{
    "dmnFile": "sample-decision.dmn",
    "decisionName": "Approval Decision",
    "inputData": {
      "age": 30,
      "income": 25000,
      "action": "changeUp"
    }
  }'
```

### Example 3: Health Check
```bash
curl http://localhost:8080/dmn/health
```

## DMN File Format

The service supports standard OMG DMN 1.3 format. Place your DMN files in `src/main/resources/` or provide absolute paths.

Example DMN structure:
- Input Data: Define input variables (age, income, etc.)
- Decision: Define the decision logic using decision tables
- Rules: Define the business rules with conditions and outcomes

## Configuration

Key configuration options in `application.properties`:

- `quarkus.http.port`: Server port (default: 8080)
- `quarkus.log.level`: Logging level
- `quarkus.http.cors`: Enable CORS for frontend integration

## Error Handling

The service provides detailed error messages for:
- Invalid DMN files
- Missing input data
- DMN evaluation errors
- File not found errors

## Dependencies

- Quarkus Framework
- KIE DMN Core (Drools DMN engine)
- Jackson for JSON processing
- RESTEasy Reactive for REST endpoints

## Testing

The service includes comprehensive error handling and validation. Test with various DMN files and input combinations to ensure proper functionality.

## Extending the Service

To add more features:
1. Create additional DMN files in resources
2. Extend the DmnRequest/DmnResponse DTOs
3. Add new endpoints in DmnController
4. Enhance decision processing logic in DmnService
