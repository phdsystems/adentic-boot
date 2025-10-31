package dev.adeengineer.adentic.tool.websearch.model;

import static org.junit.jupiter.api.Assertions.*;

import dev.adeengineer.adentic.tool.websearch.model.SearchRequest.SafeSearch;
import dev.adeengineer.adentic.tool.websearch.model.SearchRequest.TimeRange;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Comprehensive tests for Web Search model classes. */
@DisplayName("Web Search Models Tests")
class WebSearchModelsTest {

  @Nested
  @DisplayName("SearchProvider Tests")
  class SearchProviderTests {

    @Test
    @DisplayName("Should have all supported search providers")
    void testSearchProviders() {
      SearchProvider[] providers = SearchProvider.values();
      assertEquals(3, providers.length);
      assertNotNull(SearchProvider.valueOf("DUCKDUCKGO"));
      assertNotNull(SearchProvider.valueOf("GOOGLE"));
      assertNotNull(SearchProvider.valueOf("BING"));
    }

    @Test
    @DisplayName("Should throw exception for invalid provider")
    void testInvalidProvider() {
      assertThrows(IllegalArgumentException.class, () -> SearchProvider.valueOf("YAHOO"));
    }

    @Test
    @DisplayName("Should return correct name")
    void testProviderName() {
      assertEquals("DUCKDUCKGO", SearchProvider.DUCKDUCKGO.name());
      assertEquals("GOOGLE", SearchProvider.GOOGLE.name());
      assertEquals("BING", SearchProvider.BING.name());
    }
  }

  @Nested
  @DisplayName("SearchRequest Tests")
  class SearchRequestTests {

    @Test
    @DisplayName("Should create request with builder")
    void testBuilder() {
      SearchRequest request =
          SearchRequest.builder()
              .query("test query")
              .maxResults(20)
              .provider(SearchProvider.GOOGLE)
              .region("en-GB")
              .safeSearch(SafeSearch.STRICT)
              .timeRange(TimeRange.WEEK)
              .build();

      assertEquals("test query", request.getQuery());
      assertEquals(20, request.getMaxResults());
      assertEquals(SearchProvider.GOOGLE, request.getProvider());
      assertEquals("en-GB", request.getRegion());
      assertEquals(SafeSearch.STRICT, request.getSafeSearch());
      assertEquals(TimeRange.WEEK, request.getTimeRange());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      SearchRequest request = SearchRequest.builder().query("test").build();

      assertEquals("test", request.getQuery());
      assertEquals(10, request.getMaxResults());
      assertNull(request.getProvider());
      assertEquals("en-US", request.getRegion());
      assertEquals(SafeSearch.MODERATE, request.getSafeSearch());
      assertNull(request.getTimeRange());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      SearchRequest request = SearchRequest.builder().query("initial").build();

      request.setQuery("updated query");
      request.setMaxResults(50);
      request.setProvider(SearchProvider.BING);
      request.setRegion("de-DE");
      request.setSafeSearch(SafeSearch.OFF);
      request.setTimeRange(TimeRange.MONTH);

      assertEquals("updated query", request.getQuery());
      assertEquals(50, request.getMaxResults());
      assertEquals(SearchProvider.BING, request.getProvider());
      assertEquals("de-DE", request.getRegion());
      assertEquals(SafeSearch.OFF, request.getSafeSearch());
      assertEquals(TimeRange.MONTH, request.getTimeRange());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      SearchRequest request1 =
          SearchRequest.builder()
              .query("test")
              .maxResults(10)
              .region("en-US")
              .safeSearch(SafeSearch.MODERATE)
              .build();

      SearchRequest request2 =
          SearchRequest.builder()
              .query("test")
              .maxResults(10)
              .region("en-US")
              .safeSearch(SafeSearch.MODERATE)
              .build();

      assertEquals(request1, request2);
      assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      SearchRequest request =
          SearchRequest.builder().query("test query").provider(SearchProvider.GOOGLE).build();

      String str = request.toString();
      assertTrue(str.contains("test query"));
      assertTrue(str.contains("GOOGLE"));
    }

