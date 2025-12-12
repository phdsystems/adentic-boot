package dev.engineeringlab.adentic.boot.llm;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an LLM provider implementation.
 *
 * <p>This annotation provides metadata for LLM provider discovery and configuration. Classes
 * annotated with {@code @LLMProvider} can be discovered via ServiceLoader and registered in the
 * {@link dev.engineeringlab.llm.LLM} facade.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @LLMProvider(
 *     name = "openai",
 *     description = "OpenAI GPT models",
 *     capabilities = {"chat", "completion", "embedding"},
 *     models = {"gpt-4", "gpt-4-turbo-preview", "gpt-3.5-turbo"},
 *     priority = 100
 * )
 * public class OpenAITextGenerationProvider implements TextGenerationProvider {
 *     // ...
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LLMProvider {

  /**
   * The provider name used for lookup.
   *
   * @return provider name (e.g., "openai", "anthropic", "ollama")
   */
  String name();

  /**
   * Human-readable description of the provider.
   *
   * @return provider description
   */
  String description() default "";

  /**
   * Capabilities supported by this provider.
   *
   * @return array of capability names (e.g., "chat", "completion", "embedding", "vision")
   */
  String[] capabilities() default {"chat", "completion"};

  /**
   * Models available through this provider.
   *
   * @return array of model identifiers
   */
  String[] models() default {};

  /**
   * Priority for provider selection (higher = preferred).
   *
   * @return priority value
   */
  int priority() default 0;

  /**
   * Whether this provider requires an API key.
   *
   * @return true if API key is required
   */
  boolean requiresApiKey() default true;

  /**
   * Environment variable name for the API key.
   *
   * @return env var name (e.g., "OPENAI_API_KEY")
   */
  String apiKeyEnvVar() default "";

  /**
   * Whether this provider supports streaming responses.
   *
   * @return true if streaming is supported
   */
  boolean supportsStreaming() default true;

  /**
   * Base URL for the API (if configurable).
   *
   * @return default base URL or empty string
   */
  String defaultBaseUrl() default "";
}
