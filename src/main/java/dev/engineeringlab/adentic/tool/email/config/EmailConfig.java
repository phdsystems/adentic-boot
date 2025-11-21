package dev.engineeringlab.adentic.tool.email.config;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration for Email Tool.
 *
 * @since 0.3.0
 */
@Data
@Builder
public class EmailConfig {

  /** SMTP provider type. */
  @Builder.Default private EmailProvider provider = EmailProvider.SMTP;

  /** SMTP server host. */
  @Builder.Default private String smtpHost = "smtp.gmail.com";

  /** SMTP server port. */
  @Builder.Default private int smtpPort = 587;

  /** SMTP username. */
  @Builder.Default private String username = null;

  /** SMTP password. */
  @Builder.Default private String password = null;

  /** Whether to use TLS. */
  @Builder.Default private boolean useTls = true;

  /** Whether to use SSL. */
  @Builder.Default private boolean useSsl = false;

  /** Connection timeout in milliseconds. */
  @Builder.Default private int timeoutMs = 30000;

  /** API key for SendGrid/Mailgun. */
  @Builder.Default private String apiKey = null;

  /** API domain for Mailgun. */
  @Builder.Default private String apiDomain = null;

  /** Whether to enable debug logging. */
  @Builder.Default private boolean debug = false;

  /** Email provider types. */
  public enum EmailProvider {
    SMTP, // Direct SMTP
    SENDGRID, // SendGrid API
    MAILGUN // Mailgun API
  }

  /**
   * Creates default Gmail SMTP configuration.
   *
   * @param username Gmail username
   * @param password Gmail app password
   * @return EmailConfig
   */
  public static EmailConfig gmail(String username, String password) {
    return EmailConfig.builder()
        .provider(EmailProvider.SMTP)
        .smtpHost("smtp.gmail.com")
        .smtpPort(587)
        .username(username)
        .password(password)
        .useTls(true)
        .build();
  }

  /**
   * Creates SendGrid configuration.
   *
   * @param apiKey SendGrid API key
   * @return EmailConfig
   */
  public static EmailConfig sendgrid(String apiKey) {
    return EmailConfig.builder().provider(EmailProvider.SENDGRID).apiKey(apiKey).build();
  }

  /**
   * Creates Mailgun configuration.
   *
   * @param apiKey Mailgun API key
   * @param domain Mailgun domain
   * @return EmailConfig
   */
  public static EmailConfig mailgun(String apiKey, String domain) {
    return EmailConfig.builder()
        .provider(EmailProvider.MAILGUN)
        .apiKey(apiKey)
        .apiDomain(domain)
        .build();
  }
}
