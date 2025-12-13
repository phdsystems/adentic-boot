package examples.llm.integration;

import dev.engineeringlab.adentic.boot.AgenticApplication;
import dev.engineeringlab.adentic.boot.annotations.AgenticBootApplication;
import dev.engineeringlab.adentic.boot.annotations.RestController;
import dev.engineeringlab.adentic.boot.context.AgenticContext;
import dev.engineeringlab.llm.LLM;
import dev.engineeringlab.common.provider.Registry;
import dev.engineeringlab.adentic.boot.web.annotations.GetMapping;
import dev.engineeringlab.adentic.boot.web.annotations.PostMapping;
import dev.engineeringlab.adentic.boot.web.annotations.RequestBody;
import dev.engineeringlab.llm.text.TextGenerationProvider;
import dev.engineeringlab.llm.text.model.TextGenerationRequest;
import dev.engineeringlab.llm.text.model.TextGenerationResponse;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Example application demonstrating LLM integration with AgenticBoot.
 *
 * <p>Uses the {@link LLM} facade for provider discovery and creation.
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
 * ./mvnw compile exec:java -Dexec.mainClass="examples.llm.integration.OpenAIExample"
 * }</pre>
 *
 * <h2>Test</h2>
 *
 * <pre>{@code
 * curl http://localhost:8080/api/health
 * curl http://localhost:8080/api/llm/status
 * curl http://localhost:8080/api/llm/providers
 * curl -X POST http://localhost:8080/api/llm/chat \
 *   -H "Content-Type: application/json" \
 *   -d '{"message":"Hello"}'
 * curl -X POST http://localhost:8080/api/llm/chat \
 *   -H "Content-Type: application/json" \
 *   -d '{"message":"Hello", "provider":"anthropic"}'
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

    private Registry registry;

    private Registry getRegistry() {
      if (registry == null && OpenAIExample.getContext() != null) {
        registry = OpenAIExample.getContext().getBean(Registry.class);
      }
      return registry;
    }

    private TextGenerationProvider getLLM(String name) {
      if (name == null || name.isBlank()) {
        name = "openai";
      }

      // Check if already registered
      if (getRegistry() != null) {
        Object cached = getRegistry().get("llm", name);
        if (cached instanceof TextGenerationProvider llm) {
          return llm;
        }
      }

      // Get from facade
      TextGenerationProvider llm = LLM.using(name).provider();
      log.info("Initialized LLM: {} with model {}",
          llm.getProviderName(), llm.getModel());

      // Register for caching
      if (getRegistry() != null) {
        getRegistry().register("llm", name, llm);
      }

      return llm;
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
      return Map.of("status", "UP", "service", "llm-example");
    }

    @GetMapping("/api/llm/providers")
    public Map<String, Object> listProviders() {
      return Map.of(
          "available", java.util.List.of("openai", "anthropic", "ollama"),
          "configured", Map.of(
              "openai", System.getenv("OPENAI_API_KEY") != null,
              "anthropic", System.getenv("ANTHROPIC_API_KEY") != null,
              "ollama", true
          )
      );
    }

    @GetMapping("/api/llm/status")
    public Map<String, Object> getStatus() {
      try {
        TextGenerationProvider provider = getLLM("openai");
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
      String providerName = (String) body.getOrDefault("provider", "openai");
      log.info("Chat request [{}]: {}", providerName, message);

      return Mono.fromCallable(() -> getLLM(providerName))
          .flatMap(provider -> {
            TextGenerationRequest request = TextGenerationRequest.simple(message);
            return provider.generate(request);
          })
          .map(response -> Map.<String, Object>of(
              "question", message,
              "answer", response.content(),
              "model", response.model(),
              "provider", providerName))
          .onErrorResume(error -> {
            log.error("Chat error", error);
            return Mono.just(Map.of(
                "error", error.getMessage(),
                "provider", providerName));
          });
    }

    @PostMapping("/api/llm/generate")
    public Mono<TextGenerationResponse> generate(@RequestBody Map<String, Object> params) {
      String prompt = (String) params.getOrDefault("prompt", "Hello");
      String providerName = (String) params.getOrDefault("provider", "openai");
      log.info("Generate request [{}]: {}", providerName, prompt);

      return Mono.fromCallable(() -> getLLM(providerName))
          .flatMap(provider -> {
            TextGenerationRequest request = TextGenerationRequest.builder()
                .prompt(prompt)
                .build();
            return provider.generate(request);
          });
    }
  }
}
