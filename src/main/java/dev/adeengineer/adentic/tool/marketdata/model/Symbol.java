package dev.adeengineer.adentic.tool.marketdata.model;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a trading symbol with metadata.
 *
 * <p>Contains symbol information like ticker, name, exchange, and market type.
 */
@Data
@Builder
public class Symbol {
  /** Trading symbol/ticker (e.g., "AAPL", "BTCUSDT"). */
  private String ticker;

  /** Full name of the asset (e.g., "Apple Inc.", "Bitcoin/USD Tether"). */
  private String name;

  /** Exchange where the symbol is traded (e.g., "NASDAQ", "NYSE", "Binance"). */
  private String exchange;

  /** Market type. */
  private MarketType marketType;

  /** Currency of the asset (e.g., "USD", "EUR"). */
  private String currency;

  /** Region/country code (e.g., "US", "UK"). */
  private String region;

  /** Whether the symbol is actively traded. */
  @Builder.Default private boolean active = true;

  /** Additional description or notes. */
  private String description;

  @Override
  public String toString() {
    return String.format("%s - %s (%s, %s)", ticker, name, exchange, marketType);
  }
}
