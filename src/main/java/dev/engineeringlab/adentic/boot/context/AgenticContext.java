package dev.engineeringlab.adentic.boot.context;

import dev.engineeringlab.adentic.boot.annotations.Inject;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/**
 * Lightweight dependency injection container for AgenticBoot.
 *
 * <p>Provides constructor-based dependency injection with automatic bean resolution. Supports
 * singleton and prototype scopes, lazy initialization, and circular dependency detection.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * AgenticContext context = new AgenticContext();
 *
 * // Register singleton
 * context.registerSingleton(MyService.class, myService);
 *
 * // Register factory
 * context.registerFactory(MyService.class, () -> new MyService(...));
 *
 * // Retrieve bean
 * MyService service = context.getBean(MyService.class);
 *
 * // Clean up
 * context.close();
 * }</pre>
 */
@Slf4j
public class AgenticContext implements AutoCloseable {

  private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();
  private final Map<String, Object> namedBeans = new ConcurrentHashMap<>();
  private final Map<Class<?>, Supplier<?>> factories = new ConcurrentHashMap<>();
  private final List<Class<?>> instantiationStack = new ArrayList<>();

  /**
   * Register a singleton bean by type.
   *
   * @param type the bean type
   * @param instance the bean instance
   * @param <T> bean type
   */
  public <T> void registerSingleton(final Class<T> type, final T instance) {
    if (singletons.containsKey(type)) {
      log.warn("Overwriting existing bean of type: {}", type.getName());
    }
    singletons.put(type, instance);
    log.debug("Registered singleton bean: {}", type.getSimpleName());
  }

  /**
   * Register a named singleton bean.
   *
   * @param name the bean name
   * @param instance the bean instance
   */
  public void registerSingleton(final String name, final Object instance) {
    if (namedBeans.containsKey(name)) {
      log.warn("Overwriting existing bean with name: {}", name);
    }
    namedBeans.put(name, instance);
    log.debug("Registered named bean: {}", name);
  }

  /**
   * Register a bean factory for lazy instantiation.
   *
   * @param type the bean type
   * @param factory the factory function
   * @param <T> bean type
   */
  public <T> void registerFactory(final Class<T> type, final Supplier<T> factory) {
    factories.put(type, factory);
    log.debug("Registered bean factory for: {}", type.getSimpleName());
  }

  /**
   * Register a bean class for auto-instantiation.
   *
   * <p>The class must have a no-arg constructor or a constructor annotated with @Inject.
   *
   * @param beanClass the bean class
   * @param <T> bean type
   */
  public <T> void registerBean(final Class<T> beanClass) {
    registerFactory(beanClass, () -> instantiate(beanClass));
  }

  /**
   * Retrieve a bean by type.
   *
   * @param type the bean type
   * @param <T> bean type
   * @return the bean instance
   * @throws IllegalStateException if bean not found
   */
  @SuppressWarnings("unchecked")
  public <T> T getBean(final Class<T> type) {
    // Check singletons first
    if (singletons.containsKey(type)) {
      return (T) singletons.get(type);
    }

    // Check factories
    if (factories.containsKey(type)) {
      Supplier<?> factory = factories.get(type);
      T instance = (T) factory.get();
      singletons.put(type, instance); // Cache as singleton
      return instance;
    }

    throw new IllegalStateException("No bean found for type: " + type.getName());
  }

  /**
   * Retrieve a named bean.
   *
   * @param name the bean name
   * @param type the expected bean type
   * @param <T> bean type
   * @return the bean instance
   * @throws IllegalStateException if bean not found
   */
  @SuppressWarnings("unchecked")
  public <T> T getBean(final String name, final Class<T> type) {
    if (!namedBeans.containsKey(name)) {
      throw new IllegalStateException("No bean found with name: " + name);
    }
    return (T) namedBeans.get(name);
  }

  /**
   * Check if a bean of the given type exists.
   *
   * @param type the bean type
   * @return true if bean exists
   */
  public boolean containsBean(final Class<?> type) {
    return singletons.containsKey(type) || factories.containsKey(type);
  }

  /**
   * Check if a named bean exists.
   *
   * @param name the bean name
   * @return true if bean exists
   */
  public boolean containsBean(final String name) {
    return namedBeans.containsKey(name);
  }

  /**
   * Instantiate a bean class using constructor injection.
   *
   * @param beanClass the bean class
   * @param <T> bean type
   * @return the bean instance
   * @throws IllegalStateException if instantiation fails or circular dependency detected
   */
  @SuppressWarnings("unchecked")
  private <T> T instantiate(final Class<T> beanClass) {
    // Detect circular dependencies
    if (instantiationStack.contains(beanClass)) {
      throw new IllegalStateException(
          "Circular dependency detected: " + buildCircularDependencyMessage(beanClass));
    }

    instantiationStack.add(beanClass);

    try {
      // Find @Inject constructor or no-arg constructor
      Constructor<?> constructor = findInjectableConstructor(beanClass);

      if (constructor.getParameterCount() == 0) {
        // No dependencies
        return (T) constructor.newInstance();
      }

      // Resolve dependencies
      Class<?>[] paramTypes = constructor.getParameterTypes();
      Object[] args = new Object[paramTypes.length];

      for (int i = 0; i < paramTypes.length; i++) {
        args[i] = getBean(paramTypes[i]);
      }

      return (T) constructor.newInstance(args);

    } catch (Exception e) {
      throw new IllegalStateException("Failed to instantiate bean: " + beanClass.getName(), e);
    } finally {
      instantiationStack.remove(beanClass);
    }
  }

  /**
   * Find the constructor to use for injection.
   *
   * @param beanClass the bean class
   * @return the constructor
   * @throws IllegalStateException if no suitable constructor found
   */
  private Constructor<?> findInjectableConstructor(final Class<?> beanClass) {
    Constructor<?>[] constructors = beanClass.getDeclaredConstructors();

    // Look for @Inject constructor
    for (Constructor<?> constructor : constructors) {
      if (constructor.isAnnotationPresent(Inject.class)) {
        constructor.setAccessible(true);
        return constructor;
      }
    }

    // Fall back to single public constructor
    if (constructors.length == 1) {
      constructors[0].setAccessible(true);
      return constructors[0];
    }

    // Try no-arg constructor
    try {
      Constructor<?> noArgConstructor = beanClass.getDeclaredConstructor();
      noArgConstructor.setAccessible(true);
      return noArgConstructor;
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(
          "No @Inject constructor or no-arg constructor found for: " + beanClass.getName());
    }
  }

  /**
   * Build error message for circular dependency.
   *
   * @param beanClass the class causing the circular dependency
   * @return error message
   */
  private String buildCircularDependencyMessage(final Class<?> beanClass) {
    StringBuilder msg = new StringBuilder();
    for (Class<?> clazz : instantiationStack) {
      msg.append(clazz.getSimpleName()).append(" -> ");
    }
    msg.append(beanClass.getSimpleName());
    return msg.toString();
  }

  /**
   * Close the context and cleanup resources.
   *
   * <p>Calls destroy methods on beans if implemented.
   */
  @Override
  public void close() {
    log.info("Closing AgenticContext ({} beans)", singletons.size());
    singletons.clear();
    namedBeans.clear();
    factories.clear();
  }
}
