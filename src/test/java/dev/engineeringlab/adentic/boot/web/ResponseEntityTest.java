package dev.engineeringlab.adentic.boot.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for ResponseEntity.
 *
 * <p>Tests all factory methods, builders, and status code handling.
 */
@DisplayName("ResponseEntity Tests")
class ResponseEntityTest {

  // OK Tests

  @Test
  @DisplayName("Should create OK response with body")
  void shouldCreateOkResponseWithBody() {
    TestData data = new TestData("test");

    ResponseEntity<TestData> response = ResponseEntity.ok(data);

    assertThat(response.getStatusCode()).isEqualTo(200);
    assertThat(response.getBody()).isSameAs(data);
  }

  @Test
  @DisplayName("Should create OK response with null body")
  void shouldCreateOkResponseWithNullBody() {
    ResponseEntity<TestData> response = ResponseEntity.ok(null);

    assertThat(response.getStatusCode()).isEqualTo(200);
    assertThat(response.getBody()).isNull();
  }

  // Created Tests

  @Test
  @DisplayName("Should create Created response builder")
  void shouldCreateCreatedResponseBuilder() {
    ResponseEntity.BodyBuilder builder = ResponseEntity.created();

    assertThat(builder).isNotNull();
  }

  @Test
  @DisplayName("Should create Created response with body")
  void shouldCreateCreatedResponseWithBody() {
    TestData data = new TestData("created");

    ResponseEntity<TestData> response = ResponseEntity.created().body(data);

    assertThat(response.getStatusCode()).isEqualTo(201);
    assertThat(response.getBody()).isSameAs(data);
  }

  @Test
  @DisplayName("Should create Created response without body")
  void shouldCreateCreatedResponseWithoutBody() {
    ResponseEntity<Void> response = ResponseEntity.created().build();

    assertThat(response.getStatusCode()).isEqualTo(201);
    assertThat(response.getBody()).isNull();
  }

  // Accepted Tests

  @Test
  @DisplayName("Should create Accepted response builder")
  void shouldCreateAcceptedResponseBuilder() {
    ResponseEntity.BodyBuilder builder = ResponseEntity.accepted();

    assertThat(builder).isNotNull();
  }

  @Test
  @DisplayName("Should create Accepted response with body")
  void shouldCreateAcceptedResponseWithBody() {
    TestData data = new TestData("accepted");

    ResponseEntity<TestData> response = ResponseEntity.accepted().body(data);

    assertThat(response.getStatusCode()).isEqualTo(202);
    assertThat(response.getBody()).isSameAs(data);
  }

  @Test
  @DisplayName("Should create Accepted response without body")
  void shouldCreateAcceptedResponseWithoutBody() {
    ResponseEntity<Void> response = ResponseEntity.accepted().build();

    assertThat(response.getStatusCode()).isEqualTo(202);
    assertThat(response.getBody()).isNull();
  }

  // Not Found Tests

  @Test
  @DisplayName("Should create Not Found response builder")
  void shouldCreateNotFoundResponseBuilder() {
    ResponseEntity.BodyBuilder builder = ResponseEntity.notFound();

    assertThat(builder).isNotNull();
  }

  @Test
  @DisplayName("Should create Not Found response with body")
  void shouldCreateNotFoundResponseWithBody() {
    ErrorData error = new ErrorData("Resource not found");

    ResponseEntity<ErrorData> response = ResponseEntity.notFound().body(error);

    assertThat(response.getStatusCode()).isEqualTo(404);
    assertThat(response.getBody()).isSameAs(error);
  }

  @Test
  @DisplayName("Should create Not Found response without body")
  void shouldCreateNotFoundResponseWithoutBody() {
    ResponseEntity<Void> response = ResponseEntity.notFound().build();

    assertThat(response.getStatusCode()).isEqualTo(404);
    assertThat(response.getBody()).isNull();
  }

  // Bad Request Tests

  @Test
  @DisplayName("Should create Bad Request response builder")
  void shouldCreateBadRequestResponseBuilder() {
    ResponseEntity.BodyBuilder builder = ResponseEntity.badRequest();

    assertThat(builder).isNotNull();
  }

  @Test
  @DisplayName("Should create Bad Request response with body")
  void shouldCreateBadRequestResponseWithBody() {
    ErrorData error = new ErrorData("Invalid request");

    ResponseEntity<ErrorData> response = ResponseEntity.badRequest().body(error);

    assertThat(response.getStatusCode()).isEqualTo(400);
    assertThat(response.getBody()).isSameAs(error);
  }

  @Test
  @DisplayName("Should create Bad Request response without body")
  void shouldCreateBadRequestResponseWithoutBody() {
    ResponseEntity<Void> response = ResponseEntity.badRequest().build();

    assertThat(response.getStatusCode()).isEqualTo(400);
    assertThat(response.getBody()).isNull();
  }

  // Internal Server Error Tests

  @Test
  @DisplayName("Should create Internal Server Error response builder")
  void shouldCreateInternalServerErrorResponseBuilder() {
    ResponseEntity.BodyBuilder builder = ResponseEntity.internalServerError();

    assertThat(builder).isNotNull();
  }

