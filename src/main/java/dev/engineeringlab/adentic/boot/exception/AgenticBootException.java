package dev.engineeringlab.adentic.boot.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Base exception for all AgenticBoot framework exceptions.
 *
 * <p>Provides structured error handling with error codes, categorization, and additional context.
 *
 * <p>All AgenticBoot-specific exceptions should extend this class.
 *
 * <p>This is an unchecked exception (extends RuntimeException) to work seamlessly with modern Java
 * frameworks and reactive programming patterns.
 *
 * <h3>Features:</h3>
 *
 * <ul>
 *   <li><b>Structured Error Codes:</b> Type-safe error categorization
 *   <li><b>HTTP Mapping:</b> Automatic HTTP status code mapping
 *   <li><b>Context Information:</b> Additional runtime context via Map
 *   <li><b>Distributed Tracing:</b> Trace ID and Span ID for request tracking
 * </ul>
 *
 * @since 1.0.0
 */
public class AgenticBootException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** Structured error code for categorization and handling. */
  private final transient ErrorCode errorCode;

  /** Additional context information (component name, operation details, etc.). */
  private final transient Map<String, Object> context;

  /** Distributed tracing - correlation ID for end-to-end request tracking. */
  private final String traceId;

  /** Distributed tracing - span ID for this specific operation. */
  private final String spanId;

  /**
   * Constructs a new exception with error code and message.
   *
   * @param errorCode the error code
   * @param message the error message
   */
  public AgenticBootException(final ErrorCode errorCode, final String message) {
    super(formatMessage(errorCode, message));
    this.errorCode = errorCode;
    this.context = Collections.emptyMap();
    this.traceId = generateTraceId();
    this.spanId = generateSpanId();
  }

  /**
   * Constructs a new exception with error code, message, and cause.
   *
   * @param errorCode the error code
   * @param message the error message
   * @param cause the underlying cause
   */
  public AgenticBootException(
      final ErrorCode errorCode, final String message, final Throwable cause) {
    super(formatMessage(errorCode, message), cause);
    this.errorCode = errorCode;
    this.context = Collections.emptyMap();
    this.traceId = generateTraceId();
    this.spanId = generateSpanId();
  }

  /**
   * Constructs a new exception with error code, message, cause, and context.
   *
   * @param errorCode the error code
   * @param message the error message
   * @param cause the underlying cause
   * @param context additional context information
   */
  public AgenticBootException(
      final ErrorCode errorCode,
      final String message,
      final Throwable cause,
      final Map<String, Object> context) {
    super(formatMessage(errorCode, message), cause);
    this.errorCode = errorCode;
    this.context = context != null ? new HashMap<>(context) : Collections.emptyMap();
    this.traceId = generateTraceId();
    this.spanId = generateSpanId();
  }

  /**
   * Format exception message with error code.
   *
   * @param errorCode error code
   * @param message message
   * @return formatted message
   */
  private static String formatMessage(final ErrorCode errorCode, final String message) {
    if (errorCode == null) {
      return message;
    }
    return String.format("[%s] %s", errorCode.getFormattedCode(), message);
  }

  /**
   * Generate unique trace ID.
   *
   * @return trace ID
   */
  private static String generateTraceId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Generate unique span ID.
   *
   * @return span ID
   */
  private static String generateSpanId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Get the error code.
   *
   * @return error code
   */
  public ErrorCode getErrorCode() {
    return errorCode;
  }

  /**
   * Get additional context information.
   *
   * @return immutable context map
   */
  public Map<String, Object> getContext() {
    return Collections.unmodifiableMap(context);
  }

  /**
   * Get trace ID for distributed tracing.
   *
   * @return trace ID
   */
  public String getTraceId() {
    return traceId;
  }

  /**
   * Get span ID for distributed tracing.
   *
   * @return span ID
   */
  public String getSpanId() {
    return spanId;
  }

  /**
   * Get HTTP status code.
   *
   * @return HTTP status code
   */
  public int getHttpStatus() {
    return errorCode != null ? errorCode.getHttpStatus() : 500;
  }

  /**
   * Check if this error is retryable.
   *
   * @return true if retryable
   */
  public boolean isRetryable() {
    return errorCode != null && errorCode.isRetryable();
  }

  @Override
  public String toString() {
    return String.format(
        "%s: %s [traceId=%s, spanId=%s, httpStatus=%d, retryable=%s]",
        getClass().getSimpleName(),
        getMessage(),
        traceId,
        spanId,
        getHttpStatus(),
        isRetryable());
  }
}
