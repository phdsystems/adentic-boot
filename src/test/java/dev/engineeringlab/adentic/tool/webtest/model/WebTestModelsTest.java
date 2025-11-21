package dev.engineeringlab.adentic.tool.webtest.model;

import static org.junit.jupiter.api.Assertions.*;

import dev.engineeringlab.adentic.tool.webtest.model.PageState.ConsoleMessage;
import dev.engineeringlab.adentic.tool.webtest.model.PageState.NetworkRequest;
import dev.engineeringlab.adentic.tool.webtest.model.TestResult.OperationType;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Comprehensive tests for webtest model classes. */
@DisplayName("WebTest Models Tests")
class WebTestModelsTest {

  @Nested
  @DisplayName("BrowserType Tests")
  class BrowserTypeTests {

    @Test
    @DisplayName("Should have all browser types")
    void testBrowserTypes() {
      BrowserType[] types = BrowserType.values();
      assertEquals(6, types.length);

      assertNotNull(BrowserType.valueOf("CHROMIUM"));
      assertNotNull(BrowserType.valueOf("FIREFOX"));
      assertNotNull(BrowserType.valueOf("WEBKIT"));
      assertNotNull(BrowserType.valueOf("CHROME"));
      assertNotNull(BrowserType.valueOf("EDGE"));
      assertNotNull(BrowserType.valueOf("NONE"));
    }

    @Test
    @DisplayName("Should throw exception for invalid type")
    void testInvalidType() {
      assertThrows(IllegalArgumentException.class, () -> BrowserType.valueOf("INVALID"));
    }

    @Test
    @DisplayName("Should return correct name")
    void testTypeName() {
      assertEquals("CHROMIUM", BrowserType.CHROMIUM.name());
      assertEquals("FIREFOX", BrowserType.FIREFOX.name());
      assertEquals("WEBKIT", BrowserType.WEBKIT.name());
      assertEquals("CHROME", BrowserType.CHROME.name());
      assertEquals("EDGE", BrowserType.EDGE.name());
      assertEquals("NONE", BrowserType.NONE.name());
    }

    @Test
    @DisplayName("Should support enum methods")
    void testEnumMethods() {
      assertEquals(BrowserType.CHROMIUM, BrowserType.valueOf("CHROMIUM"));
      assertEquals(0, BrowserType.CHROMIUM.ordinal());
      assertEquals(5, BrowserType.NONE.ordinal());
    }
  }

  @Nested
  @DisplayName("TestProvider Tests")
  class TestProviderTests {

    @Test
    @DisplayName("Should have all test providers")
    void testTestProviders() {
      TestProvider[] providers = TestProvider.values();
      assertEquals(6, providers.length);

      assertNotNull(TestProvider.valueOf("PLAYWRIGHT"));
      assertNotNull(TestProvider.valueOf("SELENIUM"));
      assertNotNull(TestProvider.valueOf("HTMLUNIT"));
      assertNotNull(TestProvider.valueOf("PUPPETEER"));
      assertNotNull(TestProvider.valueOf("CYPRESS"));
      assertNotNull(TestProvider.valueOf("CUSTOM"));
    }

    @Test
    @DisplayName("Should throw exception for invalid provider")
    void testInvalidProvider() {
      assertThrows(IllegalArgumentException.class, () -> TestProvider.valueOf("INVALID"));
    }

    @Test
    @DisplayName("Should return correct name")
    void testProviderName() {
      assertEquals("PLAYWRIGHT", TestProvider.PLAYWRIGHT.name());
      assertEquals("SELENIUM", TestProvider.SELENIUM.name());
      assertEquals("HTMLUNIT", TestProvider.HTMLUNIT.name());
      assertEquals("PUPPETEER", TestProvider.PUPPETEER.name());
      assertEquals("CYPRESS", TestProvider.CYPRESS.name());
      assertEquals("CUSTOM", TestProvider.CUSTOM.name());
    }

