# Web Search Tool - Configuration Guide

**Version:** 0.1.0
**Date:** 2025-10-25

---

## Table of Contents

- [Overview](#overview)
- [Configuration Options](#configuration-options)
- [Configuration Presets](#configuration-presets)
- [Custom Configuration](#custom-configuration)
- [Provider-Specific Configuration](#provider-specific-configuration)
- [Performance Tuning](#performance-tuning)
- [Common Scenarios](#common-scenarios)
- [Best Practices](#best-practices)
- [FAQ](#faq)

---

## Overview

The Web Search Tool behavior is controlled via `WebSearchConfig`, which allows you to customize:

- Search provider selection
- Result limits
- HTTP timeouts and retries
- Caching behavior
- Safe search levels
- Region/language preferences
- API keys for commercial providers

---

## Configuration Options

### Complete Configuration Reference

|       Option        |      Type      |     Default      |         Description         |
|---------------------|----------------|------------------|-----------------------------|
| `defaultProvider`   | SearchProvider | DUCKDUCKGO       | Default search provider     |
| `maxResults`        | int            | 10               | Maximum results per search  |
| `httpTimeoutMs`     | int            | 10000            | HTTP timeout (milliseconds) |
| `httpRetries`       | int            | 2                | Number of retry attempts    |
| `cacheResults`      | boolean        | true             | Enable result caching       |
| `cacheTtlMs`        | long           | 300000           | Cache TTL (5 minutes)       |
| `defaultSafeSearch` | SafeSearch     | MODERATE         | Safe search level           |
| `defaultRegion`     | String         | "en-US"          | Default region/language     |
| `userAgent`         | String         | "Mozilla/5.0..." | HTTP User-Agent string      |
| `googleApiKey`      | String         | null             | Google API key              |
| `googleCseId`       | String         | null             | Google CSE ID               |
| `bingApiKey`        | String         | null             | Bing API key                |

### Search Providers

```java
public enum SearchProvider {
    DUCKDUCKGO,  // Free, no API key
    GOOGLE,      // Requires API key
    BING         // Requires API key
}
```

### Safe Search Levels

```java
public enum SafeSearch {
    STRICT,    // Maximum filtering
    MODERATE,  // Balanced filtering (default)
    OFF        // No filtering
}
```

---

## Configuration Presets

### 1. Default Configuration

Balanced settings for general use:

```java
WebSearchConfig config = WebSearchConfig.defaults();
```

**Settings:**
- Provider: DuckDuckGo
- Max results: 10
- Timeout: 10 seconds
- Retries: 2
- Caching: Enabled (5 minutes)
- Safe search: Moderate

**Use Cases:**
- General-purpose searching
- AI agent research
- Documentation lookup
- Balanced speed/thoroughness

---

### 2. Fast Configuration

Optimized for speed:

```java
WebSearchConfig config = WebSearchConfig.fast();
```

**Settings:**
- Provider: DuckDuckGo
- Max results: 5
- Timeout: 5 seconds
- Retries: 1
- Caching: Enabled (5 minutes)

**Use Cases:**
- Interactive user queries
- Quick lookups
- Real-time search suggestions
- Low-latency requirements

---

### 3. Thorough Configuration

Optimized for comprehensive results:

```java
WebSearchConfig config = WebSearchConfig.thorough();
```

**Settings:**
- Provider: DuckDuckGo
- Max results: 20
- Timeout: 15 seconds
- Retries: 3
- Caching: Enabled (5 minutes)

**Use Cases:**
- Background research
- Comprehensive analysis
- Data gathering
- When quality > speed

---

### 4. No-Cache Configuration

Always fetch fresh results:

```java
WebSearchConfig config = WebSearchConfig.noCache();
```

**Settings:**
- Same as defaults
- Caching: **Disabled**

**Use Cases:**
- Real-time data (stock prices, news)
- Time-sensitive information
- Always-current results needed
- Testing/debugging

---

### 5. Google Search Configuration

Use Google Custom Search API:

```java
WebSearchConfig config = WebSearchConfig.google(
    "YOUR_API_KEY",
    "YOUR_CSE_ID"
);
```

**Settings:**
- Provider: Google
- All other settings: defaults
- Requires: API key and CSE ID

**Use Cases:**
- When Google results preferred
- Commercial applications
- High-quality search required
- API budget available

---

### 6. Bing Search Configuration

Use Bing Search API:

```java
WebSearchConfig config = WebSearchConfig.bing("YOUR_API_KEY");
```

**Settings:**
- Provider: Bing
- All other settings: defaults
- Requires: Bing API key

**Use Cases:**
- Microsoft ecosystem integration
- Bing-specific features needed
- Alternative to Google

---

## Custom Configuration

### Builder Pattern

```java
WebSearchConfig config = WebSearchConfig.builder()
    .defaultProvider(SearchProvider.DUCKDUCKGO)
    .maxResults(15)
    .httpTimeoutMs(8000)
    .httpRetries(3)
    .cacheResults(true)
    .cacheTtlMs(600000)  // 10 minutes
    .defaultSafeSearch(SafeSearch.STRICT)
    .defaultRegion("en-GB")
    .userAgent("MyBot/1.0")
    .build();

searchTool.setConfig(config);
```

### Progressive Customization

Start with preset, then customize:

```java
WebSearchConfig config = WebSearchConfig.fast()
    .toBuilder()  // Convert to builder
    .maxResults(8)
    .httpTimeoutMs(7000)
    .build();
```

---

## Provider-Specific Configuration

### DuckDuckGo Configuration

No API key required:

```java
WebSearchConfig config = WebSearchConfig.builder()
    .defaultProvider(SearchProvider.DUCKDUCKGO)
    .maxResults(10)
    .httpTimeoutMs(10000)
    .defaultRegion("en-US")
    .build();
```

**Best Practices:**
- Use reasonable timeouts (5-15 seconds)
- Enable caching to reduce requests
- Limit results (10-20 max)
- Respect rate limits

---

### Google Custom Search Configuration

Requires API key and Custom Search Engine ID:

```java
WebSearchConfig config = WebSearchConfig.builder()
    .defaultProvider(SearchProvider.GOOGLE)
    .googleApiKey(System.getenv("GOOGLE_API_KEY"))
    .googleCseId(System.getenv("GOOGLE_CSE_ID"))
    .maxResults(10)  // Google allows up to 10 per request
    .build();
```

**Setup Steps:**
1. Get API key: https://developers.google.com/custom-search
2. Create Custom Search Engine: https://cse.google.com/
3. Note CSE ID from engine settings
4. Store credentials securely (environment variables)

**Pricing:**
- 100 queries/day: Free
- Additional queries: $5 per 1,000 queries

---

### Bing Search Configuration

Requires Azure subscription and API key:

```java
WebSearchConfig config = WebSearchConfig.builder()
    .defaultProvider(SearchProvider.BING)
    .bingApiKey(System.getenv("BING_API_KEY"))
    .maxResults(50)  // Bing allows up to 50
    .build();
```

**Setup Steps:**
1. Create Azure account
2. Create Bing Search resource
3. Copy API key from Azure portal
4. Store securely (environment variables)

**Pricing:**
- Free tier: 1,000 transactions/month
- Paid tiers: $3-$10 per 1,000 transactions

---

## Performance Tuning

### Optimizing for Speed

```java
WebSearchConfig config = WebSearchConfig.builder()
    .maxResults(5)           // Fewer results = faster parsing
    .httpTimeoutMs(5000)     // Quick timeout
    .httpRetries(1)          // Minimal retries
    .cacheResults(true)      // Cache hits are fast
    .cacheTtlMs(300000)      // 5 minutes
    .build();
```

**Expected Performance:**
- First search: ~800-1,200ms
- Cached search: ~10-20ms

---

### Optimizing for Quality

```java
WebSearchConfig config = WebSearchConfig.builder()
    .maxResults(20)          // More results
    .httpTimeoutMs(15000)    // Patient timeout
    .httpRetries(3)          // More retries
    .cacheResults(false)     // Always fresh
    .build();
```

**Trade-off:**
- Higher quality, more comprehensive
- Slower (2-3 seconds per search)

---

### Balancing Speed and Quality

```java
WebSearchConfig config = WebSearchConfig.builder()
    .maxResults(10)          // Moderate count
    .httpTimeoutMs(10000)    // Balanced timeout
    .httpRetries(2)          // Standard retries
    .cacheResults(true)      // Cache common queries
    .cacheTtlMs(300000)      // 5-minute freshness
    .build();
```

**Recommended for:**
- General-purpose use
- AI agent research
- Most applications

---

## Common Scenarios

### Scenario 1: Interactive User Search

User typing queries in real-time:

```java
WebSearchConfig config = WebSearchConfig.fast();
searchTool.setConfig(config);
```

**Why:**
- Users expect fast responses
- 5 results sufficient for initial view
- Can request more if needed

---

### Scenario 2: Background Research Agent

AI agent researching topics overnight:

```java
WebSearchConfig config = WebSearchConfig.thorough();
searchTool.setConfig(config);
```

**Why:**
- No time pressure
- More results = better synthesis
- Higher retry count for reliability

---

### Scenario 3: Real-Time Data Monitoring

Stock prices, news alerts, weather:

```java
WebSearchConfig config = WebSearchConfig.builder()
    .cacheResults(false)     // Always fresh
    .maxResults(5)           // Just latest
    .httpTimeoutMs(5000)     // Quick timeout
    .build();
```

**Why:**
- Data changes rapidly
- Caching would serve stale data
- Need current information

---

### Scenario 4: API-Based Application

Using Google/Bing for production:

```java
WebSearchConfig config = WebSearchConfig.google(
    System.getenv("GOOGLE_API_KEY"),
    System.getenv("GOOGLE_CSE_ID")
);
searchTool.setConfig(config);
```

**Why:**
- Higher quality results
- Better reliability
- Commercial support
- Worth the API cost

---

### Scenario 5: Development/Testing

Testing search functionality:

```java
WebSearchConfig config = WebSearchConfig.builder()
    .cacheResults(false)     // See actual behavior
    .maxResults(3)           // Minimal for testing
    .httpTimeoutMs(5000)     // Fast failures
    .build();
```

**Why:**
- Avoid cached results during testing
- Fast iterations
- Minimal API quota usage

---

## Best Practices

### 1. Use Environment Variables for API Keys

```java
WebSearchConfig config = WebSearchConfig.builder()
    .googleApiKey(System.getenv("GOOGLE_API_KEY"))
    .googleCseId(System.getenv("GOOGLE_CSE_ID"))
    .bingApiKey(System.getenv("BING_API_KEY"))
    .build();
```

**Never:**
- Hardcode API keys in source code
- Commit keys to version control
- Share keys in documentation

---

### 2. Enable Caching for Repeated Queries

```java
WebSearchConfig config = WebSearchConfig.builder()
    .cacheResults(true)
    .cacheTtlMs(300000)  // 5 minutes
    .build();
```

**Benefits:**
- 100x faster for cache hits
- Reduces API quota usage
- Lower costs

---

### 3. Set Appropriate Timeouts

```java
// Interactive: Short timeout
WebSearchConfig.builder().httpTimeoutMs(5000).build()

// Background: Long timeout
WebSearchConfig.builder().httpTimeoutMs(15000).build()
```

**Guidelines:**
- Interactive: 5-8 seconds
- Background: 10-15 seconds
- Critical: 3-5 seconds

---

### 4. Limit Result Count

```java
WebSearchConfig config = WebSearchConfig.builder()
    .maxResults(10)  // Usually sufficient
    .build();
```

**Why:**
- Faster parsing
- Lower bandwidth
- Users rarely need >10 results

---

### 5. Use Safe Search

```java
WebSearchConfig config = WebSearchConfig.builder()
    .defaultSafeSearch(SafeSearch.MODERATE)
    .build();
```

**Recommendations:**
- Public apps: STRICT or MODERATE
- Internal tools: MODERATE
- Special cases only: OFF

---

## FAQ

### Q: Which provider should I use?

**A:** Start with DuckDuckGo (free, no setup). Upgrade to Google/Bing if you need:
- Higher quality results
- Commercial support
- Specific features
- Guaranteed uptime

---

### Q: How long should I cache results?

**A:**
- General searches: 5-10 minutes
- News/stocks: 1-2 minutes or no cache
- Documentation: 30-60 minutes
- Static content: Hours

---

### Q: What's a good timeout value?

**A:**
- Interactive: 5-8 seconds
- Background: 10-15 seconds
- Critical: 3-5 seconds
- Testing: 5 seconds

---

### Q: How many results should I request?

**A:**
- Quick lookup: 5 results
- General search: 10 results
- Research: 15-20 results
- Rarely need: >20 results

---

### Q: Should I use retries?

**A:** Yes, 2-3 retries recommended:
- Handles transient network issues
- Improves reliability
- Minimal time cost
- Exponential backoff built-in

---

### Q: How do I secure API keys?

**A:**
1. Use environment variables
2. Use secrets management (Vault, AWS Secrets Manager)
3. Never commit to version control
4. Rotate keys periodically
5. Use different keys for dev/prod

---

### Q: Can I use multiple providers?

**A:** Yes, specify per-request:

```java
SearchRequest googleRequest = SearchRequest.builder()
    .query("topic")
    .provider(SearchProvider.GOOGLE)
    .build();

SearchRequest duckRequest = SearchRequest.builder()
    .query("topic")
    .provider(SearchProvider.DUCKDUCKGO)
    .build();
```

---

### Q: How do I disable caching temporarily?

**A:**

```java
// Save current config
WebSearchConfig original = searchTool.getConfig();

// Disable caching
searchTool.setConfig(WebSearchConfig.noCache());

// Do searches...

// Restore original
searchTool.setConfig(original);
```

---

*Last Updated: 2025-10-25*
*Version: 0.1.0*