  @Test
  @DisplayName("Should create Internal Server Error response with body")
  void shouldCreateInternalServerErrorResponseWithBody() {
    ErrorData error = new ErrorData("Server error");

    ResponseEntity<ErrorData> response = ResponseEntity.internalServerError().body(error);

    assertThat(response.getStatusCode()).isEqualTo(500);
    assertThat(response.getBody()).isSameAs(error);
  }

  @Test
  @DisplayName("Should create Internal Server Error response without body")
  void shouldCreateInternalServerErrorResponseWithoutBody() {
    ResponseEntity<Void> response = ResponseEntity.internalServerError().build();

    assertThat(response.getStatusCode()).isEqualTo(500);
    assertThat(response.getBody()).isNull();
  }

  // Custom Status Tests

  @Test
  @DisplayName("Should create custom status response builder")
  void shouldCreateCustomStatusResponseBuilder() {
    ResponseEntity.BodyBuilder builder = ResponseEntity.status(418);

    assertThat(builder).isNotNull();
  }

  @Test
  @DisplayName("Should create custom status response with body")
  void shouldCreateCustomStatusResponseWithBody() {
    TestData data = new TestData("teapot");

    ResponseEntity<TestData> response = ResponseEntity.status(418).body(data);

    assertThat(response.getStatusCode()).isEqualTo(418);
    assertThat(response.getBody()).isSameAs(data);
  }

  @Test
  @DisplayName("Should create custom status response without body")
  void shouldCreateCustomStatusResponseWithoutBody() {
    ResponseEntity<Void> response = ResponseEntity.status(418).build();

    assertThat(response.getStatusCode()).isEqualTo(418);
    assertThat(response.getBody()).isNull();
  }

  @Test
  @DisplayName("Should support various HTTP status codes")
  void shouldSupportVariousHttpStatusCodes() {
    assertThat(ResponseEntity.status(100).build().getStatusCode()).isEqualTo(100); // Continue
    assertThat(ResponseEntity.status(204).build().getStatusCode()).isEqualTo(204); // No Content
    assertThat(ResponseEntity.status(301).build().getStatusCode())
        .isEqualTo(301); // Moved Permanently
    assertThat(ResponseEntity.status(302).build().getStatusCode()).isEqualTo(302); // Found
    assertThat(ResponseEntity.status(401).build().getStatusCode()).isEqualTo(401); // Unauthorized
    assertThat(ResponseEntity.status(403).build().getStatusCode()).isEqualTo(403); // Forbidden
    assertThat(ResponseEntity.status(503).build().getStatusCode())
        .isEqualTo(503); // Service Unavailable
  }

  // Builder Tests

  @Test
  @DisplayName("Should allow fluent builder pattern")
  void shouldAllowFluentBuilderPattern() {
    TestData data = new TestData("fluent");

    ResponseEntity<TestData> response = ResponseEntity.status(201).body(data);

    assertThat(response.getStatusCode()).isEqualTo(201);
    assertThat(response.getBody()).isSameAs(data);
  }

  @Test
  @DisplayName("Should support different body types")
  void shouldSupportDifferentBodyTypes() {
    // String body
    ResponseEntity<String> stringResponse = ResponseEntity.ok("hello");
    assertThat(stringResponse.getBody()).isEqualTo("hello");

    // Integer body
    ResponseEntity<Integer> intResponse = ResponseEntity.ok(42);
    assertThat(intResponse.getBody()).isEqualTo(42);

    // Custom object body
    TestData customResponse = new TestData("custom");
    ResponseEntity<TestData> objectResponse = ResponseEntity.ok(customResponse);
    assertThat(objectResponse.getBody()).isSameAs(customResponse);
  }

  // Getters Tests

  @Test
  @DisplayName("Should get status code")
  void shouldGetStatusCode() {
    ResponseEntity<Void> response = ResponseEntity.status(200).build();

    assertThat(response.getStatusCode()).isEqualTo(200);
  }

  @Test
  @DisplayName("Should get body")
  void shouldGetBody() {
    TestData data = new TestData("body");
    ResponseEntity<TestData> response = ResponseEntity.ok(data);

    assertThat(response.getBody()).isSameAs(data);
  }

  // Edge Cases

  @Test
  @DisplayName("Should handle zero status code")
  void shouldHandleZeroStatusCode() {
    ResponseEntity<Void> response = ResponseEntity.status(0).build();

    assertThat(response.getStatusCode()).isZero();
  }

  @Test
  @DisplayName("Should handle negative status code")
  void shouldHandleNegativeStatusCode() {
    ResponseEntity<Void> response = ResponseEntity.status(-1).build();

    assertThat(response.getStatusCode()).isEqualTo(-1);
  }

  @Test
  @DisplayName("Should handle large status code")
  void shouldHandleLargeStatusCode() {
    ResponseEntity<Void> response = ResponseEntity.status(999).build();

    assertThat(response.getStatusCode()).isEqualTo(999);
  }

  // Test Data Classes

  static class TestData {
    private final String value;

    TestData(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  static class ErrorData {
    private final String message;

    ErrorData(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }
}
