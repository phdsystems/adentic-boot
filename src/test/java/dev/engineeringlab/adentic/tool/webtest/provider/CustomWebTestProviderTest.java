package dev.engineeringlab.adentic.tool.webtest.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import dev.engineeringlab.adentic.tool.webtest.config.WebTestConfig;
import dev.engineeringlab.adentic.tool.webtest.model.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

/**
 * Tests for CustomWebTestProvider base class.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>Custom provider extension pattern
 *   <li>Access to base provider utilities
 *   <li>Hook methods for customization
 *   <li>Configuration validation
 *   <li>Custom provider name
 * </ul>
 */
@DisplayName("CustomWebTestProvider Tests")
class CustomWebTestProviderTest {

  private TestCustomProvider provider;
  private WebTestConfig config;

  @BeforeEach
  void setUp() {
    config = WebTestConfig.builder().timeout(5000).build();
    provider = new TestCustomProvider(config);
  }

  @Nested
  @DisplayName("Constructor and Initialization")
  class ConstructorTests {

    @Test
    @DisplayName("Should initialize with custom config")
    void shouldInitializeWithCustomConfig() {
      assertNotNull(provider);
      assertNotNull(provider.getConfig());
      assertEquals(config, provider.getConfig());
    }

    @Test
    @DisplayName("Should inherit from BaseWebTestProvider")
    void shouldInheritFromBaseWebTestProvider() {
      assertTrue(provider instanceof BaseWebTestProvider);
      assertTrue(provider instanceof WebTestProvider);
    }

    @Test
    @DisplayName("Should support custom configuration")
    void shouldSupportCustomConfiguration() {
      WebTestConfig customConfig =
          WebTestConfig.builder()
              .timeout(10000)
              .pageLoadTimeout(15000)
              .viewportWidth(1920)
              .viewportHeight(1080)
              .build();

      TestCustomProvider customProvider = new TestCustomProvider(customConfig);

      assertNotNull(customProvider);
      assertEquals(10000, customProvider.getConfig().getTimeout());
      assertEquals(15000, customProvider.getConfig().getPageLoadTimeout());
      assertEquals(1920, customProvider.getConfig().getViewportWidth());
      assertEquals(1080, customProvider.getConfig().getViewportHeight());
    }
  }

  @Nested
  @DisplayName("Provider Name")
  class ProviderNameTests {

    @Test
    @DisplayName("Should return default provider name")
    void shouldReturnDefaultProviderName() {
      String name = provider.getProviderName();

      assertNotNull(name);
      assertEquals("TestCustomProvider", name);
    }

    @Test
    @DisplayName("Should allow custom provider name override")
    void shouldAllowCustomProviderNameOverride() {
      CustomProviderWithName namedProvider = new CustomProviderWithName(config);

      assertEquals("MyCustomTestProvider", namedProvider.getProviderName());
    }
  }

  @Nested
  @DisplayName("Configuration Validation")
  class ConfigurationValidationTests {

    @Test
    @DisplayName("Should provide default validation (no-op)")
    void shouldProvideDefaultValidation() {
      // Default validation does nothing
      assertDoesNotThrow(() -> provider.validateConfiguration());
    }

    @Test
    @DisplayName("Should support custom validation logic")
    void shouldSupportCustomValidationLogic() {
      ValidatingCustomProvider validatingProvider = new ValidatingCustomProvider(config);

      // Valid config should pass
      assertDoesNotThrow(() -> validatingProvider.validateConfiguration());

      // Invalid config should fail
      WebTestConfig invalidConfig = WebTestConfig.builder().timeout(-1).build();
      ValidatingCustomProvider invalidProvider = new ValidatingCustomProvider(invalidConfig);

      assertThrows(IllegalArgumentException.class, () -> invalidProvider.validateConfiguration());
    }
  }

  @Nested
  @DisplayName("Lifecycle Hooks")
  class LifecycleHooksTests {

    @Test
    @DisplayName("Should provide onStart hook")
    void shouldProvideOnStartHook() {
      HookedCustomProvider hookedProvider = new HookedCustomProvider(config);

      assertFalse(hookedProvider.startHookCalled);

      hookedProvider.start().block();

      assertTrue(hookedProvider.startHookCalled);
    }

