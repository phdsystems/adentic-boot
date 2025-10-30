# Web Search Tool - Usage Guide

**Version:** 0.1.0
**Date:** 2025-10-25

---

## Table of Contents

- [Getting Started](#getting-started)
- [Basic Operations](#basic-operations)
- [Advanced Search](#advanced-search)
- [Configuration](#configuration)
- [Agent Integration](#agent-integration)
- [Async Operations](#async-operations)
- [Caching](#caching)
- [Error Handling](#error-handling)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)

---

## Getting Started

### Installation

The Web Search Tool is part of the `adentic-boot` module. No additional dependencies required.

### Basic Setup

```java
import dev.adeengineer.adentic.tool.websearch.WebSearchTool;
import dev.adeengineer.adentic.tool.websearch.model.*;

@Component
public class MyService {

    @Inject
    private WebSearchTool searchTool;

    public void performSearch() {
        SearchResult result = searchTool.search("Java tutorials").block();
        System.out.println(result.getSummary());
    }
}
```

---

## Basic Operations

### 1. Simple Search

Perform a basic web search with default settings:

```java
// Search with just a query string
SearchResult result = searchTool
    .search("machine learning tutorials")
    .block();

// Check if successful
if (result.isSuccess() && result.hasResults()) {
    System.out.println("✅ " + result.getSummary());
} else {
    System.out.println("❌ Search failed or no results");
}
```

### 2. Display Results

```java
SearchResult result = searchTool.search("Spring Boot").block();

// Summary output
System.out.println(result.getSummary());
// Output: ✅ Found 10 results for 'Spring Boot' using DUCKDUCKGO [1234ms]

// Detailed report
System.out.println(result.getDetailedReport());
// Output:
// ✅ Found 10 results for 'Spring Boot' using DUCKDUCKGO [1234ms]
//
// [1] Spring Boot Official Documentation
//     URL: https://spring.io/projects/spring-boot
//     Learn about Spring Boot framework...
//
// [2] Spring Boot Tutorial - Baeldung
//     URL: https://www.baeldung.com/spring-boot
//     Comprehensive guide to Spring Boot...
```

### 3. Access Individual Results

```java
SearchResult result = searchTool.search("Java design patterns").block();

if (result.hasResults()) {
    for (SearchItem item : result.getItems()) {
        System.out.printf("[%d] %s\n", item.getPosition(), item.getTitle());
        System.out.printf("    URL: %s\n", item.getUrl());
        System.out.printf("    %s\n\n", item.getSnippet());
    }
}
```

### 4. Get Metadata

```java
SearchResult result = searchTool.search("Python tutorials").block();
SearchMetadata metadata = result.getMetadata();

System.out.println("Query: " + metadata.getQuery());
System.out.println("Provider: " + metadata.getProvider());
System.out.println("Result Count: " + metadata.getResultCount());
System.out.println("Query Time: " + metadata.getQueryTime().toMillis() + "ms");
System.out.println("Cached: " + metadata.isCached());
System.out.println("Timestamp: " + metadata.getTimestamp());
```

---

## Advanced Search

### 1. Custom Result Count

```java
// Get more results
SearchRequest request = SearchRequest.builder()
    .query("Java concurrency")
    .maxResults(20)
    .build();

SearchResult result = searchTool.search(request).block();
```

### 2. Provider Selection

```java
// Explicitly choose DuckDuckGo
SearchRequest request = SearchRequest.builder()
    .query("React best practices")
    .provider(SearchProvider.DUCKDUCKGO)
    .build();

SearchResult result = searchTool.search(request).block();
```

### 3. Region/Language Preferences

```java
// Search with specific region
SearchRequest request = SearchRequest.builder()
    .query("weather forecast")
    .region("en-GB")  // UK English
    .build();

SearchResult result = searchTool.search(request).block();
```

### 4. Safe Search

```java
import dev.adeengineer.adentic.tool.websearch.model.SearchRequest.SafeSearch;

// Strict safe search
SearchRequest request = SearchRequest.builder()
    .query("educational content")
    .safeSearch(SafeSearch.STRICT)
    .build();

// Moderate safe search (default)
SearchRequest request2 = SearchRequest.builder()
    .query("general topic")
    .safeSearch(SafeSearch.MODERATE)
    .build();

// Safe search off
SearchRequest request3 = SearchRequest.builder()
    .query("unrestricted search")
    .safeSearch(SafeSearch.OFF)
    .build();
```

### 5. Complete Advanced Example

```java
SearchRequest request = SearchRequest.builder()
    .query("Spring Boot microservices")
    .maxResults(15)
    .provider(SearchProvider.DUCKDUCKGO)
    .region("en-US")
    .safeSearch(SafeSearch.MODERATE)
    .build();

SearchResult result = searchTool.search(request).block();

if (result.isSuccess()) {
    System.out.println("Found " + result.getResultCount() + " results");
    result.getItems().forEach(item ->
        System.out.println(item.getTitle() + " - " + item.getUrl())
    );
}
```

---

## Configuration

### 1. Using Presets

```java
// Default configuration (10 results, 10s timeout, caching)
searchTool.setConfig(WebSearchConfig.defaults());

// Fast configuration (5 results, 5s timeout)
searchTool.setConfig(WebSearchConfig.fast());

// Thorough configuration (20 results, 15s timeout, 3 retries)
searchTool.setConfig(WebSearchConfig.thorough());

// No caching (always fresh results)
searchTool.setConfig(WebSearchConfig.noCache());
```

### 2. Custom Configuration

```java
WebSearchConfig customConfig = WebSearchConfig.builder()
    .defaultProvider(SearchProvider.DUCKDUCKGO)
    .maxResults(15)
    .httpTimeoutMs(8000)
    .httpRetries(3)
    .cacheResults(true)
    .cacheTtlMs(600000)  // 10 minutes
    .defaultSafeSearch(SafeSearch.MODERATE)
    .defaultRegion("en-US")
    .userAgent("MyCustomBot/1.0")
    .build();

searchTool.setConfig(customConfig);
```

### 3. Per-Search Configuration

```java
// Override config for specific search
WebSearchTool customTool = new WebSearchTool(
    WebSearchConfig.builder()
        .maxResults(50)
        .httpTimeoutMs(15000)
        .build()
);

SearchResult result = customTool.search("comprehensive search").block();
```

---

## Agent Integration

### 1. Research Agent

```java
@AgentService
public class ResearchAgent {

    @Inject
    private WebSearchTool searchTool;

    @Inject
    @LLM(name = "claude")
    private TextGenerationProvider llm;

    public Mono<String> researchTopic(String topic) {
        return searchTool.search(topic)
            .flatMap(results -> {
                if (!results.hasResults()) {
                    return Mono.just("No information found for: " + topic);
                }

                // Build context from top results
                String context = results.getItems().stream()
                    .limit(5)
                    .map(item -> String.format("Title: %s\nSnippet: %s",
                        item.getTitle(), item.getSnippet()))
                    .collect(Collectors.joining("\n\n"));

                // Synthesize using LLM
                String prompt = String.format(
                    "Based on these search results, summarize key information about %s:\n\n%s",
                    topic, context
                );

                return llm.generate(prompt);
            });
    }
}
```

### 2. Documentation Helper Agent

```java
@AgentService
public class DocHelperAgent {

    @Inject
    private WebSearchTool searchTool;

    public Mono<List<String>> findDocumentation(String technology, String topic) {
        String query = String.format("%s %s documentation tutorial examples",
            technology, topic);

        return searchTool.search(query)
            .map(result -> result.getItems().stream()
                .filter(item -> item.getUrl().contains("docs") ||
                               item.getUrl().contains("tutorial"))
                .map(SearchItem::getUrl)
                .collect(Collectors.toList())
            );
    }
}
```

### 3. Fact Verification Agent

```java
@AgentService
public class FactCheckerAgent {

    @Inject
    private WebSearchTool searchTool;

    @Inject
    @LLM(name = "claude")
    private TextGenerationProvider llm;

    public Mono<String> verifyClaim(String claim) {
        return searchTool.search(claim)
            .flatMap(results -> {
                if (!results.hasResults()) {
                    return Mono.just("Insufficient information to verify claim.");
                }

                // Analyze top results
                String evidence = results.getItems().stream()
                    .limit(3)
                    .map(item -> item.getSnippet())
                    .collect(Collectors.joining("\n"));

                String prompt = String.format(
                    "Claim: %s\n\nEvidence from search:\n%s\n\nIs this claim supported?",
                    claim, evidence
                );

                return llm.generate(prompt);
            });
    }
}
```

---

## Async Operations

### 1. Non-Blocking Search

```java
// Don't block - use reactive operators
searchTool.search("Java tutorials")
    .subscribe(result -> {
        System.out.println("Search completed: " + result.getSummary());
    });

// Continue doing other work...
```

### 2. Parallel Searches

```java
import reactor.core.publisher.Flux;

public Mono<Map<String, SearchResult>> searchMultipleTopics(List<String> topics) {
    return Flux.fromIterable(topics)
        .flatMap(topic ->
            searchTool.search(topic)
                .map(result -> Map.entry(topic, result))
        )
        .collectMap(Map.Entry::getKey, Map.Entry::getValue);
}

// Usage
List<String> topics = List.of("Java", "Python", "JavaScript");
Map<String, SearchResult> results = searchMultipleTopics(topics).block();
```

### 3. Combining with Other Operations

```java
public Mono<String> searchAndSummarize(String query) {
    return searchTool.search(query)
        .filter(SearchResult::hasResults)
        .map(result -> result.getItems().get(0).getSnippet())
        .defaultIfEmpty("No results found");
}
```

### 4. Error Recovery

```java
public Mono<SearchResult> searchWithFallback(String query) {
    return searchTool.search(query)
        .onErrorResume(error -> {
            log.error("Primary search failed, trying alternate query", error);
            return searchTool.search(query + " tutorial");
        })
        .onErrorReturn(SearchResult.builder().build());  // Empty result as last resort
}
```

---

## Caching

### 1. Check Cache Status

```java
SearchResult result = searchTool.search("popular topic").block();

if (result.getMetadata().isCached()) {
    System.out.println("✅ Result served from cache");
} else {
    System.out.println("🔍 Fresh search performed");
}
```

### 2. Clear Cache

```java
// Clear all cached results
searchTool.clearCache();

// Check cache size (also removes expired entries)
int cacheSize = searchTool.getCacheSize();
System.out.println("Cache contains " + cacheSize + " entries");
```

### 3. Disable Caching

```java
// Temporary disable
WebSearchConfig noCache = WebSearchConfig.noCache();
searchTool.setConfig(noCache);

// Or custom config without cache
WebSearchConfig config = WebSearchConfig.builder()
    .cacheResults(false)
    .build();
searchTool.setConfig(config);
```

### 4. Custom Cache TTL

```java
// Cache for 10 minutes instead of default 5
WebSearchConfig config = WebSearchConfig.builder()
    .cacheResults(true)
    .cacheTtlMs(600000)  // 10 minutes
    .build();
searchTool.setConfig(config);
```

---

## Error Handling

### 1. Check for Success

```java
SearchResult result = searchTool.search("query").block();

if (result.isSuccess()) {
    System.out.println("✅ Search succeeded");
    if (result.hasResults()) {
        // Process results
    } else {
        System.out.println("No results found");
    }
} else {
    System.out.println("❌ Search failed: " + result.getMetadata().getError());
}
```

### 2. Handle Empty Results

```java
SearchResult result = searchTool.search("very specific query").block();

if (!result.hasResults()) {
    System.out.println("No results found. Try a different query.");
} else {
    System.out.println("Found " + result.getResultCount() + " results");
}
```

### 3. Timeout Handling

```java
// Configure shorter timeout
searchTool.setConfig(WebSearchConfig.builder()
    .httpTimeoutMs(5000)  // 5 seconds
    .build());

SearchResult result = searchTool.search("query")
    .timeout(Duration.ofSeconds(6))  // Overall timeout
    .onErrorResume(TimeoutException.class, e -> {
        log.warn("Search timed out");
        return Mono.just(createEmptyResult());
    })
    .block();
```

### 4. Retry on Failure

```java
import reactor.util.retry.Retry;

SearchResult result = searchTool.search("query")
    .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
    .block();
```

---

## Best Practices

### 1. Use Appropriate Configuration

```java
// For interactive user queries - use fast config
searchTool.setConfig(WebSearchConfig.fast());

// For background research - use thorough config
searchTool.setConfig(WebSearchConfig.thorough());

// For always-current data (stock prices, weather) - disable cache
searchTool.setConfig(WebSearchConfig.noCache());
```

### 2. Limit Result Count

```java
// Only get what you need
SearchRequest request = SearchRequest.builder()
    .query("topic")
    .maxResults(5)  // Just top 5 results
    .build();
```

### 3. Use Caching for Repeated Queries

```java
// Enable caching for frequently searched topics
searchTool.setConfig(WebSearchConfig.builder()
    .cacheResults(true)
    .cacheTtlMs(300000)  // 5 minutes
    .build());

// These will be cached
searchTool.search("Java tutorials").block();
searchTool.search("Java tutorials").block();  // From cache
```

### 4. Handle Errors Gracefully

```java
public List<SearchItem> safeSearch(String query) {
    try {
        SearchResult result = searchTool.search(query).block();
        return result.hasResults() ? result.getItems() : Collections.emptyList();
    } catch (Exception e) {
        log.error("Search failed for query: {}", query, e);
        return Collections.emptyList();
    }
}
```

### 5. Log Search Activity

```java
SearchResult result = searchTool.search("important query").block();

log.info("Search completed: query='{}', provider={}, results={}, time={}ms, cached={}",
    result.getMetadata().getQuery(),
    result.getMetadata().getProvider(),
    result.getResultCount(),
    result.getMetadata().getQueryTime().toMillis(),
    result.getMetadata().isCached()
);
```

---

## Troubleshooting

### Problem: No Results Returned

**Possible Causes:**
1. Query too specific
2. Network connectivity issues
3. Search provider blocked/down

**Solutions:**

```java
// Try broader query
searchTool.search("general topic").block();

// Check provider status
SearchResult result = searchTool.search("test").block();
if (!result.isSuccess()) {
    System.err.println("Provider error: " + result.getMetadata().getError());
}
```

### Problem: Slow Searches

**Possible Causes:**
1. Network latency
2. Large result count
3. No caching

**Solutions:**

```java
// Enable caching
searchTool.setConfig(WebSearchConfig.builder()
    .cacheResults(true)
    .build());

// Reduce result count
searchTool.setConfig(WebSearchConfig.builder()
    .maxResults(5)
    .build());

// Use faster timeout
searchTool.setConfig(WebSearchConfig.fast());
```

### Problem: Cache Not Working

**Check:**

```java
// Verify caching is enabled
WebSearchConfig config = searchTool.getConfig();
System.out.println("Cache enabled: " + config.isCacheResults());

// Check cache size
int size = searchTool.getCacheSize();
System.out.println("Cache entries: " + size);

// Verify cache hits
SearchResult result = searchTool.search("test").block();
System.out.println("From cache: " + result.getMetadata().isCached());
```

### Problem: Timeout Errors

**Solutions:**

```java
// Increase timeout
searchTool.setConfig(WebSearchConfig.builder()
    .httpTimeoutMs(15000)  // 15 seconds
    .httpRetries(3)
    .build());

// Or use thorough config
searchTool.setConfig(WebSearchConfig.thorough());
```

---

*Last Updated: 2025-10-25*
*Version: 0.1.0*
