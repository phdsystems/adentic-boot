package examples.infrastructure.integration;

import dev.adeengineer.adentic.boot.AgenticApplication;
import dev.adeengineer.adentic.boot.annotations.AgenticBootApplication;
import dev.adeengineer.adentic.boot.annotations.RestController;
import dev.adeengineer.adentic.boot.provider.ProviderRegistry;
import dev.adeengineer.adentic.boot.web.GetMapping;
import dev.adeengineer.adentic.boot.web.PostMapping;
import dev.adeengineer.adentic.boot.web.RequestBody;
import dev.adeengineer.adentic.boot.web.RequestParam;
import dev.adeengineer.adentic.provider.memory.InMemoryMemoryProvider;
import dev.adeengineer.rag.memory.entity.MemoryEntry;
import dev.adeengineer.rag.vectorstore.valueobject.SearchResult;
import io.javalin.http.Context;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Example demonstrating InMemoryMemoryProvider usage.
 *
 * <p>This example shows:
 *
 * <ul>
 *   <li>Storing memories with vector embeddings
 *   <li>Semantic search using vector similarity
 *   <li>Conversation history tracking
 *   <li>Metadata filtering
 *   <li>Memory management operations
 * </ul>
 *
 * <h2>Prerequisites</h2>
 *
 * Set OPENAI_API_KEY environment variable:
 *
 * <pre>
 * export OPENAI_API_KEY=sk-...
 * </pre>
 *
 * <h2>Running the Example</h2>
 *
 * <pre>
 * mvn exec:java -Dexec.mainClass="examples.infrastructure.integration.MemoryProviderExample"
 * </pre>
 *
 * <h2>Testing with curl</h2>
 *
 * <pre>
 * # Store a memory
 * curl -X POST http://localhost:8080/api/memory/store \
 *   -H "Content-Type: application/json" \
 *   -d '{"content":"Paris is the capital of France","metadata":{"type":"fact","topic":"geography"}}'
 *
 * # Search for similar memories
 * curl -X POST http://localhost:8080/api/memory/search \
 *   -H "Content-Type: application/json" \
 *   -d '{"query":"What is the capital of France?","topK":5}'
 *
 * # Get conversation history
 * curl "http://localhost:8080/api/memory/conversation/conv-123?limit=10"
 *
 * # Retrieve memory by ID
 * curl "http://localhost:8080/api/memory/retrieve/{id}"
 *
 * # Delete memory
 * curl -X DELETE "http://localhost:8080/api/memory/delete/{id}"
 *
 * # Get memory stats
 * curl "http://localhost:8080/api/memory/stats"
 *
 * # Clear all memories
 * curl -X POST "http://localhost:8080/api/memory/clear"
 * </pre>
 *
 * @see InMemoryMemoryProvider
 * @see MemoryEntry
 * @see SearchResult
 */
@Slf4j
@AgenticBootApplication
public class MemoryProviderExample {

  /**
   * Main entry point.
   *
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    String apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null || apiKey.isBlank()) {
      log.error("OPENAI_API_KEY environment variable not set");
      log.error("Please set it to use memory provider with embeddings:");
      log.error("  export OPENAI_API_KEY=sk-...");
      System.exit(1);
    }

    log.info("Starting MemoryProvider Example Application...");
    AgenticApplication.run(MemoryProviderExample.class, args);
  }

  /** REST controller for memory operations. */
  @RestController
  public static class MemoryController {

    private final ProviderRegistry registry;

    // Track conversation messages for example purposes
    private final Map<String, Integer> conversationMessageCounts = new ConcurrentHashMap<>();

    @Inject
    public MemoryController(final ProviderRegistry registry) {
      this.registry = registry;
      log.info("Initialized MemoryController");
    }

