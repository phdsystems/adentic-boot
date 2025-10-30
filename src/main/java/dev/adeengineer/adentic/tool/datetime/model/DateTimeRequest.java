package dev.adeengineer.adentic.tool.datetime.model;

import java.time.ZoneId;
import lombok.Builder;
import lombok.Data;

/**
 * Request for datetime operations.
 *
 * @since 0.3.0
 */
@Data
@Builder
public class DateTimeRequest {

  /** Operation type. */
  private DateTimeOperation operation;

  /** Input datetime string. */
  @Builder.Default private String dateTime = null;

  /** Input format pattern (for parsing). */
  @Builder.Default private String inputFormat = null;

  /** Output format pattern (for formatting). */
  @Builder.Default private String outputFormat = null;

  /** Source timezone. */
  @Builder.Default private ZoneId sourceTimezone = ZoneId.systemDefault();

  /** Target timezone. */
  @Builder.Default private ZoneId targetTimezone = ZoneId.systemDefault();

  /** Amount to add/subtract (for arithmetic operations). */
  @Builder.Default private Long amount = 0L;

  /** Second datetime for comparison operations. */
  @Builder.Default private String secondDateTime = null;

  /** Epoch milliseconds (for conversion operations). */
  @Builder.Default private Long epochMillis = null;

  /**
   * Creates a parse request.
   *
   * @param dateTime datetime string
   * @param format format pattern
   * @return DateTimeRequest
   */
  public static DateTimeRequest parse(String dateTime, String format) {
    return DateTimeRequest.builder()
        .operation(DateTimeOperation.PARSE)
        .dateTime(dateTime)
        .inputFormat(format)
        .build();
  }

  /**
   * Creates a format request.
   *
   * @param dateTime datetime string
   * @param format output format pattern
   * @return DateTimeRequest
   */
  public static DateTimeRequest format(String dateTime, String format) {
    return DateTimeRequest.builder()
        .operation(DateTimeOperation.FORMAT)
        .dateTime(dateTime)
        .outputFormat(format)
        .build();
  }

  /**
   * Creates a timezone conversion request.
   *
   * @param dateTime datetime string
   * @param fromZone source timezone
   * @param toZone target timezone
   * @return DateTimeRequest
   */
  public static DateTimeRequest convertTimezone(String dateTime, ZoneId fromZone, ZoneId toZone) {
    return DateTimeRequest.builder()
        .operation(DateTimeOperation.CONVERT_TIMEZONE)
        .dateTime(dateTime)
        .sourceTimezone(fromZone)
        .targetTimezone(toZone)
        .build();
  }
}
