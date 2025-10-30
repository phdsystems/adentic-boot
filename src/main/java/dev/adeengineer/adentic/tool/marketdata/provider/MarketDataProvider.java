package dev.adeengineer.adentic.tool.marketdata.provider;

import dev.adeengineer.adentic.tool.marketdata.config.MarketDataConfig;
import dev.adeengineer.adentic.tool.marketdata.model.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interface for market data providers.
 *
 * <p>Implementations include Alpha Vantage, Yahoo Finance, Binance, etc.
 */
public interface MarketDataProvider {

  // ========== LIFECYCLE ==========

  /** Connect to the data provider. */
  Mono<Void> connect();

  /** Disconnect from the data provider. */
  Mono<Void> disconnect();

  /** Check if provider is connected. */
  boolean isConnected();

  /** Get provider configuration. */
  MarketDataConfig getConfig();

  // ========== OHLC DATA ==========

  /**
   * Get OHLC bars for a symbol with a limit on number of bars.
   *
   * @param symbol Trading symbol (e.g., "AAPL", "BTCUSDT")
   * @param timeFrame Time frame for bars
   * @param limit Maximum number of bars to retrieve
   * @return OHLC result with bars
   */
  Mono<OHLCResult> getOHLCBars(String symbol, TimeFrame timeFrame, int limit);

  /**
   * Get OHLC bars for a symbol within a time range.
   *
   * @param symbol Trading symbol
   * @param timeFrame Time frame for bars
   * @param startTime Start time (inclusive)
   * @param endTime End time (inclusive)
   * @return OHLC result with bars
   */
  Mono<OHLCResult> getOHLCBars(
      String symbol, TimeFrame timeFrame, Instant startTime, Instant endTime);

  /**
   * Get historical OHLC data by date range.
   *
   * @param symbol Trading symbol
   * @param timeFrame Time frame for bars
   * @param startDate Start date (inclusive)
   * @param endDate End date (inclusive)
   * @return OHLC result with bars
   */
  Mono<OHLCResult> getHistoricalData(
      String symbol, TimeFrame timeFrame, LocalDate startDate, LocalDate endDate);

  // ========== REAL-TIME QUOTES ==========

  /**
   * Get current quote for a symbol.
   *
   * @param symbol Trading symbol
   * @return Quote data
   */
  Mono<QuoteData> getQuote(String symbol);

  /**
   * Stream real-time quotes for a symbol.
   *
   * @param symbol Trading symbol
   * @return Flux of quote updates
   */
  Flux<QuoteData> streamQuotes(String symbol);

  // ========== SYMBOL SEARCH ==========

  /**
   * Search for symbols matching a query.
   *
   * @param query Search query (symbol or name)
   * @return List of matching symbols
   */
  Mono<List<Symbol>> searchSymbols(String query);

  /**
   * Get detailed information about a symbol.
   *
   * @param symbol Trading symbol
   * @return Symbol information
   */
  Mono<Symbol> getSymbolInfo(String symbol);

  // ========== PROVIDER INFO ==========

  /**
   * Get provider name.
   *
   * @return Provider name
   */
  String getProviderName();

  /**
   * Get supported market type.
   *
   * @return Market type
   */
  MarketType getSupportedMarketType();

  /**
   * Check if provider supports a specific time frame.
   *
   * @param timeFrame Time frame to check
   * @return true if supported
   */
  boolean supportsTimeFrame(TimeFrame timeFrame);

  /**
   * Check if provider supports streaming quotes.
   *
   * @return true if streaming is supported
   */
  boolean supportsStreaming();
}
