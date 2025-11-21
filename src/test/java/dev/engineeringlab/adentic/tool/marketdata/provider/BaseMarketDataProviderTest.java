package dev.engineeringlab.adentic.tool.marketdata.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import dev.engineeringlab.adentic.tool.marketdata.config.MarketDataConfig;
import dev.engineeringlab.adentic.tool.marketdata.model.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Comprehensive tests for BaseMarketDataProvider covering:
 *
 * <ul>
 *   <li>Constructor and initialization
 *   <li>Configuration management
 *   <li>Connection state tracking
 *   <li>Validation methods (symbol, limit)
 *   <li>Result creation helpers
 *   <li>Logging utilities
 *   <li>Default implementations
 * </ul>
 */
@DisplayName("BaseMarketDataProvider Tests")
class BaseMarketDataProviderTest {

  private MarketDataConfig config;
  private TestMarketDataProvider provider;

  /** Test implementation of BaseMarketDataProvider for testing purposes. */
  private static class TestMarketDataProvider extends BaseMarketDataProvider {

    private boolean connectCalled = false;
    private boolean disconnectCalled = false;

    public TestMarketDataProvider(MarketDataConfig config) {
      super(config);
    }

    @Override
    public Mono<Void> connect() {
      connectCalled = true;
      setConnected(true);
      return Mono.empty();
    }

    @Override
    public Mono<Void> disconnect() {
      disconnectCalled = true;
      setConnected(false);
      return Mono.empty();
    }

    @Override
    public Mono<OHLCResult> getOHLCBars(String symbol, TimeFrame timeFrame, int limit) {
      validateSymbol(symbol);
      validateLimit(limit);
      List<OHLCBar> bars = createMockBars(symbol, timeFrame, limit);
      return Mono.just(createSuccessResult(bars, symbol, timeFrame, false, 100));
    }

    @Override
    public Mono<OHLCResult> getOHLCBars(
        String symbol, TimeFrame timeFrame, Instant startTime, Instant endTime) {
      return Mono.just(createSuccessResult(List.of(), symbol, timeFrame, false, 100));
    }

    @Override
    public Mono<OHLCResult> getHistoricalData(
        String symbol, TimeFrame timeFrame, LocalDate startDate, LocalDate endDate) {
      return Mono.just(createSuccessResult(List.of(), symbol, timeFrame, false, 100));
    }

    @Override
    public Mono<QuoteData> getQuote(String symbol) {
      return Mono.error(new UnsupportedOperationException("Not implemented"));
    }

    @Override
    public Flux<QuoteData> streamQuotes(String symbol) {
      return Flux.error(new UnsupportedOperationException("Streaming not supported"));
    }

    @Override
    public Mono<List<Symbol>> searchSymbols(String query) {
      return Mono.just(List.of());
    }

    @Override
    public Mono<Symbol> getSymbolInfo(String symbol) {
      return Mono.error(new UnsupportedOperationException("Not implemented"));
    }

    @Override
    public String getProviderName() {
      return "TestProvider";
    }

    @Override
    public MarketType getSupportedMarketType() {
      return MarketType.STOCKS;
    }

    private List<OHLCBar> createMockBars(String symbol, TimeFrame timeFrame, int limit) {
      List<OHLCBar> bars = new ArrayList<>();
      Instant now = Instant.now();
      for (int i = 0; i < limit; i++) {
        bars.add(
            OHLCBar.builder()
                .symbol(symbol)
                .timeFrame(timeFrame)
                .timestamp(now.minusSeconds(timeFrame.getSeconds() * i))
                .open(BigDecimal.valueOf(100 + i))
                .high(BigDecimal.valueOf(105 + i))
                .low(BigDecimal.valueOf(95 + i))
                .close(BigDecimal.valueOf(102 + i))
                .volume(BigDecimal.valueOf(1000000))
                .build());
      }
      return bars;
    }

