package dev.engineeringlab.adentic.boot.registry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Registry for managing service instances by category.
 *
 * <p>Provides centralized access to all registered services. Categories are created dynamically as
 * needed.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * ServiceRegistry registry = new ServiceRegistry();
 *
 * // Register instance
 * registry.register("llm", "openai", openAiInstance);
 *
 * // Retrieve by category and name
 * Optional<Object> llm = registry.get("llm", "openai");
 *
 * // Get all in a category
 * Map<String, Object> llms = registry.getAll("llm");
 * }</pre>
 */
@Slf4j
public class ServiceRegistry {

  private final Map<String, Map<String, Object>> instances;

  public ServiceRegistry() {
    this.instances = new LinkedHashMap<>();
  }

  /**
   * Register an instance.
   *
   * @param category the category (llm, storage, messaging, etc.)
   * @param name the service name
   * @param instance the service instance
   */
  public void register(final String category, final String name, final Object instance) {
    instances.computeIfAbsent(category, k -> new LinkedHashMap<>()).put(name, instance);
    log.info("Registered {}: {}", category, name);
  }

  /**
   * Get by category and name.
   *
   * @param category the category
   * @param name the service name
   * @return optional containing the instance, or empty if not found
   */
  public Optional<Object> get(final String category, final String name) {
    if (!instances.containsKey(category)) {
      return Optional.empty();
    }
    return Optional.ofNullable(instances.get(category).get(name));
  }

  /**
   * Get all services in a category.
   *
   * @param category the category
   * @return map of name to instance
   */
  public Map<String, Object> getAll(final String category) {
    return instances.getOrDefault(category, Map.of());
  }

  /**
   * Get all categories.
   *
   * @return set of all categories
   */
  public Set<String> getCategories() {
    return instances.keySet();
  }

  /**
   * Get count in a category.
   *
   * @param category the category
   * @return number of instances in the category
   */
  public int count(final String category) {
    return instances.getOrDefault(category, Map.of()).size();
  }

  /**
   * Get total count across all categories.
   *
   * @return total number of instances
   */
  public int totalCount() {
    return instances.values().stream().mapToInt(Map::size).sum();
  }

  /**
   * Check if an instance exists.
   *
   * @param category the category
   * @param name the service name
   * @return true if exists, false otherwise
   */
  public boolean has(final String category, final String name) {
    return instances.containsKey(category) && instances.get(category).containsKey(name);
  }

  /**
   * Clear all instances from a category.
   *
   * @param category the category to clear
   */
  public void clear(final String category) {
    if (instances.containsKey(category)) {
      instances.get(category).clear();
      log.info("Cleared category: {}", category);
    }
  }

  /** Clear all instances from all categories. */
  public void clearAll() {
    instances.clear();
    log.info("Cleared all categories");
  }
}
