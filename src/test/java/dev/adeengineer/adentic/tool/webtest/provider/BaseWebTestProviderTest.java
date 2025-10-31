package dev.adeengineer.adentic.tool.webtest.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import dev.adeengineer.adentic.tool.webtest.config.WebTestConfig;
import dev.adeengineer.adentic.tool.webtest.model.TestResult;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

/**
 * Tests for BaseWebTestProvider base functionality.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>Constructor and initialization
 *   <li>Configuration access
 *   <li>Result creation helpers
 *   <li>URL validation with domain filtering
 *   <li>Slow-mo delay functionality
 *   <li>Operation logging
 * </ul>
 */
@DisplayName("BaseWebTestProvider Tests")
class BaseWebTestProviderTest {

  private TestWebTestProvider provider;
  private WebTestConfig config;

  @BeforeEach
  void setUp() {
    config = WebTestConfig.playwright();
    provider = new TestWebTestProvider(config);
  }

  @Nested
  @DisplayName("Constructor and Initialization")
  class ConstructorTests {

    @Test
    @DisplayName("Should initialize with valid config")
    void shouldInitializeWithValidConfig() {
      assertNotNull(provider);
      assertNotNull(provider.getConfig());
      assertSame(config, provider.getConfig());
    }

    @Test
    @DisplayName("Should not be running initially")
    void shouldNotBeRunningInitially() {
      assertFalse(provider.isRunning());
    }

    @Test
    @DisplayName("Should accept null config gracefully")
    void shouldAcceptNullConfig() {
      assertDoesNotThrow(() -> new TestWebTestProvider(null));
    }
  }

  @Nested
  @DisplayName("Configuration Access")
  class ConfigurationTests {

    @Test
    @DisplayName("Should return correct config")
    void shouldReturnCorrectConfig() {
      assertEquals(config, provider.getConfig());
    }

    @Test
    @DisplayName("Should track running state")
    void shouldTrackRunningState() {
      assertFalse(provider.isRunning());

      provider.running = true;
      assertTrue(provider.isRunning());

      provider.running = false;
      assertFalse(provider.isRunning());
    }
  }

  @Nested
  @DisplayName("Result Creation Helpers")
  class ResultCreationTests {

    @Test
    @DisplayName("Should create success result with details")
    void shouldCreateSuccessResultWithDetails() {
      TestResult result =
          provider.createSuccessResult(TestResult.OperationType.NAVIGATE, "Test details");

      assertNotNull(result);
      assertTrue(result.isPassed());
      assertEquals(TestResult.OperationType.NAVIGATE, result.getOperation());
      assertEquals("Test details", result.getDetails());
      assertNotNull(result.getTimestamp());
      assertNull(result.getError());
    }

    @Test
    @DisplayName("Should create failure result with error")
    void shouldCreateFailureResultWithError() {
      TestResult result =
          provider.createFailureResult(TestResult.OperationType.CLICK, "Test error message");

      assertNotNull(result);
      assertFalse(result.isPassed());
      assertEquals(TestResult.OperationType.CLICK, result.getOperation());
      assertEquals("Test error message", result.getError());
      assertNotNull(result.getTimestamp());
      assertNull(result.getDetails());
    }

    @Test
    @DisplayName("Should create timed result with execution time")
    void shouldCreateTimedResultWithExecutionTime() throws InterruptedException {
      Instant start = Instant.now();
      Thread.sleep(50); // Small delay to measure

      TestResult result =
          provider.createTimedResult(TestResult.OperationType.WAIT, start, "Timed operation");

      assertNotNull(result);
      assertTrue(result.isPassed());
      assertEquals(TestResult.OperationType.WAIT, result.getOperation());
      assertEquals("Timed operation", result.getDetails());
      assertEquals(start, result.getTimestamp());
      assertNotNull(result.getExecutionTime());
      assertTrue(
          result.getExecutionTime().toMillis() >= 50, "Execution time should be at least 50ms");
    }

    @Test
    @DisplayName("Should create result with immediate timestamp")
    void shouldCreateResultWithImmediateTimestamp() {
      Instant before = Instant.now();
      TestResult result =
          provider.createSuccessResult(TestResult.OperationType.SCREENSHOT, "Screenshot taken");
      Instant after = Instant.now();

      assertNotNull(result.getTimestamp());
      assertFalse(result.getTimestamp().isBefore(before));
      assertFalse(result.getTimestamp().isAfter(after));
    }
  }

  @Nested
  @DisplayName("URL Validation")
  class UrlValidationTests {

    @Test
    @DisplayName("Should accept valid URLs without domain restrictions")
    void shouldAcceptValidUrlsWithoutDomainRestrictions() {
      assertDoesNotThrow(() -> provider.validateUrl("https://example.com"));
      assertDoesNotThrow(() -> provider.validateUrl("http://localhost:8080"));
      assertDoesNotThrow(() -> provider.validateUrl("https://test.example.org/path"));
    }

