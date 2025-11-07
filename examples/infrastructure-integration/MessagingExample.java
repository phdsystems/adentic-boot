package examples.infrastructure.integration;

import dev.adeengineer.adentic.boot.AgenticApplication;
import dev.adeengineer.adentic.boot.annotations.AgenticBootApplication;
import dev.adeengineer.adentic.boot.annotations.GetMapping;
import dev.adeengineer.adentic.boot.annotations.PostMapping;
import dev.adeengineer.adentic.boot.annotations.RequestBody;
import dev.adeengineer.adentic.boot.annotations.RequestParam;
import dev.adeengineer.adentic.boot.annotations.RestController;
import dev.adeengineer.adentic.boot.registry.ProviderRegistry;
import dev.adeengineer.adentic.agent.coordination.InMemoryMessageBus;
import dev.adeengineer.agent.communication.AgentMessage;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Example application demonstrating Messaging Provider integration with AgenticBoot.
 *
 * <p>This example shows:
 *
 * <ul>
 *   <li>Pub/sub messaging between agents
 *   <li>Topic-based subscriptions
 *   <li>Broadcast messages
 *   <li>Multi-agent communication
 *   <li>Message history tracking
 * </ul>
 *
 * <h2>Run</h2>
 *
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="examples.infrastructure.integration.MessagingExample"
 * }</pre>
 *
 * <h2>Test Endpoints</h2>
 *
 * <pre>{@code
 * # Subscribe an agent to a topic
 * curl -X POST "http://localhost:8080/api/messaging/subscribe?topic=tasks&agent=worker-1"
 *
 * # Publish a message to a topic
 * curl -X POST "http://localhost:8080/api/messaging/publish?topic=tasks&from=orchestrator" \
 *   -H "Content-Type: application/json" \
 *   -d '{"taskId":"task-123","action":"process","data":{"input":"test"}}'
 *
 * # Unsubscribe from a topic
 * curl -X POST "http://localhost:8080/api/messaging/unsubscribe?topic=tasks&agent=worker-1"
 *
 * # Unsubscribe from all topics
 * curl -X POST "http://localhost:8080/api/messaging/unsubscribe-all?agent=worker-1"
 *
 * # Get active topics
 * curl "http://localhost:8080/api/messaging/topics"
 *
 * # Get subscriber count for a topic
 * curl "http://localhost:8080/api/messaging/subscriber-count?topic=tasks"
 *
 * # Get message history for an agent
 * curl "http://localhost:8080/api/messaging/history?agent=worker-1"
 * }</pre>
 */
@AgenticBootApplication(port = 8080, scanBasePackages = "examples.infrastructure.integration")
public class MessagingExample {

  // In-memory message history storage (for demo purposes)
  private static final Map<String, List<AgentMessage>> MESSAGE_HISTORY = new ConcurrentHashMap<>();

  public static void main(String[] args) {
    AgenticApplication.run(MessagingExample.class, args);
  }

  /** REST controller for Messaging operations. */
  @Slf4j
  @RestController
  public static class MessagingController {

    @Inject private ProviderRegistry registry;

    /**
     * Subscribe an agent to a topic.
     *
     * <p>Example: {@code curl -X POST
     * "http://localhost:8080/api/messaging/subscribe?topic=tasks&agent=worker-1"}
     */
    @PostMapping("/api/messaging/subscribe")
    public Mono<Map<String, Object>> subscribe(
        @RequestParam("topic") String topic, @RequestParam("agent") String agentName) {
      log.info("Subscribing agent '{}' to topic '{}'", agentName, topic);

      return getMessageBus()
          .map(
              bus -> {
                // Subscribe agent with a handler that stores messages
                bus.subscribe(
                    topic,
                    agentName,
                    message -> {
                      log.info(
                          "Agent '{}' received message on topic '{}': {}",
                          agentName,
                          topic,
                          message.payload());

                      // Store message in history
                      MESSAGE_HISTORY
                          .computeIfAbsent(agentName, k -> new ArrayList<>())
                          .add(message);
                    });

                return Map.of(
                    "status", "subscribed",
                    "agent", agentName,
                    "topic", topic);
              });
    }

    /**
     * Publish a message to a topic.
     *
     * <p>Example: {@code curl -X POST
     * "http://localhost:8080/api/messaging/publish?topic=tasks&from=orchestrator" -H
     * "Content-Type: application/json" -d
     * '{"taskId":"task-123","action":"process","data":{"input":"test"}}'}
     */
    @PostMapping("/api/messaging/publish")
    public Mono<Map<String, Object>> publish(
        @RequestParam("topic") String topic,
        @RequestParam("from") String fromAgent,
        @RequestBody Map<String, Object> payload) {
      log.info("Publishing message to topic '{}' from agent '{}'", topic, fromAgent);

      return getMessageBus()
          .map(
              bus -> {
                // Create broadcast message
                AgentMessage message = AgentMessage.broadcast(fromAgent, topic, payload);

                // Publish to topic
                bus.publish(topic, message);

                int subscriberCount = bus.getSubscriberCount(topic);

                return Map.of(
                    "status", "published",
                    "topic", topic,
                    "from", fromAgent,
                    "subscribers", subscriberCount,
                    "messageId", message.id());
              });
    }

