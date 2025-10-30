# ADE Agent Serialization

JSON serialization support for ADE Agent using Jackson.

**Example Code:** [SerializationExample.java](../../examples/src/main/java/com/phdsystems/agent/examples/SerializationExample.java)

## Overview

This module provides JSON serialization for ADE Agent types:
- TaskRequest/TaskResult serialization
- AgentInfo serialization
- File-based result storage
- Custom ObjectMapper support
- Pretty-printed JSON output

## Installation

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-serialization</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

### Basic Serialization

```java
import com.phdsystems.agent.serialization.AgentSerializer;

AgentSerializer serializer = new AgentSerializer();

// Serialize TaskResult
TaskResult result = agent.executeTask(request);
String json = serializer.serializeTaskResult(result);
System.out.println(json);

// Deserialize TaskResult
TaskResult deserialized = serializer.deserializeTaskResult(json);
```

### File Storage

```java
import com.phdsystems.agent.serialization.JsonFileStorage;
import java.nio.file.Path;

JsonFileStorage storage = new JsonFileStorage(Path.of("results"));

// Save result
storage.saveResult("task-001", result);

// Load result
TaskResult loaded = storage.loadResult("task-001");

// Check existence
if (storage.resultExists("task-001")) {
    System.out.println("Result exists");
}

// Load all results
List<TaskResult> allResults = storage.loadAllResults();
```

## Features

- **Jackson integration** - Industry-standard JSON library
- **Type-safe** - Serializes Java records correctly
- **Pretty-printing** - Human-readable JSON output
- **File storage** - Simple file-based persistence
- **Customizable** - Use custom ObjectMapper

## API Reference

### AgentSerializer

```java
public final class AgentSerializer {
    String serializeTaskRequest(TaskRequest request) throws IOException;
    TaskRequest deserializeTaskRequest(String json) throws IOException;

    String serializeTaskResult(TaskResult result) throws IOException;
    TaskResult deserializeTaskResult(String json) throws IOException;

    String serializeAgentInfo(AgentInfo info) throws IOException;
    AgentInfo deserializeAgentInfo(String json) throws IOException;

    ObjectMapper getObjectMapper();
}
```

### JsonFileStorage

```java
public final class JsonFileStorage {
    void saveResult(String id, TaskResult result) throws IOException;
    TaskResult loadResult(String id) throws IOException;
    boolean resultExists(String id);
    boolean deleteResult(String id) throws IOException;
    List<TaskResult> loadAllResults() throws IOException;
    long count() throws IOException;
    void clear() throws IOException;
}
```

## Examples

### JSON Output Format

```java
AgentSerializer serializer = new AgentSerializer();

TaskResult result = TaskResult.success(
    "CalculatorAgent",
    "2 + 2",
    "4",
    Map.of("operation", "addition"),
    50L
);

String json = serializer.serializeTaskResult(result);
System.out.println(json);
```

Output:

```json
{
  "success" : true,
  "agentName" : "CalculatorAgent",
  "task" : "2 + 2",
  "output" : "4",
  "metadata" : {
    "operation" : "addition"
  },
  "executionTimeMs" : 50
}
```

### Custom ObjectMapper

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

ObjectMapper customMapper = new ObjectMapper();
customMapper.disable(SerializationFeature.INDENT_OUTPUT); // Compact JSON
customMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

AgentSerializer serializer = new AgentSerializer(customMapper);
```

### Result Logging

```java
JsonFileStorage storage = new JsonFileStorage(Path.of("logs"));

Agent agent = new MyAgent();
TaskRequest request = TaskRequest.of("MyAgent", "task data");

// Execute and log
TaskResult result = agent.executeTask(request);
String taskId = UUID.randomUUID().toString();
storage.saveResult(taskId, result);

System.out.println("Saved result: " + taskId);
```

### Audit Trail

```java
JsonFileStorage auditLog = new JsonFileStorage(Path.of("audit"));

// Save all executions
for (TaskRequest req : requests) {
    TaskResult result = agent.executeTask(req);

    String auditId = String.format(
        "%s-%d",
        req.agentName(),
        System.currentTimeMillis()
    );

    auditLog.saveResult(auditId, result);
}

