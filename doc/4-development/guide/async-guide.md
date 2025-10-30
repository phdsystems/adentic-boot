# ADE Agent Async

Asynchronous execution support for ADE Agent using CompletableFuture.

**Example Code:** [AsyncAgentExample.java](../../examples/src/main/java/com/phdsystems/agent/examples/AsyncAgentExample.java)

## Overview

This module provides non-blocking task execution for agents, enabling:
- Concurrent agent execution
- Composable async workflows
- Custom thread pool configuration
- Integration with reactive frameworks

## Installation

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-async</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

### Basic Async Execution

```java
import com.phdsystems.agent.Agent;
import com.phdsystems.agent.async.AsyncAgent;
import com.phdsystems.agent.async.AsyncAgents;

// Wrap any synchronous agent
Agent syncAgent = new MyAgent();
AsyncAgent asyncAgent = AsyncAgents.fromAgent(syncAgent);

// Execute asynchronously
CompletableFuture<TaskResult> future = asyncAgent.executeTaskAsync(request);

// Handle result
future.thenAccept(result -> {
    if (result.success()) {
        System.out.println("Result: " + result.output());
    }
});
```

### Parallel Execution

```java
List<AsyncAgent> agents = List.of(agent1, agent2, agent3);
TaskRequest request = TaskRequest.of("Task", "analyze data");

// Execute all agents in parallel
CompletableFuture<List<TaskResult>> allResults = CompletableFuture.allOf(
    agents.stream()
        .map(agent -> agent.executeTaskAsync(request))
        .toArray(CompletableFuture[]::new)
).thenApply(v ->
    agents.stream()
        .map(agent -> agent.executeTaskAsync(request).join())
        .toList()
);
```

### Custom Executor

```java
Executor customExecutor = Executors.newFixedThreadPool(10);
AsyncAgent asyncAgent = AsyncAgents.fromAgent(syncAgent, customExecutor);

CompletableFuture<TaskResult> future = asyncAgent.executeTaskAsync(request);
```

## Features

- **Zero overhead** - Thin wrapper over synchronous agents
- **CompletableFuture API** - Full Java async capabilities
- **Custom executors** - Control thread pools and scheduling
- **Backward compatible** - AsyncAgent extends Agent interface

## API Reference

### AsyncAgent Interface

```java
public interface AsyncAgent extends Agent {
    CompletableFuture<TaskResult> executeTaskAsync(TaskRequest request);
    CompletableFuture<TaskResult> executeTaskAsync(TaskRequest request, Executor executor);
}
```

### AsyncAgents Utility

```java
public final class AsyncAgents {
    static AsyncAgent fromAgent(Agent agent);
    static AsyncAgent fromAgent(Agent agent, Executor executor);
}
```

## Examples

See `examples/` directory for complete examples:
- Parallel agent execution
- Sequential chaining
- Error handling
- Timeout configuration

## Future Enhancements

- Circuit breaker support
- Retry policies
- Rate limiting
- Metrics collection

## License

Apache License 2.0
