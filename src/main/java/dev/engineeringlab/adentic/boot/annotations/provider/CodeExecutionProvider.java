package dev.engineeringlab.adentic.boot.annotations.provider;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a code execution provider implementation.
 *
 * <p>Code execution providers handle running code in various languages and environments, with
 * sandboxing, resource limits, and security controls.
 *
 * <p>The framework discovers all {@code @CodeExecutionProvider} annotated classes at runtime and
 * registers them for auto-configuration and dependency injection.
 *
 * <p><b>Supported Providers:</b>
 *
 * <ul>
 *   <li><b>Docker:</b> Container-based code execution
 *   <li><b>Lambda:</b> AWS Lambda function execution
 *   <li><b>GraalVM:</b> Polyglot execution engine
 *   <li><b>JShell:</b> Java REPL execution
 *   <li><b>ProcessBuilder:</b> Native process execution
 * </ul>
 *
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CodeExecutionProvider {

  /**
   * Unique name for this code execution provider.
   *
   * <p>Examples: "docker", "lambda", "graalvm", "jshell", "process"
   *
   * @return provider name
   */
  String name();

  /**
   * Supported programming languages.
   *
   * <p>Examples: "java", "python", "javascript", "bash"
   *
   * @return array of supported language identifiers
   */
  String[] supportedLanguages() default {};

  /**
   * Whether this provider supports sandboxing.
   *
   * @return true if sandboxing is supported
   */
  boolean supportsSandboxing() default false;

  /**
   * Whether this provider supports resource limits (CPU, memory).
   *
   * @return true if resource limits are supported
   */
  boolean supportsResourceLimits() default false;

  /**
   * Whether this provider supports timeout controls.
   *
   * @return true if timeout is supported
   */
  boolean supportsTimeout() default true;

  /**
   * Default execution timeout in milliseconds.
   *
   * <p>Set to -1 for no timeout.
   *
   * @return default timeout in ms
   */
  long defaultTimeout() default 30000; // 30 seconds

  /**
   * Human-readable description of this code execution provider.
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