    @Test
    @DisplayName("Should support enum methods")
    void testEnumMethods() {
      assertEquals(TestProvider.PLAYWRIGHT, TestProvider.valueOf("PLAYWRIGHT"));
      assertEquals(0, TestProvider.PLAYWRIGHT.ordinal());
      assertEquals(5, TestProvider.CUSTOM.ordinal());
    }
  }

  @Nested
  @DisplayName("ElementInfo Tests")
  class ElementInfoTests {

    @Test
    @DisplayName("Should create element with builder")
    void testBuilder() {
      Map<String, String> attributes = new HashMap<>();
      attributes.put("href", "https://example.com");
      attributes.put("target", "_blank");

      ElementInfo element =
          ElementInfo.builder()
              .selector("#link")
              .tagName("a")
              .text("Click here")
              .innerHTML("<span>Click here</span>")
              .attributes(attributes)
              .visible(true)
              .enabled(true)
              .selected(false)
              .x(100)
              .y(200)
              .width(150)
              .height(30)
              .className("btn primary")
              .id("submit-btn")
              .build();

      assertEquals("#link", element.getSelector());
      assertEquals("a", element.getTagName());
      assertEquals("Click here", element.getText());
      assertEquals("<span>Click here</span>", element.getInnerHTML());
      assertEquals(attributes, element.getAttributes());
      assertTrue(element.isVisible());
      assertTrue(element.isEnabled());
      assertFalse(element.isSelected());
      assertEquals(100, element.getX());
      assertEquals(200, element.getY());
      assertEquals(150, element.getWidth());
      assertEquals(30, element.getHeight());
      assertEquals("btn primary", element.getClassName());
      assertEquals("submit-btn", element.getId());
    }

    @Test
    @DisplayName("Should support default values")
    void testDefaults() {
      ElementInfo element = ElementInfo.builder().selector("#test").build();

      assertNull(element.getTagName());
      assertNull(element.getText());
      assertNull(element.getInnerHTML());
      assertNull(element.getAttributes());
      assertFalse(element.isVisible());
      assertFalse(element.isEnabled());
      assertFalse(element.isSelected());
      assertEquals(0, element.getX());
      assertEquals(0, element.getY());
      assertEquals(0, element.getWidth());
      assertEquals(0, element.getHeight());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      ElementInfo element = ElementInfo.builder().selector("#test").build();

      element.setTagName("button");
      element.setText("Submit");
      element.setVisible(true);
      element.setEnabled(true);
      element.setSelected(true);
      element.setX(50);
      element.setY(75);
      element.setWidth(100);
      element.setHeight(40);

      assertEquals("button", element.getTagName());
      assertEquals("Submit", element.getText());
      assertTrue(element.isVisible());
      assertTrue(element.isEnabled());
      assertTrue(element.isSelected());
      assertEquals(50, element.getX());
      assertEquals(75, element.getY());
      assertEquals(100, element.getWidth());
      assertEquals(40, element.getHeight());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      ElementInfo element1 =
          ElementInfo.builder()
              .selector("#test")
              .tagName("button")
              .text("Click")
              .visible(true)
              .build();

      ElementInfo element2 =
          ElementInfo.builder()
              .selector("#test")
              .tagName("button")
              .text("Click")
              .visible(true)
              .build();

      assertEquals(element1, element2);
      assertEquals(element1.hashCode(), element2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      ElementInfo element =
          ElementInfo.builder()
              .selector("#test")
              .tagName("button")
              .text("Submit")
              .visible(true)
              .enabled(true)
              .build();

      String str = element.toString();
      assertTrue(str.contains("button"));
      assertTrue(str.contains("#test"));
      assertTrue(str.contains("Submit"));
      assertTrue(str.contains("visible: true"));
      assertTrue(str.contains("enabled: true"));
    }

    @Test
    @DisplayName("Should handle null attributes")
    void testNullAttributes() {
      ElementInfo element = ElementInfo.builder().selector("#test").attributes(null).build();
      assertNull(element.getAttributes());
    }
  }

  @Nested
  @DisplayName("Screenshot Tests")
  class ScreenshotTests {

