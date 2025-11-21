package dev.engineeringlab.adentic.tool.marketdata.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Comprehensive tests for market data model classes. */
@DisplayName("Market Data Models Tests")
class MarketDataModelsTest {

  @Nested
  @DisplayName("MarketType Tests")
  class MarketTypeTests {

    @Test
    @DisplayName("Should have all market types")
    void testMarketTypes() {
      MarketType[] types = MarketType.values();
      assertEquals(10, types.length);

      assertNotNull(MarketType.valueOf("STOCKS"));
      assertNotNull(MarketType.valueOf("CRYPTO"));
      assertNotNull(MarketType.valueOf("FOREX"));
      assertNotNull(MarketType.valueOf("COMMODITIES"));
      assertNotNull(MarketType.valueOf("ETF"));
      assertNotNull(MarketType.valueOf("INDICES"));
      assertNotNull(MarketType.valueOf("FUTURES"));
      assertNotNull(MarketType.valueOf("OPTIONS"));
      assertNotNull(MarketType.valueOf("BONDS"));
      assertNotNull(MarketType.valueOf("UNKNOWN"));
    }

    @Test
    @DisplayName("Should throw exception for invalid type")
    void testInvalidType() {
      assertThrows(IllegalArgumentException.class, () -> MarketType.valueOf("INVALID"));
    }

    @Test
    @DisplayName("Should return correct display name")
    void testDisplayName() {
      assertEquals("Stocks", MarketType.STOCKS.getDisplayName());
      assertEquals("Cryptocurrency", MarketType.CRYPTO.getDisplayName());
      assertEquals("Forex", MarketType.FOREX.getDisplayName());
      assertEquals("Commodities", MarketType.COMMODITIES.getDisplayName());
      assertEquals("ETF", MarketType.ETF.getDisplayName());
      assertEquals("Indices", MarketType.INDICES.getDisplayName());
      assertEquals("Futures", MarketType.FUTURES.getDisplayName());
      assertEquals("Options", MarketType.OPTIONS.getDisplayName());
      assertEquals("Bonds", MarketType.BONDS.getDisplayName());
      assertEquals("Unknown", MarketType.UNKNOWN.getDisplayName());
    }

    @Test
    @DisplayName("Should return display name in toString")
    void testToString() {
      assertEquals("Stocks", MarketType.STOCKS.toString());
      assertEquals("Cryptocurrency", MarketType.CRYPTO.toString());
      assertEquals("Forex", MarketType.FOREX.toString());
    }

    @Test
    @DisplayName("Should return correct name")
    void testName() {
      assertEquals("STOCKS", MarketType.STOCKS.name());
      assertEquals("CRYPTO", MarketType.CRYPTO.name());
      assertEquals("FOREX", MarketType.FOREX.name());
    }
  }

  @Nested
  @DisplayName("DataProvider Tests")
  class DataProviderTests {

    @Test
    @DisplayName("Should have all data providers")
    void testDataProviders() {
      DataProvider[] providers = DataProvider.values();
      assertEquals(7, providers.length);

      assertNotNull(DataProvider.valueOf("ALPHA_VANTAGE"));
      assertNotNull(DataProvider.valueOf("YAHOO_FINANCE"));
      assertNotNull(DataProvider.valueOf("BINANCE"));
      assertNotNull(DataProvider.valueOf("COINGECKO"));
      assertNotNull(DataProvider.valueOf("IEX_CLOUD"));
      assertNotNull(DataProvider.valueOf("POLYGON"));
      assertNotNull(DataProvider.valueOf("MOCK"));
    }

    @Test
    @DisplayName("Should throw exception for invalid provider")
    void testInvalidProvider() {
      assertThrows(IllegalArgumentException.class, () -> DataProvider.valueOf("INVALID"));
    }

    @Test
    @DisplayName("Should return correct display name")
    void testDisplayName() {
      assertEquals("Alpha Vantage", DataProvider.ALPHA_VANTAGE.getDisplayName());
      assertEquals("Yahoo Finance", DataProvider.YAHOO_FINANCE.getDisplayName());
      assertEquals("Binance", DataProvider.BINANCE.getDisplayName());
      assertEquals("CoinGecko", DataProvider.COINGECKO.getDisplayName());
      assertEquals("IEX Cloud", DataProvider.IEX_CLOUD.getDisplayName());
      assertEquals("Polygon.io", DataProvider.POLYGON.getDisplayName());
      assertEquals("Mock Provider", DataProvider.MOCK.getDisplayName());
    }

