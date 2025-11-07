package dev.adeengineer.adentic.boot.provider;

import dev.adeengineer.adentic.messaging.kafka.KafkaMessageBroker;
import dev.adeengineer.adentic.messaging.rabbitmq.RabbitMQMessageBroker;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating enterprise messaging provider instances.
 *
 * <p>Creates pre-configured messaging providers from adentic-core with:
 *
 * <ul>
 *   <li>Kafka support (distributed streaming)
 *   <li>RabbitMQ support (message queuing)
 *   <li>Environment-based configuration
 *   <li>Health checks enabled
 *   <li>Ready for registration in ProviderRegistry
 * </ul>
 *
 * <h2>Supported Brokers</h2>
 *
 * <ul>
 *   <li><strong>Kafka:</strong> Distributed event streaming platform
 *   <li><strong>RabbitMQ:</strong> Reliable message queuing system
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Create Kafka broker
 * if (MessagingProviderFactory.isKafkaAvailable()) {
 *   KafkaMessageBroker kafka = MessagingProviderFactory.createKafkaBroker();
 *   registry.registerProvider("kafka", "messaging", kafka);
 * }
 *
 * // Create RabbitMQ broker
 * if (MessagingProviderFactory.isRabbitMQAvailable()) {
 *   RabbitMQMessageBroker rabbitmq = MessagingProviderFactory.createRabbitMQBroker();
 *   registry.registerProvider("rabbitmq", "messaging", rabbitmq);
 * }
 * }</pre>
 */
@Slf4j
public final class MessagingProviderFactory {

  private MessagingProviderFactory() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Create Kafka message broker with environment configuration.
   *
   * <p>Configuration via environment variables:
   *
   * <ul>
   *   <li>KAFKA_BOOTSTRAP_SERVERS - Required (e.g., localhost:9092)
   *   <li>KAFKA_CLIENT_ID - Optional client ID
   *   <li>KAFKA_GROUP_ID - Optional consumer group ID
   * </ul>
   *
   * @return configured Kafka message broker
   */
  public static KafkaMessageBroker createKafkaBroker() {
    log.info("Creating Kafka message broker");

    String bootstrapServers =
        System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");

    if (bootstrapServers.equals("localhost:9092")) {
      log.warn("Using default Kafka bootstrap servers: localhost:9092");
      log.warn("Set KAFKA_BOOTSTRAP_SERVERS environment variable for production");
    }

    KafkaMessageBroker broker = new KafkaMessageBroker(bootstrapServers);
    log.info("Kafka broker created with servers: {}", bootstrapServers);
    return broker;
  }

  /**
   * Check if Kafka broker can be created.
   *
   * @return true if KAFKA_BOOTSTRAP_SERVERS is set
   */
  public static boolean isKafkaAvailable() {
    String servers = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
    return servers != null && !servers.isEmpty();
  }

  /**
   * Create RabbitMQ message broker with environment configuration.
   *
   * <p>Configuration via environment variables:
   *
   * <ul>
   *   <li>RABBITMQ_HOST - Required hostname (default: localhost)
   *   <li>RABBITMQ_PORT - Optional port (default: 5672)
   *   <li>RABBITMQ_USERNAME - Optional username (default: guest)
   *   <li>RABBITMQ_PASSWORD - Optional password (default: guest)
   *   <li>RABBITMQ_VHOST - Optional virtual host (default: /)
   * </ul>
   *
   * @return configured RabbitMQ message broker
   */
  public static RabbitMQMessageBroker createRabbitMQBroker() {
    log.info("Creating RabbitMQ message broker");

    String host = System.getenv().getOrDefault("RABBITMQ_HOST", "localhost");
    int port = Integer.parseInt(System.getenv().getOrDefault("RABBITMQ_PORT", "5672"));

    if (host.equals("localhost")) {
      log.warn("Using default RabbitMQ host: localhost");
      log.warn("Set RABBITMQ_HOST environment variable for production");
    }

    RabbitMQMessageBroker broker = new RabbitMQMessageBroker(host, port);
    log.info("RabbitMQ broker created with host: {}:{}", host, port);
    return broker;
  }

  /**
   * Check if RabbitMQ broker can be created.
   *
   * @return true if RABBITMQ_HOST is set
   */
  public static boolean isRabbitMQAvailable() {
    String host = System.getenv("RABBITMQ_HOST");
    return host != null && !host.isEmpty();
  }
}
