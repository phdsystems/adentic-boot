package dev.engineeringlab.adentic.boot.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps HTTP requests to controller methods or classes.
 *
 * <p>Can be used at class level to define base path, or at method level for specific endpoints.
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
 *         return agentService.findById(id);
 *     }
 * }
 * }</pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {

  /**
   * The path mapping URIs (e.g., "/api/v1/agents").
   *
   * @return path mapping
   */
  String value() default "";
}
