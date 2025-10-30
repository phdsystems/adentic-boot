# Market Data Tool - Usage Guide

**Version:** 0.1.0
**Date:** 2025-10-25

---

## Table of Contents

- [Getting Started](#getting-started)
- [Basic Operations](#basic-operations)
- [Advanced Usage](#advanced-usage)
- [Provider Configuration](#provider-configuration)
- [Error Handling](#error-handling)
- [Best Practices](#best-practices)

---

## Getting Started

### Dependency Injection

```java
@Component
public class TradingService {

    @Inject
    private MarketDataTool marketData;

    public void analyzePrices() {
        OHLCResult result = marketData.getOHLCBars("AAPL", TimeFrame.D1, 30).block();
        // Process result...
    }
}
```

### Manual Instantiation

```java
// Default configuration (Alpha Vantage with demo key)
MarketDataTool marketData = new MarketDataTool();

// Custom configuration
MarketDataConfig config = MarketDataConfig.mock();
MarketDataTool marketData = new MarketDataTool(config);
```

---

## Basic Operations

### 1. Get OHLC Bars by Limit

Retrieve a specific number of recent bars:

```java
// Get last 30 daily candles for Apple stock
OHLCResult result = marketData.getOHLCBars("AAPL", TimeFrame.D1, 30).block();

if (result.isSuccess()) {
    System.out.printf("Retrieved %d bars for %s%n", result.getCount(), result.getSymbol());

    for (OHLCBar bar : result.getBars()) {
        System.out.printf("%s: O=%.2f H=%.2f L=%.2f C=%.2f V=%.0f%n",
            bar.getTimestamp(),
            bar.getOpen(),
            bar.getHigh(),
            bar.getLow(),
            bar.getClose(),
            bar.getVolume()
        );
    }
} else {
    System.err.println("Error: " + result.getErrorMessage());
}
```

### 2. Get OHLC Bars by Time Range

Retrieve bars within a specific time window:

```java
Instant startTime = Instant.now().minus(7, ChronoUnit.DAYS);
Instant endTime = Instant.now();

OHLCResult result = marketData.getOHLCBars(
    "TSLA",
    TimeFrame.H1,
    startTime,
    endTime
).block();

System.out.printf("Retrieved %d hourly bars%n", result.getCount());
```

### 3. Get Historical Data by Date

Retrieve historical data using date range:

```java
LocalDate startDate = LocalDate.of(2025, 1, 1);
LocalDate endDate = LocalDate.of(2025, 10, 25);

OHLCResult result = marketData.getHistoricalData(
    "GOOGL",
    TimeFrame.D1,
    startDate,
    endDate
).block();

System.out.printf("Retrieved %d days of historical data%n", result.getCount());
```

### 4. Get Real-Time Quote

Get current market quote for a symbol:

```java
QuoteData quote = marketData.getQuote("TSLA").block();

System.out.printf(
    "TSLA: Last=$%.2f Bid=$%.2f Ask=$%.2f Change=%.2f%% Vol=%.0f%n",
    quote.getLastPrice(),
    quote.getBidPrice(),
    quote.getAskPrice(),
    quote.getPercentageChange(),
    quote.getVolume()
);
```

### 5. Search Symbols

Find symbols matching a query:

```java
List<Symbol> symbols = marketData.searchSymbols("Apple").block();

for (Symbol symbol : symbols) {
    System.out.printf("%s - %s (%s)%n",
        symbol.getTicker(),
        symbol.getName(),
        symbol.getExchange()
    );
}
```

### 6. Get Symbol Information

Get detailed info about a specific symbol:

```java
Symbol info = marketData.getSymbolInfo("AAPL").block();

System.out.printf(
    "Symbol: %s%nName: %s%nExchange: %s%nMarket: %s%nCurrency: %s%n",
    info.getTicker(),
    info.getName(),
    info.getExchange(),
    info.getMarketType(),
    info.getCurrency()
);
```

---

## Advanced Usage

### Working with Time Frames

```java
// Intraday analysis - 1-minute candles
OHLCResult m1 = marketData.getOHLCBars("AAPL", TimeFrame.M1, 60).block();

// Day trading - 15-minute candles
OHLCResult m15 = marketData.getOHLCBars("AAPL", TimeFrame.M15, 100).block();

// Swing trading - 4-hour candles
OHLCResult h4 = marketData.getOHLCBars("AAPL", TimeFrame.H4, 50).block();

// Position trading - daily candles
OHLCResult d1 = marketData.getOHLCBars("AAPL", TimeFrame.D1, 200).block();

// Long-term analysis - weekly candles
OHLCResult w1 = marketData.getOHLCBars("AAPL", TimeFrame.W1, 52).block();
```

### Analyzing OHLC Bars

```java
OHLCResult result = marketData.getOHLCBars("AAPL", TimeFrame.D1, 10).block();

for (OHLCBar bar : result.getBars()) {
    // Check candle type
    if (bar.isBullish()) {
        System.out.printf("🟢 %s: Bullish +%.2f%%%n",
            bar.getTimestamp(), bar.getPercentageChange());
    } else if (bar.isBearish()) {
        System.out.printf("🔴 %s: Bearish %.2f%%%n",
            bar.getTimestamp(), bar.getPercentageChange());
    } else if (bar.isDoji()) {
        System.out.printf("➖ %s: Doji (neutral)%n", bar.getTimestamp());
    }

    // Calculate price change
    BigDecimal change = bar.getPriceChange();
    System.out.printf("   Price change: $%.2f%n", change);
}
```

### Reactive Composition

```java
// Chain multiple operations
marketData.getOHLCBars("AAPL", TimeFrame.D1, 30)
    .map(result -> result.getBars())
    .flatMapMany(Flux::fromIterable)
    .filter(bar -> bar.isBullish())
    .map(bar -> bar.getClose())
    .reduce(BigDecimal.ZERO, BigDecimal::add)
    .doOnSuccess(total -> System.out.println("Total close prices: " + total))
    .subscribe();
```

### Parallel Data Retrieval

```java
// Fetch data for multiple symbols in parallel
List<String> symbols = List.of("AAPL", "GOOGL", "TSLA", "MSFT");

Flux.fromIterable(symbols)
    .parallel()
    .flatMap(symbol -> marketData.getOHLCBars(symbol, TimeFrame.D1, 5))
    .doOnNext(result -> System.out.println(result))
    .sequential()
    .collectList()
    .block();
```

---

## Provider Configuration

### Switch Providers at Runtime

```java
MarketDataTool marketData = new MarketDataTool();

// Use Mock provider for testing
marketData.setConfig(MarketDataConfig.mock());
OHLCResult mockData = marketData.getOHLCBars("TEST", TimeFrame.D1, 10).block();

// Switch to Alpha Vantage for real data
marketData.setConfig(MarketDataConfig.alphaVantage("your-api-key"));
OHLCResult realData = marketData.getOHLCBars("AAPL", TimeFrame.D1, 10).block();

// Switch to Yahoo Finance
marketData.setConfig(MarketDataConfig.yahooFinance());
OHLCResult yahooData = marketData.getOHLCBars("GOOGL", TimeFrame.D1, 10).block();
```

### Custom Configuration

```java
MarketDataConfig config = MarketDataConfig.builder()
    .provider(DataProvider.ALPHA_VANTAGE)
    .apiKey("your-api-key")
    .rateLimitPerMinute(5)
    .enableCaching(true)
    .cacheTTLSeconds(600)  // 10 minutes
    .maxBarsPerRequest(1000)
    .requestTimeout(60000)  // 60 seconds
    .enableRetry(true)
    .maxRetryAttempts(3)
    .enableLogging(true)
    .build();

MarketDataTool marketData = new MarketDataTool(config);
```

### Provider Capabilities

```java
// Check provider capabilities
boolean supportsM1 = marketData.supportsTimeFrame(TimeFrame.M1);
boolean supportsStreaming = marketData.supportsStreaming();

String providerName = marketData.getProviderName();
MarketType supportedMarket = marketData.getSupportedMarketType();

System.out.printf(
    "Provider: %s, Market: %s, Supports M1: %s, Streaming: %s%n",
    providerName, supportedMarket, supportsM1, supportsStreaming
);
```

---

## Error Handling

### Check Result Success

```java
OHLCResult result = marketData.getOHLCBars("INVALID", TimeFrame.D1, 30).block();

if (!result.isSuccess()) {
    System.err.printf("Failed to retrieve data: %s%n", result.getErrorMessage());
    return;
}

// Process successful result
List<OHLCBar> bars = result.getBars();
```

### Handle Exceptions

```java
try {
    OHLCResult result = marketData.getOHLCBars("", TimeFrame.D1, 30).block();
} catch (IllegalArgumentException e) {
    System.err.println("Invalid symbol: " + e.getMessage());
} catch (Exception e) {
    System.err.println("Unexpected error: " + e.getMessage());
}
```

### Reactive Error Handling

```java
marketData.getOHLCBars("AAPL", TimeFrame.D1, 30)
    .doOnSuccess(result -> {
        if (result.isSuccess()) {
            System.out.println("Success: " + result.getCount() + " bars");
        } else {
            System.err.println("Error: " + result.getErrorMessage());
        }
    })
    .onErrorResume(e -> {
        System.err.println("Exception: " + e.getMessage());
        return Mono.empty();
    })
    .subscribe();
```

---

## Best Practices

### 1. Use Appropriate Time Frames

```java
// ✅ GOOD: Use appropriate time frame for use case
OHLCResult dayTrading = marketData.getOHLCBars("AAPL", TimeFrame.M15, 100).block();
OHLCResult longTerm = marketData.getOHLCBars("AAPL", TimeFrame.D1, 365).block();

// ❌ BAD: Using M1 for long-term analysis (excessive data)
OHLCResult tooMuchData = marketData.getOHLCBars("AAPL", TimeFrame.M1, 1000).block();
```

### 2. Respect Rate Limits

```java
// ✅ GOOD: Configure rate limits appropriately
MarketDataConfig config = MarketDataConfig.builder()
    .provider(DataProvider.ALPHA_VANTAGE)
    .apiKey(apiKey)
    .rateLimitPerMinute(5)  // Match provider's free tier
    .build();

// ❌ BAD: Exceeding rate limits
for (String symbol : hugeSymbolList) {
    marketData.getOHLCBars(symbol, TimeFrame.D1, 30).block();  // May hit rate limit
}
```

### 3. Use Caching for Repeated Queries

```java
// ✅ GOOD: Enable caching for repeated queries
MarketDataConfig config = MarketDataConfig.builder()
    .enableCaching(true)
    .cacheTTLSeconds(900)  // 15 minutes
    .build();

// First call: Fetches from API
OHLCResult result1 = marketData.getOHLCBars("AAPL", TimeFrame.D1, 30).block();

// Second call: Served from cache
OHLCResult result2 = marketData.getOHLCBars("AAPL", TimeFrame.D1, 30).block();
assert result2.isCached();
```

### 4. Validate Input

```java
// ✅ GOOD: Validate before making API calls
private void fetchData(String symbol, int days) {
    if (symbol == null || symbol.isEmpty()) {
        throw new IllegalArgumentException("Symbol required");
    }
    if (days <= 0 || days > 365) {
        throw new IllegalArgumentException("Days must be 1-365");
    }

    OHLCResult result = marketData.getOHLCBars(symbol, TimeFrame.D1, days).block();
}
```

### 5. Use Mock Provider for Testing

```java
// ✅ GOOD: Use Mock provider in tests
@Test
public void testPriceAnalysis() {
    MarketDataTool marketData = new MarketDataTool(MarketDataConfig.mock());

    OHLCResult result = marketData.getOHLCBars("TEST", TimeFrame.D1, 10).block();

    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals(10, result.getCount());
}
```

### 6. Handle Connection Lifecycle

```java
// ✅ GOOD: Explicit connection management
MarketDataTool marketData = new MarketDataTool(config);

try {
    marketData.connect().block();

    // Perform operations
    OHLCResult result = marketData.getOHLCBars("AAPL", TimeFrame.D1, 30).block();

} finally {
    marketData.disconnect().block();
}
```

### 7. Log Important Operations

```java
// ✅ GOOD: Enable logging for debugging
MarketDataConfig config = MarketDataConfig.builder()
    .enableLogging(true)
    .build();

// Logs: [Mock] getOHLCBars - AAPL D1 limit=30
```

---

*Last Updated: 2025-10-25*
