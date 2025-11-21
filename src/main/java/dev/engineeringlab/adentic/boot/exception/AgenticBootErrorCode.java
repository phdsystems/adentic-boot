package dev.engineeringlab.adentic.boot.exception;

import static dev.engineeringlab.adentic.boot.exception.HttpStatusCode.*;

/**
 * Error codes for AgenticBoot framework operations.
 *
 * <p>Defines all error codes in the 10001-10999 range for AgenticBoot framework-level operations.
 *
 * <p><b>Error Code Range: 10001-10060</b>
 *
 * @since 1.0.0
 */
public enum AgenticBootErrorCode implements ErrorCode {

  // ========== Configuration Errors (10001-10010) ==========

  /** Boot configuration is invalid or missing required parameters. */
  BOOT_CONFIGURATION_INVALID(
      "10001", "AgenticBoot configuration is invalid", BAD_REQUEST, false),

  /** Boot initialization failed. */
  BOOT_INITIALIZATION_FAILED(
      "10002", "AgenticBoot initialization failed", INTERNAL_SERVER_ERROR, false),

  /** Configuration file not found. */
  BOOT_CONFIGURATION_NOT_FOUND("10003", "Configuration file not found", NOT_FOUND, false),

  /** Configuration validation failed. */
  BOOT_CONFIGURATION_VALIDATION_FAILED(
      "10004", "Configuration validation failed", UNPROCESSABLE_ENTITY, false),

  // ========== Component Scanning Errors (10011-10020) ==========

  /** Component scanning failed. */
  COMPONENT_SCAN_FAILED("10011", "Component scan failed", INTERNAL_SERVER_ERROR, false),

  /** Base package not found during scan. */
  BASE_PACKAGE_NOT_FOUND("10012", "Base package not found", NOT_FOUND, false),

  /** Component class loading failed. */
  COMPONENT_CLASS_LOAD_FAILED(
      "10013", "Component class loading failed", INTERNAL_SERVER_ERROR, false),

  /** Duplicate component name detected. */
  DUPLICATE_COMPONENT_NAME("10014", "Duplicate component name", CONFLICT, false),

  // ========== Dependency Injection Errors (10021-10030) ==========

  /** Dependency injection failed. */
  DEPENDENCY_INJECTION_FAILED(
      "10021", "Dependency injection failed", INTERNAL_SERVER_ERROR, false),

  /** Circular dependency detected. */
  CIRCULAR_DEPENDENCY("10022", "Circular dependency detected", INTERNAL_SERVER_ERROR, false),

  /** Bean not found in context. */
  BEAN_NOT_FOUND("10023", "Bean not found", NOT_FOUND, false),

  /** Constructor injection failed. */
  CONSTRUCTOR_INJECTION_FAILED(
      "10024", "Constructor injection failed", INTERNAL_SERVER_ERROR, false),

  /** Ambiguous bean definition (multiple candidates). */
  AMBIGUOUS_BEAN("10025", "Ambiguous bean definition", CONFLICT, false),

  /** Bean instantiation failed. */
  BEAN_INSTANTIATION_FAILED(
      "10026", "Bean instantiation failed", INTERNAL_SERVER_ERROR, false),

  // ========== HTTP Server Errors (10031-10040) ==========

  /** HTTP server start failed. */
  SERVER_START_FAILED("10031", "HTTP server start failed", INTERNAL_SERVER_ERROR, false),

  /** HTTP server stop failed. */
  SERVER_STOP_FAILED("10032", "HTTP server stop failed", INTERNAL_SERVER_ERROR, false),

  /** Route registration failed. */
  ROUTE_REGISTRATION_FAILED(
      "10033", "Route registration failed", INTERNAL_SERVER_ERROR, false),

  /** Port already in use. */
  PORT_IN_USE("10034", "Port already in use", CONFLICT, false),

  /** Handler method invocation failed. */
  HANDLER_INVOCATION_FAILED(
      "10035", "Handler method invocation failed", INTERNAL_SERVER_ERROR, true),

  // ========== Provider Registry Errors (10041-10050) ==========

  /** Provider registration failed. */
  PROVIDER_REGISTRATION_FAILED(
      "10041", "Provider registration failed", INTERNAL_SERVER_ERROR, false),

  /** Provider not found. */
  PROVIDER_NOT_FOUND("10042", "Provider not found", NOT_FOUND, false),

  /** Provider initialization failed. */
  PROVIDER_INITIALIZATION_FAILED(
      "10043", "Provider initialization failed", INTERNAL_SERVER_ERROR, false),

  /** Duplicate provider name. */
  DUPLICATE_PROVIDER_NAME("10044", "Duplicate provider name", CONFLICT, false),

  /** Provider category invalid. */
  PROVIDER_CATEGORY_INVALID("10045", "Provider category invalid", BAD_REQUEST, false),

  // ========== Event Bus Errors (10051-10060) ==========

  /** Event publishing failed. */
  EVENT_PUBLISH_FAILED("10051", "Event publishing failed", INTERNAL_SERVER_ERROR, true),

  /** Event listener registration failed. */
  EVENT_LISTENER_REGISTRATION_FAILED(
      "10052", "Event listener registration failed", INTERNAL_SERVER_ERROR, false),

  /** Event listener execution failed. */
  EVENT_LISTENER_EXECUTION_FAILED(
      "10053", "Event listener execution failed", INTERNAL_SERVER_ERROR, true),

  /** Event type not supported. */
  EVENT_TYPE_NOT_SUPPORTED("10054", "Event type not supported", BAD_REQUEST, false);

  private final String code;
  private final String description;
  private final HttpStatusCode httpStatusCode;
  private final boolean retryable;

  AgenticBootErrorCode(
      final String code,
      final String description,
      final HttpStatusCode httpStatusCode,
      final boolean retryable) {
    this.code = code;
    this.description = description;
    this.httpStatusCode = httpStatusCode;
    this.retryable = retryable;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public HttpStatusCode getHttpStatusCode() {
    return httpStatusCode;
  }

  @Override
  public int getHttpStatus() {
    return httpStatusCode.getCode();
  }

  @Override
  public boolean isRetryable() {
    return retryable;
  }

  @Override
  public String getFormattedCode() {
    return "BOOT-" + code;
  }

  @Override
  public String toString() {
    return String.format(
        "%s: %s (HTTP %d, Retryable: %s)",
        getFormattedCode(), description, getHttpStatus(), retryable);
  }
}
