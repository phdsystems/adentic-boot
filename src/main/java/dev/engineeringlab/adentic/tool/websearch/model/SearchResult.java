package dev.engineeringlab.adentic.tool.websearch.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** Container for web search results. */
@Data
@Builder(toBuilder = true)
public class SearchResult {
  /** Metadata about the search query. */
  private SearchMetadata metadata;

  /** List of search result items. */
  @Builder.Default private List<SearchItem> items = new ArrayList<>();

  /** Whether the search was successful. */
  public boolean isSuccess() {
    return metadata != null && metadata.isSuccess();
  }

  /** Get the total number of results returned. */
  public int getResultCount() {
    return items.size();
  }

  /** Check if there are any results. */
  public boolean hasResults() {
    return items != null && !items.isEmpty();
  }

  /** Get a formatted summary of the search results. */
  public String getSummary() {
    if (!isSuccess()) {
      return String.format(
          "❌ Search failed: %s", metadata != null ? metadata.getError() : "Unknown error");
    }

    if (!hasResults()) {
      return String.format(
          "No results found for query: %s", metadata != null ? metadata.getQuery() : "unknown");
    }

    return String.format(
        "✅ Found %d results for '%s' using %s [%dms]%s",
        items.size(),
        metadata.getQuery(),
        metadata.getProvider(),
        metadata.getQueryTime() != null ? metadata.getQueryTime().toMillis() : 0,
        metadata.isCached() ? " (cached)" : "");
  }

  /** Get a detailed report of all search results. */
  public String getDetailedReport() {
    StringBuilder report = new StringBuilder();
    report.append(getSummary()).append("\n\n");

    if (hasResults()) {
      for (SearchItem item : items) {
        report.append(item.toString()).append("\n\n");
      }
    }

    return report.toString();
  }

  @Override
  public String toString() {
    return getSummary();
  }
}
