package dev.engineeringlab.rag.embedding.valueobject;

import java.util.List;

/** Stub class for Embedding - to be replaced with real implementation when available. */
public class Embedding {
  private final List<Float> vector;
  private final String model;
  private final int dimensions;

  public Embedding(List<Float> vector, String model, int dimensions) {
    this.vector = vector;
    this.model = model;
    this.dimensions = dimensions;
  }

  public static EmbeddingBuilder builder() {
    return new EmbeddingBuilder();
  }

  public List<Float> getVector() {
    return vector;
  }

  public String getModel() {
    return model;
  }

  public int getDimensions() {
    return dimensions;
  }

  public static class EmbeddingBuilder {
    private List<Float> vector;
    private String model;
    private int dimensions;

    public EmbeddingBuilder vector(List<Float> vector) {
      this.vector = vector;
      return this;
    }

    public EmbeddingBuilder model(String model) {
      this.model = model;
      return this;
    }

    public EmbeddingBuilder dimensions(int dimensions) {
      this.dimensions = dimensions;
      return this;
    }

    public Embedding build() {
      return new Embedding(vector, model, dimensions);
    }
  }
}
