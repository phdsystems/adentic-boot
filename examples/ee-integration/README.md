# AgenticBoot + Adentic EE Integration Example

This example demonstrates how to integrate **Adentic Enterprise Edition (EE)** agents with **AgenticBoot** to build intelligent agentic applications.

---

## 📋 Overview

**What this example shows:**
- ✅ Manual registration of EE agents in `ProviderRegistry`
- ✅ Dependency injection of agents into REST controllers
- ✅ REST API for executing agent queries
- ✅ Reactive agent responses using Project Reactor (`Mono`)
- ✅ Error handling for agent execution failures

**Agents Used:**
- `SimpleAgent` - Basic LLM agent for question answering

---

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+
- AgenticBoot with `adentic-ee-core` dependency

### Run the Example

```bash
# From adentic-boot root directory
cd /home/developer/adentic-boot

# Compile
mvn clean compile

# Run the application
mvn exec:java -Dexec.mainClass="examples.ee.integration.SimpleAgentExample"
```

The application will start on `http://localhost:8080`.

---

## 📡 API Endpoints

### 1. Health Check

**Endpoint:** `GET /api/agent/status`

**Description:** Check if agents are registered and available

**Request:**
```bash
curl http://localhost:8080/api/agent/status
```

**Response:**
```json
{
  "available": true,
  "agentCount": 1,
  "message": "SimpleAgent is ready"
}
```

---

### 2. Ask Agent

**Endpoint:** `POST /api/agent/ask`

**Description:** Send a question to the SimpleAgent

**Request:**
```bash
curl -X POST http://localhost:8080/api/agent/ask \
  -H "Content-Type: application/json" \
  -d '{"question":"What is 2+2?"}'
```

**Response:**
```json
{
  "success": true,
  "answer": "2+2 equals 4",
  "model": "gpt-3.5-turbo",
  "tokensUsed": 15,
  "error": null
}
```

**Error Response:**
```json
{
  "success": false,
  "answer": null,
  "model": null,
  "tokensUsed": null,
  "error": "Agent execution failed: Connection timeout"
}
```

---

## 🏗️ Architecture

```
SimpleAgentExample (Main Application)
    ↓
AgenticBootApplication (Framework)
    ↓
AgentInitializationService (@Service)
    ↓ registers
ProviderRegistry (agent → SimpleAgent)
    ↑ retrieves
AgentController (@RestController)
    ↓ executes
SimpleAgent (EE Agent)
    ↓ uses
MockLLMClient (or real LLM client)
```

---

## 💻 Code Walkthrough

### 1. Main Application Class

```java
@Slf4j
@AgenticBootApplication
public class SimpleAgentExample {
  public static void main(String[] args) {
    AgenticApplication.run(SimpleAgentExample.class, args);
  }
}
```

**What it does:**
- Bootstraps AgenticBoot framework
- Scans for `@Service` and `@RestController` components
- Initializes dependency injection
- Starts HTTP server on port 8080

---

### 2. Agent Initialization Service

```java
@Service
class AgentInitializationService {
  @Inject
  public AgentInitializationService(ProviderRegistry registry) {
    this.registry = registry;
    initializeAgents();
  }

  private void initializeAgents() {
    // Create LLM client
    LLMClient llmClient = new MockLLMClient(LLMClientConfig.mock());

    // Configure agent
    AgentConfig config = AgentConfig.builder()
        .model("gpt-3.5-turbo")
        .systemPrompt("You are a helpful assistant...")
        .temperature(0.7)
        .maxTokens(500)
        .build();

    // Create and register SimpleAgent
    SimpleAgent simpleAgent = new SimpleAgent(llmClient, null, null, config);
    registry.registerAgent("simple", simpleAgent);
  }
}
```

**What it does:**
- Runs at application startup (via `@Service` + `@Inject`)
- Creates `SimpleAgent` with `MockLLMClient`
- Registers agent in `ProviderRegistry` under `"agent"` category with name `"simple"`

---

### 3. REST Controller

```java
@RestController
@RequestMapping("/api/agent")
class AgentController {
  @Inject
  public AgentController(ProviderRegistry registry) {
    this.registry = registry;
  }

  @PostMapping("/ask")
  public Mono<AgentResponse> ask(@RequestBody QuestionRequest request) {
    // Get agent from registry
    Agent agent = registry.<Agent>getAgent("simple")
        .orElseThrow(() -> new RuntimeException("SimpleAgent not found"));

    // Execute agent
    return agent.execute(AgentRequest.of(request.getQuestion()))
        .map(result -> AgentResponse.builder()
            .answer(result.getAnswer())
            .model(result.getMetadata().getModel())
            .tokensUsed(result.getMetadata().getTotalTokens())
            .success(true)
            .build())
        .onErrorResume(error -> Mono.just(
            AgentResponse.builder()
                .success(false)
                .error(error.getMessage())
                .build()
        ));
  }
}
```

