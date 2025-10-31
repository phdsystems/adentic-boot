package dev.adeengineer.adentic.boot.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for EventBus.
 *
 * <p>Tests synchronous and asynchronous event publishing, subscription management, and cleanup.
 */
@DisplayName("EventBus Tests")
class EventBusTest {

  private EventBus eventBus;

  @BeforeEach
  void setUp() {
    eventBus = new EventBus();
  }

  @AfterEach
  void tearDown() {
    if (eventBus != null) {
      eventBus.close();
    }
  }

  // Constructor Tests

  @Test
  @DisplayName("Should create EventBus with default executor")
  void shouldCreateWithDefaultExecutor() {
    EventBus bus = new EventBus();

    assertThat(bus).isNotNull();
    assertThat(bus.getListenerCount(TestEvent.class)).isZero();

    bus.close();
  }

  @Test
  @DisplayName("Should create EventBus with custom executor")
  void shouldCreateWithCustomExecutor() {
    ExecutorService customExecutor = Executors.newFixedThreadPool(5);
    EventBus bus = new EventBus(customExecutor);

    assertThat(bus).isNotNull();
    assertThat(bus.getListenerCount(TestEvent.class)).isZero();

    bus.close();
  }

  // Subscribe Tests

  @Test
  @DisplayName("Should subscribe to event type")
  void shouldSubscribeToEventType() {
    List<TestEvent> receivedEvents = new ArrayList<>();

    eventBus.subscribe(TestEvent.class, receivedEvents::add);

    assertThat(eventBus.getListenerCount(TestEvent.class)).isEqualTo(1);
  }

  @Test
  @DisplayName("Should allow multiple subscribers for same event type")
  void shouldAllowMultipleSubscribers() {
    List<TestEvent> receivedEvents1 = new ArrayList<>();
    List<TestEvent> receivedEvents2 = new ArrayList<>();

    eventBus.subscribe(TestEvent.class, receivedEvents1::add);
    eventBus.subscribe(TestEvent.class, receivedEvents2::add);

    assertThat(eventBus.getListenerCount(TestEvent.class)).isEqualTo(2);
  }

  @Test
  @DisplayName("Should track listener count for different event types")
  void shouldTrackListenerCountByEventType() {
    eventBus.subscribe(TestEvent.class, event -> {});
    eventBus.subscribe(AnotherTestEvent.class, event -> {});
    eventBus.subscribe(AnotherTestEvent.class, event -> {});

    assertThat(eventBus.getListenerCount(TestEvent.class)).isEqualTo(1);
    assertThat(eventBus.getListenerCount(AnotherTestEvent.class)).isEqualTo(2);
    assertThat(eventBus.getListenerCount(YetAnotherEvent.class)).isZero();
  }

  // SubscribeAsync Tests

  @Test
  @DisplayName("Should subscribe async to event type")
  void shouldSubscribeAsyncToEventType() {
    List<TestEvent> receivedEvents = Collections.synchronizedList(new ArrayList<>());

    eventBus.subscribeAsync(TestEvent.class, receivedEvents::add);

    assertThat(eventBus.getListenerCount(TestEvent.class)).isEqualTo(1);
  }

  @Test
  @DisplayName("Should handle async listener exceptions without crashing")
  void shouldHandleAsyncListenerExceptions() throws InterruptedException {
    AtomicInteger callCount = new AtomicInteger(0);
    CountDownLatch latch = new CountDownLatch(1);

    eventBus.subscribeAsync(
        TestEvent.class,
        event -> {
          callCount.incrementAndGet();
          latch.countDown();
          throw new RuntimeException("Intentional exception in async listener");
        });

    TestEvent event = new TestEvent("test");
    eventBus.publish(event);

    // Wait for async execution
    boolean completed = latch.await(2, TimeUnit.SECONDS);

    assertThat(completed).isTrue();
    assertThat(callCount.get()).isEqualTo(1);
  }

  // Publish Tests

  @Test
  @DisplayName("Should publish event to subscribers")
  void shouldPublishEventToSubscribers() {
    List<TestEvent> receivedEvents = new ArrayList<>();
    eventBus.subscribe(TestEvent.class, receivedEvents::add);

    TestEvent event = new TestEvent("test-message");
    eventBus.publish(event);

    assertThat(receivedEvents).hasSize(1);
    assertThat(receivedEvents.get(0).getMessage()).isEqualTo("test-message");
  }

