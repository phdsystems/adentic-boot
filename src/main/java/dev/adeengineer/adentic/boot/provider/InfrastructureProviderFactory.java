package dev.adeengineer.adentic.boot.provider;

import dev.adeengineer.adentic.provider.memory.InMemoryMemoryProvider;
import dev.adeengineer.adentic.provider.orchestration.SimpleOrchestrationProvider;
import dev.adeengineer.adentic.provider.queue.InMemoryTaskQueueProvider;
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
   * Check if infrastructure providers can be created.
   *
   * @return true (infrastructure providers always available)
   */
  public static boolean isInfrastructureAvailable() {
    return true; // In-memory providers always available
  }
}
