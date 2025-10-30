# Market Data Tool - Architecture

**Version:** 0.1.0
**Date:** 2025-10-25

---

## Overview

The Market Data Tool follows the provider/service pattern used across Adentic tools (Database, Web Search, File System, Web Test). It provides a unified API for accessing market data from multiple providers with runtime provider switching.

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    MarketDataTool                        │
│                    (@Tool annotation)                    │
│  • Unified API for market data access                   │
│  • Provider lifecycle management                        │
│  • Runtime provider switching                           │
└────────────────────┬────────────────────────────────────┘
                     │
          ┌──────────┼──────────┬──────────┬──────────┐
          │          │          │          │          │
          ▼          ▼          ▼          ▼          ▼
    ┌─────────┐┌─────────┐┌─────────┐┌─────────┐┌─────────┐
    │  Alpha  ││ Yahoo   ││ Binance ││CoinGecko││  Mock   │
    │ Vantage ││ Finance ││(planned)││(planned)││Provider │
    │ (stub)  ││ (stub)  ││         ││         ││  (impl) │
    └─────────┘└─────────┘└─────────┘└─────────┘└─────────┘
         │          │          │          │          │
         └──────────┴──────────┴──────────┴──────────┘
                              │
                    ┌─────────┴─────────┐
                    │  BaseMarketData   │
                    │     Provider      │
                    │  • Validation     │
                    │  • Result builders│
                    │  • Logging        │
                    └───────────────────┘
```

---

## Component Structure

### 1. MarketDataTool (Main API)

**Location:** `dev.adeengineer.adentic.tool.marketdata.MarketDataTool`

**Responsibilities:**
- Provide unified API for all market data operations
- Manage provider lifecycle (connect/disconnect)
- Route requests to active provider
- Handle provider switching at runtime

**Annotations:**
- `@Tool(name = "market-data")` - Registers as Adentic tool

**Key Methods:**

```java
// OHLC Data
Mono<OHLCResult> getOHLCBars(String symbol, TimeFrame timeFrame, int limit)
Mono<OHLCResult> getHistoricalData(String symbol, TimeFrame timeFrame,
                                   LocalDate startDate, LocalDate endDate)

// Real-Time Quotes
Mono<QuoteData> getQuote(String symbol)
Flux<QuoteData> streamQuotes(String symbol)

// Symbol Search
Mono<List<Symbol>> searchSymbols(String query)
Mono<Symbol> getSymbolInfo(String symbol)

// Configuration
void setConfig(MarketDataConfig config)
```

---

### 2. MarketDataProvider (Interface)

**Location:** `dev.adeengineer.adentic.tool.marketdata.provider.MarketDataProvider`

**Purpose:** Define contract for all market data providers

**Core Operations:**
- Lifecycle: `connect()`, `disconnect()`, `isConnected()`
- OHLC Data: `getOHLCBars()`, `getHistoricalData()`
- Real-Time: `getQuote()`, `streamQuotes()`
- Search: `searchSymbols()`, `getSymbolInfo()`
- Metadata: `getProviderName()`, `getSupportedMarketType()`

---

### 3. BaseMarketDataProvider (Abstract Base)

**Location:** `dev.adeengineer.adentic.tool.marketdata.provider.BaseMarketDataProvider`

**Purpose:** Common functionality for all providers

**Provides:**
- Configuration management
- Connection state tracking
- Input validation (symbols, limits)
- Result builders (success/failure)
- Logging utilities

**Protected Methods:**

```java
// Result Builders
OHLCResult createSuccessResult(List<OHLCBar> bars, String symbol, ...)
OHLCResult createFailureResult(String symbol, TimeFrame timeFrame, String error)

// Validation
void validateSymbol(String symbol)
void validateLimit(int limit)

