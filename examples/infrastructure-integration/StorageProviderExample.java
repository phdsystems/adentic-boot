package examples.infrastructure.integration;

import dev.adeengineer.adentic.boot.AgenticApplication;
import dev.adeengineer.adentic.boot.annotations.AgenticBootApplication;
import dev.adeengineer.adentic.boot.annotations.DeleteMapping;
import dev.adeengineer.adentic.boot.annotations.GetMapping;
import dev.adeengineer.adentic.boot.annotations.PathVariable;
import dev.adeengineer.adentic.boot.annotations.PostMapping;
import dev.adeengineer.adentic.boot.annotations.RequestBody;
import dev.adeengineer.adentic.boot.annotations.RequestParam;
import dev.adeengineer.adentic.boot.annotations.RestController;
import dev.adeengineer.adentic.boot.registry.ProviderRegistry;
import dev.adeengineer.adentic.storage.local.LocalStorageProvider;
import dev.adeengineer.storage.model.Document;
import dev.adeengineer.storage.model.StorageQuery;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Example application demonstrating Storage Provider integration with AgenticBoot.
 *
 * <p>This example shows:
 *
 * <ul>
 *   <li>Document storage and retrieval
 *   <li>Metadata-based querying
 *   <li>File operations (store, retrieve, delete)
 *   <li>Storage statistics
 * </ul>
 *
 * <h2>Run</h2>
 *
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="examples.infrastructure.integration.StorageProviderExample"
 * }</pre>
 *
 * <h2>Test Endpoints</h2>
 *
 * <pre>{@code
 * # Store a document
 * curl -X POST "http://localhost:8080/api/storage/store" \
 *   -H "Content-Type: application/json" \
 *   -d '{"content":"Hello, World!","contentType":"text/plain","metadata":{"author":"user","category":"notes"}}'
 *
 * # Retrieve a document
 * curl "http://localhost:8080/api/storage/retrieve/<document-id>"
 *
 * # Delete a document
 * curl -X DELETE "http://localhost:8080/api/storage/delete/<document-id>"
 *
 * # Query documents by metadata
 * curl -X POST "http://localhost:8080/api/storage/query" \
 *   -H "Content-Type: application/json" \
 *   -d '{"filters":{"category":"notes"},"limit":10,"offset":0}'
 *
 * # Check if document exists
 * curl "http://localhost:8080/api/storage/exists/<document-id>"
 *
 * # Get total storage size
 * curl "http://localhost:8080/api/storage/stats"
 * }</pre>
 */
@AgenticBootApplication(port = 8080, scanBasePackages = "examples.infrastructure.integration")
public class StorageProviderExample {

  public static void main(String[] args) {
    AgenticApplication.run(StorageProviderExample.class, args);
  }

  /** REST controller for Storage operations. */
  @Slf4j
  @RestController
  public static class StorageController {

    @Inject private ProviderRegistry registry;

    /**
     * Store a document.
     *
     * <p>Example: {@code curl -X POST "http://localhost:8080/api/storage/store" -H "Content-Type:
     * application/json" -d '{"content":"Hello,
     * World!","contentType":"text/plain","metadata":{"author":"user","category":"notes"}}'}
     */
    @PostMapping("/api/storage/store")
    public Mono<Map<String, Object>> store(@RequestBody Map<String, Object> request) {
      log.info("Storing document");

      String content = (String) request.get("content");
      String contentType = (String) request.getOrDefault("contentType", "text/plain");
      @SuppressWarnings("unchecked")
      Map<String, Object> metadata = (Map<String, Object>) request.getOrDefault("metadata", Map.of());

      Document document =
          new Document(
              null,
              content.getBytes(StandardCharsets.UTF_8),
              contentType,
              metadata,
              null,
              0);

      return getStorageProvider()
          .flatMap(provider -> provider.store(document))
          .map(
              stored ->
                  Map.of(
                      "id", stored.id(),
                      "contentType", stored.contentType(),
                      "size", stored.size(),
                      "createdAt", stored.createdAt().toString(),
                      "metadata", stored.metadata()));
    }

    /**
     * Retrieve a document by ID.
     *
     * <p>Example: {@code curl "http://localhost:8080/api/storage/retrieve/<document-id>"}
     */
    @GetMapping("/api/storage/retrieve/{id}")
    public Mono<Map<String, Object>> retrieve(@PathVariable String id) {
      log.info("Retrieving document: {}", id);

      return getStorageProvider()
          .flatMap(provider -> provider.retrieve(id))
          .map(
              doc ->
                  Map.of(
                      "id", doc.id(),
                      "content", new String(doc.content(), StandardCharsets.UTF_8),
                      "contentType", doc.contentType(),
                      "size", doc.size(),
                      "createdAt", doc.createdAt().toString(),
                      "metadata", doc.metadata()))
          .switchIfEmpty(Mono.error(new RuntimeException("Document not found: " + id)));
    }

