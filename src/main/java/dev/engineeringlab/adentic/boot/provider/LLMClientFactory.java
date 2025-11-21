package dev.engineeringlab.adentic.boot.provider;

import dev.engineeringlab.ai.anthropic.AnthropicClient;
import dev.engineeringlab.ai.config.LLMClientConfig;
import dev.engineeringlab.ai.config.MetricsConfig;
import dev.engineeringlab.ai.gemini.GeminiClient;
import dev.engineeringlab.ai.openai.OpenAIClient;
import dev.engineeringlab.ai.runtime.ollama.OllamaClient;
import dev.engineeringlab.ai.runtime.vllm.VLLMClient;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating multi-vendor LLM client instances.
 *
 * <p>Creates pre-configured LLM clients from adentic-ai-client with:
 *
 * <ul>
 *   <li>Multi-vendor support (OpenAI, Anthropic, Gemini, vLLM, Ollama)
 *   <li>Environment-based configuration
 *   <li>Metrics enabled (Micrometer)
 *   <li>Health checks enabled
 *   <li>Ready for registration in ProviderRegistry
 * </ul>
 *
 * <h2>Supported Providers</h2>
 *
 * <ul>
 *   <li><strong>OpenAI:</strong> GPT-4, GPT-3.5-turbo, GPT-4o
 *   <li><strong>Anthropic:</strong> Claude 3.5 Sonnet, Claude 3 Opus/Haiku
 *   <li><strong>Google Gemini:</strong> Gemini 1.5 Pro/Flash
 *   <li><strong>vLLM:</strong> Self-hosted inference server
 *   <li><strong>Ollama:</strong> Local models (Llama 3, Mistral, etc.)
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Create clients for all available providers
 * if (LLMClientFactory.isOpenAIAvailable()) {
 *   OpenAIClient openai = LLMClientFactory.createOpenAIClient();
 *   registry.registerProvider("openai", "llm", openai);
 * }
 *
 * if (LLMClientFactory.isAnthropicAvailable()) {
 *   AnthropicClient claude = LLMClientFactory.createAnthropicClient();
 *   registry.registerProvider("anthropic", "llm", claude);
 * }
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

  /**
   * Create Anthropic Claude client with environment configuration.
   *
   * <p>Configuration via environment variables:
   *
   * <ul>
   *   <li>ANTHROPIC_API_KEY - Required API key
   *   <li>ANTHROPIC_MODEL - Optional default model (default: claude-3-5-sonnet-20241022)
   * </ul>
   *
   * @return configured Anthropic client
   */
  public static AnthropicClient createAnthropicClient() {
    log.info("Creating Anthropic Claude LLM client");

    String apiKey = System.getenv("ANTHROPIC_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      log.warn("ANTHROPIC_API_KEY not set - Anthropic client will not be functional");
    }

    String defaultModel =
        System.getenv().getOrDefault("ANTHROPIC_MODEL", "claude-3-5-sonnet-20241022");

    MetricsConfig metricsConfig =
        MetricsConfig.builder()
            .enableMetrics(true)
            .meterRegistry(new SimpleMeterRegistry())
            .metricPrefix("adentic.llm.anthropic")
            .build();

    LLMClientConfig config =
        LLMClientConfig.builder()
            .apiKey(apiKey)
            .defaultModel(defaultModel)
            .temperature(0.7)
            .maxTokens(4096)
            .metricsConfig(metricsConfig)
            .build();

    AnthropicClient client = new AnthropicClient(config);
    log.info("Anthropic client created with model: {}", defaultModel);
    return client;
  }

  /**
   * Check if Anthropic client can be created.
   *
   * @return true if ANTHROPIC_API_KEY is set
   */
  public static boolean isAnthropicAvailable() {
    String apiKey = System.getenv("ANTHROPIC_API_KEY");
    return apiKey != null && !apiKey.isEmpty();
  }

  /**
   * Create Google Gemini client with environment configuration.
   *
   * <p>Configuration via environment variables:
   *
   * <ul>
   *   <li>GEMINI_API_KEY - Required API key
   *   <li>GEMINI_MODEL - Optional default model (default: gemini-2.0-flash-exp)
   * </ul>
   *
   * @return configured Gemini client
   */
  public static GeminiClient createGeminiClient() {
    log.info("Creating Google Gemini LLM client");

    String apiKey = System.getenv("GEMINI_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      log.warn("GEMINI_API_KEY not set - Gemini client will not be functional");
    }

    String defaultModel = System.getenv().getOrDefault("GEMINI_MODEL", "gemini-2.0-flash-exp");

    MetricsConfig metricsConfig =
        MetricsConfig.builder()
            .enableMetrics(true)
            .meterRegistry(new SimpleMeterRegistry())
            .metricPrefix("adentic.llm.gemini")
            .build();

    LLMClientConfig config =
        LLMClientConfig.builder()
            .apiKey(apiKey)
            .defaultModel(defaultModel)
            .temperature(0.7)
            .maxTokens(8192)
            .metricsConfig(metricsConfig)
            .build();

    GeminiClient client = new GeminiClient(config);
    log.info("Gemini client created with model: {}", defaultModel);
    return client;
  }

  /**
   * Check if Gemini client can be created.
   *
   * @return true if GEMINI_API_KEY is set
   */
  public static boolean isGeminiAvailable() {
    String apiKey = System.getenv("GEMINI_API_KEY");
    return apiKey != null && !apiKey.isEmpty();
  }

  /**
   * Create vLLM client for self-hosted models.
   *
   * <p>Configuration via environment variables:
   *
   * <ul>
   *   <li>VLLM_BASE_URL - Required base URL (e.g., http://localhost:8000)
   *   <li>VLLM_MODEL - Optional default model (default: llama-3.1-8b-instruct)
   * </ul>
   *
   * @return configured vLLM client
   */
  public static VLLMClient createVLLMClient() {
    log.info("Creating vLLM (self-hosted) LLM client");

    String baseUrl = System.getenv().getOrDefault("VLLM_BASE_URL", "http://localhost:8000");
    String defaultModel = System.getenv().getOrDefault("VLLM_MODEL", "llama-3.1-8b-instruct");

    MetricsConfig metricsConfig =
        MetricsConfig.builder()
            .enableMetrics(true)
            .meterRegistry(new SimpleMeterRegistry())
            .metricPrefix("adentic.llm.vllm")
            .build();

    LLMClientConfig config =
        LLMClientConfig.builder()
            .baseUrlOverride(baseUrl)
            .defaultModel(defaultModel)
            .temperature(0.7)
            .maxTokens(2048)
            .metricsConfig(metricsConfig)
            .build();

    VLLMClient client = new VLLMClient(config);
    log.info("vLLM client created with base URL: {}", baseUrl);
    return client;
  }

  /**
   * Check if vLLM client can be created.
   *
   * @return true if VLLM_BASE_URL is set
   */
  public static boolean isVLLMAvailable() {
    String baseUrl = System.getenv("VLLM_BASE_URL");
    return baseUrl != null && !baseUrl.isEmpty();
  }

  /**
   * Create Ollama client for local models.
   *
   * <p>Configuration via environment variables:
   *
   * <ul>
   *   <li>OLLAMA_BASE_URL - Optional base URL (default: http://localhost:11434)
   *   <li>OLLAMA_MODEL - Optional default model (default: llama3.1)
   * </ul>
   *
   * @return configured Ollama client
   */
  public static OllamaClient createOllamaClient() {
    log.info("Creating Ollama (local) LLM client");

    String baseUrl = System.getenv().getOrDefault("OLLAMA_BASE_URL", "http://localhost:11434");
    String defaultModel = System.getenv().getOrDefault("OLLAMA_MODEL", "llama3.1");

    MetricsConfig metricsConfig =
        MetricsConfig.builder()
            .enableMetrics(true)
            .meterRegistry(new SimpleMeterRegistry())
            .metricPrefix("adentic.llm.ollama")
            .build();

    LLMClientConfig config =
        LLMClientConfig.builder()
            .baseUrlOverride(baseUrl)
            .defaultModel(defaultModel)
            .temperature(0.7)
            .maxTokens(2048)
            .metricsConfig(metricsConfig)
            .build();

    OllamaClient client = new OllamaClient(config);
    log.info("Ollama client created with base URL: {}", baseUrl);
    return client;
  }

  /**
   * Check if Ollama client can be created.
   *
   * @return always true (Ollama has default localhost URL)
   */
  public static boolean isOllamaAvailable() {
    return true; // Ollama always available with default localhost URL
  }
}