// Configuration Access
String getApiKey()
boolean isCachingEnabled()
long getCacheTTL()
```

---

### 4. Provider Implementations

#### MockMarketDataProvider (✅ Fully Implemented)

**Status:** Production-ready
**Purpose:** Testing and development

**Features:**
- Generates synthetic OHLC data
- Realistic price movements (2% volatility)
- Gaussian distribution for price changes
- Supports all time frames
- No external dependencies

**Implementation Details:**

```java
// Generate synthetic bars with realistic price action
private List<OHLCBar> generateSyntheticBars(String symbol, TimeFrame tf, int count) {
    BigDecimal currentPrice = BASE_PRICE;

    for (int i = 0; i < count; i++) {
        BigDecimal open = currentPrice;
        BigDecimal close = generatePrice(open);  // Gaussian random walk
        BigDecimal high = max(open, close) + randomChange();
        BigDecimal low = min(open, close) - randomChange();
        BigDecimal volume = random 500k-1.5M shares

        // Create bar with realistic data
    }
}
```

#### AlphaVantageProvider (🟡 Stub)

**Status:** Stub implementation
**API:** https://www.alphavantage.co/documentation/
**Supports:** Stocks, Forex, Crypto
**Rate Limits:** 5 req/min, 500 req/day (free tier)

**TODO:**
- HTTP client implementation (OkHttp/WebClient)
- JSON parsing (Jackson)
- Rate limiting enforcement
- Caching layer
- Error handling (429, 5xx)

**API Endpoints:**

```
GET /query?function=TIME_SERIES_INTRADAY&symbol=AAPL&interval=1min&apikey=KEY
GET /query?function=TIME_SERIES_DAILY&symbol=AAPL&apikey=KEY
GET /query?function=SYMBOL_SEARCH&keywords=Apple&apikey=KEY
```

#### YahooFinanceProvider (🟡 Stub)

**Status:** Stub implementation
**API:** Yahoo Finance Chart API
**Supports:** Stocks, ETFs, Indices
**Rate Limits:** Best effort (no official limits)

**TODO:**
- HTTP client implementation
- CSV/JSON parsing
- Historical data retrieval
- Quote extraction

**API Endpoints:**

```
GET /v8/finance/chart/AAPL?interval=1d&range=1mo
GET /v7/finance/quote?symbols=AAPL,TSLA,GOOGL
```

---

### 5. Model Classes

#### OHLCBar

```java
@Data @Builder
public class OHLCBar {
    private Instant timestamp;
    private BigDecimal open, high, low, close, volume;
    private String symbol;
    private TimeFrame timeFrame;

    // Utility methods
    public BigDecimal getPriceChange()      // close - open
    public BigDecimal getPercentageChange() // (close - open) / open * 100
    public boolean isBullish()              // close > open
    public boolean isBearish()              // close < open
    public boolean isDoji()                 // |change| < 0.1%
}
```

#### OHLCResult

```java
@Data @Builder
public class OHLCResult {
    private boolean success;
    private List<OHLCBar> bars;
    private int count;
    private String symbol;
    private TimeFrame timeFrame;
    private String provider;
    private String errorMessage;
    private boolean cached;
    private long executionTimeMs;
}
```

#### QuoteData

```java
@Data @Builder
public class QuoteData {
    private String symbol;
    private BigDecimal lastPrice, bidPrice, askPrice;
    private BigDecimal bidSize, askSize, volume;
    private BigDecimal openPrice, highPrice, lowPrice, previousClose;
    private Instant timestamp;
    private String provider;

    // Utility methods
    public BigDecimal getSpread()           // ask - bid
    public BigDecimal getPriceChange()      // last - previousClose
    public BigDecimal getPercentageChange() // (last - prev) / prev * 100
}
```

#### Symbol

```java
@Data @Builder
public class Symbol {
    private String ticker;        // "AAPL"
    private String name;          // "Apple Inc."
    private String exchange;      // "NASDAQ"
    private MarketType marketType; // STOCKS, CRYPTO, FOREX, etc.
    private String currency;      // "USD"
    private String region;        // "US"
    private boolean active;
    private String description;
}
```

---

### 6. Configuration

#### MarketDataConfig

```java
@Data @Builder
public class MarketDataConfig {
    private DataProvider provider;         // ALPHA_VANTAGE, YAHOO_FINANCE, etc.
    private String apiKey;
    private int rateLimitPerMinute;       // Default: 5
    private boolean enableCaching;         // Default: true
    private long cacheTTLSeconds;          // Default: 900 (15 min)
    private int maxBarsPerRequest;         // Default: 1000
    private long requestTimeout;           // Default: 30000ms
    private boolean enableRetry;           // Default: true
    private int maxRetryAttempts;          // Default: 3
    private boolean enableLogging;         // Default: true
}
```

**Factory Methods:**

```java
MarketDataConfig.alphaVantage(apiKey)
MarketDataConfig.yahooFinance()
MarketDataConfig.binance(apiKey)
MarketDataConfig.mock()
MarketDataConfig.defaults()  // Alpha Vantage with demo key
```

---

### 7. Enumerations

#### TimeFrame

```java
public enum TimeFrame {
    M1(60, "1min", "1m"),       // 1 minute
    M5(300, "5min", "5m"),      // 5 minutes
    M15(900, "15min", "15m"),   // 15 minutes
    M30(1800, "30min", "30m"),  // 30 minutes
    H1(3600, "60min", "1h"),    // 1 hour
    H4(14400, "240min", "4h"),  // 4 hours
    D1(86400, "daily", "1d"),   // 1 day
    W1(604800, "weekly", "1w"), // 1 week
    MN1(2592000, "monthly", "1M"); // 1 month
}
```

#### DataProvider

```java
public enum DataProvider {
    ALPHA_VANTAGE("Alpha Vantage", true),    // ✅ Implemented (stub)
    YAHOO_FINANCE("Yahoo Finance", true),    // ✅ Implemented (stub)
    BINANCE("Binance", false),               // ⚪ Planned
    COINGECKO("CoinGecko", false),           // ⚪ Planned
    IEX_CLOUD("IEX Cloud", false),           // ⚪ Planned
    POLYGON("Polygon.io", false),            // ⚪ Planned
    MOCK("Mock Provider", true);             // ✅ Fully implemented
}
```

#### MarketType

```java
public enum MarketType {
    STOCKS, CRYPTO, FOREX, COMMODITIES, ETF, INDICES,
    FUTURES, OPTIONS, BONDS, UNKNOWN
}
```

---

## Design Patterns

### 1. Provider Pattern

- **MarketDataProvider** interface defines contract
- **BaseMarketDataProvider** provides common functionality
- Concrete providers (Alpha Vantage, Yahoo, Mock) implement specifics
- **MarketDataTool** orchestrates provider lifecycle

### 2. Builder Pattern

- All model classes use Lombok `@Builder`
- Fluent API for configuration: `MarketDataConfig.builder().provider(...).build()`

### 3. Reactive Programming

- All async operations return `Mono<T>` or `Flux<T>`
- Non-blocking I/O with Project Reactor
- Composable operations with `map()`, `flatMap()`, `then()`

### 4. Validation Pattern

- Input validation in `BaseMarketDataProvider`
- Symbol format validation (alphanumeric + dash/underscore/dot)
- Limit validation (positive, within max bounds)
- Early validation prevents unnecessary API calls

---

## Data Flow

### OHLC Data Retrieval Flow

```
User/Agent
    │
    ▼