    @Nested
    @DisplayName("SafeSearch Tests")
    class SafeSearchTests {

      @Test
      @DisplayName("Should have all safe search levels")
      void testSafeSearchLevels() {
        SafeSearch[] levels = SafeSearch.values();
        assertEquals(3, levels.length);
        assertNotNull(SafeSearch.valueOf("STRICT"));
        assertNotNull(SafeSearch.valueOf("MODERATE"));
        assertNotNull(SafeSearch.valueOf("OFF"));
      }

      @Test
      @DisplayName("Should throw exception for invalid safe search level")
      void testInvalidSafeSearchLevel() {
        assertThrows(IllegalArgumentException.class, () -> SafeSearch.valueOf("INVALID"));
      }

      @Test
      @DisplayName("Should return correct name")
      void testSafeSearchName() {
        assertEquals("STRICT", SafeSearch.STRICT.name());
        assertEquals("MODERATE", SafeSearch.MODERATE.name());
        assertEquals("OFF", SafeSearch.OFF.name());
      }
    }

    @Nested
    @DisplayName("TimeRange Tests")
    class TimeRangeTests {

      @Test
      @DisplayName("Should have all time ranges")
      void testTimeRanges() {
        TimeRange[] ranges = TimeRange.values();
        assertEquals(5, ranges.length);
        assertNotNull(TimeRange.valueOf("DAY"));
        assertNotNull(TimeRange.valueOf("WEEK"));
        assertNotNull(TimeRange.valueOf("MONTH"));
        assertNotNull(TimeRange.valueOf("YEAR"));
        assertNotNull(TimeRange.valueOf("ALL"));
      }

      @Test
      @DisplayName("Should throw exception for invalid time range")
      void testInvalidTimeRange() {
        assertThrows(IllegalArgumentException.class, () -> TimeRange.valueOf("DECADE"));
      }

      @Test
      @DisplayName("Should return correct name")
      void testTimeRangeName() {
        assertEquals("DAY", TimeRange.DAY.name());
        assertEquals("WEEK", TimeRange.WEEK.name());
        assertEquals("MONTH", TimeRange.MONTH.name());
        assertEquals("YEAR", TimeRange.YEAR.name());
        assertEquals("ALL", TimeRange.ALL.name());
      }
    }
  }

  @Nested
  @DisplayName("SearchMetadata Tests")
  class SearchMetadataTests {

    @Test
    @DisplayName("Should create metadata with builder")
    void testBuilder() {
      Instant now = Instant.now();
      SearchMetadata metadata =
          SearchMetadata.builder()
              .query("test query")
              .provider(SearchProvider.DUCKDUCKGO)
              .totalResults(1000L)
              .resultCount(10)
              .timestamp(now)
              .queryTime(Duration.ofMillis(250))
              .cached(true)
              .error(null)
              .success(true)
              .build();

      assertEquals("test query", metadata.getQuery());
      assertEquals(SearchProvider.DUCKDUCKGO, metadata.getProvider());
      assertEquals(1000L, metadata.getTotalResults());
      assertEquals(10, metadata.getResultCount());
      assertEquals(now, metadata.getTimestamp());
      assertEquals(Duration.ofMillis(250), metadata.getQueryTime());
      assertTrue(metadata.isCached());
      assertNull(metadata.getError());
      assertTrue(metadata.isSuccess());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      SearchMetadata metadata = SearchMetadata.builder().query("test").build();

      assertEquals("test", metadata.getQuery());
      assertNull(metadata.getProvider());
      assertNull(metadata.getTotalResults());
      assertEquals(0, metadata.getResultCount());
      assertNotNull(metadata.getTimestamp());
      assertNull(metadata.getQueryTime());
      assertFalse(metadata.isCached());
      assertNull(metadata.getError());
      assertTrue(metadata.isSuccess());
    }