  @Test
  @DisplayName("Should publish event to all subscribers")
  void shouldPublishEventToAllSubscribers() {
    List<TestEvent> receivedEvents1 = new ArrayList<>();
    List<TestEvent> receivedEvents2 = new ArrayList<>();
    List<TestEvent> receivedEvents3 = new ArrayList<>();

    eventBus.subscribe(TestEvent.class, receivedEvents1::add);
    eventBus.subscribe(TestEvent.class, receivedEvents2::add);
    eventBus.subscribe(TestEvent.class, receivedEvents3::add);

    TestEvent event = new TestEvent("broadcast");
    eventBus.publish(event);

    assertThat(receivedEvents1).hasSize(1);
    assertThat(receivedEvents2).hasSize(1);
    assertThat(receivedEvents3).hasSize(1);
  }

  @Test
  @DisplayName("Should handle null event gracefully")
  void shouldHandleNullEvent() {
    List<TestEvent> receivedEvents = new ArrayList<>();
    eventBus.subscribe(TestEvent.class, receivedEvents::add);

    eventBus.publish(null);

    assertThat(receivedEvents).isEmpty();
  }

  @Test
  @DisplayName("Should handle event with no subscribers")
  void shouldHandleEventWithNoSubscribers() {
    TestEvent event = new TestEvent("orphan");

    // Should not throw exception
    eventBus.publish(event);
  }

  @Test
  @DisplayName("Should handle listener exceptions without affecting other listeners")
  void shouldHandleListenerExceptions() {
    List<TestEvent> receivedEvents1 = new ArrayList<>();
    List<TestEvent> receivedEvents2 = new ArrayList<>();

    eventBus.subscribe(
        TestEvent.class,
        event -> {
          receivedEvents1.add(event);
          throw new RuntimeException("Intentional exception");
        });

    eventBus.subscribe(TestEvent.class, receivedEvents2::add);

    TestEvent event = new TestEvent("test");
    eventBus.publish(event);

    assertThat(receivedEvents1).hasSize(1);
    assertThat(receivedEvents2).hasSize(1);
  }

  @Test
  @DisplayName("Should publish async events")
  void shouldPublishAsyncEvents() throws InterruptedException {
    List<TestEvent> receivedEvents = Collections.synchronizedList(new ArrayList<>());
    CountDownLatch latch = new CountDownLatch(1);

    eventBus.subscribeAsync(
        TestEvent.class,
        event -> {
          receivedEvents.add(event);
          latch.countDown();
        });

    TestEvent event = new TestEvent("async");
    eventBus.publish(event);

    // Wait for async execution
    boolean completed = latch.await(2, TimeUnit.SECONDS);

    assertThat(completed).isTrue();
    assertThat(receivedEvents).hasSize(1);
    assertThat(receivedEvents.get(0).getMessage()).isEqualTo("async");
  }

  @Test
  @DisplayName("Should handle multiple events published sequentially")
  void shouldHandleMultipleEventsSequentially() {
    List<TestEvent> receivedEvents = new ArrayList<>();
    eventBus.subscribe(TestEvent.class, receivedEvents::add);

    eventBus.publish(new TestEvent("event1"));
    eventBus.publish(new TestEvent("event2"));
    eventBus.publish(new TestEvent("event3"));

    assertThat(receivedEvents).hasSize(3);
    assertThat(receivedEvents.get(0).getMessage()).isEqualTo("event1");
    assertThat(receivedEvents.get(1).getMessage()).isEqualTo("event2");
    assertThat(receivedEvents.get(2).getMessage()).isEqualTo("event3");
  }

  // Unsubscribe Tests

  @Test
  @DisplayName("Should unsubscribe specific listener")
  void shouldUnsubscribeSpecificListener() {
    List<TestEvent> receivedEvents = new ArrayList<>();
    java.util.function.Consumer<TestEvent> listener = receivedEvents::add;

    eventBus.subscribe(TestEvent.class, listener);
    assertThat(eventBus.getListenerCount(TestEvent.class)).isEqualTo(1);

    eventBus.unsubscribe(TestEvent.class, listener);
    assertThat(eventBus.getListenerCount(TestEvent.class)).isZero();

    eventBus.publish(new TestEvent("test"));
    assertThat(receivedEvents).isEmpty();
  }

  @Test
  @DisplayName("Should unsubscribe only specified listener")
  void shouldUnsubscribeOnlySpecifiedListener() {
    List<TestEvent> receivedEvents1 = new ArrayList<>();
    List<TestEvent> receivedEvents2 = new ArrayList<>();

    java.util.function.Consumer<TestEvent> listener1 = receivedEvents1::add;
    java.util.function.Consumer<TestEvent> listener2 = receivedEvents2::add;

    eventBus.subscribe(TestEvent.class, listener1);
    eventBus.subscribe(TestEvent.class, listener2);

    eventBus.unsubscribe(TestEvent.class, listener1);

    assertThat(eventBus.getListenerCount(TestEvent.class)).isEqualTo(1);

    eventBus.publish(new TestEvent("test"));
    assertThat(receivedEvents1).isEmpty();
    assertThat(receivedEvents2).hasSize(1);
  }

