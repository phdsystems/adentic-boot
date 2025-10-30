# Market Data Tool - Code Examples

**Version:** 0.1.0
**Date:** 2025-10-25

---

## Table of Contents

- [Basic Examples](#basic-examples)
- [Analysis Examples](#analysis-examples)
- [Trading Strategies](#trading-strategies)
- [Multi-Symbol Analysis](#multi-symbol-analysis)
- [Integration Examples](#integration-examples)

---

## Basic Examples

### Example 1: Get Daily Prices

```java
@Component
public class PriceService {

    @Inject
    private MarketDataTool marketData;

    public void printDailyPrices(String symbol, int days) {
        OHLCResult result = marketData.getOHLCBars(symbol, TimeFrame.D1, days).block();

        if (!result.isSuccess()) {
            System.err.println("Failed to retrieve data: " + result.getErrorMessage());
            return;
        }

        System.out.printf("%s - Last %d Days%n", symbol, days);
        System.out.println("Date       | Open    | High    | Low     | Close   | Volume");
        System.out.println("-".repeat(70));

        for (OHLCBar bar : result.getBars()) {
            System.out.printf(
                "%s | $%-7.2f | $%-7.2f | $%-7.2f | $%-7.2f | %,.0f%n",
                bar.getTimestamp().toString().substring(0, 10),
                bar.getOpen(),
                bar.getHigh(),
                bar.getLow(),
                bar.getClose(),
                bar.getVolume()
            );
        }
    }
}
```

**Output:**

```
AAPL - Last 5 Days
Date       | Open    | High    | Low     | Close   | Volume
----------------------------------------------------------------------
2025-10-21 | $175.23 | $177.45 | $174.80 | $176.92 | 52,300,000
2025-10-22 | $176.85 | $178.10 | $176.20 | $177.50 | 48,700,000
2025-10-23 | $177.40 | $179.20 | $177.00 | $178.80 | 51,200,000
2025-10-24 | $178.90 | $180.50 | $178.50 | $179.95 | 49,800,000
2025-10-25 | $180.10 | $181.30 | $179.80 | $180.75 | 46,500,000
```

---

### Example 2: Calculate Price Statistics

```java
public class PriceStatistics {

    public void calculateStats(MarketDataTool marketData, String symbol) {
        OHLCResult result = marketData.getOHLCBars(symbol, TimeFrame.D1, 30).block();

        if (!result.isSuccess()) {
            return;
        }

        List<OHLCBar> bars = result.getBars();

        // Calculate statistics
        BigDecimal avgClose = bars.stream()
            .map(OHLCBar::getClose)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(bars.size()), 2, RoundingMode.HALF_UP);

        BigDecimal maxHigh = bars.stream()
            .map(OHLCBar::getHigh)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        BigDecimal minLow = bars.stream()
            .map(OHLCBar::getLow)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        long totalVolume = bars.stream()
            .map(OHLCBar::getVolume)
            .map(BigDecimal::longValue)
            .reduce(0L, Long::sum);

        long bullishDays = bars.stream()
            .filter(OHLCBar::isBullish)
            .count();

        System.out.printf("Statistics for %s (30 days):%n", symbol);
        System.out.printf("  Average Close: $%.2f%n", avgClose);
        System.out.printf("  30-Day High: $%.2f%n", maxHigh);
        System.out.printf("  30-Day Low: $%.2f%n", minLow);
        System.out.printf("  Total Volume: %,d%n", totalVolume);
        System.out.printf("  Bullish Days: %d/%d (%.1f%%)%n",
            bullishDays, bars.size(), (bullishDays * 100.0) / bars.size());
    }
}
```

---

## Analysis Examples

### Example 3: Moving Average Calculation

```java
public class MovingAverageCalculator {

    public BigDecimal calculateSMA(List<OHLCBar> bars, int period) {
        if (bars.size() < period) {
            throw new IllegalArgumentException("Not enough bars for SMA calculation");
        }

        return bars.stream()
            .limit(period)
            .map(OHLCBar::getClose)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(period), 2, RoundingMode.HALF_UP);
    }

    public void analyzeWithSMA(MarketDataTool marketData, String symbol) {
        OHLCResult result = marketData.getOHLCBars(symbol, TimeFrame.D1, 50).block();

        List<OHLCBar> bars = result.getBars();

        BigDecimal sma20 = calculateSMA(bars, 20);
        BigDecimal sma50 = calculateSMA(bars, 50);
        BigDecimal currentPrice = bars.get(0).getClose();

        System.out.printf("%s Analysis:%n", symbol);
        System.out.printf("  Current Price: $%.2f%n", currentPrice);
        System.out.printf("  20-Day SMA: $%.2f%n", sma20);
        System.out.printf("  50-Day SMA: $%.2f%n", sma50);

        if (currentPrice.compareTo(sma20) > 0 && currentPrice.compareTo(sma50) > 0) {
            System.out.println("  Signal: BULLISH (price above both SMAs)");
        } else if (currentPrice.compareTo(sma20) < 0 && currentPrice.compareTo(sma50) < 0) {
            System.out.println("  Signal: BEARISH (price below both SMAs)");
        } else {
            System.out.println("  Signal: NEUTRAL");
        }
    }
}
```

---

### Example 4: Candlestick Pattern Detection

```java
public class CandlestickPatterns {

    public void detectPatterns(MarketDataTool marketData, String symbol) {
        OHLCResult result = marketData.getOHLCBars(symbol, TimeFrame.D1, 10).block();

        List<OHLCBar> bars = result.getBars();

        System.out.printf("Candlestick Patterns for %s:%n", symbol);

        for (int i = 0; i < bars.size(); i++) {
            OHLCBar bar = bars.get(i);
            String pattern = detectPattern(bar, i > 0 ? bars.get(i - 1) : null);

            if (pattern != null) {
                System.out.printf("  %s: %s%n",
                    bar.getTimestamp().toString().substring(0, 10),
                    pattern
                );
            }
        }
    }

    private String detectPattern(OHLCBar current, OHLCBar previous) {
        // Doji
        if (current.isDoji()) {
            return "Doji (indecision)";
        }

        // Bullish Engulfing
        if (previous != null &&
            previous.isBearish() &&
            current.isBullish() &&
            current.getOpen().compareTo(previous.getClose()) < 0 &&
            current.getClose().compareTo(previous.getOpen()) > 0) {
            return "Bullish Engulfing (reversal signal)";
        }

        // Bearish Engulfing
        if (previous != null &&
            previous.isBullish() &&
            current.isBearish() &&
            current.getOpen().compareTo(previous.getClose()) > 0 &&
            current.getClose().compareTo(previous.getOpen()) < 0) {
            return "Bearish Engulfing (reversal signal)";
        }

        // Hammer (potential reversal)
        BigDecimal body = current.getClose().subtract(current.getOpen()).abs();
        BigDecimal lowerShadow = current.getOpen().min(current.getClose()).subtract(current.getLow());

        if (lowerShadow.compareTo(body.multiply(BigDecimal.valueOf(2))) > 0) {
            return "Hammer (potential reversal)";
        }

        return null;
    }
}
```

---

## Trading Strategies

### Example 5: Simple Momentum Strategy

```java
@Component
public class MomentumStrategy {

    @Inject
    private MarketDataTool marketData;

    public String analyzeSignal(String symbol) {
        OHLCResult result = marketData.getOHLCBars(symbol, TimeFrame.D1, 5).block();

        if (!result.isSuccess()) {
            return "ERROR";
        }

        List<OHLCBar> bars = result.getBars();

        // Check for consecutive bullish days
        long consecutiveBullish = bars.stream()
            .filter(OHLCBar::isBullish)
            .count();

        // Calculate momentum
        BigDecimal firstClose = bars.get(bars.size() - 1).getClose();
        BigDecimal lastClose = bars.get(0).getClose();
        BigDecimal momentum = lastClose.subtract(firstClose)
            .divide(firstClose, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        System.out.printf("%s Momentum Analysis:%n", symbol);
        System.out.printf("  5-Day Momentum: %.2f%%%n", momentum);
        System.out.printf("  Bullish Days: %d/5%n", consecutiveBullish);

        if (momentum.compareTo(BigDecimal.valueOf(5)) > 0 && consecutiveBullish >= 4) {
            return "STRONG BUY";
        } else if (momentum.compareTo(BigDecimal.valueOf(2)) > 0 && consecutiveBullish >= 3) {
            return "BUY";
        } else if (momentum.compareTo(BigDecimal.valueOf(-2)) < 0 && consecutiveBullish <= 2) {
            return "SELL";
        } else if (momentum.compareTo(BigDecimal.valueOf(-5)) < 0 && consecutiveBullish <= 1) {
            return "STRONG SELL";
        } else {
            return "HOLD";
        }
    }
}
```

---

## Multi-Symbol Analysis

### Example 6: Portfolio Tracking

```java
public class PortfolioTracker {

    private final MarketDataTool marketData;

    record Holding(String symbol, BigDecimal shares, BigDecimal avgCost) {}

    public void trackPortfolio(List<Holding> holdings) {
        System.out.println("Portfolio Summary:");
        System.out.println("Symbol | Shares | Avg Cost | Current | P/L      | P/L %");
        System.out.println("-".repeat(65));

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (Holding holding : holdings) {
            QuoteData quote = marketData.getQuote(holding.symbol()).block();

            BigDecimal currentPrice = quote.getLastPrice();
            BigDecimal currentValue = currentPrice.multiply(holding.shares());
            BigDecimal cost = holding.avgCost().multiply(holding.shares());
            BigDecimal profitLoss = currentValue.subtract(cost);
            BigDecimal profitLossPct = profitLoss.divide(cost, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

            System.out.printf(
                "%-6s | %6.0f | $%7.2f | $%7.2f | $%8.2f | %+6.2f%%%n",
                holding.symbol(),
                holding.shares(),
                holding.avgCost(),
                currentPrice,
                profitLoss,
                profitLossPct
            );

            totalValue = totalValue.add(currentValue);
            totalCost = totalCost.add(cost);
        }

        BigDecimal totalPL = totalValue.subtract(totalCost);
        BigDecimal totalPLPct = totalPL.divide(totalCost, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        System.out.println("-".repeat(65));
        System.out.printf("Total Portfolio Value: $%.2f%n", totalValue);
        System.out.printf("Total Cost Basis: $%.2f%n", totalCost);
        System.out.printf("Total P/L: $%.2f (%+.2f%%)%n", totalPL, totalPLPct);
    }
}
```

---

### Example 7: Sector Comparison

```java
public class SectorComparison {

    public void compareSectors(MarketDataTool marketData) {
        Map<String, List<String>> sectors = Map.of(
            "Technology", List.of("AAPL", "GOOGL", "MSFT"),
            "Healthcare", List.of("JNJ", "PFE", "UNH"),
            "Finance", List.of("JPM", "BAC", "WFC")
        );

        System.out.println("Sector Performance (30-Day):");
        System.out.println("Sector      | Avg Change | Best Performer");
        System.out.println("-".repeat(50));

        sectors.forEach((sector, symbols) -> {
            List<BigDecimal> changes = symbols.stream()
                .map(symbol -> {
                    OHLCResult result = marketData.getOHLCBars(symbol, TimeFrame.D1, 30).block();
                    if (!result.isSuccess() || result.getBars().isEmpty()) {
                        return BigDecimal.ZERO;
                    }

                    List<OHLCBar> bars = result.getBars();
                    BigDecimal firstClose = bars.get(bars.size() - 1).getClose();
                    BigDecimal lastClose = bars.get(0).getClose();

                    return lastClose.subtract(firstClose)
                        .divide(firstClose, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                })
                .toList();

            BigDecimal avgChange = changes.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(changes.size()), 2, RoundingMode.HALF_UP);

            BigDecimal maxChange = changes.stream()
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

            int bestIndex = changes.indexOf(maxChange);
            String bestSymbol = symbols.get(bestIndex);

            System.out.printf(
                "%-11s | %+9.2f%% | %s (%+.2f%%)%n",
                sector,
                avgChange,
                bestSymbol,
                maxChange
            );
        });
    }
}
```

---

## Integration Examples

### Example 8: Spring Boot REST API

```java
@RestController
@RequestMapping("/api/market-data")
public class MarketDataController {

    @Inject
    private MarketDataTool marketData;

    @GetMapping("/quote/{symbol}")
    public ResponseEntity<QuoteResponse> getQuote(@PathVariable String symbol) {
        try {
            QuoteData quote = marketData.getQuote(symbol).block();

            QuoteResponse response = new QuoteResponse(
                symbol,
                quote.getLastPrice(),
                quote.getPriceChange(),
                quote.getPercentageChange(),
                quote.getVolume()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ohlc/{symbol}")
    public ResponseEntity<OHLCResponse> getOHLC(
        @PathVariable String symbol,
        @RequestParam(defaultValue = "D1") String timeFrame,
        @RequestParam(defaultValue = "30") int limit
    ) {
        try {
            TimeFrame tf = TimeFrame.valueOf(timeFrame);
            OHLCResult result = marketData.getOHLCBars(symbol, tf, limit).block();

            if (!result.isSuccess()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            OHLCResponse response = new OHLCResponse(
                result.getSymbol(),
                result.getTimeFrame().toString(),
                result.getBars().stream()
                    .map(bar -> new BarData(
                        bar.getTimestamp(),
                        bar.getOpen(),
                        bar.getHigh(),
                        bar.getLow(),
                        bar.getClose(),
                        bar.getVolume()
                    ))
                    .toList()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    record QuoteResponse(
        String symbol,
        BigDecimal price,
        BigDecimal change,
        BigDecimal changePercent,
        BigDecimal volume
    ) {}

    record OHLCResponse(
        String symbol,
        String timeFrame,
        List<BarData> bars
    ) {}

    record BarData(
        Instant timestamp,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal volume
    ) {}
}
```

---

### Example 9: Scheduled Data Collection

```java
@Component
public class DataCollectionService {

    @Inject
    private MarketDataTool marketData;

    private final List<String> watchlist = List.of("AAPL", "GOOGL", "TSLA", "MSFT");

    @Scheduled(cron = "0 0 16 * * MON-FRI")  // Daily at 4 PM market close
    public void collectDailyData() {
        log.info("Collecting daily market data...");

        watchlist.forEach(symbol -> {
            try {
                OHLCResult result = marketData.getOHLCBars(symbol, TimeFrame.D1, 1).block();

                if (result.isSuccess() && !result.getBars().isEmpty()) {
                    OHLCBar bar = result.getBars().get(0);
                    saveToDatabase(bar);
                    log.info("Saved {} daily bar: {}", symbol, bar);
                }

            } catch (Exception e) {
                log.error("Failed to collect data for {}: {}", symbol, e.getMessage());
            }
        });

        log.info("Daily data collection complete");
    }

    private void saveToDatabase(OHLCBar bar) {
        // Save to database implementation
    }
}
```

---

*Last Updated: 2025-10-25*
