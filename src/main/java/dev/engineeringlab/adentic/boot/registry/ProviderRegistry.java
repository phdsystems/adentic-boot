package dev.engineeringlab.adentic.boot.registry;

import dev.engineeringlab.adentic.boot.annotations.provider.Evaluation;
import dev.engineeringlab.adentic.boot.annotations.provider.Infrastructure;
import dev.engineeringlab.adentic.boot.annotations.provider.LLM;
import dev.engineeringlab.adentic.boot.annotations.provider.Memory;
import dev.engineeringlab.adentic.boot.annotations.provider.Messaging;
import dev.engineeringlab.adentic.boot.annotations.provider.Orchestration;
import dev.engineeringlab.adentic.boot.annotations.provider.Queue;
import dev.engineeringlab.adentic.boot.annotations.provider.Storage;
import dev.engineeringlab.adentic.boot.annotations.provider.Tool;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Registry for managing providers by category.
 *
 * <p>Provides centralized access to all registered providers including LLM providers,
 * infrastructure providers, storage providers, messaging providers, orchestration providers, memory
 * providers, queue providers, tool providers, and evaluation providers.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * ProviderRegistry registry = new ProviderRegistry();
 *
 * // Register provider instance
 * registry.registerProvider("llm", "openai", openAiProvider);
 *
 * // Retrieve provider by category and name
 * Optional<Object> provider = registry.getProvider("llm", "openai");
 *
 * // Get all providers in a category
 * Map<String, Object> llmProviders = registry.getProvidersByCategory("llm");
 *
 * // Get provider count
 * int count = registry.getProviderCount("llm");
 * }</pre>
 */
@Slf4j
public class ProviderRegistry {

  /** Map of category to provider name to provider instance. */
  private final Map<String, Map<String, Object>> providers;

  /** Map of annotation types to category names. */
  private static final Map<Class<? extends Annotation>, String> ANNOTATION_TO_CATEGORY;

  static {
    ANNOTATION_TO_CATEGORY = new HashMap<>();
    ANNOTATION_TO_CATEGORY.put(LLM.class, "llm");
    ANNOTATION_TO_CATEGORY.put(Infrastructure.class, "infrastructure");
    ANNOTATION_TO_CATEGORY.put(Storage.class, "storage");
    ANNOTATION_TO_CATEGORY.put(Messaging.class, "messaging");
    ANNOTATION_TO_CATEGORY.put(Orchestration.class, "orchestration");
    ANNOTATION_TO_CATEGORY.put(Memory.class, "memory");
    ANNOTATION_TO_CATEGORY.put(Queue.class, "queue");
    ANNOTATION_TO_CATEGORY.put(Tool.class, "tool");
    ANNOTATION_TO_CATEGORY.put(Evaluation.class, "evaluation");
  }

  /** Creates a new provider registry. */
  public ProviderRegistry() {
    this.providers = new LinkedHashMap<>();
    initializeCategories();
  }

  /** Initialize empty maps for all provider categories. */
  private void initializeCategories() {
    providers.put("llm", new LinkedHashMap<>());
    providers.put("infrastructure", new LinkedHashMap<>());
    providers.put("storage", new LinkedHashMap<>());
    providers.put("messaging", new LinkedHashMap<>());
    providers.put("orchestration", new LinkedHashMap<>());
    providers.put("memory", new LinkedHashMap<>());
    providers.put("queue", new LinkedHashMap<>());
    providers.put("tool", new LinkedHashMap<>());
    providers.put("evaluation", new LinkedHashMap<>());
    // NEW: EE categories
    providers.put("agent", new LinkedHashMap<>());
    // NEW: Enterprise integration categories
    providers.put("resilience", new LinkedHashMap<>());
    providers.put("health", new LinkedHashMap<>());
    providers.put("metrics", new LinkedHashMap<>());
  }

  /**
   * Register a provider instance.
   *
   * @param category the provider category (llm, infrastructure, etc.)
   * @param name the provider name
   * @param instance the provider instance
   */
  public void registerProvider(final String category, final String name, final Object instance) {
    if (!providers.containsKey(category)) {
      throw new IllegalArgumentException("Unknown provider category: " + category);
    }

    providers.get(category).put(name, instance);
    log.info("Registered {} provider: {}", category, name);
  }

  /**
   * Register a provider class using its annotation to determine category and name.
   *
   * @param providerClass the provider class
   * @param instance the provider instance
   */
  public void registerProviderFromClass(final Class<?> providerClass, final Object instance) {
    String category = getCategoryFromAnnotation(providerClass);
    String name = getNameFromAnnotation(providerClass);

    registerProvider(category, name, instance);
  }

  /**
   * Get a provider by category and name.
   *
   * @param category the provider category
   * @param name the provider name
   * @return optional containing the provider, or empty if not found
   */
  public Optional<Object> getProvider(final String category, final String name) {
    if (!providers.containsKey(category)) {
      return Optional.empty();
    }

    return Optional.ofNullable(providers.get(category).get(name));
  }

