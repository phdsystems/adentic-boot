package dev.engineeringlab.adentic.tool.websearch.model;

import lombok.Builder;
import lombok.Data;

/** Request parameters for web search. */
@Data
@Builder
public class SearchRequest {
  /** The search query. */
  private String query;

  /** Maximum number of results to return. */
  @Builder.Default private int maxResults = 10;

  /** Search provider to use (if not specified, uses configured default). */
  private SearchProvider provider;

  /** Language/region preference (e.g., "en-US"). */
  @Builder.Default private String region = "en-US";

  /** Safe search level. */
  @Builder.Default private SafeSearch safeSearch = SafeSearch.MODERATE;

  /** Time range filter. */
  private TimeRange timeRange;

  /** Safe search levels. */
  public enum SafeSearch {
    STRICT,
    MODERATE,
    OFF
  }

  /** Time range filters. */
  public enum TimeRange {
    DAY,
    WEEK,
    MONTH,
    YEAR,
    ALL
  }
}
