package dev.adeengineer.adentic.boot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a constructor, field, or method for dependency injection.
 *
 * <p>AgenticBoot prefers constructor injection for required dependencies:
 *
 * <pre>{@code
 * @Component
 * public class MyService {
 *     private final MyRepository repository;
 *
 *     @Inject
 *     public MyService(MyRepository repository) {
 *         this.repository = repository;
 *     }
 * }
 * }</pre>
 *
 * <p>Field injection is also supported but not recommended:
 *
 * <pre>{@code
 * @Component
 * public class MyService {
 *     @Inject private MyRepository repository;
 * }
 * }</pre>
 */
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
  // Marker annotation - no attributes needed
}
