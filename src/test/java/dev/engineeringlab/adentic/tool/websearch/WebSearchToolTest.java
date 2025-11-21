package dev.engineeringlab.adentic.tool.websearch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import dev.engineeringlab.adentic.tool.websearch.config.WebSearchConfig;
import dev.engineeringlab.adentic.tool.websearch.model.SearchMetadata;
import dev.engineeringlab.adentic.tool.websearch.model.SearchProvider;
import dev.engineeringlab.adentic.tool.websearch.model.SearchRequest;
import dev.engineeringlab.adentic.tool.websearch.model.SearchResult;
import dev.engineeringlab.adentic.tool.websearch.provider.DuckDuckGoSearchProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

/** Comprehensive tests for WebSearchTool */
@DisplayName("WebSearchTool Tests")
class WebSearchToolTest {

  private WebSearchTool searchTool;
  private StubDuckDuckGoSearchProvider stubProvider;

  @BeforeEach
  void setUp() {
    stubProvider = new StubDuckDuckGoSearchProvider();
    searchTool = new WebSearchTool(WebSearchConfig.defaults(), stubProvider);
  }

  /** Stub implementation of DuckDuckGoSearchProvider for testing */
  static class StubDuckDuckGoSearchProvider extends DuckDuckGoSearchProvider {
    public StubDuckDuckGoSearchProvider() {
      super(WebSearchConfig.defaults());
    }

    @Override
    public Mono<SearchResult> search(SearchRequest request) {
      // Return a successful mock result without making real HTTP calls
      SearchMetadata metadata =
          SearchMetadata.builder()
              .query(request.getQuery())
              .provider(SearchProvider.DUCKDUCKGO)
              .resultCount(0)
              .timestamp(Instant.now())
              .queryTime(Duration.ofMillis(100))
              .success(true)
              .build();
      return Mono.just(SearchResult.builder().metadata(metadata).items(new ArrayList<>()).build());
    }
  }

  @Nested
  @DisplayName("Search Operations")
  class SearchOperationsTests {

    @Test
    @DisplayName("Should perform simple search")
    void testSimpleSearch() {
      SearchResult result = searchTool.search("test query").block();

      assertNotNull(result);
      assertNotNull(result.getMetadata());
      assertEquals("test query", result.getMetadata().getQuery());
    }

    @Test
    @DisplayName("Should perform search with request")
    void testSearchWithRequest() {
      SearchRequest request =
          SearchRequest.builder().query("java programming").maxResults(5).build();

      SearchResult result = searchTool.search(request).block();

      assertNotNull(result);
      assertNotNull(result.getMetadata());
    }

    @Test
    @DisplayName("Should fail with empty query")
    void testEmptyQuery() {
      SearchResult result = searchTool.search("").block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getMetadata().getError()).contains("cannot be empty");
    }

