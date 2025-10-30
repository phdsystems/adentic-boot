package dev.adeengineer.adentic.boot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class is a service component (business logic layer).
 *
 * <p>This is a specialization of {@link Component} for service-layer classes.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Service
 * public class AsyncAgentOrchestrator {
 *     // Business logic
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Service {

  /**
   * Suggested bean name (optional).
   *
   * @return bean name
   */
  String value() default "";
}