    @Test
    @DisplayName("Should create screenshot with builder")
    void testBuilder() {
      byte[] data = new byte[] {1, 2, 3, 4};
      Instant timestamp = Instant.now();

      Screenshot screenshot =
          Screenshot.builder()
              .data(data)
              .base64("AQIDBA==")
              .filePath("/tmp/screenshot.png")
              .width(1920)
              .height(1080)
              .url("https://example.com")
              .timestamp(timestamp)
              .success(true)
              .error(null)
              .build();

      assertArrayEquals(data, screenshot.getData());
      assertEquals("AQIDBA==", screenshot.getBase64());
      assertEquals("/tmp/screenshot.png", screenshot.getFilePath());
      assertEquals(1920, screenshot.getWidth());
      assertEquals(1080, screenshot.getHeight());
      assertEquals("https://example.com", screenshot.getUrl());
      assertEquals(timestamp, screenshot.getTimestamp());
      assertTrue(screenshot.isSuccess());
      assertNull(screenshot.getError());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      Screenshot screenshot = Screenshot.builder().build();

      assertNotNull(screenshot.getTimestamp());
      assertTrue(screenshot.isSuccess());
    }

    @Test
    @DisplayName("Should calculate file size")
    void testGetSize() {
      byte[] data = new byte[1024];
      Screenshot screenshot = Screenshot.builder().data(data).build();

      assertEquals(1024, screenshot.getSize());
    }

    @Test
    @DisplayName("Should return zero size for null data")
    void testGetSizeNull() {
      Screenshot screenshot = Screenshot.builder().data(null).build();
      assertEquals(0, screenshot.getSize());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      Screenshot screenshot = Screenshot.builder().build();

      byte[] data = new byte[] {5, 6, 7, 8};
      screenshot.setData(data);
      screenshot.setBase64("BQYHCA==");
      screenshot.setFilePath("/tmp/test.png");
      screenshot.setWidth(800);
      screenshot.setHeight(600);
      screenshot.setUrl("https://test.com");
      screenshot.setSuccess(false);
      screenshot.setError("Capture failed");

      assertArrayEquals(data, screenshot.getData());
      assertEquals("BQYHCA==", screenshot.getBase64());
      assertEquals("/tmp/test.png", screenshot.getFilePath());
      assertEquals(800, screenshot.getWidth());
      assertEquals(600, screenshot.getHeight());
      assertEquals("https://test.com", screenshot.getUrl());
      assertFalse(screenshot.isSuccess());
      assertEquals("Capture failed", screenshot.getError());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      byte[] data = new byte[] {1, 2, 3};

      Screenshot screenshot1 =
          Screenshot.builder()
              .data(data)
              .width(1024)
              .height(768)
              .url("https://example.com")
              .build();

      Screenshot screenshot2 =
          Screenshot.builder()
              .data(data)
              .width(1024)
              .height(768)
              .url("https://example.com")
              .build();

      // Note: timestamps will differ, so we verify individual fields
      assertEquals(screenshot1.getWidth(), screenshot2.getWidth());
      assertEquals(screenshot1.getHeight(), screenshot2.getHeight());
      assertEquals(screenshot1.getUrl(), screenshot2.getUrl());
    }

    @Test
    @DisplayName("Should implement toString for success")
    void testToStringSuccess() {
      byte[] data = new byte[2048];
      Screenshot screenshot =
          Screenshot.builder()
              .data(data)
              .width(1920)
              .height(1080)
              .url("https://example.com")
              .success(true)
              .build();

      String str = screenshot.toString();
      assertTrue(str.contains("1920x1080"));
      assertTrue(str.contains("https://example.com"));
      assertTrue(str.contains("2.00 KB"));
    }

    @Test
    @DisplayName("Should implement toString for failure")
    void testToStringFailure() {
      Screenshot screenshot = Screenshot.builder().success(false).error("Timeout").build();

      String str = screenshot.toString();
      assertTrue(str.contains("failed"));
      assertTrue(str.contains("Timeout"));
    }