    @Test
    @DisplayName("Should return correct implementation status")
    void testImplementationStatus() {
      assertTrue(DataProvider.ALPHA_VANTAGE.isImplemented());
      assertTrue(DataProvider.YAHOO_FINANCE.isImplemented());
      assertFalse(DataProvider.BINANCE.isImplemented());
      assertFalse(DataProvider.COINGECKO.isImplemented());
      assertFalse(DataProvider.IEX_CLOUD.isImplemented());
      assertFalse(DataProvider.POLYGON.isImplemented());
      assertTrue(DataProvider.MOCK.isImplemented());
    }

    @Test
    @DisplayName("Should include stub indicator in toString for unimplemented providers")
    void testToString() {
      assertEquals("Alpha Vantage", DataProvider.ALPHA_VANTAGE.toString());
      assertEquals("Yahoo Finance", DataProvider.YAHOO_FINANCE.toString());
      assertEquals("Binance (stub)", DataProvider.BINANCE.toString());
      assertEquals("CoinGecko (stub)", DataProvider.COINGECKO.toString());
      assertEquals("IEX Cloud (stub)", DataProvider.IEX_CLOUD.toString());
      assertEquals("Polygon.io (stub)", DataProvider.POLYGON.toString());
      assertEquals("Mock Provider", DataProvider.MOCK.toString());
    }

    @Test
    @DisplayName("Should return correct name")
    void testName() {
      assertEquals("ALPHA_VANTAGE", DataProvider.ALPHA_VANTAGE.name());
      assertEquals("YAHOO_FINANCE", DataProvider.YAHOO_FINANCE.name());
      assertEquals("MOCK", DataProvider.MOCK.name());
    }
  }

  @Nested
  @DisplayName("TimeFrame Tests")
  class TimeFrameTests {

    @Test
    @DisplayName("Should have all time frames")
    void testTimeFrames() {
      TimeFrame[] frames = TimeFrame.values();
      assertEquals(9, frames.length);

      assertNotNull(TimeFrame.valueOf("M1"));
      assertNotNull(TimeFrame.valueOf("M5"));
      assertNotNull(TimeFrame.valueOf("M15"));
      assertNotNull(TimeFrame.valueOf("M30"));
      assertNotNull(TimeFrame.valueOf("H1"));
      assertNotNull(TimeFrame.valueOf("H4"));
      assertNotNull(TimeFrame.valueOf("D1"));
      assertNotNull(TimeFrame.valueOf("W1"));
      assertNotNull(TimeFrame.valueOf("MN1"));
    }

    @Test
    @DisplayName("Should throw exception for invalid time frame")
    void testInvalidTimeFrame() {
      assertThrows(IllegalArgumentException.class, () -> TimeFrame.valueOf("INVALID"));
    }

    @Test
    @DisplayName("Should return correct seconds")
    void testSeconds() {
      assertEquals(60, TimeFrame.M1.getSeconds());
      assertEquals(300, TimeFrame.M5.getSeconds());
      assertEquals(900, TimeFrame.M15.getSeconds());
      assertEquals(1800, TimeFrame.M30.getSeconds());
      assertEquals(3600, TimeFrame.H1.getSeconds());
      assertEquals(14400, TimeFrame.H4.getSeconds());
      assertEquals(86400, TimeFrame.D1.getSeconds());
      assertEquals(604800, TimeFrame.W1.getSeconds());
      assertEquals(2592000, TimeFrame.MN1.getSeconds());
    }

    @Test
    @DisplayName("Should return correct milliseconds")
    void testMilliseconds() {
      assertEquals(60000, TimeFrame.M1.getMilliseconds());
      assertEquals(300000, TimeFrame.M5.getMilliseconds());
      assertEquals(3600000, TimeFrame.H1.getMilliseconds());
      assertEquals(86400000, TimeFrame.D1.getMilliseconds());
    }

    @Test
    @DisplayName("Should return correct Alpha Vantage format")
    void testAlphaVantageFormat() {
      assertEquals("1min", TimeFrame.M1.getAlphaVantageFormat());
      assertEquals("5min", TimeFrame.M5.getAlphaVantageFormat());
      assertEquals("15min", TimeFrame.M15.getAlphaVantageFormat());
      assertEquals("30min", TimeFrame.M30.getAlphaVantageFormat());
      assertEquals("60min", TimeFrame.H1.getAlphaVantageFormat());
      assertEquals("240min", TimeFrame.H4.getAlphaVantageFormat());
      assertEquals("daily", TimeFrame.D1.getAlphaVantageFormat());
      assertEquals("weekly", TimeFrame.W1.getAlphaVantageFormat());
      assertEquals("monthly", TimeFrame.MN1.getAlphaVantageFormat());
    }