    @Test
    @DisplayName("Should reject malformed URLs")
    void shouldRejectMalformedUrls() {
      assertThatThrownBy(() -> provider.validateUrl("not-a-url"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("URI is not absolute");

      assertThatThrownBy(() -> provider.validateUrl("://noscheme"))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should enforce allowed domains whitelist")
    void shouldEnforceAllowedDomainsWhitelist() {
      WebTestConfig restrictedConfig =
          WebTestConfig.builder().allowedDomains(Set.of("example.com", "trusted.org")).build();
      TestWebTestProvider restrictedProvider = new TestWebTestProvider(restrictedConfig);

      // Allowed domains should pass
      assertDoesNotThrow(() -> restrictedProvider.validateUrl("https://example.com"));
      assertDoesNotThrow(() -> restrictedProvider.validateUrl("https://trusted.org"));

      // Non-whitelisted domains should fail
      assertThatThrownBy(() -> restrictedProvider.validateUrl("https://untrusted.com"))
          .isInstanceOf(SecurityException.class)
          .hasMessageContaining("Domain not allowed");
    }

    @Test
    @DisplayName("Should enforce blocked domains blacklist")
    void shouldEnforceBlockedDomainsBlacklist() {
      WebTestConfig restrictedConfig =
          WebTestConfig.builder().blockedDomains(Set.of("malicious.com", "spam.org")).build();
      TestWebTestProvider restrictedProvider = new TestWebTestProvider(restrictedConfig);

      // Non-blocked domains should pass
      assertDoesNotThrow(() -> restrictedProvider.validateUrl("https://example.com"));

      // Blocked domains should fail
      assertThatThrownBy(() -> restrictedProvider.validateUrl("https://malicious.com"))
          .isInstanceOf(SecurityException.class)
          .hasMessageContaining("Domain not allowed");

      assertThatThrownBy(() -> restrictedProvider.validateUrl("https://spam.org"))
          .isInstanceOf(SecurityException.class)
          .hasMessageContaining("Domain not allowed");
    }

    @Test
    @DisplayName("Should prioritize blocked domains over allowed domains")
    void shouldPrioritizeBlockedDomainsOverAllowedDomains() {
      WebTestConfig conflictConfig =
          WebTestConfig.builder()
              .allowedDomains(Set.of("example.com"))
              .blockedDomains(Set.of("example.com")) // Same domain in both lists
              .build();
      TestWebTestProvider conflictProvider = new TestWebTestProvider(conflictConfig);

      // Blocked should take precedence
      assertThatThrownBy(() -> conflictProvider.validateUrl("https://example.com"))
          .isInstanceOf(SecurityException.class)
          .hasMessageContaining("Domain not allowed");
    }

    @Test
    @DisplayName("Should handle null URL gracefully")
    void shouldHandleNullUrlGracefully() {
      assertThatThrownBy(() -> provider.validateUrl(null))
          .isInstanceOf(Exception.class); // Could be NPE or IllegalArgumentException
    }
  }

  @Nested
  @DisplayName("Slow-Mo Functionality")
  class SlowMoTests {

    @Test
    @DisplayName("Should not delay when slow-mo is disabled")
    void shouldNotDelayWhenSlowMoDisabled() {
      WebTestConfig fastConfig = WebTestConfig.builder().slowMo(0).build();
      TestWebTestProvider fastProvider = new TestWebTestProvider(fastConfig);

      Instant start = Instant.now();
      fastProvider.applySlowMo().block();

      Duration elapsed = Duration.between(start, Instant.now());
      assertThat(elapsed.toMillis()).isLessThan(50); // Should be very fast
    }

    @Test
    @DisplayName("Should delay when slow-mo is configured")
    void shouldDelayWhenSlowMoConfigured() {
      WebTestConfig slowConfig = WebTestConfig.builder().slowMo(100).build();
      TestWebTestProvider slowProvider = new TestWebTestProvider(slowConfig);

      Instant start = Instant.now();
      slowProvider.applySlowMo().block();

      Duration elapsed = Duration.between(start, Instant.now());
      // Use lenient assertion due to scheduler overhead (allow 50ms tolerance)
      assertThat(elapsed.toMillis()).isGreaterThanOrEqualTo(50); // Should delay at least 50ms
    }

    @Test
    @DisplayName("Should respect configured slow-mo duration")
    void shouldRespectConfiguredSlowMoDuration() {
      WebTestConfig slowConfig = WebTestConfig.builder().slowMo(200).build();
      TestWebTestProvider slowProvider = new TestWebTestProvider(slowConfig);

      Instant start = Instant.now();
      slowProvider.applySlowMo().block();
      Duration elapsed = Duration.between(start, Instant.now());

      // Use lenient assertion due to scheduler overhead (allow 50ms tolerance)
      assertThat(elapsed.toMillis()).isGreaterThanOrEqualTo(150);
    }
  }

  @Nested
  @DisplayName("Operation Logging")
  class OperationLoggingTests {

    @Test
    @DisplayName("Should log operation without errors")
    void shouldLogOperationWithoutErrors() {
      // This test just verifies logging doesn't throw exceptions
      assertDoesNotThrow(() -> provider.logOperation("TEST_OP", "Test details"));
      assertDoesNotThrow(() -> provider.logOperation("NAVIGATE", "https://example.com"));
    }

    @Test
    @DisplayName("Should handle null parameters in logging")
    void shouldHandleNullParametersInLogging() {
      // Logging should be resilient to null values
      assertDoesNotThrow(() -> provider.logOperation(null, "details"));
      assertDoesNotThrow(() -> provider.logOperation("operation", null));
      assertDoesNotThrow(() -> provider.logOperation(null, null));
    }
  }

  /** Concrete test implementation of BaseWebTestProvider for testing purposes. */
  private static class TestWebTestProvider extends BaseWebTestProvider {

    public TestWebTestProvider(WebTestConfig config) {
      super(config);
    }

    // Expose protected methods for testing
    @Override
    public TestResult createSuccessResult(TestResult.OperationType operation, String details) {
      return super.createSuccessResult(operation, details);
    }

    @Override
    public TestResult createFailureResult(TestResult.OperationType operation, String error) {
      return super.createFailureResult(operation, error);
    }

    @Override
    public TestResult createTimedResult(
        TestResult.OperationType operation, Instant start, String details) {
      return super.createTimedResult(operation, start, details);
    }

    @Override
    public void validateUrl(String url) {
      super.validateUrl(url);
    }

    @Override
    public Mono<Void> applySlowMo() {
      return super.applySlowMo();
    }

    @Override
    public void logOperation(String operation, String details) {
      super.logOperation(operation, details);
    }

    // Stub implementations of required interface methods (not tested here)
    @Override
    public Mono<Void> start() {
      return Mono.empty();
    }

    @Override
    public Mono<Void> close() {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> navigateTo(String url) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> click(String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> fillInput(String selector, String text) {
      return Mono.empty();
    }

    @Override
    public Mono<dev.adeengineer.adentic.tool.webtest.model.Screenshot> takeScreenshot() {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> goBack() {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> goForward() {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> refresh() {
      return Mono.empty();
    }

    @Override
    public Mono<String> getCurrentUrl() {
      return Mono.empty();
    }

    @Override
    public Mono<String> getTitle() {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> doubleClick(String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> rightClick(String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> clearInput(String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> selectOption(String selector, String value) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> check(String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> uncheck(String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> hover(String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> submit(String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<String> getText(String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<String> getAttribute(String selector, String attribute) {
      return Mono.empty();
    }

    @Override
    public Mono<dev.adeengineer.adentic.tool.webtest.model.ElementInfo> getElementInfo(
        String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<java.util.List<dev.adeengineer.adentic.tool.webtest.model.ElementInfo>>
        findElements(String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> assertElementExists(String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> assertElementVisible(String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> assertElementEnabled(String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> assertTextContains(String selector, String expected) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> assertTextEquals(String selector, String expected) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> assertUrlMatches(String pattern) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> assertTitleContains(String expected) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> waitForElement(String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> waitForVisible(String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> waitForEnabled(String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> waitFor(long milliseconds) {
      return Mono.empty();
    }

    @Override
    public Mono<dev.adeengineer.adentic.tool.webtest.model.Screenshot> takeScreenshot(
        String selector) {
      return Mono.empty();
    }

    @Override
    public Mono<dev.adeengineer.adentic.tool.webtest.model.Screenshot> saveScreenshot(
        String filePath) {
      return Mono.empty();
    }

    @Override
    public Mono<Object> executeScript(String script) {
      return Mono.empty();
    }

    @Override
    public Mono<Object> executeScript(String script, Object... args) {
      return Mono.empty();
    }

    @Override
    public Mono<dev.adeengineer.adentic.tool.webtest.model.PageState> getPageState() {
      return Mono.empty();
    }

    @Override
    public Mono<String> getPageSource() {
      return Mono.empty();
    }

    @Override
    public Mono<java.util.List<String>> getCookies() {
      return Mono.empty();
    }

    @Override
    public Mono<Void> setCookie(String name, String value) {
      return Mono.empty();
    }

    @Override
    public Mono<Void> clearCookies() {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> acceptAlert() {
      return Mono.empty();
    }

    @Override
    public Mono<TestResult> dismissAlert() {
      return Mono.empty();
    }

    @Override
    public Mono<String> getAlertText() {
      return Mono.empty();
    }

    @Override
    public Mono<Void> setViewportSize(int width, int height) {
      return Mono.empty();
    }

    @Override
    public Mono<Void> maximizeWindow() {
      return Mono.empty();
    }

    @Override
    public Mono<int[]> getWindowSize() {
      return Mono.empty();
    }
  }
}
