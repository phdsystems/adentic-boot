package dev.engineeringlab.adentic.boot.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for mapping HTTP POST requests.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @PostMapping
 * public ResponseEntity<Agent> createAgent(@RequestBody AgentRequest request) {
 *     Agent agent = agentService.create(request);
 *     return ResponseEntity.created().body(agent);
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping
public @interface PostMapping {

  /**
   * The path mapping URI (e.g., "/").
   *
   * @return path mapping
   */
  String value() default "";
}