    /**
     * Delete a document.
     *
     * <p>Example: {@code curl -X DELETE "http://localhost:8080/api/storage/delete/<document-id>"}
     */
    @DeleteMapping("/api/storage/delete/{id}")
    public Mono<Map<String, Object>> delete(@PathVariable String id) {
      log.info("Deleting document: {}", id);

      return getStorageProvider()
          .flatMap(provider -> provider.delete(id))
          .map(deleted -> Map.of("deleted", deleted, "id", id));
    }

    /**
     * Query documents by metadata.
     *
     * <p>Example: {@code curl -X POST "http://localhost:8080/api/storage/query" -H "Content-Type:
     * application/json" -d '{"filters":{"category":"notes"},"limit":10,"offset":0}'}
     */
    @PostMapping("/api/storage/query")
    public Flux<Map<String, Object>> query(@RequestBody Map<String, Object> request) {
      log.info("Querying documents with filters: {}", request);

      @SuppressWarnings("unchecked")
      Map<String, Object> filters = (Map<String, Object>) request.getOrDefault("filters", Map.of());
      int limit = (int) request.getOrDefault("limit", 10);
      int offset = (int) request.getOrDefault("offset", 0);

      StorageQuery query = new StorageQuery(filters, limit, offset);

      return getStorageProvider()
          .flatMapMany(provider -> provider.query(query))
          .map(
              doc ->
                  Map.of(
                      "id", doc.id(),
                      "contentType", doc.contentType(),
                      "size", doc.size(),
                      "createdAt", doc.createdAt().toString(),
                      "metadata", doc.metadata()));
    }

    /**
     * Check if a document exists.
     *
     * <p>Example: {@code curl "http://localhost:8080/api/storage/exists/<document-id>"}
     */
    @GetMapping("/api/storage/exists/{id}")
    public Mono<Map<String, Object>> exists(@PathVariable String id) {
      log.info("Checking if document exists: {}", id);

      return getStorageProvider()
          .flatMap(provider -> provider.exists(id))
          .map(exists -> Map.of("exists", exists, "id", id));
    }

    /**
     * Get storage statistics.
     *
     * <p>Example: {@code curl "http://localhost:8080/api/storage/stats"}
     */
    @GetMapping("/api/storage/stats")
    public Mono<Map<String, Object>> stats() {
      log.info("Getting storage statistics");

      return getStorageProvider()
          .flatMap(
              provider ->
                  provider
                      .getTotalSize()
                      .map(
                          totalSize ->
                              Map.of(
                                  "totalSizeBytes", totalSize,
                                  "healthy", provider.isHealthy(),
                                  "providerName", provider.getProviderName())));
    }

    /**
     * Store multiple documents at once.
     *
     * <p>Example: {@code curl -X POST "http://localhost:8080/api/storage/store-batch" -H
     * "Content-Type: application/json" -d
     * '[{"content":"Doc1","metadata":{"tag":"a"}},{"content":"Doc2","metadata":{"tag":"b"}}]'}
     */
    @PostMapping("/api/storage/store-batch")
    public Flux<Map<String, Object>> storeBatch(@RequestBody java.util.List<Map<String, Object>> documents) {
      log.info("Storing {} documents in batch", documents.size());

      return getStorageProvider()
          .flatMapMany(
              provider ->
                  Flux.fromIterable(documents)
                      .flatMap(
                          doc -> {
                            String content = (String) doc.get("content");
                            String contentType =
                                (String) doc.getOrDefault("contentType", "text/plain");
                            @SuppressWarnings("unchecked")
                            Map<String, Object> metadata =
                                (Map<String, Object>) doc.getOrDefault("metadata", Map.of());

                            Document document =
                                new Document(
                                    null,
                                    content.getBytes(StandardCharsets.UTF_8),
                                    contentType,
                                    metadata,
                                    null,
                                    0);

                            return provider.store(document);
                          })
                      .map(
                          stored ->
                              Map.of(
                                  "id", stored.id(),
                                  "size", stored.size(),
                                  "createdAt", stored.createdAt().toString())));
    }

    private Mono<LocalStorageProvider> getStorageProvider() {
      return Mono.justOrEmpty(registry.getProvider("storage", "local"))
          .switchIfEmpty(Mono.error(new RuntimeException("LocalStorageProvider not found")));
    }
  }
}
