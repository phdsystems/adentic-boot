# Component Specifications - ADE Agent SDK

**Project:** ADE Agent SDK
**Date:** 2025-10-19
**Version:** 1.0
**Author:** PHD Systems Development Team

---

## TL;DR

**Key concept**: Each SDK module has a specific purpose, clear interface, and minimal dependencies. **Architecture**: Core provides base contracts → Extensions add specialized capabilities → Integration modules expose protocols. **Module design**: Single responsibility, optional dependencies, composition-friendly.

**Quick rules**: One module = one feature. Core has zero dependencies. Extensions depend only on core. Protocol modules bridge SDK to external systems.

**Quick decision**: Need async? → ade-async. Need composition? → ade-composition. Need monitoring? → ade-monitoring. Each module is independent.

---

## Table of Contents

- [1. Core Module](#1-core-module)
- [2. Extension Modules](#2-extension-modules)
- [3. Integration Modules](#3-integration-modules)
- [4. Module Specifications](#4-module-specifications)
- [5. Cross-Cutting Concerns](#5-cross-cutting-concerns)
- [6. Module Evolution](#6-module-evolution)

---

## 1. Core Module

### 1.1 ade-agent (Core SDK)

**Purpose**: Define pure agent abstraction with zero dependencies.

**Maven Coordinates**:

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-agent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**Package**: `com.phdsystems.agent`

**Dependencies**: None (Java 21+ only)

**Components**:

|   Component   |   Type    |       Purpose       |
|---------------|-----------|---------------------|
| `Agent`       | Interface | Core agent contract |
| `TaskRequest` | Record    | Task input model    |
| `TaskResult`  | Record    | Task output model   |
| `AgentInfo`   | Record    | Agent metadata      |
| `AgentConfig` | Record    | Agent configuration |

**Interface: Agent**

```java
package com.phdsystems.agent;

import java.util.List;

/**
 * Core agent interface.
 *
 * <p>All agents must implement this interface. Agents are stateless
 * and thread-safe by default.</p>
 */
public interface Agent {

    /**
     * Get agent name.
     *
     * @return Unique agent identifier
     */
    String getName();

    /**
     * Get agent description.
     *
     * @return Human-readable description
     */
    String getDescription();

    /**
     * Get agent capabilities.
     *
     * @return List of capability tags
     */
    List<String> getCapabilities();

    /**
     * Execute task.
     *
     * @param request Task request
     * @return Task result
     */
    TaskResult executeTask(TaskRequest request);

    /**
     * Get agent metadata.
     *
     * @return Agent information
     */
    default AgentInfo getAgentInfo() {
        return AgentInfo.of(getName(), getDescription(), getCapabilities());
    }
}
```

**Record: TaskRequest**

```java
package com.phdsystems.agent;

import java.util.Map;

/**
 * Task request model.
 *
 * @param agentName Target agent name
 * @param task Task description/command
 * @param context Optional context parameters
 */
public record TaskRequest(
    String agentName,
    String task,
    Map<String, Object> context
) {
    /**
     * Create simple task request.
     *
     * @param agentName Agent name
     * @param task Task description
     * @return Task request
     */
    public static TaskRequest of(String agentName, String task) {
        return new TaskRequest(agentName, task, Map.of());
    }

    /**
     * Create task request with context.
     *
     * @param agentName Agent name
     * @param task Task description
     * @param context Context parameters
     * @return Task request
     */
    public static TaskRequest of(String agentName, String task,
                                  Map<String, Object> context) {
        return new TaskRequest(agentName, task, context);
    }
}
```

**Record: TaskResult**

```java
package com.phdsystems.agent;

import java.time.Instant;
import java.util.Map;

/**
 * Task result model.
 *
 * @param agentName Agent that executed task
 * @param task Original task
 * @param output Task output (null if failed)
 * @param metadata Additional metadata
 * @param timestamp Execution timestamp
 * @param durationMs Execution duration
 * @param success Success flag
 * @param errorMessage Error message (null if successful)
 */
public record TaskResult(
    String agentName,
    String task,
    String output,
    Map<String, Object> metadata,
    Instant timestamp,
    long durationMs,
    boolean success,
    String errorMessage
) {
    /**
     * Create successful result.
     */
    public static TaskResult success(String agentName, String task,
                                      String output, Map<String, Object> metadata,
                                      long durationMs) {
        return new TaskResult(agentName, task, output, metadata,
            Instant.now(), durationMs, true, null);
    }

    /**
     * Create failed result.
     */
    public static TaskResult failure(String agentName, String task,
                                      String errorMessage) {
        return new TaskResult(agentName, task, null, null,
            Instant.now(), 0, false, errorMessage);
    }
}
```

**Size**: ~10KB JAR
**Performance**: < 0.1ms per operation
**Coverage**: 100%

---

## 2. Extension Modules

### 2.1 ade-async

**Purpose**: Asynchronous agent execution with CompletableFuture.

**Maven Coordinates**:

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-async</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**Package**: `com.phdsystems.agent.async`

**Dependencies**: ade-agent

**Components**:

|      Component       |   Type    |           Purpose            |
|----------------------|-----------|------------------------------|
| `AsyncAgent`         | Interface | Async execution contract     |
| `AsyncAgents`        | Factory   | Factory methods              |
| `AsyncAgentExecutor` | Class     | Async wrapper implementation |

**Interface: AsyncAgent**

```java
package com.phdsystems.agent.async;

import com.phdsystems.agent.Agent;
import com.phdsystems.agent.TaskRequest;
import com.phdsystems.agent.TaskResult;

import java.util.concurrent.CompletableFuture;

/**
 * Agent with asynchronous execution support.
 */
public interface AsyncAgent extends Agent {

    /**
     * Execute task asynchronously.
     *
     * @param request Task request
     * @return Future of task result
     */
    CompletableFuture<TaskResult> executeTaskAsync(TaskRequest request);
}
```

**Factory: AsyncAgents**

```java
package com.phdsystems.agent.async;

import com.phdsystems.agent.Agent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * Factory for creating async agents.
 */
public final class AsyncAgents {

    private AsyncAgents() {
        // Utility class
    }

    /**
     * Create async agent with default executor.
     *
     * @param agent Delegate agent
     * @return Async agent
     */
    public static AsyncAgent fromAgent(Agent agent) {
        return fromAgent(agent, ForkJoinPool.commonPool());
    }

    /**
     * Create async agent with custom executor.
     *
     * @param agent Delegate agent
     * @param executor Executor service
     * @return Async agent
     */
    public static AsyncAgent fromAgent(Agent agent, ExecutorService executor) {
        return new AsyncAgentExecutor(agent, executor);
    }
}
```

**Usage Example**:

```java
Agent agent = new MyAgent();
AsyncAgent asyncAgent = AsyncAgents.fromAgent(agent);

CompletableFuture<TaskResult> future =
    asyncAgent.executeTaskAsync(TaskRequest.of("MyAgent", "process data"));

future.thenAccept(result -> {
    System.out.println("Result: " + result.output());
});
```

**Size**: ~15KB JAR
**Performance**: < 5ms overhead
**Coverage**: 95%+

---

### 2.2 ade-composition

**Purpose**: Agent composition patterns (sequential, parallel).

**Maven Coordinates**:

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-composition</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**Package**: `com.phdsystems.agent.composition`

**Dependencies**: ade-agent

**Components**:

|     Component      | Type  |        Purpose        |
|--------------------|-------|-----------------------|
| `SequentialAgent`  | Class | Sequential execution  |
| `ParallelAgent`    | Class | Parallel execution    |
| `ConditionalAgent` | Class | Conditional branching |

**Class: SequentialAgent**

```java
package com.phdsystems.agent.composition;

import com.phdsystems.agent.Agent;
import com.phdsystems.agent.TaskRequest;
import com.phdsystems.agent.TaskResult;

import java.util.List;

/**
 * Agent that executes agents sequentially, passing output to next agent.
 */
public final class SequentialAgent implements Agent {

    private final String name;
    private final List<Agent> agents;

    private SequentialAgent(String name, List<Agent> agents) {
        this.name = name;
        this.agents = List.copyOf(agents);
    }

    /**
     * Create sequential agent.
     *
     * @param name Agent name
     * @param agents Agents to execute in order
     * @return Sequential agent
     */
    public static Agent of(String name, List<Agent> agents) {
        return new SequentialAgent(name, agents);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return "Sequential composition of " + agents.size() + " agents";
    }

    @Override
    public List<String> getCapabilities() {
        return List.of("composition", "sequential");
    }

    @Override
    public TaskResult executeTask(TaskRequest request) {
        TaskResult result = agents.get(0).executeTask(request);

        for (int i = 1; i < agents.size(); i++) {
            if (!result.success()) {
                return result; // Stop on failure
            }
            result = agents.get(i).executeTask(
                TaskRequest.of(agents.get(i).getName(), result.output())
            );
        }

        return result;
    }
}
```

**Class: ParallelAgent**

```java
package com.phdsystems.agent.composition;

import com.phdsystems.agent.Agent;
import com.phdsystems.agent.TaskRequest;
import com.phdsystems.agent.TaskResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Agent that executes multiple agents in parallel.
 */
public final class ParallelAgent implements Agent {

    private final String name;
    private final List<Agent> agents;

    private ParallelAgent(String name, List<Agent> agents) {
        this.name = name;
        this.agents = List.copyOf(agents);
    }

    /**
     * Create parallel agent.
     *
     * @param name Agent name
     * @param agents Agents to execute in parallel
     * @return Parallel agent
     */
    public static Agent of(String name, List<Agent> agents) {
        return new ParallelAgent(name, agents);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return "Parallel composition of " + agents.size() + " agents";
    }

    @Override
    public List<String> getCapabilities() {
        return List.of("composition", "parallel");
    }

    @Override
    public TaskResult executeTask(TaskRequest request) {
        long startTime = System.currentTimeMillis();

        List<CompletableFuture<TaskResult>> futures = agents.stream()
            .map(agent -> CompletableFuture.supplyAsync(() ->
                agent.executeTask(request)))
            .toList();

        List<TaskResult> results = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        long duration = System.currentTimeMillis() - startTime;

        // Combine results
        String combinedOutput = results.stream()
            .map(TaskResult::output)
            .collect(Collectors.joining("\n"));

        boolean allSuccess = results.stream().allMatch(TaskResult::success);

        if (allSuccess) {
            return TaskResult.success(name, request.task(), combinedOutput,
                null, duration);
        } else {
            String errors = results.stream()
                .filter(r -> !r.success())
                .map(TaskResult::errorMessage)
                .collect(Collectors.joining("; "));
            return TaskResult.failure(name, request.task(), errors);
        }
    }
}
```

**Usage Example**:

```java
Agent agent1 = new ValidationAgent();
Agent agent2 = new TransformAgent();
Agent agent3 = new EnrichAgent();

// Sequential: agent1 -> agent2 -> agent3
Agent pipeline = SequentialAgent.of("DataPipeline",
    List.of(agent1, agent2, agent3));

// Parallel: agent1, agent2, agent3 execute concurrently
Agent parallel = ParallelAgent.of("Validators",
    List.of(agent1, agent2, agent3));
```

**Size**: ~20KB JAR
**Performance**: Sequential overhead < 1ms, Parallel overhead < 10ms
**Coverage**: 96%+

---

### 2.3 ade-streaming

**Purpose**: Progressive result streaming for long-running tasks.

**Maven Coordinates**:

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-streaming</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**Package**: `com.phdsystems.agent.streaming`

**Dependencies**: ade-agent

**Components**:

|    Component     |   Type    |            Purpose            |
|------------------|-----------|-------------------------------|
| `StreamingAgent` | Interface | Streaming execution contract  |
| `StreamBuffer`   | Class     | Buffer for collecting streams |

**Interface: StreamingAgent**

```java
package com.phdsystems.agent.streaming;

import com.phdsystems.agent.Agent;
import com.phdsystems.agent.TaskRequest;

import java.util.function.Consumer;

/**
 * Agent that produces streaming results.
 */
public interface StreamingAgent extends Agent {

    /**
     * Execute task with streaming results.
     *
     * @param request Task request
     * @param onChunk Callback for each result chunk
     * @param onError Callback for errors
     * @param onComplete Callback when streaming completes
     */
    void executeTaskStreaming(
        TaskRequest request,
        Consumer<String> onChunk,
        Consumer<Throwable> onError,
        Runnable onComplete
    );

    /**
     * Execute task with streaming results (simplified).
     *
     * @param request Task request
     * @param onChunk Callback for each result chunk
     */
    default void executeTaskStreaming(TaskRequest request,
                                       Consumer<String> onChunk) {
        executeTaskStreaming(request, onChunk, error -> {}, () -> {});
    }
}
```

**Class: StreamBuffer**

```java
package com.phdsystems.agent.streaming;

import java.util.ArrayList;
import java.util.List;

/**
 * Buffer for collecting streaming chunks.
 */
public final class StreamBuffer {

    private final List<String> chunks = new ArrayList<>();
    private Throwable error;
    private boolean completed;

    public void append(String chunk) {
        synchronized (chunks) {
            chunks.add(chunk);
        }
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public void complete() {
        this.completed = true;
    }

    public String getResult() {
        synchronized (chunks) {
            return String.join("", chunks);
        }
    }

    public List<String> getChunks() {
        synchronized (chunks) {
            return new ArrayList<>(chunks);
        }
    }

    public boolean hasError() {
        return error != null;
    }

    public Throwable getError() {
        return error;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void clear() {
        synchronized (chunks) {
            chunks.clear();
        }
        error = null;
        completed = false;
    }
}
```

**Usage Example**:

```java
StreamingAgent agent = new LLMStreamingAgent();

StreamBuffer buffer = new StreamBuffer();

agent.executeTaskStreaming(
    TaskRequest.of("LLMAgent", "Write a story"),
    buffer::append,
    buffer::setError,
    buffer::complete
);

// Or with custom handling
agent.executeTaskStreaming(
    request,
    chunk -> System.out.print(chunk),
    error -> System.err.println("Error: " + error.getMessage()),
    () -> System.out.println("\nDone!")
);
```

**Size**: ~18KB JAR
**Performance**: < 2ms per chunk
**Coverage**: 93%+

---

### 2.4 ade-monitoring

**Purpose**: Agent metrics and observability.

**Maven Coordinates**:

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-monitoring</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**Package**: `com.phdsystems.agent.monitoring`

**Dependencies**: ade-agent, Micrometer

**Components**:

|     Component      |   Type    |      Purpose       |
|--------------------|-----------|--------------------|
| `MonitoredAgent`   | Interface | Agent with metrics |
| `AgentMetrics`     | Interface | Metrics contract   |
| `MonitoringAgents` | Factory   | Factory methods    |

**Interface: MonitoredAgent**

```java
package com.phdsystems.agent.monitoring;

import com.phdsystems.agent.Agent;

/**
 * Agent with monitoring capabilities.
 */
public interface MonitoredAgent extends Agent {

    /**
     * Get agent metrics.
     *
     * @return Current metrics
     */
    AgentMetrics getMetrics();

    /**
     * Reset metrics.
     */
    void resetMetrics();
}
```

**Interface: AgentMetrics**

```java
package com.phdsystems.agent.monitoring;

import java.time.Instant;

/**
 * Agent execution metrics.
 */
public interface AgentMetrics {

    /**
     * Get total task count.
     *
     * @return Total tasks executed
     */
    long getTotalTasks();

    /**
     * Get successful task count.
     *
     * @return Successful tasks
     */
    long getSuccessfulTasks();

    /**
     * Get failed task count.
     *
     * @return Failed tasks
     */
    long getFailedTasks();

    /**
     * Get average execution time.
     *
     * @return Average duration in milliseconds
     */
    double getAverageExecutionTimeMs();

    /**
     * Get minimum execution time.
     *
     * @return Min duration in milliseconds
     */
    long getMinExecutionTimeMs();

    /**
     * Get maximum execution time.
     *
     * @return Max duration in milliseconds
     */
    long getMaxExecutionTimeMs();

    /**
     * Get last execution timestamp.
     *
     * @return Last execution time
     */
    Instant getLastExecutionTime();

    /**
     * Get success rate.
     *
     * @return Success rate (0.0 to 1.0)
     */
    default double getSuccessRate() {
        long total = getTotalTasks();
        return total > 0 ? (double) getSuccessfulTasks() / total : 0.0;
    }
}
```

**Usage Example**:

```java
Agent agent = new MyAgent();
MonitoredAgent monitored = MonitoringAgents.monitored(agent);

// Execute tasks
monitored.executeTask(TaskRequest.of("MyAgent", "task1"));
monitored.executeTask(TaskRequest.of("MyAgent", "task2"));

// Get metrics
AgentMetrics metrics = monitored.getMetrics();
System.out.println("Total tasks: " + metrics.getTotalTasks());
System.out.println("Success rate: " + metrics.getSuccessRate());
System.out.println("Avg time: " + metrics.getAverageExecutionTimeMs() + "ms");
```

**Size**: ~25KB JAR
**Performance**: < 1ms overhead per task
**Coverage**: 95%+

---

### 2.5 ade-discovery

**Purpose**: Agent registration and discovery.

**Maven Coordinates**:

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-discovery</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**Package**: `com.phdsystems.agent.discovery`

**Dependencies**: ade-agent

**Components**:

|       Component        |   Type    |          Purpose          |
|------------------------|-----------|---------------------------|
| `AgentRegistry`        | Interface | Agent registration/lookup |
| `DefaultAgentRegistry` | Class     | In-memory registry        |

**Interface: AgentRegistry**

```java
package com.phdsystems.agent.discovery;

import com.phdsystems.agent.Agent;

import java.util.List;
import java.util.Optional;

/**
 * Agent registration and discovery.
 */
public interface AgentRegistry {

    /**
     * Register agent.
     *
     * @param agent Agent to register
     */
    void register(Agent agent);

    /**
     * Unregister agent.
     *
     * @param agentName Agent name
     */
    void unregister(String agentName);

    /**
     * Find agent by name.
     *
     * @param agentName Agent name
     * @return Optional agent
     */
    Optional<Agent> findByName(String agentName);

    /**
     * Find agents by capability.
     *
     * @param capability Capability tag
     * @return List of matching agents
     */
    List<Agent> findByCapability(String capability);

    /**
     * Get all registered agents.
     *
     * @return List of all agents
     */
    List<Agent> getAllAgents();

    /**
     * Get count of registered agents.
     *
     * @return Agent count
     */
    default int size() {
        return getAllAgents().size();
    }
}
```

**Usage Example**:

```java
AgentRegistry registry = new DefaultAgentRegistry();

// Register agents
registry.register(new WeatherAgent());
registry.register(new NewsAgent());
registry.register(new CalculatorAgent());

// Find by name
Optional<Agent> agent = registry.findByName("WeatherAgent");

// Find by capability
List<Agent> mathAgents = registry.findByCapability("math");

// Execute task
agent.ifPresent(a -> {
    TaskResult result = a.executeTask(TaskRequest.of("WeatherAgent", "London"));
});
```

**Size**: ~22KB JAR
**Performance**: O(1) lookup by name, O(n) by capability
**Coverage**: 98%+

---

### 2.6 ade-resilience

**Purpose**: Retry, circuit breaker, timeout patterns.

**Maven Coordinates**:

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-resilience</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**Package**: `com.phdsystems.agent.resilience`

**Dependencies**: ade-agent, Resilience4j

**Components**:

|       Component       |  Type   |           Purpose            |
|-----------------------|---------|------------------------------|
| `ResilienceAgents`    | Factory | Factory for resilient agents |
| `RetryAgent`          | Class   | Retry wrapper                |
| `CircuitBreakerAgent` | Class   | Circuit breaker wrapper      |
| `TimeoutAgent`        | Class   | Timeout wrapper              |

**Usage Example**:

```java
Agent agent = new UnreliableAgent();

// Add retry
Agent retryAgent = ResilienceAgents.withRetry(agent, 3);

// Add circuit breaker
Agent cbAgent = ResilienceAgents.withCircuitBreaker(agent);

// Add timeout
Agent timeoutAgent = ResilienceAgents.withTimeout(agent,
    Duration.ofSeconds(5));

// Combine all
Agent resilientAgent = ResilienceAgents.builder(agent)
    .withRetry(3)
    .withCircuitBreaker()
    .withTimeout(Duration.ofSeconds(5))
    .build();
```

**Size**: ~28KB JAR
**Performance**: < 5ms overhead
**Coverage**: 91%+

---

## 3. Integration Modules

### 3.1 ade-grpc

**Purpose**: gRPC protocol support for agents.

**Maven Coordinates**:

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-grpc</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**Package**: `com.phdsystems.agent.grpc`

**Dependencies**: ade-agent, gRPC-Java

**Protocol Definition** (`agent.proto`):

```protobuf
syntax = "proto3";

package com.phdsystems.agent.grpc;

service AgentService {
  rpc ExecuteTask(TaskRequest) returns (TaskResult);
  rpc StreamTask(TaskRequest) returns (stream TaskChunk);
  rpc GetAgentInfo(AgentInfoRequest) returns (AgentInfo);
  rpc ListAgents(ListAgentsRequest) returns (AgentList);
}

message TaskRequest {
  string agent_name = 1;
  string task = 2;
  map<string, string> context = 3;
}

message TaskResult {
  string agent_name = 1;
  string task = 2;
  string output = 3;
  map<string, string> metadata = 4;
  int64 timestamp_ms = 5;
  int64 duration_ms = 6;
  bool success = 7;
  string error_message = 8;
}

message TaskChunk {
  string chunk = 1;
  bool is_final = 2;
}

message AgentInfoRequest {
  string agent_name = 1;
}

message AgentInfo {
  string name = 1;
  string description = 2;
  repeated string capabilities = 3;
}

message ListAgentsRequest {
  string capability_filter = 1;
}

message AgentList {
  repeated AgentInfo agents = 1;
}
```

**Size**: ~40KB JAR
**Performance**: < 10ms overhead per RPC
**Coverage**: 88%+

---

### 3.2 ade-rest

**Purpose**: REST API support for agents.

**Maven Coordinates**:

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-rest</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**Package**: `com.phdsystems.agent.rest`

**Dependencies**: ade-agent, JAX-RS/Jersey

**REST Endpoints**:

```
POST   /api/agents/{agentName}/execute
POST   /api/agents/{agentName}/stream
GET    /api/agents/{agentName}/info
GET    /api/agents
```

**Usage Example**:

```java
@Path("/api/agents")
public class AgentResource {

    @Inject
    private AgentRegistry registry;

    @POST
    @Path("/{agentName}/execute")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeTask(
        @PathParam("agentName") String agentName,
        TaskRequest request) {

        return registry.findByName(agentName)
            .map(agent -> agent.executeTask(request))
            .map(result -> Response.ok(result).build())
            .orElse(Response.status(404).build());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<AgentInfo> listAgents() {
        return registry.getAllAgents().stream()
            .map(Agent::getAgentInfo)
            .toList();
    }
}
```

**Size**: ~35KB JAR
**Performance**: < 15ms overhead per request
**Coverage**: 90%+

---

### 3.3 ade-spring-boot-starter

**Purpose**: Spring Boot auto-configuration and integration.

**Maven Coordinates**:

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**Package**: `com.phdsystems.agent.spring`

**Dependencies**: ade-agent, Spring Boot 3.x

**Auto-Configuration**:

```java
@Configuration
@ConditionalOnClass(Agent.class)
@EnableConfigurationProperties(AgentProperties.class)
public class AgentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AgentRegistry agentRegistry() {
        return new DefaultAgentRegistry();
    }

    @Bean
    public AgentRegistrar agentRegistrar(AgentRegistry registry,
                                           List<Agent> agents) {
        return new AgentRegistrar(registry, agents);
    }
}
```

**Usage Example**:

```java
@SpringBootApplication
@EnableAgentDiscovery
public class MyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }

    @Bean
    public Agent myAgent() {
        return new MyCustomAgent();
    }
}
```

**Size**: ~20KB JAR
**Performance**: Spring startup overhead
**Coverage**: 92%+

---

## 4. Module Specifications

### 4.1 Module Template

Each module follows this structure:

```
ade-agent-{feature}/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/phdsystems/agent/{feature}/
│   │   │   ├── {Feature}Agent.java
│   │   │   ├── {Feature}*.java
│   │   │   └── package-info.java
│   │   └── resources/
│   └── test/
│       └── java/com/phdsystems/agent/{feature}/
│           └── {Feature}*Test.java
└── target/
```

### 4.2 Module Checklist

All modules must have:

- ✅ README.md with examples
- ✅ package-info.java with module documentation
- ✅ Maven POM with proper dependencies
- ✅ Unit tests (90%+ coverage)
- ✅ JavaDoc for all public APIs
- ✅ Examples in README
- ✅ Factory methods for convenience
- ✅ Zero checkstyle violations

---

## 5. Cross-Cutting Concerns

### 5.1 Error Handling

All modules follow consistent error handling:

```java
// Use TaskResult for business errors
TaskResult.failure(agentName, task, "Validation failed");

// Throw exceptions for programming errors
throw new IllegalArgumentException("Agent name cannot be null");

// Document exceptions in JavaDoc
/**
 * @throws IllegalStateException if agent is not initialized
 */
```

### 5.2 Thread Safety

All modules are thread-safe by default:

- Immutable records
- No shared mutable state
- Concurrent collections where needed
- Document thread-safety guarantees

### 5.3 Performance

Performance targets for all modules:

- Agent instantiation: < 1ms
- Method overhead: < 10ms
- Memory per instance: < 1MB
- No memory leaks

---

## 6. Module Evolution

### 6.1 Versioning Strategy

- Major version: Breaking API changes
- Minor version: New features (backward compatible)
- Patch version: Bug fixes only

### 6.2 Deprecation Policy

- Deprecated features marked with @Deprecated
- Minimum 2 minor versions before removal
- Migration guide provided
- Clear replacement documented

---

*Last Updated: 2025-10-19*
*Version: 1.0*
