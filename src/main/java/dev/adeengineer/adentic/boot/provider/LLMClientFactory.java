package dev.adeengineer.adentic.boot.provider;

import dev.adeengineer.ai.config.LLMClientConfig;
import dev.adeengineer.ai.config.MetricsConfig;
import dev.adeengineer.ai.openai.OpenAIClient;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating LLM client instances.
 *
 * <p>Creates pre-configured LLM clients from adentic-ai-client with:
 *
 * <ul>
 *   <li>Environment-based configuration
 *   <li>Metrics enabled (Micrometer)
 *   <li>Health checks enabled
 *   <li>Ready for registration in ProviderRegistry
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Create OpenAI client
 * OpenAIClient client = LLMClientFactory.createOpenAIClient();
 *
 * // Register in ProviderRegistry
 * registry.registerProvider("openai", "llm", client);
 * }</pre>
 */
@Slf4j
public final class LLMClientFactory {

  private LLMClientFactory() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Create OpenAI client with environment configuration.
   *
   * <p>Configuration via environment variables:
   *
   * <ul>
   *   <li>OPENAI_API_KEY - Required API key
   *   <li>OPENAI_BASE_URL - Optional base URL (default: https://api.openai.com/v1)
   *   <li>OPENAI_MODEL - Optional default model (default: gpt-4)
   * </ul>
   *
   * @return configured OpenAI client
   */
  public static OpenAIClient createOpenAIClient() {
    log.info("Creating OpenAI LLM client");

    // Get configuration from environment
    String apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      log.warn("OPENAI_API_KEY not set - OpenAI client will not be functional");
      log.warn("Set OPENAI_API_KEY environment variable to enable OpenAI");
    }

    String baseUrl = System.getenv("OPENAI_BASE_URL");
    String defaultModel = System.getenv().getOrDefault("OPENAI_MODEL", "gpt-4");

    // Configure metrics
    MetricsConfig metricsConfig =
        MetricsConfig.builder()
            .enableMetrics(true)
            .meterRegistry(new SimpleMeterRegistry())
            .metricPrefix("adentic.llm.openai")
            .build();

    // Build client configuration
    LLMClientConfig config =
        LLMClientConfig.builder()
            .apiKey(apiKey)
            .baseUrlOverride(baseUrl)
            .defaultModel(defaultModel)
            .temperature(0.7)
            .maxTokens(2000)
            .metricsConfig(metricsConfig)
            .build();

    OpenAIClient client = new OpenAIClient(config);
    log.info("OpenAI client created with model: {}", defaultModel);

    return client;
  }

  /**
   * Check if OpenAI client can be created.
   *
   * @return true if OPENAI_API_KEY is set
   */
  public static boolean isOpenAIAvailable() {
    String apiKey = System.getenv("OPENAI_API_KEY");
    return apiKey != null && !apiKey.isEmpty();
  }
}
