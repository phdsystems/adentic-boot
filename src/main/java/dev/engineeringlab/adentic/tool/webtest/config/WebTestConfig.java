package dev.engineeringlab.adentic.tool.webtest.config;

import dev.engineeringlab.adentic.tool.webtest.model.BrowserType;
import dev.engineeringlab.adentic.tool.webtest.model.TestProvider;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

/** Configuration for WebTestTool behavior and provider selection. */
@Data
@Builder
public class WebTestConfig {
  /** Test provider to use. */
  @Builder.Default private TestProvider provider = TestProvider.PLAYWRIGHT;

  /** Browser type. */
  @Builder.Default private BrowserType browser = BrowserType.CHROMIUM;

  /** Run in headless mode (no visible browser). */
  @Builder.Default private boolean headless = true;

  /** Default timeout for operations in milliseconds. */
  @Builder.Default private long timeout = 30000;

  /** Page load timeout in milliseconds. */
  @Builder.Default private long pageLoadTimeout = 30000;

  /** Script execution timeout in milliseconds. */
  @Builder.Default private long scriptTimeout = 30000;

  /** Viewport width. */
  @Builder.Default private int viewportWidth = 1280;

  /** Viewport height. */
  @Builder.Default private int viewportHeight = 720;

  /** Allowed domains (whitelist). Empty = all allowed. */
  @Builder.Default private Set<String> allowedDomains = new HashSet<>();

  /** Blocked domains (blacklist). */
  @Builder.Default private Set<String> blockedDomains = new HashSet<>();

  /** User agent string. */
  private String userAgent;

  /** Whether to accept insecure SSL certificates. */
  @Builder.Default private boolean acceptInsecureCerts = false;

  /** Whether to enable JavaScript. */
  @Builder.Default private boolean javascriptEnabled = true;

  /** Whether to load images. */
  @Builder.Default private boolean imagesEnabled = true;

  /** Slow down operations (for debugging, in milliseconds). */
  @Builder.Default private long slowMo = 0;

  /** Screenshot directory. */
  private String screenshotDir;

  /** Whether to capture console logs. */
  @Builder.Default private boolean captureConsoleLogs = false;

  /** Whether to capture network requests. */
  @Builder.Default private boolean captureNetwork = false;

  /** Maximum number of screenshots to keep. */
  @Builder.Default private int maxScreenshots = 100;

  /**
   * Playwright configuration preset. - Modern, fast, auto-wait - Headless Chromium - 30s timeout
   */
  public static WebTestConfig playwright() {
    return WebTestConfig.builder()
        .provider(TestProvider.PLAYWRIGHT)
        .browser(BrowserType.CHROMIUM)
        .headless(true)
        .build();
  }

  /** Selenium configuration preset. - Compatible, multi-browser - Headless Chrome - 30s timeout */
  public static WebTestConfig selenium() {
    return WebTestConfig.builder()
        .provider(TestProvider.SELENIUM)
        .browser(BrowserType.CHROME)
        .headless(true)
        .build();
  }

  /** HtmlUnit configuration preset. - Lightweight, fast - No browser - 10s timeout */
  public static WebTestConfig htmlunit() {
    return WebTestConfig.builder()
        .provider(TestProvider.HTMLUNIT)
        .browser(BrowserType.NONE)
        .timeout(10000)
        .pageLoadTimeout(10000)
        .build();
  }

  /** Lightweight configuration preset. - HtmlUnit provider - Fast, headless - For simple tests */
  public static WebTestConfig lightweight() {
    return htmlunit();
  }

  /**
   * Visible mode configuration preset. - Playwright with visible browser - For debugging - Slower
   * operations (500ms)
   */
  public static WebTestConfig visible() {
    return WebTestConfig.builder()
        .provider(TestProvider.PLAYWRIGHT)
        .browser(BrowserType.CHROMIUM)
        .headless(false)
        .slowMo(500)
        .build();
  }

  /**
   * Debug mode configuration preset. - Visible browser - Slow operations (1000ms) - Console log
   * capture - Network capture
   */
  public static WebTestConfig debug() {
    return WebTestConfig.builder()
        .provider(TestProvider.PLAYWRIGHT)
        .browser(BrowserType.CHROMIUM)
        .headless(false)
        .slowMo(1000)
        .captureConsoleLogs(true)
        .captureNetwork(true)
        .build();
  }

  /** Mobile viewport configuration preset. - iPhone 12 dimensions - Portrait orientation */
  public static WebTestConfig mobile() {
    return WebTestConfig.builder()
        .provider(TestProvider.PLAYWRIGHT)
        .browser(BrowserType.CHROMIUM)
        .headless(true)
        .viewportWidth(390)
        .viewportHeight(844)
        .build();
  }

  /** Desktop viewport configuration preset. - 1920x1080 resolution */
  public static WebTestConfig desktop() {
    return WebTestConfig.builder()
        .provider(TestProvider.PLAYWRIGHT)
        .browser(BrowserType.CHROMIUM)
        .headless(true)
        .viewportWidth(1920)
        .viewportHeight(1080)
        .build();
  }

  /** Default configuration preset. - Playwright provider - Headless Chromium - 1280x720 viewport */
  public static WebTestConfig defaults() {
    return playwright();
  }

  /**
   * Fast configuration preset. - HtmlUnit for speed - Shorter timeouts (10s) - No JavaScript
   * rendering
   */
  public static WebTestConfig fast() {
    return WebTestConfig.builder()
        .provider(TestProvider.HTMLUNIT)
        .browser(BrowserType.NONE)
        .timeout(10000)
        .pageLoadTimeout(10000)
        .scriptTimeout(5000)
        .imagesEnabled(false)
        .build();
  }

  /**
   * Cross-browser testing preset. - Selenium provider - Firefox browser - For compatibility testing
   */
  public static WebTestConfig crossBrowser() {
    return WebTestConfig.builder()
        .provider(TestProvider.SELENIUM)
        .browser(BrowserType.FIREFOX)
        .headless(true)
        .build();
  }

  /**
   * Puppeteer configuration preset. - Google's Chrome DevTools Protocol tool - Headless Chromium -
   * For CDP features, PDF generation, performance analysis - Requires Java-Node.js bridge or CDP
   * client
   */
  public static WebTestConfig puppeteer() {
    return WebTestConfig.builder()
        .provider(TestProvider.PUPPETEER)
        .browser(BrowserType.CHROMIUM)
        .headless(true)
        .build();
  }

  /**
   * Cypress configuration preset. - Modern JavaScript E2E testing framework - Headless Chromium -
   * For developer-friendly testing, time-travel debugging - Requires REST wrapper service or
   * GraalVM polyglot
   */
  public static WebTestConfig cypress() {
    return WebTestConfig.builder()
        .provider(TestProvider.CYPRESS)
        .browser(BrowserType.CHROMIUM)
        .headless(true)
        .build();
  }

  /** Check if a domain is allowed. */
  public boolean isDomainAllowed(String domain) {
    // Check blocked first
    if (blockedDomains.contains(domain)) {
      return false;
    }

    // If whitelist is empty, all domains allowed
    if (allowedDomains.isEmpty()) {
      return true;
    }

    // Check whitelist
    return allowedDomains.contains(domain);
  }
}
