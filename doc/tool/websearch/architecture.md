# Web Search Tool - Architecture

**Version:** 0.1.0
**Date:** 2025-10-25

---

## Table of Contents

- [Overview](#overview)
- [Design Principles](#design-principles)
- [Component Architecture](#component-architecture)
- [Data Flow](#data-flow)
- [Search Providers](#search-providers)
- [Caching Strategy](#caching-strategy)
- [Error Handling](#error-handling)
- [Extensibility](#extensibility)
- [Performance Considerations](#performance-considerations)

---

## Overview

The Web Search Tool is designed as a **Tool Provider** in the Adentic Framework, enabling AI agents to perform web searches through multiple search providers.

**Core Goals:**
1. **Simple API** - Easy-to-use search interface
2. **Provider Agnostic** - Support multiple search engines
3. **Performance** - Caching, async operations, timeouts
4. **Agent-Ready** - Injectable, reactive, composable
5. **Extensible** - Easy to add new providers

---

## Design Principles

### 1. Async/Reactive

All search operations return `Mono<SearchResult>` for non-blocking execution:

```java
public Mono<SearchResult> search(String query) {
    return search(SearchRequest.builder()
        .query(query)
        .build());
}
```

**Benefits:**
- Non-blocking I/O
- Composable with other reactive operations
- Efficient resource usage
- Scalable for high-concurrency scenarios

### 2. Stateless Design

The tool maintains no per-request state (except optional cache):

```java
@Tool(name = "web-search")
public class WebSearchTool {
    @Setter
    private WebSearchConfig config;  // Configuration (can be changed)

    private final Map<String, CachedResult> cache;  // Optional cache (thread-safe)
    private DuckDuckGoSearchProvider duckDuckGoProvider;
}
```

**Benefits:**
- Thread-safe
- Can be used by multiple agents concurrently
- Predictable behavior
- Easy to test

### 3. Separation of Concerns

Clear separation between:
- **Tool** - Public API, orchestration, caching
- **Providers** - Search engine-specific logic
- **Models** - Data structures
- **Config** - Behavior customization

### 4. Configuration-Driven

Behavior is controlled via `WebSearchConfig`:

```java
WebSearchConfig config = WebSearchConfig.builder()
    .maxResults(10)
    .httpTimeoutMs(10000)
    .cacheResults(true)
    .build();
```

---

## Component Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────┐
│                  AI Agent / Service                  │
└───────────────────┬─────────────────────────────────┘
                    │ @Inject
                    ▼
┌─────────────────────────────────────────────────────┐
│               WebSearchTool (@Tool)                  │
│  - search(String query): Mono<SearchResult>         │
│  - search(SearchRequest): Mono<SearchResult>        │
│  - clearCache()                                      │
└─────┬─────────────┬─────────────┬───────────────────┘
      │             │             │
      ▼             ▼             ▼
┌─────────────┐ ┌──────────┐ ┌──────────┐
│ DuckDuckGo  │ │  Google  │ │   Bing   │
│  Provider   │ │ Provider │ │ Provider │
│             │ │ (planned)│ │ (planned)│
└─────────────┘ └──────────┘ └──────────┘
      │
      ▼
┌─────────────────────────────────────────────────────┐
│                  Search Results                      │
│  SearchResult → SearchMetadata + List<SearchItem>   │
└─────────────────────────────────────────────────────┘
```

### Component Details

#### 1. WebSearchTool

**Responsibilities:**
- Public API for search operations
- Provider selection and routing
- Result caching
- Configuration management

**Key Methods:**

```java
Mono<SearchResult> search(String query)
Mono<SearchResult> search(SearchRequest request)
void clearCache()
int getCacheSize()
```

#### 2. Search Providers

**DuckDuckGoSearchProvider:**
- HTML-based search (no API key)
- HTTP POST to `https://html.duckduckgo.com/html/`
- Regex parsing of HTML response
- Retry logic with exponential backoff

**Future Providers:**
- GoogleSearchProvider (Custom Search API)
- BingSearchProvider (Bing Search API)

#### 3. Models

**SearchRequest:**
- Query string
- Max results
- Provider preference
- Region/language
- Safe search level
- Time range

**SearchResult:**
- Metadata (query, provider, timing, success)
- List of SearchItems
- Formatted output methods

**SearchItem:**
- Title
- URL
- Snippet/description
- Display URL
- Position
- Metadata map

#### 4. Configuration

**WebSearchConfig:**
- Default provider
- Max results
- HTTP timeout
- Retry attempts
- Cache settings
- API keys (for Google/Bing)

---

## Data Flow

### Simple Search Flow

```
1. Agent calls: searchTool.search("Java tutorials")
                    ↓
2. WebSearchTool creates SearchRequest with defaults
                    ↓
3. Check cache for existing result
   ├─ HIT → Return cached result (mark as cached)
   └─ MISS → Continue to provider
                    ↓
4. Route to appropriate provider (DuckDuckGo)
                    ↓
5. DuckDuckGoSearchProvider:
   - Build HTTP request
   - Execute with timeout/retry
   - Parse HTML response
   - Extract SearchItems
   - Build SearchResult
                    ↓
6. WebSearchTool caches result (if enabled)
                    ↓
7. Return Mono<SearchResult> to agent
```

### Advanced Search Flow

```
1. Agent creates SearchRequest with custom params
                    ↓
2. WebSearchTool validates request
                    ↓
3. Build cache key from request params
                    ↓
4. Check cache (same as simple flow)
                    ↓
5. Route to provider based on request.provider
                    ↓
6. Provider executes search with custom params
                    ↓
7. Cache and return result
```

---

## Search Providers

### DuckDuckGo Provider

**Implementation Strategy:**
- HTML scraping (no API key required)
- POST request with form data
- Regex-based parsing

**URL:** `https://html.duckduckgo.com/html/`

**Request:**

```http
POST /html/ HTTP/1.1
Host: html.duckduckgo.com
Content-Type: application/x-www-form-urlencoded
User-Agent: Mozilla/5.0 (compatible; AdenticBot/1.0)

q=java+tutorials&b=&kl=en-US
```

**Parsing Strategy:**

```java
// Result links
Pattern RESULT_PATTERN = Pattern.compile(
    "<a[^>]*class=\"result__a\"[^>]*href=\"(.*?)\"[^>]*>(.*?)</a>",
    Pattern.DOTALL
);

// Snippets
Pattern SNIPPET_PATTERN = Pattern.compile(
    "<a[^>]*class=\"result__snippet\"[^>]*>(.*?)</a>",
    Pattern.DOTALL
);
```

**Error Handling:**
- HTTP timeout → Retry with exponential backoff
- Parse failure → Return error result
- 4xx/5xx status → Return error result

### Google Provider (Planned)

**Implementation Strategy:**
- Google Custom Search JSON API
- REST API with API key
- JSON parsing

**Requirements:**
- Google API key
- Custom Search Engine ID

**API Endpoint:**

```
GET https://www.googleapis.com/customsearch/v1
  ?key={API_KEY}
  &cx={CSE_ID}
  &q={query}
  &num={maxResults}
```

### Bing Provider (Planned)

**Implementation Strategy:**
- Bing Search API v7
- REST API with subscription key
- JSON parsing

**Requirements:**
- Bing API subscription key

**API Endpoint:**

```
GET https://api.bing.microsoft.com/v7.0/search
  ?q={query}
  &count={maxResults}
  &mkt={region}
```

---

## Caching Strategy

### Cache Implementation

**Data Structure:**

```java
private final Map<String, CachedResult> cache = new ConcurrentHashMap<>();

private static class CachedResult {
    private final SearchResult result;
    private final Instant expiresAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
```

### Cache Key

Cache key includes all parameters that affect results:

```java
String cacheKey = String.format("%s:%s:%d:%s",
    provider,      // DUCKDUCKGO, GOOGLE, BING
    query,         // "java tutorials"
    maxResults,    // 10
    region         // "en-US"
);
```

### Cache Behavior

**Cache Hit:**
1. Check if key exists in cache
2. Check if entry is expired
3. If valid → Return cached result (mark `cached=true`)
4. If expired → Remove entry, continue to search

**Cache Miss:**
1. Execute search
2. Store result with expiration time
3. Return result

**Cache Management:**

```java
// Clear entire cache
searchTool.clearCache();

// Get cache size (removes expired entries)
int size = searchTool.getCacheSize();
```

### TTL (Time-To-Live)

**Default:** 5 minutes (300,000 ms)

**Rationale:**
- Search results change slowly
- 5 minutes balances freshness vs performance
- Configurable per use case

---

## Error Handling

### Error Scenarios

1. **Empty Query**
   - Detection: Before provider call
   - Response: Error SearchResult with message
2. **HTTP Timeout**
   - Detection: During HTTP request
   - Response: Retry with exponential backoff
   - Final: Error SearchResult after retries exhausted
3. **Parse Failure**
   - Detection: During HTML/JSON parsing
   - Response: Log warning, return error SearchResult
4. **Provider Not Implemented**
   - Detection: When routing to Google/Bing
   - Response: Error SearchResult with helpful message
5. **Network Error**
   - Detection: During HTTP request
   - Response: Retry, then error SearchResult

### Error Result Format

```java
SearchResult errorResult = SearchResult.builder()
    .metadata(SearchMetadata.builder()
        .query(query)
        .provider(provider)
        .resultCount(0)
        .error("Error message here")
        .success(false)
        .build())
    .items(new ArrayList<>())
    .build();
```

---

## Extensibility

### Adding a New Provider

**Steps:**

1. **Create Provider Class:**

```java
public class NewSearchProvider {
    private final WebSearchConfig config;

    public Mono<SearchResult> search(SearchRequest request) {
        // Implementation
    }
}
```

2. **Add to SearchProvider Enum:**

```java
public enum SearchProvider {
    DUCKDUCKGO,
    GOOGLE,
    BING,
    NEWSEARCH  // Add new provider
}
```

3. **Update WebSearchTool Routing:**

```java
Mono<SearchResult> searchMono = switch (provider) {
    case DUCKDUCKGO -> searchWithDuckDuckGo(request);
    case GOOGLE -> searchWithGoogle(request);
    case BING -> searchWithBing(request);
    case NEWSEARCH -> searchWithNewSearch(request);  // Add routing
};
```

4. **Add Configuration (if needed):**

```java
@Builder
public class WebSearchConfig {
    // ...existing fields...
    private String newSearchApiKey;  // Add API key field

    public static WebSearchConfig newSearch(String apiKey) {
        return builder()
            .defaultProvider(SearchProvider.NEWSEARCH)
            .newSearchApiKey(apiKey)
            .build();
    }
}
```

---

## Performance Considerations

### Optimization Strategies

1. **Caching**
   - Reduces redundant HTTP requests
   - 5-minute TTL balances freshness
   - Concurrent map for thread safety
2. **Async/Reactive**
   - Non-blocking I/O
   - Parallel searches possible
   - Efficient resource usage
3. **Timeouts**
   - Prevent hanging requests
   - Configurable per use case
   - Default: 10 seconds
4. **Retries**
   - Exponential backoff (500ms, 1000ms, 1500ms)
   - Configurable retry count
   - Default: 2 retries
5. **Result Limiting**
   - Max results caps parsing work
   - Faster response times
   - Default: 10 results

### Benchmarks

|             Scenario             | Cache |   Time   |      Notes      |
|----------------------------------|-------|----------|-----------------|
| First search                     | Miss  | ~1,200ms | Network + parse |
| Repeat search                    | Hit   | ~12ms    | Cache retrieval |
| Advanced search (20 results)     | Miss  | ~2,100ms | More parsing    |
| Parallel searches (5 concurrent) | Mixed | ~1,500ms | Network limited |

**Bottlenecks:**
1. Network latency (primary)
2. HTML parsing (secondary)
3. Cache lookup (negligible)

---

*Last Updated: 2025-10-25*
*Version: 0.1.0*
