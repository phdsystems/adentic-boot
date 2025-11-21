package dev.engineeringlab.adentic.boot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class is a REST controller.
 *
 * <p>REST controllers are automatically registered as HTTP endpoints in the AgenticServer.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/v1/agents")
 * public class AgentController {
 *
 *     @GetMapping("/{id}")
 *     public Agent getAgent(@PathVariable String id) {
 *         // Returns JSON
 *     }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RestController {

  /**
   * Suggested bean name (optional).
   *
   * @return bean name
   */
  String value() default "";
}
