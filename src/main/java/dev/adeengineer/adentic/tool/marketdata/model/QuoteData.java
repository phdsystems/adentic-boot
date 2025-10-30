package dev.adeengineer.adentic.tool.marketdata.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

/**
 * Represents real-time quote data for a trading symbol.
 *
 * <p>Contains bid/ask prices, last trade, and volume information.
 */
@Data
@Builder(toBuilder = true)
@SuppressWarnings("unused")
public class QuoteData {
  /** Trading symbol. */
  private String symbol;

  /** Last traded price. */
  private BigDecimal lastPrice;

  /** Bid price (highest price buyers are willing to pay). */
  private BigDecimal bidPrice;

  /** Ask price (lowest price sellers are willing to accept). */
  private BigDecimal askPrice;

  /** Bid size (quantity at bid price). */
  private BigDecimal bidSize;

  /** Ask size (quantity at ask price). */
  private BigDecimal askSize;

  /** 24-hour trading volume. */
  private BigDecimal volume;

  /** Opening price for the day. */
  private BigDecimal openPrice;

  /** Highest price for the day. */
  private BigDecimal highPrice;

  /** Lowest price for the day. */
  private BigDecimal lowPrice;

  /** Previous day's closing price. */
  private BigDecimal previousClose;

  /** Timestamp of the quote. */
  private Instant timestamp;

  /** Data provider name. */
  private String provider;

  /**
   * Calculate the bid-ask spread.
   *
   * @return Spread (ask - bid)
   */
  public BigDecimal getSpread() {
    if (askPrice == null || bidPrice == null) {
      return BigDecimal.ZERO;
    }
    return askPrice.subtract(bidPrice);
  }

  /**
   * Calculate the price change from previous close.
   *
   * @return Price change (last - previous close)
   */
  public BigDecimal getPriceChange() {
    if (lastPrice == null || previousClose == null) {
      return BigDecimal.ZERO;
    }
    return lastPrice.subtract(previousClose);
  }

  /**
   * Calculate the percentage change from previous close.
   *
   * @return Percentage change ((last - previous) / previous * 100)
   */
  public BigDecimal getPercentageChange() {
    if (lastPrice == null
        || previousClose == null
        || previousClose.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    return lastPrice
        .subtract(previousClose)
        .divide(previousClose, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100));
  }

  @Override
  public String toString() {
    return String.format(
        "%s: $%.2f (bid: $%.2f, ask: $%.2f) %s%.2f%% vol: %.0f",
        symbol,
        lastPrice,
        bidPrice,
        askPrice,
        getPriceChange().compareTo(BigDecimal.ZERO) >= 0 ? "+" : "",
        getPercentageChange(),
        volume);
  }
}