// Review audit trail
List<TaskResult> allResults = auditLog.loadAllResults();
long successCount = allResults.stream()
    .filter(TaskResult::success)
    .count();

System.out.println("Success rate: " +
    (successCount * 100.0 / allResults.size()) + "%");
```

### Result Caching

```java
public class CachingAgent implements Agent {
    private final Agent delegate;
    private final JsonFileStorage cache;

    public CachingAgent(Agent delegate, Path cacheDir) throws IOException {
        this.delegate = delegate;
        this.cache = new JsonFileStorage(cacheDir);
    }

    @Override
    public TaskResult executeTask(TaskRequest request) {
        String cacheKey = generateCacheKey(request);

        // Check cache
        if (cache.resultExists(cacheKey)) {
            try {
                return cache.loadResult(cacheKey);
            } catch (IOException e) {
                // Cache miss, execute
            }
        }

        // Execute and cache
        TaskResult result = delegate.executeTask(request);
        try {
            cache.saveResult(cacheKey, result);
        } catch (IOException e) {
            // Log error, but return result
        }

        return result;
    }

    private String generateCacheKey(TaskRequest request) {
        return request.agentName() + "-" +
               Integer.toHexString(request.task().hashCode());
    }

    // ... implement other Agent methods
}
```

### Batch Processing

```java
JsonFileStorage inputStorage = new JsonFileStorage(Path.of("input"));
JsonFileStorage outputStorage = new JsonFileStorage(Path.of("output"));

// Load requests from files
List<TaskResult> inputs = inputStorage.loadAllResults();

// Process each
Agent agent = new MyAgent();
for (int i = 0; i < inputs.size(); i++) {
    TaskResult inputResult = inputs.get(i);

    TaskRequest request = TaskRequest.of(
        agent.getName(),
        inputResult.output() // Use previous output as input
    );

    TaskResult output = agent.executeTask(request);
    outputStorage.saveResult("output-" + i, output);
}
```

### Export/Import

```java
AgentSerializer serializer = new AgentSerializer();

// Export agent info
Agent agent = new MyAgent();
AgentInfo info = agent.getAgentInfo();
String infoJson = serializer.serializeAgentInfo(info);

// Save to file
Files.writeString(Path.of("agent-info.json"), infoJson);

// Import
String loadedJson = Files.readString(Path.of("agent-info.json"));
AgentInfo loadedInfo = serializer.deserializeAgentInfo(loadedJson);

System.out.println("Loaded: " + loadedInfo.name());
```

## Use Cases

- **Result logging** - Log all task executions to JSON files
- **Audit trails** - Maintain audit log of agent executions
- **Result caching** - Cache expensive agent results
- **API responses** - Serialize results for REST APIs
- **Testing** - Serialize test data and expected results
- **Data export** - Export agent results for analysis

## Best Practices

1. **Use file storage for persistence** - Simple and reliable
2. **Generate unique IDs** - Use UUIDs or timestamps for result IDs
3. **Handle IOExceptions** - File operations can fail
4. **Pretty-print for debugging** - Enable indentation for logs
5. **Compact for APIs** - Disable indentation for network transfer

## Integration Examples

### With REST API

```java
@RestController
public class AgentController {
    private final AgentSerializer serializer = new AgentSerializer();
    private final Agent agent;

    @PostMapping("/execute")
    public String execute(@RequestBody String requestJson) throws IOException {
        TaskRequest request = serializer.deserializeTaskRequest(requestJson);
        TaskResult result = agent.executeTask(request);
        return serializer.serializeTaskResult(result);
    }
}
```

### With Message Queue

```java
public class AgentMessageProducer {
    private final AgentSerializer serializer = new AgentSerializer();

    public void sendResult(TaskResult result) throws IOException {
        String json = serializer.serializeTaskResult(result);
        messageQueue.publish("agent.results", json);
    }
}
```

## Future Enhancements

- XML serialization support
- Protocol Buffers support
- Database storage adapter
- Result compression
- Schema validation
- Versioning support

## License

Apache License 2.0
