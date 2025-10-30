package dev.adeengineer.adentic.boot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class is a component managed by AgenticBoot.
 *
 * <p>Annotated classes are automatically discovered during component scanning and registered in the
 * AgenticContext as singleton beans.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Component
 * public class MyService {
 *     @Inject
 *     public MyService(MyRepository repository) {
 *         // Dependencies auto-injected
 *     }
 * }
 * }</pre>
 *
 * @see Service
 * @see RestController
 * @see Inject
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {

  /**
   * Suggested bean name (optional).
   *
   * @return bean name, defaults to decapitalized class name
   */
  String value() default "";
}
