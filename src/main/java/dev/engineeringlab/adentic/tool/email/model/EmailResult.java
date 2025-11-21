package dev.engineeringlab.adentic.tool.email.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

/**
 * Result of an email operation.
 *
 * @since 0.3.0
 */
@Data
@Builder
public class EmailResult {

  /** Whether the email was sent successfully. */
  private boolean success;

  /** Message ID from mail server (if available). */
  @Builder.Default private String messageId = null;

  /** Error message if send failed. */
  @Builder.Default private String errorMessage = null;

  /** Timestamp of send attempt. */
  @Builder.Default private Instant timestamp = Instant.now();

  /** Number of recipients. */
  @Builder.Default private int recipientCount = 0;

  /** Server response message. */
  @Builder.Default private String serverResponse = null;

  /**
   * Creates a successful result.
   *
   * @param messageId message ID from server
   * @param recipientCount number of recipients
   * @return EmailResult
   */
  public static EmailResult success(String messageId, int recipientCount) {
    return EmailResult.builder()
        .success(true)
        .messageId(messageId)
        .recipientCount(recipientCount)
        .build();
  }

  /**
   * Creates a failed result.
   *
   * @param error error message
   * @return EmailResult
   */
  public static EmailResult error(String error) {
    return EmailResult.builder().success(false).errorMessage(error).build();
  }
}
