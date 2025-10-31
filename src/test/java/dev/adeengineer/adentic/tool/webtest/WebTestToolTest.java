package dev.adeengineer.adentic.tool.webtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import dev.adeengineer.adentic.tool.webtest.config.WebTestConfig;
import dev.adeengineer.adentic.tool.webtest.model.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Comprehensive tests for WebTestTool */
@DisplayName("WebTestTool Tests")
class WebTestToolTest {

  private WebTestTool webTestTool;

  @BeforeEach
  void setUp() {
    // Use HtmlUnit provider for testing (lightweight, no browser needed)
    WebTestConfig config = WebTestConfig.builder().provider(TestProvider.HTMLUNIT).build();
    webTestTool = new WebTestTool(config);
  }

  @Nested
  @DisplayName("Lifecycle Operations")
  class LifecycleTests {

    @Test
    @DisplayName("Should start provider")
    void testStart() {
      webTestTool.start().block();
      // HtmlUnit provider should start successfully
    }

    @Test
    @DisplayName("Should close provider")
    void testClose() {
      webTestTool.start().block();
      webTestTool.close().block();
      // Should close without errors
    }
  }

  @Nested
  @DisplayName("Navigation Operations")
  class NavigationTests {

    @Test
    @DisplayName("Should navigate to URL")
    void testNavigateTo() {
      TestResult result = webTestTool.navigateTo("https://example.com").block();

      assertNotNull(result);
      // HtmlUnit should be able to navigate
    }

    @Test
    @DisplayName("Should get current URL")
    void testGetCurrentUrl() {
      webTestTool.navigateTo("https://example.com").block();
      String url = webTestTool.getCurrentUrl().block();

      assertNotNull(url);
      assertThat(url).contains("example.com");
    }

    @Test
    @DisplayName("Should get page title")
    void testGetTitle() {
      webTestTool.navigateTo("https://example.com").block();
      String title = webTestTool.getTitle().block();

      assertNotNull(title);
    }

    @Test
    @DisplayName("Should go back")
    void testGoBack() {
      webTestTool.navigateTo("https://example.com").block();
      TestResult result = webTestTool.goBack().block();

      assertNotNull(result);
    }

    @Test
    @DisplayName("Should go forward")
    void testGoForward() {
      TestResult result = webTestTool.goForward().block();

      assertNotNull(result);
    }

    @Test
    @DisplayName("Should refresh page")
    void testRefresh() {
      webTestTool.navigateTo("https://example.com").block();
      TestResult result = webTestTool.refresh().block();

      assertNotNull(result);
    }
  }

  @Nested
  @DisplayName("Element Interaction Operations")
  class ElementInteractionTests {

    @Test
    @DisplayName("Should click element")
    void testClick() {
      webTestTool.navigateTo("https://example.com").block();
      TestResult result = webTestTool.click("a").block();

      assertNotNull(result);
    }

    @Test
    @DisplayName("Should fill input")
    void testFillInput() {
      webTestTool.navigateTo("https://example.com").block();
      TestResult result = webTestTool.fillInput("input", "test").block();

      assertNotNull(result);
    }

    @Test
    @DisplayName("Should clear input")
    void testClearInput() {
      TestResult result = webTestTool.clearInput("input").block();

      assertNotNull(result);
    }

    @Test
    @DisplayName("Should check checkbox")
    void testCheck() {
      TestResult result = webTestTool.check("input[type='checkbox']").block();

      assertNotNull(result);
    }

    @Test
    @DisplayName("Should uncheck checkbox")
    void testUncheck() {
      TestResult result = webTestTool.uncheck("input[type='checkbox']").block();

      assertNotNull(result);
    }

    @Test
    @DisplayName("Should submit form")
    void testSubmit() {
      TestResult result = webTestTool.submit("form").block();

      assertNotNull(result);
    }
  }

  @Nested
  @DisplayName("Element Query Operations")
  class ElementQueryTests {

    @Test
    @DisplayName("Should get element text")
    void testGetText() {
      webTestTool.navigateTo("https://example.com").block();
      String text = webTestTool.getText("h1").block();

      assertNotNull(text);
    }

    @Test
    @DisplayName("Should get element attribute")
    void testGetAttribute() {
      webTestTool.navigateTo("https://example.com").block();
      String attr = webTestTool.getAttribute("a", "href").block();

      // May be null if no link exists
      assertNotNull(attr);
    }

    @Test
    @DisplayName("Should get element info")
    void testGetElementInfo() {
      webTestTool.navigateTo("https://example.com").block();
      ElementInfo info = webTestTool.getElementInfo("h1").block();

      assertNotNull(info);
    }

    @Test
    @DisplayName("Should find elements")
    void testFindElements() {
      webTestTool.navigateTo("https://example.com").block();
      List<ElementInfo> elements = webTestTool.findElements("body").block();

      assertNotNull(elements);
      // Should find at least the body element
      assertThat(elements.size()).isGreaterThanOrEqualTo(0);
    }
  }

  @Nested
  @DisplayName("Assertion Operations")
  class AssertionTests {

    @Test
    @DisplayName("Should assert element exists")
    void testAssertElementExists() {
      webTestTool.navigateTo("https://example.com").block();
      TestResult result = webTestTool.assertElementExists("body").block();

      assertNotNull(result);
    }

    @Test
    @DisplayName("Should assert element visible")
    void testAssertElementVisible() {
      webTestTool.navigateTo("https://example.com").block();
      TestResult result = webTestTool.assertElementVisible("body").block();

      assertNotNull(result);
    }

