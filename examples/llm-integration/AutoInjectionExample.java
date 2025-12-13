package examples.llm.integration;

import dev.engineeringlab.adentic.boot.AgenticApplication;
import dev.engineeringlab.adentic.boot.annotations.AgenticBootApplication;
import dev.engineeringlab.adentic.boot.annotations.RestController;
import dev.engineeringlab.adentic.boot.context.AgenticContext;
import dev.engineeringlab.adentic.boot.web.annotations.GetMapping;
import dev.engineeringlab.adentic.boot.web.annotations.PostMapping;
import dev.engineeringlab.adentic.boot.web.annotations.RequestBody;
import dev.engineeringlab.common.annotation.Provider;
import dev.engineeringlab.llm.text.TextGenerationProvider;
import dev.engineeringlab.llm.text.model.TextGenerationRequest;
import dev.engineeringlab.llm.text.model.TextGenerationResponse;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Example application demonstrating automatic provider injection using the @Provider annotation.
 *
 * <p>This example showcases true auto-injection where providers are automatically injected into
 * {@code @Provider} annotated fields by AgenticBoot's bean lifecycle - no manual code required.
 *
 * <h2>Auto-Injection Pattern</h2>
 *
 * <pre>{@code
 * @RestController
 * public class MyController {
 *   @Provider
 *   private TextGenerationProvider provider;  // Automatically injected!
 *
 *   // No constructor needed - AgenticBoot handles injection
 * }
 * }</pre>
 *
 * <p>The {@code @Provider} annotation marks fields for automatic injection. When AgenticBoot
 * creates the bean, it automatically:
 * <ol>
 *   <li>Scans for fields annotated with @Provider</li>
 *   <li>Discovers providers via ServiceLoader</li>
 *   <li>Injects the highest priority provider into the field</li>
 * </ol>
 *
 * <h2>Setup</h2>
 *
 * <pre>{@code
 * # For OpenAI
 * export OPENAI_API_KEY="sk-..."
 * export OPENAI_MODEL="gpt-4-turbo-preview"  # Optional
 *
 * # For Anthropic
 * export ANTHROPIC_API_KEY="sk-ant-..."
 * export ANTHROPIC_MODEL="claude-3-sonnet-20240229"  # Optional
 *
 * # For Ollama (local)
 * export OLLAMA_MODEL="llama3.1"  # Optional, defaults to llama3.1
 * }</pre>
 *
 * <h2>Run</h2>
 *
 * <pre>{@code
 * cd adentic-boot
 * ./mvnw compile exec:java -Dexec.mainClass="examples.llm.integration.AutoInjectionExample"
 * }</pre>
 *
 * <h2>Test</h2>
 *
 * <pre>{@code
 * curl http://localhost:8080/api/health
 * curl http://localhost:8080/api/status
 * curl -X POST http://localhost:8080/api/chat \
 *   -H "Content-Type: application/json" \
 *   -d '{"message":"Explain dependency injection in one sentence"}'
 * }</pre>
 */
@AgenticBootApplication(port = 8080, scanBasePackages = "examples.llm.integration")
public class AutoInjectionExample {

  private static AgenticContext context;

  public static void main(String[] args) {
    context = AgenticApplication.run(AutoInjectionExample.class, args);
  }

  public static AgenticContext getContext() {
    return context;
  }

  /**
   * REST controller demonstrating automatic provider injection.
   *
   * <p>The TextGenerationProvider is automatically injected via the @Provider annotation
   * when AgenticBoot creates this controller. No manual injection code required.
   */
  @Slf4j
  @RestController
  public static class AutoInjectionController {

    /**
     * Auto-injected text generation provider.
     *
     * <p>This field is automatically populated by AgenticBoot's bean lifecycle.
     * The framework calls Providers.inject() after instantiation.
     */
    @Provider
    private TextGenerationProvider provider;

    @GetMapping("/api/health")
    public Map<String, String> health() {
      return Map.of("status", "UP", "service", "auto-injection-example");
    }

    @GetMapping("/api/status")
    public Map<String, Object> getStatus() {
      try {
        return Map.of(
            "status", "UP",
            "provider", provider.getProviderName(),
            "model", provider.getModel(),
            "healthy", provider.isHealthy(),
            "injectionMethod", "auto-injection with @Provider annotation");
      } catch (Exception e) {
        log.error("Status check failed", e);
        return Map.of("status", "DOWN", "error", e.getMessage());
      }
    }

    /**
     * Chat endpoint demonstrating usage of auto-injected provider.
     *
     * <p>The provider is already initialized and ready to use - no manual setup required.
     *
     * @param body Request body containing the message to send
     * @return Mono containing the chat response
     */
    @PostMapping("/api/chat")
    public Mono<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
      String message = (String) body.getOrDefault("message", "Hello");
      log.info("Chat request: {}", message);

      TextGenerationRequest request = TextGenerationRequest.simple(message);

      return provider.generate(request)
          .map(response -> Map.<String, Object>of(
              "question", message,
              "answer", response.content(),
              "model", response.model(),
              "provider", provider.getProviderName()))
          .onErrorResume(error -> {
            log.error("Chat error", error);
            return Mono.just(Map.of("error", error.getMessage()));
          });
    }

    /**
     * Generate endpoint with configurable parameters.
     *
     * @param params Request parameters including prompt and optional generation settings
     * @return Mono containing the full TextGenerationResponse
     */
    @PostMapping("/api/generate")
    public Mono<TextGenerationResponse> generate(@RequestBody Map<String, Object> params) {
      String prompt = (String) params.getOrDefault("prompt", "Hello");
      log.info("Generate request: {}", prompt);

      TextGenerationRequest request = TextGenerationRequest.builder()
          .prompt(prompt)
          .build();

      return provider.generate(request);
    }
  }
}
