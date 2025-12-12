package examples.llm.integration;

import dev.engineeringlab.adentic.boot.AgenticApplication;
import dev.engineeringlab.adentic.boot.annotations.AgenticBootApplication;
import dev.engineeringlab.adentic.boot.annotations.RestController;
import dev.engineeringlab.adentic.boot.context.AgenticContext;
import dev.engineeringlab.adentic.boot.web.annotations.GetMapping;
import dev.engineeringlab.adentic.boot.web.annotations.PostMapping;
import dev.engineeringlab.adentic.boot.web.annotations.RequestBody;
import dev.engineeringlab.common.annotation.Provider;
import dev.engineeringlab.common.provider.Providers;
import dev.engineeringlab.llm.text.TextGenerationProvider;
import dev.engineeringlab.llm.text.model.TextGenerationRequest;
import dev.engineeringlab.llm.text.model.TextGenerationResponse;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Example application demonstrating automatic provider injection using the @Provider annotation.
 *
 * <p>This example showcases the field-level auto-injection pattern where providers are
 * automatically injected into annotated fields by calling {@link Providers#inject(Object, Class)}
 * in the constructor.
 *
 * <h2>Auto-Injection Pattern</h2>
 *
 * <pre>{@code
 * public class MyClass {
 *   @Provider(name = "openai")
 *   private TextGenerationProvider provider;
 *
 *   public MyClass() {
 *     Providers.inject(this, MyClass.class);
 *     // provider is now automatically initialized
 *   }
 * }
 * }</pre>
 *
 * <p>The {@code @Provider} annotation marks fields for automatic injection. When
 * {@code Providers.inject(this, MyClass.class)} is called, it:
 * <ol>
 *   <li>Scans for fields annotated with @Provider</li>
 *   <li>Discovers and initializes the appropriate provider based on the field type and name</li>
 *   <li>Injects the provider instance into the field</li>
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
   * when the controller is instantiated. This eliminates the need for manual provider
   * discovery and initialization.
   */
  @Slf4j
  @RestController
  public static class AutoInjectionController {

    /**
     * Auto-injected text generation provider.
     *
     * <p>This field is automatically populated by the Providers.inject() call in the
     * constructor. The provider name "openai" specifies which LLM provider to use.
     */
    @Provider(name = "openai")
    private TextGenerationProvider provider;

    /**
     * Constructor that triggers automatic provider injection.
     *
     * <p>Calling {@code Providers.inject(this, AutoInjectionController.class)} scans
     * this class for @Provider-annotated fields and automatically injects the
     * appropriate provider instances.
     */
    public AutoInjectionController() {
      Providers.inject(this, AutoInjectionController.class);
      log.info("Auto-injection completed. Provider: {}, Model: {}",
          provider.getProviderName(), provider.getModel());
    }

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
