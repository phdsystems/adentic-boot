package examples.llm.integration;

import dev.adeengineer.adentic.boot.AgenticApplication;
import dev.adeengineer.adentic.boot.annotations.AgenticBootApplication;
import dev.adeengineer.adentic.boot.annotations.GetMapping;
import dev.adeengineer.adentic.boot.annotations.PostMapping;
import dev.adeengineer.adentic.boot.annotations.RequestBody;
import dev.adeengineer.adentic.boot.annotations.RequestParam;
import dev.adeengineer.adentic.boot.annotations.RestController;
import dev.adeengineer.adentic.boot.registry.ProviderRegistry;
import dev.adeengineer.ai.model.CompletionRequest;
import dev.adeengineer.ai.model.CompletionResult;
import dev.adeengineer.ai.model.common.Message;
import dev.adeengineer.ai.model.common.Role;
import dev.adeengineer.ai.openai.OpenAIClient;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Example application demonstrating OpenAI integration with AgenticBoot using adentic-ai-client.
 *
 * <p>This example shows:
 *
 * <ul>
 *   <li>Direct use of OpenAIClient from adentic-ai-client
 *   <li>Auto-registration of LLM clients in ProviderRegistry
 *   <li>REST endpoints for chat completions
 *   <li>Reactive responses with Project Reactor
 *   <li>Metrics and health checks built-in
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
 * # Simple chat
 * curl "http://localhost:8080/api/llm/chat?message=What%20is%202+2?"
 *
 * # Full completion
 * curl -X POST http://localhost:8080/api/llm/complete \
 *   -H "Content-Type: application/json" \
 *   -d '{"messages":[{"role":"user","content":"Explain AI"}]}'
 * }</pre>
 */
@AgenticBootApplication(port = 8080, scanBasePackages = "examples.llm.integration")
public class OpenAIExample {

  public static void main(String[] args) {
    AgenticApplication.run(OpenAIExample.class, args);
  }

  /**
   * REST controller for LLM interactions using OpenAIClient directly.
   */
  @Slf4j
  @RestController
  public static class LLMController {

    @Inject private ProviderRegistry registry;

    /**
     * Get LLM provider status.
     *
     * <p>Example: {@code curl http://localhost:8080/api/llm/status}
     *
     * @return status information
     */
    @GetMapping("/api/llm/status")
    public Mono<Map<String, Object>> getStatus() {
      return Mono.fromCallable(
          () -> {
            var clientOpt = registry.<OpenAIClient>getProvider("openai", "llm");

            if (clientOpt.isEmpty()) {
              return Map.of(
                  "status", "DOWN",
                  "message", "OpenAI client not available (OPENAI_API_KEY not set)");
            }

            OpenAIClient client = clientOpt.get();
            return Map.of(
                "status", "UP",
                "provider", "openai",
                "type", "llm",
                "connected", client.isConnected(),
                "message", "OpenAI client ready");
          });
    }

    /**
     * Simple chat endpoint.
     *
     * <p>Example: {@code curl "http://localhost:8080/api/llm/chat?message=What is 2+2?"}
     *
     * @param message user message
     * @return AI response
     */
    @GetMapping("/api/llm/chat")
    public Mono<Map<String, Object>> chat(@RequestParam("message") String message) {
      log.info("Chat request: {}", message);

      return getClient()
          .flatMap(client -> client.connect())
          .flatMap(
              _ -> {
                OpenAIClient client = getClient().block();
                CompletionRequest request =
                    CompletionRequest.builder()
                        .messages(List.of(Message.of(Role.USER, message)))
                        .build();

                return client.complete(request);
              })
          .map(
              result ->
                  Map.of(
                      "question", message,
                      "answer", result.getContent(),
                      "model", result.getModel(),
                      "tokens", result.getTotalTokens()))
          .onErrorResume(
              error -> {
                log.error("Chat error", error);
                return Mono.just(
                    Map.of(
                        "error", error.getMessage(),
                        "message", "Failed to get response from OpenAI"));
              });
    }

    /**
     * Full completion endpoint.
     *
     * <p>Example:
     *
     * <pre>{@code
     * curl -X POST http://localhost:8080/api/llm/complete \
     *   -H "Content-Type: application/json" \
     *   -d '{"messages":[{"role":"user","content":"What is AI?"}]}'
     * }</pre>
     *
     * @param request completion request
     * @return completion result
     */
    @PostMapping("/api/llm/complete")
    public Mono<CompletionResult> complete(@RequestBody CompletionRequest request) {
      log.info("Completion request: {} messages", request.getMessages().size());

      return getClient()
          .flatMap(client -> client.connect())
          .flatMap(
              _ -> {
                OpenAIClient client = getClient().block();
                return client.complete(request);
              })
          .doOnSuccess(result -> log.info("Completion: {} tokens", result.getTotalTokens()))
          .doOnError(error -> log.error("Completion error", error));
    }

    /**
     * Get OpenAI client from registry.
     *
     * @return OpenAI client
     */
    private Mono<OpenAIClient> getClient() {
      return Mono.fromCallable(
          () ->
              registry
                  .<OpenAIClient>getProvider("openai", "llm")
                  .orElseThrow(() -> new IllegalStateException("OpenAI client not available")));
    }
  }
}
