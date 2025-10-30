# Market Data Tool - LLM Integration Guide

**Version:** 0.1.0
**Category:** Tool Integration
**Status:** Alpha (Mock Provider Fully Implemented)
**Date:** 2025-10-25

---

## TL;DR

**Market Data Tool enables LLMs to retrieve financial market data through natural language**. Agents analyze user requests, select appropriate data operations (OHLC bars, quotes, historical data, symbol search), extract parameters, and fetch market data from multiple providers. **Benefits**: Natural language → market data, multi-provider support, real-time quotes, historical analysis. **Use cases**: Price analysis, trading strategies, portfolio tracking, market research.

---

## Table of Contents

- [Overview](#overview)
- [Integration Architecture](#integration-architecture)
- [Tool Registration](#tool-registration)
- [LLM Workflow Examples](#llm-workflow-examples)
- [Tool Descriptor Format](#tool-descriptor-format)
- [Parameter Mapping](#parameter-mapping)
- [Error Handling](#error-handling)
- [Security Considerations](#security-considerations)
- [Use Cases](#use-cases)
- [Best Practices](#best-practices)
- [Complete Integration Example](#complete-integration-example)

---

## Overview

The Market Data Tool integrates with LLM-based agents to provide financial market data access through natural language. The tool handles the complexity of API calls, data formatting, and provider management while the LLM focuses on understanding user intent.

### Integration Flow

```
┌──────────────────────────────────────────────────────────────┐
│                         User Input                            │
│    "Show me Apple stock prices for the last month"          │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                      LLM/AI Agent                             │
│  • Analyzes intent: "retrieve stock prices"                  │
│  • Identifies symbol: "AAPL" (Apple)                         │
│  • Identifies timeframe: "last month" → D1, 30 days          │
│  • Selects tool: market-data                                 │
│  • Extracts parameters: symbol, timeFrame, limit             │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                    MarketDataTool                             │
│  • Method: getOHLCBars(symbol, timeFrame, limit)             │
│  • Provider: AlphaVantageProvider                            │
│  • Execution: HTTP GET to Alpha Vantage API                  │
│  • URL: /query?function=TIME_SERIES_DAILY&symbol=AAPL       │
│  • Parse: JSON → List<OHLCBar>                               │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                     OHLCResult                                │
│  • success: true                                             │
│  • bars: [{timestamp, open, high, low, close, volume}, ...]  │
│  • count: 30                                                 │
│  • symbol: "AAPL"                                            │
│  • timeFrame: D1                                             │
│  • provider: "Alpha Vantage"                                 │
│  • executionTimeMs: 850                                      │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                      LLM Response                             │
│  "Here are Apple's daily closing prices for the last month:  │
│                                                               │
│   Oct 1:  $175.23 (+0.5%)                                    │
│   Oct 2:  $176.45 (+0.7%)                                    │
│   ...                                                         │
│   Oct 25: $180.75 (+3.1% from month start)                   │
│                                                               │
│   The stock has shown positive momentum with an overall      │
│   gain of 3.1% over the past month."                         │
└──────────────────────────────────────────────────────────────┘
```

---

## Integration Architecture

### Component Roles

**1. LLM/AI Agent**
- Understands natural language market queries
- Maps company names to ticker symbols ("Apple" → "AAPL")
- Interprets time expressions ("last week" → 7 days, D1)
- Selects appropriate data operation (OHLC, quote, search)
- Formats results for user presentation
- Provides market insights and analysis

**2. MarketDataTool (@Tool)**
- Provides consistent API for market data access
- Routes to appropriate provider (Alpha Vantage, Yahoo, Mock)
- Returns structured results (OHLCResult, QuoteData, Symbol)
- Manages provider lifecycle and switching

**3. MarketDataProvider (Alpha Vantage, Yahoo, Mock, etc.)**
- Executes HTTP API calls to data providers
- Parses JSON/CSV responses
- Maps provider data to common model (OHLCBar)
- Handles rate limiting and retries
- Manages caching

**4. Result Models**
- **OHLCResult** - Collection of candlestick bars with metadata
- **OHLCBar** - Single candlestick (open, high, low, close, volume)
- **QuoteData** - Real-time quote (bid, ask, last, volume)
- **Symbol** - Trading symbol information (ticker, name, exchange)

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│                      LLM/AI Agent                        │
│  • Natural language understanding                       │
│  • Symbol mapping (Apple → AAPL)                        │
│  • Time expression parsing                              │
│  • Result analysis and insights                         │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│              MarketDataTool (@Tool)                      │
│  • getOHLCBars(symbol, timeFrame, limit)                │
│  • getQuote(symbol)                                     │
│  • searchSymbols(query)                                 │
│  • getHistoricalData(symbol, tf, start, end)            │
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
```

---

## Tool Registration

The Market Data Tool is registered with the `@Tool` annotation:

```java
@Tool(name = "market-data")
public class MarketDataTool {
    // Registered for LLM agent discovery
    // Agents can invoke market data operations
}
```

**Registration details:**
- **Tool name:** `market-data`
- **Capabilities:** OHLC data, real-time quotes, historical data, symbol search
- **Providers:** Alpha Vantage, Yahoo Finance, Mock (+ Binance, CoinGecko planned)
- **Reactive API:** All methods return `Mono<T>` or `Flux<T>` for async execution

---

## LLM Workflow Examples

### Example 1: Daily Price Retrieval

**User Request:**

> "Get Apple stock prices for the last 30 days"

**LLM Analysis:**

```json
{
  "intent": "retrieve_ohlc_data",
  "operation": "getOHLCBars",
  "parameters": {
    "symbol": "AAPL",
    "timeFrame": "D1",
    "limit": 30
  },
  "symbolMapping": {
    "userInput": "Apple",
    "ticker": "AAPL",
    "confidence": "high"
  }
}
```

**Agent Code:**

```java
OHLCResult result = marketDataTool.getOHLCBars("AAPL", TimeFrame.D1, 30).block();
```

**Execution Result:**

```json
{
  "success": true,
  "bars": [
    {
      "timestamp": "2025-10-25T00:00:00Z",
      "open": 180.10,
      "high": 181.30,
      "low": 179.80,
      "close": 180.75,
      "volume": 46500000,
      "symbol": "AAPL",
      "timeFrame": "D1"
    },
    // ... 29 more bars
  ],
  "count": 30,
  "symbol": "AAPL",
  "timeFrame": "D1",
  "provider": "Mock",
  "cached": false,
  "executionTimeMs": 45
}
```

**LLM Response:**

> "Here are Apple's daily closing prices for the last 30 days:
>
> **Recent Trend:** The stock has climbed from $175.23 to $180.75, a gain of **+3.1%** over the past month.
>
> **Key Observations:**
> - Highest close: $180.75 (today)
> - Lowest close: $174.50 (Oct 5)
> - Average volume: 49.2M shares/day
> - Bullish days: 18 out of 30 (60%)
>
> The upward momentum suggests positive sentiment around Apple stock."

---

### Example 2: Real-Time Quote

**User Request:**

> "What's the current price of Tesla?"

**LLM Analysis:**

```json
{
  "intent": "get_realtime_quote",
  "operation": "getQuote",
  "parameters": {
    "symbol": "TSLA"
  }
}
```

**Agent Code:**

```java
QuoteData quote = marketDataTool.getQuote("TSLA").block();
```

**Result:**

```json
{
  "symbol": "TSLA",
  "lastPrice": 245.67,
  "bidPrice": 245.62,
  "askPrice": 245.72,
  "bidSize": 100,
  "askSize": 100,
  "volume": 1000000,
  "openPrice": 240.50,
  "highPrice": 247.80,
  "lowPrice": 239.90,
  "previousClose": 240.00,
  "timestamp": "2025-10-25T14:30:00Z",
  "provider": "Mock"
}
```

**LLM Response:**

> "Tesla (TSLA) is currently trading at **$245.67**, up **$5.67 (+2.36%)** from yesterday's close of $240.00.
>
> **Today's Range:** $239.90 - $247.80
> **Volume:** 1.0M shares
> **Bid/Ask:** $245.62 / $245.72 (spread: $0.10)
>
> The stock is showing strong positive momentum today."

---

### Example 3: Intraday Analysis

**User Request:**

> "Show me Bitcoin's 1-hour candles for the last 24 hours"

**LLM Analysis:**

```json
{
  "intent": "retrieve_intraday_data",
  "operation": "getOHLCBars",
  "parameters": {
    "symbol": "BTCUSDT",
    "timeFrame": "H1",
    "limit": 24
  },
  "providerSuggestion": "BINANCE",
  "marketType": "CRYPTO"
}
```

**Agent Code:**

```java
// Switch to Binance provider for crypto
marketDataTool.setConfig(MarketDataConfig.binance("api-key"));

OHLCResult result = marketDataTool.getOHLCBars("BTCUSDT", TimeFrame.H1, 24).block();
```

**Result:**

```json
{
  "success": true,
  "bars": [
    {
      "timestamp": "2025-10-25T14:00:00Z",
      "open": 67250.50,
      "high": 67380.00,
      "low": 67100.00,
      "close": 67325.75,
      "volume": 125.45,
      "symbol": "BTCUSDT",
      "timeFrame": "H1"
    },
    // ... 23 more hourly bars
  ],
  "count": 24,
  "symbol": "BTCUSDT",
  "timeFrame": "H1",
  "provider": "Binance",
  "executionTimeMs": 120
}
```

**LLM Response:**

> "Here's Bitcoin's price action over the last 24 hours (1-hour candles):
>
> **24-Hour Performance:**
> - Opening: $66,500.00
> - Current: $67,325.75
> - Change: **+$825.75 (+1.24%)**
> - High: $67,850.00 (at 10:00 AM)
> - Low: $66,200.00 (at 3:00 AM)
>
> **Analysis:** Bitcoin has been trending upward with 15 bullish hours out of 24. The recent breakout above $67,000 shows strong buying pressure."

---

### Example 4: Historical Date Range

**User Request:**

> "Get Google's daily prices from January 1 to March 31, 2025"

**LLM Analysis:**

```json
{
  "intent": "retrieve_historical_data",
  "operation": "getHistoricalData",
  "parameters": {
    "symbol": "GOOGL",
    "timeFrame": "D1",
    "startDate": "2025-01-01",
    "endDate": "2025-03-31"
  }
}
```

**Agent Code:**

```java
LocalDate startDate = LocalDate.of(2025, 1, 1);
LocalDate endDate = LocalDate.of(2025, 3, 31);

OHLCResult result = marketDataTool.getHistoricalData(
    "GOOGL",
    TimeFrame.D1,
    startDate,
    endDate
).block();
```

**Result:**

```json
{
  "success": true,
  "count": 90,
  "symbol": "GOOGL",
  "timeFrame": "D1",
  "provider": "Yahoo Finance",
  "executionTimeMs": 1250
}
```

**LLM Response:**

> "I've retrieved 90 days of Google (GOOGL) daily prices from January 1 to March 31, 2025:
>
> **Q1 2025 Performance:**
> - Starting price (Jan 1): $142.50
> - Ending price (Mar 31): $156.75
> - Quarterly gain: **+$14.25 (+10.0%)**
>
> **Statistics:**
> - Highest close: $158.20 (Mar 25)
> - Lowest close: $139.80 (Jan 15)
> - Average daily volume: 22.5M shares
>
> Google had a strong Q1 with consistent upward momentum."

---

### Example 5: Symbol Search

**User Request:**

> "Find all Apple-related stocks"

**LLM Analysis:**

```json
{
  "intent": "search_symbols",
  "operation": "searchSymbols",
  "parameters": {
    "query": "Apple"
  }
}
```

**Agent Code:**

```java
List<Symbol> symbols = marketDataTool.searchSymbols("Apple").block();
```

**Result:**

```json
[
  {
    "ticker": "AAPL",
    "name": "Apple Inc.",
    "exchange": "NASDAQ",
    "marketType": "STOCKS",
    "currency": "USD",
    "region": "US",
    "active": true,
    "description": "Technology company"
  },
  {
    "ticker": "GOOGL",
    "name": "Alphabet Inc.",
    "exchange": "NASDAQ",
    "marketType": "STOCKS",
    "currency": "USD",
    "region": "US",
    "active": true
  }
]
```

**LLM Response:**

> "I found 2 Apple-related stocks:
>
> 1. **AAPL** - Apple Inc. (NASDAQ)
>    - Market: Stocks
>    - Currency: USD
>    - Status: Active
> 2. **GOOGL** - Alphabet Inc. (NASDAQ)
>    - Market: Stocks
>    - Currency: USD
>    - Status: Active
>
> The primary symbol for Apple Inc. is **AAPL** on the NASDAQ exchange."

---

## Tool Descriptor Format

### OpenAI Function Calling Format

LLMs use these tool descriptors to understand available operations:

#### 1. Get OHLC Bars

```json
{
  "name": "marketdata_get_ohlc_bars",
  "description": "Retrieve OHLC (candlestick) price bars for a trading symbol",
  "parameters": {
    "type": "object",
    "properties": {
      "symbol": {
        "type": "string",
        "description": "Trading symbol/ticker (e.g., 'AAPL', 'BTCUSDT', 'EUR/USD')"
      },
      "timeFrame": {
        "type": "string",
        "enum": ["M1", "M5", "M15", "M30", "H1", "H4", "D1", "W1", "MN1"],
        "description": "Candlestick time frame (M1=1min, H1=1hour, D1=1day, etc.)"
      },
      "limit": {
        "type": "integer",
        "description": "Number of bars to retrieve (max 1000)",
        "minimum": 1,
        "maximum": 1000,
        "default": 30
      }
    },
    "required": ["symbol", "timeFrame"]
  }
}
```

#### 2. Get Quote

```json
{
  "name": "marketdata_get_quote",
  "description": "Get current real-time quote for a trading symbol",
  "parameters": {
    "type": "object",
    "properties": {
      "symbol": {
        "type": "string",
        "description": "Trading symbol/ticker (e.g., 'AAPL', 'TSLA')"
      }
    },
    "required": ["symbol"]
  }
}
```

#### 3. Get Historical Data

```json
{
  "name": "marketdata_get_historical",
  "description": "Retrieve historical OHLC data by date range",
  "parameters": {
    "type": "object",
    "properties": {
      "symbol": {
        "type": "string",
        "description": "Trading symbol/ticker"
      },
      "timeFrame": {
        "type": "string",
        "enum": ["M1", "M5", "M15", "M30", "H1", "H4", "D1", "W1", "MN1"],
        "description": "Candlestick time frame"
      },
      "startDate": {
        "type": "string",
        "format": "date",
        "description": "Start date (YYYY-MM-DD)"
      },
      "endDate": {
        "type": "string",
        "format": "date",
        "description": "End date (YYYY-MM-DD)"
      }
    },
    "required": ["symbol", "timeFrame", "startDate", "endDate"]
  }
}
```

#### 4. Search Symbols

```json
{
  "name": "marketdata_search_symbols",
  "description": "Search for trading symbols by name or ticker",
  "parameters": {
    "type": "object",
    "properties": {
      "query": {
        "type": "string",
        "description": "Search query (company name, ticker, or keyword)"
      }
    },
    "required": ["query"]
  }
}
```

---

## Parameter Mapping

### Symbol Mapping

LLMs should map user-friendly names to ticker symbols:

|  User Input   | Ticker Symbol | Exchange | Market Type |
|---------------|---------------|----------|-------------|
| "Apple"       | AAPL          | NASDAQ   | STOCKS      |
| "Tesla"       | TSLA          | NASDAQ   | STOCKS      |
| "Google"      | GOOGL         | NASDAQ   | STOCKS      |
| "Bitcoin"     | BTCUSDT       | Binance  | CRYPTO      |
| "Ethereum"    | ETHUSDT       | Binance  | CRYPTO      |
| "Euro Dollar" | EUR/USD       | Forex    | FOREX       |
| "Gold"        | XAUUSD        | Forex    | COMMODITIES |

### Time Frame Mapping

LLMs should interpret time expressions:

| User Expression | Time Frame |            Limit Calculation            |
|-----------------|------------|-----------------------------------------|
| "last hour"     | M1         | 60 bars                                 |
| "last day"      | M15 or H1  | 96 or 24 bars                           |
| "last week"     | D1         | 7 bars                                  |
| "last month"    | D1         | 30 bars                                 |
| "last year"     | W1 or D1   | 52 or 365 bars                          |
| "today"         | M5 or M15  | (current time - market open) / interval |
| "this week"     | D1         | Days elapsed this week                  |

### Provider Selection

LLMs should suggest appropriate providers based on market type:

| Market Type |      Suggested Provider      |        Reason         |
|-------------|------------------------------|-----------------------|
| Stocks (US) | ALPHA_VANTAGE, YAHOO_FINANCE | Best coverage         |
| Crypto      | BINANCE, COINGECKO           | Real-time crypto data |
| Forex       | ALPHA_VANTAGE                | Supports forex pairs  |
| Commodities | ALPHA_VANTAGE                | Supports commodities  |

---

## Error Handling

### Common Error Scenarios

**1. Symbol Not Found**

```json
{
  "success": false,
  "symbol": "INVALIDDD",
  "errorMessage": "Symbol not found: INVALIDDD"
}
```

**LLM Handling:**
- Suggest alternative spellings
- Search for similar symbols
- Inform user to check ticker symbol

**2. Rate Limit Exceeded**

```json
{
  "success": false,
  "symbol": "AAPL",
  "errorMessage": "Rate limit exceeded (5 req/min)"
}
```

**LLM Handling:**
- Inform user about rate limits
- Suggest waiting or upgrading API tier
- Use cached data if available

**3. Invalid Time Frame**

```json
{
  "success": false,
  "symbol": "AAPL",
  "errorMessage": "Provider does not support M1 time frame"
}
```

**LLM Handling:**
- Suggest supported time frames
- Recommend different provider
- Use nearest supported time frame

**4. API Timeout**

```json
{
  "success": false,
  "symbol": "AAPL",
  "errorMessage": "Request timeout after 30000ms"
}
```

**LLM Handling:**
- Retry with exponential backoff
- Switch to alternative provider
- Inform user of temporary issue

---

## Security Considerations

### 1. Symbol Validation

**Problem:** Prevent injection attacks through symbol parameter

**Solution:** Validate symbol format in `BaseMarketDataProvider`

```java
protected void validateSymbol(String symbol) {
    if (!symbol.matches("^[A-Za-z0-9._-]+$")) {
        throw new IllegalArgumentException("Invalid symbol format");
    }
}
```

**LLM Guidance:**
- Only use alphanumeric symbols with dash/underscore/dot
- Reject symbols with special characters
- Validate before API calls

### 2. API Key Protection

**Problem:** Prevent API key exposure in logs or responses

**Solution:** Never log or return API keys

```java
// ✅ GOOD
.apiKey(System.getenv("ALPHA_VANTAGE_KEY"))

// ❌ BAD
.apiKey("abc123def456")  // Hardcoded
log.info("API Key: {}", apiKey);  // Logged
```

**LLM Guidance:**
- Never include API keys in responses
- Redact sensitive configuration from logs
- Use environment variables

### 3. Rate Limiting

**Problem:** Avoid exceeding provider rate limits

**Solution:** Configure appropriate rate limits

```java
MarketDataConfig.builder()
    .rateLimitPerMinute(5)  // Match provider tier
    .build();
```

**LLM Guidance:**
- Respect provider rate limits
- Cache results to reduce API calls
- Inform user about rate limit constraints

### 4. Data Privacy

**Problem:** Protect user trading strategies and queries

**Solution:** Don't log user queries or trading intentions

```java
// ✅ GOOD
log.debug("Retrieving OHLC for symbol");

// ❌ BAD
log.info("User querying AAPL before earnings (possible insider trading)");
```

**LLM Guidance:**
- Don't log user intent or strategy
- Respect data privacy
- Minimize data retention

---

## Use Cases

### 1. Price Analysis

**Description:** Analyze price trends and patterns

**LLM Role:**
- Retrieve historical prices
- Calculate statistics (avg, high, low)
- Identify trends (uptrend, downtrend)
- Detect patterns (support, resistance)

**Example:**

```
User: "Analyze Apple's price trend over the last 3 months"
LLM: Retrieves 90 days → Calculates SMA → Identifies uptrend → Reports findings
```

### 2. Trading Signals

**Description:** Generate buy/sell signals based on technical analysis

**LLM Role:**
- Retrieve multi-timeframe data
- Apply technical indicators
- Generate signals
- Explain reasoning

**Example:**

```
User: "Should I buy Tesla stock?"
LLM: Retrieves data → Analyzes momentum → Checks moving averages → Provides signal
```

### 3. Portfolio Tracking

**Description:** Monitor portfolio performance

**LLM Role:**
- Get current quotes for holdings
- Calculate P/L
- Track daily changes
- Provide portfolio summary

**Example:**

```
User: "How is my portfolio doing today?"
LLM: Fetches quotes for all holdings → Calculates total P/L → Summarizes performance
```

### 4. Market Research

**Description:** Research stocks and compare options

**LLM Role:**
- Search for symbols
- Compare multiple stocks
- Provide sector analysis
- Suggest alternatives

**Example:**

```
User: "Compare tech stocks: Apple, Google, Microsoft"
LLM: Retrieves data for all → Compares performance → Ranks by metrics → Recommends
```

### 5. Automated Reporting

**Description:** Generate periodic market reports

**LLM Role:**
- Retrieve daily/weekly data
- Calculate changes
- Format as report
- Highlight notable events

**Example:**

```
User: "Give me the weekly market summary"
LLM: Fetches week's data → Identifies top movers → Generates summary report
```

---

## Best Practices

### For LLM Developers

**1. Map Company Names to Symbols**

```java
// ✅ GOOD: Resolve "Apple" to "AAPL"
String ticker = symbolResolver.resolve("Apple");  // Returns "AAPL"
OHLCResult result = marketDataTool.getOHLCBars(ticker, TimeFrame.D1, 30).block();

// ❌ BAD: Use company name directly
OHLCResult result = marketDataTool.getOHLCBars("Apple", TimeFrame.D1, 30).block();
```

**2. Choose Appropriate Time Frames**

```java
// ✅ GOOD: Match time frame to use case
// "last hour" → M1 or M5
// "last week" → D1
// "last year" → W1 or MN1

// ❌ BAD: Using M1 for "last year" (excessive data)
```

**3. Provide Context in Responses**

```java
// ✅ GOOD: Rich context
"Apple (AAPL) is currently at $180.75, up 3.1% from last month's open of $175.23.
 The stock has shown consistent upward momentum with 18 out of 30 days being bullish."

// ❌ BAD: Minimal context
"AAPL is at $180.75"
```

**4. Handle Errors Gracefully**

```java
if (!result.isSuccess()) {
    return String.format(
        "Unable to retrieve data for %s: %s. Would you like to try a different symbol?",
        symbol,
        result.getErrorMessage()
    );
}
```

**5. Use Caching Wisely**

```java
// Historical data can be cached longer
marketDataTool.setConfig(MarketDataConfig.builder()
    .cacheTTLSeconds(3600)  // 1 hour for historical
    .build());

// Real-time quotes need fresh data
marketDataTool.setConfig(MarketDataConfig.builder()
    .cacheTTLSeconds(60)  // 1 minute for quotes
    .build());
```

### For Agent Implementers

**1. Implement Retry Logic**

```java
int maxRetries = 3;
for (int i = 0; i < maxRetries; i++) {
    try {
        OHLCResult result = marketDataTool.getOHLCBars(symbol, timeFrame, limit).block();
        if (result.isSuccess()) {
            return TaskResult.success(...);
        }
    } catch (Exception e) {
        if (i == maxRetries - 1) {
            return TaskResult.failure(...);
        }
        Thread.sleep(1000 * (i + 1));  // Exponential backoff
    }
}
```

**2. Validate Symbols**

```java
private boolean isValidSymbol(String symbol) {
    return symbol != null &&
           !symbol.isEmpty() &&
           symbol.matches("^[A-Za-z0-9._-]+$") &&
           symbol.length() <= 20;
}
```

**3. Format Results Clearly**

```java
private String formatOHLCResults(OHLCResult result) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%s - %s Price Data (%d bars)%n%n",
        result.getSymbol(), result.getTimeFrame(), result.getCount()));

    for (OHLCBar bar : result.getBars()) {
        sb.append(String.format("%s: O=%.2f H=%.2f L=%.2f C=%.2f V=%.0f (%s%.2f%%)%n",
            bar.getTimestamp().toString().substring(0, 10),
            bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(),
            bar.getVolume(),
            bar.isBullish() ? "+" : "",
            bar.getPercentageChange()
        ));
    }

    return sb.toString();
}
```

**4. Select Provider Based on Market**

```java
private void selectProvider(String symbol, MarketType marketType) {
    MarketDataConfig config = switch (marketType) {
        case STOCKS -> MarketDataConfig.alphaVantage(apiKey);
        case CRYPTO -> MarketDataConfig.binance(apiKey);
        case FOREX -> MarketDataConfig.alphaVantage(apiKey);
        default -> MarketDataConfig.mock();
    };

    marketDataTool.setConfig(config);
}
```

---

## Complete Integration Example

### Scenario: Market Analysis Agent

**User Request:**

> "Analyze Tesla's performance over the last week and give me a buy/sell recommendation"

**Complete Agent Implementation:**

```java
@Tool(name = "market-analysis-agent")
public class MarketAnalysisAgent implements Agent {

    @Inject
    private MarketDataTool marketDataTool;

    @Override
    public TaskResult executeTask(TaskRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // Step 1: Extract symbol from request
            String symbol = extractSymbol(request.task());  // "Tesla" → "TSLA"

            log.info("Analyzing {} performance...", symbol);

            // Step 2: Get weekly data
            OHLCResult weeklyData = marketDataTool.getOHLCBars(
                symbol,
                TimeFrame.D1,
                7
            ).block();

            if (!weeklyData.isSuccess()) {
                return TaskResult.failure("analysis-agent", request.task(),
                    "Failed to retrieve data: " + weeklyData.getErrorMessage());
            }

            // Step 3: Calculate statistics
            List<OHLCBar> bars = weeklyData.getBars();

            BigDecimal weekStart = bars.get(bars.size() - 1).getClose();
            BigDecimal weekEnd = bars.get(0).getClose();
            BigDecimal weekChange = weekEnd.subtract(weekStart)
                .divide(weekStart, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

            long bullishDays = bars.stream().filter(OHLCBar::isBullish).count();
            BigDecimal avgVolume = bars.stream()
                .map(OHLCBar::getVolume)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(bars.size()), 0, RoundingMode.HALF_UP);

            // Step 4: Generate recommendation
            String recommendation = generateRecommendation(
                weekChange,
                bullishDays,
                avgVolume
            );

            // Step 5: Format response
            String output = String.format("""
                %s (TSLA) - Weekly Performance Analysis

                📊 Performance Metrics:
                   Week Start: $%.2f
                   Week End: $%.2f
                   Change: %s%.2f%% (%s)
                   Bullish Days: %d/7
                   Avg Daily Volume: %.0fM shares

                💡 Recommendation: %s

                Analysis based on %d days of price data from %s.
                Execution time: %dms
                """,
                symbol,
                weekStart, weekEnd,
                weekChange.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "",
                weekChange,
                weekChange.compareTo(BigDecimal.ZERO) >= 0 ? "UP" : "DOWN",
                bullishDays,
                avgVolume.divide(BigDecimal.valueOf(1000000), 1, RoundingMode.HALF_UP),
                recommendation,
                bars.size(),
                weeklyData.getProvider(),
                System.currentTimeMillis() - startTime
            );

            return TaskResult.success("analysis-agent", request.task(), output,
                Map.of(
                    "symbol", symbol,
                    "weekChange", weekChange,
                    "recommendation", recommendation
                ),
                System.currentTimeMillis() - startTime
            );

        } catch (Exception e) {
            log.error("Analysis failed", e);
            return TaskResult.failure("analysis-agent", request.task(),
                "Analysis error: " + e.getMessage());
        }
    }

    private String extractSymbol(String task) {
        // Map "Tesla" → "TSLA"
        if (task.toLowerCase().contains("tesla")) return "TSLA";
        if (task.toLowerCase().contains("apple")) return "AAPL";
        // ... more mappings
        return "UNKNOWN";
    }

    private String generateRecommendation(
        BigDecimal weekChange,
        long bullishDays,
        BigDecimal avgVolume
    ) {
        if (weekChange.compareTo(BigDecimal.valueOf(5)) > 0 && bullishDays >= 5) {
            return "STRONG BUY - Stock shows strong upward momentum";
        } else if (weekChange.compareTo(BigDecimal.valueOf(2)) > 0 && bullishDays >= 4) {
            return "BUY - Positive trend with good momentum";
        } else if (weekChange.compareTo(BigDecimal.valueOf(-2)) < 0 && bullishDays <= 3) {
            return "SELL - Negative trend with weak momentum";
        } else if (weekChange.compareTo(BigDecimal.valueOf(-5)) < 0 && bullishDays <= 2) {
            return "STRONG SELL - Stock shows strong downward momentum";
        } else {
            return "HOLD - Mixed signals, wait for clearer trend";
        }
    }

    @Override
    public String getName() {
        return "market-analysis-agent";
    }

    @Override
    public String getDescription() {
        return "Analyzes stock performance and provides buy/sell recommendations";
    }

    @Override
    public List<String> getCapabilities() {
        return List.of(
            "Stock price analysis",
            "Technical indicators",
            "Buy/sell recommendations",
            "Performance statistics"
        );
    }
}
```

**Expected Output:**

```
TSLA (TSLA) - Weekly Performance Analysis

📊 Performance Metrics:
   Week Start: $240.50
   Week End: $245.67
   Change: +2.15% (UP)
   Bullish Days: 5/7
   Avg Daily Volume: 1.2M shares

💡 Recommendation: BUY - Positive trend with good momentum

Analysis based on 7 days of price data from Mock.
Execution time: 156ms
```

---

*Last Updated: 2025-10-25*
