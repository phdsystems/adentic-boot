package dev.adeengineer.adentic.boot.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple, type-safe event bus for pub/sub messaging.
 *
 * <p>Provides synchronous and asynchronous event publishing with type-safe listeners. No Spring
 * dependencies required.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * EventBus eventBus = new EventBus();
 *
 * // Subscribe to events
 * eventBus.subscribe(AgentCompletedEvent.class, event -> {
 *     log.info("Agent completed: {}", event.getAgentName());
 * });
 *
 * // Publish event (synchronous)
 * eventBus.publish(new AgentCompletedEvent(...));
 *
 * // Subscribe async
 * eventBus.subscribeAsync(AgentCompletedEvent.class, event -> {
 *     // Runs in separate thread
 * });
 *
 * // Cleanup
 * eventBus.close();
 * }</pre>
 */
@Slf4j
public class EventBus implements AutoCloseable {

  private final Map<Class<?>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();
  private final ExecutorService asyncExecutor;

  /** Creates a new EventBus with default async executor (10 threads). */
  public EventBus() {
    this(
        Executors.newFixedThreadPool(
            10,
            r -> {
              Thread thread = new Thread(r);
              thread.setName("event-bus-" + thread.threadId());
              thread.setDaemon(true);
              return thread;
            }));
  }

  /**
   * Creates a new EventBus with custom async executor.
   *
   * @param asyncExecutor the executor for async event processing
   */
  public EventBus(final ExecutorService asyncExecutor) {
    this.asyncExecutor = asyncExecutor;
  }

  /**
   * Subscribe to events of a specific type (synchronous).
   *
   * <p>The listener is invoked immediately when an event is published, blocking the publisher.
   *
   * @param eventType the event type
   * @param listener the event listener
   * @param <T> event type
   */
  public <T> void subscribe(final Class<T> eventType, final Consumer<T> listener) {
    listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    log.debug("Subscribed to event type: {}", eventType.getSimpleName());
  }

  /**
   * Subscribe to events of a specific type (asynchronous).
   *
   * <p>The listener is invoked in a separate thread, not blocking the publisher.
   *
   * @param eventType the event type
   * @param listener the event listener
   * @param <T> event type
   */
  public <T> void subscribeAsync(final Class<T> eventType, final Consumer<T> listener) {
    Consumer<T> asyncListener =
        event ->
            asyncExecutor.submit(
                () -> {
                  try {
                    listener.accept(event);
                  } catch (Exception e) {
                    log.error("Error in async event listener for {}", eventType.getSimpleName(), e);
                  }
                });

    subscribe(eventType, asyncListener);
  }

  /**
   * Publish an event to all subscribers.
   *
   * <p>Synchronous listeners are invoked immediately. Async listeners are submitted to the thread
   * pool.
   *
   * @param event the event to publish
   * @param <T> event type
   */
  @SuppressWarnings("unchecked")
  public <T> void publish(final T event) {
    if (event == null) {
      log.warn("Attempted to publish null event");
      return;
    }

    Class<?> eventType = event.getClass();
    List<Consumer<?>> eventListeners = listeners.get(eventType);

    if (eventListeners == null || eventListeners.isEmpty()) {
      log.trace("No listeners for event type: {}", eventType.getSimpleName());
      return;
    }

    log.debug(
        "Publishing event: {} to {} listeners", eventType.getSimpleName(), eventListeners.size());

    for (Consumer<?> listener : eventListeners) {
      try {
        ((Consumer<T>) listener).accept(event);
      } catch (Exception e) {
        log.error("Error in event listener for {}", eventType.getSimpleName(), e);
      }
    }
  }

  /**
   * Unsubscribe a specific listener.
   *
   * @param eventType the event type
   * @param listener the listener to remove
   * @param <T> event type
   */
  public <T> void unsubscribe(final Class<T> eventType, final Consumer<T> listener) {
    List<Consumer<?>> eventListeners = listeners.get(eventType);
    if (eventListeners != null) {
      eventListeners.remove(listener);
      log.debug("Unsubscribed from event type: {}", eventType.getSimpleName());
    }
  }

  /**
   * Unsubscribe all listeners for a specific event type.
   *
   * @param eventType the event type
   * @param <T> event type
   */
  public <T> void unsubscribeAll(final Class<T> eventType) {
    listeners.remove(eventType);
    log.debug("Removed all listeners for event type: {}", eventType.getSimpleName());
  }

  /**
   * Get the number of listeners for a specific event type.
   *
   * @param eventType the event type
   * @return number of listeners
   */
  public int getListenerCount(final Class<?> eventType) {
    List<Consumer<?>> eventListeners = listeners.get(eventType);
    return eventListeners != null ? eventListeners.size() : 0;
  }

  /** Close the event bus and shutdown async executor. */
  @Override
  public void close() {
    log.info("Closing EventBus ({} event types)", listeners.size());
    listeners.clear();
    asyncExecutor.shutdown();
  }
}
