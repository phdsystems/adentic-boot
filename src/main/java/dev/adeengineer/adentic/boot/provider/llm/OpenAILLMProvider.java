package dev.adeengineer.adentic.boot.provider.llm;

import dev.adeengineer.adentic.boot.annotations.Service;
import dev.adeengineer.ai.config.LLMClientConfig;
import dev.adeengineer.ai.openai.OpenAIClient;
import dev.adeengineer.annotation.provider.TextGenerationProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI LLM Provider for AgenticBoot.
 *
 * <p>Provides OpenAI GPT models (GPT-4, GPT-3.5-turbo, etc.) as a text generation provider.
 *
 * <p>Configuration via environment variables:
 *
 * <ul>
 *   <li>OPENAI_API_KEY - Required OpenAI API key
 *   <li>OPENAI_BASE_URL - Optional base URL override (default: https://api.openai.com/v1)
 *   <li>OPENAI_MODEL - Optional default model (default: gpt-4)
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @RestController
 * public class MyController {
 *   @Inject
 *   private ProviderRegistry registry;
 *
 *   public Mono<String> chat(String prompt) {
 *     OpenAILLMProvider provider = registry.<OpenAILLMProvider>getProvider("openai", "text-generation")
 *         .orElseThrow();
 *     return provider.getClient().complete(CompletionRequest.of(prompt))
 *         .map(result -> result.getContent());
 *   }
 * }
 * }</pre>
 */
@Slf4j
@Service
@TextGenerationProvider(
    name = "openai",
    model = "gpt-4",
    description = "OpenAI GPT models (GPT-4, GPT-3.5-turbo, etc.)",
    supportsStreaming = true,
    maxTokens = 128000,
    contextWindow = 128000,
    isLocal = false,
    priority = 10,
    enabledByDefault = true)
public class OpenAILLMProvider {

  @Getter private final OpenAIClient client;

  /**
   * Create OpenAI provider with configuration from environment variables.
   *
   * <p>Requires OPENAI_API_KEY environment variable to be set.
   */
  public OpenAILLMProvider() {
    log.info("Initializing OpenAI LLM provider");

    // Get configuration from environment
    String apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      log.warn(
          "OPENAI_API_KEY environment variable not set - OpenAI provider will not be functional");
      log.warn("Set OPENAI_API_KEY to enable OpenAI LLM support");
    }

    String baseUrl = System.getenv("OPENAI_BASE_URL");
    String defaultModel = System.getenv().getOrDefault("OPENAI_MODEL", "gpt-4");

    // Build configuration
    LLMClientConfig config =
        LLMClientConfig.builder()
            .apiKey(apiKey)
            .baseUrlOverride(baseUrl)
            .defaultModel(defaultModel)
            .temperature(0.7)
            .maxTokens(2000)
            .build();

    // Create client
    this.client = new OpenAIClient(config);

    log.info("OpenAI LLM provider initialized with model: {}", defaultModel);
  }

  /**
   * Get the provider name.
   *
   * @return provider name
   */
  public String getName() {
    return "openai";
  }

  /**
   * Get the provider type/category.
   *
   * @return provider type
   */
  public String getType() {
    return "text-generation";
  }

  /**
   * Check if provider is ready for use.
   *
   * @return true if API key is configured
   */
  public boolean isReady() {
    String apiKey = System.getenv("OPENAI_API_KEY");
    return apiKey != null && !apiKey.isEmpty();
  }
}
