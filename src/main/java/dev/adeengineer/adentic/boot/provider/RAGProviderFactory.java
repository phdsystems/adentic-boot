package dev.adeengineer.adentic.boot.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.adeengineer.adentic.boot.embedding.OpenAIEmbeddingService;
import dev.adeengineer.adentic.provider.memory.InMemoryMemoryProvider;
import dev.adeengineer.rag.embedding.EmbeddingService;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating RAG (Retrieval-Augmented Generation) provider instances.
 *
 * <p>Creates pre-configured RAG providers with:
 *
 * <ul>
 *   <li>Vector search capabilities
 *   <li>Embedding generation (OpenAI, future: local models)
 *   <li>Memory providers with similarity search
 *   <li>Environment-based configuration
 * </ul>
 *
 * <h2>Supported Capabilities</h2>
 *
 * <ul>
 *   <li><strong>Embeddings:</strong> OpenAI text-embedding-3-small/large
 *   <li><strong>Vector Search:</strong> In-memory similarity search
 *   <li><strong>Memory:</strong> Short-term and long-term memory with RAG
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Create embedding service
 * if (RAGProviderFactory.isOpenAIEmbeddingAvailable()) {
 *   EmbeddingService embeddings = RAGProviderFactory.createOpenAIEmbeddingService(objectMapper);
 *   registry.registerProvider("openai", "embedding", embeddings);
 *
 *   // Create memory provider with RAG capabilities
 *   InMemoryMemoryProvider memory = InfrastructureProviderFactory.createMemoryProvider(embeddings);
 *   registry.registerProvider("in-memory", "memory", memory);
 * }
 * }</pre>
 */
@Slf4j
public final class RAGProviderFactory {

  private RAGProviderFactory() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Create OpenAI embedding service for RAG.
   *
   * <p>Configuration via environment variables:
   *
   * <ul>
   *   <li>OPENAI_API_KEY - Required API key
   *   <li>OPENAI_EMBEDDING_MODEL - Optional model (default: text-embedding-3-small)
   * </ul>
   *
   * @param objectMapper JSON mapper for API communication
   * @return configured embedding service
   */
  public static EmbeddingService createOpenAIEmbeddingService(final ObjectMapper objectMapper) {
    log.info("Creating OpenAI embedding service for RAG");

    String apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      log.warn("OPENAI_API_KEY not set - embedding service will not be functional");
      log.warn("Set OPENAI_API_KEY environment variable to enable RAG capabilities");
    }

    String model = System.getenv().getOrDefault("OPENAI_EMBEDDING_MODEL", "text-embedding-3-small");

    EmbeddingService service = new OpenAIEmbeddingService(apiKey, model, objectMapper);
    log.info("OpenAI embedding service created with model: {}", model);
    return service;
  }

  /**
   * Create memory provider with RAG capabilities.
   *
   * <p>Provides vector-based similarity search for memory retrieval.
   *
   * @param embeddingService embedding service for vector generation
   * @return configured memory provider with RAG
   */
  public static InMemoryMemoryProvider createRAGMemoryProvider(
      final EmbeddingService embeddingService) {
    log.info("Creating RAG-enabled memory provider");

    if (embeddingService == null) {
      log.warn("No embedding service provided - memory will not support similarity search");
    }

    InMemoryMemoryProvider provider = new InMemoryMemoryProvider(embeddingService);
    log.info("RAG memory provider created successfully");
    return provider;
  }

  /**
   * Check if OpenAI embedding service can be created.
   *
   * @return true if OPENAI_API_KEY is set
   */
  public static boolean isOpenAIEmbeddingAvailable() {
    String apiKey = System.getenv("OPENAI_API_KEY");
    return apiKey != null && !apiKey.isEmpty();
  }

  /**
   * Get available embedding models.
   *
   * @return array of supported OpenAI embedding models
   */
  public static String[] getAvailableEmbeddingModels() {
    return new String[] {
      "text-embedding-3-small", // 1536 dimensions, cost-effective
      "text-embedding-3-large", // 3072 dimensions, higher quality
      "text-embedding-ada-002" // Legacy model
    };
  }
}