    @Test
    @DisplayName("Should create error metadata")
    void testErrorMetadata() {
      SearchMetadata metadata =
          SearchMetadata.builder()
              .query("failed query")
              .provider(SearchProvider.GOOGLE)
              .error("API quota exceeded")
              .success(false)
              .build();

      assertEquals("failed query", metadata.getQuery());
      assertEquals(SearchProvider.GOOGLE, metadata.getProvider());
      assertEquals("API quota exceeded", metadata.getError());
      assertFalse(metadata.isSuccess());
    }

    @Test
    @DisplayName("Should support toBuilder")
    void testToBuilder() {
      SearchMetadata original =
          SearchMetadata.builder()
              .query("original")
              .provider(SearchProvider.BING)
              .resultCount(5)
              .success(true)
              .build();

      SearchMetadata modified = original.toBuilder().query("modified").resultCount(10).build();

      assertEquals("modified", modified.getQuery());
      assertEquals(SearchProvider.BING, modified.getProvider());
      assertEquals(10, modified.getResultCount());
      assertTrue(modified.isSuccess());

      // Original should be unchanged
      assertEquals("original", original.getQuery());
      assertEquals(5, original.getResultCount());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      SearchMetadata metadata = SearchMetadata.builder().query("initial").build();

      Instant newTimestamp = Instant.now();
      metadata.setQuery("updated");
      metadata.setProvider(SearchProvider.GOOGLE);
      metadata.setTotalResults(5000L);
      metadata.setResultCount(20);
      metadata.setTimestamp(newTimestamp);
      metadata.setQueryTime(Duration.ofMillis(500));
      metadata.setCached(true);
      metadata.setError("Some error");
      metadata.setSuccess(false);

      assertEquals("updated", metadata.getQuery());
      assertEquals(SearchProvider.GOOGLE, metadata.getProvider());
      assertEquals(5000L, metadata.getTotalResults());
      assertEquals(20, metadata.getResultCount());
      assertEquals(newTimestamp, metadata.getTimestamp());
      assertEquals(Duration.ofMillis(500), metadata.getQueryTime());
      assertTrue(metadata.isCached());
      assertEquals("Some error", metadata.getError());
      assertFalse(metadata.isSuccess());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      Instant timestamp = Instant.parse("2025-01-01T12:00:00Z");

      SearchMetadata metadata1 =
          SearchMetadata.builder()
              .query("test")
              .provider(SearchProvider.GOOGLE)
              .resultCount(10)
              .timestamp(timestamp)
              .success(true)
              .build();

      SearchMetadata metadata2 =
          SearchMetadata.builder()
              .query("test")
              .provider(SearchProvider.GOOGLE)
              .resultCount(10)
              .timestamp(timestamp)
              .success(true)
              .build();

      assertEquals(metadata1, metadata2);
      assertEquals(metadata1.hashCode(), metadata2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      SearchMetadata metadata =
          SearchMetadata.builder()
              .query("test query")
              .provider(SearchProvider.BING)
              .resultCount(15)
              .success(true)
              .build();

      String str = metadata.toString();
      assertTrue(str.contains("test query"));
      assertTrue(str.contains("BING"));
      assertTrue(str.contains("15"));
    }
  }

  @Nested
  @DisplayName("SearchItem Tests")
  class SearchItemTests {

