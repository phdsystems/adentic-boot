# ADE Agent SDK - Examples Documentation Overview

**Last Updated:** 2025-10-20
**Version:** 1.0

---

## TL;DR

**Purpose**: Complete reference for all ADE Agent SDK extension modules and examples. **Quick access**: Find your module guide below → Copy-paste code examples → Start building. **Coverage**: 10 comprehensive guides with 3,500+ lines of documentation and working code.

---

## Quick Navigation

### By Use Case

|         I want to...          |                       Guide                       |  Time  |
|-------------------------------|---------------------------------------------------|--------|
| Execute agents asynchronously | [Async Guide](#async-guide)                       | 5 min  |
| Combine multiple agents       | [Composition Guide](#composition-guide)           | 8 min  |
| Register and discover agents  | [Discovery Guide](#discovery-guide)               | 10 min |
| Monitor agent performance     | [Monitoring Examples](#monitoring-examples-guide) | 12 min |
| Stream progressive results    | [Streaming Guide](#streaming-guide)               | 10 min |
| Persist agents to JSON        | [Serialization Guide](#serialization-guide)       | 12 min |
| Add security/auth             | [Security Guide](#security-guide)                 | 15 min |
| Build REST APIs               | [REST Guide](#rest-guide)                         | 10 min |
| Build gRPC services           | [gRPC Guide](#grpc-guide)                         | 10 min |
| Integrate with Spring Boot    | [Spring Boot Starter](#spring-boot-starter-guide) | 15 min |
| Achieve high test coverage    | [Test Coverage Design](#test-coverage-design)     | 30 min |

### By Module Type

**Execution Patterns**
- [Async Guide](#async-guide) - Asynchronous execution with CompletableFuture
- [Composition Guide](#composition-guide) - Sequential and parallel agent composition
- [Streaming Guide](#streaming-guide) - Progressive result emission

**Infrastructure**
- [Discovery Guide](#discovery-guide) - Agent registration and discovery
- [Monitoring Examples](#monitoring-examples-guide) - Metrics and observability
- [Serialization Guide](#serialization-guide) - JSON/XML persistence

**Integration**
- [REST Guide](#rest-guide) - REST API with Javalin
- [gRPC Guide](#grpc-guide) - gRPC service implementation
- [Spring Boot Starter](#spring-boot-starter-guide) - Spring Boot autoconfiguration

**Development**
- [Test Coverage Design](#test-coverage-design) - Testing strategy achieving 94% coverage

---

## Documentation Statistics

|      Category      | Guides | Total Lines | Avg Time |
|--------------------|--------|-------------|----------|
| **Execution**      | 3      | 527         | 23 min   |
| **Infrastructure** | 3      | 894         | 34 min   |
| **Integration**    | 3      | 981         | 35 min   |
| **Testing**        | 1      | 1,095       | 30 min   |
| **Total**          | 10     | 3,497       | ~2 hours |

---

## Execution Patterns

### Async Guide

**File:** [async-guide.md](async-guide.md)
**Lines:** 118
**Time to Read:** ~5 minutes
**Module:** `ade-async`

**What it covers:**
- Wrapping agents with `AsyncAgent` for async execution
- Using `CompletableFuture` for non-blocking operations
- Parallel execution of multiple agents
- Chaining async operations with `thenApply`, `thenCompose`
- ExecutorService integration
- Error handling in async context

**Use this when:**
- You need non-blocking agent execution
- Running multiple agents concurrently
- Building reactive/async applications
- Integrating with async frameworks

**Code Example:**

```java
AsyncAgent async = AsyncAgent.of(myAgent);
CompletableFuture<TaskResult> future = async.executeAsync(request);
future.thenAccept(result -> processResult(result));
```

---

### Composition Guide

**File:** [composition-guide.md](composition-guide.md)
**Lines:** 183
**Time to Read:** ~8 minutes
**Module:** `ade-composition`

**What it covers:**
- Sequential composition with `SequentialAgent`
- Parallel composition with `ParallelAgent`
- Building agent pipelines
- Data flow between composed agents
- Error handling in compositions
- Performance optimization strategies

**Use this when:**
- Building multi-step workflows
- Creating agent pipelines
- Running validation/processing stages
- Orchestrating complex agent interactions

**Code Example:**

```java
// Sequential pipeline
Agent pipeline = SequentialAgent.of(
    fetchAgent,
    validateAgent,
    transformAgent
);

// Parallel execution
Agent parallel = ParallelAgent.of(
    validator1,
    validator2,
    validator3
);
```

---

### Streaming Guide

**File:** [streaming-guide.md](streaming-guide.md)
**Lines:** 226
**Time to Read:** ~10 minutes
**Module:** `ade-streaming`

**What it covers:**
- Progressive result emission with `StreamingAgent`
- Real-time output with `StreamBuffer`
- Callbacks: `onChunk`, `onError`, `onComplete`
- Concurrent streaming from multiple agents
- Error handling in streaming
- Backpressure management

**Use this when:**
- Need progressive/incremental results
- Building real-time interfaces
- Processing large datasets
- Implementing chat/conversational agents
- Providing user feedback during long operations

**Code Example:**

```java
StreamingAgent streaming = new StreamingAgent(baseAgent);
streaming.executeStreaming(request,
    chunk -> System.out.println("Chunk: " + chunk),
    error -> handleError(error),
    () -> System.out.println("Complete!")
);
```

---

## Infrastructure

### Discovery Guide

**File:** [discovery-guide.md](discovery-guide.md)
**Lines:** 257
**Time to Read:** ~10 minutes
**Module:** `ade-discovery`

**What it covers:**
- Agent registration with `InMemoryAgentRegistry`
- Finding agents by name, capability, pattern
- `AgentDiscoveryService` for querying
- Discovery statistics
- Listing all agents
- Dynamic agent lookup

**Use this when:**
- Building agent marketplaces
- Implementing capability-based routing
- Creating dynamic agent systems
- Managing large numbers of agents
- Building plugin architectures

**Code Example:**

```java
AgentRegistry registry = new InMemoryAgentRegistry();
registry.register(myAgent);

AgentDiscoveryService discovery = new AgentDiscoveryService(registry);
List<Agent> validators = discovery.findByCapability("validation");
```

---

### Monitoring Examples Guide

**File:** [monitoring-examples-guide.md](monitoring-examples-guide.md)
**Lines:** 292
**Time to Read:** ~12 minutes
**Module:** `ade-monitoring`

**What it covers:**
- Automatic metrics with `MonitoredAgent`
- Aggregated statistics with `MetricsCollector`
- Micrometer integration (counters, timers, gauges)
- Performance tracking and comparison
- Success rate and execution time metrics
- Custom metrics creation

**Use this when:**
- Monitoring agent performance
- Tracking success/failure rates
- Comparing agent efficiency
- Integrating with Prometheus/Grafana
- Building production observability

**Code Example:**

```java
MeterRegistry registry = new SimpleMeterRegistry();
Agent monitored = new MonitoredAgent(myAgent, registry);

// Metrics automatically collected
TaskResult result = monitored.executeTask(request);

// View statistics
MetricsCollector collector = new MetricsCollector(registry);
Map<String, AgentStats> stats = collector.getAllStats();
```

**Related:**
- Main monitoring guide: [../../docs/monitoring-guide.md](../../docs/monitoring-guide.md)
- Example code: `examples/src/main/java/com/phdsystems/agent/examples/MonitoringExample.java`

---

### Serialization Guide

**File:** [serialization-guide.md](serialization-guide.md)
**Lines:** 345
**Time to Read:** ~12 minutes
**Module:** `ade-serialization`

**What it covers:**
- JSON serialization with `AgentSerializer`
- TaskRequest, TaskResult, AgentInfo serialization
- File-based persistence with `JsonFileStorage`
- Saving and loading task results
- Pretty-printing and compact formats
- Custom serialization configurations

**Use this when:**
- Persisting agent data
- Building audit trails
- Creating task history
- Implementing undo/redo
- Sharing agent configurations
- Debugging with serialized data

**Code Example:**

```java
AgentSerializer serializer = new AgentSerializer();

// Serialize TaskResult
String json = serializer.serialize(result);

// Deserialize
TaskResult loaded = serializer.deserialize(json, TaskResult.class);

// File storage
JsonFileStorage storage = new JsonFileStorage("/data/tasks");
storage.save("task-123", result);
```

---

## Integration

### REST Guide

**File:** [rest-guide.md](rest-guide.md)
**Lines:** 269
**Time to Read:** ~10 minutes
**Module:** `ade-rest`

**What it covers:**
- REST API creation with `AgentRestServer`
- HTTP client with `AgentRestClient`
- Javalin web framework integration
- Endpoint configuration and routing
- Request/response handling
- Error handling and status codes

**Use this when:**
- Building REST APIs for agents
- Creating HTTP-based agent services
- Integrating with web applications
- Building microservices
- Exposing agents over HTTP

**Code Example:**

```java
// Server
AgentRestServer server = AgentRestServer.builder()
    .port(8080)
    .agent(myAgent)
    .build();
server.start();

// Client
AgentRestClient client = AgentRestClient.builder()
    .baseUrl("http://localhost:8080")
    .build();
TaskResult result = client.executeTask("MyAgent", "task description");
```

---

### gRPC Guide

**File:** [grpc-guide.md](grpc-guide.md)
**Lines:** 220
**Time to Read:** ~10 minutes
**Module:** `ade-grpc`

**What it covers:**
- gRPC service implementation
- Protocol Buffers definitions
- `AgentGrpcServer` setup
- `AgentGrpcClient` usage
- Streaming support
- Error handling and status codes

**Use this when:**
- Building high-performance services
- Implementing microservices communication
- Need bi-directional streaming
- Polyglot environments
- Low-latency requirements

**Code Example:**

```java
// Server
AgentGrpcServer server = AgentGrpcServer.builder()
    .port(9090)
    .agent(myAgent)
    .build();
server.start();

// Client
AgentGrpcClient client = AgentGrpcClient.builder()
    .host("localhost")
    .port(9090)
    .build();
TaskResult result = client.executeTask(request);
```

---

### Spring Boot Starter Guide

**File:** [spring-boot-starter-guide.md](spring-boot-starter-guide.md)
**Lines:** 492
**Time to Read:** ~15 minutes
**Module:** `ade-spring-boot-starter`

**What it covers:**
- Spring Boot autoconfiguration
- Automatic bean registration
- `AgentRegistry` integration
- `AgentDiscoveryService` setup
- `MetricsCollector` configuration
- Application properties configuration
- REST endpoints with Spring MVC

**Use this when:**
- Building Spring Boot applications
- Need autoconfiguration
- Using Spring dependency injection
- Integrating with Spring ecosystem
- Building enterprise applications

**Code Example:**

```java
// application.yml
agent:
  sdk:
    auto-register: true
    monitoring:
      enabled: true

// Spring Boot app
@SpringBootApplication
public class MyAgentApp {
    @Autowired
    private AgentRegistry registry;

    @Autowired
    private MetricsCollector metrics;
}
```

---

## Testing & Quality

### Test Coverage Design

**File:** [test-coverage-design.md](test-coverage-design.md)
**Lines:** 1,095
**Time to Read:** ~30 minutes
**Purpose:** Testing strategy documentation

**What it covers:**
- Test coverage strategy and approach
- 3-phase testing methodology
- Implementation details for 94% coverage
- Testing patterns for all module types
- Challenges and solutions
- JUnit 5 and JaCoCo integration
- Future improvements

**Use this when:**
- Writing tests for agents
- Achieving high test coverage
- Understanding testing patterns
- Contributing to the SDK
- Learning best practices

**Coverage Achieved:**
- 94% instruction coverage
- 92% branch coverage
- 100% coverage for core agents

**Key Patterns:**
- Unit testing with JUnit 5
- Mocking and stubbing
- Async testing patterns
- Streaming test patterns
- Integration testing strategies

---

## Module Dependencies

### Core Dependencies

All extension modules depend on:
- `ade-agent` (core module) - Zero dependencies
- Java 21+
- JUnit 5 (test scope)

### External Dependencies by Module

|           Module            |          Key Dependencies          |
|-----------------------------|------------------------------------|
| **ade-async**               | None (uses Java CompletableFuture) |
| **ade-composition**         | None (pure Java)                   |
| **ade-discovery**           | None (pure Java)                   |
| **ade-streaming**           | None (pure Java)                   |
| **ade-serialization**       | Jackson (JSON)                     |
| **ade-monitoring**          | Micrometer                         |
| **ade-rest**                | Javalin                            |
| **ade-grpc**                | gRPC, Protobuf                     |
| **ade-spring-boot-starter** | Spring Boot                        |

---

## Common Patterns Across Guides

### Pattern 1: Decorator Pattern

Most extension modules use the decorator pattern:

```java
Agent original = new MyAgent();
Agent wrapped = new MonitoredAgent(original, registry);
Agent async = AsyncAgent.of(wrapped);
```

### Pattern 2: Builder Pattern

Builders for complex configuration:

```java
AgentRestServer server = AgentRestServer.builder()
    .port(8080)
    .agent(myAgent)
    .timeout(Duration.ofSeconds(30))
    .build();
```

### Pattern 3: Factory Methods

Static factories for common use cases:

```java
AsyncAgent async = AsyncAgent.of(agent);
MonitoredAgent monitored = AgentMetrics.monitored(agent, registry);
```

### Pattern 4: Callback Interfaces

Streaming and async operations use callbacks:

```java
streaming.executeStreaming(request,
    chunk -> processChunk(chunk),
    error -> handleError(error),
    () -> onComplete()
);
```

---

## Quick Reference

### Installation

**All modules:**

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-agent-[module-name]</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**Example modules:**
- `ade-async`
- `ade-composition`
- `ade-monitoring`
- `ade-spring-boot-starter`

### Running Examples

All examples are in `examples/src/main/java/com/phdsystems/agent/examples/`:

```bash
# List all examples
ls examples/src/main/java/com/phdsystems/agent/examples/

# Run an example
cd examples
mvn exec:java -Dexec.mainClass="com.phdsystems.agent.examples.MonitoringExample"
```

---

## Related Documentation

### Main SDK Documentation

- [Getting Started Guide](../../docs/getting-started.md) - Installation and basics
- [API Reference](../../docs/api-reference.md) - Complete API docs
- [Best Practices](../../docs/best-practices.md) - Design patterns
- [Monitoring Guide](../../docs/monitoring-guide.md) - Production monitoring
- [Testing Guide](../../docs/testing-guide.md) - Testing strategies

### SDLC Documentation

- [Architecture](../3-design/architecture.md) - System architecture
- [Component Specifications](../3-design/component-specifications.md) - Detailed component specs
- [API Design](../3-design/api-design.md) - API design decisions
- [Dataflow](../3-design/dataflow.md) - Data flow diagrams
- [Workflow](../3-design/workflow.md) - Execution workflows
- [Overview](../overview.md) - Master documentation index

---

## Contributing

### Adding New Examples

1. Create example code in `examples/src/main/java/`
2. Write tests in `examples/src/test/java/`
3. Document in appropriate guide under `doc/example/`
4. Update this overview
5. Submit pull request

### Documentation Standards

All example guides follow these standards:
- ✅ Clear "What it covers" section
- ✅ "Use this when" practical guidance
- ✅ Code examples with explanations
- ✅ Common patterns highlighted
- ✅ Related documentation links
- ✅ Installation instructions

---

## Support

### Questions?

- Check the appropriate guide above
- Review example code in `examples/` directory
- See main SDK documentation
- Search GitHub issues

### Found an Issue?

- Documentation issues: Create GitHub issue with "docs:" prefix
- Module issues: Create GitHub issue with "[module-name]:" prefix

---

*This overview is updated when new examples are added.*

*Last Updated: 2025-10-20*
*Version: 1.0*