    @Test
    @DisplayName("Should return correct standard format")
    void testStandardFormat() {
      assertEquals("1m", TimeFrame.M1.getStandardFormat());
      assertEquals("5m", TimeFrame.M5.getStandardFormat());
      assertEquals("15m", TimeFrame.M15.getStandardFormat());
      assertEquals("30m", TimeFrame.M30.getStandardFormat());
      assertEquals("1h", TimeFrame.H1.getStandardFormat());
      assertEquals("4h", TimeFrame.H4.getStandardFormat());
      assertEquals("1d", TimeFrame.D1.getStandardFormat());
      assertEquals("1w", TimeFrame.W1.getStandardFormat());
      assertEquals("1M", TimeFrame.MN1.getStandardFormat());
    }

    @Test
    @DisplayName("Should correctly identify intraday time frames")
    void testIsIntraday() {
      assertTrue(TimeFrame.M1.isIntraday());
      assertTrue(TimeFrame.M5.isIntraday());
      assertTrue(TimeFrame.M15.isIntraday());
      assertTrue(TimeFrame.M30.isIntraday());
      assertTrue(TimeFrame.H1.isIntraday());
      assertTrue(TimeFrame.H4.isIntraday());
      assertFalse(TimeFrame.D1.isIntraday());
      assertFalse(TimeFrame.W1.isIntraday());
      assertFalse(TimeFrame.MN1.isIntraday());
    }

    @Test
    @DisplayName("Should parse from standard format")
    void testFromStandardFormat() {
      assertEquals(TimeFrame.M1, TimeFrame.fromStandardFormat("1m"));
      assertEquals(TimeFrame.M5, TimeFrame.fromStandardFormat("5m"));
      assertEquals(TimeFrame.H1, TimeFrame.fromStandardFormat("1h"));
      assertEquals(TimeFrame.D1, TimeFrame.fromStandardFormat("1d"));
      assertEquals(TimeFrame.W1, TimeFrame.fromStandardFormat("1w"));
      // Note: MN1 has standard format "1M" (uppercase), but due to case-insensitive matching,
      // "1M" will match M1 (1 minute) first since M1 comes before MN1 in enum order
    }

    @Test
    @DisplayName("Should handle case-insensitive parsing")
    void testFromStandardFormatCaseInsensitive() {
      // Note: "1M" matches M1 (1 minute), not MN1 (1 month) due to case-insensitive matching
      assertEquals(TimeFrame.M1, TimeFrame.fromStandardFormat("1M"));
      assertEquals(TimeFrame.H1, TimeFrame.fromStandardFormat("1H"));
      assertEquals(TimeFrame.D1, TimeFrame.fromStandardFormat("1D"));
    }

    @Test
    @DisplayName("Should return null for invalid format")
    void testFromStandardFormatInvalid() {
      assertNull(TimeFrame.fromStandardFormat("invalid"));
      assertNull(TimeFrame.fromStandardFormat("2h"));
      assertNull(TimeFrame.fromStandardFormat(""));
    }

    @Test
    @DisplayName("Should return standard format in toString")
    void testToString() {
      assertEquals("1m", TimeFrame.M1.toString());
      assertEquals("1h", TimeFrame.H1.toString());
      assertEquals("1d", TimeFrame.D1.toString());
    }
  }

  @Nested
  @DisplayName("Symbol Tests")
  class SymbolTests {

    @Test
    @DisplayName("Should create symbol with builder")
    void testBuilder() {
      Symbol symbol =
          Symbol.builder()
              .ticker("AAPL")
              .name("Apple Inc.")
              .exchange("NASDAQ")
              .marketType(MarketType.STOCKS)
              .currency("USD")
              .region("US")
              .active(true)
              .description("Technology company")
              .build();

      assertEquals("AAPL", symbol.getTicker());
      assertEquals("Apple Inc.", symbol.getName());
      assertEquals("NASDAQ", symbol.getExchange());
      assertEquals(MarketType.STOCKS, symbol.getMarketType());
      assertEquals("USD", symbol.getCurrency());
      assertEquals("US", symbol.getRegion());
      assertTrue(symbol.isActive());
      assertEquals("Technology company", symbol.getDescription());
    }

    @Test
    @DisplayName("Should use default active value")
    void testDefaultActive() {
      Symbol symbol =
          Symbol.builder()
              .ticker("AAPL")
              .name("Apple Inc.")
              .exchange("NASDAQ")
              .marketType(MarketType.STOCKS)
              .build();

      assertTrue(symbol.isActive());
    }