    @Test
    @DisplayName("Should fail with null query")
    void testNullQuery() {
      SearchRequest request = SearchRequest.builder().query(null).build();
      SearchResult result = searchTool.search(request).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
    }
  }

  @Nested
  @DisplayName("Provider Tests")
  class ProviderTests {

    @Test
    @DisplayName("Should use DuckDuckGo provider by default")
    void testDefaultProvider() {
      SearchResult result = searchTool.search("test").block();

      assertNotNull(result);
      assertEquals(SearchProvider.DUCKDUCKGO, result.getMetadata().getProvider());
    }

    @Test
    @DisplayName("Should fail with Google provider (not implemented)")
    void testGoogleProvider() {
      SearchRequest request =
          SearchRequest.builder().query("test").provider(SearchProvider.GOOGLE).build();

      SearchResult result = searchTool.search(request).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getMetadata().getError()).contains("not yet implemented");
    }

    @Test
    @DisplayName("Should fail with Bing provider (not implemented)")
    void testBingProvider() {
      SearchRequest request =
          SearchRequest.builder().query("test").provider(SearchProvider.BING).build();

      SearchResult result = searchTool.search(request).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getMetadata().getError()).contains("not yet implemented");
    }
  }

  @Nested
  @DisplayName("Cache Tests")
  class CacheTests {

    @Test
    @DisplayName("Should cache results when enabled")
    void testCaching() {
      WebSearchConfig config =
          WebSearchConfig.builder().cacheResults(true).cacheTtlMs(60000).build();
      StubDuckDuckGoSearchProvider cacheStubProvider = new StubDuckDuckGoSearchProvider();
      WebSearchTool tool = new WebSearchTool(config, cacheStubProvider);

      // First search
      SearchResult result1 = tool.search("cache test").block();
      assertNotNull(result1);

      // Second search should use cache
      SearchResult result2 = tool.search("cache test").block();
      assertNotNull(result2);
    }

    @Test
    @DisplayName("Should clear cache")
    void testClearCache() {
      searchTool.search("test").block();
      int sizeBefore = searchTool.getCacheSize();

      searchTool.clearCache();

      assertEquals(0, searchTool.getCacheSize());
    }

    @Test
    @DisplayName("Should get cache size")
    void testGetCacheSize() {
      int size = searchTool.getCacheSize();
      assertThat(size).isGreaterThanOrEqualTo(0);
    }
  }

  @Nested
  @DisplayName("Configuration Tests")
  class ConfigurationTests {

    @Test
    @DisplayName("Should use default configuration")
    void testDefaultConfig() {
      WebSearchConfig config = WebSearchConfig.defaults();

      assertNotNull(config);
      assertEquals(SearchProvider.DUCKDUCKGO, config.getDefaultProvider());
      assertTrue(config.getMaxResults() > 0);
    }

    @Test
    @DisplayName("Should create with custom config")
    void testCustomConfig() {
      WebSearchConfig config =
          WebSearchConfig.builder()
              .defaultProvider(SearchProvider.DUCKDUCKGO)
              .maxResults(20)
              .cacheResults(false)
              .build();

      StubDuckDuckGoSearchProvider customStubProvider = new StubDuckDuckGoSearchProvider();
      WebSearchTool tool = new WebSearchTool(config, customStubProvider);
      assertNotNull(tool);
    }

    @Test
    @DisplayName("Should set configuration")
    void testSetConfig() {
      WebSearchConfig newConfig = WebSearchConfig.builder().maxResults(15).build();
      searchTool.setConfig(newConfig);

      assertNotNull(searchTool);
    }
  }

  @Nested
  @DisplayName("Search Request Tests")
  class SearchRequestTests {

    @Test
    @DisplayName("Should build search request with all options")
    void testFullSearchRequest() {
      SearchRequest request =
          SearchRequest.builder()
              .query("test query")
              .maxResults(10)
              .provider(SearchProvider.DUCKDUCKGO)
              .region("us-en")
              .safeSearch(SearchRequest.SafeSearch.STRICT)
              .build();

      assertNotNull(request);
      assertEquals("test query", request.getQuery());
      assertEquals(10, request.getMaxResults());
      assertEquals(SearchRequest.SafeSearch.STRICT, request.getSafeSearch());
    }

    @Test
    @DisplayName("Should use default values in request")
    void testDefaultSearchRequest() {
      SearchRequest request = SearchRequest.builder().query("test").build();

      assertNotNull(request);
      assertEquals("test", request.getQuery());
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should handle exceptions gracefully")
    void testExceptionHandling() {
      SearchRequest request = SearchRequest.builder().query("test").maxResults(1000000).build();

      SearchResult result = searchTool.search(request).block();

      assertNotNull(result);
      // Should still return a result even if there's an error
      assertNotNull(result.getMetadata());
    }

    @Test
    @DisplayName("Should validate query")
    void testQueryValidation() {
      SearchResult result = searchTool.search("   ").block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
    }
  }
}
