package dev.engineeringlab.adentic.boot.annotations.provider;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a workflow provider implementation.
 *
 * <p>Workflow providers handle workflow orchestration, task execution, state management, and
 * long-running processes with support for various workflow patterns.
 *
 * <p>The framework discovers all {@code @WorkflowProvider} annotated classes at runtime and
 * registers them for auto-configuration and dependency injection.
 *
 * <p><b>Supported Providers:</b>
 *
 * <ul>
 *   <li><b>Temporal:</b> Durable workflow execution engine
 *   <li><b>Camunda:</b> BPMN workflow engine
 *   <li><b>Airflow:</b> Task scheduling and orchestration
 *   <li><b>Step Functions:</b> AWS serverless workflows
 *   <li><b>Simple:</b> In-memory workflow execution
 * </ul>
 *
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WorkflowProvider {

  /**
   * Unique name for this workflow provider.
   *
   * <p>Examples: "temporal", "camunda", "airflow", "step-functions", "simple"
   *
   * @return provider name
   */
  String name();

  /**
   * Whether this provider supports long-running workflows.
   *
   * @return true if long-running workflows are supported
   */
  boolean supportsLongRunning() default true;

  /**
   * Whether this provider supports workflow versioning.
   *
   * @return true if versioning is supported
   */
  boolean supportsVersioning() default false;

  /**
   * Whether this provider supports parallel execution.
   *
   * @return true if parallel execution is supported
   */
  boolean supportsParallelExecution() default true;

  /**
   * Whether this provider supports conditional branching.
   *
   * @return true if conditional branching is supported
   */
  boolean supportsConditionalBranching() default true;

  /**
   * Whether this provider supports error handling and retries.
   *
   * @return true if error handling is supported
   */
  boolean supportsErrorHandling() default true;

  /**
   * Whether this provider supports workflow state persistence.
   *
   * @return true if state persistence is supported
   */
  boolean supportsStatePersistence() default false;

  /**
   * Human-readable description of this workflow provider.
   *
   * @return provider description
   */
  String description() default "";

  /**
   * Selection priority when multiple providers are available.
   *
   * @return selection priority
   */
  int priority() default 0;

  /**
   * Whether this provider is enabled by default.
   *
   * @return true if enabled by default
   */
  boolean enabledByDefault() default true;
}