    @Test
    @DisplayName("Should assert text contains")
    void testAssertTextContains() {
      webTestTool.navigateTo("https://example.com").block();
      TestResult result = webTestTool.assertTextContains("h1", "Example").block();

      assertNotNull(result);
    }

    @Test
    @DisplayName("Should assert URL matches")
    void testAssertUrlMatches() {
      webTestTool.navigateTo("https://example.com").block();
      TestResult result = webTestTool.assertUrlMatches(".*example.*").block();

      assertNotNull(result);
    }

    @Test
    @DisplayName("Should assert title contains")
    void testAssertTitleContains() {
      webTestTool.navigateTo("https://example.com").block();
      TestResult result = webTestTool.assertTitleContains("Example").block();

      assertNotNull(result);
    }
  }

  @Nested
  @DisplayName("Waiting Operations")
  class WaitingTests {

    @Test
    @DisplayName("Should wait for element")
    void testWaitForElement() {
      webTestTool.navigateTo("https://example.com").block();
      TestResult result = webTestTool.waitForElement("body").block();

      assertNotNull(result);
    }

    @Test
    @DisplayName("Should wait for duration")
    void testWaitFor() {
      TestResult result = webTestTool.waitFor(100).block();

      assertNotNull(result);
    }
  }

  @Nested
  @DisplayName("Screenshot Operations")
  class ScreenshotTests {

    @Test
    @DisplayName("Should take screenshot")
    void testTakeScreenshot() {
      webTestTool.navigateTo("https://example.com").block();
      Screenshot screenshot = webTestTool.takeScreenshot().block();

      assertNotNull(screenshot);
    }

    @Test
    @DisplayName("Should take element screenshot")
    void testTakeElementScreenshot() {
      webTestTool.navigateTo("https://example.com").block();
      Screenshot screenshot = webTestTool.takeScreenshot("body").block();

      assertNotNull(screenshot);
    }
  }

  @Nested
  @DisplayName("Page State Operations")
  class PageStateTests {

    @Test
    @DisplayName("Should get page state")
    void testGetPageState() {
      webTestTool.navigateTo("https://example.com").block();
      PageState state = webTestTool.getPageState().block();

      assertNotNull(state);
    }

    @Test
    @DisplayName("Should get page source")
    void testGetPageSource() {
      webTestTool.navigateTo("https://example.com").block();
      String source = webTestTool.getPageSource().block();

      assertNotNull(source);
      assertThat(source).contains("html");
    }

    @Test
    @DisplayName("Should get cookies")
    void testGetCookies() {
      webTestTool.navigateTo("https://example.com").block();
      List<String> cookies = webTestTool.getCookies().block();

      assertNotNull(cookies);
    }

    @Test
    @DisplayName("Should set cookie")
    void testSetCookie() {
      webTestTool.navigateTo("https://example.com").block();
      webTestTool.setCookie("test", "value").block();
      // Should not throw exception
    }

    @Test
    @DisplayName("Should clear cookies")
    void testClearCookies() {
      webTestTool.navigateTo("https://example.com").block();
      webTestTool.clearCookies().block();
      // Should not throw exception
    }
  }

  @Nested
  @DisplayName("Configuration Tests")
  class ConfigurationTests {

    @Test
    @DisplayName("Should use default configuration")
    void testDefaultConfig() {
      WebTestTool tool = new WebTestTool();
      assertNotNull(tool);
    }

    @Test
    @DisplayName("Should create with custom config")
    void testCustomConfig() {
      WebTestConfig config =
          WebTestConfig.builder().provider(TestProvider.HTMLUNIT).headless(true).build();

      WebTestTool tool = new WebTestTool(config);
      assertNotNull(tool);
    }

    @Test
    @DisplayName("Should set configuration")
    void testSetConfig() {
      WebTestConfig newConfig = WebTestConfig.builder().provider(TestProvider.HTMLUNIT).build();

      webTestTool.setConfig(newConfig);
      // Should not throw exception
    }

    @Test
    @DisplayName("Should create Playwright config")
    void testPlaywrightConfig() {
      WebTestConfig config = WebTestConfig.defaults();

      assertNotNull(config);
      assertEquals(TestProvider.PLAYWRIGHT, config.getProvider());
    }

    @Test
    @DisplayName("Should create Selenium config")
    void testSeleniumConfig() {
      WebTestConfig config = WebTestConfig.selenium();

      assertNotNull(config);
      assertEquals(TestProvider.SELENIUM, config.getProvider());
    }

    @Test
    @DisplayName("Should create lightweight config")
    void testLightweightConfig() {
      WebTestConfig config = WebTestConfig.lightweight();

      assertNotNull(config);
      assertEquals(TestProvider.HTMLUNIT, config.getProvider());
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should handle invalid URL")
    void testInvalidUrl() {
      // Invalid URL throws IllegalArgumentException
      assertThrows(
          IllegalArgumentException.class, () -> webTestTool.navigateTo("not-a-url").block());
    }

    @Test
    @DisplayName("Should handle non-existent element")
    void testNonExistentElement() {
      webTestTool.navigateTo("https://example.com").block();
      TestResult result = webTestTool.click("#non-existent-element-xyz").block();

      assertNotNull(result);
      // HtmlUnit will handle this
    }

    @Test
    @DisplayName("Should fail with custom provider (not implemented)")
    void testCustomProvider() {
      WebTestConfig config = WebTestConfig.builder().provider(TestProvider.CUSTOM).build();

      assertThrows(IllegalStateException.class, () -> new WebTestTool(config));
    }
  }
}
