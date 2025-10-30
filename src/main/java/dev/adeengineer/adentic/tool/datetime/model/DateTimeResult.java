package dev.adeengineer.adentic.tool.datetime.model;

import java.time.Instant;
import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Result of a datetime operation.
 *
 * @since 0.3.0
 */
@Data
@Builder
public class DateTimeResult {

  /** Operation performed. */
  private DateTimeOperation operation;

  /** Result as string. */
  @Builder.Default private String result = null;

  /** Result as ZonedDateTime. */
  @Builder.Default private ZonedDateTime zonedDateTime = null;

  /** Result as Instant. */
  @Builder.Default private Instant instant = null;

  /** Result as epoch milliseconds. */
  @Builder.Default private Long epochMillis = null;

  /** Whether the operation was successful. */
  private boolean success;

  /** Error message if operation failed. */
  @Builder.Default private String errorMessage = null;

  /** Additional metadata. */
  @Builder.Default private String metadata = null;

  /**
   * Creates a successful result with string output.
   *
   * @param operation operation performed
   * @param result result string
   * @return DateTimeResult
   */
  public static DateTimeResult success(DateTimeOperation operation, String result) {
    return DateTimeResult.builder().operation(operation).success(true).result(result).build();
  }

  /**
   * Creates a successful result with ZonedDateTime.
   *
   * @param operation operation performed
   * @param zonedDateTime result datetime
   * @return DateTimeResult
   */
  public static DateTimeResult success(DateTimeOperation operation, ZonedDateTime zonedDateTime) {
    return DateTimeResult.builder()
        .operation(operation)
        .success(true)
        .zonedDateTime(zonedDateTime)
        .result(zonedDateTime.toString())
        .build();
  }

  /**
   * Creates a failed result.
   *
   * @param operation operation attempted
   * @param error error message
   * @return DateTimeResult
   */
  public static DateTimeResult error(DateTimeOperation operation, String error) {
    return DateTimeResult.builder().operation(operation).success(false).errorMessage(error).build();
  }
}
