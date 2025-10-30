# ADE Agent Discovery

Agent registry and discovery mechanism for dynamic agent management.

**Example Code:** [DiscoveryExample.java](../../examples/src/main/java/com/phdsystems/agent/examples/DiscoveryExample.java)

## Overview

This module provides centralized agent registration and discovery:
- In-memory agent registry
- Capability-based discovery
- Advanced filtering and search
- Agent statistics and monitoring
- Thread-safe implementation

## Installation

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-discovery</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

### Basic Registry Usage

```java
import com.phdsystems.agent.discovery.AgentRegistry;
import com.phdsystems.agent.discovery.InMemoryAgentRegistry;

AgentRegistry registry = new InMemoryAgentRegistry();

// Register agents
registry.register(calculatorAgent);
registry.register(weatherAgent);
registry.register(analysisAgent);

// Find by name
Optional<Agent> found = registry.findByName("CalculatorAgent");

// Find by capability
List<Agent> analysisAgents = registry.findByCapability("analysis");

// Get all agents
List<Agent> all = registry.findAll();
```

### Discovery Service

```java
import com.phdsystems.agent.discovery.AgentDiscoveryService;

AgentDiscoveryService discovery = new AgentDiscoveryService(registry);

// Search by description
List<Agent> weatherAgents = discovery.searchByDescription("weather");

// Find by multiple capabilities
List<Agent> capable = discovery.findByCapabilities(
  List.of("analysis", "reporting")
);

// Get statistics
DiscoveryStats stats = discovery.getStats();
System.out.println("Total agents: " + stats.agentCount());
System.out.println("Unique capabilities: " + stats.uniqueCapabilities());
```

## Features

- **Thread-safe registry** - ConcurrentHashMap-based implementation
- **Capability discovery** - Find agents by capabilities
- **Advanced search** - Filter by name, description, capabilities
- **Statistics** - Track agent counts and capabilities
- **Extensible** - Implement AgentRegistry for custom backends

## API Reference

### AgentRegistry Interface

```java
public interface AgentRegistry {
    void register(Agent agent);
    boolean unregister(String name);
    Optional<Agent> findByName(String name);
    List<Agent> findByCapability(String capability);
    List<Agent> findAll();
    List<AgentInfo> getAllAgentInfo();
    boolean isRegistered(String name);
    int count();
    void clear();
}
```

### AgentDiscoveryService

```java
public final class AgentDiscoveryService {
    List<Agent> findAgents(Predicate<Agent> predicate);
    List<Agent> searchByDescription(String keyword);
    List<Agent> findByCapabilities(List<String> capabilities);
    List<Agent> findByNamePattern(String pattern);
    Optional<AgentInfo> getAgentInfo(String name);
    List<String> getAllAgentNames();
    DiscoveryStats getStats();
}
```

## Examples

### Centralized Agent Management

```java
AgentRegistry registry = new InMemoryAgentRegistry();

// Register all agents at startup
registry.register(new CalculatorAgent());
registry.register(new WeatherAgent());
registry.register(new EmailAgent());
registry.register(new DataAnalysisAgent());

// Application code can discover agents dynamically
Optional<Agent> calculator = registry.findByName("CalculatorAgent");
if (calculator.isPresent()) {
    TaskResult result = calculator.get().executeTask(request);
}
```

### Dynamic Capability Discovery

```java
AgentDiscoveryService discovery = new AgentDiscoveryService(registry);

// Find all agents that can analyze data
List<Agent> analysisAgents = discovery.findByCapabilities(
  List.of("analysis", "data-processing")
);

// Execute task on all capable agents
for (Agent agent : analysisAgents) {
    TaskResult result = agent.executeTask(request);
    System.out.println(agent.getName() + ": " + result.output());
}
```

### Custom Filtering

```java
// Find agents with more than 3 capabilities
List<Agent> versatileAgents = discovery.findAgents(
  agent -> agent.getCapabilities().size() > 3
);

// Find agents by name prefix
List<Agent> dataAgents = discovery.findByNamePattern("Data");
```

### Agent Listing and Discovery

```java
AgentDiscoveryService discovery = new AgentDiscoveryService(registry);

// Get all agent names
List<String> names = discovery.getAllAgentNames();
names.forEach(System.out::println);

// Get agent information
for (String name : names) {
    discovery.getAgentInfo(name).ifPresent(info -> {
        System.out.println(info.name() + ": " + info.description());
        System.out.println("  Capabilities: " + info.capabilities());
    });
}
```

### Statistics and Monitoring

```java
DiscoveryStats stats = discovery.getStats();

System.out.println("=== Agent Registry Statistics ===");
System.out.println("Total agents: " + stats.agentCount());
System.out.println("Total capabilities: " + stats.totalCapabilities());
System.out.println("Unique capabilities: " + stats.uniqueCapabilities());
System.out.println("Avg capabilities per agent: " +
  (stats.totalCapabilities() / stats.agentCount()));
```

## Architecture Patterns

### Plugin Architecture

```java
// Application loads agents dynamically
AgentRegistry registry = new InMemoryAgentRegistry();

// Plugin loader discovers and registers agents
PluginLoader loader = new PluginLoader();
List<Agent> plugins = loader.loadAgents("plugins/");
plugins.forEach(registry::register);

// Application discovers agents at runtime
AgentDiscoveryService discovery = new AgentDiscoveryService(registry);
List<Agent> imageAgents = discovery.searchByDescription("image");
```

### Microservices Registry

```java
// Each microservice registers its agents
AgentRegistry registry = new InMemoryAgentRegistry();
registry.register(new LocalAgent());

// Service can discover remote agents too
// (combine with gRPC/REST modules)
Agent remoteAgent = AgentGrpcClient.builder()
  .host("remote-service")
  .port(9090)
  .agentName("RemoteAgent")
  .build();

registry.register(remoteAgent);

// Unified discovery across local and remote agents
List<Agent> all = registry.findAll();
```

## Use Cases

- **Plugin systems** - Dynamically load and discover agent plugins
- **Service registries** - Register agents from multiple sources
- **Capability routing** - Route tasks to capable agents
- **Agent marketplaces** - Browse available agents
- **Load balancing** - Discover multiple instances of same agent

## Best Practices

1. **Register at startup** - Register all agents during application initialization
2. **Use capabilities** - Define clear capabilities for discoverable agents
3. **Descriptive names** - Use meaningful agent names and descriptions
4. **Thread safety** - InMemoryAgentRegistry is thread-safe
5. **Graceful shutdown** - Unregister agents on shutdown

## Future Enhancements

- Distributed registry (Redis, Consul, etcd)
- Service health checking
- Agent versioning support
- Tag-based discovery
- Event notifications (agent registered/unregistered)
- TTL and expiration
- Agent dependency resolution

## License

Apache License 2.0
