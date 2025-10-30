# Web Search Tool - Examples

**Version:** 0.1.0
**Date:** 2025-10-25

---

## Table of Contents

- [Basic Examples](#basic-examples)
- [Advanced Search](#advanced-search)
- [Configuration Examples](#configuration-examples)
- [Agent Integration](#agent-integration)
- [Async/Reactive Examples](#asyncreactive-examples)
- [Error Handling](#error-handling)
- [Caching Examples](#caching-examples)
- [Real-World Applications](#real-world-applications)
- [Testing Examples](#testing-examples)
- [Complete Applications](#complete-applications)

---

## Basic Examples

### Example 1: Simple Search

```java
import dev.adeengineer.adentic.tool.websearch.WebSearchTool;
import dev.adeengineer.adentic.tool.websearch.model.SearchResult;

@Component
public class SimpleSearchExample {

    @Inject
    private WebSearchTool searchTool;

    public void searchExample() {
        SearchResult result = searchTool
            .search("Java Spring Boot tutorials")
            .block();

        System.out.println(result.getSummary());
        // Output: ✅ Found 10 results for 'Java Spring Boot tutorials' using DUCKDUCKGO [1234ms]
    }
}
```

---

### Example 2: Display All Results

```java
public void displayResults() {
    SearchResult result = searchTool
        .search("machine learning basics")
        .block();

    if (result.isSuccess() && result.hasResults()) {
        System.out.println("Search Results:\n");

        for (SearchItem item : result.getItems()) {
            System.out.printf("[%d] %s\n", item.getPosition(), item.getTitle());
            System.out.printf("    %s\n", item.getUrl());
            System.out.printf("    %s\n\n", item.getSnippet());
        }
    } else {
        System.out.println("No results found or search failed");
    }
}
```

**Output:**

```
Search Results:

[1] Introduction to Machine Learning
    https://example.com/ml-intro
    Learn the fundamentals of machine learning including supervised...

[2] Machine Learning Tutorial for Beginners
    https://tutorial.com/ml-basics
    Step-by-step guide to understanding machine learning concepts...
```

---

### Example 3: Extract URLs Only

```java
public List<String> getSearchUrls(String query) {
    SearchResult result = searchTool.search(query).block();

    return result.hasResults()
        ? result.getItems().stream()
            .map(SearchItem::getUrl)
            .collect(Collectors.toList())
        : Collections.emptyList();
}

// Usage
List<String> urls = getSearchUrls("Python tutorials");
urls.forEach(System.out::println);
```

---

## Advanced Search

### Example 4: Custom Result Count

```java
public void advancedSearch() {
    SearchRequest request = SearchRequest.builder()
        .query("React best practices 2025")
        .maxResults(20)
        .build();

    SearchResult result = searchTool.search(request).block();

    System.out.println("Found " + result.getResultCount() + " results");
}
```

---

### Example 5: Region-Specific Search

```java
public void regionalSearch() {
    // Search for UK-specific results
    SearchRequest ukRequest = SearchRequest.builder()
        .query("weather forecast London")
        .region("en-GB")
        .build();

    // Search for US-specific results
    SearchRequest usRequest = SearchRequest.builder()
        .query("weather forecast New York")
        .region("en-US")
        .build();

    SearchResult ukResult = searchTool.search(ukRequest).block();
    SearchResult usResult = searchTool.search(usRequest).block();
}
```

---

### Example 6: Safe Search Levels

```java
public void safeSearchExample() {
    // Strict filtering
    SearchRequest strictRequest = SearchRequest.builder()
        .query("educational content")
        .safeSearch(SafeSearch.STRICT)
        .build();

    // No filtering
    SearchRequest offRequest = SearchRequest.builder()
        .query("unrestricted search")
        .safeSearch(SafeSearch.OFF)
        .build();

    SearchResult strictResult = searchTool.search(strictRequest).block();
    SearchResult offResult = searchTool.search(offRequest).block();
}
```

---

## Configuration Examples

### Example 7: Using Fast Configuration

```java
@Configuration
public class SearchConfiguration {

    @Bean
    public WebSearchTool fastSearchTool() {
        WebSearchTool tool = new WebSearchTool();
        tool.setConfig(WebSearchConfig.fast());
        return tool;
    }
}
```

---

### Example 8: Custom Configuration

```java
public void customConfigExample() {
    WebSearchConfig customConfig = WebSearchConfig.builder()
        .defaultProvider(SearchProvider.DUCKDUCKGO)
        .maxResults(15)
        .httpTimeoutMs(8000)
        .httpRetries(3)
        .cacheResults(true)
        .cacheTtlMs(600000)  // 10 minutes
        .defaultSafeSearch(SafeSearch.MODERATE)
        .defaultRegion("en-US")
        .build();

    searchTool.setConfig(customConfig);

    // Now searches use custom config
    SearchResult result = searchTool.search("query").block();
}
```

---

### Example 9: Disable Caching

```java
public void noCacheExample() {
    // Disable caching for always-fresh results
    searchTool.setConfig(WebSearchConfig.noCache());

    // These searches will never use cache
    SearchResult result1 = searchTool.search("stock price AAPL").block();
    SearchResult result2 = searchTool.search("current weather").block();
}
```

---

## Agent Integration

### Example 10: Research Agent

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

                // Extract context from top 5 results
                String context = results.getItems().stream()
                    .limit(5)
                    .map(item -> String.format(
                        "Title: %s\nURL: %s\nSnippet: %s",
                        item.getTitle(), item.getUrl(), item.getSnippet()
                    ))
                    .collect(Collectors.joining("\n\n---\n\n"));

                // Synthesize using LLM
                String prompt = String.format(
                    "Research topic: %s\n\n" +
                    "Search results:\n%s\n\n" +
                    "Please provide a comprehensive summary of the key findings.",
                    topic, context
                );

                return llm.generate(prompt);
            });
    }
}

// Usage
String summary = researchAgent.researchTopic("quantum computing").block();
System.out.println(summary);
```

---

### Example 11: Documentation Finder Agent

```java
@AgentService
public class DocFinderAgent {

    @Inject
    private WebSearchTool searchTool;

    public Mono<List<SearchItem>> findDocumentation(
        String technology,
        String topic
    ) {
        String query = String.format(
            "%s %s documentation tutorial examples",
            technology, topic
        );

        return searchTool.search(query)
            .map(result -> result.getItems().stream()
                .filter(item ->
                    item.getUrl().contains("docs") ||
                    item.getUrl().contains("documentation") ||
                    item.getUrl().contains("tutorial") ||
                    item.getTitle().toLowerCase().contains("documentation")
                )
                .collect(Collectors.toList())
            );
    }
}

// Usage
List<SearchItem> docs = docFinder
    .findDocumentation("Spring Boot", "security")
    .block();

docs.forEach(doc ->
    System.out.println(doc.getTitle() + " - " + doc.getUrl())
);
```

---

### Example 12: Fact Checker Agent

```java
@AgentService
public class FactCheckerAgent {

    @Inject
    private WebSearchTool searchTool;

    @Inject
    @LLM(name = "claude")
    private TextGenerationProvider llm;

    public Mono<FactCheckResult> verifyClaim(String claim) {
        return searchTool.search(claim)
            .flatMap(results -> {
                if (!results.hasResults()) {
                    return Mono.just(new FactCheckResult(
                        claim,
                        false,
                        "Insufficient information to verify"
                    ));
                }

                // Gather evidence
                String evidence = results.getItems().stream()
                    .limit(3)
                    .map(item -> String.format(
                        "Source: %s\n%s",
                        item.getUrl(), item.getSnippet()
                    ))
                    .collect(Collectors.joining("\n\n"));

                // Ask LLM to verify
                String prompt = String.format(
                    "Claim: %s\n\n" +
                    "Evidence:\n%s\n\n" +
                    "Is this claim supported by the evidence? " +
                    "Respond with: SUPPORTED, REFUTED, or INCONCLUSIVE",
                    claim, evidence
                );

                return llm.generate(prompt)
                    .map(verdict -> new FactCheckResult(
                        claim,
                        results.hasResults(),
                        verdict
                    ));
            });
    }
}

@Data
@AllArgsConstructor
class FactCheckResult {
    private String claim;
    private boolean evidenceFound;
    private String verdict;
}

// Usage
FactCheckResult result = factChecker
    .verifyClaim("The Earth is flat")
    .block();

System.out.println("Claim: " + result.getClaim());
System.out.println("Verdict: " + result.getVerdict());
```

---

## Async/Reactive Examples

### Example 13: Non-Blocking Search

```java
public void asyncSearchExample() {
    searchTool.search("Java tutorials")
        .subscribe(result -> {
            System.out.println("Search completed: " + result.getSummary());
            result.getItems().forEach(item ->
                System.out.println(" - " + item.getTitle())
            );
        });

    // Continue doing other work...
    System.out.println("Search started, continuing...");
}
```

---

### Example 14: Parallel Searches

```java
import reactor.core.publisher.Flux;

public Mono<Map<String, SearchResult>> searchMultipleTopics(
    List<String> topics
) {
    return Flux.fromIterable(topics)
        .flatMap(topic ->
            searchTool.search(topic)
                .map(result -> Map.entry(topic, result))
        )
        .collectMap(Map.Entry::getKey, Map.Entry::getValue);
}

// Usage
List<String> topics = List.of(
    "Java concurrency",
    "Python asyncio",
    "JavaScript promises"
);

Map<String, SearchResult> results = searchMultipleTopics(topics).block();

results.forEach((topic, result) ->
    System.out.println(topic + ": " + result.getResultCount() + " results")
);
```

---

### Example 15: Chaining Operations

```java
public Mono<String> searchAndExtractFirstUrl(String query) {
    return searchTool.search(query)
        .filter(SearchResult::hasResults)
        .map(result -> result.getItems().get(0).getUrl())
        .defaultIfEmpty("No results found");
}

// Usage
String url = searchAndExtractFirstUrl("Spring Boot documentation").block();
System.out.println("First result: " + url);
```

---

### Example 16: Timeout and Retry

```java
import reactor.util.retry.Retry;

public Mono<SearchResult> searchWithRetry(String query) {
    return searchTool.search(query)
        .timeout(Duration.ofSeconds(5))
        .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))
        .onErrorResume(error -> {
            log.error("Search failed after retries", error);
            return Mono.just(createEmptyResult(query));
        });
}
```

---

## Error Handling

### Example 17: Comprehensive Error Handling

```java
public SearchResult safeSearch(String query) {
    try {
        SearchResult result = searchTool.search(query).block();

        if (!result.isSuccess()) {
            log.error("Search failed: {}", result.getMetadata().getError());
            return createEmptyResult(query);
        }

        if (!result.hasResults()) {
            log.warn("No results found for query: {}", query);
            return result;
        }

        return result;

    } catch (Exception e) {
        log.error("Unexpected error during search: {}", query, e);
        return createEmptyResult(query);
    }
}

private SearchResult createEmptyResult(String query) {
    return SearchResult.builder()
        .metadata(SearchMetadata.builder()
            .query(query)
            .resultCount(0)
            .success(false)
            .build())
        .build();
}
```

---

### Example 18: Graceful Degradation

```java
public List<SearchItem> searchWithFallback(String query) {
    return searchTool.search(query)
        .onErrorResume(error -> {
            log.warn("Primary search failed, trying simpler query", error);
            // Try simpler query
            return searchTool.search(simplifyQuery(query));
        })
        .map(result -> result.hasResults()
            ? result.getItems()
            : Collections.<SearchItem>emptyList()
        )
        .onErrorReturn(Collections.emptyList())
        .block();
}

private String simplifyQuery(String query) {
    // Remove special characters, limit words
    return query.replaceAll("[^a-zA-Z0-9 ]", "")
        .split(" ")[0];  // Just first word
}
```

---

## Caching Examples

### Example 19: Cache Status Monitoring

```java
public void monitorCache() {
    // First search (cache miss)
    SearchResult result1 = searchTool.search("popular topic").block();
    System.out.println("First search cached: " +
        result1.getMetadata().isCached());  // false

    // Second search (cache hit)
    SearchResult result2 = searchTool.search("popular topic").block();
    System.out.println("Second search cached: " +
        result2.getMetadata().isCached());  // true

    // Check cache size
    int size = searchTool.getCacheSize();
    System.out.println("Cache entries: " + size);
}
```

---

### Example 20: Cache Management

```java
public void manageCacheExample() {
    // Enable caching
    searchTool.setConfig(WebSearchConfig.builder()
        .cacheResults(true)
        .cacheTtlMs(300000)  // 5 minutes
        .build());

    // Do some searches
    searchTool.search("topic 1").block();
    searchTool.search("topic 2").block();
    searchTool.search("topic 3").block();

    System.out.println("Cache size: " + searchTool.getCacheSize());

    // Clear cache
    searchTool.clearCache();

    System.out.println("Cache size after clear: " + searchTool.getCacheSize());
}
```

---

## Real-World Applications

### Example 21: News Aggregator

```java
@Service
public class NewsAggregator {

    @Inject
    private WebSearchTool searchTool;

    public Mono<List<NewsItem>> getLatestNews(String topic) {
        String query = topic + " news latest";

        return searchTool.search(query)
            .map(result -> result.getItems().stream()
                .filter(item ->
                    item.getUrl().contains("news") ||
                    item.getTitle().toLowerCase().contains("news")
                )
                .map(item -> new NewsItem(
                    item.getTitle(),
                    item.getUrl(),
                    item.getSnippet()
                ))
                .collect(Collectors.toList())
            );
    }
}

@Data
@AllArgsConstructor
class NewsItem {
    private String headline;
    private String url;
    private String summary;
}
```

---

### Example 22: Competitive Analysis Tool

```java
@Service
public class CompetitorAnalyzer {

    @Inject
    private WebSearchTool searchTool;

    public Mono<CompetitorReport> analyzeCompetitor(String companyName) {
        List<String> queries = List.of(
            companyName + " products features",
            companyName + " pricing",
            companyName + " reviews",
            companyName + " news"
        );

        return Flux.fromIterable(queries)
            .flatMap(query -> searchTool.search(query))
            .collectList()
            .map(results -> generateReport(companyName, results));
    }

    private CompetitorReport generateReport(
        String company,
        List<SearchResult> results
    ) {
        // Analyze results and create report
        return new CompetitorReport(
            company,
            extractProducts(results.get(0)),
            extractPricing(results.get(1)),
            extractReviews(results.get(2)),
            extractNews(results.get(3))
        );
    }
}
```

---

### Example 23: Tutorial Recommender

```java
@Service
public class TutorialRecommender {

    @Inject
    private WebSearchTool searchTool;

    public Mono<List<Tutorial>> recommendTutorials(
        String technology,
        String skillLevel
    ) {
        String query = String.format(
            "%s tutorial %s beginner guide",
            technology, skillLevel
        );

        return searchTool.search(query)
            .map(result -> result.getItems().stream()
                .filter(this::isTutorialSite)
                .map(item -> new Tutorial(
                    item.getTitle(),
                    item.getUrl(),
                    item.getSnippet(),
                    determineQuality(item)
                ))
                .sorted(Comparator.comparingInt(Tutorial::getQuality).reversed())
                .collect(Collectors.toList())
            );
    }

    private boolean isTutorialSite(SearchItem item) {
        String url = item.getUrl().toLowerCase();
        return url.contains("tutorial") ||
               url.contains("learn") ||
               url.contains("course") ||
               url.contains("docs");
    }

    private int determineQuality(SearchItem item) {
        // Quality score based on position and content
        int score = 10 - item.getPosition();
        if (item.getUrl().contains("official")) score += 5;
        if (item.getUrl().contains("docs")) score += 3;
        return score;
    }
}

@Data
@AllArgsConstructor
class Tutorial {
    private String title;
    private String url;
    private String description;
    private int quality;
}
```

---

## Testing Examples

### Example 24: Unit Testing

```java
@SpringBootTest
public class WebSearchToolTest {

    @Inject
    private WebSearchTool searchTool;

    @Test
    public void testSimpleSearch() {
        SearchResult result = searchTool
            .search("test query")
            .block();

        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testCaching() {
        // First search
        SearchResult result1 = searchTool.search("cache test").block();
        assertFalse(result1.getMetadata().isCached());

        // Second search (should be cached)
        SearchResult result2 = searchTool.search("cache test").block();
        assertTrue(result2.getMetadata().isCached());
    }

    @Test
    public void testEmptyQuery() {
        SearchResult result = searchTool.search("").block();
        assertFalse(result.isSuccess());
        assertNotNull(result.getMetadata().getError());
    }
}
```

---

## Complete Applications

### Example 25: Research Assistant Application

```java
@RestController
@RequestMapping("/api/research")
public class ResearchController {

    @Inject
    private WebSearchTool searchTool;

    @Inject
    @LLM(name = "claude")
    private TextGenerationProvider llm;

    @PostMapping("/query")
    public Mono<ResearchResponse> research(
        @RequestBody ResearchRequest request
    ) {
        return searchTool.search(request.getTopic())
            .flatMap(results -> synthesizeFindings(request.getTopic(), results));
    }

    private Mono<ResearchResponse> synthesizeFindings(
        String topic,
        SearchResult results
    ) {
        if (!results.hasResults()) {
            return Mono.just(new ResearchResponse(
                topic,
                "No information found",
                Collections.emptyList()
            ));
        }

        String context = results.getItems().stream()
            .limit(5)
            .map(item -> String.format("%s: %s",
                item.getTitle(), item.getSnippet()))
            .collect(Collectors.joining("\n"));

        String prompt = String.format(
            "Summarize key findings about %s based on:\n%s",
            topic, context
        );

        return llm.generate(prompt)
            .map(summary -> new ResearchResponse(
                topic,
                summary,
                results.getItems().stream()
                    .limit(5)
                    .map(item -> item.getUrl())
                    .collect(Collectors.toList())
            ));
    }
}

@Data
class ResearchRequest {
    private String topic;
}

@Data
@AllArgsConstructor
class ResearchResponse {
    private String topic;
    private String summary;
    private List<String> sources;
}
```

---

*Last Updated: 2025-10-25*
*Version: 0.1.0*