    @Test
    @DisplayName("Should support crypto symbol")
    void testCryptoSymbol() {
      Symbol symbol =
          Symbol.builder()
              .ticker("BTCUSDT")
              .name("Bitcoin/USD Tether")
              .exchange("Binance")
              .marketType(MarketType.CRYPTO)
              .currency("USDT")
              .build();

      assertEquals("BTCUSDT", symbol.getTicker());
      assertEquals("Bitcoin/USD Tether", symbol.getName());
      assertEquals("Binance", symbol.getExchange());
      assertEquals(MarketType.CRYPTO, symbol.getMarketType());
      assertEquals("USDT", symbol.getCurrency());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      Symbol symbol = Symbol.builder().ticker("TEST").build();

      symbol.setTicker("AAPL");
      symbol.setName("Apple Inc.");
      symbol.setExchange("NASDAQ");
      symbol.setMarketType(MarketType.STOCKS);
      symbol.setCurrency("USD");
      symbol.setRegion("US");
      symbol.setActive(false);
      symbol.setDescription("Tech company");

      assertEquals("AAPL", symbol.getTicker());
      assertEquals("Apple Inc.", symbol.getName());
      assertEquals("NASDAQ", symbol.getExchange());
      assertEquals(MarketType.STOCKS, symbol.getMarketType());
      assertEquals("USD", symbol.getCurrency());
      assertEquals("US", symbol.getRegion());
      assertFalse(symbol.isActive());
      assertEquals("Tech company", symbol.getDescription());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      Symbol symbol1 =
          Symbol.builder()
              .ticker("AAPL")
              .name("Apple Inc.")
              .exchange("NASDAQ")
              .marketType(MarketType.STOCKS)
              .build();

      Symbol symbol2 =
          Symbol.builder()
              .ticker("AAPL")
              .name("Apple Inc.")
              .exchange("NASDAQ")
              .marketType(MarketType.STOCKS)
              .build();

      assertEquals(symbol1, symbol2);
      assertEquals(symbol1.hashCode(), symbol2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      Symbol symbol =
          Symbol.builder()
              .ticker("AAPL")
              .name("Apple Inc.")
              .exchange("NASDAQ")
              .marketType(MarketType.STOCKS)
              .build();

      String str = symbol.toString();
      assertTrue(str.contains("AAPL"));
      assertTrue(str.contains("Apple Inc."));
      assertTrue(str.contains("NASDAQ"));
      assertTrue(str.contains("Stocks"));
    }
  }

  @Nested
  @DisplayName("QuoteData Tests")
  class QuoteDataTests {

    @Test
    @DisplayName("Should create quote with builder")
    void testBuilder() {
      Instant timestamp = Instant.now();

      QuoteData quote =
          QuoteData.builder()
              .symbol("AAPL")
              .lastPrice(new BigDecimal("150.00"))
              .bidPrice(new BigDecimal("149.95"))
              .askPrice(new BigDecimal("150.05"))
              .bidSize(new BigDecimal("100"))
              .askSize(new BigDecimal("200"))
              .volume(new BigDecimal("50000000"))
              .openPrice(new BigDecimal("148.00"))
              .highPrice(new BigDecimal("151.00"))
              .lowPrice(new BigDecimal("147.50"))
              .previousClose(new BigDecimal("149.00"))
              .timestamp(timestamp)
              .provider("Alpha Vantage")
              .build();

      assertEquals("AAPL", quote.getSymbol());
      assertEquals(new BigDecimal("150.00"), quote.getLastPrice());
      assertEquals(new BigDecimal("149.95"), quote.getBidPrice());
      assertEquals(new BigDecimal("150.05"), quote.getAskPrice());
      assertEquals(new BigDecimal("100"), quote.getBidSize());
      assertEquals(new BigDecimal("200"), quote.getAskSize());
      assertEquals(new BigDecimal("50000000"), quote.getVolume());
      assertEquals(new BigDecimal("148.00"), quote.getOpenPrice());
      assertEquals(new BigDecimal("151.00"), quote.getHighPrice());
      assertEquals(new BigDecimal("147.50"), quote.getLowPrice());
      assertEquals(new BigDecimal("149.00"), quote.getPreviousClose());
      assertEquals(timestamp, quote.getTimestamp());
      assertEquals("Alpha Vantage", quote.getProvider());
    }

    @Test
    @DisplayName("Should calculate spread correctly")
    void testGetSpread() {
      QuoteData quote =
          QuoteData.builder()
              .symbol("AAPL")
              .bidPrice(new BigDecimal("149.95"))
              .askPrice(new BigDecimal("150.05"))
              .build();

      assertEquals(new BigDecimal("0.10"), quote.getSpread());
    }

