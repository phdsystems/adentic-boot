package dev.engineeringlab.adentic.tool.marketdata.model;

/**
 * Time frames for OHLC candlestick bars.
 *
 * <p>Represents the duration of each candlestick (e.g., 1 minute, 1 hour, 1 day).
 */
public enum TimeFrame {
  /** 1 minute. */
  M1(60, "1min", "1m"),

  /** 5 minutes. */
  M5(300, "5min", "5m"),

  /** 15 minutes. */
  M15(900, "15min", "15m"),

  /** 30 minutes. */
  M30(1800, "30min", "30m"),

  /** 1 hour. */
  H1(3600, "60min", "1h"),

  /** 4 hours. */
  H4(14400, "240min", "4h"),

  /** 1 day. */
  D1(86400, "daily", "1d"),

  /** 1 week. */
  W1(604800, "weekly", "1w"),

  /** 1 month (approximately 30 days). */
  MN1(2592000, "monthly", "1M");

  private final long seconds;
  private final String alphaVantageFormat;
  private final String standardFormat;

  TimeFrame(long seconds, String alphaVantageFormat, String standardFormat) {
    this.seconds = seconds;
    this.alphaVantageFormat = alphaVantageFormat;
    this.standardFormat = standardFormat;
  }

  /**
   * Get duration in seconds.
   *
   * @return Duration in seconds
   */
  public long getSeconds() {
    return seconds;
  }

  /**
   * Get duration in milliseconds.
   *
   * @return Duration in milliseconds
   */
  public long getMilliseconds() {
    return seconds * 1000;
  }

  /**
   * Get Alpha Vantage API format string.
   *
   * @return Alpha Vantage interval format
   */
  public String getAlphaVantageFormat() {
    return alphaVantageFormat;
  }

  /**
   * Get standard format string.
   *
   * @return Standard interval format (e.g., "1m", "1h", "1d")
   */
  public String getStandardFormat() {
    return standardFormat;
  }

  /**
   * Check if this is an intraday time frame (< 1 day).
   *
   * @return true if intraday
   */
  public boolean isIntraday() {
    return seconds < D1.seconds;
  }

  /**
   * Parse time frame from standard format string.
   *
   * @param format Standard format (e.g., "1m", "1h", "1d")
   * @return Matching TimeFrame or null
   */
  public static TimeFrame fromStandardFormat(String format) {
    for (TimeFrame tf : values()) {
      if (tf.standardFormat.equalsIgnoreCase(format)) {
        return tf;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return standardFormat;
  }
}
