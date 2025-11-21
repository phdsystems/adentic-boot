package dev.engineeringlab.adentic.boot.annotations.provider;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a source control management provider implementation.
 *
 * <p>SCM providers handle interactions with source control platforms including repository
 * management, pull requests, issues, and CI/CD integration.
 *
 * <p>The framework discovers all {@code @SCMProvider} annotated classes at runtime and registers
 * them for auto-configuration and dependency injection.
 *
 * <p><b>Supported Providers:</b>
 *
 * <ul>
 *   <li><b>GitHub:</b> GitHub API integration
 *   <li><b>GitLab:</b> GitLab API integration
 *   <li><b>Bitbucket:</b> Bitbucket API integration
 *   <li><b>Azure DevOps:</b> Azure Repos integration
 *   <li><b>Gitea:</b> Self-hosted Git service
 * </ul>
 *
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SCMProvider {

  /**
   * Unique name for this SCM provider.
   *
   * <p>Examples: "github", "gitlab", "bitbucket", "azure-devops", "gitea"
   *
   * @return provider name
   */
  String name();

  /**
   * Whether this provider supports pull request operations.
   *
   * @return true if pull requests are supported
   */
  boolean supportsPullRequests() default true;

  /**
   * Whether this provider supports issue tracking.
   *
   * @return true if issues are supported
   */
  boolean supportsIssues() default true;

  /**
   * Whether this provider supports webhooks.
   *
   * @return true if webhooks are supported
   */
  boolean supportsWebhooks() default true;

  /**
   * Whether this provider supports CI/CD integration.
   *
   * @return true if CI/CD is supported
   */
  boolean supportsCICD() default false;

  /**
   * Whether this provider supports code review.
   *
   * @return true if code review is supported
   */
  boolean supportsCodeReview() default true;

  /**
   * Human-readable description of this SCM provider.
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