    /**
     * Store a memory entry.
     *
     * <p>Request body:
     *
     * <pre>
     * {
     *   "content": "Paris is the capital of France",
     *   "metadata": {
     *     "type": "fact",
     *     "topic": "geography"
     *   },
     *   "importance": 0.8
     * }
     * </pre>
     *
     * @param ctx Javalin context
     */
    @PostMapping("/api/memory/store")
    public void store(final Context ctx) {
      InMemoryMemoryProvider memory = getMemoryProvider();

      Map<String, Object> request = ctx.bodyAsClass(Map.class);
      String content = (String) request.get("content");
      @SuppressWarnings("unchecked")
      Map<String, Object> metadata = (Map<String, Object>) request.getOrDefault("metadata", new HashMap<>());
      Double importance = request.containsKey("importance")
          ? ((Number) request.get("importance")).doubleValue()
          : 0.5;

      MemoryEntry entry = new MemoryEntry(null, content, metadata, null, importance);

      memory
          .store(entry)
          .subscribe(
              storedEntry -> {
                log.info("Stored memory: {}", storedEntry.id());
                ctx.json(
                    Map.of(
                        "success", true,
                        "id", storedEntry.id(),
                        "timestamp", storedEntry.timestamp().toString()));
              },
              error -> {
                log.error("Failed to store memory", error);
                ctx.status(500).json(Map.of("success", false, "error", error.getMessage()));
              });
    }

    /**
     * Search for similar memories using semantic search.
     *
     * <p>Request body:
     *
     * <pre>
     * {
     *   "query": "What is the capital of France?",
     *   "topK": 5,
     *   "filters": {
     *     "type": "fact"
     *   }
     * }
     * </pre>
     *
     * @param ctx Javalin context
     */
    @PostMapping("/api/memory/search")
    public void search(final Context ctx) {
      InMemoryMemoryProvider memory = getMemoryProvider();

      Map<String, Object> request = ctx.bodyAsClass(Map.class);
      String query = (String) request.get("query");
      int topK = request.containsKey("topK") ? ((Number) request.get("topK")).intValue() : 5;
      @SuppressWarnings("unchecked")
      Map<String, Object> filters = (Map<String, Object>) request.getOrDefault("filters", Map.of());

      memory
          .search(query, topK, filters)
          .collectList()
          .subscribe(
              results -> {
                log.info("Found {} similar memories for query: {}", results.size(), query);
                List<Map<String, Object>> resultMaps =
                    results.stream()
                        .map(
                            r ->
                                Map.of(
                                    "id", r.document().id(),
                                    "content", r.document().content(),
                                    "score", r.score(),
                                    "rank", r.rank(),
                                    "metadata", r.document().metadata()))
                        .toList();
                ctx.json(Map.of("success", true, "results", resultMaps, "count", results.size()));
              },
              error -> {
                log.error("Failed to search memories", error);
                ctx.status(500).json(Map.of("success", false, "error", error.getMessage()));
              });
    }

    /**
     * Retrieve a memory by ID.
     *
     * @param ctx Javalin context
     * @param id Memory ID
     */
    @GetMapping("/api/memory/retrieve/{id}")
    public void retrieve(final Context ctx, @RequestParam("id") final String id) {
      InMemoryMemoryProvider memory = getMemoryProvider();

      memory
          .retrieve(id)
          .subscribe(
              entry -> {
                log.info("Retrieved memory: {}", id);
                ctx.json(
                    Map.of(
                        "success", true,
                        "id", entry.id(),
                        "content", entry.content(),
                        "metadata", entry.metadata(),
                        "timestamp", entry.timestamp().toString(),
                        "importance", entry.importance()));
              },
              error -> {
                log.error("Failed to retrieve memory", error);
                ctx.status(500).json(Map.of("success", false, "error", error.getMessage()));
              },
              () -> {
                log.warn("Memory not found: {}", id);
                ctx.status(404).json(Map.of("success", false, "error", "Memory not found"));
              });
    }

    /**
     * Delete a memory by ID.
     *
     * @param ctx Javalin context
     * @param id Memory ID
     */
    @PostMapping("/api/memory/delete/{id}")
    public void delete(final Context ctx, @RequestParam("id") final String id) {
      InMemoryMemoryProvider memory = getMemoryProvider();

      memory
          .delete(id)
          .subscribe(
              deleted -> {
                if (deleted) {
                  log.info("Deleted memory: {}", id);
                  ctx.json(Map.of("success", true, "message", "Memory deleted"));
                } else {
                  log.warn("Memory not found: {}", id);
                  ctx.status(404).json(Map.of("success", false, "error", "Memory not found"));
                }
              },
              error -> {
                log.error("Failed to delete memory", error);
                ctx.status(500).json(Map.of("success", false, "error", error.getMessage()));
              });
    }

