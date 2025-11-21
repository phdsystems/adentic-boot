package dev.engineeringlab.adentic.tool.webtest;

import dev.engineeringlab.adentic.tool.webtest.config.WebTestConfig;
import dev.engineeringlab.adentic.tool.webtest.model.*;
import dev.engineeringlab.adentic.tool.webtest.provider.*;
import dev.engineeringlab.annotation.provider.ToolProvider;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Tool for web application testing using multiple provider backends.
 *
 * <p>Supports six testing providers:
 *
 * <ul>
 *   <li>**Playwright** - Modern, fast, auto-wait capabilities (recommended)
 *   <li>**Selenium** - Mature, multi-browser support, wide compatibility
 *   <li>**HtmlUnit** - Lightweight, headless, no browser needed (fast)
 *   <li>**Puppeteer** - Google's Chrome DevTools Protocol tool (CDP features, PDF generation)
 *   <li>**Cypress** - JavaScript E2E testing framework (time-travel debugging)
 *   <li>**Custom** - User-provided custom implementations
 * </ul>
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Provider/service pattern for flexibility
 *   <li>Runtime provider switching
 *   <li>Comprehensive browser automation
 *   <li>Element interaction and assertions
 *   <li>Screenshot capture
 *   <li>JavaScript execution
 *   <li>Security controls (domain whitelisting)
 *   <li>Extensible with custom providers
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Inject
 * private WebTestTool webTest;
 *
 * // Use Playwright (default)
 * webTest.navigateTo("https://example.com").block();
 * webTest.click("#login-button").block();
 * webTest.fillInput("#username", "test@example.com").block();
 *
 * // Switch to Selenium for cross-browser testing
 * webTest.setConfig(WebTestConfig.selenium());
 * webTest.navigateTo("https://example.com").block();
 *
 * // Use HtmlUnit for fast, lightweight tests
 * webTest.setConfig(WebTestConfig.lightweight());
 *
 * // Use Puppeteer for CDP features
 * webTest.setConfig(WebTestConfig.puppeteer());
 *
 * // Use Cypress for E2E testing
 * webTest.setConfig(WebTestConfig.cypress());
 * }</pre>
 *
 * @see WebTestConfig
 * @see WebTestProvider
 * @see TestProvider
 */
@ToolProvider(name = "web-test")
@Slf4j
public class WebTestTool {

  private WebTestConfig config;

  private WebTestProvider provider;

  /** Default constructor with default configuration (Playwright). */
  public WebTestTool() {
    this(WebTestConfig.defaults());
  }

  /** Constructor with custom configuration. */
  public WebTestTool(WebTestConfig config) {
    this.config = config;
    this.provider = createProvider(config);
  }

  /** Set configuration and recreate provider. */
  public void setConfig(WebTestConfig config) {
    // Close existing provider if running
    if (provider != null && provider.isRunning()) {
      provider.close().block();
    }

    this.config = config;
    this.provider = createProvider(config);

    log.info("Switched to {} provider", config.getProvider());
  }

  /**
   * Create provider based on configuration.
   *
   * <p>For CUSTOM provider, you must provide a custom implementation by:
   *
   * <ol>
   *   <li>Extending CustomWebTestProvider
   *   <li>Implementing all WebTestProvider methods
   *   <li>Using WebTestTool constructor with custom provider instance
   * </ol>
   */
  private WebTestProvider createProvider(WebTestConfig config) {
    return switch (config.getProvider()) {
      case PLAYWRIGHT -> new PlaywrightWebTestProvider(config);
      case SELENIUM -> new SeleniumWebTestProvider(config);
      case HTMLUNIT -> new HtmlUnitWebTestProvider(config);
      case PUPPETEER -> new PuppeteerWebTestProvider(config);
      case CYPRESS -> new CypressWebTestProvider(config);
      case CUSTOM ->
          throw new IllegalStateException(
              "CUSTOM provider requires manual instantiation. "
                  + "Create a class extending CustomWebTestProvider and "
                  + "use WebTestTool constructor with your provider instance.");
    };
  }

  /** Ensure provider is started. */
  private Mono<Void> ensureStarted() {
    if (!provider.isRunning()) {
      return provider.start();
    }
    return Mono.empty();
  }

  // ========== LIFECYCLE ==========

  /** Start the test provider. */
  public Mono<Void> start() {
    return provider.start();
  }

  /** Close the test provider. */
  public Mono<Void> close() {
    return provider.close();
  }

  // ========== NAVIGATION ==========

  /** Navigate to a URL. */
  public Mono<TestResult> navigateTo(String url) {
    return ensureStarted().then(provider.navigateTo(url));
  }

  /** Go back in browser history. */
  public Mono<TestResult> goBack() {
    return provider.goBack();
  }

  /** Go forward in browser history. */
  public Mono<TestResult> goForward() {
    return provider.goForward();
  }

  /** Refresh the current page. */
  public Mono<TestResult> refresh() {
    return provider.refresh();
  }

  /** Get current URL. */
  public Mono<String> getCurrentUrl() {
    return provider.getCurrentUrl();
  }

  /** Get page title. */
  public Mono<String> getTitle() {
    return provider.getTitle();
  }

  // ========== ELEMENT INTERACTION ==========

  /** Click an element. */
  public Mono<TestResult> click(String selector) {
    return provider.click(selector);
  }

  /** Double-click an element. */
  public Mono<TestResult> doubleClick(String selector) {
    return provider.doubleClick(selector);
  }

  /** Right-click an element. */
  public Mono<TestResult> rightClick(String selector) {
    return provider.rightClick(selector);
  }

