package dev.adeengineer.adentic.tool.websearch.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** Represents a single search result item. */
@Data
@Builder
public class SearchItem {
  /** The title of the search result. */
  private String title;

  /** The URL of the search result. */
  private String url;

  /** A snippet/description of the content. */
  private String snippet;

  /** The display URL (may be formatted differently from actual URL). */
  private String displayUrl;

  /** Position in search results (1-indexed). */
  @Builder.Default private int position = 0;

  /** Additional metadata specific to the search provider. */
  @Builder.Default private Map<String, Object> metadata = new HashMap<>();

  @Override
  public String toString() {
    return String.format(
        "[%d] %s%n    URL: %s%n    %s",
        position, title, url, snippet != null ? snippet : "No description");
  }
}