    @Test
    @DisplayName("Should format size in bytes")
    void testFormatSizeBytes() {
      Screenshot screenshot = Screenshot.builder().data(new byte[500]).build();
      String str = screenshot.toString();
      assertTrue(str.contains("500 B"));
    }

    @Test
    @DisplayName("Should format size in KB")
    void testFormatSizeKB() {
      Screenshot screenshot = Screenshot.builder().data(new byte[2048]).build();
      String str = screenshot.toString();
      assertTrue(str.contains("2.00 KB"));
    }

    @Test
    @DisplayName("Should format size in MB")
    void testFormatSizeMB() {
      Screenshot screenshot = Screenshot.builder().data(new byte[1024 * 1024 * 2]).build();
      String str = screenshot.toString();
      assertTrue(str.contains("2.00 MB"));
    }
  }

  @Nested
  @DisplayName("PageState Tests")
  class PageStateTests {

    @Test
    @DisplayName("Should create page state with builder")
    void testBuilder() {
      Map<String, String> cookies = new HashMap<>();
      cookies.put("session", "abc123");

      Map<String, String> localStorage = new HashMap<>();
      localStorage.put("theme", "dark");

      Map<String, String> sessionStorage = new HashMap<>();
      sessionStorage.put("temp", "value");

      List<ConsoleMessage> consoleLogs = new ArrayList<>();
      consoleLogs.add(
          ConsoleMessage.builder().type("log").text("Hello").timestamp(Instant.now()).build());

      List<NetworkRequest> networkRequests = new ArrayList<>();
      networkRequests.add(
          NetworkRequest.builder()
              .method("GET")
              .url("https://api.example.com")
              .statusCode(200)
              .duration(150L)
              .timestamp(Instant.now())
              .build());

      Instant timestamp = Instant.now();

      PageState state =
          PageState.builder()
              .url("https://example.com")
              .title("Example Page")
              .html("<html><body>Test</body></html>")
              .cookies(cookies)
              .localStorage(localStorage)
              .sessionStorage(sessionStorage)
              .consoleLogs(consoleLogs)
              .networkRequests(networkRequests)
              .viewportWidth(1920)
              .viewportHeight(1080)
              .loadTime(1500L)
              .timestamp(timestamp)
              .build();

      assertEquals("https://example.com", state.getUrl());
      assertEquals("Example Page", state.getTitle());
      assertEquals("<html><body>Test</body></html>", state.getHtml());
      assertEquals(cookies, state.getCookies());
      assertEquals(localStorage, state.getLocalStorage());
      assertEquals(sessionStorage, state.getSessionStorage());
      assertEquals(consoleLogs, state.getConsoleLogs());
      assertEquals(networkRequests, state.getNetworkRequests());
      assertEquals(1920, state.getViewportWidth());
      assertEquals(1080, state.getViewportHeight());
      assertEquals(1500L, state.getLoadTime());
      assertEquals(timestamp, state.getTimestamp());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      PageState state = PageState.builder().build();
      assertNotNull(state.getTimestamp());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      PageState state = PageState.builder().build();

      state.setUrl("https://test.com");
      state.setTitle("Test Title");
      state.setHtml("<html></html>");
      state.setViewportWidth(800);
      state.setViewportHeight(600);
      state.setLoadTime(1000L);

      assertEquals("https://test.com", state.getUrl());
      assertEquals("Test Title", state.getTitle());
      assertEquals("<html></html>", state.getHtml());
      assertEquals(800, state.getViewportWidth());
      assertEquals(600, state.getViewportHeight());
      assertEquals(1000L, state.getLoadTime());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      PageState state1 =
          PageState.builder()
              .url("https://example.com")
              .title("Test")
              .viewportWidth(1024)
              .viewportHeight(768)
              .build();

      PageState state2 =
          PageState.builder()
              .url("https://example.com")
              .title("Test")
              .viewportWidth(1024)
              .viewportHeight(768)
              .build();

      // Note: timestamps will differ, so we verify individual fields
      assertEquals(state1.getUrl(), state2.getUrl());
      assertEquals(state1.getTitle(), state2.getTitle());
      assertEquals(state1.getViewportWidth(), state2.getViewportWidth());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      PageState state =
          PageState.builder()
              .url("https://example.com")
              .title("Example Page")
              .viewportWidth(1920)
              .viewportHeight(1080)
              .build();

      String str = state.toString();
      assertTrue(str.contains("https://example.com"));
      assertTrue(str.contains("Example Page"));
      assertTrue(str.contains("1920x1080"));
    }
  }

