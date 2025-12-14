package dev.engineeringlab.adentic.boot.llm;

import dev.engineeringlab.llm.LLM;
import dev.engineeringlab.llm.text.TextGenerationProvider;
import dev.engineeringlab.llm.text.model.TextGenerationRequest;
import dev.engineeringlab.llm.text.model.TextGenerationResponse;
import dev.engineeringlab.llm.text.model.TextStreamEvent;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Injectable service wrapper for the LLM facade.
 *
 * <p>Provides dependency-injection friendly access to LLM text generation capabilities. This
 * service wraps the static {@link LLM} facade, enabling constructor injection in components.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Component
 * public class MyAgent {
 *     private final LLMService llm;
 *
 *     public MyAgent(LLMService llm) {
 *         this.llm = llm;
 *     }
 *
 *     public String chat(String message) {
 *         return llm.generate(message).block().content();
 *     }
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
@Slf4j
public class LLMService {

  private final boolean available;
  private final String initError;

  /** Creates a new LLMService, checking provider availability. */
  public LLMService() {
    String error = null;
    boolean isAvailable = false;

    try {
      // Try to access the LLM class - this triggers static initialization
      LLM.getAvailableProviderNames();
      isAvailable = true;
    } catch (IllegalStateException e) {
      error = e.getMessage();
      log.debug("No LLM provider configured: {}", e.getMessage());
    } catch (ExceptionInInitializerError e) {
      Throwable cause = e.getCause();
      error = cause != null ? cause.getMessage() : e.getMessage();
      log.debug("LLM initialization failed: {}", error);
    } catch (NoClassDefFoundError e) {
      error = "LLM dependencies not available: " + e.getMessage();
      log.debug("LLM class not found: {}", e.getMessage());
    } catch (Exception e) {
      error = e.getMessage();
      log.debug("LLM initialization error: {}", e.getMessage());
    }

    this.available = isAvailable;
    this.initError = error;

    if (available) {
      log.info("LLMService initialized with provider: {} ({})", providerName(), model());
    } else {
      log.info(
          "LLMService initialized without provider ({})", error != null ? error : "no provider");
    }
  }

  /**
   * Checks if an LLM provider is available.
   *
   * @return true if a provider is configured and available
   */
  public boolean isAvailable() {
    return available;
  }

  /**
   * Generates text from a simple prompt.
   *
   * @param prompt the prompt text
   * @return Mono emitting the response
   * @throws IllegalStateException if no provider is available
   */
  public Mono<TextGenerationResponse> generate(String prompt) {
    ensureAvailable();
    return LLM.generate(prompt);
  }

  /**
   * Generates text from a request.
   *
   * @param request the generation request
   * @return Mono emitting the response
   * @throws IllegalStateException if no provider is available
   */
  public Mono<TextGenerationResponse> generate(TextGenerationRequest request) {
    ensureAvailable();
    return LLM.generate(request);
  }

  /**
   * Generates text as a stream from a simple prompt.
   *
   * @param prompt the prompt text
   * @return Flux emitting stream events
   * @throws IllegalStateException if no provider is available
   */
  public Flux<TextStreamEvent> generateStream(String prompt) {
    ensureAvailable();
    return LLM.generateStream(prompt);
  }

  /**
   * Generates text as a stream.
   *
   * @param request the generation request
   * @return Flux emitting stream events
   * @throws IllegalStateException if no provider is available
   */
  public Flux<TextStreamEvent> generateStream(TextGenerationRequest request) {
    ensureAvailable();
    return LLM.generateStream(request);
  }

  /**
   * Returns a service for a specific provider by name.
   *
   * @param providerName the provider name (e.g., "anthropic", "openai", "ollama")
   * @return provider-specific LLM service
   * @throws IllegalArgumentException if no provider with that name exists
   */
  public LLM.LLMService using(String providerName) {
    return LLM.using(providerName);
  }

  /**
   * Returns all available provider names.
   *
   * @return list of provider names
   */
  public List<String> getAvailableProviders() {
    return LLM.getAvailableProviderNames();
  }

  /**
   * Returns the underlying provider.
   *
   * @return the text generation provider
   * @throws IllegalStateException if no provider is available
   */
  public TextGenerationProvider provider() {
    ensureAvailable();
    return LLM.provider();
  }

  /**
   * Returns the name of the current provider.
   *
   * @return provider name (e.g., "anthropic", "openai")
   * @throws IllegalStateException if no provider is available
   */
  public String providerName() {
    ensureAvailable();
    return LLM.providerName();
  }

  /**
   * Returns the model of the current provider.
   *
   * @return model name (e.g., "claude-3-5-sonnet", "gpt-4-turbo")
   * @throws IllegalStateException if no provider is available
   */
  public String model() {
    ensureAvailable();
    return LLM.model();
  }

  /**
   * Checks if streaming is supported by the current provider.
   *
   * @return true if streaming is supported
   * @throws IllegalStateException if no provider is available
   */
  public boolean supportsStreaming() {
    ensureAvailable();
    return LLM.supportsStreaming();
  }

  /**
   * Checks if the current provider is healthy.
   *
   * @return true if healthy
   * @throws IllegalStateException if no provider is available
   */
  public boolean isHealthy() {
    ensureAvailable();
    return LLM.isHealthy();
  }

  /**
   * Returns the initialization error message if LLM is not available.
   *
   * @return error message or null if available
   */
  public String getInitializationError() {
    return initError;
  }

  private void ensureAvailable() {
    if (!available) {
      String msg =
          initError != null
              ? "LLM not available: " + initError
              : "No LLM provider available. Add a provider dependency (e.g., llm-provider-anthropic)";
      throw new IllegalStateException(msg);
    }
  }
}
