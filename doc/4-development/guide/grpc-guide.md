# ADE Agent gRPC

gRPC protocol implementation for remote agent execution.

**Example Code:** This integration module is demonstrated through inline code examples below. See also [AgentGrpcBuilderTest.java](../../ade-grpc/src/test/java/com/phdsystems/agent/grpc/AgentGrpcBuilderTest.java) for test examples.

## Overview

This module enables agents to be exposed and accessed over gRPC:
- Remote agent execution
- Distributed agent architectures
- Language-agnostic client support (via protobuf)
- High-performance binary protocol
- Support for streaming (future)

## Installation

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-grpc</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

### Server Setup

```java
import com.phdsystems.agent.grpc.AgentGrpcServer;

Agent myAgent = new MyAgent();

AgentGrpcServer server = AgentGrpcServer.builder()
  .port(9090)
  .addAgent(myAgent)
  .build();

server.start();
System.out.println("Server started on port 9090");

server.blockUntilShutdown();
```

### Client Setup

```java
import com.phdsystems.agent.grpc.AgentGrpcClient;

Agent remoteAgent = AgentGrpcClient.builder()
  .host("localhost")
  .port(9090)
  .agentName("MyAgent")
  .build();

TaskRequest request = TaskRequest.of("MyAgent", "process data");
TaskResult result = remoteAgent.executeTask(request);

System.out.println(result.output());

remoteAgent.shutdown();
```

## Features

- **Transparent remote execution** - gRPC client implements Agent interface
- **Multiple agents per server** - Host multiple agents on single port
- **Agent info queries** - Remote capability discovery
- **Builder pattern** - Fluent configuration API
- **Protocol buffers** - Efficient binary serialization
- **Streaming support** - StreamingAgent integration for progressive results

## Protocol Definition

```protobuf
service AgentService {
  rpc ExecuteTask(TaskRequest) returns (TaskResult);
  rpc GetAgentInfo(AgentInfoRequest) returns (AgentInfo);
  rpc StreamTask(TaskRequest) returns (stream TaskChunk);
}

message TaskRequest {
  string agent_name = 1;
  string task = 2;
}

message TaskResult {
  bool success = 1;
  string agent_name = 2;
  string task = 3;
  string output = 4;
  map<string, string> metadata = 5;
  int64 execution_time_ms = 6;
}
```

## Examples

### Multi-Agent Server

```java
Agent calculatorAgent = new CalculatorAgent();
Agent weatherAgent = new WeatherAgent();
Agent analysisAgent = new DataAnalysisAgent();

AgentGrpcServer server = AgentGrpcServer.builder()
  .port(9090)
  .addAgent(calculatorAgent)
  .addAgent(weatherAgent)
  .addAgent(analysisAgent)
  .build();

server.start();
```

### Remote Agent Usage

```java
Agent remoteCalculator = AgentGrpcClient.builder()
  .host("agent-server.example.com")
  .port(9090)
  .agentName("CalculatorAgent")
  .build();

// Use exactly like local agent
TaskResult result = remoteCalculator.executeTask(
  TaskRequest.of("CalculatorAgent", "2 + 2")
);

System.out.println(result.output());

remoteCalculator.shutdown();
```

### Capability Discovery

```java
Agent remoteAgent = AgentGrpcClient.builder()
  .host("localhost")
  .port(9090)
  .agentName("MyAgent")
  .build();

AgentInfo info = remoteAgent.getAgentInfo();

System.out.println("Name: " + info.name());
System.out.println("Description: " + info.description());
System.out.println("Capabilities: " + info.capabilities());
```

## Architecture

```
┌─────────────┐         gRPC          ┌─────────────┐
│   Client    │ ───────────────────► │   Server    │
│             │                       │             │
│ AgentGrpc   │  TaskRequest/Result  │ AgentGrpc   │
│ Client      │ ◄─────────────────── │ Server      │
│             │                       │             │
│ (implements │                       │ (delegates  │
│  Agent)     │                       │  to Agent)  │
└─────────────┘                       └─────────────┘
                                             │
                                             ▼
                                      ┌─────────────┐
                                      │ Local Agent │
                                      │ (MyAgent)   │
                                      └─────────────┘
```

## Use Cases

- **Microservices** - Expose agents as microservices
- **Distributed systems** - Agents across multiple machines
- **Language interop** - Call Java agents from Python/Go/etc
- **Load balancing** - Distribute agent execution
- **Service mesh** - Integrate with Kubernetes/Istio

## Configuration

### Server Configuration

```java
AgentGrpcServer server = AgentGrpcServer.builder()
  .port(9090)                    // Custom port (default: 9090)
  .addAgent(agent1)              // Add agents
  .addAgent(agent2)
  .build();
```

### Client Configuration

```java
AgentGrpcClient client = AgentGrpcClient.builder()
  .host("agent-server.com")      // Server host (default: localhost)
  .port(9090)                    // Server port (default: 9090)
  .agentName("MyAgent")          // Agent to connect to
  .build();
```

## Best Practices

1. **Resource cleanup** - Always call `shutdown()` on clients
2. **Error handling** - Handle `StatusRuntimeException` for network errors
3. **Connection pooling** - Reuse channels for multiple agents
4. **Timeouts** - Configure appropriate timeouts for long-running tasks
5. **Security** - Use TLS in production environments

## Future Enhancements

- TLS/SSL support
- Authentication and authorization
- Streaming task execution
- Bidirectional streaming
- Connection pooling
- Circuit breaker integration
- Load balancing

## License

Apache License 2.0
