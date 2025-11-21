package dev.engineeringlab.adentic.tool.marketdata.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a single OHLC (Open, High, Low, Close) candlestick bar.
 *
 * <p>Contains price and volume data for a specific time period.
 */
@Data
@Builder(toBuilder = true)
@SuppressWarnings("unused")
public class OHLCBar {
  /** Timestamp of the bar (opening time). */
  private Instant timestamp;

  /** Opening price. */
  private BigDecimal open;

  /** Highest price during the period. */
  private BigDecimal high;

  /** Lowest price during the period. */
  private BigDecimal low;

  /** Closing price. */
  private BigDecimal close;

  /** Trading volume. */
  private BigDecimal volume;

  /** Trading symbol. */
  private String symbol;

  /** Time frame of this bar. */
  private TimeFrame timeFrame;

  /**
   * Calculate the price change for this bar.
   *
   * @return Price change (close - open)
   */
  public BigDecimal getPriceChange() {
    return close.subtract(open);
  }

  /**
   * Calculate the percentage change for this bar.
   *
   * @return Percentage change ((close - open) / open * 100)
   */
  public BigDecimal getPercentageChange() {
    if (open.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    return close
        .subtract(open)
        .divide(open, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100));
  }

  /**
   * Check if this is a bullish (green) candle.
   *
   * @return true if close > open
   */
  public boolean isBullish() {
    return close.compareTo(open) > 0;
  }

  /**
   * Check if this is a bearish (red) candle.
   *
   * @return true if close < open
   */
  public boolean isBearish() {
    return close.compareTo(open) < 0;
  }

  /**
   * Check if this is a doji candle (open ≈ close).
   *
   * @return true if absolute change is less than 0.1%
   */
  public boolean isDoji() {
    return getPercentageChange().abs().compareTo(new BigDecimal("0.1")) < 0;
  }

  @Override
  public String toString() {
    return String.format(
        "%s [%s] O:%.2f H:%.2f L:%.2f C:%.2f V:%.0f (%s%.2f%%)",
        symbol,
        timestamp,
        open,
        high,
        low,
        close,
        volume,
        isBullish() ? "+" : "",
        getPercentageChange());
  }
}