    @Test
    @DisplayName("Should provide onClose hook")
    void shouldProvideOnCloseHook() {
      HookedCustomProvider hookedProvider = new HookedCustomProvider(config);
      hookedProvider.start().block();

      assertFalse(hookedProvider.closeHookCalled);

      hookedProvider.close().block();

      assertTrue(hookedProvider.closeHookCalled);
    }

    @Test
    @DisplayName("Should call hooks in correct order")
    void shouldCallHooksInCorrectOrder() {
      HookedCustomProvider hookedProvider = new HookedCustomProvider(config);

      hookedProvider.start().block();
      assertTrue(hookedProvider.isRunning());
      assertTrue(hookedProvider.startHookCalled);
      assertFalse(hookedProvider.closeHookCalled);

      hookedProvider.close().block();
      assertFalse(hookedProvider.isRunning());
      assertTrue(hookedProvider.closeHookCalled);
    }
  }

  @Nested
  @DisplayName("Access to Base Utilities")
  class BaseUtilitiesTests {

    @Test
    @DisplayName("Should access createSuccessResult helper")
    void shouldAccessCreateSuccessResultHelper() {
      TestResult result =
          provider.createSuccessResult(TestResult.OperationType.NAVIGATE, "Test navigation");

      assertNotNull(result);
      assertTrue(result.isPassed());
      assertEquals("Test navigation", result.getDetails());
    }

    @Test
    @DisplayName("Should access createFailureResult helper")
    void shouldAccessCreateFailureResultHelper() {
      TestResult result =
          provider.createFailureResult(TestResult.OperationType.CLICK, "Element not found");

      assertNotNull(result);
      assertFalse(result.isPassed());
      assertEquals("Element not found", result.getError());
    }

    @Test
    @DisplayName("Should access validateUrl helper")
    void shouldAccessValidateUrlHelper() {
      assertDoesNotThrow(() -> provider.validateUrl("https://example.com"));

      assertThrows(Exception.class, () -> provider.validateUrl("not-a-url"));
    }

    @Test
    @DisplayName("Should access logOperation helper")
    void shouldAccessLogOperationHelper() {
      assertDoesNotThrow(() -> provider.logOperation("TEST", "Test operation"));
    }

    @Test
    @DisplayName("Should access applySlowMo helper")
    void shouldAccessApplySlowMoHelper() {
      Mono<Void> slowMo = provider.applySlowMo();

      assertNotNull(slowMo);
      assertDoesNotThrow(() -> slowMo.block());
    }
  }

  @Nested
  @DisplayName("Custom Implementation Pattern")
  class CustomImplementationPatternTests {

    @Test
    @DisplayName("Should implement WebTestProvider interface")
    void shouldImplementWebTestProviderInterface() {
      assertTrue(provider instanceof WebTestProvider);

      // All methods should be callable
      assertDoesNotThrow(
          () -> {
            provider.start().block();
            provider.navigateTo("https://example.com").block();
            provider.click("#button").block();
            provider.takeScreenshot().block();
            provider.close().block();
          });
    }

    @Test
    @DisplayName("Should use custom implementation logic")
    void shouldUseCustomImplementationLogic() {
      provider.start().block();

      TestResult result = provider.navigateTo("https://custom-test.com").block();

      assertNotNull(result);
      assertTrue(result.isPassed());
      assertThat(result.getDetails()).contains("Custom navigation");
    }

    @Test
    @DisplayName("Should support custom browser automation")
    void shouldSupportCustomBrowserAutomation() {
      provider.start().block();

      // Custom click implementation
      TestResult clickResult = provider.click(".custom-selector").block();
      assertTrue(clickResult.isPassed());

      // Custom input fill implementation
      TestResult fillResult = provider.fillInput("#custom-input", "test").block();
      assertTrue(fillResult.isPassed());

      provider.close().block();
    }
  }

