package dev.adeengineer.adentic.tool.http.model;

import java.time.Duration;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * HTTP response data.
 *
 * @since 0.3.0
 */
@Data
@Builder
public class HttpResponse {

  /** HTTP status code. */
  private int statusCode;

  /** Response body. */
  @Builder.Default private String body = null;

  /** Response headers. */
  @Builder.Default private Map<String, String> headers = Map.of();

  /** Whether the request was successful (2xx status). */
  private boolean success;

  /** Error message if request failed. */
  @Builder.Default private String errorMessage = null;

  /** Request duration. */
  @Builder.Default private Duration duration = Duration.ZERO;

  /** Content type from response headers. */
  @Builder.Default private String contentType = null;

  /** Content length. */
  @Builder.Default private long contentLength = 0;

  /**
   * Creates a successful response.
   *
   * @param statusCode HTTP status code
   * @param body response body
   * @param headers response headers
   * @return HttpResponse
   */
  public static HttpResponse success(int statusCode, String body, Map<String, String> headers) {
    return HttpResponse.builder()
        .statusCode(statusCode)
        .body(body)
        .headers(headers)
        .success(true)
        .contentType(headers.getOrDefault("Content-Type", "text/plain"))
        .contentLength(body != null ? body.length() : 0)
        .build();
  }

  /**
   * Creates a failed response.
   *
   * @param statusCode HTTP status code
   * @param errorMessage error message
   * @return HttpResponse
   */
  public static HttpResponse error(int statusCode, String errorMessage) {
    return HttpResponse.builder()
        .statusCode(statusCode)
        .success(false)
        .errorMessage(errorMessage)
        .build();
  }

  /**
   * Creates an exception-based error response.
   *
   * @param error exception
   * @return HttpResponse
   */
  public static HttpResponse error(Exception error) {
    return HttpResponse.builder()
        .statusCode(0)
        .success(false)
        .errorMessage(error.getMessage())
        .build();
  }

  /** Checks if status is 2xx. */
  public boolean is2xxSuccess() {
    return statusCode >= 200 && statusCode < 300;
  }

  /** Checks if status is 4xx. */
  public boolean is4xxClientError() {
    return statusCode >= 400 && statusCode < 500;
  }

  /** Checks if status is 5xx. */
  public boolean is5xxServerError() {
    return statusCode >= 500 && statusCode < 600;
  }
}