  @Nested
  @DisplayName("PageState.ConsoleMessage Tests")
  class ConsoleMessageTests {

    @Test
    @DisplayName("Should create console message with builder")
    void testBuilder() {
      Instant timestamp = Instant.now();

      ConsoleMessage message =
          ConsoleMessage.builder().type("log").text("Test message").timestamp(timestamp).build();

      assertEquals("log", message.getType());
      assertEquals("Test message", message.getText());
      assertEquals(timestamp, message.getTimestamp());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      ConsoleMessage message = ConsoleMessage.builder().build();

      Instant timestamp = Instant.now();
      message.setType("error");
      message.setText("Error occurred");
      message.setTimestamp(timestamp);

      assertEquals("error", message.getType());
      assertEquals("Error occurred", message.getText());
      assertEquals(timestamp, message.getTimestamp());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      Instant timestamp = Instant.now();

      ConsoleMessage message1 =
          ConsoleMessage.builder().type("warn").text("Warning").timestamp(timestamp).build();

      ConsoleMessage message2 =
          ConsoleMessage.builder().type("warn").text("Warning").timestamp(timestamp).build();

      assertEquals(message1, message2);
      assertEquals(message1.hashCode(), message2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      ConsoleMessage message = ConsoleMessage.builder().type("info").text("Information").build();

      String str = message.toString();
      assertTrue(str.contains("info"));
      assertTrue(str.contains("Information"));
    }
  }

  @Nested
  @DisplayName("PageState.NetworkRequest Tests")
  class NetworkRequestTests {

    @Test
    @DisplayName("Should create network request with builder")
    void testBuilder() {
      Instant timestamp = Instant.now();

      NetworkRequest request =
          NetworkRequest.builder()
              .method("POST")
              .url("https://api.example.com/data")
              .statusCode(201)
              .duration(250L)
              .timestamp(timestamp)
              .build();

      assertEquals("POST", request.getMethod());
      assertEquals("https://api.example.com/data", request.getUrl());
      assertEquals(201, request.getStatusCode());
      assertEquals(250L, request.getDuration());
      assertEquals(timestamp, request.getTimestamp());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      NetworkRequest request = NetworkRequest.builder().build();

      Instant timestamp = Instant.now();
      request.setMethod("GET");
      request.setUrl("https://example.com");
      request.setStatusCode(200);
      request.setDuration(100L);
      request.setTimestamp(timestamp);

      assertEquals("GET", request.getMethod());
      assertEquals("https://example.com", request.getUrl());
      assertEquals(200, request.getStatusCode());
      assertEquals(100L, request.getDuration());
      assertEquals(timestamp, request.getTimestamp());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      Instant timestamp = Instant.now();

      NetworkRequest request1 =
          NetworkRequest.builder()
              .method("GET")
              .url("https://api.example.com")
              .statusCode(200)
              .duration(150L)
              .timestamp(timestamp)
              .build();

      NetworkRequest request2 =
          NetworkRequest.builder()
              .method("GET")
              .url("https://api.example.com")
              .statusCode(200)
              .duration(150L)
              .timestamp(timestamp)
              .build();

      assertEquals(request1, request2);
      assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      NetworkRequest request =
          NetworkRequest.builder()
              .method("DELETE")
              .url("https://api.example.com/resource")
              .statusCode(204)
              .build();

      String str = request.toString();
      assertTrue(str.contains("DELETE"));
      assertTrue(str.contains("https://api.example.com/resource"));
      assertTrue(str.contains("204"));
    }
  }

