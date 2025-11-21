package dev.engineeringlab.adentic.boot.web;

/**
 * Represents an HTTP response with status code and body.
 *
 * <p>Provides a fluent API for building responses similar to Spring's ResponseEntity.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * return ResponseEntity.ok(agent);
 * return ResponseEntity.created().body(agent);
 * return ResponseEntity.notFound().build();
 * return ResponseEntity.accepted().body(response);
 * }</pre>
 *
 * @param <T> response body type
 */
public class ResponseEntity<T> {

  private final int statusCode;
  private final T body;

  private ResponseEntity(final int statusCode, final T body) {
    this.statusCode = statusCode;
    this.body = body;
  }

  /**
   * Get the HTTP status code.
   *
   * @return status code
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * Get the response body.
   *
   * @return response body
   */
  public T getBody() {
    return body;
  }

  /** Builder for creating ResponseEntity instances. */
  public static class BodyBuilder {
    private final int statusCode;

    private BodyBuilder(final int statusCode) {
      this.statusCode = statusCode;
    }

    /**
     * Set the response body.
     *
     * @param body the response body
     * @param <T> body type
     * @return ResponseEntity with body
     */
    public <T> ResponseEntity<T> body(final T body) {
      return new ResponseEntity<>(statusCode, body);
    }

    /**
     * Build ResponseEntity with no body.
     *
     * @param <T> response type
     * @return ResponseEntity with no body
     */
    public <T> ResponseEntity<T> build() {
      return new ResponseEntity<>(statusCode, null);
    }
  }

  /**
   * Create a 200 OK response.
   *
   * @param body the response body
   * @param <T> body type
   * @return ResponseEntity with 200 status
   */
  public static <T> ResponseEntity<T> ok(final T body) {
    return new ResponseEntity<>(200, body);
  }

  /**
   * Create a 201 Created response builder.
   *
   * @return response builder
   */
  public static BodyBuilder created() {
    return new BodyBuilder(201);
  }

  /**
   * Create a 202 Accepted response builder.
   *
   * @return response builder
   */
  public static BodyBuilder accepted() {
    return new BodyBuilder(202);
  }

  /**
   * Create a 404 Not Found response builder.
   *
   * @return response builder
   */
  public static BodyBuilder notFound() {
    return new BodyBuilder(404);
  }

  /**
   * Create a 400 Bad Request response builder.
   *
   * @return response builder
   */
  public static BodyBuilder badRequest() {
    return new BodyBuilder(400);
  }

  /**
   * Create a 500 Internal Server Error response builder.
   *
   * @return response builder
   */
  public static BodyBuilder internalServerError() {
    return new BodyBuilder(500);
  }

  /**
   * Create a custom status response builder.
   *
   * @param statusCode HTTP status code
   * @return response builder
   */
  public static BodyBuilder status(final int statusCode) {
    return new BodyBuilder(statusCode);
  }
}
