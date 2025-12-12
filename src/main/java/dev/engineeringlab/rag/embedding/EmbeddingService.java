package dev.engineeringlab.rag.embedding;

import dev.engineeringlab.rag.embedding.valueobject.Embedding;
import dev.engineeringlab.rag.embedding.valueobject.EmbeddingRequest;
import java.util.List;
import reactor.core.publisher.Mono;

/** Stub interface for EmbeddingService - to be replaced with real implementation when available. */
public interface EmbeddingService {
  Mono<Embedding> embed(EmbeddingRequest request);

  Mono<List<Embedding>> embedBatch(List<EmbeddingRequest> requests);

  int getDimensions();

  String getModelName();
}