  @Test
  @DisplayName("Should handle unsubscribe for non-existent listener")
  void shouldHandleUnsubscribeForNonExistentListener() {
    List<TestEvent> receivedEvents = new ArrayList<>();
    java.util.function.Consumer<TestEvent> listener = receivedEvents::add;

    // Should not throw exception
    eventBus.unsubscribe(TestEvent.class, listener);
  }

  // UnsubscribeAll Tests

  @Test
  @DisplayName("Should unsubscribe all listeners for event type")
  void shouldUnsubscribeAllListeners() {
    List<TestEvent> receivedEvents1 = new ArrayList<>();
    List<TestEvent> receivedEvents2 = new ArrayList<>();

    eventBus.subscribe(TestEvent.class, receivedEvents1::add);
    eventBus.subscribe(TestEvent.class, receivedEvents2::add);

    assertThat(eventBus.getListenerCount(TestEvent.class)).isEqualTo(2);

    eventBus.unsubscribeAll(TestEvent.class);

    assertThat(eventBus.getListenerCount(TestEvent.class)).isZero();

    eventBus.publish(new TestEvent("test"));
    assertThat(receivedEvents1).isEmpty();
    assertThat(receivedEvents2).isEmpty();
  }

  @Test
  @DisplayName("Should handle unsubscribeAll for non-existent event type")
  void shouldHandleUnsubscribeAllForNonExistentEventType() {
    // Should not throw exception
    eventBus.unsubscribeAll(TestEvent.class);
  }

  // GetListenerCount Tests

  @Test
  @DisplayName("Should return zero for event type with no listeners")
  void shouldReturnZeroForNoListeners() {
    assertThat(eventBus.getListenerCount(TestEvent.class)).isZero();
  }

  @Test
  @DisplayName("Should return correct listener count")
  void shouldReturnCorrectListenerCount() {
    eventBus.subscribe(TestEvent.class, event -> {});
    eventBus.subscribe(TestEvent.class, event -> {});
    eventBus.subscribe(TestEvent.class, event -> {});

    assertThat(eventBus.getListenerCount(TestEvent.class)).isEqualTo(3);
  }

  // Close Tests

  @Test
  @DisplayName("Should close and cleanup resources")
  void shouldCloseAndCleanup() {
    eventBus.subscribe(TestEvent.class, event -> {});
    eventBus.subscribe(AnotherTestEvent.class, event -> {});

    eventBus.close();

    assertThat(eventBus.getListenerCount(TestEvent.class)).isZero();
    assertThat(eventBus.getListenerCount(AnotherTestEvent.class)).isZero();
  }

  @Test
  @DisplayName("Should shutdown executor on close")
  void shouldShutdownExecutorOnClose() {
    ExecutorService customExecutor = Executors.newFixedThreadPool(1);
    EventBus bus = new EventBus(customExecutor);

    bus.close();

    assertThat(customExecutor.isShutdown()).isTrue();
  }

  // Integration Tests

  @Test
  @DisplayName("Should handle mixed sync and async subscribers")
  void shouldHandleMixedSyncAndAsyncSubscribers() throws InterruptedException {
    List<TestEvent> syncEvents = new ArrayList<>();
    List<TestEvent> asyncEvents = Collections.synchronizedList(new ArrayList<>());
    CountDownLatch latch = new CountDownLatch(1);

    eventBus.subscribe(TestEvent.class, syncEvents::add);
    eventBus.subscribeAsync(
        TestEvent.class,
        event -> {
          asyncEvents.add(event);
          latch.countDown();
        });

    TestEvent event = new TestEvent("mixed");
    eventBus.publish(event);

    assertThat(syncEvents).hasSize(1);

    boolean completed = latch.await(2, TimeUnit.SECONDS);

    assertThat(completed).isTrue();
    assertThat(asyncEvents).hasSize(1);
  }

  @Test
  @DisplayName("Should handle high volume of events")
  void shouldHandleHighVolumeOfEvents() {
    List<TestEvent> receivedEvents = Collections.synchronizedList(new ArrayList<>());
    eventBus.subscribe(TestEvent.class, receivedEvents::add);

    int eventCount = 1000;
    for (int i = 0; i < eventCount; i++) {
      eventBus.publish(new TestEvent("event-" + i));
    }

    assertThat(receivedEvents).hasSize(eventCount);
  }

  // Test Event Classes

  static class TestEvent {
    private final String message;

    TestEvent(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }

  static class AnotherTestEvent {
    private final String data;

    AnotherTestEvent(String data) {
      this.data = data;
    }

    public String getData() {
      return data;
    }
  }

  static class YetAnotherEvent {
    private final int value;

    YetAnotherEvent(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }
}
