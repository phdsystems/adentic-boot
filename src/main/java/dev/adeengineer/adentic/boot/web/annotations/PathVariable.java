package dev.adeengineer.adentic.boot.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for extracting path variables from the URI.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @GetMapping("/{id}")
 * public Agent getAgent(@PathVariable String id) {
 *     return agentService.findById(id);
 * }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathVariable {

  /**
   * The name of the path variable (optional if parameter name matches).
   *
   * @return variable name
   */
  String value() default "";
}
