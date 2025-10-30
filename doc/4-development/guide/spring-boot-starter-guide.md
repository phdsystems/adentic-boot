# ADE Agent Spring Boot Starter

Spring Boot starter for ADE Agent with auto-configuration.

**Example Code:** This integration module is demonstrated through inline code examples below. See also [AgentSdkAutoConfigurationTest.java](../../ade-spring-boot-starter/src/test/java/com/phdsystems/agent/spring/AgentSdkAutoConfigurationTest.java) for test examples.

## Overview

This module provides Spring Boot integration with automatic configuration:
- Auto-registers all Agent beans with registry
- Configures discovery service
- Enables monitoring with Micrometer
- Provides serialization support
- Exposes /actuator/agents endpoint
- Configurable via application.properties

## Installation

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

### Basic Application

```java
@SpringBootApplication
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }

    @Bean
    public Agent calculatorAgent() {
        return new CalculatorAgent();
    }

    @Bean
    public Agent weatherAgent() {
        return new WeatherAgent();
    }
}
```

That's it! Agents are automatically:
- Registered with AgentRegistry
- Wrapped with monitoring (if Micrometer present)
- Available for discovery
- Exposed via actuator endpoint

### Using Agents

```java
@RestController
@RequestMapping("/api")
public class AgentController {

    private final AgentRegistry registry;
    private final AgentDiscoveryService discovery;

    public AgentController(AgentRegistry registry, AgentDiscoveryService discovery) {
        this.registry = registry;
        this.discovery = discovery;
    }

    @GetMapping("/agents")
    public List<AgentInfo> getAllAgents() {
        return registry.getAllAgentInfo();
    }

    @PostMapping("/execute/{agentName}")
    public TaskResult execute(
        @PathVariable String agentName,
        @RequestBody TaskRequest request
    ) {
        return registry.findByName(agentName)
            .map(agent -> agent.executeTask(request))
            .orElseThrow(() -> new AgentNotFoundException(agentName));
    }
}
```

## Features

- **Auto-configuration** - Zero-config setup for most use cases
- **Agent registration** - All @Bean agents automatically registered
- **Monitoring** - Automatic Micrometer integration
- **Discovery** - AgentRegistry and AgentDiscoveryService beans
- **Actuator endpoint** - /actuator/agents for agent info
- **Serialization** - AgentSerializer bean for JSON
- **Configurable** - application.properties configuration

## Configuration

### application.properties

```properties
# Enable/disable auto-registration (default: true)
agent.sdk.auto-register=true

# Enable/disable monitoring (default: true)
agent.sdk.monitoring.enabled=true

# Enable/disable discovery (default: true)
agent.sdk.discovery.enabled=true
```

### application.yml

```yaml
agent:
  sdk:
    auto-register: true
    monitoring:
      enabled: true
    discovery:
      enabled: true
```

## Auto-Configured Beans

The starter auto-configures these beans:

|         Bean          |         Type          |       Condition       |
|-----------------------|-----------------------|-----------------------|
| agentRegistry         | AgentRegistry         | Always                |
| agentDiscoveryService | AgentDiscoveryService | Always                |
| agentSerializer       | AgentSerializer       | Always                |
| metricsCollector      | MetricsCollector      | If Micrometer present |
| agentEndpoint         | AgentEndpoint         | If Actuator present   |

## Examples

### REST API

```java
@SpringBootApplication
public class AgentApiApplication {

    @Bean
    public Agent calculatorAgent() {
        return new CalculatorAgent();
    }

    @Bean
    public Agent dataAnalysisAgent() {
        return new DataAnalysisAgent();
    }
}

@RestController
@RequestMapping("/api/agents")
class AgentApiController {

    private final AgentRegistry registry;

    @PostMapping("/{name}/execute")
    public ResponseEntity<TaskResult> execute(
        @PathVariable String name,
        @RequestBody String task
    ) {
        return registry.findByName(name)
            .map(agent -> {
                TaskRequest request = TaskRequest.of(name, task);
                TaskResult result = agent.executeTask(request);
                return ResponseEntity.ok(result);
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
```

### Scheduled Agent Execution

```java
@Component
public class ScheduledAgentRunner {

    private final AgentRegistry registry;

    @Scheduled(fixedRate = 60000) // Every minute
    public void runAnalysis() {
        registry.findByName("DataAnalysisAgent")
            .ifPresent(agent -> {
                TaskRequest request = TaskRequest.of(
                    "DataAnalysisAgent",
                    "analyze recent data"
                );
                agent.executeTask(request);
            });
    }
}
```

### Service Layer

```java
@Service
public class AgentService {

    private final AgentDiscoveryService discovery;
    private final MetricsCollector metrics;

    public List<TaskResult> executeOnCapableAgents(
        String capability,
        String task
    ) {
        List<Agent> agents = discovery.findByCapabilities(
            List.of(capability)
        );

        return agents.stream()
            .map(agent -> {
                TaskRequest request = TaskRequest.of(agent.getName(), task);
                TaskResult result = agent.executeTask(request);

                metrics.recordTaskExecution(
                    agent.getName(),
                    result.success(),
                    result.executionTimeMs()
                );

                return result;
            })
            .toList();
    }
}
```

### Actuator Endpoint

```bash
# Get all agents
curl http://localhost:8080/actuator/agents

# Response
{
  "totalAgents": 3,
  "agents": [
    {
      "name": "CalculatorAgent",
      "description": "Performs mathematical calculations",
      "capabilities": ["math", "calculation"]
    },
    {
      "name": "WeatherAgent",
      "description": "Gets weather information",
      "capabilities": ["weather", "http"]
    }
  ],
  "stats": {
    "totalCapabilities": 5,
    "uniqueCapabilities": 4
  }
}
```

