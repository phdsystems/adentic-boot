package dev.engineeringlab.rag.embedding.valueobject;

/** Stub class for EmbeddingRequest - to be replaced with real implementation when available. */
public class EmbeddingRequest {
  private final String text;
  private final String model;

  public EmbeddingRequest(String text, String model) {
    this.text = text;
    this.model = model;
  }

  public static EmbeddingRequestBuilder builder() {
    return new EmbeddingRequestBuilder();
  }

  public String getText() {
    return text;
  }

  public String getModel() {
    return model;
  }

  public static class EmbeddingRequestBuilder {
    private String text;
    private String model;

    public EmbeddingRequestBuilder text(String text) {
      this.text = text;
      return this;
    }

    public EmbeddingRequestBuilder model(String model) {
      this.model = model;
      return this;
    }

    public EmbeddingRequest build() {
      return new EmbeddingRequest(text, model);
    }
  }
}
