package dev.engineeringlab.adentic.boot.annotations.provider;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a notification provider implementation.
 *
 * <p>Notification providers handle multi-channel notifications including push notifications, SMS,
 * webhooks, and in-app messaging.
 *
 * <p>The framework discovers all {@code @NotificationProvider} annotated classes at runtime and
 * registers them for auto-configuration and dependency injection.
 *
 * <p><b>Supported Providers:</b>
 *
 * <ul>
 *   <li><b>FCM:</b> Firebase Cloud Messaging (push notifications)
 *   <li><b>SNS:</b> Amazon Simple Notification Service
 *   <li><b>Twilio:</b> SMS and voice notifications
 *   <li><b>Slack:</b> Slack webhook notifications
 *   <li><b>Pusher:</b> Real-time push notifications
 * </ul>
 *
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NotificationProvider {

  /**
   * Unique name for this notification provider.
   *
   * <p>Examples: "fcm", "sns", "twilio", "slack", "pusher"
   *
   * @return provider name
   */
  String name();

  /**
   * Whether this provider supports push notifications.
   *
   * @return true if push is supported
   */
  boolean supportsPush() default false;

  /**
   * Whether this provider supports SMS.
   *
   * @return true if SMS is supported
   */
  boolean supportsSMS() default false;

  /**
   * Whether this provider supports webhooks.
   *
   * @return true if webhooks are supported
   */
  boolean supportsWebhooks() default false;

  /**
   * Whether this provider supports in-app messaging.
   *
   * @return true if in-app messaging is supported
   */
  boolean supportsInApp() default false;

  /**
   * Whether this provider supports delivery receipts.
   *
   * @return true if delivery receipts are supported
   */
  boolean supportsDeliveryReceipts() default false;

  /**
   * Human-readable description of this notification provider.
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
