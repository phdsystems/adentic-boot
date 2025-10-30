package dev.adeengineer.adentic.boot.scanner;

import dev.adeengineer.adentic.boot.annotations.Component;
import dev.adeengineer.adentic.boot.annotations.RestController;
import dev.adeengineer.adentic.boot.annotations.Service;
import dev.adeengineer.adentic.boot.annotations.provider.Evaluation;
import dev.adeengineer.adentic.boot.annotations.provider.Infrastructure;
import dev.adeengineer.adentic.boot.annotations.provider.LLM;
import dev.adeengineer.adentic.boot.annotations.provider.Memory;
import dev.adeengineer.adentic.boot.annotations.provider.Messaging;
import dev.adeengineer.adentic.boot.annotations.provider.Orchestration;
import dev.adeengineer.adentic.boot.annotations.provider.Queue;
import dev.adeengineer.adentic.boot.annotations.provider.Storage;
import dev.adeengineer.adentic.boot.annotations.provider.Tool;
import dev.adeengineer.adentic.boot.annotations.service.AgentService;
import dev.adeengineer.adentic.boot.annotations.service.DomainService;
import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Scans classpath for components annotated with @Component and its specializations.
 *
 * <p>Supports scanning by base package and finds all classes annotated with:
 *
 * <ul>
 *   <li>Core: {@link Component}, {@link Service}, {@link RestController}
 *   <li>Providers: {@link LLM}, {@link Infrastructure}, {@link Storage}, {@link Messaging}, {@link
 *       Orchestration}, {@link Memory}, {@link Queue}, {@link Tool}, {@link Evaluation}
 *   <li>Services: {@link DomainService}, {@link AgentService}
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * ComponentScanner scanner = new ComponentScanner("com.example.myapp");
 *
 * // Scan all component types
 * Set<Class<?>> components = scanner.scan();
 *
 * // Scan providers grouped by category
 * Map<String, Set<Class<?>>> providers = scanner.scanProviders();
 *
 * // Scan specific annotation type
 * Set<Class<?>> llmProviders = scanner.scanForAnnotation(LLM.class);
 * }</pre>
 */
@Slf4j
public class ComponentScanner {

  /**
   * All recognized component annotation types.
   *
   * <p>Includes core annotations, provider annotations, and service annotations.
   */
  private static final List<Class<? extends Annotation>> COMPONENT_ANNOTATIONS =
      List.of(
          // Core annotations
          Component.class,
          Service.class,
          RestController.class,
          // Provider annotations
          LLM.class,
          Infrastructure.class,
          Storage.class,
          Messaging.class,
          Orchestration.class,
          Memory.class,
          Queue.class,
          Tool.class,
          Evaluation.class,
          // Service annotations
          DomainService.class,
          AgentService.class);

  private final String basePackage;
  private final ClassLoader classLoader;

  /**
   * Creates a component scanner for the given base package.
   *
   * @param basePackage the base package to scan (e.g., "com.example.myapp")
   */
  public ComponentScanner(final String basePackage) {
    this(basePackage, Thread.currentThread().getContextClassLoader());
  }

  /**
   * Creates a component scanner with custom class loader.
   *
   * @param basePackage the base package to scan
   * @param classLoader the class loader
   */
  public ComponentScanner(final String basePackage, final ClassLoader classLoader) {
    this.basePackage = basePackage;
    this.classLoader = classLoader;
  }

  /**
   * Scan for all component annotated classes.
   *
   * <p>Scans for all recognized component types including @Component, @Service, @RestController,
   * all provider annotations, and service annotations.
   *
   * @return set of all component classes
   */
  public Set<Class<?>> scan() {
    long startTime = System.currentTimeMillis();
    Set<Class<?>> allComponents = new HashSet<>();

    for (Class<? extends Annotation> annotationType : COMPONENT_ANNOTATIONS) {
      allComponents.addAll(scanForAnnotation(annotationType));
    }

    long duration = System.currentTimeMillis() - startTime;
    log.info(
        "Component scan completed: found {} components in {}ms", allComponents.size(), duration);

    return allComponents;
  }