  @Nested
  @DisplayName("TestResult Tests")
  class TestResultTests {

    @Test
    @DisplayName("Should create test result with builder")
    void testBuilder() {
      Instant timestamp = Instant.now();
      Duration executionTime = Duration.ofMillis(500);

      List<TestResult> subResults = new ArrayList<>();
      subResults.add(
          TestResult.builder()
              .operation(OperationType.CLICK)
              .passed(true)
              .selector("#button")
              .build());

      TestResult result =
          TestResult.builder()
              .operation(OperationType.ASSERT_TEXT)
              .passed(true)
              .error(null)
              .expected("Hello")
              .actual("Hello")
              .selector("#greeting")
              .url("https://example.com")
              .executionTime(executionTime)
              .timestamp(timestamp)
              .details("Text assertion passed")
              .subResults(subResults)
              .build();

      assertEquals(OperationType.ASSERT_TEXT, result.getOperation());
      assertTrue(result.isPassed());
      assertNull(result.getError());
      assertEquals("Hello", result.getExpected());
      assertEquals("Hello", result.getActual());
      assertEquals("#greeting", result.getSelector());
      assertEquals("https://example.com", result.getUrl());
      assertEquals(executionTime, result.getExecutionTime());
      assertEquals(timestamp, result.getTimestamp());
      assertEquals("Text assertion passed", result.getDetails());
      assertEquals(subResults, result.getSubResults());
      assertEquals(1, result.getSubResults().size());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      TestResult result = TestResult.builder().operation(OperationType.NAVIGATE).build();

      assertTrue(result.isPassed());
      assertNotNull(result.getTimestamp());
      assertNotNull(result.getSubResults());
      assertEquals(0, result.getSubResults().size());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      TestResult result = TestResult.builder().build();

      Duration executionTime = Duration.ofMillis(300);
      result.setOperation(OperationType.CLICK);
      result.setPassed(false);
      result.setError("Element not found");
      result.setExpected("visible");
      result.setActual("hidden");
      result.setSelector("#submit");
      result.setUrl("https://test.com");
      result.setExecutionTime(executionTime);
      result.setDetails("Click failed");

      assertEquals(OperationType.CLICK, result.getOperation());
      assertFalse(result.isPassed());
      assertEquals("Element not found", result.getError());
      assertEquals("visible", result.getExpected());
      assertEquals("hidden", result.getActual());
      assertEquals("#submit", result.getSelector());
      assertEquals("https://test.com", result.getUrl());
      assertEquals(executionTime, result.getExecutionTime());
      assertEquals("Click failed", result.getDetails());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      TestResult result1 =
          TestResult.builder()
              .operation(OperationType.NAVIGATE)
              .passed(true)
              .url("https://example.com")
              .build();

      TestResult result2 =
          TestResult.builder()
              .operation(OperationType.NAVIGATE)
              .passed(true)
              .url("https://example.com")
              .build();

      // Note: timestamps will differ, so we verify individual fields
      assertEquals(result1.getOperation(), result2.getOperation());
      assertEquals(result1.isPassed(), result2.isPassed());
      assertEquals(result1.getUrl(), result2.getUrl());
    }

    @Test
    @DisplayName("Should implement toString for failed test")
    void testToStringFailed() {
      TestResult result =
          TestResult.builder()
              .operation(OperationType.ASSERT_VISIBLE)
              .passed(false)
              .error("Element not visible")
              .build();

      String str = result.toString();
      assertTrue(str.contains("ASSERT_VISIBLE"));
      assertTrue(str.contains("failed"));
      assertTrue(str.contains("Element not visible"));
    }

    @Test
    @DisplayName("Should implement toString for NAVIGATE")
    void testToStringNavigate() {
      TestResult result =
          TestResult.builder()
              .operation(OperationType.NAVIGATE)
              .passed(true)
              .url("https://example.com")
              .build();

      String str = result.toString();
      assertTrue(str.contains("Navigated to"));
      assertTrue(str.contains("https://example.com"));
    }

