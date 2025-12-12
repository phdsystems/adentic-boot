package examples.llm.integration;

import dev.engineeringlab.adentic.boot.AgenticApplication;
import dev.engineeringlab.adentic.boot.annotations.AgenticBootApplication;
import dev.engineeringlab.adentic.boot.annotations.RestController;
import dev.engineeringlab.adentic.boot.registry.ProviderRegistry;
import dev.engineeringlab.adentic.boot.web.annotations.GetMapping;
import dev.engineeringlab.adentic.boot.web.annotations.PostMapping;
import dev.engineeringlab.adentic.boot.web.annotations.RequestBody;
import dev.engineeringlab.adentic.boot.web.annotations.RequestParam;
import dev.engineeringlab.llm.provider.OpenAICompatibleTextGenerationProvider;
import dev.engineeringlab.llm.text.TextGenerationProvider;
import dev.engineeringlab.llm.text.model.TextGenerationRequest;
import dev.engineeringlab.llm.text.model.TextGenerationResponse;
import jakarta.inject.Inject;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Example application demonstrating LLM integration with AgenticBoot.
 *
 * <p>This example shows:
 *
 * <ul>
 *   <li>TextGenerationProvider from engineeringlab-llm module
 *   <li>OpenAI-compatible API (works with OpenAI, Ollama, vLLM, etc.)
 *   <li>REST endpoints for text generation
 *   <li>Reactive responses with Project Reactor
 * </ul>
 *
 * <h2>Setup</h2>
 *
 * <p>Set environment variables:
 *
 * <pre>{@code
 * # For OpenAI
 * export OPENAI_API_KEY="sk-..."
 * export OPENAI_BASE_URL="https://api.openai.com/v1"  # optional
 * export OPENAI_MODEL="gpt-4"  # optional
 *
 * # For Ollama (local)
 * export OPENAI_BASE_URL="http://localhost:11434/v1"
 * export OPENAI_MODEL="llama3.1"
 * }</pre>
 *
 * <h2>Run</h2>
 *
 * <pre>{@code
 * cd adentic-boot
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
 * # Full generation
 * curl -X POST http://localhost:8080/api/llm/generate \
 *   -H "Content-Type: application/json" \
 *   -d '{"prompt":"Explain AI in one sentence"}'
 * }</pre>
 */
@AgenticBootApplication(port = 8080, scanBasePackages = "examples.llm.integration")
public class OpenAIExample {

  public static void main(String[] args) {
    AgenticApplication.run(OpenAIExample.class, args);
  }

  /** REST controller for LLM interactions. */
  @Slf4j
  @RestController
  public static class LLMController {

    @Inject private ProviderRegistry registry;

    private TextGenerationProvider llmProvider;

    /** Initialize the LLM provider on first request. */
    private TextGenerationProvider getProvider() {
      if (llmProvider == null) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        String baseUrl =
            System.getenv().getOrDefault("OPENAI_BASE_URL", "https://api.openai.com/v1");
        String model = System.getenv().getOrDefault("OPENAI_MODEL", "gpt-4");

        if (apiKey == null || apiKey.isEmpty()) {
          throw new IllegalStateException(
              "OPENAI_API_KEY environment variable not set. "
                  + "Set it to use OpenAI, or set OPENAI_BASE_URL for Ollama/vLLM.");
        }

        llmProvider = new OpenAICompatibleTextGenerationProvider(apiKey, baseUrl, model);
        log.info("Initialized LLM provider: {} with model {}", baseUrl, model);

        // Register in ProviderRegistry
        registry.registerProvider("llm", "openai", llmProvider);
      }
      return llmProvider;
    }

    /**
     * Get LLM provider status.
     *
     * <p>Example: {@code curl http://localhost:8080/api/llm/status}
     */
    @GetMapping("/api/llm/status")
    public Mono<Map<String, Object>> getStatus() {
      return Mono.fromCallable(
          () -> {
            try {
              TextGenerationProvider provider = getProvider();
              return Map.of(
                  "status", "UP",
                  "provider", provider.getProviderName(),
                  "model", provider.getModel(),
                  "healthy", provider.isHealthy(),
                  "supportsStreaming", provider.supportsStreaming());
            } catch (Exception e) {
              return Map.of(
                  "status", "DOWN", "error", e.getMessage(), "hint", "Set OPENAI_API_KEY env var");
            }
          });
    }

    /**
     * Simple chat endpoint.
     *
     * <p>Example: {@code curl "http://localhost:8080/api/llm/chat?message=What is 2+2?"}
     */
    @GetMapping("/api/llm/chat")
    public Mono<Map<String, Object>> chat(@RequestParam("message") String message) {
      log.info("Chat request: {}", message);

      return Mono.fromCallable(() -> getProvider())
          .flatMap(
              provider -> {
                TextGenerationRequest request = TextGenerationRequest.simple(message);
                return provider.generate(request);
              })
          .map(
              response ->
                  Map.of(
                      "question", message,
                      "answer", response.content(),
                      "model", response.model(),
                      "tokens",
                          response.usage() != null ? response.usage().totalTokens() : 0,
                      "finishReason",
                          response.finishReason() != null
                              ? response.finishReason().name()
                              : "unknown"))
          .onErrorResume(
              error -> {
                log.error("Chat error", error);
                return Mono.just(Map.of("error", error.getMessage(), "question", message));
              });
    }

    /**
     * Full generation endpoint with custom parameters.
     *
     * <p>Example:
     *
     * <pre>{@code
     * curl -X POST http://localhost:8080/api/llm/generate \
     *   -H "Content-Type: application/json" \
     *   -d '{"prompt":"What is AI?","temperature":0.7,"maxTokens":100}'
     * }</pre>
     */
    @PostMapping("/api/llm/generate")
    public Mono<TextGenerationResponse> generate(@RequestBody Map<String, Object> params) {
      String prompt = (String) params.getOrDefault("prompt", "Hello");
      Double temperature = params.containsKey("temperature")
          ? ((Number) params.get("temperature")).doubleValue()
          : 0.7;
      Integer maxTokens = params.containsKey("maxTokens")
          ? ((Number) params.get("maxTokens")).intValue()
          : 500;

      log.info("Generation request: prompt='{}', temp={}, maxTokens={}", prompt, temperature, maxTokens);

      return Mono.fromCallable(() -> getProvider())
          .flatMap(
              provider -> {
                TextGenerationRequest request =
                    TextGenerationRequest.builder()
                        .prompt(prompt)
                        .temperature(temperature)
                        .maxTokens(maxTokens)
                        .build();
                return provider.generate(request);
              })
          .doOnSuccess(r -> log.info("Generated {} tokens", r.usage() != null ? r.usage().totalTokens() : 0))
          .doOnError(e -> log.error("Generation error", e));
    }

    /** Health check endpoint. */
    @GetMapping("/api/health")
    public Mono<Map<String, String>> health() {
      return Mono.just(Map.of("status", "UP", "service", "llm-example"));
    }
  }
}
