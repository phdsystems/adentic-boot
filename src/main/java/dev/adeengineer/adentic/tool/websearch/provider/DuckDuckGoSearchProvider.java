package dev.adeengineer.adentic.tool.websearch.provider;

import dev.adeengineer.adentic.tool.websearch.config.WebSearchConfig;
import dev.adeengineer.adentic.tool.websearch.model.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Search provider implementation for DuckDuckGo HTML search. Uses HTML parsing (no API key
 * required).
 */
@Slf4j
public class DuckDuckGoSearchProvider {
  private static final String SEARCH_URL = "https://html.duckduckgo.com/html/";

  // Regex patterns for parsing DuckDuckGo HTML
  private static final Pattern RESULT_PATTERN =
      Pattern.compile(
          "<a[^>]*class=\"result__a\"[^>]*href=\"(.*?)\"[^>]*>(.*?)</a>", Pattern.DOTALL);

  private static final Pattern SNIPPET_PATTERN =
      Pattern.compile("<a[^>]*class=\"result__snippet\"[^>]*>(.*?)</a>", Pattern.DOTALL);

  private final HttpClient httpClient;
  private final WebSearchConfig config;

  public DuckDuckGoSearchProvider(WebSearchConfig config) {
    this.config = config;
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(config.getHttpTimeoutMs()))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
  }

  /** Execute a search query. */
  public Mono<SearchResult> search(SearchRequest request) {
    Instant startTime = Instant.now();

    return Mono.fromCallable(() -> executeSearch(request))
        .retryWhen(Retry.fixedDelay(config.getHttpRetries(), Duration.ofMillis(500)))
        .map(html -> parseSearchResults(html, request, startTime))
        .onErrorResume(
            error -> {
              log.error("DuckDuckGo search failed for query: {}", request.getQuery(), error);
              return Mono.just(createErrorResult(request, error, startTime));
            });
  }

  /** Execute HTTP request to DuckDuckGo. */
  private String executeSearch(SearchRequest request) throws IOException, InterruptedException {
    String query = URLEncoder.encode(request.getQuery(), StandardCharsets.UTF_8);
    String url = SEARCH_URL + "?q=" + query;

    HttpRequest httpRequest =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", config.getUserAgent())
            .header("Accept", "text/html")
            .timeout(Duration.ofMillis(config.getHttpTimeoutMs()))
            .POST(buildFormData(request))
            .build();

    HttpResponse<String> response =
        httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new IOException("DuckDuckGo returned status code: " + response.statusCode());
    }

    return response.body();
  }

  /** Build form data for POST request. */
  private HttpRequest.BodyPublisher buildFormData(SearchRequest request) {
    String formData =
        String.format(
            "q=%s&b=&kl=%s",
            URLEncoder.encode(request.getQuery(), StandardCharsets.UTF_8),
            request.getRegion() != null ? request.getRegion() : config.getDefaultRegion());
    return HttpRequest.BodyPublishers.ofString(formData);
  }

  /** Parse HTML response to extract search results. */
  private SearchResult parseSearchResults(String html, SearchRequest request, Instant startTime) {
    List<SearchItem> items = new ArrayList<>();

    Matcher resultMatcher = RESULT_PATTERN.matcher(html);
    int position = 1;
    int maxResults = request.getMaxResults() > 0 ? request.getMaxResults() : config.getMaxResults();

    while (resultMatcher.find() && position <= maxResults) {
      String url = cleanUrl(resultMatcher.group(1));
      String title = cleanHtml(resultMatcher.group(2));

      // Find corresponding snippet
      String snippet = findSnippet(html, resultMatcher.end());

      SearchItem item =
          SearchItem.builder()
              .position(position)
              .title(title)
              .url(url)
              .displayUrl(extractDomain(url))
              .snippet(snippet)
              .build();

      items.add(item);
      position++;
    }

    Duration queryTime = Duration.between(startTime, Instant.now());

    SearchMetadata metadata =
        SearchMetadata.builder()
            .query(request.getQuery())
            .provider(SearchProvider.DUCKDUCKGO)
            .resultCount(items.size())
            .timestamp(startTime)
            .queryTime(queryTime)
            .cached(false)
            .success(true)
            .build();

    return SearchResult.builder().metadata(metadata).items(items).build();
  }

  /** Find snippet text near the given position in HTML. */
  private String findSnippet(String html, int startPos) {
    Matcher snippetMatcher = SNIPPET_PATTERN.matcher(html);
    if (snippetMatcher.find(startPos)) {
      return cleanHtml(snippetMatcher.group(1));
    }
    return null;
  }

  /** Clean DuckDuckGo redirect URL to get actual destination. */
  private String cleanUrl(String url) {
    if (url.startsWith("//duckduckgo.com/l/?uddg=")) {
      // Extract actual URL from DuckDuckGo redirect
      int start = url.indexOf("uddg=") + 5;
      int end = url.indexOf("&", start);
      if (end == -1) {
        end = url.length();
      }
      try {
        return URLEncoder.encode(url.substring(start, end), StandardCharsets.UTF_8);
      } catch (Exception e) {
        log.warn("Failed to decode URL: {}", url, e);
      }
    }
    return url;
  }

  /** Remove HTML tags and decode entities. */
  private String cleanHtml(String html) {
    if (html == null) {
      return null;
    }

    return html.replaceAll("<[^>]*>", "") // Remove HTML tags
        .replaceAll("&nbsp;", " ") // Decode nbsp
        .replaceAll("&amp;", "&") // Decode amp
        .replaceAll("&lt;", "<") // Decode lt
        .replaceAll("&gt;", ">") // Decode gt
        .replaceAll("&quot;", "\"") // Decode quot
        .replaceAll("&#39;", "'") // Decode apostrophe
        .replaceAll("\\s+", " ") // Normalize whitespace
        .trim();
  }

  /** Extract domain from URL for display. */
  private String extractDomain(String url) {
    try {
      URI uri = URI.create(url);
      return uri.getHost() != null ? uri.getHost() : url;
    } catch (Exception e) {
      return url;
    }
  }

  /** Create error result when search fails. */
  private SearchResult createErrorResult(
      SearchRequest request, Throwable error, Instant startTime) {
    Duration queryTime = Duration.between(startTime, Instant.now());

    SearchMetadata metadata =
        SearchMetadata.builder()
            .query(request.getQuery())
            .provider(SearchProvider.DUCKDUCKGO)
            .resultCount(0)
            .timestamp(startTime)
            .queryTime(queryTime)
            .error(error.getMessage())
            .success(false)
            .build();

    return SearchResult.builder().metadata(metadata).items(new ArrayList<>()).build();
  }
}