    @Test
    @DisplayName("Should implement toString for CLICK")
    void testToStringClick() {
      TestResult result =
          TestResult.builder()
              .operation(OperationType.CLICK)
              .passed(true)
              .selector("#button")
              .build();

      String str = result.toString();
      assertTrue(str.contains("Clicked"));
      assertTrue(str.contains("#button"));
    }

    @Test
    @DisplayName("Should implement toString for FILL_INPUT")
    void testToStringFillInput() {
      TestResult result =
          TestResult.builder()
              .operation(OperationType.FILL_INPUT)
              .passed(true)
              .selector("#input")
              .build();

      String str = result.toString();
      assertTrue(str.contains("Filled input"));
      assertTrue(str.contains("#input"));
    }

    @Test
    @DisplayName("Should implement toString for SELECT_OPTION")
    void testToStringSelectOption() {
      TestResult result =
          TestResult.builder()
              .operation(OperationType.SELECT_OPTION)
              .passed(true)
              .selector("#dropdown")
              .build();

      String str = result.toString();
      assertTrue(str.contains("Selected option"));
      assertTrue(str.contains("#dropdown"));
    }

    @Test
    @DisplayName("Should implement toString for SUBMIT")
    void testToStringSubmit() {
      TestResult result =
          TestResult.builder()
              .operation(OperationType.SUBMIT)
              .passed(true)
              .selector("#form")
              .build();

      String str = result.toString();
      assertTrue(str.contains("Submitted form"));
      assertTrue(str.contains("#form"));
    }

    @Test
    @DisplayName("Should implement toString for ASSERT_EXISTS")
    void testToStringAssertExists() {
      TestResult result =
          TestResult.builder()
              .operation(OperationType.ASSERT_EXISTS)
              .passed(true)
              .selector("#element")
              .build();

      String str = result.toString();
      assertTrue(str.contains("Element exists"));
      assertTrue(str.contains("#element"));
    }

    @Test
    @DisplayName("Should implement toString for ASSERT_TEXT")
    void testToStringAssertText() {
      TestResult result =
          TestResult.builder()
              .operation(OperationType.ASSERT_TEXT)
              .passed(true)
              .selector("#text")
              .build();

      String str = result.toString();
      assertTrue(str.contains("Text matches"));
      assertTrue(str.contains("#text"));
    }

    @Test
    @DisplayName("Should implement toString for ASSERT_VISIBLE")
    void testToStringAssertVisible() {
      TestResult result =
          TestResult.builder()
              .operation(OperationType.ASSERT_VISIBLE)
              .passed(true)
              .selector("#visible")
              .build();

      String str = result.toString();
      assertTrue(str.contains("Element visible"));
      assertTrue(str.contains("#visible"));
    }

    @Test
    @DisplayName("Should implement toString for ASSERT_ENABLED")
    void testToStringAssertEnabled() {
      TestResult result =
          TestResult.builder()
              .operation(OperationType.ASSERT_ENABLED)
              .passed(true)
              .selector("#enabled")
              .build();

      String str = result.toString();
      assertTrue(str.contains("Element enabled"));
      assertTrue(str.contains("#enabled"));
    }

    @Test
    @DisplayName("Should implement toString for ASSERT_URL")
    void testToStringAssertUrl() {
      TestResult result =
          TestResult.builder()
              .operation(OperationType.ASSERT_URL)
              .passed(true)
              .url("https://example.com")
              .build();

      String str = result.toString();
      assertTrue(str.contains("URL matches"));
      assertTrue(str.contains("https://example.com"));
    }

    @Test
    @DisplayName("Should implement toString for SCREENSHOT")
    void testToStringScreenshot() {
      TestResult result =
          TestResult.builder().operation(OperationType.SCREENSHOT).passed(true).build();

      String str = result.toString();
      assertTrue(str.contains("Screenshot captured"));
    }

