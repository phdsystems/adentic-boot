package dev.engineeringlab.adentic.boot.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for mapping HTTP GET requests.
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
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping
public @interface GetMapping {

  /**
   * The path mapping URI (e.g., "/{id}").
   *
   * @return path mapping
   */
  String value() default "";
}
