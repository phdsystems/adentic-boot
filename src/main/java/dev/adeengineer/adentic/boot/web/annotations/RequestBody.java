package dev.adeengineer.adentic.boot.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for binding HTTP request body to a method parameter.
 *
 * <p>The request body is automatically deserialized from JSON.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @PostMapping
 * public Agent createAgent(@RequestBody AgentRequest request) {
 *     return agentService.create(request);
 * }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestBody {
  // Marker annotation
}
