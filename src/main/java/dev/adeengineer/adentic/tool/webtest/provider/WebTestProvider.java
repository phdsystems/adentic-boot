package dev.adeengineer.adentic.tool.webtest.provider;

import dev.adeengineer.adentic.tool.webtest.config.WebTestConfig;
import dev.adeengineer.adentic.tool.webtest.model.ElementInfo;
import dev.adeengineer.adentic.tool.webtest.model.PageState;
import dev.adeengineer.adentic.tool.webtest.model.Screenshot;
import dev.adeengineer.adentic.tool.webtest.model.TestResult;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * Interface for web testing providers. Implementations include Playwright, Selenium, and HtmlUnit.
 */
public interface WebTestProvider {

  // ========== LIFECYCLE ==========

  /** Start the browser/provider. */
  Mono<Void> start();

  /** Close the browser/provider. */
  Mono<Void> close();

  /** Check if provider is running. */
  boolean isRunning();

  /** Get provider configuration. */
  WebTestConfig getConfig();

  // ========== NAVIGATION ==========

  /** Navigate to a URL. */
  Mono<TestResult> navigateTo(String url);

  /** Go back in browser history. */
  Mono<TestResult> goBack();

  /** Go forward in browser history. */
  Mono<TestResult> goForward();

  /** Refresh the current page. */
  Mono<TestResult> refresh();

  /** Get current URL. */
  Mono<String> getCurrentUrl();

  /** Get page title. */
  Mono<String> getTitle();

  // ========== ELEMENT INTERACTION ==========

  /** Click an element. */
  Mono<TestResult> click(String selector);

  /** Double-click an element. */
  Mono<TestResult> doubleClick(String selector);

  /** Right-click an element. */
  Mono<TestResult> rightClick(String selector);

  /** Fill an input field. */
  Mono<TestResult> fillInput(String selector, String text);

  /** Clear an input field. */
  Mono<TestResult> clearInput(String selector);

  /** Select an option from dropdown. */
  Mono<TestResult> selectOption(String selector, String value);

  /** Check a checkbox. */
  Mono<TestResult> check(String selector);

  /** Uncheck a checkbox. */
  Mono<TestResult> uncheck(String selector);

  /** Hover over an element. */
  Mono<TestResult> hover(String selector);

  /** Submit a form. */
  Mono<TestResult> submit(String selector);

  // ========== ELEMENT QUERIES ==========

  /** Get element text content. */
  Mono<String> getText(String selector);

  /** Get element attribute value. */
  Mono<String> getAttribute(String selector, String attribute);

  /** Get element information. */
  Mono<ElementInfo> getElementInfo(String selector);

  /** Find all elements matching selector. */
  Mono<List<ElementInfo>> findElements(String selector);

  // ========== ASSERTIONS ==========

  /** Assert element exists. */
  Mono<TestResult> assertElementExists(String selector);

  /** Assert element is visible. */
  Mono<TestResult> assertElementVisible(String selector);

  /** Assert element is enabled. */
  Mono<TestResult> assertElementEnabled(String selector);

  /** Assert element text contains value. */
  Mono<TestResult> assertTextContains(String selector, String expected);

  /** Assert element text equals value. */
  Mono<TestResult> assertTextEquals(String selector, String expected);

  /** Assert URL matches pattern. */
  Mono<TestResult> assertUrlMatches(String pattern);

  /** Assert page title contains value. */
  Mono<TestResult> assertTitleContains(String expected);

  // ========== WAITING ==========

  /** Wait for element to exist. */
  Mono<TestResult> waitForElement(String selector);

  /** Wait for element to be visible. */
  Mono<TestResult> waitForVisible(String selector);

  /** Wait for element to be enabled. */
  Mono<TestResult> waitForEnabled(String selector);

  /** Wait for specific duration. */
  Mono<TestResult> waitFor(long milliseconds);

  // ========== SCREENSHOTS ==========

  /** Take full page screenshot. */
  Mono<Screenshot> takeScreenshot();

  /** Take screenshot of specific element. */
  Mono<Screenshot> takeScreenshot(String selector);

  /** Save screenshot to file. */
  Mono<Screenshot> saveScreenshot(String filePath);

  // ========== JAVASCRIPT EXECUTION ==========

  /** Execute JavaScript code. */
  Mono<Object> executeScript(String script);

  /** Execute JavaScript code with arguments. */
  Mono<Object> executeScript(String script, Object... args);

  // ========== PAGE STATE ==========

  /** Get current page state. */
  Mono<PageState> getPageState();

  /** Get page HTML source. */
  Mono<String> getPageSource();

  /** Get cookies. */
  Mono<List<String>> getCookies();

  /** Set cookie. */
  Mono<Void> setCookie(String name, String value);

  /** Clear cookies. */
  Mono<Void> clearCookies();

  // ========== ALERTS/DIALOGS ==========

  /** Accept alert/confirm dialog. */
  Mono<TestResult> acceptAlert();

  /** Dismiss alert/confirm dialog. */
  Mono<TestResult> dismissAlert();

  /** Get alert text. */
  Mono<String> getAlertText();

  // ========== VIEWPORT/WINDOW ==========

  /** Set viewport size. */
  Mono<Void> setViewportSize(int width, int height);

  /** Maximize window. */
  Mono<Void> maximizeWindow();

  /** Get window size. */
  Mono<int[]> getWindowSize();
}
