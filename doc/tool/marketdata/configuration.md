# Market Data Tool - Configuration

**Version:** 0.1.0
**Date:** 2025-10-25

---

## Configuration Options

### MarketDataConfig

All configuration options for the Market Data Tool:

|      Parameter       |      Type      |     Default     |           Description           |
|----------------------|----------------|-----------------|---------------------------------|
| `provider`           | `DataProvider` | `ALPHA_VANTAGE` | Data provider to use            |
| `apiKey`             | `String`       | `null`          | API key for provider            |
| `rateLimitPerMinute` | `int`          | `5`             | Maximum requests per minute     |
| `enableCaching`      | `boolean`      | `true`          | Enable response caching         |
| `cacheTTLSeconds`    | `long`         | `900`           | Cache TTL (15 minutes)          |
| `maxBarsPerRequest`  | `int`          | `1000`          | Maximum bars per request        |
| `requestTimeout`     | `long`         | `30000`         | Request timeout (milliseconds)  |
| `enableRetry`        | `boolean`      | `true`          | Enable retry on failure         |
| `maxRetryAttempts`   | `int`          | `3`             | Maximum retry attempts          |
| `enableLogging`      | `boolean`      | `true`          | Enable request/response logging |
| `baseUrlOverride`    | `String`       | `null`          | Override provider base URL      |

---

## Provider Configurations

### Alpha Vantage

```java
MarketDataConfig config = MarketDataConfig.alphaVantage("your-api-key");

// Equivalent to:
MarketDataConfig config = MarketDataConfig.builder()
    .provider(DataProvider.ALPHA_VANTAGE)
    .apiKey("your-api-key")
    .rateLimitPerMinute(5)        // Free tier limit
    .enableCaching(true)
    .cacheTTLSeconds(900)
    .build();
```

**Free Tier Limits:**
- 5 requests per minute
- 500 requests per day

**Get API Key:** https://www.alphavantage.co/support/#api-key

---

### Yahoo Finance

```java
MarketDataConfig config = MarketDataConfig.yahooFinance();

// Equivalent to:
MarketDataConfig config = MarketDataConfig.builder()
    .provider(DataProvider.YAHOO_FINANCE)
    .rateLimitPerMinute(60)       // More generous
    .enableCaching(true)
    .cacheTTLSeconds(900)
    .build();
```

**Rate Limits:** Best effort (no official limits)

**No API key required**

---

### Binance (Stub)

```java
MarketDataConfig config = MarketDataConfig.binance("api-key");

// Equivalent to:
MarketDataConfig config = MarketDataConfig.builder()
    .provider(DataProvider.BINANCE)
    .apiKey("api-key")  // Optional for public endpoints
    .rateLimitPerMinute(20)
    .enableCaching(true)
    .cacheTTLSeconds(900)
    .build();
```

**Rate Limits:** 1200 requests per minute (generous)

---

### Mock Provider

```java
MarketDataConfig config = MarketDataConfig.mock();

// Equivalent to:
MarketDataConfig config = MarketDataConfig.builder()
    .provider(DataProvider.MOCK)
    .enableCaching(false)  // No need to cache synthetic data
    .enableLogging(true)
    .build();
```

**Use Case:** Testing and development

**No API key required**

---

## Custom Configuration

### Builder Pattern

```java
MarketDataConfig config = MarketDataConfig.builder()
    .provider(DataProvider.ALPHA_VANTAGE)
    .apiKey(System.getenv("ALPHA_VANTAGE_KEY"))
    .rateLimitPerMinute(5)
    .enableCaching(true)
    .cacheTTLSeconds(600)        // 10 minutes
    .maxBarsPerRequest(500)
    .requestTimeout(60000)        // 60 seconds
    .enableRetry(true)
    .maxRetryAttempts(5)
    .enableLogging(true)
    .build();

MarketDataTool marketData = new MarketDataTool(config);
```

---

## Configuration Parameters

### provider

**Type:** `DataProvider` enum

**Options:**
- `ALPHA_VANTAGE` - Alpha Vantage API (stub)
- `YAHOO_FINANCE` - Yahoo Finance API (stub)
- `BINANCE` - Binance API (planned)
- `COINGECKO` - CoinGecko API (planned)
- `IEX_CLOUD` - IEX Cloud API (planned)
- `POLYGON` - Polygon.io API (planned)
- `MOCK` - Mock provider (fully implemented)

**Example:**

```java
.provider(DataProvider.ALPHA_VANTAGE)
```

---

### apiKey

**Type:** `String`

**Purpose:** API key for authentication with provider

**Required for:**
- Alpha Vantage
- Binance (optional for public data)
- IEX Cloud
- Polygon.io

**Not required for:**
- Yahoo Finance
- Mock provider

**Best Practice:** Use environment variables

```java
.apiKey(System.getenv("ALPHA_VANTAGE_KEY"))
```

---

### rateLimitPerMinute

**Type:** `int`

**Default:** `5`

**Purpose:** Maximum API requests per minute

**Recommended values:**
- Alpha Vantage free tier: `5`
- Yahoo Finance: `60`
- Binance: `20-100`
- CoinGecko: `10`

**Example:**

```java
.rateLimitPerMinute(5)
```

---

### enableCaching

**Type:** `boolean`

**Default:** `true`

**Purpose:** Enable response caching to reduce API calls

**Cache Key Format:** `provider:symbol:timeFrame:limit`

**Example:**

```java
.enableCaching(true)
```

---

### cacheTTLSeconds

**Type:** `long`

