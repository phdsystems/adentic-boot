package dev.engineeringlab.adentic.boot.annotations.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a class as an evaluation provider. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EvaluationProvider {
  String name() default "";
}
