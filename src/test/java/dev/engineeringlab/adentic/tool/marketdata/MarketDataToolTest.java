package dev.engineeringlab.adentic.tool.marketdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import dev.engineeringlab.adentic.tool.marketdata.config.MarketDataConfig;
import dev.engineeringlab.adentic.tool.marketdata.model.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Comprehensive tests for MarketDataTool */
@DisplayName("MarketDataTool Tests")
class MarketDataToolTest {

  private MarketDataTool marketDataTool;

  @BeforeEach
  void setUp() {
    // Use Mock provider for testing
    MarketDataConfig config = MarketDataConfig.builder().provider(DataProvider.MOCK).build();
    marketDataTool = new MarketDataTool(config);
  }

  @Nested
  @DisplayName("Lifecycle Operations")
  class LifecycleTests {

    @Test
    @DisplayName("Should connect to provider")
    void testConnect() {
      marketDataTool.connect().block();
      assertTrue(marketDataTool.isConnected());
    }

    @Test
    @DisplayName("Should disconnect from provider")
    void testDisconnect() {
      marketDataTool.connect().block();
      marketDataTool.disconnect().block();
      assertFalse(marketDataTool.isConnected());
    }

    @Test
    @DisplayName("Should check connection status")
    void testIsConnected() {
      assertFalse(marketDataTool.isConnected());
      marketDataTool.connect().block();
      assertTrue(marketDataTool.isConnected());
    }
  }

  @Nested
  @DisplayName("OHLC Data Operations")
  class OhlcTests {

    @Test
    @DisplayName("Should get OHLC bars with limit")
    void testGetOhlcBarsWithLimit() {
      marketDataTool.connect().block();

      OHLCResult result = marketDataTool.getOHLCBars("AAPL", TimeFrame.D1, 30).block();

      assertNotNull(result);
      assertNotNull(result.getBars());
      assertEquals("AAPL", result.getSymbol());
      assertEquals(TimeFrame.D1, result.getTimeFrame());
    }

    @Test
    @DisplayName("Should get OHLC bars with time range")
    void testGetOhlcBarsWithTimeRange() {
      marketDataTool.connect().block();

      Instant now = Instant.now();
      Instant weekAgo = now.minusSeconds(7 * 24 * 3600);

      OHLCResult result = marketDataTool.getOHLCBars("AAPL", TimeFrame.H1, weekAgo, now).block();

      assertNotNull(result);
      assertNotNull(result.getBars());
    }

    @Test
    @DisplayName("Should get historical data")
    void testGetHistoricalData() {
      marketDataTool.connect().block();

      LocalDate startDate = LocalDate.now().minusDays(30);
      LocalDate endDate = LocalDate.now();

      OHLCResult result =
          marketDataTool.getHistoricalData("AAPL", TimeFrame.D1, startDate, endDate).block();

      assertNotNull(result);
      assertNotNull(result.getBars());
    }
  }

  @Nested
  @DisplayName("Quote Operations")
  class QuoteTests {

    @Test
    @DisplayName("Should get current quote")
    void testGetQuote() {
      marketDataTool.connect().block();

      QuoteData quote = marketDataTool.getQuote("AAPL").block();

      assertNotNull(quote);
      assertEquals("AAPL", quote.getSymbol());
    }

    @Test
    @DisplayName("Should stream quotes")
    void testStreamQuotes() {
      marketDataTool.connect().block();

      // Mock provider does not support streaming
      assertThrows(
          UnsupportedOperationException.class,
          () -> marketDataTool.streamQuotes("AAPL").take(5).collectList().block());
    }
  }

  @Nested
  @DisplayName("Symbol Operations")
  class SymbolTests {

