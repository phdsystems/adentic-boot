# ADE Agent Composition

Agent composition and delegation patterns for building complex multi-agent workflows.

**Example Code:** [CompositionAgentExample.java](../../examples/src/main/java/com/phdsystems/agent/examples/CompositionAgentExample.java)

## Overview

This module provides patterns for combining multiple agents:
- Sequential execution (pipeline/chain pattern)
- Parallel execution (fan-out pattern)
- Result aggregation
- Failure handling

## Installation

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-composition</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

### Sequential Execution

```java
import com.phdsystems.agent.composition.SequentialAgent;

// Create a data pipeline
Agent pipeline = SequentialAgent.of(
  "DataPipeline",
  List.of(validateAgent, transformAgent, storeAgent)
);

// Each agent's output becomes input for the next
TaskResult result = pipeline.executeTask(request);
```

### Parallel Execution

```java
import com.phdsystems.agent.composition.ParallelAgent;

// Execute multiple analyses concurrently
Agent analysis = ParallelAgent.of(
  "MultiAnalysis",
  List.of(sentimentAgent, keywordAgent, summaryAgent)
);

// All agents receive same input, results are aggregated
TaskResult result = analysis.executeTask(request);
```

## Features

- **Sequential chaining** - Output of one agent feeds into the next
- **Parallel execution** - Execute multiple agents concurrently
- **Automatic aggregation** - Combine results from all agents
- **Failure handling** - Stop-on-first-failure for sequential, report-all for parallel
- **Capability aggregation** - Composite capabilities from all sub-agents

## API Reference

### CompositeAgent Interface

```java
public interface CompositeAgent extends Agent {
    List<Agent> getSubAgents();
    TaskResult executeTask(TaskRequest request);
}
```

### SequentialAgent

Executes agents in sequence, chaining outputs:

```java
SequentialAgent sequential = SequentialAgent.of(
  "Pipeline",
  List.of(agent1, agent2, agent3)
);

// agent1.output → agent2.input → agent2.output → agent3.input → final.output
```

**Behavior:**
- Stops on first failure
- Each agent's output becomes next agent's input
- Returns final agent's output on success

### ParallelAgent

Executes agents concurrently and aggregates results:

```java
ParallelAgent parallel = ParallelAgent.of(
  "MultiTask",
  List.of(agent1, agent2, agent3)
);

// All agents receive same input, execute in parallel
```

**Behavior:**
- All agents execute concurrently
- Results separated by `---` delimiter
- Success only if all agents succeed
- Remember to call `shutdown()` when done

## Examples

### Data Processing Pipeline

```java
Agent validator = new ValidationAgent();
Agent transformer = new TransformAgent();
Agent enricher = new EnrichmentAgent();
Agent persister = new PersistenceAgent();

Agent pipeline = SequentialAgent.of(
  "ETL-Pipeline",
  List.of(validator, transformer, enricher, persister)
);

TaskRequest request = TaskRequest.of("ETL-Pipeline", csvData);
TaskResult result = pipeline.executeTask(request);
```

### Multi-Model Analysis

```java
Agent gptAnalysis = new GPTAgent();
Agent claudeAnalysis = new ClaudeAgent();
Agent geminiAnalysis = new GeminiAgent();

ParallelAgent multiModel = ParallelAgent.of(
  "MultiModel",
  List.of(gptAnalysis, claudeAnalysis, geminiAnalysis)
);

TaskRequest request = TaskRequest.of("MultiModel", "Analyze sentiment");
TaskResult result = multiModel.executeTask(request);

// Cleanup when done
multiModel.shutdown();
```

### Nested Composition

```java
// Parallel preprocessing
Agent parallelPrep = ParallelAgent.of(
  "Preprocessing",
  List.of(cleanAgent, normalizeAgent)
);

// Sequential pipeline with parallel step
Agent workflow = SequentialAgent.of(
  "ComplexWorkflow",
  List.of(validateAgent, parallelPrep, analyzeAgent)
);
```

## Best Practices

1. **Sequential for pipelines** - Use when output of one step feeds the next
2. **Parallel for independence** - Use when tasks are independent and can run concurrently
3. **Remember shutdown** - Always call `shutdown()` on ParallelAgent when done
4. **Validate inputs** - Ensure each agent can handle the output format of the previous agent
5. **Handle failures** - Sequential stops on first failure, parallel aggregates all results

## Future Enhancements

- Conditional execution (if-then-else patterns)
- Retry policies for sub-agents
- Circuit breaker support
- Dynamic agent selection
- Result filtering and transformation

## License

Apache License 2.0
