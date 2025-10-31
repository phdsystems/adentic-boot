package dev.adeengineer.adentic.tool.marketdata.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.adeengineer.adentic.tool.marketdata.config.MarketDataConfig;
import dev.adeengineer.adentic.tool.marketdata.model.*;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for YahooFinanceProvider covering:
 *
 * <ul>
 *   <li>Constructor and initialization
 *   <li>Configuration handling (no API key required)
 *   <li>Connection lifecycle
 *   <li>OHLC data retrieval (stub behavior)
 *   <li>Quote data operations
 *   <li>Symbol search
 *   <li>Provider capabilities
 *   <li>Time frame support (limited to daily and higher)
 *   <li>Error handling
 * </ul>
 */
@DisplayName("YahooFinanceProvider Tests")
class YahooFinanceProviderTest {

  private MarketDataConfig config;
  private YahooFinanceProvider provider;

  @BeforeEach
  void setUp() {
    config = MarketDataConfig.yahooFinance();
    provider = new YahooFinanceProvider(config);
  }

  @Nested
  @DisplayName("Constructor and Initialization Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should initialize with valid configuration")
    void testInitialization() {
      assertNotNull(provider);
      assertNotNull(provider.getConfig());
    }

    @Test
    @DisplayName("Should not require API key")
    void testInitializationWithoutApiKey() {
      assertNotNull(provider);
      assertEquals(DataProvider.YAHOO_FINANCE, provider.getConfig().getProvider());
    }
  }

  @Nested
  @DisplayName("Connection Lifecycle Tests")
  class ConnectionLifecycleTests {

    @Test
    @DisplayName("Should connect successfully")
    void testConnect() {
      provider.connect().block();
      assertTrue(provider.isConnected());
    }

    @Test
    @DisplayName("Should disconnect successfully")
    void testDisconnect() {
      provider.connect().block();
      provider.disconnect().block();
      assertFalse(provider.isConnected());
    }
  }

  @Nested
  @DisplayName("OHLC Data Retrieval Tests")
  class OHLCDataTests {

    @Test
    @DisplayName("Should return empty result for OHLC bars with limit")
    void testGetOHLCBarsWithLimit() {
      OHLCResult result = provider.getOHLCBars("AAPL", TimeFrame.D1, 100).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, result.getCount());
    }

    @Test
    @DisplayName("Should validate symbol when getting OHLC bars")
    void testGetOHLCBarsValidatesSymbol() {
      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getOHLCBars("INVALID@SYMBOL", TimeFrame.D1, 10).block());
    }

    @Test
    @DisplayName("Should validate limit when getting OHLC bars")
    void testGetOHLCBarsValidatesLimit() {
      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getOHLCBars("AAPL", TimeFrame.D1, -1).block());

      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getOHLCBars("AAPL", TimeFrame.D1, 2000).block());
    }

    @Test
    @DisplayName("Should return empty result for historical data")
    void testGetHistoricalData() {
      LocalDate startDate = LocalDate.now().minusDays(30);
      LocalDate endDate = LocalDate.now();

      OHLCResult result =
          provider.getHistoricalData("AAPL", TimeFrame.D1, startDate, endDate).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, result.getCount());
    }
  }

  @Nested
  @DisplayName("Provider Capabilities Tests")
  class ProviderCapabilitiesTests {

    @Test
    @DisplayName("Should return correct provider name")
    void testGetProviderName() {
      assertEquals("Yahoo Finance", provider.getProviderName());
    }

    @Test
    @DisplayName("Should support stocks market type")
    void testGetSupportedMarketType() {
      assertEquals(MarketType.STOCKS, provider.getSupportedMarketType());
    }

    @Test
    @DisplayName("Should support daily and higher time frames only")
    void testSupportsTimeFrames() {
      // Supported time frames
      assertTrue(provider.supportsTimeFrame(TimeFrame.D1));
      assertTrue(provider.supportsTimeFrame(TimeFrame.W1));
      assertTrue(provider.supportsTimeFrame(TimeFrame.MN1));

      // Unsupported time frames (intraday)
      assertFalse(provider.supportsTimeFrame(TimeFrame.M1));
      assertFalse(provider.supportsTimeFrame(TimeFrame.M5));
      assertFalse(provider.supportsTimeFrame(TimeFrame.H1));
    }

    @Test
    @DisplayName("Should not support streaming")
    void testSupportsStreaming() {
      assertFalse(provider.supportsStreaming());
    }
  }

  @Nested
  @DisplayName("Configuration Tests")
  class ConfigurationTests {

    @Test
    @DisplayName("Should use higher rate limit than Alpha Vantage")
    void testHigherRateLimit() {
      assertEquals(60, provider.getConfig().getRateLimitPerMinute());
    }

    @Test
    @DisplayName("Should handle custom configuration")
    void testCustomConfiguration() {
      MarketDataConfig customConfig =
          MarketDataConfig.builder()
              .provider(DataProvider.YAHOO_FINANCE)
              .rateLimitPerMinute(120)
              .enableCaching(false)
              .build();

      YahooFinanceProvider customProvider = new YahooFinanceProvider(customConfig);

      assertEquals(120, customProvider.getConfig().getRateLimitPerMinute());
      assertFalse(customProvider.getConfig().isEnableCaching());
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should validate null symbol")
    void testNullSymbol() {
      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getOHLCBars(null, TimeFrame.D1, 10).block());
    }

    @Test
    @DisplayName("Should validate empty symbol")
    void testEmptySymbol() {
      assertThrows(
          IllegalArgumentException.class, () -> provider.getOHLCBars("", TimeFrame.D1, 10).block());
    }

    @Test
    @DisplayName("Should validate symbol length")
    void testSymbolTooLong() {
      String longSymbol = "A".repeat(21);
      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getOHLCBars(longSymbol, TimeFrame.D1, 10).block());
    }
  }

  @Nested
  @DisplayName("TimeFrame Tests")
  class TimeFrameTests {

    @Test
    @DisplayName("Should get standard format for time frames")
    void testStandardFormat() {
      assertEquals("1d", TimeFrame.D1.getStandardFormat());
      assertEquals("1w", TimeFrame.W1.getStandardFormat());
      assertEquals("1M", TimeFrame.MN1.getStandardFormat());
    }

    @Test
    @DisplayName("Should parse time frame from standard format")
    void testParseFromStandardFormat() {
      assertEquals(TimeFrame.D1, TimeFrame.fromStandardFormat("1d"));
      assertEquals(TimeFrame.W1, TimeFrame.fromStandardFormat("1w"));
      assertNull(TimeFrame.fromStandardFormat("invalid"));
    }
  }

  @Nested
  @DisplayName("Quote and Symbol Tests")
  class QuoteAndSymbolTests {

    @Test
    @DisplayName("Should throw UnsupportedOperationException for getQuote")
    void testGetQuote() {
      assertThrows(UnsupportedOperationException.class, () -> provider.getQuote("AAPL").block());
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException for streamQuotes")
    void testStreamQuotes() {
      assertThrows(
          UnsupportedOperationException.class, () -> provider.streamQuotes("AAPL").blockFirst());
    }

    @Test
    @DisplayName("Should return empty list for symbol search")
    void testSearchSymbols() {
      java.util.List<Symbol> symbols = provider.searchSymbols("Apple").block();
      assertNotNull(symbols);
      assertTrue(symbols.isEmpty());
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException for getSymbolInfo")
    void testGetSymbolInfo() {
      assertThrows(
          UnsupportedOperationException.class, () -> provider.getSymbolInfo("AAPL").block());
    }
  }
}
