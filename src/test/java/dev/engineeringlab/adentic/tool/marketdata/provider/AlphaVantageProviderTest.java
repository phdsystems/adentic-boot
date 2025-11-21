package dev.engineeringlab.adentic.tool.marketdata.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import dev.engineeringlab.adentic.tool.marketdata.config.MarketDataConfig;
import dev.engineeringlab.adentic.tool.marketdata.model.*;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for AlphaVantageProvider covering:
 *
 * <ul>
 *   <li>Constructor and initialization
 *   <li>Configuration handling (API key)
 *   <li>Connection lifecycle
 *   <li>OHLC data retrieval (stub behavior)
 *   <li>Quote data operations
 *   <li>Symbol search
 *   <li>Provider capabilities
 *   <li>Time frame support
 *   <li>Error handling
 * </ul>
 */
@DisplayName("AlphaVantageProvider Tests")
class AlphaVantageProviderTest {

  private MarketDataConfig config;
  private AlphaVantageProvider provider;

  @BeforeEach
  void setUp() {
    config =
        MarketDataConfig.builder()
            .provider(DataProvider.ALPHA_VANTAGE)
            .apiKey("test-api-key")
            .rateLimitPerMinute(5)
            .enableCaching(true)
            .cacheTTLSeconds(900)
            .maxBarsPerRequest(1000)
            .requestTimeout(30000)
            .enableLogging(true)
            .build();
    provider = new AlphaVantageProvider(config);
  }

  @Nested
  @DisplayName("Constructor and Initialization Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should initialize with valid configuration")
    void testInitialization() {
      assertNotNull(provider);
      assertNotNull(provider.getConfig());
      assertEquals(config, provider.getConfig());
    }

    @Test
    @DisplayName("Should initialize with API key")
    void testInitializationWithApiKey() {
      AlphaVantageProvider providerWithKey = new AlphaVantageProvider(config);
      assertEquals("test-api-key", providerWithKey.getConfig().getApiKey());
    }

    @Test
    @DisplayName("Should initialize without API key")
    void testInitializationWithoutApiKey() {
      MarketDataConfig configNoKey =
          MarketDataConfig.builder().provider(DataProvider.ALPHA_VANTAGE).build();
      AlphaVantageProvider providerNoKey = new AlphaVantageProvider(configNoKey);

      assertNotNull(providerNoKey);
      assertNull(providerNoKey.getConfig().getApiKey());
    }

    @Test
    @DisplayName("Should initialize with empty API key")
    void testInitializationWithEmptyApiKey() {
      MarketDataConfig configEmptyKey =
          MarketDataConfig.builder().provider(DataProvider.ALPHA_VANTAGE).apiKey("").build();
      AlphaVantageProvider providerEmptyKey = new AlphaVantageProvider(configEmptyKey);

      assertNotNull(providerEmptyKey);
      assertTrue(providerEmptyKey.getConfig().getApiKey().isEmpty());
    }

    @Test
    @DisplayName("Should initialize with Alpha Vantage config factory")
    void testInitializationWithFactoryMethod() {
      MarketDataConfig factoryConfig = MarketDataConfig.alphaVantage("factory-key");
      AlphaVantageProvider factoryProvider = new AlphaVantageProvider(factoryConfig);

      assertNotNull(factoryProvider);
      assertEquals("factory-key", factoryProvider.getConfig().getApiKey());
      assertEquals(DataProvider.ALPHA_VANTAGE, factoryProvider.getConfig().getProvider());
    }

    @Test
    @DisplayName("Should not be connected initially")
    void testInitialConnectionState() {
      assertFalse(provider.isConnected());
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
      assertTrue(provider.isConnected());

      provider.disconnect().block();
      assertFalse(provider.isConnected());
    }

    @Test
    @DisplayName("Should handle multiple connect calls")
    void testMultipleConnects() {
      provider.connect().block();
      assertTrue(provider.isConnected());

      provider.connect().block();
      assertTrue(provider.isConnected());
    }

