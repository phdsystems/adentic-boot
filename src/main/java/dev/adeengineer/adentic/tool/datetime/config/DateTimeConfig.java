package dev.adeengineer.adentic.tool.datetime.config;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.Data;

/**
 * Configuration for DateTime Tool.
 *
 * @since 0.3.0
 */
@Data
@Builder
public class DateTimeConfig {

  /** Default timezone. */
  @Builder.Default private ZoneId defaultTimezone = ZoneId.systemDefault();

  /** Default date format. */
  @Builder.Default private String defaultDateFormat = "yyyy-MM-dd";

  /** Default datetime format. */
  @Builder.Default private String defaultDateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss";

  /** Default time format. */
  @Builder.Default private String defaultTimeFormat = "HH:mm:ss";

  /** Whether to use UTC as default for conversions. */
  @Builder.Default private boolean useUtcDefault = false;

  /** Whether to include timezone in formatted output. */
  @Builder.Default private boolean includeTimezone = true;

  /**
   * Creates default datetime configuration.
   *
   * @return default config
   */
  public static DateTimeConfig defaults() {
    return DateTimeConfig.builder().build();
  }

  /**
   * Creates UTC-based configuration.
   *
   * @return UTC config
   */
  public static DateTimeConfig utc() {
    return DateTimeConfig.builder().defaultTimezone(ZoneId.of("UTC")).useUtcDefault(true).build();
  }

  /**
   * Creates ISO 8601 configuration.
   *
   * @return ISO 8601 config
   */
  public static DateTimeConfig iso8601() {
    return DateTimeConfig.builder()
        .defaultDateTimeFormat(DateTimeFormatter.ISO_DATE_TIME.toString())
        .includeTimezone(true)
        .build();
  }
}
