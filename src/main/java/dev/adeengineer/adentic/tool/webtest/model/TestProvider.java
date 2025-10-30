package dev.adeengineer.adentic.tool.webtest.model;

/** Supported web testing providers. */
public enum TestProvider {
  /**
   * Playwright - Modern, fast, auto-wait capabilities. Best for: Modern web apps, E2E testing,
   * reliable tests.
   */
  PLAYWRIGHT,

  /**
   * Selenium WebDriver - Mature, multi-browser support. Best for: Cross-browser testing, legacy
   * apps, wide compatibility.
   */
  SELENIUM,

  /**
   * HtmlUnit - Lightweight, headless, no browser needed. Best for: Fast tests, API testing, simple
   * scraping.
   */
  HTMLUNIT,

  /**
   * Puppeteer - Google's Node.js browser automation library. Best for: Chrome DevTools Protocol
   * features, PDF generation, performance analysis.
   */
  PUPPETEER,

  /**
   * Cypress - Modern E2E testing framework (JavaScript-based). Best for: Developer-friendly
   * testing, time-travel debugging, automatic waiting. Note: Requires Java-JavaScript bridge or
   * REST API wrapper.
   */
  CYPRESS,

  /**
   * Custom - User-provided custom implementation. Best for: Specialized testing needs, custom
   * integrations, proprietary tools.
   */
  CUSTOM
}
