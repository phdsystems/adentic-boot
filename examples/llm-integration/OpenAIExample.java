package examples.llm.integration;

import dev.adeengineer.adentic.boot.AgenticApplication;
import dev.adeengineer.adentic.boot.annotations.AgenticBootApplication;
import dev.adeengineer.adentic.boot.annotations.GetMapping;
import dev.adeengineer.adentic.boot.annotations.PostMapping;
import dev.adeengineer.adentic.boot.annotations.RequestBody;
import dev.adeengineer.adentic.boot.annotations.RequestParam;
import dev.adeengineer.adentic.boot.annotations.RestController;
import dev.adeengineer.adentic.boot.provider.llm.OpenAILLMProvider;
import dev.adeengineer.adentic.boot.registry.ProviderRegistry;
import dev.adeengineer.ai.client.BaseLLMClient;
import dev.adeengineer.ai.model.CompletionRequest;
import dev.adeengineer.ai.model.CompletionResult;
import dev.adeengineer.ai.model.common.Message;
import dev.adeengineer.ai.model.common.Role;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Example application demonstrating real OpenAI integration with AgenticBoot.
 *
 * <p>This example shows:
 *
 * <ul>
 *   <li>Auto-discovery and registration of OpenAI LLM provider
 *   <li>Dependency injection of ProviderRegistry
 *   <li>REST endpoints for chat completions
 *   <li>Reactive responses with Project Reactor
 *   <li>Error handling for LLM calls
 * </ul>
 *
 * <h2>Setup</h2>
 *
 * <p>Set environment variable:
 *
 * <pre>{@code
 * export OPENAI_API_KEY="sk-..."
 * }</pre>
 *
 * <h2>Run</h2>
 *
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="examples.llm.integration.OpenAIExample"
 * }</pre>
 *
 * <h2>Test Endpoints</h2>
 *
 * <pre>{@code
 * # Check provider status
 * curl http://localhost:8080/api/llm/status
 *
 * # Simple chat completion
 * curl http://localhost:8080/api/llm/chat?message=What%20is%202+2?
 *
 * # Chat completion with JSON body
 * curl -X POST http://localhost:8080/api/llm/complete \
 *   -H "Content-Type: application/json" \
 *   -d '{"messages":[{"role":"user","content":"Explain quantum computing in one sentence"}]}'
 * }</pre>
 */
@AgenticBootApplication(port = 8080, scanBasePackages = "examples.llm.integration")
public class OpenAIExample {

  public static void main(String[] args) {
    AgenticApplication.run(OpenAIExample.class, args);
  }

  /**
   * REST controller for LLM interactions.
   *
   * <p>Provides endpoints for:
   *
   * <ul>
   *   <li>GET /api/llm/status - Provider status
   *   <li>GET /api/llm/chat - Simple chat endpoint
   *   <li>POST /api/llm/complete - Chat completion with full control
   * </ul>
   */
  @Slf4j
  @RestController
  public static class LLMController {

    @Inject private ProviderRegistry registry;

    /**
     * Get LLM provider status.
     *
     * <p>Example:
     *
     * <pre>{@code
     * curl http://localhost:8080/api/llm/status
     * }</pre>
     *
     * @return status information
     */
    @GetMapping("/api/llm/status")
    public Mono<Map<String, Object>> getStatus() {
      return Mono.fromCallable(
          () -> {
            OpenAILLMProvider provider =
                registry
                    .<OpenAILLMProvider>getProvider("openai", "text-generation")
                    .orElseThrow(() -> new IllegalStateException("OpenAI provider not found"));

            return Map.of(
                "provider", "openai",
                "type", "text-generation",
                "ready", provider.isReady(),
                "model", provider.getClient().getDefaultModel(),
                "message",
                    provider.isReady()
                        ? "OpenAI provider is ready"
                        : "OPENAI_API_KEY not configured");
          });
    }

    /**
     * Simple chat endpoint.
     *
     * <p>Example:
     *
     * <pre>{@code
     * curl "http://localhost:8080/api/llm/chat?message=What is 2+2?"
     * }</pre>
     *
     * @param message user message
     * @return AI response
     */
    @GetMapping("/api/llm/chat")
    public Mono<Map<String, String>> chat(@RequestParam("message") String message) {
      log.info("Received chat request: {}", message);

      return getProvider()
          .flatMap(provider -> provider.getClient().connect())
          .flatMap(
              _ -> {
                OpenAILLMProvider provider =
                    registry
                        .<OpenAILLMProvider>getProvider("openai", "text-generation")
                        .orElseThrow();

                CompletionRequest request =
                    CompletionRequest.builder()
                        .messages(List.of(Message.of(Role.USER, message)))
                        .build();

                return provider.getClient().complete(request);
              })
          .map(
              result ->
                  Map.of(
                      "question", message,
                      "answer", result.getContent(),
                      "model", result.getModel(),
                      "tokensUsed", String.valueOf(result.getTotalTokens())))
          .doOnError(error -> log.error("Error during chat completion", error))
          .onErrorResume(
              error ->
                  Mono.just(
                      Map.of(
                          "error", error.getMessage(),
                          "message", "Failed to get response from OpenAI")));
    }

    /**
     * Chat completion with full request body.
     *
     * <p>Example:
     *
     * <pre>{@code
     * curl -X POST http://localhost:8080/api/llm/complete \
     *   -H "Content-Type: application/json" \
     *   -d '{
     *     "messages": [
     *       {"role": "system", "content": "You are a helpful assistant."},
     *       {"role": "user", "content": "What is quantum computing?"}
     *     ],
     *     "temperature": 0.7,
     *     "maxTokens": 200
     *   }'
     * }</pre>
     *
     * @param request completion request
     * @return completion result
     */
    @PostMapping("/api/llm/complete")
    public Mono<CompletionResult> complete(@RequestBody CompletionRequest request) {
      log.info("Received completion request with {} messages", request.getMessages().size());

      return getProvider()
          .flatMap(provider -> provider.getClient().connect())
          .flatMap(
              _ -> {
                OpenAILLMProvider provider =
                    registry
                        .<OpenAILLMProvider>getProvider("openai", "text-generation")
                        .orElseThrow();

                return provider.getClient().complete(request);
              })
          .doOnSuccess(result -> log.info("Completion successful: {} tokens", result.getTotalTokens()))
          .doOnError(error -> log.error("Error during completion", error));
    }

    /**
     * Get OpenAI provider from registry.
     *
     * @return OpenAI provider
     */
    private Mono<OpenAILLMProvider> getProvider() {
      return Mono.fromCallable(
          () ->
              registry
                  .<OpenAILLMProvider>getProvider("openai", "text-generation")
                  .orElseThrow(() -> new IllegalStateException("OpenAI provider not registered")));
    }
  }
}