  /** Fill an input field. */
  public Mono<TestResult> fillInput(String selector, String text) {
    return provider.fillInput(selector, text);
  }

  /** Clear an input field. */
  public Mono<TestResult> clearInput(String selector) {
    return provider.clearInput(selector);
  }

  /** Select an option from dropdown. */
  public Mono<TestResult> selectOption(String selector, String value) {
    return provider.selectOption(selector, value);
  }

  /** Check a checkbox. */
  public Mono<TestResult> check(String selector) {
    return provider.check(selector);
  }

  /** Uncheck a checkbox. */
  public Mono<TestResult> uncheck(String selector) {
    return provider.uncheck(selector);
  }

  /** Hover over an element. */
  public Mono<TestResult> hover(String selector) {
    return provider.hover(selector);
  }

  /** Submit a form. */
  public Mono<TestResult> submit(String selector) {
    return provider.submit(selector);
  }

  // ========== ELEMENT QUERIES ==========

  /** Get element text content. */
  public Mono<String> getText(String selector) {
    return provider.getText(selector);
  }

  /** Get element attribute value. */
  public Mono<String> getAttribute(String selector, String attribute) {
    return provider.getAttribute(selector, attribute);
  }

  /** Get element information. */
  public Mono<ElementInfo> getElementInfo(String selector) {
    return provider.getElementInfo(selector);
  }

  /** Find all elements matching selector. */
  public Mono<List<ElementInfo>> findElements(String selector) {
    return provider.findElements(selector);
  }

  // ========== ASSERTIONS ==========

  /** Assert element exists. */
  public Mono<TestResult> assertElementExists(String selector) {
    return provider.assertElementExists(selector);
  }

  /** Assert element is visible. */
  public Mono<TestResult> assertElementVisible(String selector) {
    return provider.assertElementVisible(selector);
  }

  /** Assert element is enabled. */
  public Mono<TestResult> assertElementEnabled(String selector) {
    return provider.assertElementEnabled(selector);
  }

  /** Assert element text contains value. */
  public Mono<TestResult> assertTextContains(String selector, String expected) {
    return provider.assertTextContains(selector, expected);
  }

  /** Assert element text equals value. */
  public Mono<TestResult> assertTextEquals(String selector, String expected) {
    return provider.assertTextEquals(selector, expected);
  }

  /** Assert URL matches pattern. */
  public Mono<TestResult> assertUrlMatches(String pattern) {
    return provider.assertUrlMatches(pattern);
  }

  /** Assert page title contains value. */
  public Mono<TestResult> assertTitleContains(String expected) {
    return provider.assertTitleContains(expected);
  }

  // ========== WAITING ==========

  /** Wait for element to exist. */
  public Mono<TestResult> waitForElement(String selector) {
    return provider.waitForElement(selector);
  }

  /** Wait for element to be visible. */
  public Mono<TestResult> waitForVisible(String selector) {
    return provider.waitForVisible(selector);
  }

  /** Wait for element to be enabled. */
  public Mono<TestResult> waitForEnabled(String selector) {
    return provider.waitForEnabled(selector);
  }

  /** Wait for specific duration. */
  public Mono<TestResult> waitFor(long milliseconds) {
    return provider.waitFor(milliseconds);
  }

  // ========== SCREENSHOTS ==========

  /** Take full page screenshot. */
  public Mono<Screenshot> takeScreenshot() {
    return provider.takeScreenshot();
  }

  /** Take screenshot of specific element. */
  public Mono<Screenshot> takeScreenshot(String selector) {
    return provider.takeScreenshot(selector);
  }

  /** Save screenshot to file. */
  public Mono<Screenshot> saveScreenshot(String filePath) {
    return provider.saveScreenshot(filePath);
  }

  // ========== JAVASCRIPT EXECUTION ==========

  /** Execute JavaScript code. */
  public Mono<Object> executeScript(String script) {
    return provider.executeScript(script);
  }

  /** Execute JavaScript code with arguments. */
  public Mono<Object> executeScript(String script, Object... args) {
    return provider.executeScript(script, args);
  }

  // ========== PAGE STATE ==========

  /** Get current page state. */
  public Mono<PageState> getPageState() {
    return provider.getPageState();
  }

  /** Get page HTML source. */
  public Mono<String> getPageSource() {
    return provider.getPageSource();
  }

  /** Get cookies. */
  public Mono<List<String>> getCookies() {
    return provider.getCookies();
  }

  /** Set cookie. */
  public Mono<Void> setCookie(String name, String value) {
    return provider.setCookie(name, value);
  }

  /** Clear cookies. */
  public Mono<Void> clearCookies() {
    return provider.clearCookies();
  }

  // ========== ALERTS/DIALOGS ==========

  /** Accept alert/confirm dialog. */
  public Mono<TestResult> acceptAlert() {
    return provider.acceptAlert();
  }

  /** Dismiss alert/confirm dialog. */
  public Mono<TestResult> dismissAlert() {
    return provider.dismissAlert();
  }

  /** Get alert text. */
  public Mono<String> getAlertText() {
    return provider.getAlertText();
  }

  // ========== VIEWPORT/WINDOW ==========

  /** Set viewport size. */
  public Mono<Void> setViewportSize(int width, int height) {
    return provider.setViewportSize(width, height);
  }

  /** Maximize window. */
  public Mono<Void> maximizeWindow() {
    return provider.maximizeWindow();
  }

  /** Get window size. */
  public Mono<int[]> getWindowSize() {
    return provider.getWindowSize();
  }
}
