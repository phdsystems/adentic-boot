package dev.adeengineer.adentic.tool.http.model;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * HTTP request specification.
 *
 * @since 0.3.0
 */
@Data
@Builder
public class HttpRequest {

  /** HTTP method. */
  private HttpMethod method;

  /** Target URL. */
  private String url;

  /** Request headers. */
  @Builder.Default private Map<String, String> headers = Map.of();

  /** Request body (for POST, PUT, PATCH). */
  @Builder.Default private String body = null;

  /** Query parameters. */
  @Builder.Default private Map<String, String> queryParams = Map.of();

  /** Request timeout in milliseconds. */
  @Builder.Default private int timeoutMs = 30000;

  /** Whether to follow redirects. */
  @Builder.Default private boolean followRedirects = true;

  /** Content type (convenience field, also in headers). */
  @Builder.Default private String contentType = "application/json";

  /**
   * Creates a GET request.
   *
   * @param url target URL
   * @return HttpRequest
   */
  public static HttpRequest get(String url) {
    return HttpRequest.builder().method(HttpMethod.GET).url(url).build();
  }

  /**
   * Creates a POST request.
   *
   * @param url target URL
   * @param body request body
   * @return HttpRequest
   */
  public static HttpRequest post(String url, String body) {
    return HttpRequest.builder().method(HttpMethod.POST).url(url).body(body).build();
  }

  /**
   * Creates a PUT request.
   *
   * @param url target URL
   * @param body request body
   * @return HttpRequest
   */
  public static HttpRequest put(String url, String body) {
    return HttpRequest.builder().method(HttpMethod.PUT).url(url).body(body).build();
  }

  /**
   * Creates a DELETE request.
   *
   * @param url target URL
   * @return HttpRequest
   */
  public static HttpRequest delete(String url) {
    return HttpRequest.builder().method(HttpMethod.DELETE).url(url).build();
  }
}