    @Test
    @DisplayName("Should create item with builder")
    void testBuilder() {
      Map<String, Object> metadata = Map.of("source", "web", "score", 0.95);

      SearchItem item =
          SearchItem.builder()
              .title("Test Title")
              .url("https://example.com/page")
              .snippet("This is a test snippet")
              .displayUrl("example.com/page")
              .position(1)
              .metadata(metadata)
              .build();

      assertEquals("Test Title", item.getTitle());
      assertEquals("https://example.com/page", item.getUrl());
      assertEquals("This is a test snippet", item.getSnippet());
      assertEquals("example.com/page", item.getDisplayUrl());
      assertEquals(1, item.getPosition());
      assertEquals(metadata, item.getMetadata());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      SearchItem item = SearchItem.builder().title("Title").url("http://test.com").build();

      assertEquals("Title", item.getTitle());
      assertEquals("http://test.com", item.getUrl());
      assertNull(item.getSnippet());
      assertNull(item.getDisplayUrl());
      assertEquals(0, item.getPosition());
      assertNotNull(item.getMetadata());
      assertTrue(item.getMetadata().isEmpty());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      SearchItem item = SearchItem.builder().title("Initial").url("http://initial.com").build();

      Map<String, Object> newMetadata = new HashMap<>();
      newMetadata.put("key", "value");

      item.setTitle("Updated Title");
      item.setUrl("https://updated.com");
      item.setSnippet("Updated snippet");
      item.setDisplayUrl("updated.com");
      item.setPosition(5);
      item.setMetadata(newMetadata);

      assertEquals("Updated Title", item.getTitle());
      assertEquals("https://updated.com", item.getUrl());
      assertEquals("Updated snippet", item.getSnippet());
      assertEquals("updated.com", item.getDisplayUrl());
      assertEquals(5, item.getPosition());
      assertEquals(newMetadata, item.getMetadata());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      SearchItem item1 =
          SearchItem.builder()
              .title("Test")
              .url("http://test.com")
              .snippet("snippet")
              .position(1)
              .build();

      SearchItem item2 =
          SearchItem.builder()
              .title("Test")
              .url("http://test.com")
              .snippet("snippet")
              .position(1)
              .build();

      assertEquals(item1, item2);
      assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      SearchItem item =
          SearchItem.builder()
              .title("Example Title")
              .url("https://example.com")
              .snippet("Example snippet")
              .position(3)
              .build();

      String str = item.toString();
      assertTrue(str.contains("[3]"));
      assertTrue(str.contains("Example Title"));
      assertTrue(str.contains("https://example.com"));
      assertTrue(str.contains("Example snippet"));
    }

    @Test
    @DisplayName("Should handle null snippet in toString")
    void testToStringWithNullSnippet() {
      SearchItem item =
          SearchItem.builder().title("Title").url("http://test.com").position(1).build();

      String str = item.toString();
      assertTrue(str.contains("No description"));
    }
  }

  @Nested
  @DisplayName("SearchResult Tests")
  class SearchResultTests {

    @Test
    @DisplayName("Should create result with builder")
    void testBuilder() {
      SearchMetadata metadata =
          SearchMetadata.builder()
              .query("test")
              .provider(SearchProvider.GOOGLE)
              .resultCount(2)
              .success(true)
              .build();

      List<SearchItem> items = new ArrayList<>();
      items.add(SearchItem.builder().title("Item 1").url("http://test1.com").position(1).build());
      items.add(SearchItem.builder().title("Item 2").url("http://test2.com").position(2).build());

      SearchResult result = SearchResult.builder().metadata(metadata).items(items).build();

      assertEquals(metadata, result.getMetadata());
      assertEquals(items, result.getItems());
      assertEquals(2, result.getItems().size());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      SearchResult result = SearchResult.builder().build();

      assertNull(result.getMetadata());
      assertNotNull(result.getItems());
      assertTrue(result.getItems().isEmpty());
    }

    @Test
    @DisplayName("Should support toBuilder")
    void testToBuilder() {
      SearchMetadata metadata =
          SearchMetadata.builder().query("original").provider(SearchProvider.BING).build();

      SearchResult original = SearchResult.builder().metadata(metadata).build();

      SearchMetadata newMetadata =
          SearchMetadata.builder().query("modified").provider(SearchProvider.GOOGLE).build();

      SearchResult modified = original.toBuilder().metadata(newMetadata).build();

      assertEquals(newMetadata, modified.getMetadata());
      assertEquals("modified", modified.getMetadata().getQuery());

      // Original should be unchanged
      assertEquals(metadata, original.getMetadata());
      assertEquals("original", original.getMetadata().getQuery());
    }