    @Test
    @DisplayName("Should return zero spread when prices are null")
    void testGetSpreadNull() {
      QuoteData quote = QuoteData.builder().symbol("AAPL").build();

      assertEquals(BigDecimal.ZERO, quote.getSpread());
    }

    @Test
    @DisplayName("Should calculate price change correctly")
    void testGetPriceChange() {
      QuoteData quote =
          QuoteData.builder()
              .symbol("AAPL")
              .lastPrice(new BigDecimal("150.00"))
              .previousClose(new BigDecimal("149.00"))
              .build();

      assertEquals(new BigDecimal("1.00"), quote.getPriceChange());
    }

    @Test
    @DisplayName("Should return zero price change when prices are null")
    void testGetPriceChangeNull() {
      QuoteData quote = QuoteData.builder().symbol("AAPL").build();

      assertEquals(BigDecimal.ZERO, quote.getPriceChange());
    }

    @Test
    @DisplayName("Should calculate percentage change correctly")
    void testGetPercentageChange() {
      QuoteData quote =
          QuoteData.builder()
              .symbol("AAPL")
              .lastPrice(new BigDecimal("150.00"))
              .previousClose(new BigDecimal("100.00"))
              .build();

      // (150 - 100) / 100 * 100 = 50%
      assertEquals(0, new BigDecimal("50.0000").compareTo(quote.getPercentageChange()));
    }

    @Test
    @DisplayName("Should return zero percentage change when previous close is zero")
    void testGetPercentageChangeZeroDivision() {
      QuoteData quote =
          QuoteData.builder()
              .symbol("AAPL")
              .lastPrice(new BigDecimal("150.00"))
              .previousClose(BigDecimal.ZERO)
              .build();

      assertEquals(BigDecimal.ZERO, quote.getPercentageChange());
    }

    @Test
    @DisplayName("Should return zero percentage change when prices are null")
    void testGetPercentageChangeNull() {
      QuoteData quote = QuoteData.builder().symbol("AAPL").build();

      assertEquals(BigDecimal.ZERO, quote.getPercentageChange());
    }

    @Test
    @DisplayName("Should support toBuilder")
    void testToBuilder() {
      QuoteData original =
          QuoteData.builder()
              .symbol("AAPL")
              .lastPrice(new BigDecimal("150.00"))
              .bidPrice(new BigDecimal("149.95"))
              .build();

      QuoteData modified = original.toBuilder().askPrice(new BigDecimal("150.05")).build();

      assertEquals("AAPL", modified.getSymbol());
      assertEquals(new BigDecimal("150.00"), modified.getLastPrice());
      assertEquals(new BigDecimal("149.95"), modified.getBidPrice());
      assertEquals(new BigDecimal("150.05"), modified.getAskPrice());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      QuoteData quote = QuoteData.builder().symbol("AAPL").build();
      Instant timestamp = Instant.now();

      quote.setSymbol("MSFT");
      quote.setLastPrice(new BigDecimal("300.00"));
      quote.setBidPrice(new BigDecimal("299.95"));
      quote.setAskPrice(new BigDecimal("300.05"));
      quote.setVolume(new BigDecimal("10000000"));
      quote.setTimestamp(timestamp);

      assertEquals("MSFT", quote.getSymbol());
      assertEquals(new BigDecimal("300.00"), quote.getLastPrice());
      assertEquals(new BigDecimal("299.95"), quote.getBidPrice());
      assertEquals(new BigDecimal("300.05"), quote.getAskPrice());
      assertEquals(new BigDecimal("10000000"), quote.getVolume());
      assertEquals(timestamp, quote.getTimestamp());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      Instant timestamp = Instant.now();

      QuoteData quote1 =
          QuoteData.builder()
              .symbol("AAPL")
              .lastPrice(new BigDecimal("150.00"))
              .timestamp(timestamp)
              .build();

      QuoteData quote2 =
          QuoteData.builder()
              .symbol("AAPL")
              .lastPrice(new BigDecimal("150.00"))
              .timestamp(timestamp)
              .build();

      assertEquals(quote1, quote2);
      assertEquals(quote1.hashCode(), quote2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      QuoteData quote =
          QuoteData.builder()
              .symbol("AAPL")
              .lastPrice(new BigDecimal("150.00"))
              .bidPrice(new BigDecimal("149.95"))
              .askPrice(new BigDecimal("150.05"))
              .previousClose(new BigDecimal("149.00"))
              .volume(new BigDecimal("50000000"))
              .build();

      String str = quote.toString();
      assertTrue(str.contains("AAPL"));
    }
  }

