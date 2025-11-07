package examples.infrastructure.integration;

import dev.adeengineer.adentic.boot.AgenticApplication;
import dev.adeengineer.adentic.boot.annotations.AgenticBootApplication;
import dev.adeengineer.adentic.boot.annotations.GetMapping;
import dev.adeengineer.adentic.boot.annotations.PostMapping;
import dev.adeengineer.adentic.boot.annotations.RequestBody;
import dev.adeengineer.adentic.boot.annotations.RequestParam;
import dev.adeengineer.adentic.boot.annotations.RestController;
import dev.adeengineer.adentic.boot.registry.ProviderRegistry;
import dev.adeengineer.adentic.provider.orchestration.SimpleOrchestrationProvider;
import dev.adeengineer.adentic.provider.queue.InMemoryTaskQueueProvider;
import dev.adeengineer.orchestration.model.WorkflowDefinition;
import dev.adeengineer.orchestration.model.WorkflowExecution;
import dev.adeengineer.orchestration.model.WorkflowStep;
import dev.adeengineer.queue.model.QueueStats;
import dev.adeengineer.queue.model.Task;
import dev.adeengineer.queue.model.TaskResult;
import dev.adeengineer.queue.model.TaskStatus;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Example application demonstrating infrastructure provider integration with AgenticBoot.
 *
 * <p>This example shows:
 *
 * <ul>
 *   <li>Task queue operations (enqueue, dequeue, status)
 *   <li>Workflow orchestration (define, execute, monitor)
 *   <li>Auto-registration of infrastructure providers
 *   <li>Reactive operations with Project Reactor
 * </ul>
 *
 * <h2>Run</h2>
 *
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="examples.infrastructure.integration.InfrastructureExample"
 * }</pre>
 *
 * <h2>Test Endpoints</h2>
 *
 * <pre>{@code
 * # Task Queue Operations
 * curl -X POST "http://localhost:8080/api/queue/enqueue?queue=default" \
 *   -H "Content-Type: application/json" \
 *   -d '{"type":"email","priority":5,"payload":{"to":"user@example.com"}}'
 *
 * curl "http://localhost:8080/api/queue/dequeue?queue=default"
 * curl "http://localhost:8080/api/queue/stats?queue=default"
 *
 * # Workflow Operations
 * curl -X POST "http://localhost:8080/api/workflow/execute?workflowId=demo-workflow" \
 *   -H "Content-Type: application/json" \
 *   -d '{"input":"test data"}'
 *
 * curl "http://localhost:8080/api/workflow/status?executionId=<execution-id>"
 * }</pre>
 */
@AgenticBootApplication(port = 8080, scanBasePackages = "examples.infrastructure.integration")
public class InfrastructureExample {

  public static void main(String[] args) {
    AgenticApplication.run(InfrastructureExample.class, args);
  }

  /** REST controller for task queue operations. */
  @Slf4j
  @RestController
  public static class TaskQueueController {

    @Inject private ProviderRegistry registry;

    /**
     * Enqueue a task.
     *
     * <p>Example: {@code curl -X POST
     * "http://localhost:8080/api/queue/enqueue?queue=default" -H "Content-Type: application/json"
     * -d '{"type":"email","priority":5,"payload":{"to":"user@example.com"}}'}
     *
     * @param queueName queue name
     * @param task task to enqueue
     * @return enqueued task
     */
    @PostMapping("/api/queue/enqueue")
    public Mono<Task> enqueueTask(
        @RequestParam("queue") String queueName, @RequestBody Task task) {
      log.info("Enqueue task to queue: {}", queueName);

      return getQueueProvider()
          .flatMap(
              provider -> {
                try {
                  return provider.enqueue(queueName, task);
                } catch (Exception e) {
                  return Mono.error(e);
                }
              })
          .doOnSuccess(t -> log.info("Task enqueued: {}", t.id()))
          .doOnError(e -> log.error("Failed to enqueue task", e));
    }

    /**
     * Dequeue a task.
     *
     * <p>Example: {@code curl "http://localhost:8080/api/queue/dequeue?queue=default"}
     *
     * @param queueName queue name
     * @return dequeued task or empty if no tasks
     */
    @GetMapping("/api/queue/dequeue")
    public Mono<Task> dequeueTask(@RequestParam("queue") String queueName) {
      log.info("Dequeue task from queue: {}", queueName);

      return getQueueProvider()
          .flatMap(
              provider -> {
                try {
                  return provider.dequeue(queueName);
                } catch (Exception e) {
                  return Mono.error(e);
                }
              })
          .doOnSuccess(t -> log.info("Task dequeued: {}", t != null ? t.id() : "none"))
          .doOnError(e -> log.error("Failed to dequeue task", e));
    }

