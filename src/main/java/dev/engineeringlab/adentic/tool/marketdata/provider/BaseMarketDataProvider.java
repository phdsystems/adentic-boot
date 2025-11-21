package dev.engineeringlab.adentic.tool.marketdata.provider;

import dev.engineeringlab.adentic.tool.marketdata.config.MarketDataConfig;
import dev.engineeringlab.adentic.tool.marketdata.model.*;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for market data provider implementations.
 *
 * <p>Provides common functionality for all providers: - Configuration management - Connection state
 * tracking - Logging utilities - Result creation helpers - Rate limiting support
 */
@Slf4j
public abstract class BaseMarketDataProvider implements MarketDataProvider {

  private final MarketDataConfig config;
  private boolean connected = false;

  protected BaseMarketDataProvider(MarketDataConfig config) {
    this.config = config;
  }

  /** Get provider configuration. */
  @Override
  public MarketDataConfig getConfig() {
    return config;
  }

  /** Set connected state. */
  protected void setConnected(boolean isConnected) {
    this.connected = isConnected;
  }

  @Override
  public boolean isConnected() {
    return connected;
  }

  /**
   * Log a market data operation.
   *
   * @param operation Operation name
   * @param details Operation details
   */
  protected void logOperation(String operation, String details) {
    if (config.isEnableLogging()) {
      log.debug("[{}] {} - {}", getProviderName(), operation, details);
    }
  }

  /**
   * Create a successful OHLC result.
   *
   * @param bars OHLC bars
   * @param symbol Trading symbol
   * @param timeFrame Time frame
   * @param cached Whether data was cached
   * @param executionTimeMs Execution time
   * @return OHLC result
   */
  protected OHLCResult createSuccessResult(
      List<OHLCBar> bars,
      String symbol,
      TimeFrame timeFrame,
      boolean cached,
      long executionTimeMs) {
    return OHLCResult.builder()
        .success(true)
        .bars(bars)
        .count(bars != null ? bars.size() : 0)
        .symbol(symbol)
        .timeFrame(timeFrame)
        .provider(getProviderName())
        .cached(cached)
        .executionTimeMs(executionTimeMs)
        .build();
  }

  /**
   * Create a failed OHLC result.
   *
   * @param symbol Trading symbol
   * @param timeFrame Time frame
   * @param error Error message
   * @return OHLC result
   */
  protected OHLCResult createFailureResult(String symbol, TimeFrame timeFrame, String error) {
    log.error("[{}] Failed to retrieve data for {}: {}", getProviderName(), symbol, error);
    return OHLCResult.builder()
        .success(false)
        .symbol(symbol)
        .timeFrame(timeFrame)
        .provider(getProviderName())
        .errorMessage(error)
        .build();
  }

  /**
   * Validate symbol to prevent injection attacks.
   *
   * @param symbol Trading symbol
   * @throws IllegalArgumentException if symbol is invalid
   */
  protected void validateSymbol(String symbol) {
    if (symbol == null || symbol.trim().isEmpty()) {
      throw new IllegalArgumentException("Symbol cannot be null or empty");
    }

    // Basic validation: alphanumeric, dash, underscore, dot
    if (!symbol.matches("^[A-Za-z0-9._-]+$")) {
      throw new IllegalArgumentException("Invalid symbol format: " + symbol);
    }

    // Length check
    if (symbol.length() > 20) {
      throw new IllegalArgumentException("Symbol too long (max 20 characters): " + symbol);
    }
  }

  /**
   * Validate limit parameter.
   *
   * @param limit Number of bars requested
   * @throws IllegalArgumentException if limit is invalid
   */
  protected void validateLimit(int limit) {
    if (limit <= 0) {
      throw new IllegalArgumentException("Limit must be positive: " + limit);
    }

    if (limit > config.getMaxBarsPerRequest()) {
      throw new IllegalArgumentException(
          String.format("Limit exceeds maximum (%d > %d)", limit, config.getMaxBarsPerRequest()));
    }
  }

  /**
   * Check if request logging is enabled.
   *
   * @return true if logging is enabled
   */
  protected boolean isLoggingEnabled() {
    return config.isEnableLogging();
  }

  /**
   * Get request timeout in milliseconds.
   *
   * @return Request timeout
   */
  protected long getRequestTimeout() {
    return config.getRequestTimeout();
  }

  /**
   * Get API key from configuration.
   *
   * @return API key
   */
  protected String getApiKey() {
    return config.getApiKey();
  }

  /**
   * Check if caching is enabled.
   *
   * @return true if caching is enabled
   */
  protected boolean isCachingEnabled() {
    return config.isEnableCaching();
  }

  /**
   * Get cache TTL in seconds.
   *
   * @return Cache TTL
   */
  protected long getCacheTTL() {
    return config.getCacheTTLSeconds();
  }

  @Override
  public boolean supportsTimeFrame(TimeFrame timeFrame) {
    // Default: support all time frames
    return true;
  }

  @Override
  public boolean supportsStreaming() {
    // Default: no streaming support
    return false;
  }
}
