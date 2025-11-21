package dev.engineeringlab.adentic.tool.websearch.config;

import dev.engineeringlab.adentic.tool.websearch.model.SearchProvider;
import dev.engineeringlab.adentic.tool.websearch.model.SearchRequest.SafeSearch;
import lombok.Builder;
import lombok.Data;

/** Configuration for WebSearchTool behavior. */
@Data
@Builder
public class WebSearchConfig {
  /** Default search provider. */
  @Builder.Default private SearchProvider defaultProvider = SearchProvider.DUCKDUCKGO;

  /** Maximum number of results to return per search. */
  @Builder.Default private int maxResults = 10;

  /** HTTP timeout in milliseconds. */
  @Builder.Default private int httpTimeoutMs = 10000;

  /** Number of retry attempts for failed HTTP requests. */
  @Builder.Default private int httpRetries = 2;

  /** Whether to cache search results. */
  @Builder.Default private boolean cacheResults = true;

  /** Cache TTL (time-to-live) in milliseconds. */
  @Builder.Default private long cacheTtlMs = 300000; // 5 minutes

  /** Default safe search level. */
  @Builder.Default private SafeSearch defaultSafeSearch = SafeSearch.MODERATE;

  /** Default region/language preference. */
  @Builder.Default private String defaultRegion = "en-US";

  /** User-Agent string for HTTP requests. */
  @Builder.Default private String userAgent = "Mozilla/5.0 (compatible; AdenticBot/1.0)";

  /** Google Custom Search API key (if using Google). */
  private String googleApiKey;

  /** Google Custom Search Engine ID (if using Google). */
  private String googleCseId;

  /** Bing Search API key (if using Bing). */
  private String bingApiKey;

  /**
   * Default configuration preset. - DuckDuckGo as provider (free, no API key) - 10 results max -
   * 10s timeout - 2 retries - 5-minute cache
   */
  public static WebSearchConfig defaults() {
    return WebSearchConfig.builder().build();
  }

  /**
   * Fast configuration preset. - DuckDuckGo as provider - 5 results max - 5s timeout - 1 retry -
   * Caching enabled
   */
  public static WebSearchConfig fast() {
    return WebSearchConfig.builder()
        .maxResults(5)
        .httpTimeoutMs(5000)
        .httpRetries(1)
        .cacheResults(true)
        .build();
  }

  /**
   * Thorough configuration preset. - DuckDuckGo as provider - 20 results max - 15s timeout - 3
   * retries - Caching enabled
   */
  public static WebSearchConfig thorough() {
    return WebSearchConfig.builder()
        .maxResults(20)
        .httpTimeoutMs(15000)
        .httpRetries(3)
        .cacheResults(true)
        .build();
  }

  /** Google Search configuration preset. Requires API key and CSE ID to be set separately. */
  public static WebSearchConfig google(String apiKey, String cseId) {
    return WebSearchConfig.builder()
        .defaultProvider(SearchProvider.GOOGLE)
        .googleApiKey(apiKey)
        .googleCseId(cseId)
        .build();
  }

  /** Bing Search configuration preset. Requires API key to be set separately. */
  public static WebSearchConfig bing(String apiKey) {
    return WebSearchConfig.builder()
        .defaultProvider(SearchProvider.BING)
        .bingApiKey(apiKey)
        .build();
  }

  /** No-cache configuration preset. Disables result caching for always-fresh results. */
  public static WebSearchConfig noCache() {
    return WebSearchConfig.builder().cacheResults(false).build();
  }
}
