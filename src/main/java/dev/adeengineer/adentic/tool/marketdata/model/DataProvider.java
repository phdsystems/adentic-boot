package dev.adeengineer.adentic.tool.marketdata.model;

/**
 * Supported market data providers.
 *
 * <p>Each provider has different capabilities, rate limits, and supported markets.
 */
public enum DataProvider {
  /**
   * Alpha Vantage API.
   *
   * <p>Supports: Stocks, Forex, Crypto Free tier: 5 requests/min, 500 requests/day
   */
  ALPHA_VANTAGE("Alpha Vantage", true),

  /**
   * Yahoo Finance API.
   *
   * <p>Supports: Stocks, ETFs, Indices Free tier: Generous (best effort)
   */
  YAHOO_FINANCE("Yahoo Finance", true),

  /**
   * Binance API (stub).
   *
   * <p>Supports: Cryptocurrency pairs Free tier: High rate limits
   */
  BINANCE("Binance", false),

  /**
   * CoinGecko API (stub).
   *
   * <p>Supports: Cryptocurrency data Free tier: 10-50 calls/min
   */
  COINGECKO("CoinGecko", false),

  /**
   * IEX Cloud API (stub).
   *
   * <p>Supports: Stocks, ETFs Paid service with free tier
   */
  IEX_CLOUD("IEX Cloud", false),

  /**
   * Polygon.io API (stub).
   *
   * <p>Supports: Stocks, Options, Forex, Crypto Paid service
   */
  POLYGON("Polygon.io", false),

  /**
   * Mock provider for testing.
   *
   * <p>Generates synthetic market data for development and testing.
   */
  MOCK("Mock Provider", true);

  private final String displayName;
  private final boolean implemented;

  DataProvider(String displayName, boolean implemented) {
    this.displayName = displayName;
    this.implemented = implemented;
  }

  /**
   * Get provider display name.
   *
   * @return Display name
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Check if provider is implemented.
   *
   * @return true if implemented
   */
  public boolean isImplemented() {
    return implemented;
  }

  @Override
  public String toString() {
    return displayName + (implemented ? "" : " (stub)");
  }
}