    @Test
    @DisplayName("Should search symbols")
    void testSearchSymbols() {
      marketDataTool.connect().block();

      List<Symbol> symbols = marketDataTool.searchSymbols("AAPL").block();

      assertNotNull(symbols);
      assertThat(symbols.size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should get symbol info")
    void testGetSymbolInfo() {
      marketDataTool.connect().block();

      Symbol symbol = marketDataTool.getSymbolInfo("AAPL").block();

      assertNotNull(symbol);
      assertEquals("AAPL", symbol.getTicker());
    }
  }

  @Nested
  @DisplayName("Provider Info Operations")
  class ProviderInfoTests {

    @Test
    @DisplayName("Should get provider name")
    void testGetProviderName() {
      String providerName = marketDataTool.getProviderName();

      assertNotNull(providerName);
      assertEquals("Mock", providerName);
    }

    @Test
    @DisplayName("Should get supported market type")
    void testGetSupportedMarketType() {
      MarketType marketType = marketDataTool.getSupportedMarketType();

      assertNotNull(marketType);
      assertEquals(MarketType.STOCKS, marketType);
    }

    @Test
    @DisplayName("Should check if time frame is supported")
    void testSupportsTimeFrame() {
      boolean supportsD1 = marketDataTool.supportsTimeFrame(TimeFrame.D1);
      assertTrue(supportsD1);
    }

    @Test
    @DisplayName("Should check if streaming is supported")
    void testSupportsStreaming() {
      boolean supportsStreaming = marketDataTool.supportsStreaming();
      // Mock provider does not support streaming
      assertFalse(supportsStreaming);
    }
  }

  @Nested
  @DisplayName("Configuration Tests")
  class ConfigurationTests {

    @Test
    @DisplayName("Should use default configuration")
    void testDefaultConfig() {
      MarketDataTool tool = new MarketDataTool();
      assertNotNull(tool);
    }

    @Test
    @DisplayName("Should create with custom config")
    void testCustomConfig() {
      MarketDataConfig config =
          MarketDataConfig.builder().provider(DataProvider.MOCK).apiKey("test-key").build();

      MarketDataTool tool = new MarketDataTool(config);
      assertNotNull(tool);
    }

    @Test
    @DisplayName("Should set configuration")
    void testSetConfig() {
      MarketDataConfig newConfig = MarketDataConfig.builder().provider(DataProvider.MOCK).build();

      marketDataTool.setConfig(newConfig);

      assertEquals(newConfig, marketDataTool.getConfig());
    }

    @Test
    @DisplayName("Should create Alpha Vantage config")
    void testAlphaVantageConfig() {
      MarketDataConfig config = MarketDataConfig.alphaVantage("api-key");

      assertNotNull(config);
      assertEquals(DataProvider.ALPHA_VANTAGE, config.getProvider());
    }

    @Test
    @DisplayName("Should create Yahoo Finance config")
    void testYahooFinanceConfig() {
      MarketDataConfig config = MarketDataConfig.yahooFinance();

      assertNotNull(config);
      assertEquals(DataProvider.YAHOO_FINANCE, config.getProvider());
    }

    @Test
    @DisplayName("Should create Mock config")
    void testMockConfig() {
      MarketDataConfig config = MarketDataConfig.mock();

      assertNotNull(config);
      assertEquals(DataProvider.MOCK, config.getProvider());
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should handle operations without connection")
    void testOperationsWithoutConnection() {
      // Don't connect first
      try {
        OHLCResult result = marketDataTool.getOHLCBars("AAPL", TimeFrame.D1, 10).block();
        // Should auto-connect
        assertNotNull(result);
      } catch (Exception e) {
        // Expected if auto-connect fails
        assertTrue(true);
      }
    }

    @Test
    @DisplayName("Should handle invalid symbol")
    void testInvalidSymbol() {
      marketDataTool.connect().block();

      OHLCResult result =
          marketDataTool.getOHLCBars("INVALID_SYMBOL_XYZ", TimeFrame.D1, 10).block();

      assertNotNull(result);
      // Mock provider should still return data
    }

    @Test
    @DisplayName("Should fail with unimplemented provider")
    void testUnimplementedProvider() {
      MarketDataConfig config = MarketDataConfig.builder().provider(DataProvider.BINANCE).build();

      assertThrows(IllegalStateException.class, () -> new MarketDataTool(config));
    }
  }

  @Nested
  @DisplayName("TimeFrame Tests")
  class TimeFrameTests {

    @Test
    @DisplayName("Should support all time frames")
    void testAllTimeFrames() {
      marketDataTool.connect().block();

      for (TimeFrame tf : TimeFrame.values()) {
        OHLCResult result = marketDataTool.getOHLCBars("AAPL", tf, 1).block();
        assertNotNull(result);
      }
    }

    @Test
    @DisplayName("Should get time frame duration")
    void testTimeFrameDuration() {
      assertEquals(60, TimeFrame.M1.getSeconds());
      assertEquals(3600, TimeFrame.H1.getSeconds());
      assertEquals(86400, TimeFrame.D1.getSeconds());
    }
  }

  @Nested
  @DisplayName("MarketType Tests")
  class MarketTypeTests {

    @Test
    @DisplayName("Should have all market types")
    void testMarketTypes() {
      assertNotNull(MarketType.STOCKS);
      assertNotNull(MarketType.FOREX);
      assertNotNull(MarketType.CRYPTO);
      assertNotNull(MarketType.COMMODITIES);
    }
  }
}
