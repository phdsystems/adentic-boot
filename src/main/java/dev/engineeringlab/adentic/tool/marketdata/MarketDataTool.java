package dev.engineeringlab.adentic.tool.marketdata;

import dev.engineeringlab.adentic.tool.marketdata.config.MarketDataConfig;
import dev.engineeringlab.adentic.tool.marketdata.model.*;
import dev.engineeringlab.adentic.tool.marketdata.provider.*;
import dev.engineeringlab.annotation.provider.ToolProvider;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Tool for retrieving market data (OHLC, quotes) from multiple providers.
 *
 * <p>Supports multiple data providers:
 *
 * <ul>
 *   <li>**Alpha Vantage** - Stocks, Forex, Crypto (free tier: 5 req/min)
 *   <li>**Yahoo Finance** - Stocks, ETFs, Indices (free tier: best effort)
 *   <li>**Binance** - Cryptocurrency pairs (stub)
 *   <li>**CoinGecko** - Cryptocurrency data (stub)
 *   <li>**Mock** - Synthetic data for testing
 * </ul>
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Provider/service pattern for flexibility
 *   <li>Runtime provider switching
 *   <li>OHLC bars at multiple time frames (M1, M5, M15, H1, H4, D1, W1, MN1)
 *   <li>Real-time quotes
 *   <li>Historical data retrieval
 *   <li>Symbol search
 *   <li>Rate limiting and caching
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Inject
 * private MarketDataTool marketData;
 *
 * // Get daily Apple stock prices (last 30 days)
 * OHLCResult result = marketData.getOHLCBars("AAPL", TimeFrame.D1, 30).block();
 *
 * // Get Bitcoin 1-hour candles
 * marketData.setConfig(MarketDataConfig.binance("api-key"));
 * OHLCResult btc = marketData.getOHLCBars("BTCUSDT", TimeFrame.H1, 24).block();
 *
 * // Get real-time quote
 * QuoteData quote = marketData.getQuote("TSLA").block();
 * }</pre>
 *
 * @see MarketDataConfig
 * @see MarketDataProvider
 * @see DataProvider
 */
@ToolProvider(name = "market-data")
@Slf4j
public class MarketDataTool {

  private MarketDataConfig config;
  private MarketDataProvider provider;

  /** Default constructor with default configuration (Alpha Vantage with demo key). */
  public MarketDataTool() {
    this(MarketDataConfig.defaults());
  }

  /** Constructor with custom configuration. */
  public MarketDataTool(MarketDataConfig config) {
    this.config = config;
    this.provider = createProvider(config);
  }

  /** Set configuration and recreate provider. */
  public void setConfig(MarketDataConfig config) {
    // Close existing provider if connected
    if (provider != null && provider.isConnected()) {
      provider.disconnect().block();
    }

    this.config = config;
    this.provider = createProvider(config);

    log.info("Switched to {} provider", config.getProvider());
  }

  /** Get current configuration. */
  public MarketDataConfig getConfig() {
    return config;
  }

  /**
   * Create provider based on configuration.
   *
   * @param config Market data configuration
   * @return MarketDataProvider instance
   */
  private MarketDataProvider createProvider(MarketDataConfig config) {
    return switch (config.getProvider()) {
      case ALPHA_VANTAGE -> new AlphaVantageProvider(config);
      case YAHOO_FINANCE -> new YahooFinanceProvider(config);
      case MOCK -> new MockMarketDataProvider(config);
      case BINANCE, COINGECKO, IEX_CLOUD, POLYGON ->
          throw new IllegalStateException(
              config.getProvider() + " provider is not yet implemented (stub)");
    };
  }

  /** Ensure provider is connected. */
  private Mono<Void> ensureConnected() {
    if (!provider.isConnected()) {
      return provider.connect();
    }
    return Mono.empty();
  }

  // ========== LIFECYCLE ==========

