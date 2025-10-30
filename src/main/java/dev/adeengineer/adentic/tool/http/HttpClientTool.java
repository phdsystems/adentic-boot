package dev.adeengineer.adentic.tool.http;

import dev.adeengineer.adentic.tool.http.config.HttpConfig;
import dev.adeengineer.adentic.tool.http.model.HttpMethod;
import dev.adeengineer.adentic.tool.http.model.HttpRequest;
import dev.adeengineer.adentic.tool.http.model.HttpResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * HTTP Client Tool for REST API interactions.
 *
 * <p>Supports:
 *
 * <ul>
 *   <li><b>Methods</b>: GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS
 *   <li><b>Headers</b>: custom headers, authentication
 *   <li><b>Body</b>: JSON, form data, plain text
 *   <li><b>Timeouts</b>: configurable request/connect timeouts
 *   <li><b>Redirects</b>: automatic redirect following
 *   <li><b>SSL</b>: certificate validation
 * </ul>
 *
 * @since 0.3.0
 */
@Slf4j
public class HttpClientTool {

  private final HttpConfig config;
  private final HttpClient httpClient;

  public HttpClientTool(HttpConfig config) {
    this.config = config;
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(config.getConnectTimeoutMs()))
            .followRedirects(
                config.isFollowRedirects() ? HttpClient.Redirect.NORMAL : HttpClient.Redirect.NEVER)
            .build();
  }

  public HttpClientTool() {
    this(HttpConfig.defaults());
  }

  /**
   * Executes an HTTP request.
   *
   * @param request HTTP request
   * @return Mono emitting HTTP response
   */
  public Mono<HttpResponse> execute(HttpRequest request) {
    return Mono.fromCallable(
        () -> {
          Instant startTime = Instant.now();

          try {
            if (config.isEnableLogging()) {
              log.debug("HTTP {} {}", request.getMethod(), request.getUrl());
            }

            // Build URI with query parameters
            URI uri = buildUri(request);

            // Build HTTP request
            java.net.http.HttpRequest.Builder requestBuilder =
                java.net.http.HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofMillis(request.getTimeoutMs()));

            // Add headers
            Map<String, String> headers = new HashMap<>(request.getHeaders());
            headers.put("User-Agent", config.getUserAgent());
            if (request.getBody() != null && !headers.containsKey("Content-Type")) {
              headers.put("Content-Type", request.getContentType());
            }

            headers.forEach(requestBuilder::header);

            // Set method and body
            switch (request.getMethod()) {
              case GET -> requestBuilder.GET();
              case POST ->
                  requestBuilder.POST(
                      BodyPublishers.ofString(request.getBody() != null ? request.getBody() : ""));
              case PUT ->
                  requestBuilder.PUT(
                      BodyPublishers.ofString(request.getBody() != null ? request.getBody() : ""));
              case PATCH ->
                  requestBuilder.method(
                      "PATCH",
                      BodyPublishers.ofString(request.getBody() != null ? request.getBody() : ""));
              case DELETE -> requestBuilder.DELETE();
              case HEAD -> requestBuilder.method("HEAD", BodyPublishers.noBody());
              case OPTIONS -> requestBuilder.method("OPTIONS", BodyPublishers.noBody());
            }

            // Execute request
            java.net.http.HttpRequest httpRequest = requestBuilder.build();
            java.net.http.HttpResponse<String> httpResponse =
                httpClient.send(httpRequest, BodyHandlers.ofString());

            // Calculate duration
            Duration duration = Duration.between(startTime, Instant.now());

            // Extract headers
            Map<String, String> responseHeaders =
                httpResponse.headers().map().entrySet().stream()
                    .collect(
                        Collectors.toMap(Map.Entry::getKey, e -> String.join(", ", e.getValue())));

            // Build response
            HttpResponse response =
                HttpResponse.builder()
                    .statusCode(httpResponse.statusCode())
                    .body(httpResponse.body())
                    .headers(responseHeaders)
                    .success(httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300)
                    .duration(duration)
                    .contentType(
                        responseHeaders.getOrDefault("Content-Type", "application/octet-stream"))
                    .contentLength(httpResponse.body() != null ? httpResponse.body().length() : 0)
                    .build();

            if (config.isEnableLogging()) {
              log.debug(
                  "HTTP {} {} -> {} ({}ms)",
                  request.getMethod(),
                  request.getUrl(),
                  response.getStatusCode(),
                  duration.toMillis());
            }

            return response;

          } catch (IOException e) {
            log.error("HTTP request failed (I/O error): {}", e.getMessage());
            return HttpResponse.error(e);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt status
            log.error("HTTP request interrupted: {}", e.getMessage());
            return HttpResponse.error(e);
          } catch (IllegalArgumentException e) {
            log.error("Invalid HTTP request: {}", e.getMessage());
            return HttpResponse.error(e);
          }
        });
  }

  /**
   * Builds URI with query parameters.
   *
   * @param request HTTP request
   * @return URI
   */
  private URI buildUri(HttpRequest request) {
    String url = request.getUrl();

    if (!request.getQueryParams().isEmpty()) {
      String queryString =
          request.getQueryParams().entrySet().stream()
              .map(
                  entry ->
                      String.format(
                          "%s=%s", urlEncode(entry.getKey()), urlEncode(entry.getValue())))
              .collect(Collectors.joining("&"));

      url += (url.contains("?") ? "&" : "?") + queryString;
    }

    return URI.create(url);
  }

  /**
   * URL-encodes a string.
   *
   * @param value value to encode
   * @return encoded value
   */
  private String urlEncode(String value) {
    return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
  }

  /**
   * Convenience methods for common HTTP operations.
   *
   * @param url target URL
   * @return Mono emitting response
   */
  public Mono<HttpResponse> get(String url) {
    return execute(HttpRequest.get(url));
  }

  public Mono<HttpResponse> get(String url, Map<String, String> headers) {
    return execute(HttpRequest.builder().method(HttpMethod.GET).url(url).headers(headers).build());
  }

  public Mono<HttpResponse> post(String url, String body) {
    return execute(HttpRequest.post(url, body));
  }

  public Mono<HttpResponse> post(String url, String body, Map<String, String> headers) {
    return execute(
        HttpRequest.builder().method(HttpMethod.POST).url(url).body(body).headers(headers).build());
  }

  public Mono<HttpResponse> put(String url, String body) {
    return execute(HttpRequest.put(url, body));
  }

  public Mono<HttpResponse> delete(String url) {
    return execute(HttpRequest.delete(url));
  }

  /**
   * Executes a GET request and returns the body as a string.
   *
   * @param url target URL
   * @return Mono emitting response body
   */
  public Mono<String> getBody(String url) {
    return get(url).map(HttpResponse::getBody);
  }

  /**
   * Checks if a URL is reachable.
   *
   * @param url target URL
   * @return Mono emitting true if reachable
   */
  public Mono<Boolean> isReachable(String url) {
    return execute(HttpRequest.builder().method(HttpMethod.HEAD).url(url).build())
        .map(HttpResponse::isSuccess)
        .onErrorReturn(false);
  }
}
