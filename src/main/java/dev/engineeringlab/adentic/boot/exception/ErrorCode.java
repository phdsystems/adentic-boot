package dev.engineeringlab.adentic.boot.exception;

/**
 * Interface for structured error codes used in AgenticBoot exception handling.
 *
 * <p>Error codes follow the pattern: DOMAIN_CATEGORY_SPECIFIC
 *
 * <p>Each error code includes:
 *
 * <ul>
 *   <li><b>Code</b>: Numeric identifier (10001-10999 range for AgenticBoot)
 *   <li><b>Description</b>: Human-readable description
 *   <li><b>HTTP Status</b>: Appropriate HTTP status code for web applications
 *   <li><b>Retryable</b>: Whether the operation can be retried
 * </ul>
 *
 * @since 1.0.0
 */
public interface ErrorCode {

  /**
   * Get the error code.
   *
   * @return Error code string
   */
  String getCode();

  /**
   * Get the error description.
   *
   * @return Error description
   */
  String getDescription();

  /**
   * Get the HTTP status code for this error.
   *
   * @return HTTP status code
   */
  HttpStatusCode getHttpStatusCode();

  /**
   * Get the numeric HTTP status code.
   *
   * @return HTTP status code (e.g., 404, 500)
   */
  int getHttpStatus();

  /**
   * Check if this error is retryable.
   *
   * <p>Retryable errors are typically transient (timeouts, rate limits, temporary unavailability).
   * Non-retryable errors are permanent (validation failures, not found, unauthorized).
   *
   * @return true if the operation can be retried
   */
  boolean isRetryable();

  /**
   * Get a formatted error code string.
   *
   * @return Formatted error code (e.g., "BOOT-10001")
   */
  String getFormattedCode();
}