### Monitoring with Prometheus

```java
@SpringBootApplication
public class MonitoredAgentApp {

    @Bean
    public Agent monitoredCalculator(MeterRegistry registry) {
        return new MonitoredAgent(new CalculatorAgent(), registry);
    }
}
```

```properties
# Enable Prometheus endpoint
management.endpoints.web.exposure.include=prometheus,agents
management.metrics.export.prometheus.enabled=true
```

Access metrics:

```bash
curl http://localhost:8080/actuator/prometheus

# agent_tasks_total{agent="CalculatorAgent"} 150
# agent_tasks_success{agent="CalculatorAgent"} 148
# agent_execution_time_seconds_sum{agent="CalculatorAgent"} 2.5
```

### Custom Configuration

```java
@Configuration
public class AgentConfig {

    @Bean
    @ConditionalOnProperty(name = "custom.agent.enabled", havingValue = "true")
    public Agent customAgent() {
        return new CustomAgent();
    }

    @Bean
    public Agent compositeAgent(AgentRegistry registry) {
        Agent agent1 = registry.findByName("Agent1").orElseThrow();
        Agent agent2 = registry.findByName("Agent2").orElseThrow();

        return SequentialAgent.of("Pipeline", List.of(agent1, agent2));
    }
}
```

## Testing

```java
@SpringBootTest
class AgentIntegrationTest {

    @Autowired
    private AgentRegistry registry;

    @Autowired
    private AgentDiscoveryService discovery;

    @Test
    void shouldAutoRegisterAgents() {
        assertTrue(registry.isRegistered("CalculatorAgent"));
        assertEquals(2, registry.count());
    }

    @Test
    void shouldDiscoverByCapability() {
        List<Agent> mathAgents = registry.findByCapability("math");
        assertFalse(mathAgents.isEmpty());
    }
}
```

## Architecture

```
┌──────────────────────────────────────────┐
│      Spring Boot Application             │
└────────────┬─────────────────────────────┘
             │
             ▼
┌──────────────────────────────────────────┐
│   AgentSdkAutoConfiguration              │
│                                           │
│  - Auto-configures AgentRegistry         │
│  - Auto-configures AgentDiscoveryService │
│  - Auto-configures AgentSerializer       │
│  - Auto-configures MetricsCollector      │
│  - Registers all @Bean Agents            │
└────────────┬─────────────────────────────┘
             │
             ▼
┌──────────────────────────────────────────┐
│        Application Beans                  │
│                                           │
│  - AgentRegistry (autowired)             │
│  - AgentDiscoveryService (autowired)     │
│  - AgentSerializer (autowired)           │
│  - MetricsCollector (autowired)          │
└──────────────────────────────────────────┘
```

## Use Cases

- **Microservices** - Agent-based microservice architecture
- **API services** - REST APIs backed by agents
- **Scheduled tasks** - Cron-based agent execution
- **Event processing** - Event-driven agent workflows
- **Plugin systems** - Dynamic agent loading
- **Multi-tenant** - Tenant-specific agent instances

## Best Practices

1. **Define agents as @Bean** - Spring manages lifecycle
2. **Use @Qualifier** - Disambiguate multiple agents
3. **Enable actuator** - Monitor agents in production
4. **Configure metrics** - Export to Prometheus/Grafana
5. **Use profiles** - Different agents per environment

## Integration Examples

### With Spring Data

```java
@Service
public class DataService {

    private final UserRepository repository;
    private final AgentRegistry agentRegistry;

    public void processUsers() {
        List<User> users = repository.findAll();

        Agent processor = agentRegistry
            .findByName("UserProcessor")
            .orElseThrow();

        users.forEach(user -> {
            TaskRequest request = TaskRequest.of(
                "UserProcessor",
                user.getData()
            );
            processor.executeTask(request);
        });
    }
}
```

### With Spring Security

```java
@Service
public class SecureAgentService {

    private final AgentRegistry registry;

    @PreAuthorize("hasRole('ADMIN')")
    public TaskResult executePrivilegedAgent(String name, String task) {
        return registry.findByName(name)
            .map(agent -> agent.executeTask(TaskRequest.of(name, task)))
            .orElseThrow();
    }
}
```

### With Spring Cloud

```java
@FeignClient(name = "agent-service")
public interface RemoteAgentClient {

    @PostMapping("/agents/{name}/execute")
    TaskResult execute(@PathVariable String name, @RequestBody TaskRequest request);
}

@Service
public class DistributedAgentService {

    private final AgentRegistry localRegistry;
    private final RemoteAgentClient remoteClient;

    public TaskResult executeAnywhere(String agentName, TaskRequest request) {
        // Try local first
        Optional<Agent> local = localRegistry.findByName(agentName);
        if (local.isPresent()) {
            return local.get().executeTask(request);
        }

        // Fallback to remote
        return remoteClient.execute(agentName, request);
    }
}
```

## Troubleshooting

### Agents not registered

Check that:
- Agents are defined as @Bean methods
- Auto-registration is enabled: `agent.sdk.auto-register=true`
- Agent names are unique

### Metrics not collected

Ensure:
- Micrometer is on classpath
- Monitoring is enabled: `agent.sdk.monitoring.enabled=true`
- Agents are wrapped with MonitoredAgent

### Actuator endpoint not available

Verify:
- Spring Boot Actuator is included
- Endpoint is exposed: `management.endpoints.web.exposure.include=agents`

## Future Enhancements

- gRPC server auto-configuration
- REST API auto-configuration
- Agent health indicators
- Agent auto-scaling support
- Distributed tracing integration
- Circuit breaker integration

## License

Apache License 2.0
