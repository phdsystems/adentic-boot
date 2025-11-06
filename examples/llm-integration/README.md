# OpenAI LLM Integration Example

This example demonstrates real OpenAI integration with AgenticBoot using `adentic-ai-client`.

## Features

- ✅ Auto-discovery and registration of OpenAI LLM provider
- ✅ Dependency injection of ProviderRegistry
- ✅ REST endpoints for chat completions
- ✅ Reactive responses with Project Reactor (`Mono<T>`)
- ✅ Error handling for LLM API calls
- ✅ Environment-based configuration
- ✅ Real GPT-4/GPT-3.5-turbo integration

## Prerequisites

1. **OpenAI API Key**: Get one from [OpenAI Platform](https://platform.openai.com/api-keys)
2. **Java 21**: Required for AgenticBoot
3. **Maven**: For building and running

## Setup

### 1. Set Environment Variable

```bash
export OPENAI_API_KEY="sk-proj-..."
```

Optional environment variables:

```bash
export OPENAI_BASE_URL="https://api.openai.com/v1"  # Custom base URL (optional)
export OPENAI_MODEL="gpt-4"                          # Default model (optional, default: gpt-4)
```

### 2. Build Project

```bash
cd /home/developer/adentic-boot
mvn clean compile
```

### 3. Run Example

```bash
mvn exec:java -Dexec.mainClass="examples.llm.integration.OpenAIExample"
```

You should see output like:

```
 █████╗  ██████╗ ███████╗███╗   ██╗████████╗██╗ ██████╗
██╔══██╗██╔════╝ ██╔════╝████╗  ██║╚══██╔══╝██║██╔════╝
███████║██║  ███╗█████╗  ██╔██╗ ██║   ██║   ██║██║
██╔══██║██║   ██║██╔══╝  ██║╚██╗██║   ██║   ██║██║
██║  ██║╚██████╔╝███████╗██║ ╚████║   ██║   ██║╚██████╗
╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚═╝  ╚═══╝   ╚═╝   ╚═╝ ╚═════╝
██████╗  ██████╗  ██████╗ ████████╗
██╔══██╗██╔═══██╗██╔═══██╗╚══██╔══╝
██████╔╝██║   ██║██║   ██║   ██║
██╔══██╗██║   ██║██║   ██║   ██║
██████╔╝╚██████╔╝╚██████╔╝   ██║
╚═════╝  ╚═════╝  ╚═════╝    ╚═╝

:: AgenticBoot ::        (v1.0.0)

INFO  Starting AgenticBoot application: OpenAIExample
INFO  Initializing OpenAI LLM provider
INFO  OpenAI LLM provider initialized with model: gpt-4
INFO  Registered 1 providers across 1 categories
INFO  Server started on port 8080
INFO  AgenticBoot application started in 523ms
```

## API Endpoints

### 1. Check Provider Status

```bash
curl http://localhost:8080/api/llm/status
```

**Response:**

```json
{
  "provider": "openai",
  "type": "text-generation",
  "ready": true,
  "model": "gpt-4",
  "message": "OpenAI provider is ready"
}
```

### 2. Simple Chat (GET)

```bash
curl "http://localhost:8080/api/llm/chat?message=What%20is%202+2?"
```

**Response:**

```json
{
  "question": "What is 2+2?",
  "answer": "2 + 2 equals 4.",
  "model": "gpt-4",
  "tokensUsed": "25"
}
```

### 3. Chat Completion (POST)

```bash
curl -X POST http://localhost:8080/api/llm/complete \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {"role": "system", "content": "You are a helpful assistant."},
      {"role": "user", "content": "Explain quantum computing in one sentence"}
    ],
    "temperature": 0.7,
    "maxTokens": 200
  }'
```

**Response:**

```json
{
  "content": "Quantum computing uses quantum bits (qubits) that can exist in multiple states simultaneously, enabling parallel processing of information far beyond classical computers.",
  "model": "gpt-4",
  "finishReason": "STOP",
  "totalTokens": 45,
  "promptTokens": 28,
  "completionTokens": 17
}
```

## How It Works

### 1. Provider Registration

The `OpenAILLMProvider` is automatically discovered and registered:

```java
@Service
@TextGenerationProvider(
    name = "openai",
    model = "gpt-4",
    description = "OpenAI GPT models",
    supportsStreaming = true,
    maxTokens = 128000,
    contextWindow = 128000,
    isLocal = false,
    priority = 10,
    enabledByDefault = true)
public class OpenAILLMProvider {
  @Getter private final OpenAIClient client;

  public OpenAILLMProvider() {
    // Initialize with environment config
    LLMClientConfig config = LLMClientConfig.builder()
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .defaultModel("gpt-4")
        .build();

    this.client = new OpenAIClient(config);
  }
}
```

### 2. Dependency Injection

The controller gets the provider via ProviderRegistry:

```java
@RestController
public class LLMController {
  @Inject
  private ProviderRegistry registry;

  @GetMapping("/api/llm/chat")
  public Mono<Map<String, String>> chat(@RequestParam("message") String message) {
    // Get provider from registry
    OpenAILLMProvider provider = registry
        .<OpenAILLMProvider>getProvider("openai", "text-generation")
        .orElseThrow();

    // Make LLM call
    return provider.getClient().complete(request)
        .map(result -> Map.of("answer", result.getContent()));
  }
}
```

### 3. Reactive LLM Calls

All LLM calls return `Mono<T>` for non-blocking I/O:

```java
CompletionRequest request = CompletionRequest.builder()
    .messages(List.of(Message.of(Role.USER, "Hello!")))
    .build();

Mono<CompletionResult> result = client.complete(request);
```

## Customization

### Change Default Model

```java
export OPENAI_MODEL="gpt-3.5-turbo"  # Faster, cheaper
export OPENAI_MODEL="gpt-4-turbo"    # Latest GPT-4 Turbo
```

### Custom Base URL (for proxies or compatible APIs)

```bash
export OPENAI_BASE_URL="https://your-proxy.com/v1"
```

### Modify Request Parameters

```java
CompletionRequest request = CompletionRequest.builder()
    .messages(messages)
    .temperature(0.9)      // More creative (0.0-2.0)
    .maxTokens(500)        // Limit response length
    .topP(0.95)            // Nucleus sampling
    .build();
```

## Troubleshooting

### Error: "OPENAI_API_KEY not configured"

**Solution**: Set the environment variable before running:

```bash
export OPENAI_API_KEY="sk-..."
mvn exec:java -Dexec.mainClass="examples.llm.integration.OpenAIExample"
```

### Error: "OpenAI provider not found"

**Solution**: Check that the provider is being discovered. Look for this in logs:

```
INFO  Registered 1 providers across 1 categories
```

If not found, ensure:
- `OpenAILLMProvider` is in the scan path
- `@Service` and `@TextGenerationProvider` annotations are present

### Error: "Rate limit exceeded"

**Solution**: OpenAI has rate limits. Wait a few seconds and retry. Consider:
- Using `gpt-3.5-turbo` (higher rate limits)
- Upgrading your OpenAI tier
- Implementing retry logic with exponential backoff

### Error: "Invalid API key"

**Solution**: Verify your API key:
- Check it's not expired
- Ensure it starts with `sk-`
- Test it directly with OpenAI CLI or Postman

## Next Steps

- **Add streaming support**: Use `completeStream()` for Server-Sent Events
- **Add more providers**: Create `GeminiLLMProvider`, `LocalLLMProvider`
- **Add function calling**: Use OpenAI's function calling for tool use
- **Add embeddings**: Use `OpenAIClient.generateEmbeddings()`
- **Add caching**: Cache frequent requests to reduce API calls

## Architecture

```
AgenticBoot Application
  │
  ├─> ComponentScanner
  │     └─> Discovers @TextGenerationProvider classes
  │
  ├─> ProviderRegistry
  │     └─> Registers OpenAILLMProvider under "text-generation" category
  │
  ├─> OpenAILLMProvider (@Service + @TextGenerationProvider)
  │     └─> OpenAIClient (adentic-ai-client)
  │           └─> HTTP calls to api.openai.com
  │
  └─> LLMController (@RestController)
        └─> Injects ProviderRegistry
        └─> Calls OpenAIClient via provider
        └─> Returns Mono<T> reactive responses
```

## Related Examples

- `examples/ee-integration/` - EE Agent integration
- `examples/ee-integration/SimpleAgentExample.java` - SimpleAgent REST API

## References

- [OpenAI API Documentation](https://platform.openai.com/docs/api-reference)
- [AgenticBoot Documentation](../../README.md)
- [adentic-ai-client Source](https://github.com/phdsystems/adentic-framework/tree/main/adentic-ai-client)
- [Project Reactor Documentation](https://projectreactor.io/docs)

---

**Last Updated:** 2025-11-06
**AgenticBoot Version:** 1.0.0-SNAPSHOT