    /**
     * Get conversation history.
     *
     * @param ctx Javalin context
     * @param conversationId Conversation ID
     */
    @GetMapping("/api/memory/conversation/{conversationId}")
    public void getConversationHistory(
        final Context ctx,
        @RequestParam("conversationId") final String conversationId,
        @RequestParam(value = "limit", required = false) final Integer limit) {
      InMemoryMemoryProvider memory = getMemoryProvider();
      int maxEntries = limit != null ? limit : 10;

      memory
          .getConversationHistory(conversationId, maxEntries)
          .collectList()
          .subscribe(
              entries -> {
                log.info(
                    "Retrieved {} conversation entries for: {}", entries.size(), conversationId);
                List<Map<String, Object>> entryMaps =
                    entries.stream()
                        .map(
                            e ->
                                Map.of(
                                    "id", e.id(),
                                    "content", e.content(),
                                    "metadata", e.metadata(),
                                    "timestamp", e.timestamp().toString(),
                                    "importance", e.importance()))
                        .toList();
                ctx.json(
                    Map.of(
                        "success", true,
                        "conversationId", conversationId,
                        "entries", entryMaps,
                        "count", entries.size()));
              },
              error -> {
                log.error("Failed to get conversation history", error);
                ctx.status(500).json(Map.of("success", false, "error", error.getMessage()));
              });
    }

    /**
     * Store a conversation message (helper for building conversation history).
     *
     * <p>Request body:
     *
     * <pre>
     * {
     *   "conversationId": "conv-123",
     *   "role": "user",
     *   "content": "What is the capital of France?"
     * }
     * </pre>
     *
     * @param ctx Javalin context
     */
    @PostMapping("/api/memory/conversation/message")
    public void storeConversationMessage(final Context ctx) {
      InMemoryMemoryProvider memory = getMemoryProvider();

      Map<String, Object> request = ctx.bodyAsClass(Map.class);
      String conversationId = (String) request.get("conversationId");
      String role = (String) request.get("role");
      String content = (String) request.get("content");

      // Increment message count for this conversation
      int messageIndex = conversationMessageCounts.merge(conversationId, 1, Integer::sum);

      Map<String, Object> metadata = new HashMap<>();
      metadata.put("conversation_id", conversationId);
      metadata.put("role", role);
      metadata.put("message_index", messageIndex);

      MemoryEntry entry = new MemoryEntry(null, content, metadata, null, 0.5);

      memory
          .store(entry)
          .subscribe(
              storedEntry -> {
                log.info(
                    "Stored conversation message for {}: {} ({})",
                    conversationId,
                    role,
                    storedEntry.id());
                ctx.json(
                    Map.of(
                        "success", true,
                        "id", storedEntry.id(),
                        "conversationId", conversationId,
                        "messageIndex", messageIndex));
              },
              error -> {
                log.error("Failed to store conversation message", error);
                ctx.status(500).json(Map.of("success", false, "error", error.getMessage()));
              });
    }

    /**
     * Get memory provider statistics.
     *
     * @param ctx Javalin context
     */
    @GetMapping("/api/memory/stats")
    public void getStats(final Context ctx) {
      InMemoryMemoryProvider memory = getMemoryProvider();

      boolean healthy = memory.isHealthy();
      String providerName = memory.getProviderName();

      ctx.json(
          Map.of(
              "success", true,
              "provider", providerName,
              "healthy", healthy,
              "conversations", conversationMessageCounts.size(),
              "totalMessages",
                  conversationMessageCounts.values().stream().mapToInt(Integer::intValue).sum()));
    }

    /**
     * Clear all memories (use with caution).
     *
     * @param ctx Javalin context
     */
    @PostMapping("/api/memory/clear")
    public void clear(final Context ctx) {
      InMemoryMemoryProvider memory = getMemoryProvider();

      memory
          .clear()
          .subscribe(
              count -> {
                log.warn("Cleared all memories: {} deleted", count);
                conversationMessageCounts.clear();
                ctx.json(Map.of("success", true, "deleted", count));
              },
              error -> {
                log.error("Failed to clear memories", error);
                ctx.status(500).json(Map.of("success", false, "error", error.getMessage()));
              });
    }

    /**
     * Get memory provider from registry.
     *
     * @return memory provider instance
     * @throws RuntimeException if provider not found
     */
    private InMemoryMemoryProvider getMemoryProvider() {
      return registry
          .<InMemoryMemoryProvider>getProvider("memory", "in-memory")
          .orElseThrow(
              () ->
                  new RuntimeException(
                      "InMemory memory provider not registered. Ensure OPENAI_API_KEY is set."));
    }
  }
}
