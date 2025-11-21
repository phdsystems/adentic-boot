package dev.engineeringlab.adentic.tool.marketdata.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Result containing OHLC bars and metadata.
 *
 * <p>Returned by market data provider operations.
 */
@Data
@Builder
public class OHLCResult {
  /** Whether the request was successful. */
  private boolean success;

  /** List of OHLC bars. */
  private List<OHLCBar> bars;

  /** Number of bars returned. */
  private int count;

  /** Trading symbol. */
  private String symbol;

  /** Time frame of the bars. */
  private TimeFrame timeFrame;

  /** Data provider name. */
  private String provider;

  /** Error message if request failed. */
  private String errorMessage;

  /** Whether data was served from cache. */
  private boolean cached;

  /** Execution time in milliseconds. */
  private long executionTimeMs;

  @Override
  public String toString() {
    if (!success) {
      return String.format("❌ Failed to retrieve data: %s", errorMessage);
    }

    return String.format(
        "✅ %s %s bars: %d records from %s (cached: %s, %dms)",
        symbol, timeFrame, count, provider, cached, executionTimeMs);
  }
}