    /**
     * Get queue statistics.
     *
     * <p>Example: {@code curl "http://localhost:8080/api/queue/stats?queue=default"}
     *
     * @param queueName queue name
     * @return queue statistics
     */
    @GetMapping("/api/queue/stats")
    public Mono<QueueStats> getQueueStats(@RequestParam("queue") String queueName) {
      log.info("Get stats for queue: {}", queueName);

      return getQueueProvider()
          .flatMap(
              provider -> {
                try {
                  return provider.getQueueStats(queueName);
                } catch (Exception e) {
                  return Mono.error(e);
                }
              })
          .doOnSuccess(stats -> log.info("Queue stats: {} pending tasks", stats.pendingCount()))
          .doOnError(e -> log.error("Failed to get queue stats", e));
    }

    /**
     * Get task queue provider from registry.
     *
     * @return task queue provider
     */
    private Mono<InMemoryTaskQueueProvider> getQueueProvider() {
      return Mono.fromCallable(
          () ->
              registry
                  .<InMemoryTaskQueueProvider>getProvider("queue", "in-memory")
                  .orElseThrow(
                      () -> new IllegalStateException("Task queue provider not available")));
    }
  }

  /** REST controller for workflow orchestration operations. */
  @Slf4j
  @RestController
  public static class WorkflowController {

    @Inject private ProviderRegistry registry;

    /**
     * Register a demo workflow on startup.
     *
     * <p>This is called automatically when the controller is created.
     */
    public WorkflowController() {
      // Constructor-based initialization will run after injection
    }

    /**
     * Initialize demo workflow (called after dependency injection).
     */
    private void initializeDemoWorkflow() {
      getOrchestrationProvider()
          .flatMap(
              provider -> {
                // Create demo workflow
                WorkflowDefinition workflow =
                    new WorkflowDefinition(
                        "demo-workflow",
                        "Demo Workflow",
                        "Example workflow for testing",
                        List.of(
                            new WorkflowStep(
                                "step1",
                                "validate-input",
                                "simple-agent",
                                Map.of("validation_rules", List.of("not_empty")),
                                Map.of()),
                            new WorkflowStep(
                                "step2",
                                "process-data",
                                "simple-agent",
                                Map.of("operation", "transform"),
                                Map.of()),
                            new WorkflowStep(
                                "step3",
                                "save-result",
                                "simple-agent",
                                Map.of("destination", "database"),
                                Map.of())),
                        Map.of("version", "1.0"));

                return provider.registerWorkflow(workflow);
              })
          .subscribe(
              wf -> log.info("Registered demo workflow: {}", wf.id()),
              error -> log.error("Failed to register demo workflow", error));
    }

    /**
     * Execute a workflow.
     *
     * <p>Example: {@code curl -X POST
     * "http://localhost:8080/api/workflow/execute?workflowId=demo-workflow" -H "Content-Type:
     * application/json" -d '{"input":"test data"}'}
     *
     * @param workflowId workflow ID
     * @param input workflow input
     * @return workflow execution
     */
    @PostMapping("/api/workflow/execute")
    public Mono<WorkflowExecution> executeWorkflow(
        @RequestParam("workflowId") String workflowId, @RequestBody Map<String, Object> input) {
      log.info("Execute workflow: {}", workflowId);

      // Initialize demo workflow if not already done
      initializeDemoWorkflow();

      return getOrchestrationProvider()
          .flatMap(provider -> provider.executeWorkflow(workflowId, input))
          .doOnSuccess(exec -> log.info("Workflow execution started: {}", exec.executionId()))
          .doOnError(e -> log.error("Failed to execute workflow", e));
    }

    /**
     * Get workflow execution status.
     *
     * <p>Example: {@code curl
     * "http://localhost:8080/api/workflow/status?executionId=<execution-id>"}
     *
     * @param executionId execution ID
     * @return workflow execution status
     */
    @GetMapping("/api/workflow/status")
    public Mono<WorkflowExecution> getExecutionStatus(@RequestParam("executionId") String executionId) {
      log.info("Get workflow execution status: {}", executionId);

      return getOrchestrationProvider()
          .flatMap(provider -> provider.getExecutionStatus(executionId))
          .doOnSuccess(exec -> log.info("Workflow status: {}", exec.status()))
          .doOnError(e -> log.error("Failed to get workflow status", e));
    }

    /**
     * Cancel a workflow execution.
     *
     * <p>Example: {@code curl -X POST
     * "http://localhost:8080/api/workflow/cancel?executionId=<execution-id>"}
     *
     * @param executionId execution ID
     * @return true if cancelled
     */
    @PostMapping("/api/workflow/cancel")
    public Mono<Boolean> cancelExecution(@RequestParam("executionId") String executionId) {
      log.info("Cancel workflow execution: {}", executionId);

      return getOrchestrationProvider()
          .flatMap(provider -> provider.cancelExecution(executionId))
          .doOnSuccess(cancelled -> log.info("Workflow cancelled: {}", cancelled))
          .doOnError(e -> log.error("Failed to cancel workflow", e));
    }

    /**
     * Get orchestration provider from registry.
     *
     * @return orchestration provider
     */
    private Mono<SimpleOrchestrationProvider> getOrchestrationProvider() {
      return Mono.fromCallable(
          () ->
              registry
                  .<SimpleOrchestrationProvider>getProvider("orchestration", "simple")
                  .orElseThrow(
                      () -> new IllegalStateException("Orchestration provider not available")));
    }
  }
}
