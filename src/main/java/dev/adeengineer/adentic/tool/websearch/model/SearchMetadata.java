package dev.adeengineer.adentic.tool.websearch.model;

import java.time.Duration;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

/** Metadata about a search query and its results. */
@Data
@Builder(toBuilder = true)
public class SearchMetadata {
  /** The original search query. */
  private String query;

  /** The search provider used. */
  private SearchProvider provider;

  /** Total number of results found (if available). */
  @Builder.Default private Long totalResults = null;

  /** Number of results returned in this response. */
  private int resultCount;

  /** Timestamp when the search was executed. */
  @Builder.Default private Instant timestamp = Instant.now();

  /** Time taken to execute the search. */
  private Duration queryTime;

  /** Whether the search was served from cache. */
  @Builder.Default private boolean cached = false;

  /** Error message if the search failed. */
  private String error;

  /** Whether the search was successful. */
  @Builder.Default private boolean success = true;
}
