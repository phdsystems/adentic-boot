# ADE Agent Monitoring

Metrics collection and monitoring for ADE Agent using Micrometer.

**Example Code:** [MonitoringExample.java](../../examples/src/main/java/com/phdsystems/agent/examples/MonitoringExample.java)

## Overview

This module provides automatic metrics collection for agents:
- Execution time tracking
- Success/failure rate monitoring
- Task count metrics
- Integration with Micrometer (Prometheus, Graphite, etc.)
- Thread-safe metrics collection

## Installation

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-monitoring</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- Optional: Prometheus registry -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
    <version>1.12.2</version>
</dependency>
```

## Quick Start

### Basic Monitoring

```java
import com.phdsystems.agent.monitoring.MonitoredAgent;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

MeterRegistry registry = new SimpleMeterRegistry();
Agent original = new MyAgent();

Agent monitored = new MonitoredAgent(original, registry);

TaskResult result = monitored.executeTask(request);

// Metrics automatically recorded
```

### Metrics Collector

```java
import com.phdsystems.agent.monitoring.MetricsCollector;

MeterRegistry registry = new SimpleMeterRegistry();
MetricsCollector collector = new MetricsCollector(registry);

// Record task execution
collector.recordTaskExecution("MyAgent", true, 150L);
collector.recordTaskExecution("MyAgent", true, 200L);
collector.recordTaskExecution("MyAgent", false, 100L);

// Get statistics
AgentStats stats = collector.getAgentStats("MyAgent");
System.out.println("Total: " + stats.totalTasks());
System.out.println("Success rate: " + stats.successRate() + "%");
```

## Features

- **Automatic metrics** - Wrap agents to automatically collect metrics
- **Micrometer integration** - Export to Prometheus, Graphite, etc.
- **Pre-built metrics** - Execution time, success/failure counts
- **Aggregated statistics** - Success rates, total tasks
- **Thread-safe** - Safe for concurrent execution
- **Low overhead** - Minimal performance impact

## Metrics Collected

### Timers

- `agent.execution.time` - Agent execution duration
  - Tag: `agent` (agent name)

### Counters

- `agent.tasks.total` - Total task executions
  - Tag: `agent` (agent name)
- `agent.tasks.success` - Successful executions
  - Tag: `agent` (agent name)
- `agent.tasks.failure` - Failed executions
  - Tag: `agent` (agent name)

### Gauges

- `agent.tasks.total.current` - Current total task count
  - Tag: `agent` (agent name)
- `agent.success.rate` - Success rate (0-100)
  - Tag: `agent` (agent name)

## Examples

### Prometheus Integration

```java
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

Agent monitored = new MonitoredAgent(myAgent, registry);

// Execute tasks
monitored.executeTask(request1);
monitored.executeTask(request2);

// Expose metrics endpoint
String metrics = registry.scrape();
System.out.println(metrics);
```

Output:

```
# HELP agent_execution_time_seconds Agent execution time
# TYPE agent_execution_time_seconds summary
agent_execution_time_seconds_count{agent="MyAgent"} 2.0
agent_execution_time_seconds_sum{agent="MyAgent"} 0.35

# HELP agent_tasks_total Total task executions
# TYPE agent_tasks_total counter
agent_tasks_total{agent="MyAgent"} 2.0

# HELP agent_tasks_success Successful task executions
# TYPE agent_tasks_success counter
agent_tasks_success{agent="MyAgent"} 2.0
```

### Custom Metrics

```java
import com.phdsystems.agent.monitoring.AgentMetrics;

MeterRegistry registry = new SimpleMeterRegistry();

// Record execution with custom logic
TaskResult result = AgentMetrics.recordExecution(registry, "MyAgent", () -> {
    // Custom execution logic
    return myAgent.executeTask(request);
});

// Add custom tags
List<Tag> tags = AgentMetrics.createTags("MyAgent", "analysis");
TaskResult result = AgentMetrics.recordExecution(registry, "MyAgent", tags, () -> {
    return myAgent.executeTask(request);
});
```

### Dashboard Monitoring

```java
MetricsCollector collector = new MetricsCollector(registry);

// Record multiple agents
collector.recordTaskExecution("Agent1", true, 100L);
collector.recordTaskExecution("Agent2", true, 150L);
collector.recordTaskExecution("Agent3", false, 200L);

// Get all stats for dashboard
Map<String, AgentStats> allStats = collector.getAllStats();

allStats.forEach((name, stats) -> {
    System.out.println("=== " + name + " ===");
    System.out.println("Total: " + stats.totalTasks());
    System.out.println("Success: " + stats.successTasks());
    System.out.println("Failed: " + stats.failureTasks());
    System.out.println("Success Rate: " + String.format("%.2f%%", stats.successRate()));
});
```

### Spring Boot Integration

```java
@Configuration
public class AgentConfig {

    @Bean
    public Agent monitoredCalculator(MeterRegistry registry) {
        return new MonitoredAgent(new CalculatorAgent(), registry);
    }

    @Bean
    public Agent monitoredWeather(MeterRegistry registry) {
        return new MonitoredAgent(new WeatherAgent(), registry);
    }

    @Bean
    public MetricsCollector metricsCollector(MeterRegistry registry) {
        return new MetricsCollector(registry);
    }
}
```

### Health Monitoring

```java
MetricsCollector collector = new MetricsCollector(registry);

// Monitor agent health
AgentStats stats = collector.getAgentStats("MyAgent");

if (stats.successRate() < 90.0) {
    System.err.println("WARNING: Agent success rate below 90%");
}

if (stats.totalTasks() > 10000) {
    System.out.println("INFO: Agent has processed 10k+ tasks");
}
```

## Architecture

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ executeTask()
       ▼
┌─────────────────┐
│ MonitoredAgent  │
│                 │
│  ┌───────────┐ │
│  │ Record    │ │
│  │ Metrics   │ │
│  └───────────┘ │
└────────┬────────┘
         │ delegate
         ▼
┌─────────────┐
│   Agent     │
│ (original)  │
└─────────────┘

         │
         ▼
┌─────────────────┐
│ MeterRegistry   │
│                 │
│ - Prometheus    │
│ - Graphite      │
│ - CloudWatch    │
└─────────────────┘
```

## Use Cases

- **Production monitoring** - Track agent performance in production
- **SLA tracking** - Monitor success rates and latency
- **Capacity planning** - Analyze task execution patterns
- **Debugging** - Identify slow or failing agents
- **Alerting** - Trigger alerts based on metrics
- **Performance tuning** - Identify bottlenecks

## Best Practices

1. **Wrap all agents** - Monitor all agents in production
2. **Set up dashboards** - Visualize metrics in Grafana/Prometheus
3. **Configure alerts** - Alert on high failure rates or slow execution
4. **Use tags** - Add meaningful tags for filtering
5. **Export to central registry** - Aggregate metrics from all instances

## Micrometer Backends

Supported metric registries:
- **Prometheus** - Time-series database
- **Graphite** - Metrics aggregation
- **InfluxDB** - Time-series database
- **CloudWatch** - AWS monitoring
- **Datadog** - Monitoring platform
- **New Relic** - APM platform

## Future Enhancements

- Custom metric annotations
- Distributed tracing integration
- Anomaly detection
- Automatic alerting
- Performance regression detection
- SLA enforcement

## License

Apache License 2.0
