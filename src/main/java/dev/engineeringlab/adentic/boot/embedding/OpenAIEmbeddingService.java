package dev.engineeringlab.adentic.boot.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.engineeringlab.rag.embedding.EmbeddingService;
import dev.engineeringlab.rag.embedding.valueobject.Embedding;
import dev.engineeringlab.rag.embedding.valueobject.EmbeddingRequest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Framework-agnostic OpenAI embedding service implementation.
 *
 * <p>Uses Java 21 HttpClient (no Spring dependencies) for OpenAI API communication. Supports
 * text-embedding-3-small and text-embedding-3-large models.
 *
 * <p><b>Features:</b>
 *
 * <ul>
 *   <li>Single text and batch embedding generation
 *   <li>Configurable model selection
 *   <li>API key authentication
 *   <li>Thread-safe operations
 *   <li>Reactive API (Mono/Flux)
 * </ul>
 *
 * <p><b>Framework-agnostic:</b> This is a pure POJO with no Spring dependencies. Can be used in any
 * Java application.
 *
 * @see EmbeddingService
 */
@Slf4j
public final class OpenAIEmbeddingService implements EmbeddingService {

  /** Default OpenAI embeddings model. */
  private static final String DEFAULT_MODEL = "text-embedding-3-small";

  /** OpenAI embeddings API endpoint URL. */
  private static final String API_URL = "https://api.openai.com/v1/embeddings";

  /** Timeout in seconds for API calls. */
  private static final int TIMEOUT_SECONDS = 30;

  /** Maximum text length for OpenAI embeddings (in tokens). */
  private static final int MAX_TEXT_LENGTH = 8191;

  /** Dimensions for text-embedding-3-small. */
  private static final int DIMENSIONS_SMALL = 1536;

  /** Dimensions for text-embedding-3-large. */
  private static final int DIMENSIONS_LARGE = 3072;

  /** HTTP client for API communication. */
  private final HttpClient httpClient;

  /** JSON object mapper for request/response serialization. */
  private final ObjectMapper objectMapper;

  /** OpenAI API key. */
  private final String apiKey;

  /** Model identifier to use for embeddings. */
  private final String model;

  /** Embedding dimensions for the configured model. */
  private final int dimensions;

  /**
   * Creates OpenAI embedding service with specified configuration.
   *
   * @param apiKey OpenAI API key
   * @param model Model identifier (null = text-embedding-3-small)
   * @param objectMapper JSON mapper for serialization
   */
  public OpenAIEmbeddingService(
      final String apiKey, final String model, final ObjectMapper objectMapper) {
    this.apiKey = apiKey;
    this.model = model != null ? model : DEFAULT_MODEL;
    this.objectMapper = objectMapper;
    this.dimensions = this.model.contains("large") ? DIMENSIONS_LARGE : DIMENSIONS_SMALL;

    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .version(HttpClient.Version.HTTP_2)
            .build();

    log.info(
        "Initialized OpenAIEmbeddingService with model: {}, dimensions: {}",
        this.model,
        dimensions);
  }

  /**
   * Generate embedding for a single text.
   *
   * @param text The text to embed
   * @return Mono emitting the embedding
   */
  @Override
  public Mono<Embedding> embed(final String text) {
    return embedBatch(new EmbeddingRequest(List.of(text), model, true)).next();
  }

  /**
   * Generate embeddings for multiple texts in batch.
   *
   * @param request The embedding request
   * @return Flux of embeddings in the same order as input texts
   */
  @Override
  public Flux<Embedding> embedBatch(final EmbeddingRequest request) {
    return Mono.fromCallable(() -> callOpenAIAPI(request)).flatMapMany(Flux::fromIterable);
  }

  /**
   * Call OpenAI API synchronously.
   *
   * @param request The embedding request
   * @return List of embeddings
   * @throws Exception if API call fails
   */
  private List<Embedding> callOpenAIAPI(final EmbeddingRequest request) throws Exception {
    // Build request body
    final ObjectNode requestBody = objectMapper.createObjectNode();
    requestBody.put("model", request.model() != null ? request.model() : model);

    final ArrayNode inputArray = objectMapper.createArrayNode();
    for (String text : request.texts()) {
      inputArray.add(text);
    }
    requestBody.set("input", inputArray);

    if (request.normalized()) {
      requestBody.put("encoding_format", "float");
    }

    // Build HTTP request
    final HttpRequest httpRequest =
        HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build();

    // Execute request
    final HttpResponse<String> response =
        httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new RuntimeException(
          "OpenAI API error: " + response.statusCode() + " - " + response.body());
    }

    // Parse response
    return parseEmbeddingsResponse(response.body());
  }

  /**
   * Parses the OpenAI API response and extracts embeddings.
   *
   * @param responseBody JSON response from OpenAI
   * @return List of embeddings
   * @throws Exception if parsing fails
   */
  private List<Embedding> parseEmbeddingsResponse(final String responseBody) throws Exception {
    final JsonNode root = objectMapper.readTree(responseBody);
    final JsonNode dataArray = root.get("data");

    if (dataArray == null || !dataArray.isArray()) {
      throw new IllegalStateException("Invalid OpenAI embeddings response: missing data array");
    }

    final List<Embedding> embeddings = new ArrayList<>();

    for (JsonNode item : dataArray) {
      final JsonNode embeddingNode = item.get("embedding");
      if (embeddingNode == null || !embeddingNode.isArray()) {
        continue;
      }

      final List<Float> vector = new ArrayList<>();
      for (JsonNode value : embeddingNode) {
        vector.add((float) value.asDouble());
      }

      embeddings.add(new Embedding(vector, model, dimensions));
    }

    return embeddings;
  }

  /**
   * Get the embedding dimensions for this service.
   *
   * @return Number of dimensions in the embedding vectors
   */
  @Override
  public int getDimensions() {
    return dimensions;
  }

  /**
   * Get the model name being used.
   *
   * @return Model identifier
   */
  @Override
  public String getModel() {
    return model;
  }

  /**
   * Get the maximum text length supported.
   *
   * @return Maximum number of tokens
   */
  @Override
  public int getMaxTextLength() {
    return MAX_TEXT_LENGTH;
  }

  /**
   * Get the provider name.
   *
   * @return Provider name
   */
  @Override
  public String getProviderName() {
    return "openai";
  }

  /**
   * Check if the service is healthy and accessible.
   *
   * @return true if the service is ready to use
   */
  @Override
  public boolean isHealthy() {
    return apiKey != null && !apiKey.isBlank();
  }
}