**Default:** `900` (15 minutes)

**Purpose:** How long to cache responses

**Recommended values:**
- Intraday data (M1, M5): `60-300` seconds
- Daily data (D1): `900-1800` seconds
- Historical data: `3600-86400` seconds

**Example:**

```java
.cacheTTLSeconds(600)  // 10 minutes
```

---

### maxBarsPerRequest

**Type:** `int`

**Default:** `1000`

**Purpose:** Maximum number of bars to retrieve in a single request

**Provider limits:**
- Alpha Vantage: Varies by endpoint
- Yahoo Finance: Unlimited (practically)
- Binance: 1000

**Example:**

```java
.maxBarsPerRequest(1000)
```

---

### requestTimeout

**Type:** `long`

**Default:** `30000` (30 seconds)

**Purpose:** HTTP request timeout in milliseconds

**Recommended values:**
- Fast providers (Binance): `10000` ms
- Average providers: `30000` ms
- Slow providers: `60000` ms

**Example:**

```java
.requestTimeout(30000)  // 30 seconds
```

---

### enableRetry

**Type:** `boolean`

**Default:** `true`

**Purpose:** Retry failed requests automatically

**Retry scenarios:**
- Network timeouts
- HTTP 5xx errors
- Rate limit errors (429)

**Example:**

```java
.enableRetry(true)
```

---

### maxRetryAttempts

**Type:** `int`

**Default:** `3`

**Purpose:** Maximum number of retry attempts

**Backoff strategy:** Exponential backoff (1s, 2s, 4s, 8s, ...)

**Example:**

```java
.maxRetryAttempts(3)
```

---

### enableLogging

**Type:** `boolean`

**Default:** `true`

**Purpose:** Log API requests and responses

**Log format:**

```
[Alpha Vantage] getOHLCBars - AAPL D1 limit=30
```

**Example:**

```java
.enableLogging(true)
```

---

### baseUrlOverride

**Type:** `String`

**Default:** `null`

**Purpose:** Override provider's base URL (for testing or proxies)

**Example:**

```java
.baseUrlOverride("https://proxy.example.com/api")
```

---

## Environment-Based Configuration

### Using Environment Variables

```java
MarketDataConfig config = MarketDataConfig.builder()
    .provider(DataProvider.valueOf(System.getenv("MARKET_DATA_PROVIDER")))
    .apiKey(System.getenv("MARKET_DATA_API_KEY"))
    .rateLimitPerMinute(Integer.parseInt(
        System.getenv().getOrDefault("RATE_LIMIT", "5")
    ))
    .build();
```

**Environment variables:**

```bash
export MARKET_DATA_PROVIDER=ALPHA_VANTAGE
export MARKET_DATA_API_KEY=your-api-key-here
export RATE_LIMIT=5
```

---

## Spring Boot Configuration

### application.yml

```yaml
market-data:
  provider: ALPHA_VANTAGE
  api-key: ${ALPHA_VANTAGE_API_KEY}
  rate-limit-per-minute: 5
  enable-caching: true
  cache-ttl-seconds: 900
  max-bars-per-request: 1000
  request-timeout: 30000
  enable-retry: true
  max-retry-attempts: 3
  enable-logging: true
```

### Configuration Class

```java
@Configuration
public class MarketDataConfiguration {

    @Bean
    public MarketDataTool marketDataTool(
        @Value("${market-data.provider}") String provider,
        @Value("${market-data.api-key}") String apiKey,
        @Value("${market-data.rate-limit-per-minute}") int rateLimit
    ) {
        MarketDataConfig config = MarketDataConfig.builder()
            .provider(DataProvider.valueOf(provider))
            .apiKey(apiKey)
            .rateLimitPerMinute(rateLimit)
            .build();

        return new MarketDataTool(config);
    }
}
```

---

## Runtime Configuration Changes

### Switch Provider Dynamically

```java
MarketDataTool marketData = new MarketDataTool();

// Start with Mock provider
marketData.setConfig(MarketDataConfig.mock());
OHLCResult mockData = marketData.getOHLCBars("TEST", TimeFrame.D1, 10).block();

// Switch to real provider
marketData.setConfig(MarketDataConfig.alphaVantage("api-key"));
OHLCResult realData = marketData.getOHLCBars("AAPL", TimeFrame.D1, 10).block();
```

---

## Configuration Best Practices

### 1. Use Environment Variables for Secrets

```java
// ✅ GOOD
.apiKey(System.getenv("ALPHA_VANTAGE_KEY"))

// ❌ BAD (hardcoded secrets)
.apiKey("abc123def456")
```

### 2. Tune Cache TTL by Use Case

```java
// Intraday trading - short cache
MarketDataConfig intraday = MarketDataConfig.builder()
    .cacheTTLSeconds(60)  // 1 minute
    .build();

// Historical analysis - long cache
MarketDataConfig historical = MarketDataConfig.builder()
    .cacheTTLSeconds(3600)  // 1 hour
    .build();
```

### 3. Match Rate Limits to Provider Tiers

```java
// Free tier - conservative
.rateLimitPerMinute(5)

// Paid tier - aggressive
.rateLimitPerMinute(60)
```

### 4. Enable Logging in Development

```java
// Development
.enableLogging(true)

// Production (performance)
.enableLogging(false)
```

### 5. Use Mock Provider in Tests

```java
@Test
public void testDataRetrieval() {
    MarketDataTool marketData = new MarketDataTool(MarketDataConfig.mock());
    // Test with synthetic data
}
```

---

*Last Updated: 2025-10-25*
