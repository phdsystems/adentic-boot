package dev.adeengineer.adentic.boot.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.adeengineer.adentic.agent.coordination.InMemoryMessageBus;
import dev.adeengineer.adentic.provider.memory.InMemoryMemoryProvider;
import dev.adeengineer.adentic.provider.orchestration.SimpleOrchestrationProvider;
import dev.adeengineer.adentic.provider.queue.InMemoryTaskQueueProvider;
import dev.adeengineer.adentic.provider.tools.MavenToolProvider;
import dev.adeengineer.adentic.provider.tools.SimpleToolProvider;
import dev.adeengineer.adentic.storage.local.LocalStorageProvider;
import dev.adeengineer.adentic.tool.config.MavenToolConfig;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating infrastructure provider instances.
 *
 * <p>Creates pre-configured infrastructure providers from adentic-core with:
 *
 * <ul>
 *   <li>Memory providers (short-term, long-term storage)
 *   <li>Task queue providers (async task processing)
 *   <li>Orchestration providers (workflow execution)
 *   <li>Tool providers (function calling, Maven operations)
 *   <li>Storage providers (document/artifact storage)
 *   <li>Messaging providers (pub/sub communication)
 *   <li>Ready for registration in ProviderRegistry
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Create memory provider
 * InMemoryMemoryProvider memoryProvider = InfrastructureProviderFactory.createMemoryProvider(embeddingService);
 *
 * // Register in ProviderRegistry
 * registry.registerProvider("in-memory", "memory", memoryProvider);
 * }</pre>
 */
@Slf4j
public final class InfrastructureProviderFactory {

  private InfrastructureProviderFactory() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Create in-memory memory provider.
   *
   * <p>Provides short-term memory storage with vector similarity search.
   *
   * @param embeddingService embedding service for vector search
   * @return configured memory provider
   */
  public static InMemoryMemoryProvider createMemoryProvider(
      final dev.adeengineer.rag.embedding.EmbeddingService embeddingService) {
    log.info("Creating InMemory memory provider");

    if (embeddingService == null) {
      log.warn("EmbeddingService not provided - memory provider will have limited functionality");
    }

    return new InMemoryMemoryProvider(embeddingService);
  }

  /**
   * Create in-memory task queue provider.
   *
   * <p>Provides async task queuing with priority support.
   *
   * @return configured task queue provider
   */
  public static InMemoryTaskQueueProvider createTaskQueueProvider() {
    log.info("Creating InMemory task queue provider");
    return new InMemoryTaskQueueProvider();
  }

  /**
   * Create simple orchestration provider.
   *
   * <p>Provides sequential workflow execution and monitoring.
   *
   * @return configured orchestration provider
   */
  public static SimpleOrchestrationProvider createOrchestrationProvider() {
    log.info("Creating Simple orchestration provider");
    return new SimpleOrchestrationProvider();
  }

  /**
   * Create simple tool provider.
   *
   * <p>Provides dynamic function calling with built-in tools (echo, calculator, timestamp).
   *
   * @return configured tool provider
   */
  public static SimpleToolProvider createSimpleToolProvider() {
    log.info("Creating Simple tool provider");
    return new SimpleToolProvider();
  }

  /**
   * Create Maven tool provider.
   *
   * <p>Provides Maven operations with 8 built-in tools (compile, test, package, etc.).
   *
   * @param config Maven tool configuration
   * @return configured Maven tool provider
   */
  public static MavenToolProvider createMavenToolProvider(final MavenToolConfig config) {
    log.info("Creating Maven tool provider with config: {}", config);
    return new MavenToolProvider(config);
  }

  /**
   * Create local storage provider.
   *
   * <p>Provides file-based document storage with metadata querying.
   *
   * @param storagePath base directory path for storage
   * @param objectMapper JSON mapper for metadata serialization
   * @return configured storage provider
   * @throws IOException if storage directory cannot be created
   */
  public static LocalStorageProvider createLocalStorageProvider(
      final String storagePath, final ObjectMapper objectMapper) throws IOException {
    log.info("Creating Local storage provider at: {}", storagePath);
    return new LocalStorageProvider(storagePath, objectMapper);
  }

  /**
   * Create in-memory message bus.
   *
   * <p>Provides pub/sub messaging for agent communication.
   *
   * @return configured message bus
   */
  public static InMemoryMessageBus createMessageBus() {
    log.info("Creating InMemory message bus");
    return new InMemoryMessageBus();
  }

  /**
   * Check if infrastructure providers can be created.
   *
   * @return true (infrastructure providers always available)
   */
  public static boolean isInfrastructureAvailable() {
    return true; // In-memory providers always available
  }
}
