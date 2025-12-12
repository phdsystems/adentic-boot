package examples.llm.integration;

import dev.engineeringlab.adentic.boot.AgenticApplication;
import dev.engineeringlab.adentic.boot.annotations.AgenticBootApplication;
import dev.engineeringlab.adentic.boot.annotations.RestController;
import dev.engineeringlab.adentic.boot.context.AgenticContext;
import dev.engineeringlab.adentic.boot.registry.ProviderRegistry;
import dev.engineeringlab.adentic.boot.web.annotations.GetMapping;
import dev.engineeringlab.adentic.boot.web.annotations.PostMapping;
import dev.engineeringlab.adentic.boot.web.annotations.RequestBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.engineeringlab.llm.llm.text.OpenAITextGenerationProvider;
import dev.engineeringlab.llm.text.TextGenerationProvider;
import dev.engineeringlab.llm.text.model.TextGenerationRequest;
import dev.engineeringlab.llm.text.model.TextGenerationResponse;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Example application demonstrating OpenAI LLM integration with AgenticBoot.
 *
 * <h2>Setup</h2>
 *
 * <pre>{@code
 * export OPENAI_API_KEY="sk-..."
 * export OPENAI_MODEL="gpt-4-turbo-preview"  # Optional, defaults to gpt-4-turbo-preview
 * }</pre>
 *
 * <h2>Run</h2>
 *
 * <pre>{@code
 * cd adentic-boot
 * ./mvnw compile exec:java -Dexec.mainClass="examples.llm.integration.OpenAIExample"
 * }</pre>
 *
 * <h2>Test</h2>
 *
 * <pre>{@code
 * curl http://localhost:8080/api/health
 * curl http://localhost:8080/api/llm/status
 * curl -X POST http://localhost:8080/api/llm/chat \
 *   -H "Content-Type: application/json" \
 *   -d '{"message":"Hello"}'
 * }</pre>
 */
@AgenticBootApplication(port = 8080, scanBasePackages = "examples.llm.integration")
public class OpenAIExample {

  private static AgenticContext context;

  public static void main(String[] args) {
    context = AgenticApplication.run(OpenAIExample.class, args);
  }

  public static AgenticContext getContext() {
    return context;
  }

  @Slf4j
  @RestController
  public static class LLMController {

    private TextGenerationProvider llmProvider;
    private ProviderRegistry registry;

    private ProviderRegistry getRegistry() {
      if (registry == null && OpenAIExample.getContext() != null) {
        registry = OpenAIExample.getContext().getBean(ProviderRegistry.class);
      }
      return registry;
    }

    private TextGenerationProvider getProvider() {
      if (llmProvider == null) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        String model = System.getenv().getOrDefault("OPENAI_MODEL", "gpt-4-turbo-preview");

        if (apiKey == null || apiKey.isEmpty()) {
          throw new IllegalStateException("OPENAI_API_KEY not set");
        }

        llmProvider = new OpenAITextGenerationProvider(apiKey, model, new ObjectMapper());
        log.info("Initialized LLM provider: OpenAI with model {}", model);

        if (getRegistry() != null) {
          getRegistry().registerProvider("llm", "openai", llmProvider);
        }
      }
      return llmProvider;
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
      return Map.of("status", "UP", "service", "llm-example");
    }

    @GetMapping("/api/llm/status")
    public Map<String, Object> getStatus() {
      try {
        TextGenerationProvider provider = getProvider();
        return Map.of(
            "status", "UP",
            "provider", provider.getProviderName(),
            "model", provider.getModel(),
            "healthy", provider.isHealthy());
      } catch (Exception e) {
        return Map.of("status", "DOWN", "error", e.getMessage());
      }
    }

    @PostMapping("/api/llm/chat")
    public Mono<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
      String message = (String) body.getOrDefault("message", "Hello");
      log.info("Chat request: {}", message);

      return Mono.fromCallable(() -> getProvider())
          .flatMap(
              provider -> {
                TextGenerationRequest request = TextGenerationRequest.simple(message);
                return provider.generate(request);
              })
          .map(
              response ->
                  Map.<String, Object>of(
                      "question", message,
                      "answer", response.content(),
                      "model", response.model()))
          .onErrorResume(
              error -> {
                log.error("Chat error", error);
                return Mono.just(Map.of("error", error.getMessage()));
              });
    }

    @PostMapping("/api/llm/generate")
    public Mono<TextGenerationResponse> generate(@RequestBody Map<String, Object> params) {
      String prompt = (String) params.getOrDefault("prompt", "Hello");
      log.info("Generate request: {}", prompt);

      return Mono.fromCallable(() -> getProvider())
          .flatMap(
              provider -> {
                TextGenerationRequest request =
                    TextGenerationRequest.builder().prompt(prompt).build();
                return provider.generate(request);
              });
    }
  }
}