    @Test
    @DisplayName("Should correctly identify success")
    void testIsSuccess() {
      SearchMetadata successMetadata = SearchMetadata.builder().query("test").success(true).build();
      SearchResult successResult = SearchResult.builder().metadata(successMetadata).build();
      assertTrue(successResult.isSuccess());

      SearchMetadata failureMetadata =
          SearchMetadata.builder().query("test").success(false).build();
      SearchResult failureResult = SearchResult.builder().metadata(failureMetadata).build();
      assertFalse(failureResult.isSuccess());

      SearchResult noMetadataResult = SearchResult.builder().build();
      assertFalse(noMetadataResult.isSuccess());
    }

    @Test
    @DisplayName("Should return correct result count")
    void testGetResultCount() {
      List<SearchItem> items = new ArrayList<>();
      items.add(SearchItem.builder().title("Item 1").url("http://test1.com").build());
      items.add(SearchItem.builder().title("Item 2").url("http://test2.com").build());
      items.add(SearchItem.builder().title("Item 3").url("http://test3.com").build());

      SearchResult result = SearchResult.builder().items(items).build();
      assertEquals(3, result.getResultCount());

      SearchResult emptyResult = SearchResult.builder().build();
      assertEquals(0, emptyResult.getResultCount());
    }

    @Test
    @DisplayName("Should correctly identify if has results")
    void testHasResults() {
      List<SearchItem> items = new ArrayList<>();
      items.add(SearchItem.builder().title("Item").url("http://test.com").build());

      SearchResult withResults = SearchResult.builder().items(items).build();
      assertTrue(withResults.hasResults());

      SearchResult emptyResult = SearchResult.builder().build();
      assertFalse(emptyResult.hasResults());

      SearchResult nullItems = SearchResult.builder().items(null).build();
      assertFalse(nullItems.hasResults());
    }

    @Test
    @DisplayName("Should generate summary for successful search with results")
    void testGetSummarySuccess() {
      SearchMetadata metadata =
          SearchMetadata.builder()
              .query("test query")
              .provider(SearchProvider.GOOGLE)
              .queryTime(Duration.ofMillis(150))
              .cached(false)
              .success(true)
              .build();

      List<SearchItem> items = new ArrayList<>();
      items.add(SearchItem.builder().title("Item 1").url("http://test1.com").build());
      items.add(SearchItem.builder().title("Item 2").url("http://test2.com").build());

      SearchResult result = SearchResult.builder().metadata(metadata).items(items).build();

      String summary = result.getSummary();
      assertTrue(summary.contains("Found 2 results"));
      assertTrue(summary.contains("test query"));
      assertTrue(summary.contains("GOOGLE"));
      assertTrue(summary.contains("150ms"));
      assertFalse(summary.contains("cached"));
    }

    @Test
    @DisplayName("Should generate summary for cached results")
    void testGetSummaryCached() {
      SearchMetadata metadata =
          SearchMetadata.builder()
              .query("cached query")
              .provider(SearchProvider.BING)
              .queryTime(Duration.ofMillis(5))
              .cached(true)
              .success(true)
              .build();

      List<SearchItem> items = new ArrayList<>();
      items.add(SearchItem.builder().title("Item").url("http://test.com").build());

      SearchResult result = SearchResult.builder().metadata(metadata).items(items).build();

      String summary = result.getSummary();
      assertTrue(summary.contains("cached"));
    }

    @Test
    @DisplayName("Should generate summary for failed search")
    void testGetSummaryFailure() {
      SearchMetadata metadata =
          SearchMetadata.builder()
              .query("failed query")
              .error("API key invalid")
              .success(false)
              .build();

      SearchResult result = SearchResult.builder().metadata(metadata).build();

      String summary = result.getSummary();
      assertTrue(summary.contains("Search failed"));
      assertTrue(summary.contains("API key invalid"));
    }

