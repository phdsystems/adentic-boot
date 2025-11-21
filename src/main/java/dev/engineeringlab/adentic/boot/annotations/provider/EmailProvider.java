package dev.engineeringlab.adentic.boot.annotations.provider;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an email provider implementation.
 *
 * <p>Email providers handle sending emails through various services (SMTP, SendGrid, SES, etc.),
 * template rendering, attachment handling, and delivery tracking.
 *
 * <p>The framework discovers all {@code @EmailProvider} annotated classes at runtime and registers
 * them for auto-configuration and dependency injection.
 *
 * <p><b>Supported Providers:</b>
 *
 * <ul>
 *   <li><b>SMTP:</b> Standard email protocol
 *   <li><b>SendGrid:</b> Cloud email delivery service
 *   <li><b>Amazon SES:</b> AWS Simple Email Service
 *   <li><b>Mailgun:</b> Email API service
 *   <li><b>Postmark:</b> Transactional email service
 * </ul>
 *
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EmailProvider {

  /**
   * Unique name for this email provider.
   *
   * <p>Examples: "smtp", "sendgrid", "ses", "mailgun", "postmark"
   *
   * @return provider name
   */
  String name();

  /**
   * Whether this provider supports HTML emails.
   *
   * @return true if HTML is supported
   */
  boolean supportsHtml() default true;

  /**
   * Whether this provider supports attachments.
   *
   * @return true if attachments are supported
   */
  boolean supportsAttachments() default true;

  /**
   * Whether this provider supports email templates.
   *
   * @return true if templates are supported
   */
  boolean supportsTemplates() default false;

  /**
   * Whether this provider supports delivery tracking.
   *
   * @return true if tracking is supported
   */
  boolean supportsTracking() default false;

  /**
   * Maximum attachment size in bytes.
   *
   * <p>Set to -1 for unlimited.
   *
   * @return maximum attachment size
   */
  long maxAttachmentSize() default 10485760; // 10MB

  /**
   * Human-readable description of this email provider.
   *
   * @return provider description
   */
  String description() default "";

  /**
   * Selection priority when multiple providers are available.
   *
   * @return selection priority
   */
  int priority() default 0;

  /**
   * Whether this provider is enabled by default.
   *
   * @return true if enabled by default
   */
  boolean enabledByDefault() default true;
}