    /**
     * Unsubscribe an agent from a topic.
     *
     * <p>Example: {@code curl -X POST
     * "http://localhost:8080/api/messaging/unsubscribe?topic=tasks&agent=worker-1"}
     */
    @PostMapping("/api/messaging/unsubscribe")
    public Mono<Map<String, Object>> unsubscribe(
        @RequestParam("topic") String topic, @RequestParam("agent") String agentName) {
      log.info("Unsubscribing agent '{}' from topic '{}'", agentName, topic);

      return getMessageBus()
          .map(
              bus -> {
                bus.unsubscribe(topic, agentName);

                return Map.of(
                    "status", "unsubscribed",
                    "agent", agentName,
                    "topic", topic);
              });
    }

    /**
     * Unsubscribe an agent from all topics.
     *
     * <p>Example: {@code curl -X POST
     * "http://localhost:8080/api/messaging/unsubscribe-all?agent=worker-1"}
     */
    @PostMapping("/api/messaging/unsubscribe-all")
    public Mono<Map<String, Object>> unsubscribeAll(@RequestParam("agent") String agentName) {
      log.info("Unsubscribing agent '{}' from all topics", agentName);

      return getMessageBus()
          .map(
              bus -> {
                bus.unsubscribeAll(agentName);

                return Map.of(
                    "status", "unsubscribed-all",
                    "agent", agentName);
              });
    }

    /**
     * Get all active topics.
     *
     * <p>Example: {@code curl "http://localhost:8080/api/messaging/topics"}
     */
    @GetMapping("/api/messaging/topics")
    public Mono<Map<String, Object>> getActiveTopics() {
      log.info("Getting active topics");

      return getMessageBus()
          .map(
              bus -> {
                List<String> topics = bus.getActiveTopics();

                return Map.of(
                    "topics", topics,
                    "count", topics.size());
              });
    }

    /**
     * Get subscriber count for a topic.
     *
     * <p>Example: {@code curl "http://localhost:8080/api/messaging/subscriber-count?topic=tasks"}
     */
    @GetMapping("/api/messaging/subscriber-count")
    public Mono<Map<String, Object>> getSubscriberCount(@RequestParam("topic") String topic) {
      log.info("Getting subscriber count for topic '{}'", topic);

      return getMessageBus()
          .map(
              bus -> {
                int count = bus.getSubscriberCount(topic);

                return Map.of(
                    "topic", topic,
                    "subscribers", count);
              });
    }

    /**
     * Get message history for an agent.
     *
     * <p>Example: {@code curl "http://localhost:8080/api/messaging/history?agent=worker-1"}
     */
    @GetMapping("/api/messaging/history")
    public Mono<Map<String, Object>> getMessageHistory(@RequestParam("agent") String agentName) {
      log.info("Getting message history for agent '{}'", agentName);

      List<AgentMessage> messages = MESSAGE_HISTORY.getOrDefault(agentName, List.of());

      List<Map<String, Object>> formattedMessages =
          messages.stream()
              .map(
                  msg ->
                      Map.of(
                          "id", msg.id(),
                          "from", msg.fromAgent(),
                          "topic", msg.topic() != null ? msg.topic() : "",
                          "payload", msg.payload(),
                          "timestamp", msg.timestamp().toString()))
              .toList();

      return Mono.just(
          Map.of(
              "agent", agentName,
              "messageCount", formattedMessages.size(),
              "messages", formattedMessages));
    }

    /**
     * Clear message history for an agent.
     *
     * <p>Example: {@code curl -X POST
     * "http://localhost:8080/api/messaging/clear-history?agent=worker-1"}
     */
    @PostMapping("/api/messaging/clear-history")
    public Mono<Map<String, Object>> clearHistory(@RequestParam("agent") String agentName) {
      log.info("Clearing message history for agent '{}'", agentName);

      List<AgentMessage> removed = MESSAGE_HISTORY.remove(agentName);
      int count = removed != null ? removed.size() : 0;

      return Mono.just(
          Map.of(
              "status", "cleared",
              "agent", agentName,
              "messagesRemoved", count));
    }

    /**
     * Clear all subscriptions (reset message bus).
     *
     * <p>Example: {@code curl -X POST "http://localhost:8080/api/messaging/clear-all"}
     */
    @PostMapping("/api/messaging/clear-all")
    public Mono<Map<String, Object>> clearAll() {
      log.info("Clearing all subscriptions");

      return getMessageBus()
          .map(
              bus -> {
                bus.clear();
                MESSAGE_HISTORY.clear();

                return Map.of("status", "cleared-all");
              });
    }

    private Mono<InMemoryMessageBus> getMessageBus() {
      return Mono.justOrEmpty(registry.getProvider("messaging", "in-memory"))
          .switchIfEmpty(Mono.error(new RuntimeException("InMemoryMessageBus not found")));
    }
  }
}