  /** Connect to the data provider. */
  public Mono<Void> connect() {
    return provider.connect();
  }

  /** Disconnect from the data provider. */
  public Mono<Void> disconnect() {
    return provider.disconnect();
  }

  /** Check if provider is connected. */
  public boolean isConnected() {
    return provider.isConnected();
  }

  // ========== OHLC DATA ==========

  /**
   * Get OHLC bars for a symbol with a limit on number of bars.
   *
   * @param symbol Trading symbol (e.g., "AAPL", "BTCUSDT")
   * @param timeFrame Time frame for bars
   * @param limit Maximum number of bars to retrieve
   * @return OHLC result with bars
   */
  public Mono<OHLCResult> getOHLCBars(String symbol, TimeFrame timeFrame, int limit) {
    return ensureConnected().then(provider.getOHLCBars(symbol, timeFrame, limit));
  }

  /**
   * Get OHLC bars for a symbol within a time range.
   *
   * @param symbol Trading symbol
   * @param timeFrame Time frame for bars
   * @param startTime Start time (inclusive)
   * @param endTime End time (inclusive)
   * @return OHLC result with bars
   */
  public Mono<OHLCResult> getOHLCBars(
      String symbol, TimeFrame timeFrame, Instant startTime, Instant endTime) {
    return ensureConnected().then(provider.getOHLCBars(symbol, timeFrame, startTime, endTime));
  }

  /**
   * Get historical OHLC data by date range.
   *
   * @param symbol Trading symbol
   * @param timeFrame Time frame for bars
   * @param startDate Start date (inclusive)
   * @param endDate End date (inclusive)
   * @return OHLC result with bars
   */
  public Mono<OHLCResult> getHistoricalData(
      String symbol, TimeFrame timeFrame, LocalDate startDate, LocalDate endDate) {
    return ensureConnected()
        .then(provider.getHistoricalData(symbol, timeFrame, startDate, endDate));
  }

  // ========== REAL-TIME QUOTES ==========

  /**
   * Get current quote for a symbol.
   *
   * @param symbol Trading symbol
   * @return Quote data
   */
  public Mono<QuoteData> getQuote(String symbol) {
    return ensureConnected().then(provider.getQuote(symbol));
  }

  /**
   * Stream real-time quotes for a symbol.
   *
   * @param symbol Trading symbol
   * @return Flux of quote updates
   */
  public Flux<QuoteData> streamQuotes(String symbol) {
    return ensureConnected().thenMany(provider.streamQuotes(symbol));
  }

  // ========== SYMBOL SEARCH ==========

  /**
   * Search for symbols matching a query.
   *
   * @param query Search query (symbol or name)
   * @return List of matching symbols
   */
  public Mono<List<Symbol>> searchSymbols(String query) {
    return ensureConnected().then(provider.searchSymbols(query));
  }

  /**
   * Get detailed information about a symbol.
   *
   * @param symbol Trading symbol
   * @return Symbol information
   */
  public Mono<Symbol> getSymbolInfo(String symbol) {
    return ensureConnected().then(provider.getSymbolInfo(symbol));
  }

  // ========== PROVIDER INFO ==========

  /**
   * Get current provider name.
   *
   * @return Provider name
   */
  public String getProviderName() {
    return provider.getProviderName();
  }

  /**
   * Get supported market type.
   *
   * @return Market type
   */
  public MarketType getSupportedMarketType() {
    return provider.getSupportedMarketType();
  }

  /**
   * Check if provider supports a specific time frame.
   *
   * @param timeFrame Time frame to check
   * @return true if supported
   */
  public boolean supportsTimeFrame(TimeFrame timeFrame) {
    return provider.supportsTimeFrame(timeFrame);
  }

  /**
   * Check if provider supports streaming quotes.
   *
   * @return true if streaming is supported
   */
  public boolean supportsStreaming() {
    return provider.supportsStreaming();
  }
}
