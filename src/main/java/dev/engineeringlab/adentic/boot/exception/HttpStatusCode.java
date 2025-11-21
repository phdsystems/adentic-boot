package dev.engineeringlab.adentic.boot.exception;

/**
 * HTTP status codes for error responses.
 *
 * <p>Provides standard HTTP status codes without requiring Spring Web dependency.
 *
 * <p>Used by {@link ErrorCode} to map exceptions to HTTP responses in web applications.
 *
 * @since 1.0.0
 */
public enum HttpStatusCode {
  /** 200 OK - Request successful. */
  OK(200, "OK"),

  /** 400 Bad Request - Client error (invalid input, validation failure). */
  BAD_REQUEST(400, "Bad Request"),

  /** 401 Unauthorized - Authentication required. */
  UNAUTHORIZED(401, "Unauthorized"),

  /** 403 Forbidden - Insufficient permissions. */
  FORBIDDEN(403, "Forbidden"),

  /** 404 Not Found - Resource not found. */
  NOT_FOUND(404, "Not Found"),

  /** 408 Request Timeout - Request timeout. */
  REQUEST_TIMEOUT(408, "Request Timeout"),

  /** 409 Conflict - Resource conflict. */
  CONFLICT(409, "Conflict"),

  /** 422 Unprocessable Entity - Semantic validation error. */
  UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),

  /** 429 Too Many Requests - Rate limit exceeded. */
  TOO_MANY_REQUESTS(429, "Too Many Requests"),

  /** 500 Internal Server Error - Server-side error. */
  INTERNAL_SERVER_ERROR(500, "Internal Server Error"),

  /** 502 Bad Gateway - Upstream service error. */
  BAD_GATEWAY(502, "Bad Gateway"),

  /** 503 Service Unavailable - Service temporarily unavailable. */
  SERVICE_UNAVAILABLE(503, "Service Unavailable"),

  /** 504 Gateway Timeout - Upstream service timeout. */
  GATEWAY_TIMEOUT(504, "Gateway Timeout");

  private final int code;
  private final String reasonPhrase;

  HttpStatusCode(final int code, final String reasonPhrase) {
    this.code = code;
    this.reasonPhrase = reasonPhrase;
  }

  /**
   * Get the HTTP status code.
   *
   * @return HTTP status code (e.g., 404, 500)
   */
  public int getCode() {
    return code;
  }

  /**
   * Get the reason phrase.
   *
   * @return Reason phrase (e.g., "Not Found", "Internal Server Error")
   */
  public String getReasonPhrase() {
    return reasonPhrase;
  }

  /**
   * Check if this is a client error (4xx).
   *
   * @return true if 400-499
   */
  public boolean isClientError() {
    return code >= 400 && code < 500;
  }

  /**
   * Check if this is a server error (5xx).
   *
   * @return true if 500-599
   */
  public boolean isServerError() {
    return code >= 500 && code < 600;
  }

  @Override
  public String toString() {
    return code + " " + reasonPhrase;
  }
}