  /**
   * Scan for classes with a specific annotation type.
   *
   * <p>Convenience method for scanning a single annotation type.
   *
   * @param annotationType the annotation type to scan for
   * @return set of classes annotated with the specified type
   */
  public Set<Class<?>> scanForAnnotation(final Class<? extends Annotation> annotationType) {
    Set<Class<?>> components = new HashSet<>();

    String path = basePackage.replace('.', '/');
    URL resource = classLoader.getResource(path);

    if (resource == null) {
      log.warn("Base package not found: {}", basePackage);
      return components;
    }

    File directory = new File(resource.getFile());
    if (!directory.exists()) {
      log.warn("Base package directory not found: {}", basePackage);
      return components;
    }

    scanDirectory(directory, basePackage, annotationType, components);

    log.debug("Found {} classes with @{}", components.size(), annotationType.getSimpleName());

    return components;
  }

  /**
   * Scan for all providers grouped by category.
   *
   * <p>Returns a map with provider categories as keys and sets of provider classes as values.
   *
   * <p>Categories:
   *
   * <ul>
   *   <li>"llm" - LLM providers (OpenAI, Anthropic, etc.)
   *   <li>"infrastructure" - Infrastructure providers (Docker, Local, etc.)
   *   <li>"storage" - Storage providers
   *   <li>"messaging" - Messaging providers (Kafka, RabbitMQ, etc.)
   *   <li>"orchestration" - Orchestration providers
   *   <li>"memory" - Memory providers
   *   <li>"queue" - Queue providers
   *   <li>"tool" - Tool providers
   *   <li>"evaluation" - Evaluation providers
   * </ul>
   *
   * @return map of provider category to provider classes
   */
  public Map<String, Set<Class<?>>> scanProviders() {
    Map<String, Set<Class<?>>> providers = new LinkedHashMap<>();

    providers.put("llm", scanForAnnotation(LLM.class));
    providers.put("infrastructure", scanForAnnotation(Infrastructure.class));
    providers.put("storage", scanForAnnotation(Storage.class));
    providers.put("messaging", scanForAnnotation(Messaging.class));
    providers.put("orchestration", scanForAnnotation(Orchestration.class));
    providers.put("memory", scanForAnnotation(Memory.class));
    providers.put("queue", scanForAnnotation(Queue.class));
    providers.put("tool", scanForAnnotation(Tool.class));
    providers.put("evaluation", scanForAnnotation(Evaluation.class));

    int totalProviders = providers.values().stream().mapToInt(Set::size).sum();
    log.info(
        "Provider scan completed: found {} providers across {} categories",
        totalProviders,
        providers.size());

    return providers;
  }

  /**
   * Recursively scan directory for annotated classes.
   *
   * @param directory the directory to scan
   * @param packageName the package name
   * @param annotationType the annotation type
   * @param components the set to add found components
   */
  private void scanDirectory(
      final File directory,
      final String packageName,
      final Class<? extends Annotation> annotationType,
      final Set<Class<?>> components) {

    File[] files = directory.listFiles();
    if (files == null) {
      return;
    }

    for (File file : files) {
      if (file.isDirectory()) {
        // Recursively scan subdirectories
        scanDirectory(file, packageName + "." + file.getName(), annotationType, components);
      } else if (file.getName().endsWith(".class")) {
        // Load class and check annotation
        String className =
            packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
        try {
          Class<?> clazz = classLoader.loadClass(className);
          if (isComponent(clazz, annotationType)) {
            components.add(clazz);
            log.debug("Found component: {}", clazz.getSimpleName());
          }
        } catch (ClassNotFoundException e) {
          log.warn("Failed to load class: {}", className, e);
        } catch (NoClassDefFoundError e) {
          // Ignore classes with missing dependencies
          log.trace("Skipped class with missing dependencies: {}", className);
        }
      }
    }
  }

  /**
   * Check if a class is a component.
   *
   * <p>A class is a component if it's annotated with the specified annotation or any annotation
   * that is itself annotated with the specified annotation (meta-annotation support).
   *
   * @param clazz the class to check
   * @param annotationType the annotation type
   * @return true if class is a component
   */
  private boolean isComponent(
      final Class<?> clazz, final Class<? extends Annotation> annotationType) {

    // Direct annotation
    if (clazz.isAnnotationPresent(annotationType)) {
      return true;
    }

    // Check for meta-annotations (e.g., @Service is annotated with @Component)
    Annotation[] annotations = clazz.getAnnotations();
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().isAnnotationPresent(annotationType)) {
        return true;
      }
    }

    return false;
  }
}