    public boolean isConnectCalled() {
      return connectCalled;
    }

    public boolean isDisconnectCalled() {
      return disconnectCalled;
    }
  }

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
            .enableRetry(true)
            .maxRetryAttempts(3)
            .enableLogging(true)
            .build();
    provider = new TestMarketDataProvider(config);
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
    @DisplayName("Should not be connected initially")
    void testInitialConnectionState() {
      assertFalse(provider.isConnected());
    }

    @Test
    @DisplayName("Should initialize with default config")
    void testInitializationWithDefaultConfig() {
      MarketDataConfig defaultConfig = MarketDataConfig.defaults();
      TestMarketDataProvider defaultProvider = new TestMarketDataProvider(defaultConfig);

      assertNotNull(defaultProvider);
      assertEquals(DataProvider.ALPHA_VANTAGE, defaultProvider.getConfig().getProvider());
    }

    @Test
    @DisplayName("Should initialize with mock config")
    void testInitializationWithMockConfig() {
      MarketDataConfig mockConfig = MarketDataConfig.mock();
      TestMarketDataProvider mockProvider = new TestMarketDataProvider(mockConfig);

      assertNotNull(mockProvider);
      assertEquals(DataProvider.MOCK, mockProvider.getConfig().getProvider());
      assertFalse(mockProvider.getConfig().isEnableCaching());
    }
  }

  @Nested
  @DisplayName("Configuration Access Tests")
  class ConfigurationAccessTests {

    @Test
    @DisplayName("Should access API key from config")
    void testGetApiKey() {
      assertEquals("test-api-key", provider.getApiKey());
    }

    @Test
    @DisplayName("Should access request timeout from config")
    void testGetRequestTimeout() {
      assertEquals(30000, provider.getRequestTimeout());
    }

    @Test
    @DisplayName("Should check if caching is enabled")
    void testIsCachingEnabled() {
      assertTrue(provider.isCachingEnabled());
    }

    @Test
    @DisplayName("Should access cache TTL")
    void testGetCacheTTL() {
      assertEquals(900, provider.getCacheTTL());
    }

    @Test
    @DisplayName("Should check if logging is enabled")
    void testIsLoggingEnabled() {
      assertTrue(provider.isLoggingEnabled());
    }

    @Test
    @DisplayName("Should access full config object")
    void testGetConfig() {
      MarketDataConfig retrievedConfig = provider.getConfig();

      assertNotNull(retrievedConfig);
      assertEquals(config.getApiKey(), retrievedConfig.getApiKey());
      assertEquals(config.getProvider(), retrievedConfig.getProvider());
      assertEquals(config.getMaxBarsPerRequest(), retrievedConfig.getMaxBarsPerRequest());
    }
  }

  @Nested
  @DisplayName("Connection State Management Tests")
  class ConnectionStateTests {

    @Test
    @DisplayName("Should connect successfully")
    void testConnect() {
      provider.connect().block();

      assertTrue(provider.isConnected());
      assertTrue(provider.isConnectCalled());
    }

    @Test
    @DisplayName("Should disconnect successfully")
    void testDisconnect() {
      provider.connect().block();
      assertTrue(provider.isConnected());

      provider.disconnect().block();

      assertFalse(provider.isConnected());
      assertTrue(provider.isDisconnectCalled());
    }

    @Test
    @DisplayName("Should track connection state changes")
    void testConnectionStateTracking() {
      assertFalse(provider.isConnected());

      provider.connect().block();
      assertTrue(provider.isConnected());

      provider.disconnect().block();
      assertFalse(provider.isConnected());
    }

    @Test
    @DisplayName("Should allow multiple connect calls")
    void testMultipleConnectCalls() {
      provider.connect().block();
      assertTrue(provider.isConnected());

      provider.connect().block();
      assertTrue(provider.isConnected());
    }

    @Test
    @DisplayName("Should allow multiple disconnect calls")
    void testMultipleDisconnectCalls() {
      provider.connect().block();
      provider.disconnect().block();
      assertFalse(provider.isConnected());

      provider.disconnect().block();
      assertFalse(provider.isConnected());
    }
  }

  @Nested
  @DisplayName("Symbol Validation Tests")
  class SymbolValidationTests {

    @Test
    @DisplayName("Should validate valid alphanumeric symbol")
    void testValidateValidSymbol() {
      assertDoesNotThrow(() -> provider.validateSymbol("AAPL"));
      assertDoesNotThrow(() -> provider.validateSymbol("BTCUSDT"));
      assertDoesNotThrow(() -> provider.validateSymbol("SPY"));
    }

    @Test
    @DisplayName("Should validate symbol with dash")
    void testValidateSymbolWithDash() {
      assertDoesNotThrow(() -> provider.validateSymbol("BRK-A"));
      assertDoesNotThrow(() -> provider.validateSymbol("BTC-USD"));
    }

    @Test
    @DisplayName("Should validate symbol with underscore")
    void testValidateSymbolWithUnderscore() {
      assertDoesNotThrow(() -> provider.validateSymbol("BTC_USDT"));
    }

    @Test
    @DisplayName("Should validate symbol with dot")
    void testValidateSymbolWithDot() {
      assertDoesNotThrow(() -> provider.validateSymbol("BRK.A"));
    }

    @Test
    @DisplayName("Should reject null symbol")
    void testValidateNullSymbol() {
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> provider.validateSymbol(null));

      assertThat(exception.getMessage()).contains("cannot be null or empty");
    }

    @Test
    @DisplayName("Should reject empty symbol")
    void testValidateEmptySymbol() {
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> provider.validateSymbol(""));

      assertThat(exception.getMessage()).contains("cannot be null or empty");
    }

    @Test
    @DisplayName("Should reject whitespace-only symbol")
    void testValidateWhitespaceSymbol() {
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> provider.validateSymbol("   "));

      assertThat(exception.getMessage()).contains("cannot be null or empty");
    }

    @Test
    @DisplayName("Should reject symbol with special characters")
    void testValidateSymbolWithSpecialChars() {
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> provider.validateSymbol("AAPL@123"));

      assertThat(exception.getMessage()).contains("Invalid symbol format");
    }

    @Test
    @DisplayName("Should reject symbol with spaces")
    void testValidateSymbolWithSpaces() {
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> provider.validateSymbol("AAPL TEST"));

      assertThat(exception.getMessage()).contains("Invalid symbol format");
    }

    @Test
    @DisplayName("Should reject symbol exceeding max length")
    void testValidateSymbolTooLong() {
      String longSymbol = "A".repeat(21);

      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> provider.validateSymbol(longSymbol));

      assertThat(exception.getMessage()).contains("Symbol too long");
    }

    @Test
    @DisplayName("Should accept symbol at max length")
    void testValidateSymbolAtMaxLength() {
      String maxLengthSymbol = "A".repeat(20);

      assertDoesNotThrow(() -> provider.validateSymbol(maxLengthSymbol));
    }
  }

  @Nested
  @DisplayName("Limit Validation Tests")
  class LimitValidationTests {

    @Test
    @DisplayName("Should validate positive limit")
    void testValidatePositiveLimit() {
      assertDoesNotThrow(() -> provider.validateLimit(1));
      assertDoesNotThrow(() -> provider.validateLimit(100));
      assertDoesNotThrow(() -> provider.validateLimit(500));
    }

    @Test
    @DisplayName("Should accept limit at max")
    void testValidateLimitAtMax() {
      assertDoesNotThrow(() -> provider.validateLimit(1000));
    }

    @Test
    @DisplayName("Should reject zero limit")
    void testValidateZeroLimit() {
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> provider.validateLimit(0));

      assertThat(exception.getMessage()).contains("must be positive");
    }

    @Test
    @DisplayName("Should reject negative limit")
    void testValidateNegativeLimit() {
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> provider.validateLimit(-1));

      assertThat(exception.getMessage()).contains("must be positive");
    }

    @Test
    @DisplayName("Should reject limit exceeding max")
    void testValidateLimitExceedingMax() {
      IllegalArgumentException exception =
          assertThrows(IllegalArgumentException.class, () -> provider.validateLimit(1001));

      assertThat(exception.getMessage()).contains("exceeds maximum");
    }
  }

  @Nested
  @DisplayName("Result Creation Tests")
  class ResultCreationTests {

    @Test
    @DisplayName("Should create successful OHLC result")
    void testCreateSuccessResult() {
      List<OHLCBar> bars = createTestBars(5);
      OHLCResult result = provider.createSuccessResult(bars, "AAPL", TimeFrame.M1, false, 100);

      assertTrue(result.isSuccess());
      assertEquals(5, result.getCount());
      assertEquals("AAPL", result.getSymbol());
      assertEquals(TimeFrame.M1, result.getTimeFrame());
      assertEquals("TestProvider", result.getProvider());
      assertFalse(result.isCached());
      assertEquals(100, result.getExecutionTimeMs());
      assertNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("Should create successful result with cached flag")
    void testCreateSuccessResultCached() {
      List<OHLCBar> bars = createTestBars(3);
      OHLCResult result = provider.createSuccessResult(bars, "AAPL", TimeFrame.H1, true, 10);

      assertTrue(result.isSuccess());
      assertTrue(result.isCached());
      assertEquals(10, result.getExecutionTimeMs());
    }

    @Test
    @DisplayName("Should create successful result with empty bars")
    void testCreateSuccessResultEmpty() {
      OHLCResult result = provider.createSuccessResult(List.of(), "AAPL", TimeFrame.D1, false, 50);

      assertTrue(result.isSuccess());
      assertEquals(0, result.getCount());
      assertNotNull(result.getBars());
      assertTrue(result.getBars().isEmpty());
    }

    @Test
    @DisplayName("Should create failure result")
    void testCreateFailureResult() {
      OHLCResult result = provider.createFailureResult("AAPL", TimeFrame.M5, "Connection timeout");

      assertFalse(result.isSuccess());
      assertEquals("AAPL", result.getSymbol());
      assertEquals(TimeFrame.M5, result.getTimeFrame());
      assertEquals("TestProvider", result.getProvider());
      assertEquals("Connection timeout", result.getErrorMessage());
      assertNull(result.getBars());
    }

    @Test
    @DisplayName("Should create failure result with error message")
    void testCreateFailureResultWithMessage() {
      OHLCResult result = provider.createFailureResult("BTCUSDT", TimeFrame.H4, "API key invalid");

      assertFalse(result.isSuccess());
      assertThat(result.getErrorMessage()).contains("API key invalid");
    }

    private List<OHLCBar> createTestBars(int count) {
      List<OHLCBar> bars = new ArrayList<>();
      Instant now = Instant.now();
      for (int i = 0; i < count; i++) {
        bars.add(
            OHLCBar.builder()
                .symbol("AAPL")
                .timeFrame(TimeFrame.M1)
                .timestamp(now.minusSeconds(60L * i))
                .open(BigDecimal.valueOf(150))
                .high(BigDecimal.valueOf(155))
                .low(BigDecimal.valueOf(145))
                .close(BigDecimal.valueOf(152))
                .volume(BigDecimal.valueOf(1000000))
                .build());
      }
      return bars;
    }
  }

  @Nested
  @DisplayName("Default Implementation Tests")
  class DefaultImplementationTests {

    @Test
    @DisplayName("Should support all time frames by default")
    void testSupportsTimeFrame() {
      assertTrue(provider.supportsTimeFrame(TimeFrame.M1));
      assertTrue(provider.supportsTimeFrame(TimeFrame.M5));
      assertTrue(provider.supportsTimeFrame(TimeFrame.H1));
      assertTrue(provider.supportsTimeFrame(TimeFrame.D1));
      assertTrue(provider.supportsTimeFrame(TimeFrame.W1));
    }

    @Test
    @DisplayName("Should not support streaming by default")
    void testSupportsStreaming() {
      assertFalse(provider.supportsStreaming());
    }

    @Test
    @DisplayName("Should return provider name")
    void testGetProviderName() {
      assertEquals("TestProvider", provider.getProviderName());
    }

    @Test
    @DisplayName("Should return supported market type")
    void testGetSupportedMarketType() {
      assertEquals(MarketType.STOCKS, provider.getSupportedMarketType());
    }
  }

  @Nested
  @DisplayName("OHLC Data Retrieval Tests")
  class OHLCDataRetrievalTests {

    @Test
    @DisplayName("Should retrieve OHLC bars with limit")
    void testGetOHLCBarsWithLimit() {
      OHLCResult result = provider.getOHLCBars("AAPL", TimeFrame.M5, 10).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(10, result.getCount());
      assertEquals("AAPL", result.getSymbol());
      assertEquals(TimeFrame.M5, result.getTimeFrame());
    }

    @Test
    @DisplayName("Should retrieve OHLC bars with time range")
    void testGetOHLCBarsWithTimeRange() {
      Instant startTime = Instant.now().minusSeconds(3600);
      Instant endTime = Instant.now();

      OHLCResult result = provider.getOHLCBars("AAPL", TimeFrame.M15, startTime, endTime).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should retrieve historical data with date range")
    void testGetHistoricalData() {
      LocalDate startDate = LocalDate.now().minusDays(30);
      LocalDate endDate = LocalDate.now();

      OHLCResult result =
          provider.getHistoricalData("AAPL", TimeFrame.D1, startDate, endDate).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should fail with invalid symbol")
    void testGetOHLCBarsInvalidSymbol() {
      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getOHLCBars("INVALID@SYMBOL", TimeFrame.M1, 10).block());
    }

    @Test
    @DisplayName("Should fail with invalid limit")
    void testGetOHLCBarsInvalidLimit() {
      assertThrows(
          IllegalArgumentException.class,
          () -> provider.getOHLCBars("AAPL", TimeFrame.M1, -1).block());
    }
  }

  @Nested
  @DisplayName("Provider Information Tests")
  class ProviderInformationTests {

    @Test
    @DisplayName("Should return Alpha Vantage config")
    void testAlphaVantageConfig() {
      MarketDataConfig avConfig = MarketDataConfig.alphaVantage("test-key");

      assertEquals(DataProvider.ALPHA_VANTAGE, avConfig.getProvider());
      assertEquals("test-key", avConfig.getApiKey());
    }

    @Test
    @DisplayName("Should return Yahoo Finance config")
    void testYahooFinanceConfig() {
      MarketDataConfig yahooConfig = MarketDataConfig.yahooFinance();

      assertEquals(DataProvider.YAHOO_FINANCE, yahooConfig.getProvider());
      assertEquals(60, yahooConfig.getRateLimitPerMinute());
    }

    @Test
    @DisplayName("Should return Binance config")
    void testBinanceConfig() {
      MarketDataConfig binanceConfig = MarketDataConfig.binance("binance-key");

      assertEquals(DataProvider.BINANCE, binanceConfig.getProvider());
      assertEquals("binance-key", binanceConfig.getApiKey());
      assertEquals(20, binanceConfig.getRateLimitPerMinute());
    }

    @Test
    @DisplayName("Should return mock config")
    void testMockConfig() {
      MarketDataConfig mockConfig = MarketDataConfig.mock();

      assertEquals(DataProvider.MOCK, mockConfig.getProvider());
      assertFalse(mockConfig.isEnableCaching());
      assertTrue(mockConfig.isEnableLogging());
    }
  }
}
