# Web Search Tool - LLM Integration Guide

**Version:** 0.1.0
**Category:** Tool Integration
**Status:** DuckDuckGo Fully Implemented
**Date:** 2025-10-25

---

## TL;DR

**Web Search Tool enables LLMs to search the web and retrieve current information**. Agents convert natural language queries to search requests, execute searches via DuckDuckGo/Google/Bing, cache results, and format responses for users. **Benefits**: Access to current information, multi-provider support, result caching, safe search controls. **Use cases**: Fact-checking, research, news retrieval, trend analysis.

---

## Table of Contents

- [Overview](#overview)
- [Integration Architecture](#integration-architecture)
- [Tool Registration](#tool-registration)
- [LLM Workflow Examples](#llm-workflow-examples)
- [Tool Descriptor Format](#tool-descriptor-format)
- [Parameter Mapping](#parameter-mapping)
- [Result Processing](#result-processing)
- [Caching Strategy](#caching-strategy)
- [Error Handling](#error-handling)
- [Use Cases](#use-cases)
- [Best Practices](#best-practices)

---

## Overview

The Web Search Tool integrates with LLM-based agents to provide web search capabilities through natural language. The tool handles search provider selection, result caching, and response formatting while the LLM focuses on query refinement and result presentation.

### Integration Flow

```
┌──────────────────────────────────────────────────────────────┐
│                         User Input                            │
│     "What are the latest developments in AI?"                │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                      LLM/AI Agent                             │
│  • Analyzes intent: "web search for current info"           │
│  • Refines query: "latest AI developments 2024"             │
│  • Selects tool: web-search                                 │
│  • Extracts parameters: query, maxResults                   │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                    WebSearchTool                              │
│  • Method: search(query)                                     │
│  • Provider: DuckDuckGo (no API key required)               │
│  • Cache check: 15-minute TTL                               │
│  • Execution: HTTP request to search API                    │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                     SearchResult                              │
│  • success: true                                             │
│  • results: [                                                │
│      {title, url, snippet, rank},                           │
│      ...                                                     │
│    ]                                                         │
│  • metadata: {query, provider, resultCount, cached}         │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                      LLM Response                             │
│  "Here are the latest AI developments:                       │
│   1. New multimodal models from OpenAI...                   │
│   2. Google announces Gemini improvements...                │
│   3. Meta releases open-source LLM...                       │
│   [Sources: TechCrunch, The Verge, ArXiv]"                  │
└──────────────────────────────────────────────────────────────┘
```

---

## Integration Architecture

### Component Roles

**1. LLM/AI Agent**
- Understands user information needs
- Refines search queries for better results
- Selects appropriate search provider
- Synthesizes and formats search results
- Cites sources appropriately

**2. WebSearchTool (@Tool)**
- Provides unified API for web search
- Routes to search provider (DuckDuckGo, Google, Bing)
- Manages result caching (15-minute TTL)
- Returns structured results (SearchResult)

**3. Search Provider (DuckDuckGo, etc.)**
- Executes web search via HTTP API
- Handles provider-specific rate limits
- Formats results to common structure

### Tool Registration

The Web Search Tool is registered via the `@Tool` annotation:

```java
@Tool(name = "web-search")
public class WebSearchTool {
    public Mono<SearchResult> search(String query) { ... }
    public Mono<SearchResult> search(SearchRequest request) { ... }
}
```

This makes it discoverable by the ToolProvider system for LLM agents.

---

## LLM Workflow Examples

### Example 1: Simple Search

**User Request:**

```
"What's the weather in Paris today?"
```

**LLM Analysis:**

```json
{
  "intent": "web_search",
  "query": "Paris weather today",
  "maxResults": 5
}
```

**Agent Code:**

```java
SearchResult result = webSearchTool.search("Paris weather today").block();

if (result.isSuccess()) {
    // LLM processes and synthesizes results
    String answer = synthesizeWeatherInfo(result.getResults());
    // "Current weather in Paris: 18°C, partly cloudy.
    //  High of 22°C, low of 14°C expected today."
}
```

**Response Structure:**

```json
{
  "success": true,
  "results": [
    {
      "title": "Paris Weather - Current Conditions",
      "url": "https://weather.com/weather/today/l/Paris",
      "snippet": "Current weather: 18°C, partly cloudy. High 22°C...",
      "rank": 1
    }
  ],
  "metadata": {
    "query": "Paris weather today",
    "provider": "DUCKDUCKGO",
    "resultCount": 5,
    "queryTime": "125ms",
    "cached": false
  }
}
```

### Example 2: Research Query

**User Request:**

```
"Explain the latest breakthroughs in quantum computing"
```

**LLM Analysis:**

```json
{
  "intent": "research_query",
  "query": "quantum computing breakthroughs 2024",
  "maxResults": 10,
  "region": "US"
}
```

**Agent Code:**

```java
SearchRequest request = SearchRequest.builder()
    .query("quantum computing breakthroughs 2024")
    .maxResults(10)
    .region("US")
    .build();

SearchResult result = webSearchTool.search(request).block();

// LLM synthesizes from multiple sources
List<String> keyPoints = extractKeyPoints(result.getResults());
String synthesis = synthesizeResearch(keyPoints, result.getResults());
```

**LLM Response:**

```
Recent quantum computing breakthroughs include:

1. **Error Correction Milestone**: Google achieved a major advance in
   quantum error correction, reducing error rates by 50% [Nature, 2024]

2. **Commercial Applications**: IBM demonstrated quantum advantage in
   materials simulation for drug discovery [IBM Research Blog]

3. **Scalability**: New superconducting qubits maintain coherence for
   1 millisecond, enabling larger quantum systems [MIT News]

Sources:
- Nature.com: "Quantum error correction breakthrough"
- IBM.com: "Quantum computing in pharmaceuticals"
- MIT News: "Longer-lasting qubits"
```

### Example 3: Fact Checking

**User Request:**

```
"Is it true that Python 4.0 was released?"
```

**LLM Analysis:**

```json
{
  "intent": "fact_check",
  "query": "Python 4.0 release date",
  "maxResults": 5
}
```

**Agent Code:**

```java
SearchResult result = webSearchTool.search("Python 4.0 release date").block();

// LLM analyzes consensus across sources
boolean factConfirmed = checkConsensus(result.getResults());
String verification = buildFactCheckResponse(factConfirmed, result);
```

**LLM Response:**

```
No, Python 4.0 has not been released. This is a common misconception.

**Current Status**: Python 3.12 is the latest stable version (as of 2024).

**Future Plans**: The Python core developers have stated there are no
plans for Python 4.0 in the near future. Python 3 will continue to
evolve with incremental version updates.

**Sources**:
- Python.org official documentation
- PEP 602: Python release schedule
- Multiple Python community forums confirm this

**Conclusion**: FALSE - Python 4.0 does not exist yet.
```

### Example 4: News Aggregation

**User Request:**

```
"What's happening in tech news today?"
```

**LLM Analysis:**

```json
{
  "intent": "news_search",
  "query": "tech news today",
  "maxResults": 15
}
```

**Agent Code:**

```java
SearchResult result = webSearchTool.search(
    SearchRequest.builder()
        .query("tech news today")
        .maxResults(15)
        .build()
).block();

// LLM categorizes and summarizes news
Map<String, List<NewsItem>> categorized = categorizeNews(result.getResults());
String newsSummary = formatNewsBrief(categorized);
```

**LLM Response:**

```
**Today's Tech News Brief**

📱 **Mobile & Devices**
- Apple announces new iPhone features for accessibility
- Samsung reveals foldable display improvements

💻 **Software & AI**
- OpenAI releases ChatGPT plugin for data analysis
- Microsoft integrates AI into Office suite

🔐 **Security**
- Major vulnerability patched in Chrome browser
- New encryption standard proposed for IoT devices

⚡ **Emerging Tech**
- Boston Dynamics robot completes autonomous navigation test
- Tesla showcases new battery technology

[All stories verified from TechCrunch, The Verge, Wired, ArXiv]
```

### Example 5: Comparison Research

**User Request:**

```
"Compare React vs Vue.js for frontend development"
```

**LLM Analysis:**

```json
{
  "intent": "comparison_research",
  "queries": [
    "React vs Vue comparison 2024",
    "React advantages disadvantages",
    "Vue.js pros cons"
  ],
  "maxResults": 8
}
```

**Agent Code:**

```java
// Multiple searches for comprehensive comparison
SearchResult reactVue = webSearchTool.search("React vs Vue comparison 2024").block();
SearchResult reactInfo = webSearchTool.search("React advantages disadvantages").block();
SearchResult vueInfo = webSearchTool.search("Vue.js pros cons").block();

// LLM synthesizes comparative analysis
String comparison = buildComparison(reactVue, reactInfo, vueInfo);
```

**LLM Response:**

```
**React vs Vue.js Comparison (2024)**

| Aspect | React | Vue.js |
|--------|-------|--------|
| **Learning Curve** | Moderate (JSX) | Easier (HTML-like templates) |
| **Community** | Larger (Meta-backed) | Growing, strong in Asia |
| **Performance** | Excellent | Excellent |
| **Ecosystem** | Massive (React Router, Redux) | Comprehensive (Vue Router, Vuex) |
| **Job Market** | More opportunities | Growing demand |

**When to Choose React:**
- Large enterprise applications
- Need extensive third-party libraries
- Mobile development (React Native)

**When to Choose Vue:**
- Rapid prototyping
- Prefer template-based syntax
- Smaller team, simpler projects

Sources: Stack Overflow Developer Survey 2024, JavaScript frameworks benchmark
```

---

## Tool Descriptor Format

LLMs use tool descriptors to understand available search functions. Here's the OpenAI function calling format:

### Simple Search

```json
{
  "name": "web_search",
  "description": "Search the web for current information on any topic",
  "parameters": {
    "type": "object",
    "properties": {
      "query": {
        "type": "string",
        "description": "Search query in natural language"
      },
      "maxResults": {
        "type": "integer",
        "description": "Maximum number of results to return (1-20)",
        "default": 10
      }
    },
    "required": ["query"]
  }
}
```

### Advanced Search

```json
{
  "name": "web_search_advanced",
  "description": "Perform an advanced web search with additional controls",
  "parameters": {
    "type": "object",
    "properties": {
      "query": {
        "type": "string",
        "description": "Search query"
      },
      "maxResults": {
        "type": "integer",
        "description": "Max results (1-20)",
        "default": 10
      },
      "region": {
        "type": "string",
        "description": "Geographic region code (US, UK, etc.)",
        "default": "US"
      },
      "safeSearch": {
        "type": "string",
        "enum": ["STRICT", "MODERATE", "OFF"],
        "description": "Safe search level",
        "default": "MODERATE"
      },
      "provider": {
        "type": "string",
        "enum": ["DUCKDUCKGO", "GOOGLE", "BING"],
        "description": "Search provider to use",
        "default": "DUCKDUCKGO"
      }
    },
    "required": ["query"]
  }
}
```

---

## Parameter Mapping

### Query Refinement

LLMs should refine user queries for better search results:

```java
// User: "python thing for web"
// LLM refines to: "Python web framework"

// User: "that AI thing everyone talks about"
// LLM refines to: "ChatGPT OpenAI large language model"

// User: "new phone from apple"
// LLM refines to: "Apple iPhone latest model 2024"
```

### Result Limit Guidelines

|       Use Case       | Recommended maxResults |
|----------------------|------------------------|
| Quick fact check     | 3-5                    |
| General question     | 5-10                   |
| Research query       | 10-15                  |
| Comprehensive review | 15-20                  |

### Region Targeting

```java
SearchRequest request = SearchRequest.builder()
    .query("best pizza restaurants")
    .region("IT")  // Italy
    .maxResults(10)
    .build();
```

---

## Result Processing

### Result Structure

```java
public class SearchResult {
    private boolean success;
    private List<SearchItem> results;
    private SearchMetadata metadata;
    private String summary;  // LLM-generated summary
}

public class SearchItem {
    private String title;
    private String url;
    private String snippet;
    private int rank;
    private Map<String, Object> metadata;
}
```

### LLM Processing Steps

**1. Relevance Filtering**

```java
// Filter results by relevance to original question
List<SearchItem> relevant = result.getResults().stream()
    .filter(item -> isRelevant(item, userQuestion))
    .collect(Collectors.toList());
```

**2. Source Credibility**

```java
// Rank by source credibility
List<SearchItem> credible = prioritizeCredibleSources(relevant);
```

**3. Information Extraction**

```java
// Extract key facts from snippets
List<Fact> facts = extractFacts(credible);
```

**4. Synthesis**

```java
// Combine into coherent answer
String answer = synthesize(facts, userQuestion);
```

**5. Citation**

```java
// Add source citations
String citedAnswer = addCitations(answer, credible);
```

---

## Caching Strategy

### Cache Behavior

The Web Search Tool uses a 15-minute TTL cache:

```java
// First search - hits provider
SearchResult result1 = webSearchTool.search("AI news").block();
// metadata.cached = false

// Second search within 15 minutes - cache hit
SearchResult result2 = webSearchTool.search("AI news").block();
// metadata.cached = true
```

### Cache Key Generation

```
Cache Key = provider:query:maxResults:region
Example: "DUCKDUCKGO:AI news:10:US"
```

### LLM Cache Awareness

```java
if (result.getMetadata().isCached()) {
    // Inform user of potentially stale data for time-sensitive queries
    if (isTimeSensitive(query)) {
        note = "(Note: Cached results from " +
               result.getMetadata().getTimestamp() + ")";
    }
}
```

### Cache Control

```java
// Clear cache for fresh results
webSearchTool.clearCache();

// Check cache size
int cachedQueries = webSearchTool.getCacheSize();
```

---

## Error Handling

### Provider Errors

```java
if (!result.isSuccess()) {
    String error = result.getMetadata().getError();

    // LLM-friendly error responses
    switch (error) {
        case "Rate limit exceeded":
            return "I'm temporarily rate-limited. Please try again in a few minutes.";

        case "Network timeout":
            return "Search timed out. This might be due to network issues. Would you like me to try again?";

        case "Provider not available":
            return "The search provider is unavailable. Let me try an alternative provider.";

        default:
            return "Search failed: " + error;
    }
}
```

### Fallback Strategy

```java
// Try DuckDuckGo first (no API key)
SearchResult result = webSearchTool.search(
    SearchRequest.builder()
        .query(query)
        .provider(SearchProvider.DUCKDUCKGO)
        .build()
).block();

if (!result.isSuccess()) {
    // Fallback to Google if available
    result = webSearchTool.search(
        SearchRequest.builder()
            .query(query)
            .provider(SearchProvider.GOOGLE)
            .build()
    ).block();
}
```

---

## Use Cases

### 1. Current Events & News

**Strength**: Access to latest information beyond LLM's training cutoff

```java
"What happened in the tech world this week?"
→ Search: "tech news this week"
→ Synthesize top stories with sources
```

### 2. Fact Verification

**Strength**: Cross-reference multiple sources

```java
"Is it true that [claim]?"
→ Search: "[claim] fact check"
→ Analyze consensus across results
→ Provide verdict with sources
```

### 3. Product Research

**Strength**: Compare reviews and specifications

```java
"What's the best laptop under $1000?"
→ Search: "best laptop under $1000 2024 reviews"
→ Aggregate recommendations
→ Compare specs and prices
```

### 4. Academic Research

**Strength**: Find recent papers and studies

```java
"Latest research on climate change mitigation"
→ Search: "climate change mitigation research 2024 site:scholar.google.com"
→ Extract paper titles and findings
```

### 5. Travel Planning

**Strength**: Current prices, availability, conditions

```java
"Best time to visit Japan for cherry blossoms?"
→ Search: "Japan cherry blossom season 2024"
→ Synthesize timing, locations, tips
```

---

## Best Practices

### For LLM Developers

**1. Query Refinement**

```java
// Poor query
"thing about python"

// Good query
"Python programming language features tutorial"
```

**2. Result Synthesis**
- Don't just list results verbatim
- Extract and combine information
- Add your own analysis
- Cite sources clearly

**3. Time Awareness**

```java
// Add temporal context to queries
if (isTimeSensitive(question)) {
    query += " " + getCurrentYear();
}
```

**4. Source Diversity**

```java
// Prefer results from different domains
List<SearchItem> diverse = ensureDomainDiversity(results);
```

**5. Fact Checking**

```java
// For important claims, cross-check
if (isImportantClaim(answer)) {
    SearchResult verification = webSearchTool.search(
        answer + " fact check"
    ).block();
}
```

### For Agent Implementers

**1. Error Recovery**

```java
@Retry(maxAttempts = 3, backoff = @Backoff(delay = 1000))
public Mono<SearchResult> searchWithRetry(String query) {
    return webSearchTool.search(query);
}
```

**2. Rate Limiting**

```java
// Respect provider rate limits
@RateLimiter(name = "search", fallbackMethod = "searchFallback")
public Mono<SearchResult> search(String query) {
    return webSearchTool.search(query);
}
```

**3. Parallel Searches**

```java
// For comparison queries
Mono<SearchResult> result1 = webSearchTool.search("React framework");
Mono<SearchResult> result2 = webSearchTool.search("Vue framework");

Mono.zip(result1, result2)
    .map(tuple -> compareResults(tuple.getT1(), tuple.getT2()));
```

---

## Integration Example

Complete example showing LLM agent using Web Search Tool:

```java
@Component
public class SearchAgent implements Agent {

    @Inject
    private WebSearchTool webSearchTool;

    @Override
    public TaskResult executeTask(TaskRequest request) {
        String question = request.task();

        try {
            // Refine query for better results
            String refinedQuery = refineQuery(question);

            // Execute search
            SearchResult result = webSearchTool.search(refinedQuery).block();

            if (!result.isSuccess()) {
                return handleSearchError(question, result);
            }

            // Process and synthesize results
            String answer = synthesizeAnswer(question, result);

            // Add citations
            String citedAnswer = addCitations(answer, result.getResults());

            Map<String, Object> metadata = Map.of(
                "provider", result.getMetadata().getProvider(),
                "sourceCount", result.getResults().size(),
                "cached", result.getMetadata().isCached()
            );

            return TaskResult.success(getName(), question, citedAnswer, metadata);

        } catch (Exception e) {
            return TaskResult.failureWithException(getName(), question, e);
        }
    }

    private String refineQuery(String question) {
        // Add temporal context
        if (isTimeSensitive(question)) {
            return question + " " + LocalDate.now().getYear();
        }

        // Remove filler words
        return question
            .replace("Can you tell me about", "")
            .replace("I want to know", "")
            .trim();
    }

    private String synthesizeAnswer(String question, SearchResult result) {
        // Extract key information from top results
        List<String> keyPoints = result.getResults().stream()
            .limit(5)
            .map(item -> extractKeyInfo(item.getSnippet()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        // Combine into coherent answer
        return buildAnswer(question, keyPoints);
    }

    private String addCitations(String answer, List<SearchItem> results) {
        StringBuilder cited = new StringBuilder(answer);
        cited.append("\n\n**Sources:**\n");

        results.stream()
            .limit(3)
            .forEach(item ->
                cited.append("- ").append(item.getTitle())
                     .append(" (").append(extractDomain(item.getUrl())).append(")\n")
            );

        return cited.toString();
    }
}
```

---

## Conclusion

The Web Search Tool provides LLM agents with access to current information from the web, enabling fact-checking, research, news aggregation, and more. With multi-provider support, result caching, and structured responses, it integrates seamlessly into LLM workflows.

**Key Takeaways:**
- ✅ Access current information beyond LLM training cutoff
- ✅ Multi-provider support (DuckDuckGo, Google, Bing)
- ✅ Result caching for performance (15-minute TTL)
- ✅ LLM-friendly error handling and fallback
- ✅ Structured results for easy synthesis
- ✅ Safe search controls for content filtering

---

*Last Updated: 2025-10-25*
*Version: 0.1.0*
*Status: DuckDuckGo Fully Implemented*
