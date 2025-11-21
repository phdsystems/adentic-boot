package dev.engineeringlab.adentic.tool.webtest.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** Represents the current state of a web page. */
@Data
@Builder
public class PageState {
  /** Current URL. */
  private String url;

  /** Page title. */
  private String title;

  /** Page HTML source. */
  private String html;

  /** Cookies. */
  private Map<String, String> cookies;

  /** Local storage items. */
  private Map<String, String> localStorage;

  /** Session storage items. */
  private Map<String, String> sessionStorage;

  /** Console logs. */
  private List<ConsoleMessage> consoleLogs;

  /** Network requests. */
  private List<NetworkRequest> networkRequests;

  /** Viewport width. */
  private int viewportWidth;

  /** Viewport height. */
  private int viewportHeight;

  /** Page load time in milliseconds. */
  private Long loadTime;

  /** Timestamp when state was captured. */
  @Builder.Default private Instant timestamp = Instant.now();

  /** Console message. */
  @Data
  @Builder
  public static class ConsoleMessage {
    private String type; // log, warn, error, info
    private String text;
    private Instant timestamp;
  }

  /** Network request. */
  @Data
  @Builder
  public static class NetworkRequest {
    private String method;
    private String url;
    private int statusCode;
    private Long duration;
    private Instant timestamp;
  }

  @Override
  public String toString() {
    return String.format(
        "📄 Page: %s%n  Title: %s%n  Viewport: %dx%d", url, title, viewportWidth, viewportHeight);
  }
}