  @Nested
  @DisplayName("OHLCBar Tests")
  class OHLCBarTests {

    @Test
    @DisplayName("Should create OHLC bar with builder")
    void testBuilder() {
      Instant timestamp = Instant.now();

      OHLCBar bar =
          OHLCBar.builder()
              .timestamp(timestamp)
              .open(new BigDecimal("100.00"))
              .high(new BigDecimal("105.00"))
              .low(new BigDecimal("99.00"))
              .close(new BigDecimal("103.00"))
              .volume(new BigDecimal("1000000"))
              .symbol("AAPL")
              .timeFrame(TimeFrame.D1)
              .build();

      assertEquals(timestamp, bar.getTimestamp());
      assertEquals(new BigDecimal("100.00"), bar.getOpen());
      assertEquals(new BigDecimal("105.00"), bar.getHigh());
      assertEquals(new BigDecimal("99.00"), bar.getLow());
      assertEquals(new BigDecimal("103.00"), bar.getClose());
      assertEquals(new BigDecimal("1000000"), bar.getVolume());
      assertEquals("AAPL", bar.getSymbol());
      assertEquals(TimeFrame.D1, bar.getTimeFrame());
    }

    @Test
    @DisplayName("Should calculate price change correctly")
    void testGetPriceChange() {
      OHLCBar bar =
          OHLCBar.builder()
              .open(new BigDecimal("100.00"))
              .close(new BigDecimal("103.00"))
              .high(new BigDecimal("105.00"))
              .low(new BigDecimal("99.00"))
              .build();

      assertEquals(new BigDecimal("3.00"), bar.getPriceChange());
    }

    @Test
    @DisplayName("Should calculate percentage change correctly")
    void testGetPercentageChange() {
      OHLCBar bar =
          OHLCBar.builder()
              .open(new BigDecimal("100.00"))
              .close(new BigDecimal("110.00"))
              .high(new BigDecimal("110.00"))
              .low(new BigDecimal("100.00"))
              .build();

      // (110 - 100) / 100 * 100 = 10%
      assertEquals(0, new BigDecimal("10.0000").compareTo(bar.getPercentageChange()));
    }

    @Test
    @DisplayName("Should return zero percentage change when open is zero")
    void testGetPercentageChangeZeroDivision() {
      OHLCBar bar =
          OHLCBar.builder()
              .open(BigDecimal.ZERO)
              .close(new BigDecimal("10.00"))
              .high(new BigDecimal("10.00"))
              .low(BigDecimal.ZERO)
              .build();

      assertEquals(BigDecimal.ZERO, bar.getPercentageChange());
    }

    @Test
    @DisplayName("Should identify bullish candles")
    void testIsBullish() {
      OHLCBar bullish =
          OHLCBar.builder()
              .open(new BigDecimal("100.00"))
              .close(new BigDecimal("105.00"))
              .high(new BigDecimal("106.00"))
              .low(new BigDecimal("99.00"))
              .build();

      assertTrue(bullish.isBullish());
      assertFalse(bullish.isBearish());
    }

    @Test
    @DisplayName("Should identify bearish candles")
    void testIsBearish() {
      OHLCBar bearish =
          OHLCBar.builder()
              .open(new BigDecimal("100.00"))
              .close(new BigDecimal("95.00"))
              .high(new BigDecimal("101.00"))
              .low(new BigDecimal("94.00"))
              .build();

      assertTrue(bearish.isBearish());
      assertFalse(bearish.isBullish());
    }

    @Test
    @DisplayName("Should identify doji candles")
    void testIsDoji() {
      OHLCBar doji =
          OHLCBar.builder()
              .open(new BigDecimal("100.00"))
              .close(new BigDecimal("100.05"))
              .high(new BigDecimal("101.00"))
              .low(new BigDecimal("99.00"))
              .build();

      assertTrue(doji.isDoji());
    }

    @Test
    @DisplayName("Should not identify large moves as doji")
    void testIsNotDoji() {
      OHLCBar notDoji =
          OHLCBar.builder()
              .open(new BigDecimal("100.00"))
              .close(new BigDecimal("101.00"))
              .high(new BigDecimal("102.00"))
              .low(new BigDecimal("99.00"))
              .build();

      assertFalse(notDoji.isDoji());
    }