  @Nested
  @DisplayName("Error Handling")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should handle custom errors gracefully")
    void shouldHandleCustomErrorsGracefully() {
      ErrorThrowingCustomProvider errorProvider = new ErrorThrowingCustomProvider(config);

      errorProvider.start().block();

      TestResult result = errorProvider.navigateTo("https://fail.com").block();

      assertNotNull(result);
      assertFalse(result.isPassed());
      assertThat(result.getError()).contains("Custom error");
    }
  }

  /** Test implementation of CustomWebTestProvider for testing purposes. */
  private static class TestCustomProvider extends CustomWebTestProvider {

    public TestCustomProvider(WebTestConfig config) {
      super(config);
    }

    @Override
    public Mono<Void> start() {
      return Mono.fromRunnable(
          () -> {
            running = true;
            onStart();
          });
    }

    @Override
    public Mono<Void> close() {
      return Mono.fromRunnable(
          () -> {
            onClose();
            running = false;
          });
    }

    @Override
    public Mono<TestResult> navigateTo(String url) {
      return Mono.fromCallable(
          () -> {
            validateUrl(url);
            logOperation("NAVIGATE", url);
            return createSuccessResult(
                TestResult.OperationType.NAVIGATE, "Custom navigation to: " + url);
          });
    }

    @Override
    public Mono<TestResult> click(String selector) {
      return Mono.just(
          createSuccessResult(TestResult.OperationType.CLICK, "Custom click: " + selector));
    }

    @Override
    public Mono<TestResult> fillInput(String selector, String text) {
      return Mono.just(
          createSuccessResult(TestResult.OperationType.FILL_INPUT, "Custom fill: " + selector));
    }

    @Override
    public Mono<Screenshot> takeScreenshot() {
      return Mono.just(Screenshot.builder().success(true).data(new byte[0]).build());
    }

    // Minimal stubs for other required methods
    @Override
    public Mono<TestResult> goBack() {
      return Mono.just(createSuccessResult(TestResult.OperationType.NAVIGATE, "Back"));
    }

    @Override
    public Mono<TestResult> goForward() {
      return Mono.just(createSuccessResult(TestResult.OperationType.NAVIGATE, "Forward"));
    }

    @Override
    public Mono<TestResult> refresh() {
      return Mono.just(createSuccessResult(TestResult.OperationType.NAVIGATE, "Refresh"));
    }

    @Override
    public Mono<String> getCurrentUrl() {
      return Mono.just("https://example.com");
    }

    @Override
    public Mono<String> getTitle() {
      return Mono.just("Title");
    }

    @Override
    public Mono<TestResult> doubleClick(String selector) {
      return Mono.just(createSuccessResult(TestResult.OperationType.CLICK, "DoubleClick"));
    }

    @Override
    public Mono<TestResult> rightClick(String selector) {
      return Mono.just(createSuccessResult(TestResult.OperationType.CLICK, "RightClick"));
    }

    @Override
    public Mono<TestResult> clearInput(String selector) {
      return Mono.just(createSuccessResult(TestResult.OperationType.FILL_INPUT, "Clear"));
    }

    @Override
    public Mono<TestResult> selectOption(String selector, String value) {
      return Mono.just(createSuccessResult(TestResult.OperationType.SELECT_OPTION, "Select"));
    }

    @Override
    public Mono<TestResult> check(String selector) {
      return Mono.just(createSuccessResult(TestResult.OperationType.CLICK, "Check"));
    }

    @Override
    public Mono<TestResult> uncheck(String selector) {
      return Mono.just(createSuccessResult(TestResult.OperationType.CLICK, "Uncheck"));
    }

    @Override
    public Mono<TestResult> hover(String selector) {
      return Mono.just(createSuccessResult(TestResult.OperationType.CLICK, "Hover"));
    }

    @Override
    public Mono<TestResult> submit(String selector) {
      return Mono.just(createSuccessResult(TestResult.OperationType.SUBMIT, "Submit"));
    }

    @Override
    public Mono<String> getText(String selector) {
      return Mono.just("Text");
    }

    @Override
    public Mono<String> getAttribute(String selector, String attribute) {
      return Mono.just("");
    }

    @Override
    public Mono<ElementInfo> getElementInfo(String selector) {
      return Mono.just(ElementInfo.builder().selector(selector).build());
    }

    @Override
    public Mono<List<ElementInfo>> findElements(String selector) {
      return Mono.just(List.of());
    }

    @Override
    public Mono<TestResult> assertElementExists(String selector) {
      return Mono.just(createSuccessResult(TestResult.OperationType.ASSERT_EXISTS, "Exists"));
    }

    @Override
    public Mono<TestResult> assertElementVisible(String selector) {
      return Mono.just(createSuccessResult(TestResult.OperationType.ASSERT_VISIBLE, "Visible"));
    }

    @Override
    public Mono<TestResult> assertElementEnabled(String selector) {
      return Mono.just(createSuccessResult(TestResult.OperationType.ASSERT_ENABLED, "Enabled"));
    }

    @Override
    public Mono<TestResult> assertTextContains(String selector, String expected) {
      return Mono.just(createSuccessResult(TestResult.OperationType.ASSERT_TEXT, "Contains"));
    }

    @Override
    public Mono<TestResult> assertTextEquals(String selector, String expected) {
      return Mono.just(createSuccessResult(TestResult.OperationType.ASSERT_TEXT, "Equals"));
    }

    @Override
    public Mono<TestResult> assertUrlMatches(String pattern) {
      return Mono.just(createSuccessResult(TestResult.OperationType.ASSERT_URL, "URLMatches"));
    }

    @Override
    public Mono<TestResult> assertTitleContains(String expected) {
      return Mono.just(createSuccessResult(TestResult.OperationType.ASSERT_TEXT, "TitleContains"));
    }

    @Override
    public Mono<TestResult> waitForElement(String selector) {
      return Mono.just(createSuccessResult(TestResult.OperationType.WAIT, "Wait"));
    }

    @Override
    public Mono<TestResult> waitForVisible(String selector) {
      return Mono.just(createSuccessResult(TestResult.OperationType.WAIT, "WaitVisible"));
    }

    @Override
    public Mono<TestResult> waitForEnabled(String selector) {
      return Mono.just(createSuccessResult(TestResult.OperationType.WAIT, "WaitEnabled"));
    }

    @Override
    public Mono<TestResult> waitFor(long milliseconds) {
      return Mono.delay(java.time.Duration.ofMillis(milliseconds))
          .then(Mono.just(createSuccessResult(TestResult.OperationType.WAIT, "Waited")));
    }

    @Override
    public Mono<Screenshot> takeScreenshot(String selector) {
      return Mono.just(Screenshot.builder().success(true).build());
    }

    @Override
    public Mono<Screenshot> saveScreenshot(String filePath) {
      return Mono.just(Screenshot.builder().success(true).filePath(filePath).build());
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
    public Mono<PageState> getPageState() {
      return Mono.just(PageState.builder().url("https://example.com").build());
    }

    @Override
    public Mono<String> getPageSource() {
      return Mono.just("<html></html>");
    }

    @Override
    public Mono<List<String>> getCookies() {
      return Mono.just(List.of());
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
      return Mono.just(createSuccessResult(TestResult.OperationType.CLICK, "Accept"));
    }

    @Override
    public Mono<TestResult> dismissAlert() {
      return Mono.just(createSuccessResult(TestResult.OperationType.CLICK, "Dismiss"));
    }

    @Override
    public Mono<String> getAlertText() {
      return Mono.just("");
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
      return Mono.just(new int[] {1280, 720});
    }
  }

  /** Custom provider with custom name. */
  private static class CustomProviderWithName extends TestCustomProvider {
    public CustomProviderWithName(WebTestConfig config) {
      super(config);
    }

    @Override
    public String getProviderName() {
      return "MyCustomTestProvider";
    }
  }

  /** Custom provider with validation logic. */
  private static class ValidatingCustomProvider extends TestCustomProvider {
    public ValidatingCustomProvider(WebTestConfig config) {
      super(config);
    }

    @Override
    protected void validateConfiguration() {
      if (config.getTimeout() < 0) {
        throw new IllegalArgumentException("Timeout must be positive");
      }
    }
  }

  /** Custom provider with lifecycle hooks. */
  private static class HookedCustomProvider extends TestCustomProvider {
    boolean startHookCalled = false;
    boolean closeHookCalled = false;

    public HookedCustomProvider(WebTestConfig config) {
      super(config);
    }

    @Override
    protected void onStart() {
      startHookCalled = true;
    }

    @Override
    protected void onClose() {
      closeHookCalled = true;
    }
  }

  /** Custom provider that throws errors. */
  private static class ErrorThrowingCustomProvider extends TestCustomProvider {
    public ErrorThrowingCustomProvider(WebTestConfig config) {
      super(config);
    }

    @Override
    public Mono<TestResult> navigateTo(String url) {
      return Mono.just(
          createFailureResult(
              TestResult.OperationType.NAVIGATE, "Custom error: Navigation failed"));
    }
  }
}
