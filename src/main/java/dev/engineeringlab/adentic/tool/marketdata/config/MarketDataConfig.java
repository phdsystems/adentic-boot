package dev.engineeringlab.adentic.tool.marketdata.config;

import dev.engineeringlab.adentic.tool.marketdata.model.DataProvider;
import lombok.Builder;
import lombok.Data;

/** Configuration for MarketDataTool behavior and provider selection. */
@Data
@Builder
public class MarketDataConfig {
  /** Data provider to use. */
  @Builder.Default private DataProvider provider = DataProvider.ALPHA_VANTAGE;

  /** API key for the selected provider. */
  private String apiKey;

  /** Rate limit (requests per minute). */
  @Builder.Default private int rateLimitPerMinute = 5;

  /** Enable response caching. */
  @Builder.Default private boolean enableCaching = true;

  /** Cache TTL in seconds. */
  @Builder.Default private long cacheTTLSeconds = 900; // 15 minutes

  /** Maximum number of bars per request. */
  @Builder.Default private int maxBarsPerRequest = 1000;

  /** Request timeout in milliseconds. */
  @Builder.Default private long requestTimeout = 30000;

  /** Enable retry on failure. */
  @Builder.Default private boolean enableRetry = true;

  /** Maximum retry attempts. */
  @Builder.Default private int maxRetryAttempts = 3;

  /** Enable request/response logging. */
  @Builder.Default private boolean enableLogging = true;

  /** Base URL override (for custom endpoints). */
  private String baseUrlOverride;

  /**
   * Create default configuration for Alpha Vantage.
   *
   * @param apiKey Alpha Vantage API key
   * @return MarketDataConfig
   */
  public static MarketDataConfig alphaVantage(String apiKey) {
    return MarketDataConfig.builder().provider(DataProvider.ALPHA_VANTAGE).apiKey(apiKey).build();
  }

  /**
   * Create default configuration for Yahoo Finance.
   *
   * @return MarketDataConfig
   */
  public static MarketDataConfig yahooFinance() {
    return MarketDataConfig.builder()
        .provider(DataProvider.YAHOO_FINANCE)
        .rateLimitPerMinute(60) // More generous for Yahoo
        .build();
  }

  /**
   * Create configuration for Binance (stub).
   *
   * @param apiKey Binance API key (optional for public endpoints)
   * @return MarketDataConfig
   */
  public static MarketDataConfig binance(String apiKey) {
    return MarketDataConfig.builder()
        .provider(DataProvider.BINANCE)
        .apiKey(apiKey)
        .rateLimitPerMinute(20)
        .build();
  }

  /**
   * Create configuration for Mock provider (testing).
   *
   * @return MarketDataConfig
   */
  public static MarketDataConfig mock() {
    return MarketDataConfig.builder()
        .provider(DataProvider.MOCK)
        .enableCaching(false)
        .enableLogging(true)
        .build();
  }

  /**
   * Create default configuration (Alpha Vantage with demo key).
   *
   * @return MarketDataConfig
   */
  public static MarketDataConfig defaults() {
    return MarketDataConfig.builder()
        .provider(DataProvider.ALPHA_VANTAGE)
        .apiKey("demo") // Alpha Vantage demo key
        .build();
  }
}