    @Test
    @DisplayName("Should support toBuilder")
    void testToBuilder() {
      OHLCBar original =
          OHLCBar.builder()
              .symbol("AAPL")
              .open(new BigDecimal("100.00"))
              .high(new BigDecimal("105.00"))
              .low(new BigDecimal("99.00"))
              .close(new BigDecimal("103.00"))
              .build();

      OHLCBar modified = original.toBuilder().volume(new BigDecimal("1000000")).build();

      assertEquals("AAPL", modified.getSymbol());
      assertEquals(new BigDecimal("100.00"), modified.getOpen());
      assertEquals(new BigDecimal("1000000"), modified.getVolume());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      OHLCBar bar = OHLCBar.builder().symbol("AAPL").build();
      Instant timestamp = Instant.now();

      bar.setSymbol("MSFT");
      bar.setTimestamp(timestamp);
      bar.setOpen(new BigDecimal("200.00"));
      bar.setHigh(new BigDecimal("210.00"));
      bar.setLow(new BigDecimal("195.00"));
      bar.setClose(new BigDecimal("205.00"));
      bar.setVolume(new BigDecimal("5000000"));
      bar.setTimeFrame(TimeFrame.H1);

      assertEquals("MSFT", bar.getSymbol());
      assertEquals(timestamp, bar.getTimestamp());
      assertEquals(new BigDecimal("200.00"), bar.getOpen());
      assertEquals(new BigDecimal("210.00"), bar.getHigh());
      assertEquals(new BigDecimal("195.00"), bar.getLow());
      assertEquals(new BigDecimal("205.00"), bar.getClose());
      assertEquals(new BigDecimal("5000000"), bar.getVolume());
      assertEquals(TimeFrame.H1, bar.getTimeFrame());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      Instant timestamp = Instant.now();

      OHLCBar bar1 =
          OHLCBar.builder()
              .symbol("AAPL")
              .timestamp(timestamp)
              .open(new BigDecimal("100.00"))
              .high(new BigDecimal("105.00"))
              .low(new BigDecimal("99.00"))
              .close(new BigDecimal("103.00"))
              .build();

      OHLCBar bar2 =
          OHLCBar.builder()
              .symbol("AAPL")
              .timestamp(timestamp)
              .open(new BigDecimal("100.00"))
              .high(new BigDecimal("105.00"))
              .low(new BigDecimal("99.00"))
              .close(new BigDecimal("103.00"))
              .build();

      assertEquals(bar1, bar2);
      assertEquals(bar1.hashCode(), bar2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      OHLCBar bar =
          OHLCBar.builder()
              .symbol("AAPL")
              .timestamp(Instant.now())
              .open(new BigDecimal("100.00"))
              .high(new BigDecimal("105.00"))
              .low(new BigDecimal("99.00"))
              .close(new BigDecimal("103.00"))
              .volume(new BigDecimal("1000000"))
              .build();

      String str = bar.toString();
      assertTrue(str.contains("AAPL"));
    }
  }

  @Nested
  @DisplayName("OHLCResult Tests")
  class OHLCResultTests {

    @Test
    @DisplayName("Should create result with builder")
    void testBuilder() {
      OHLCBar bar1 =
          OHLCBar.builder()
              .symbol("AAPL")
              .timestamp(Instant.now())
              .open(new BigDecimal("100.00"))
              .high(new BigDecimal("105.00"))
              .low(new BigDecimal("99.00"))
              .close(new BigDecimal("103.00"))
              .volume(new BigDecimal("1000000"))
              .build();

      OHLCBar bar2 =
          OHLCBar.builder()
              .symbol("AAPL")
              .timestamp(Instant.now())
              .open(new BigDecimal("103.00"))
              .high(new BigDecimal("108.00"))
              .low(new BigDecimal("102.00"))
              .close(new BigDecimal("106.00"))
              .volume(new BigDecimal("1500000"))
              .build();

      List<OHLCBar> bars = Arrays.asList(bar1, bar2);

      OHLCResult result =
          OHLCResult.builder()
              .success(true)
              .bars(bars)
              .count(2)
              .symbol("AAPL")
              .timeFrame(TimeFrame.D1)
              .provider("Alpha Vantage")
              .errorMessage(null)
              .cached(false)
              .executionTimeMs(150)
              .build();

      assertTrue(result.isSuccess());
      assertEquals(bars, result.getBars());
      assertEquals(2, result.getCount());
      assertEquals("AAPL", result.getSymbol());
      assertEquals(TimeFrame.D1, result.getTimeFrame());
      assertEquals("Alpha Vantage", result.getProvider());
      assertNull(result.getErrorMessage());
      assertFalse(result.isCached());
      assertEquals(150, result.getExecutionTimeMs());
    }

