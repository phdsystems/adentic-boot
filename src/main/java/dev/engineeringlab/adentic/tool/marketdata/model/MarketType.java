package dev.engineeringlab.adentic.tool.marketdata.model;

/**
 * Types of financial markets supported.
 *
 * <p>Categorizes trading symbols by asset class.
 */
public enum MarketType {
  /** Stock market (equities). */
  STOCKS("Stocks"),

  /** Cryptocurrency market. */
  CRYPTO("Cryptocurrency"),

  /** Foreign exchange (currency pairs). */
  FOREX("Forex"),

  /** Commodities (gold, oil, etc.). */
  COMMODITIES("Commodities"),

  /** Exchange-traded funds. */
  ETF("ETF"),

  /** Stock market indices. */
  INDICES("Indices"),

  /** Futures contracts. */
  FUTURES("Futures"),

  /** Options contracts. */
  OPTIONS("Options"),

  /** Bonds and fixed income. */
  BONDS("Bonds"),

  /** Unknown or mixed market type. */
  UNKNOWN("Unknown");

  private final String displayName;

  MarketType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String toString() {
    return displayName;
  }
}
