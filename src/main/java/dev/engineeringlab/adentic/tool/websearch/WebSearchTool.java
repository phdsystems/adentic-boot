package dev.engineeringlab.adentic.tool.websearch;

import dev.engineeringlab.adentic.tool.websearch.config.WebSearchConfig;
import dev.engineeringlab.adentic.tool.websearch.model.*;
import dev.engineeringlab.adentic.tool.websearch.provider.DuckDuckGoSearchProvider;
import dev.engineeringlab.annotation.provider.ToolProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Tool for performing web searches using various search providers.
 *
 * <p>Supports multiple search engines:
 *
 * <ul>
 *   <li>DuckDuckGo (default, no API key required)
 *   <li>Google Custom Search API (requires API key)
 *   <li>Bing Search API (requires API key)
 * </ul>
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Async/reactive API using Mono
 *   <li>Result caching with configurable TTL
 *   <li>Configurable timeouts and retries
 *   <li>Multiple search providers
 *   <li>Safe search options
 *   <li>Region/language preferences
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Inject
 * private WebSearchTool searchTool;
 *
 * SearchResult result = searchTool.search("machine learning tutorials").block();
 * System.out.println(result.getSummary());
 * }</pre>
 *
 * @see WebSearchConfig
 * @see SearchResult
 * @see SearchRequest
 */
@ToolProvider(name = "web-search")
@Slf4j
public class WebSearchTool {

  @Setter private WebSearchConfig config;

  private final Map<String, CachedResult> cache;
  private DuckDuckGoSearchProvider duckDuckGoProvider;

  /** Default constructor with default configuration. */
  public WebSearchTool() {
    this(WebSearchConfig.defaults());
  }

  /** Constructor with custom configuration. */
  public WebSearchTool(WebSearchConfig config) {
    this(config, null);
  }

  /**
   * Constructor with custom configuration and provider (for testing).
   *
   * @param config the search configuration
   * @param duckDuckGoProvider the DuckDuckGo search provider (null to create default)
   */
  public WebSearchTool(WebSearchConfig config, DuckDuckGoSearchProvider duckDuckGoProvider) {
    this.config = config;
    this.cache = new ConcurrentHashMap<>();
    this.duckDuckGoProvider =
        duckDuckGoProvider != null ? duckDuckGoProvider : new DuckDuckGoSearchProvider(config);
  }

  /**
   * Perform a simple web search with the given query.
   *
   * @param query the search query
   * @return Mono containing search results
   */
  public Mono<SearchResult> search(String query) {
    return search(SearchRequest.builder().query(query).maxResults(config.getMaxResults()).build());
  }

  /**
   * Perform a web search with detailed request parameters.
   *
   * @param request the search request with all parameters
   * @return Mono containing search results
   */
  public Mono<SearchResult> search(SearchRequest request) {
    // Validate request
    if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
      return Mono.just(createErrorResult(request, "Query cannot be empty"));
    }

    // Check cache if enabled
    if (config.isCacheResults()) {
      String cacheKey = buildCacheKey(request);
      CachedResult cached = cache.get(cacheKey);

      if (cached != null && !cached.isExpired()) {
        log.debug("Returning cached result for query: {}", request.getQuery());
        return Mono.just(
            cached.result.toBuilder()
                .metadata(cached.result.getMetadata().toBuilder().cached(true).build())
                .build());
      }
    }

    // Determine provider
    SearchProvider provider =
        request.getProvider() != null ? request.getProvider() : config.getDefaultProvider();

    // Execute search
    Mono<SearchResult> searchMono =
        switch (provider) {
          case DUCKDUCKGO -> searchWithDuckDuckGo(request);
          case GOOGLE -> searchWithGoogle(request);
          case BING -> searchWithBing(request);
        };

    // Cache result if enabled
    if (config.isCacheResults()) {
      searchMono =
          searchMono.doOnNext(
              result -> {
                if (result.isSuccess()) {
                  String cacheKey = buildCacheKey(request);
                  cache.put(
                      cacheKey,
                      new CachedResult(result, Instant.now().plusMillis(config.getCacheTtlMs())));
                  log.debug("Cached result for query: {}", request.getQuery());
                }
              });
    }

    return searchMono;
  }

  /** Search using DuckDuckGo. */
  private Mono<SearchResult> searchWithDuckDuckGo(SearchRequest request) {
    log.info("Executing DuckDuckGo search for query: {}", request.getQuery());
    return duckDuckGoProvider.search(request);
  }

  /**
   * Search using Google Custom Search API. TODO: Implement when Google API integration is needed.
   */
  private Mono<SearchResult> searchWithGoogle(SearchRequest request) {
    log.warn("Google Custom Search API not yet implemented");
    return Mono.just(
        createErrorResult(
            request,
            "Google Custom Search API not yet implemented. Use DuckDuckGo or configure Google API keys."));
  }

  /** Search using Bing Search API. TODO: Implement when Bing API integration is needed. */
  private Mono<SearchResult> searchWithBing(SearchRequest request) {
    log.warn("Bing Search API not yet implemented");
    return Mono.just(
        createErrorResult(
            request,
            "Bing Search API not yet implemented. Use DuckDuckGo or configure Bing API key."));
  }

  /** Build cache key from request parameters. */
  private String buildCacheKey(SearchRequest request) {
    return String.format(
        "%s:%s:%d:%s",
        request.getProvider() != null ? request.getProvider() : config.getDefaultProvider(),
        request.getQuery(),
        request.getMaxResults(),
        request.getRegion() != null ? request.getRegion() : config.getDefaultRegion());
  }

  /** Create an error result. */
  private SearchResult createErrorResult(SearchRequest request, String errorMessage) {
    SearchMetadata metadata =
        SearchMetadata.builder()
            .query(request != null ? request.getQuery() : "unknown")
            .provider(
                request != null && request.getProvider() != null
                    ? request.getProvider()
                    : config.getDefaultProvider())
            .resultCount(0)
            .timestamp(Instant.now())
            .queryTime(Duration.ZERO)
            .error(errorMessage)
            .success(false)
            .build();

    return SearchResult.builder().metadata(metadata).build();
  }

  /** Clear the search result cache. */
  public void clearCache() {
    cache.clear();
    log.info("Search result cache cleared");
  }

  /** Get the current cache size. */
  public int getCacheSize() {
    // Remove expired entries first
    cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    return cache.size();
  }

  /** Cached search result with expiration time. */
  private static class CachedResult {
    private final SearchResult result;
    private final Instant expiresAt;

    public CachedResult(SearchResult result, Instant expiresAt) {
      this.result = result;
      this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
      return Instant.now().isAfter(expiresAt);
    }
  }
}