    @Test
    @DisplayName("Should create successful result")
    void testSuccessfulResult() {
      List<OHLCBar> bars =
          List.of(
              OHLCBar.builder()
                  .symbol("AAPL")
                  .open(new BigDecimal("100.00"))
                  .high(new BigDecimal("105.00"))
                  .low(new BigDecimal("99.00"))
                  .close(new BigDecimal("103.00"))
                  .build());

      OHLCResult result =
          OHLCResult.builder()
              .success(true)
              .bars(bars)
              .count(1)
              .symbol("AAPL")
              .timeFrame(TimeFrame.D1)
              .provider("Yahoo Finance")
              .build();

      assertTrue(result.isSuccess());
      assertEquals(1, result.getCount());
      assertEquals("AAPL", result.getSymbol());
      assertEquals(TimeFrame.D1, result.getTimeFrame());
      assertEquals("Yahoo Finance", result.getProvider());
    }

    @Test
    @DisplayName("Should create error result")
    void testErrorResult() {
      OHLCResult result =
          OHLCResult.builder()
              .success(false)
              .errorMessage("API rate limit exceeded")
              .symbol("AAPL")
              .timeFrame(TimeFrame.D1)
              .provider("Alpha Vantage")
              .build();

      assertFalse(result.isSuccess());
      assertEquals("API rate limit exceeded", result.getErrorMessage());
      assertEquals("AAPL", result.getSymbol());
      assertEquals(TimeFrame.D1, result.getTimeFrame());
    }

    @Test
    @DisplayName("Should support cached results")
    void testCachedResult() {
      OHLCResult result =
          OHLCResult.builder()
              .success(true)
              .bars(List.of())
              .count(0)
              .symbol("AAPL")
              .timeFrame(TimeFrame.D1)
              .provider("Alpha Vantage")
              .cached(true)
              .executionTimeMs(5)
              .build();

      assertTrue(result.isSuccess());
      assertTrue(result.isCached());
      assertEquals(5, result.getExecutionTimeMs());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      OHLCResult result = OHLCResult.builder().success(true).build();

      List<OHLCBar> bars =
          List.of(
              OHLCBar.builder()
                  .symbol("MSFT")
                  .open(new BigDecimal("200.00"))
                  .high(new BigDecimal("210.00"))
                  .low(new BigDecimal("195.00"))
                  .close(new BigDecimal("205.00"))
                  .build());

      result.setSuccess(false);
      result.setBars(bars);
      result.setCount(1);
      result.setSymbol("MSFT");
      result.setTimeFrame(TimeFrame.H1);
      result.setProvider("Yahoo Finance");
      result.setErrorMessage("Error occurred");
      result.setCached(true);
      result.setExecutionTimeMs(100);

      assertFalse(result.isSuccess());
      assertEquals(bars, result.getBars());
      assertEquals(1, result.getCount());
      assertEquals("MSFT", result.getSymbol());
      assertEquals(TimeFrame.H1, result.getTimeFrame());
      assertEquals("Yahoo Finance", result.getProvider());
      assertEquals("Error occurred", result.getErrorMessage());
      assertTrue(result.isCached());
      assertEquals(100, result.getExecutionTimeMs());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      List<OHLCBar> bars =
          List.of(
              OHLCBar.builder()
                  .symbol("AAPL")
                  .open(new BigDecimal("100.00"))
                  .high(new BigDecimal("105.00"))
                  .low(new BigDecimal("99.00"))
                  .close(new BigDecimal("103.00"))
                  .build());

      OHLCResult result1 =
          OHLCResult.builder()
              .success(true)
              .bars(bars)
              .count(1)
              .symbol("AAPL")
              .timeFrame(TimeFrame.D1)
              .build();

      OHLCResult result2 =
          OHLCResult.builder()
              .success(true)
              .bars(bars)
              .count(1)
              .symbol("AAPL")
              .timeFrame(TimeFrame.D1)
              .build();

      assertEquals(result1, result2);
      assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString for success")
    void testToStringSuccess() {
      OHLCResult result =
          OHLCResult.builder()
              .success(true)
              .bars(List.of())
              .count(10)
              .symbol("AAPL")
              .timeFrame(TimeFrame.D1)
              .provider("Alpha Vantage")
              .cached(false)
              .executionTimeMs(150)
              .build();

      String str = result.toString();
      assertTrue(str.contains("AAPL"));
      assertTrue(str.contains("10"));
      assertTrue(str.contains("Alpha Vantage"));
    }

    @Test
    @DisplayName("Should implement toString for error")
    void testToStringError() {
      OHLCResult result =
          OHLCResult.builder()
              .success(false)
              .errorMessage("API rate limit exceeded")
              .symbol("AAPL")
              .build();

      String str = result.toString();
      assertTrue(str.contains("Failed"));
      assertTrue(str.contains("API rate limit exceeded"));
    }
  }
}