**What it does:**
- Retrieves `SimpleAgent` from registry
- Executes agent with user question
- Returns reactive `Mono<AgentResponse>`
- Handles errors gracefully

---

## 🔄 Request/Response Flow

```
1. User sends POST /api/agent/ask {"question":"..."}
    ↓
2. AgentController receives request
    ↓
3. Controller gets SimpleAgent from ProviderRegistry
    ↓
4. Agent.execute(AgentRequest.of(question))
    ↓
5. SimpleAgent → LLMClient → MockLLMClient
    ↓
6. LLMClient returns completion
    ↓
7. Agent returns AgentResult
    ↓
8. Controller maps to AgentResponse
    ↓
9. HTTP 200 + JSON response
```

---

## 🔧 Customization

### Using Real LLM Client

Replace `MockLLMClient` with a real client:

```java
// OpenAI
LLMClient llmClient = new OpenAIClient(
    LLMClientConfig.openAI(System.getenv("OPENAI_API_KEY"))
);

// Anthropic
LLMClient llmClient = new AnthropicClient(
    LLMClientConfig.anthropic(System.getenv("ANTHROPIC_API_KEY"))
);
```

### Adding More Agents

Register additional agents:

```java
// ReActAgent
ReActAgent reactAgent = new ReActAgent(llmClient, null, toolRegistry, config);
registry.registerAgent("react", reactAgent);

// ChainOfThoughtAgent
ChainOfThoughtAgent cotAgent = new ChainOfThoughtAgent(llmClient, null, null, config);
registry.registerAgent("cot", cotAgent);
```

### Adding Tools

```java
// Create ToolRegistry
ToolRegistry toolRegistry = new SimpleToolRegistry();

// Register tools
toolRegistry.register(new CalculatorTool()).block();
toolRegistry.register(new FileTool()).block();

// Pass toolRegistry to agents that support tools
FunctionCallingAgent functionAgent =
    new FunctionCallingAgent(llmClient, null, toolRegistry, config);
registry.registerAgent("function-calling", functionAgent);
```

---

## 🧪 Testing

### Unit Test Example

```java
@Test
void shouldRegisterAndRetrieveAgent() {
  ProviderRegistry registry = new ProviderRegistry();
  SimpleAgent agent = mock(SimpleAgent.class);

  registry.registerAgent("simple", agent);

  Optional<SimpleAgent> retrieved = registry.getAgent("simple");
  assertThat(retrieved).isPresent();
}
```

### Integration Test

```bash
# Start application
mvn exec:java -Dexec.mainClass="examples.ee.integration.SimpleAgentExample"

# Test status endpoint
curl http://localhost:8080/api/agent/status

# Test ask endpoint
curl -X POST http://localhost:8080/api/agent/ask \
  -H "Content-Type: application/json" \
  -d '{"question":"Hello!"}'
```

---

## 📚 Related Documentation

- [AgenticBoot EE Integration Design](../../AGENTICBOOT_EE_INTEGRATION.md)
- [Adentic Framework README](../../../adentic-framework/README.md)
- [Adentic EE README](../../../adentic-framework/adentic-ee/README.md)
- [SimpleAgent JavaDoc](../../../adentic-framework/adentic-ee/adentic-ee-core/src/main/java/dev/adeengineer/ee/llm/agent/SimpleAgent.java)

---

## 🐛 Troubleshooting

### Agent Not Found

**Error:** `SimpleAgent not found in registry`

**Solution:**
- Check `AgentInitializationService` is annotated with `@Service`
- Verify agent registration in logs: `INFO - Registered EE agent: simple`
- Enable debug logging: `logging.level.dev.adeengineer=DEBUG`

### Compilation Errors

**Error:** `cannot find symbol: class SimpleAgent`

**Solution:**
- Ensure `adentic-ee-core` dependency is in `pom.xml`
- Run `mvn clean install` in adentic-framework first
- Check local Maven repository: `~/.m2/repository/dev/adeengineer/ee/adentic-ee-core/`

---

## 🎓 Next Steps

1. **Add More Agents** - Try ReActAgent, ChainOfThoughtAgent
2. **Add Tools** - Integrate Calculator, File, Shell tools
3. **Use Real LLM** - Connect to OpenAI or Anthropic
4. **Add Memory** - Implement conversation history
5. **Add Observability** - Log agent executions, token usage

---

**Last Updated:** 2025-11-06
**Version:** 1.0
**Author:** PHD Systems Engineering Team
