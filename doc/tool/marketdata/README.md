# Market Data Tool

**Version:** 0.1.0
**Status:** Alpha (Mock provider fully implemented, Alpha Vantage/Yahoo Finance stubs)
**Date:** 2025-10-25

---

## Overview

The Market Data Tool provides access to financial market data (OHLC bars, real-time quotes, historical data) through multiple data provider backends. It enables retrieval of stock, cryptocurrency, forex, and commodity price data for analysis, trading algorithms, and financial applications.

### Key Features

✅ **Multi-Provider Support**
- Alpha Vantage (stocks, forex, crypto) - stub
- Yahoo Finance (stocks, ETFs, indices) - stub
- Mock Provider (synthetic data) - **fully implemented**
- Extensible for Binance, CoinGecko, IEX Cloud, Polygon.io

✅ **Comprehensive Data**
- OHLC candlestick bars at 9 time frames (M1, M5, M15, M30, H1, H4, D1, W1, MN1)
- Real-time quotes (bid, ask, last, volume)
- Historical data by date range
- Symbol search and metadata

✅ **Production-Ready**
- Rate limiting (configurable per provider)
- Response caching (15-minute default TTL)
- Retry logic with exponential backoff
- Input validation and error handling
- Connection pooling support

✅ **LLM Integration**
- Natural language queries → market data
- "Get Apple stock prices for last month"
- "Show me Bitcoin 1-hour candles"

---

## Quick Start

### Basic Usage

```java
@Inject
private MarketDataTool marketData;

// Get daily OHLC for Apple stock (last 30 days)
OHLCResult result = marketData.getOHLCBars("AAPL", TimeFrame.D1, 30).block();

System.out.println(result);
// ✅ AAPL D1 bars: 30 records from Mock (cached: false, 45ms)

// Access the bars
for (OHLCBar bar : result.getBars()) {
    System.out.println(bar);
    // AAPL [2025-10-20T00:00:00Z] O:175.23 H:177.45 L:174.80 C:176.92 V:52300000 (+0.96%)
}
```

### Switch Provider

```java
// Use Yahoo Finance
marketData.setConfig(MarketDataConfig.yahooFinance());

// Use Alpha Vantage with API key
marketData.setConfig(MarketDataConfig.alphaVantage("your-api-key"));

// Use Mock provider for testing
marketData.setConfig(MarketDataConfig.mock());
```

### Get Real-Time Quote

```java
QuoteData quote = marketData.getQuote("TSLA").block();

System.out.println(quote);
// TSLA: $245.67 (bid: $245.62, ask: $245.72) +2.34% vol: 1000000
```

---

## Supported Providers

|     Provider      |    Status     |            Markets             |     Free Tier      | Time Frames |
|-------------------|---------------|--------------------------------|--------------------|-------------|
| **Mock**          | ✅ Implemented | All                            | Unlimited          | All         |
| **Alpha Vantage** | 🟡 Stub       | Stocks, Forex, Crypto          | 5 req/min, 500/day | All         |
| **Yahoo Finance** | 🟡 Stub       | Stocks, ETFs, Indices          | Best effort        | D1, W1, MN1 |
| **Binance**       | ⚪ Planned     | Crypto                         | High limits        | All         |
| **CoinGecko**     | ⚪ Planned     | Crypto                         | 10-50/min          | All         |
| **IEX Cloud**     | ⚪ Planned     | Stocks, ETFs                   | Paid               | All         |
| **Polygon.io**    | ⚪ Planned     | Stocks, Options, Forex, Crypto | Paid               | All         |

---

## Time Frames