    @Test
    @DisplayName("Should generate summary for no results")
    void testGetSummaryNoResults() {
      SearchMetadata metadata =
          SearchMetadata.builder()
              .query("no results query")
              .provider(SearchProvider.DUCKDUCKGO)
              .success(true)
              .build();

      SearchResult result = SearchResult.builder().metadata(metadata).build();

      String summary = result.getSummary();
      assertTrue(summary.contains("No results found"));
      assertTrue(summary.contains("no results query"));
    }

    @Test
    @DisplayName("Should handle null metadata in summary")
    void testGetSummaryNullMetadata() {
      SearchResult result = SearchResult.builder().build();
      String summary = result.getSummary();
      assertTrue(summary.contains("Search failed"));
      assertTrue(summary.contains("Unknown error"));
    }

    @Test
    @DisplayName("Should generate detailed report")
    void testGetDetailedReport() {
      SearchMetadata metadata =
          SearchMetadata.builder()
              .query("test")
              .provider(SearchProvider.GOOGLE)
              .queryTime(Duration.ofMillis(100))
              .success(true)
              .build();

      List<SearchItem> items = new ArrayList<>();
      items.add(
          SearchItem.builder()
              .title("Item 1")
              .url("http://test1.com")
              .snippet("Snippet 1")
              .position(1)
              .build());
      items.add(
          SearchItem.builder()
              .title("Item 2")
              .url("http://test2.com")
              .snippet("Snippet 2")
              .position(2)
              .build());

      SearchResult result = SearchResult.builder().metadata(metadata).items(items).build();

      String report = result.getDetailedReport();
      assertTrue(report.contains("Found 2 results"));
      assertTrue(report.contains("Item 1"));
      assertTrue(report.contains("http://test1.com"));
      assertTrue(report.contains("Snippet 1"));
      assertTrue(report.contains("Item 2"));
      assertTrue(report.contains("http://test2.com"));
      assertTrue(report.contains("Snippet 2"));
    }

    @Test
    @DisplayName("Should generate detailed report with no results")
    void testGetDetailedReportNoResults() {
      SearchMetadata metadata = SearchMetadata.builder().query("empty").success(true).build();

      SearchResult result = SearchResult.builder().metadata(metadata).build();

      String report = result.getDetailedReport();
      assertTrue(report.contains("No results found"));
      assertFalse(report.contains("[1]"));
    }

    @Test
    @DisplayName("Should implement toString as summary")
    void testToString() {
      SearchMetadata metadata =
          SearchMetadata.builder()
              .query("test")
              .provider(SearchProvider.GOOGLE)
              .success(true)
              .build();

      List<SearchItem> items = new ArrayList<>();
      items.add(SearchItem.builder().title("Item").url("http://test.com").build());

      SearchResult result = SearchResult.builder().metadata(metadata).items(items).build();

      assertEquals(result.getSummary(), result.toString());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      SearchResult result = SearchResult.builder().build();

      SearchMetadata metadata = SearchMetadata.builder().query("new query").build();
      List<SearchItem> items = new ArrayList<>();
      items.add(SearchItem.builder().title("New Item").url("http://new.com").build());

      result.setMetadata(metadata);
      result.setItems(items);

      assertEquals(metadata, result.getMetadata());
      assertEquals(items, result.getItems());
      assertEquals(1, result.getItems().size());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      SearchMetadata metadata = SearchMetadata.builder().query("test").success(true).build();

      List<SearchItem> items = new ArrayList<>();
      items.add(SearchItem.builder().title("Item").url("http://test.com").build());

      SearchResult result1 = SearchResult.builder().metadata(metadata).items(items).build();

      SearchResult result2 = SearchResult.builder().metadata(metadata).items(items).build();

      assertEquals(result1, result2);
      assertEquals(result1.hashCode(), result2.hashCode());
    }
  }
}
