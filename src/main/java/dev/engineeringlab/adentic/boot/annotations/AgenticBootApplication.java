package dev.engineeringlab.adentic.boot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Main annotation for AgenticBoot applications.
 *
 * <p>Marks the main application class and triggers component scanning, context creation, and HTTP
 * server startup.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @AgenticBootApplication
 * public class SoftwareEngineerApplication {
 *     public static void main(String[] args) {
 *         AgenticApplication.run(SoftwareEngineerApplication.class, args);
 *     }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface AgenticBootApplication {

  /**
   * Base packages to scan for components.
   *
   * <p>If empty, scans the package of the annotated class and all subpackages.
   *
   * @return base packages for component scanning
   */
  String[] scanBasePackages() default {};

  /**
   * HTTP server port.
   *
   * @return server port (default 8080)
   */
  int port() default 8080;

  /**
   * Enable built-in metrics endpoint (/metrics).
   *
   * @return true to enable metrics
   */
  boolean enableMetrics() default true;

  /**
   * Enable built-in health check endpoint (/health).
   *
   * @return true to enable health checks
   */
  boolean enableHealthCheck() default true;
}