| Code |  Duration  | Standard Format |        Use Case        |
|------|------------|-----------------|------------------------|
| M1   | 1 minute   | `1m`            | High-frequency trading |
| M5   | 5 minutes  | `5m`            | Intraday scalping      |
| M15  | 15 minutes | `15m`           | Intraday trading       |
| M30  | 30 minutes | `30m`           | Swing trading          |
| H1   | 1 hour     | `1h`            | Day trading            |
| H4   | 4 hours    | `4h`            | Position trading       |
| D1   | 1 day      | `1d`            | Daily analysis         |
| W1   | 1 week     | `1w`            | Weekly trends          |
| MN1  | 1 month    | `1M`            | Long-term analysis     |

---

## Installation

### Maven Dependency

```xml
<dependency>
    <groupId>dev.adeengineer</groupId>
    <artifactId>adentic-boot</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Configuration

**application.yml**

```yaml
market-data:
  provider: ALPHA_VANTAGE
  api-key: ${ALPHA_VANTAGE_API_KEY}
  rate-limit-per-minute: 5
  enable-caching: true
  cache-ttl-seconds: 900
```

---

## Documentation

- **[Architecture](architecture.md)** - Design details and component overview
- **[Usage Guide](usage-guide.md)** - Detailed usage examples
- **[Configuration](configuration.md)** - Configuration options
- **[Examples](examples.md)** - Real-world code examples
- **[LLM Integration](llm-integration.md)** - LLM agent integration guide

---

## Common Use Cases

### 1. Historical Analysis

```java
// Get last 90 days of Apple daily prices
OHLCResult result = marketData.getOHLCBars("AAPL", TimeFrame.D1, 90).block();
```

### 2. Intraday Trading

```java
// Get last 24 hours of Bitcoin 1-hour candles
marketData.setConfig(MarketDataConfig.binance("api-key"));
OHLCResult btc = marketData.getOHLCBars("BTCUSDT", TimeFrame.H1, 24).block();
```

### 3. Symbol Search

```java
// Search for Apple-related symbols
List<Symbol> symbols = marketData.searchSymbols("Apple").block();
```

### 4. Real-Time Monitoring

```java
// Get current Tesla quote
QuoteData quote = marketData.getQuote("TSLA").block();
System.out.printf("TSLA: $%.2f%n", quote.getLastPrice());
```

---

## Error Handling

```java
OHLCResult result = marketData.getOHLCBars("INVALID", TimeFrame.D1, 30).block();

if (!result.isSuccess()) {
    System.err.println("Error: " + result.getErrorMessage());
}
```

---

## API Rate Limits

|   Provider    |     Free Tier      |    Recommended Config     |
|---------------|--------------------|---------------------------|
| Alpha Vantage | 5 req/min, 500/day | `rateLimitPerMinute: 5`   |
| Yahoo Finance | Best effort        | `rateLimitPerMinute: 60`  |
| Binance       | 1200 req/min       | `rateLimitPerMinute: 100` |
| CoinGecko     | 10-50 req/min      | `rateLimitPerMinute: 10`  |

---

## Testing

Use the Mock provider for development and testing:

```java
MarketDataTool marketData = new MarketDataTool(MarketDataConfig.mock());

// Returns synthetic, realistic OHLC data
OHLCResult result = marketData.getOHLCBars("TEST", TimeFrame.D1, 10).block();

assertTrue(result.isSuccess());
assertEquals(10, result.getCount());
```

---

## Getting API Keys

### Alpha Vantage

1. Visit https://www.alphavantage.co/support/#api-key
2. Sign up for free API key
3. Free tier: 5 requests/minute, 500 requests/day

### Binance

1. Visit https://www.binance.com/en/my/settings/api-management
2. Create API key (optional for public data)
3. Free tier: High rate limits

---

## Future Enhancements

- [ ] Complete Alpha Vantage implementation with HTTP client
- [ ] Complete Yahoo Finance implementation
- [ ] Add Binance provider
- [ ] Add CoinGecko provider
- [ ] WebSocket streaming support
- [ ] Technical indicators (SMA, EMA, RSI, MACD)
- [ ] Options data support
- [ ] Financial news integration
- [ ] Price alerts and notifications

---

*Last Updated: 2025-10-25*
