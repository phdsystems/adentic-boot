package dev.engineeringlab.adentic.tool.email.model;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Email message specification.
 *
 * @since 0.3.0
 */
@Data
@Builder
public class EmailMessage {

  /** Sender email address. */
  private String from;

  /** Recipient email addresses. */
  private List<String> to;

  /** CC recipients. */
  @Builder.Default private List<String> cc = List.of();

  /** BCC recipients. */
  @Builder.Default private List<String> bcc = List.of();

  /** Reply-To address. */
  @Builder.Default private String replyTo = null;

  /** Email subject. */
  private String subject;

  /** Email body (plain text or HTML). */
  private String body;

  /** Whether body is HTML. */
  @Builder.Default private boolean html = false;

  /** Attachments (file path → content type). */
  @Builder.Default private Map<String, String> attachments = Map.of();

  /** Custom headers. */
  @Builder.Default private Map<String, String> headers = Map.of();

  /** Email priority (1=highest, 3=normal, 5=lowest). */
  @Builder.Default private int priority = 3;

  /**
   * Creates a simple text email.
   *
   * @param from sender address
   * @param to recipient address
   * @param subject email subject
   * @param body email body
   * @return EmailMessage
   */
  public static EmailMessage simple(String from, String to, String subject, String body) {
    return EmailMessage.builder()
        .from(from)
        .to(List.of(to))
        .subject(subject)
        .body(body)
        .html(false)
        .build();
  }

  /**
   * Creates an HTML email.
   *
   * @param from sender address
   * @param to recipient address
   * @param subject email subject
   * @param htmlBody HTML email body
   * @return EmailMessage
   */
  public static EmailMessage html(String from, String to, String subject, String htmlBody) {
    return EmailMessage.builder()
        .from(from)
        .to(List.of(to))
        .subject(subject)
        .body(htmlBody)
        .html(true)
        .build();
  }
}
