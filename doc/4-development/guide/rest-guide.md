# ADE Agent REST

REST API implementation for remote agent execution over HTTP.

**Example Code:** This integration module is demonstrated through inline code examples below. See also [AgentRestServerTest.java](../../ade-rest/src/test/java/com/phdsystems/agent/rest/AgentRestServerTest.java) for test examples.

## Overview

This module enables agents to be exposed and accessed via REST API:
- RESTful HTTP endpoints
- JSON request/response
- Simple HTTP-based architecture
- Web browser compatible
- Easy integration with existing HTTP clients

## Installation

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-rest</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

### Server Setup

```java
import com.phdsystems.agent.rest.AgentRestServer;

Agent myAgent = new MyAgent();

AgentRestServer server = AgentRestServer.builder()
  .port(8080)
  .addAgent(myAgent)
  .buildAndStart();

System.out.println("Server started on http://localhost:8080");
```

### Client Setup

```java
import com.phdsystems.agent.rest.AgentRestClient;

Agent remoteAgent = AgentRestClient.builder()
  .baseUrl("http://localhost:8080")
  .agentName("MyAgent")
  .build();

TaskRequest request = TaskRequest.of("MyAgent", "process data");
TaskResult result = remoteAgent.executeTask(request);

System.out.println(result.output());
```

## API Endpoints

### Execute Task

**POST** `/agents/{name}/execute`

Request body:

```json
{
  "task": "process this data"
}
```

Response:

```json
{
  "success": true,
  "agentName": "MyAgent",
  "task": "process this data",
  "output": "Processed result",
  "metadata": {},
  "executionTimeMs": 123
}
```

### Get Agent Info

**GET** `/agents/{name}/info`

Response:

```json
{
  "name": "MyAgent",
  "description": "My agent description",
  "capabilities": ["capability1", "capability2"]
}
```

### List All Agents

**GET** `/agents`

Response:

```json
{
  "Agent1": "Description of Agent1",
  "Agent2": "Description of Agent2"
}
```

## Features

- **RESTful design** - Standard HTTP methods and status codes
- **JSON format** - Easy to parse and debug
- **Multiple agents** - Host multiple agents on single port
- **Auto-discovery** - List all available agents
- **Transparent client** - Client implements Agent interface
- **Lightweight** - Minimal dependencies using Javalin

## Examples

### Multi-Agent Server

```java
Agent calculatorAgent = new CalculatorAgent();
Agent weatherAgent = new WeatherAgent();
Agent analysisAgent = new DataAnalysisAgent();

AgentRestServer server = AgentRestServer.builder()
  .port(8080)
  .addAgent(calculatorAgent)
  .addAgent(weatherAgent)
  .addAgent(analysisAgent)
  .buildAndStart();
```

### Using curl

```bash
# Execute task
curl -X POST http://localhost:8080/agents/CalculatorAgent/execute \
  -H "Content-Type: application/json" \
  -d '{"task": "2 + 2"}'

# Get agent info
curl http://localhost:8080/agents/CalculatorAgent/info

# List all agents
curl http://localhost:8080/agents
```

### Using REST Client

```java
Agent remoteCalculator = AgentRestClient.builder()
  .baseUrl("http://localhost:8080")
  .agentName("CalculatorAgent")
  .build();

TaskResult result = remoteCalculator.executeTask(
  TaskRequest.of("CalculatorAgent", "2 + 2")
);

System.out.println("Result: " + result.output());
```

### Error Handling

```java
Agent remoteAgent = AgentRestClient.builder()
  .baseUrl("http://localhost:8080")
  .agentName("MyAgent")
  .build();

TaskResult result = remoteAgent.executeTask(request);

if (!result.success()) {
  System.err.println("Task failed: " + result.output());
} else {
  System.out.println("Success: " + result.output());
}
```

## Architecture

```
┌─────────────┐      HTTP/JSON      ┌─────────────┐
│   Client    │ ──────────────────► │   Server    │
│             │                      │             │
│ AgentRest   │  POST /agents/*/exec │ AgentRest   │
│ Client      │ ◄──────────────────  │ Server      │
│             │                      │  (Javalin)  │
│ (implements │                      │             │
│  Agent)     │                      │ (delegates  │
└─────────────┘                      │  to Agent)  │
                                     └─────────────┘
                                            │
                                            ▼
                                     ┌─────────────┐
                                     │ Local Agent │
                                     │ (MyAgent)   │
                                     └─────────────┘
```

## Use Cases

- **Web APIs** - Expose agents as web services
- **HTTP-based systems** - Integration with existing HTTP infrastructure
- **Browser integration** - Call agents from JavaScript/web apps
- **Simple microservices** - Lightweight alternative to gRPC
- **API gateways** - Front agents with nginx/traefik

## Configuration

### Server Configuration

```java
AgentRestServer server = AgentRestServer.builder()
  .port(8080)                    // Custom port (default: 8080)
  .addAgent(agent1)              // Add agents
  .addAgent(agent2)
  .build();                       // Build without starting

// OR

AgentRestServer server = AgentRestServer.builder()
  .port(8080)
  .addAgent(agent1)
  .buildAndStart();              // Build and start immediately
```

### Client Configuration

```java
AgentRestClient client = AgentRestClient.builder()
  .baseUrl("http://api.example.com")  // Server URL (default: http://localhost:8080)
  .agentName("MyAgent")                // Agent to connect to
  .build();
```

## Best Practices

1. **Error responses** - Client converts HTTP errors to failed TaskResult
2. **Timeouts** - Configure HTTP client timeouts for long-running tasks
3. **CORS** - Configure CORS if calling from browser
4. **Authentication** - Add authentication middleware for production
5. **HTTPS** - Use HTTPS in production environments

## Comparison: REST vs gRPC

|     Feature     |         REST         |       gRPC        |
|-----------------|----------------------|-------------------|
| Protocol        | HTTP/JSON            | HTTP/2 + Protobuf |
| Performance     | Good                 | Better            |
| Browser support | Native               | Requires proxy    |
| Debugging       | Easy (curl, browser) | Requires tools    |
| Overhead        | Higher (JSON)        | Lower (binary)    |
| Streaming       | Limited              | Full support      |
| Best for        | Web integration      | Backend services  |

## Future Enhancements

- Authentication middleware
- CORS configuration
- Rate limiting
- Request validation
- OpenAPI/Swagger documentation
- Server-Sent Events for streaming
- WebSocket support

## License

Apache License 2.0
