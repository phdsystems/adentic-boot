package dev.adeengineer.adentic.tool.http.config;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration for HTTP Client Tool.
 *
 * @since 0.3.0
 */
@Data
@Builder
public class HttpConfig {

  /** Default request timeout in milliseconds. */
  @Builder.Default private int defaultTimeoutMs = 30000;

  /** Default connect timeout in milliseconds. */
  @Builder.Default private int connectTimeoutMs = 10000;

  /** Whether to follow redirects by default. */
  @Builder.Default private boolean followRedirects = true;

  /** Maximum number of redirects to follow. */
  @Builder.Default private int maxRedirects = 5;

  /** Default user agent string. */
  @Builder.Default private String userAgent = "Adentic-HttpClient/0.3.0";

  /** Whether to validate SSL certificates. */
  @Builder.Default private boolean validateSsl = true;

  /** Maximum response body size in bytes (-1 for unlimited). */
  @Builder.Default private long maxResponseSize = 10 * 1024 * 1024; // 10 MB

  /** Whether to log requests/responses. */
  @Builder.Default private boolean enableLogging = true;

  /** Whether to include detailed timing information. */
  @Builder.Default private boolean includeTimings = true;

  /**
   * Creates default HTTP client configuration.
   *
   * @return default config
   */
  public static HttpConfig defaults() {
    return HttpConfig.builder().build();
  }

  /**
   * Creates configuration for API testing.
   *
   * @return API testing config
   */
  public static HttpConfig apiTesting() {
    return HttpConfig.builder()
        .defaultTimeoutMs(60000)
        .enableLogging(true)
        .includeTimings(true)
        .build();
  }

  /**
   * Creates configuration for production use.
   *
   * @return production config
   */
  public static HttpConfig production() {
    return HttpConfig.builder()
        .defaultTimeoutMs(15000)
        .validateSsl(true)
        .enableLogging(false)
        .maxResponseSize(5 * 1024 * 1024) // 5 MB
        .build();
  }
}