MarketDataTool.getOHLCBars("AAPL", TimeFrame.D1, 30)
    │
    ├─→ ensureConnected()
    │   └─→ provider.connect() (if not connected)
    │
    ├─→ provider.getOHLCBars("AAPL", D1, 30)
    │   │
    │   ├─→ validateSymbol("AAPL")
    │   ├─→ validateLimit(30)
    │   ├─→ logOperation("getOHLCBars", "AAPL D1 limit=30")
    │   │
    │   ├─→ HTTP GET to provider API (or generate mock data)
    │   ├─→ Parse JSON/CSV response
    │   ├─→ Map to List<OHLCBar>
    │   │
    │   └─→ createSuccessResult(bars, "AAPL", D1, cached=false, execTime)
    │
    └─→ Return Mono<OHLCResult>
```

### Error Handling Flow

```
API Call
    │
    ├─→ Success → createSuccessResult()
    │
    ├─→ HTTP 429 (Rate Limit) → Retry with exponential backoff
    │
    ├─→ HTTP 404 (Symbol Not Found) → createFailureResult("Symbol not found")
    │
    ├─→ HTTP 5xx (Server Error) → Retry → createFailureResult("Server error")
    │
    └─→ Network Timeout → Retry → createFailureResult("Timeout")
```

---

## Extensibility

### Adding a New Provider

1. **Create Provider Class**

```java
public class BinanceProvider extends BaseMarketDataProvider {
    public BinanceProvider(MarketDataConfig config) {
        super(config);
    }

    @Override
    public Mono<OHLCResult> getOHLCBars(String symbol, TimeFrame tf, int limit) {
        // Implement Binance API call
    }

    // Implement other methods...
}
```

2. **Update DataProvider Enum**

```java
public enum DataProvider {
    // ...existing providers...
    BINANCE("Binance", true),  // Mark as implemented
}
```

3. **Update MarketDataTool Factory**

```java
private MarketDataProvider createProvider(MarketDataConfig config) {
    return switch (config.getProvider()) {
        // ...existing cases...
        case BINANCE -> new BinanceProvider(config);
    };
}
```

4. **Add Configuration Factory**

```java
public static MarketDataConfig binance(String apiKey) {
    return MarketDataConfig.builder()
        .provider(DataProvider.BINANCE)
        .apiKey(apiKey)
        .rateLimitPerMinute(20)
        .build();
}
```

---

## Performance Considerations

### Caching Strategy

- **Cache Key:** `provider:symbol:timeFrame:limit`
- **TTL:** 15 minutes default (configurable)
- **Invalidation:** Manual or TTL-based
- **Storage:** In-memory (future: Redis)

### Rate Limiting

- Token bucket algorithm
- Per-provider limits
- Configurable via `rateLimitPerMinute`
- Automatic retry with exponential backoff

### Connection Pooling

- Reuse HTTP connections
- Configurable pool size
- Connection timeout: 30 seconds default
- Idle connection cleanup

---

*Last Updated: 2025-10-25*
