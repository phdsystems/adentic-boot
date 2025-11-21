package dev.engineeringlab.adentic.tool.websearch.model;

/** Supported web search providers. */
public enum SearchProvider {
  /** DuckDuckGo HTML search (free, no API key required). */
  DUCKDUCKGO,

  /** Google Custom Search API (requires API key). */
  GOOGLE,

  /** Bing Search API (requires API key). */
  BING
}