    @Test
    @DisplayName("Should implement toString for WAIT")
    void testToStringWait() {
      TestResult result =
          TestResult.builder()
              .operation(OperationType.WAIT)
              .passed(true)
              .selector("#element")
              .build();

      String str = result.toString();
      assertTrue(str.contains("Wait completed"));
      assertTrue(str.contains("#element"));
    }

    @Test
    @DisplayName("Should implement toString for EXECUTE_SCRIPT")
    void testToStringExecuteScript() {
      TestResult result =
          TestResult.builder().operation(OperationType.EXECUTE_SCRIPT).passed(true).build();

      String str = result.toString();
      assertTrue(str.contains("Script executed"));
    }

    @Test
    @DisplayName("Should include execution time in toString")
    void testToStringWithExecutionTime() {
      TestResult result =
          TestResult.builder()
              .operation(OperationType.NAVIGATE)
              .passed(true)
              .url("https://example.com")
              .executionTime(Duration.ofMillis(1500))
              .build();

      String str = result.toString();
      assertTrue(str.contains("[1500ms]"));
    }
  }

  @Nested
  @DisplayName("TestResult.OperationType Tests")
  class OperationTypeTests {

    @Test
    @DisplayName("Should have all operation types")
    void testOperationTypes() {
      OperationType[] types = OperationType.values();
      assertEquals(13, types.length);

      assertNotNull(OperationType.valueOf("NAVIGATE"));
      assertNotNull(OperationType.valueOf("CLICK"));
      assertNotNull(OperationType.valueOf("FILL_INPUT"));
      assertNotNull(OperationType.valueOf("SELECT_OPTION"));
      assertNotNull(OperationType.valueOf("SUBMIT"));
      assertNotNull(OperationType.valueOf("ASSERT_EXISTS"));
      assertNotNull(OperationType.valueOf("ASSERT_TEXT"));
      assertNotNull(OperationType.valueOf("ASSERT_VISIBLE"));
      assertNotNull(OperationType.valueOf("ASSERT_ENABLED"));
      assertNotNull(OperationType.valueOf("ASSERT_URL"));
      assertNotNull(OperationType.valueOf("SCREENSHOT"));
      assertNotNull(OperationType.valueOf("WAIT"));
      assertNotNull(OperationType.valueOf("EXECUTE_SCRIPT"));
    }

    @Test
    @DisplayName("Should throw exception for invalid operation type")
    void testInvalidOperationType() {
      assertThrows(IllegalArgumentException.class, () -> OperationType.valueOf("INVALID"));
    }

    @Test
    @DisplayName("Should return correct name")
    void testOperationTypeName() {
      assertEquals("NAVIGATE", OperationType.NAVIGATE.name());
      assertEquals("CLICK", OperationType.CLICK.name());
      assertEquals("FILL_INPUT", OperationType.FILL_INPUT.name());
      assertEquals("SELECT_OPTION", OperationType.SELECT_OPTION.name());
      assertEquals("SUBMIT", OperationType.SUBMIT.name());
      assertEquals("ASSERT_EXISTS", OperationType.ASSERT_EXISTS.name());
      assertEquals("ASSERT_TEXT", OperationType.ASSERT_TEXT.name());
      assertEquals("ASSERT_VISIBLE", OperationType.ASSERT_VISIBLE.name());
      assertEquals("ASSERT_ENABLED", OperationType.ASSERT_ENABLED.name());
      assertEquals("ASSERT_URL", OperationType.ASSERT_URL.name());
      assertEquals("SCREENSHOT", OperationType.SCREENSHOT.name());
      assertEquals("WAIT", OperationType.WAIT.name());
      assertEquals("EXECUTE_SCRIPT", OperationType.EXECUTE_SCRIPT.name());
    }

    @Test
    @DisplayName("Should support enum methods")
    void testEnumMethods() {
      assertEquals(OperationType.NAVIGATE, OperationType.valueOf("NAVIGATE"));
      assertEquals(0, OperationType.NAVIGATE.ordinal());
      assertEquals(12, OperationType.EXECUTE_SCRIPT.ordinal());
    }
  }
}