  /**
   * Get all providers in a category.
   *
   * @param category the provider category
   * @return map of provider name to provider instance
   */
  public Map<String, Object> getProvidersByCategory(final String category) {
    return providers.getOrDefault(category, Map.of());
  }

  /**
   * Get all categories.
   *
   * @return set of all provider categories
   */
  public Set<String> getCategories() {
    return providers.keySet();
  }

  /**
   * Get count of providers in a category.
   *
   * @param category the provider category
   * @return number of providers in the category
   */
  public int getProviderCount(final String category) {
    return providers.getOrDefault(category, Map.of()).size();
  }

  /**
   * Get total count of all providers.
   *
   * @return total number of providers across all categories
   */
  public int getTotalProviderCount() {
    return providers.values().stream().mapToInt(Map::size).sum();
  }

  /**
   * Check if a provider exists.
   *
   * @param category the provider category
   * @param name the provider name
   * @return true if provider exists, false otherwise
   */
  public boolean hasProvider(final String category, final String name) {
    return providers.containsKey(category) && providers.get(category).containsKey(name);
  }

  /**
   * Get category from provider annotation.
   *
   * @param providerClass the provider class
   * @return category name
   */
  private String getCategoryFromAnnotation(final Class<?> providerClass) {
    for (Map.Entry<Class<? extends Annotation>, String> entry : ANNOTATION_TO_CATEGORY.entrySet()) {
      if (providerClass.isAnnotationPresent(entry.getKey())) {
        return entry.getValue();
      }
    }

    throw new IllegalArgumentException(
        "Class " + providerClass.getName() + " is not annotated with a provider annotation");
  }

  /**
   * Get provider name from annotation.
   *
   * @param providerClass the provider class
   * @return provider name from annotation, or decapitalized class name if not specified
   */
  private String getNameFromAnnotation(final Class<?> providerClass) {
    // Try each provider annotation
    if (providerClass.isAnnotationPresent(LLM.class)) {
      String name = providerClass.getAnnotation(LLM.class).name();
      return name.isEmpty() ? decapitalize(providerClass.getSimpleName()) : name;
    }

    if (providerClass.isAnnotationPresent(Infrastructure.class)) {
      String name = providerClass.getAnnotation(Infrastructure.class).name();
      return name.isEmpty() ? decapitalize(providerClass.getSimpleName()) : name;
    }

    if (providerClass.isAnnotationPresent(Storage.class)) {
      String name = providerClass.getAnnotation(Storage.class).name();
      return name.isEmpty() ? decapitalize(providerClass.getSimpleName()) : name;
    }

    if (providerClass.isAnnotationPresent(Messaging.class)) {
      String name = providerClass.getAnnotation(Messaging.class).name();
      return name.isEmpty() ? decapitalize(providerClass.getSimpleName()) : name;
    }

    if (providerClass.isAnnotationPresent(Orchestration.class)) {
      String name = providerClass.getAnnotation(Orchestration.class).name();
      return name.isEmpty() ? decapitalize(providerClass.getSimpleName()) : name;
    }

    if (providerClass.isAnnotationPresent(Memory.class)) {
      String name = providerClass.getAnnotation(Memory.class).name();
      return name.isEmpty() ? decapitalize(providerClass.getSimpleName()) : name;
    }

    if (providerClass.isAnnotationPresent(Queue.class)) {
      String name = providerClass.getAnnotation(Queue.class).name();
      return name.isEmpty() ? decapitalize(providerClass.getSimpleName()) : name;
    }

    if (providerClass.isAnnotationPresent(Tool.class)) {
      String name = providerClass.getAnnotation(Tool.class).name();
      return name.isEmpty() ? decapitalize(providerClass.getSimpleName()) : name;
    }

    if (providerClass.isAnnotationPresent(Evaluation.class)) {
      String name = providerClass.getAnnotation(Evaluation.class).name();
      return name.isEmpty() ? decapitalize(providerClass.getSimpleName()) : name;
    }

    throw new IllegalArgumentException(
        "Class " + providerClass.getName() + " is not annotated with a provider annotation");
  }

  /**
   * Register an EE agent in the "agent" category.
   *
   * @param name the agent name (e.g., "simple", "react", "cot")
   * @param instance the agent instance
   */
  public void registerAgent(final String name, final Object instance) {
    registerProvider("agent", name, instance);
    log.info("Registered EE agent: {}", name);
  }

  /**
   * Get an agent by name from "agent" category.
   *
   * @param name the agent name
   * @param <T> the expected agent type
   * @return optional containing the agent, or empty if not found
   */
  @SuppressWarnings("unchecked")
  public <T> Optional<T> getAgent(final String name) {
    return (Optional<T>) getProvider("agent", name);
  }

  /**
   * Get all registered agents.
   *
   * @return map of agent name to agent instance
   */
  public Map<String, Object> getAllAgents() {
    return getProvidersByCategory("agent");
  }

  /**
   * Decapitalize a string.
   *
   * @param str the string to decapitalize
   * @return decapitalized string
   */
  private String decapitalize(final String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return Character.toLowerCase(str.charAt(0)) + str.substring(1);
  }
}
