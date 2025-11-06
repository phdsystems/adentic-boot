package dev.adeengineer.adentic.boot.integration;

import static org.junit.jupiter.api.Assertions.*;

import dev.adeengineer.adentic.boot.annotations.AgenticBootApplication;
import dev.adeengineer.adentic.boot.context.AgenticContext;
import dev.adeengineer.adentic.boot.event.EventBus;
import dev.adeengineer.adentic.boot.integration.event.AgentRegisteredEvent;
import dev.adeengineer.adentic.boot.registry.ProviderRegistry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for agent EventBus integration.
 *
 * <p>Tests event publishing and subscription:
 *
 * <ul>
 *   <li>Agent registered events
 *   <li>Synchronous event listeners
 *   <li>Asynchronous event listeners
 *   <li>Multiple subscribers
 * </ul>
 */
@DisplayName("Agent EventBus Integration Tests")
class AgentEventBusIntegrationTest {

  private static AgenticContext context;
  private static EventBus eventBus;
  private static ProviderRegistry registry;

  @BeforeAll
  static void setUp() {
    context = dev.adeengineer.adentic.boot.AgenticApplication.run(TestApp.class);
    eventBus = context.getBean(EventBus.class);
    registry = context.getBean(ProviderRegistry.class);
  }

  @AfterAll
  static void tearDown() {
    if (context != null) {
      context.close();
    }
  }

  @Test
  @DisplayName("Should publish and receive agent registered event synchronously")
  void testSyncAgentRegisteredEvent() {
    // Given: Subscriber listening for agent registered events
    AtomicReference<String> receivedAgentName = new AtomicReference<>();
    eventBus.subscribe(
        AgentRegisteredEvent.class,
        event -> {
          receivedAgentName.set(event.getAgentName());
        });

    // When: Register agent and publish event
    TestAgent agent = new TestAgent("sync-agent", "response");
    registry.registerAgent("sync-agent", agent);
    eventBus.publish(new AgentRegisteredEvent(agent));

    // Then: Event should be received
    assertEquals("sync-agent", receivedAgentName.get(), "Should receive correct agent name");
  }

  @Test
  @DisplayName("Should publish and receive agent registered event asynchronously")
  void testAsyncAgentRegisteredEvent() throws InterruptedException {
    // Given: Async subscriber with countdown latch
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<String> receivedAgentName = new AtomicReference<>();

    eventBus.subscribeAsync(
        AgentRegisteredEvent.class,
        event -> {
          receivedAgentName.set(event.getAgentName());
          latch.countDown();
        });

    // When: Register agent and publish event
    TestAgent agent = new TestAgent("async-agent", "response");
    registry.registerAgent("async-agent", agent);
    eventBus.publish(new AgentRegisteredEvent(agent));

    // Then: Wait for async processing and verify
    assertTrue(latch.await(2, TimeUnit.SECONDS), "Should receive event within timeout");
    assertEquals("async-agent", receivedAgentName.get(), "Should receive correct agent name");
  }

  @Test
  @DisplayName("Should notify multiple subscribers of agent registration")
  void testMultipleSubscribers() {
    // Given: Multiple subscribers
    AtomicInteger callCount = new AtomicInteger(0);

    eventBus.subscribe(AgentRegisteredEvent.class, event -> callCount.incrementAndGet());
    eventBus.subscribe(AgentRegisteredEvent.class, event -> callCount.incrementAndGet());
    eventBus.subscribe(AgentRegisteredEvent.class, event -> callCount.incrementAndGet());

    // When: Publish event
    TestAgent agent = new TestAgent("multi-agent", "response");
    eventBus.publish(new AgentRegisteredEvent(agent));

    // Then: All subscribers should be notified
    assertEquals(3, callCount.get(), "All 3 subscribers should be notified");
  }

  @Test
  @DisplayName("Should handle agent registered event with full details")
  void testEventDetails() {
    // Given: Subscriber capturing event details
    AtomicReference<AgentRegisteredEvent> receivedEvent = new AtomicReference<>();
    eventBus.subscribe(AgentRegisteredEvent.class, receivedEvent::set);

    // When: Publish event with test agent
    TestAgent agent = new TestAgent("detailed-agent", "test response");
    registry.registerAgent("detailed-agent", agent);
    eventBus.publish(new AgentRegisteredEvent(agent));

    // Then: Event should contain all details
    AgentRegisteredEvent event = receivedEvent.get();
    assertNotNull(event, "Event should not be null");
    assertNotNull(event.getAgent(), "Agent should not be null");
    assertEquals("detailed-agent", event.getAgentName(), "Agent name should match");
    assertTrue(event.getTimestamp() > 0, "Timestamp should be set");
  }

  @Test
  @DisplayName("Should handle unsubscribe correctly")
  void testUnsubscribe() {
    // Given: Subscriber that will be unsubscribed
    AtomicInteger callCount = new AtomicInteger(0);
    java.util.function.Consumer<AgentRegisteredEvent> listener =
        event -> callCount.incrementAndGet();

    eventBus.subscribe(AgentRegisteredEvent.class, listener);

    // When: Publish first event, unsubscribe, then publish second event
    TestAgent agent1 = new TestAgent("agent1", "response1");
    eventBus.publish(new AgentRegisteredEvent(agent1));

    eventBus.unsubscribe(AgentRegisteredEvent.class, listener);

    TestAgent agent2 = new TestAgent("agent2", "response2");
    eventBus.publish(new AgentRegisteredEvent(agent2));

    // Then: Should only receive first event
    assertEquals(1, callCount.get(), "Should only receive first event before unsubscribe");
  }

  @Test
  @DisplayName("Should handle no subscribers gracefully")
  void testNoSubscribers() {
    // Given: No subscribers

    // When: Publish event
    TestAgent agent = new TestAgent("orphan-agent", "response");

    // Then: Should not throw exception
    assertDoesNotThrow(
        () -> {
          eventBus.publish(new AgentRegisteredEvent(agent));
        },
        "Publishing with no subscribers should not throw");
  }

  @Test
  @DisplayName("Should get listener count correctly")
  void testListenerCount() {
    // Given: Multiple subscribers
    eventBus.subscribe(AgentRegisteredEvent.class, event -> {});
    eventBus.subscribe(AgentRegisteredEvent.class, event -> {});
    eventBus.subscribe(AgentRegisteredEvent.class, event -> {});

    // When: Get listener count
    int count = eventBus.getListenerCount(AgentRegisteredEvent.class);

    // Then: Should return correct count
    assertEquals(3, count, "Should have 3 listeners");
  }

  /** Test application class. */
  @AgenticBootApplication(
      port = 8083,
      scanBasePackages = "dev.adeengineer.adentic.boot.integration")
  public static class TestApp {
    // Test application marker class
  }
}