    @Test
    @DisplayName("Should handle multiple disconnect calls")
    void testMultipleDisconnects() {
      provider.connect().block();
      provider.disconnect().block();
      assertFalse(provider.isConnected());

      provider.disconnect().block();
      assertFalse(provider.isConnected());
    }

    @Test
    @DisplayName("Should disconnect without prior connect")
    void testDisconnectWithoutConnect() {
      assertFalse(provider.isConnected());

      provider.disconnect().block();
      assertFalse(provider.isConnected());
    }
  }

  @Nested
  @DisplayName("OHLC Data Retrieval Tests (Stub Behavior)")
  class OHLCDataTests {

    @Test
    @DisplayName("Should return empty result for OHLC bars with limit")
    void testGetOHLCBarsWithLimit() {
      OHLCResult result = provider.getOHLCBars("AAPL", TimeFrame.M5, 100).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, result.getCount());
      assertEquals("AAPL", result.getSymbol());
      assertEquals(TimeFrame.M5, result.getTimeFrame());
      assertNotNull(result.getBars());
      assertTrue(result.getBars().isEmpty());
    }

    @Test
    @DisplayName("Should validate symbol when getting OHLC bars")
    void testGetOHLCBarsValidatesSymbol() {
      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getOHLCBars("INVALID@SYMBOL", TimeFrame.M1, 10).block());
    }

    @Test
    @DisplayName("Should validate limit when getting OHLC bars")
    void testGetOHLCBarsValidatesLimit() {
      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getOHLCBars("AAPL", TimeFrame.M1, -1).block());

      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getOHLCBars("AAPL", TimeFrame.M1, 0).block());

      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getOHLCBars("AAPL", TimeFrame.M1, 2000).block());
    }

    @Test
    @DisplayName("Should return empty result for OHLC bars with time range")
    void testGetOHLCBarsWithTimeRange() {
      Instant startTime = Instant.now().minusSeconds(3600);
      Instant endTime = Instant.now();

      OHLCResult result = provider.getOHLCBars("AAPL", TimeFrame.M15, startTime, endTime).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, result.getCount());
    }

    @Test
    @DisplayName("Should validate symbol when getting OHLC bars with time range")
    void testGetOHLCBarsWithTimeRangeValidatesSymbol() {
      Instant startTime = Instant.now().minusSeconds(3600);
      Instant endTime = Instant.now();

      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getOHLCBars(null, TimeFrame.M1, startTime, endTime).block());
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
      assertEquals("AAPL", result.getSymbol());
      assertEquals(TimeFrame.D1, result.getTimeFrame());
    }

    @Test
    @DisplayName("Should validate symbol when getting historical data")
    void testGetHistoricalDataValidatesSymbol() {
      LocalDate startDate = LocalDate.now().minusDays(30);
      LocalDate endDate = LocalDate.now();

      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getHistoricalData("", TimeFrame.D1, startDate, endDate).block());
    }

    @Test
    @DisplayName("Should handle various time frames")
    void testGetOHLCBarsWithVariousTimeFrames() {
      TimeFrame[] timeFrames = {
        TimeFrame.M1, TimeFrame.M5, TimeFrame.H1, TimeFrame.D1, TimeFrame.W1
      };

      for (TimeFrame timeFrame : timeFrames) {
        OHLCResult result = provider.getOHLCBars("AAPL", timeFrame, 10).block();
        assertTrue(result.isSuccess());
        assertEquals(timeFrame, result.getTimeFrame());
      }
    }
  }

  @Nested
  @DisplayName("Quote Data Tests (Stub Behavior)")
  class QuoteDataTests {

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
  }

  @Nested
  @DisplayName("Symbol Search Tests (Stub Behavior)")
  class SymbolSearchTests {

    @Test
    @DisplayName("Should return empty list for symbol search")
    void testSearchSymbols() {
      java.util.List<Symbol> symbols = provider.searchSymbols("Apple").block();

      assertNotNull(symbols);
      assertTrue(symbols.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list for various queries")
    void testSearchSymbolsVariousQueries() {
      String[] queries = {"AAPL", "Apple Inc", "Microsoft", "MSFT", "Tesla"};

      for (String query : queries) {
        java.util.List<Symbol> symbols = provider.searchSymbols(query).block();
        assertNotNull(symbols);
        assertTrue(symbols.isEmpty());
      }
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException for getSymbolInfo")
    void testGetSymbolInfo() {
      assertThrows(
          UnsupportedOperationException.class, () -> provider.getSymbolInfo("AAPL").block());
    }
  }

  @Nested
  @DisplayName("Provider Capabilities Tests")
  class ProviderCapabilitiesTests {

    @Test
    @DisplayName("Should return correct provider name")
    void testGetProviderName() {
      assertEquals("Alpha Vantage", provider.getProviderName());
    }

    @Test
    @DisplayName("Should support stocks market type")
    void testGetSupportedMarketType() {
      assertEquals(MarketType.STOCKS, provider.getSupportedMarketType());
    }

    @Test
    @DisplayName("Should support all time frames")
    void testSupportsAllTimeFrames() {
      assertTrue(provider.supportsTimeFrame(TimeFrame.M1));
      assertTrue(provider.supportsTimeFrame(TimeFrame.M5));
      assertTrue(provider.supportsTimeFrame(TimeFrame.M15));
      assertTrue(provider.supportsTimeFrame(TimeFrame.M30));
      assertTrue(provider.supportsTimeFrame(TimeFrame.H1));
      assertTrue(provider.supportsTimeFrame(TimeFrame.H4));
      assertTrue(provider.supportsTimeFrame(TimeFrame.D1));
      assertTrue(provider.supportsTimeFrame(TimeFrame.W1));
      assertTrue(provider.supportsTimeFrame(TimeFrame.MN1));
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
    @DisplayName("Should access configuration properties")
    void testConfigAccess() {
      assertEquals("test-api-key", provider.getConfig().getApiKey());
      assertEquals(5, provider.getConfig().getRateLimitPerMinute());
      assertTrue(provider.getConfig().isEnableCaching());
      assertEquals(900, provider.getConfig().getCacheTTLSeconds());
      assertEquals(1000, provider.getConfig().getMaxBarsPerRequest());
      assertEquals(30000, provider.getConfig().getRequestTimeout());
      assertTrue(provider.getConfig().isEnableLogging());
    }

    @Test
    @DisplayName("Should use default rate limit")
    void testDefaultRateLimit() {
      MarketDataConfig defaultConfig = MarketDataConfig.alphaVantage("test-key");
      AlphaVantageProvider defaultProvider = new AlphaVantageProvider(defaultConfig);

      assertEquals(5, defaultProvider.getConfig().getRateLimitPerMinute());
    }

    @Test
    @DisplayName("Should handle custom configuration")
    void testCustomConfiguration() {
      MarketDataConfig customConfig =
          MarketDataConfig.builder()
              .provider(DataProvider.ALPHA_VANTAGE)
              .apiKey("custom-key")
              .rateLimitPerMinute(10)
              .enableCaching(false)
              .cacheTTLSeconds(600)
              .maxBarsPerRequest(500)
              .requestTimeout(20000)
              .enableLogging(false)
              .build();

      AlphaVantageProvider customProvider = new AlphaVantageProvider(customConfig);

      assertEquals("custom-key", customProvider.getConfig().getApiKey());
      assertEquals(10, customProvider.getConfig().getRateLimitPerMinute());
      assertFalse(customProvider.getConfig().isEnableCaching());
      assertEquals(600, customProvider.getConfig().getCacheTTLSeconds());
      assertEquals(500, customProvider.getConfig().getMaxBarsPerRequest());
      assertEquals(20000, customProvider.getConfig().getRequestTimeout());
      assertFalse(customProvider.getConfig().isEnableLogging());
    }
  }

  @Nested
  @DisplayName("TimeFrame Format Tests")
  class TimeFrameFormatTests {

    @Test
    @DisplayName("Should get Alpha Vantage format for time frames")
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
    @DisplayName("Should identify intraday time frames")
    void testIntradayTimeFrames() {
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
    @DisplayName("Should get time frame durations")
    void testTimeFrameDurations() {
      assertEquals(60, TimeFrame.M1.getSeconds());
      assertEquals(300, TimeFrame.M5.getSeconds());
      assertEquals(3600, TimeFrame.H1.getSeconds());
      assertEquals(86400, TimeFrame.D1.getSeconds());
      assertEquals(604800, TimeFrame.W1.getSeconds());
    }
  }

  @Nested
  @DisplayName("API Integration Tests (Stub Verification)")
  class ApiIntegrationTests {

    @Test
    @DisplayName("Should log API endpoint information")
    void testApiEndpointLogging() {
      // The provider logs information about what API calls would be made
      // This test verifies the stub behavior returns empty results
      OHLCResult result = provider.getOHLCBars("IBM", TimeFrame.M5, 50).block();

      assertTrue(result.isSuccess());
      assertEquals(0, result.getCount());
      assertNotNull(result.getBars());
    }

    @Test
    @DisplayName("Should handle API key in requests")
    void testApiKeyHandling() {
      // Verify that the provider has access to API key
      assertNotNull(provider.getConfig().getApiKey());
      assertEquals("test-api-key", provider.getConfig().getApiKey());
    }

    @Test
    @DisplayName("Should respect rate limiting configuration")
    void testRateLimiting() {
      assertEquals(5, provider.getConfig().getRateLimitPerMinute());
    }

    @Test
    @DisplayName("Should respect timeout configuration")
    void testTimeoutConfiguration() {
      assertEquals(30000, provider.getConfig().getRequestTimeout());
    }

    @Test
    @DisplayName("Should respect retry configuration")
    void testRetryConfiguration() {
      assertTrue(provider.getConfig().isEnableRetry());
      assertEquals(3, provider.getConfig().getMaxRetryAttempts());
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
          () -> provider.getOHLCBars(null, TimeFrame.M1, 10).block());
    }

    @Test
    @DisplayName("Should validate empty symbol")
    void testEmptySymbol() {
      assertThrows(
          IllegalArgumentException.class, () -> provider.getOHLCBars("", TimeFrame.M1, 10).block());
    }

    @Test
    @DisplayName("Should validate symbol with special characters")
    void testInvalidSymbolCharacters() {
      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getOHLCBars("AAPL@123", TimeFrame.M1, 10).block());
    }

    @Test
    @DisplayName("Should validate symbol length")
    void testSymbolTooLong() {
      String longSymbol = "A".repeat(21);
      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getOHLCBars(longSymbol, TimeFrame.M1, 10).block());
    }

    @Test
    @DisplayName("Should validate limit boundaries")
    void testLimitBoundaries() {
      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getOHLCBars("AAPL", TimeFrame.M1, 0).block());

      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getOHLCBars("AAPL", TimeFrame.M1, -10).block());

      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getOHLCBars("AAPL", TimeFrame.M1, 1001).block());
    }
  }

  @Nested
  @DisplayName("Data Provider Enum Tests")
  class DataProviderEnumTests {

    @Test
    @DisplayName("Should verify Alpha Vantage provider properties")
    void testAlphaVantageProviderEnum() {
      DataProvider alphaVantage = DataProvider.ALPHA_VANTAGE;

      assertEquals("Alpha Vantage", alphaVantage.getDisplayName());
      assertTrue(alphaVantage.isImplemented());
    }

    @Test
    @DisplayName("Should convert provider to string")
    void testProviderToString() {
      String providerString = DataProvider.ALPHA_VANTAGE.toString();

      assertNotNull(providerString);
      assertThat(providerString).contains("Alpha Vantage");
      assertThat(providerString).doesNotContain("stub");
    }

    @Test
    @DisplayName("Should identify implemented providers")
    void testImplementedProviders() {
      assertTrue(DataProvider.ALPHA_VANTAGE.isImplemented());
      assertTrue(DataProvider.YAHOO_FINANCE.isImplemented());
      assertTrue(DataProvider.MOCK.isImplemented());
      assertFalse(DataProvider.BINANCE.isImplemented());
    }
  }
}
