# Web Test Tool

**Version:** 0.1.0
**Category:** Web Testing Tools
**Status:** Architecture Implemented (Provider Stubs)
**Date:** 2025-10-25

---

## TL;DR

**Tool Provider for web application testing with pluggable backends**. Uses provider/service pattern supporting Playwright (modern), Selenium (compatible), and HtmlUnit (lightweight). **Benefits**: Choose best tool for each test scenario, switch providers at runtime, consistent API across all backends. **Use cases**: E2E testing, form validation, accessibility scanning, visual regression, performance testing.

**Quick start:**

```java
@Inject
private WebTestTool webTest;

// Use Playwright (default, modern)
webTest.navigateTo("https://example.com").block();
webTest.click("#login").block();
webTest.fillInput("#username", "test").block();

// Switch to Selenium for cross-browser
webTest.setConfig(WebTestConfig.selenium());

// Or HtmlUnit for speed
webTest.setConfig(WebTestConfig.lightweight());
```

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Overview](#overview)
- [Provider/Service Pattern](#providerservice-pattern)
- [Providers](#providers)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Operations](#operations)
- [Use Cases](#use-cases)
- [Implementation Status](#implementation-status)

---

## Prerequisites

**Status:** ⚠️ Architecture implemented, provider dependencies required

The Web Test Tool architecture is complete with six provider options, but **you must add dependencies for the provider(s) you want to use**. Currently, all providers are stub implementations awaiting their respective dependencies.

### Core Framework Requirements (Already Satisfied)

These are satisfied by the Adentic framework:

- ✅ **Java 21** - Language requirement
- ✅ **Project Reactor** - For reactive Mono/Flux API (provided by adentic-core)
- ✅ **SLF4J + Logback** - For logging (provided by adentic-boot)

### Provider-Specific Dependencies

Choose one or more providers to implement:

#### Option 1: Playwright (Recommended) ⭐

**Status:** Stub - requires dependency

**Best for:** Modern web apps, E2E testing, reliable automation

**Add to pom.xml:**

```xml
<dependency>
    <groupId>com.microsoft.playwright</groupId>
    <artifactId>playwright</artifactId>
    <version>1.40.0</version>
</dependency>
```

**System Requirements:**
- Node.js (for Playwright browser installation)
- First run: `mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"`

**Features:**
- ✅ Auto-wait (no flaky tests)
- ✅ Multi-browser (Chromium, Firefox, WebKit)
- ✅ Network interception
- ✅ Screenshot and video capture

---

#### Option 2: Selenium WebDriver

**Status:** Stub - requires dependency

**Best for:** Cross-browser testing, legacy apps, wide compatibility

**Add to pom.xml:**

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.15.0</version>
</dependency>
```

**System Requirements:**
- Browser drivers (ChromeDriver, GeckoDriver, etc.) in PATH
- Or use WebDriverManager for automatic driver management:

```xml
<dependency>
    <groupId>io.github.bonigarcia</groupId>
    <artifactId>webdrivermanager</artifactId>
    <version>5.6.2</version>
</dependency>
```

**Features:**
- ✅ Most mature web testing library
- ✅ Supports all major browsers
- ✅ Large ecosystem and community

---

#### Option 3: HtmlUnit (Lightweight)

**Status:** Stub - requires dependency

**Best for:** Fast tests, simple forms, API-like testing

**Add to pom.xml:**

```xml
<dependency>
    <groupId>net.sourceforge.htmlunit</groupId>
    <artifactId>htmlunit</artifactId>
    <version>3.8.0</version>
</dependency>
```

**System Requirements:**
- None - pure Java, no browser needed

**Features:**
- ✅ Extremely fast (no real browser)
- ✅ Zero external dependencies
- ⚠️ Limited JavaScript support

---

#### Option 4: Puppeteer (Chrome DevTools Protocol)

**Status:** Stub - requires Java-Node.js bridge

**Best for:** CDP features, PDF generation, performance analysis

**Implementation Options:**

**Option A: CDP4J (Recommended)**

```xml
<dependency>
    <groupId>com.github.kklisura.cdt</groupId>
    <artifactId>cdt-java-client</artifactId>
    <version>4.0.0</version>
</dependency>
```

**Option B: Node.js REST Wrapper**
- Create a Node.js service that wraps Puppeteer
- Expose REST API for Java to call
- Requires Node.js runtime

**Option C: GraalVM Polyglot**
- Use GraalVM to run Node.js code from Java
- Requires GraalVM distribution

**System Requirements:**
- Option A: Chrome/Chromium browser installed
- Option B: Node.js + Puppeteer npm package
- Option C: GraalVM with Node.js support

---

#### Option 5: Cypress (JavaScript E2E Framework)

**Status:** Stub - requires Node.js REST wrapper

**Best for:** Modern E2E testing, time-travel debugging

**Implementation Approach:**

Create a Node.js REST service wrapper:

1. **Install Cypress:**

   ```bash
   npm install cypress
   ```
2. **Create REST wrapper:**

   ```javascript
   // server.js
   const express = require('express');
   const cypress = require('cypress');
   const app = express();

   app.post('/api/cypress/visit', (req, res) => {
     // Cypress command wrapper
   });

   app.listen(3000);
   ```
3. **Configure Java to call REST API**

**System Requirements:**
- Node.js runtime
- Cypress npm package
- Express for REST server

---

#### Option 6: Custom Provider

**Status:** ✅ Abstract base class ready

**Best for:** Proprietary tools, specialized frameworks, custom integrations

**Implementation:**

Extend `CustomWebTestProvider`:

```java
public class MyTestProvider extends CustomWebTestProvider {

    public MyTestProvider(WebTestConfig config) {
        super(config);
    }

    @Override
    public Mono<Void> start() {
        // Initialize your testing tool
    }

    @Override
    public Mono<TestResult> navigateTo(String url) {
        // Implement navigation
    }

    // Implement other 40+ methods...
}
```

**System Requirements:**
- Depends on your testing tool

---

### Quick Start Guide

1. **Choose a provider** (Playwright recommended for most use cases)

2. **Add Maven dependency** to `pom.xml`

3. **Install system requirements** (browsers, drivers, etc.)

4. **Test the integration:**

```java
@Inject
private WebTestTool webTest;

public void testProvider() {
    // Set configuration for your provider
    webTest.setConfig(WebTestConfig.playwright());

    // Test basic operation
    TestResult result = webTest.navigateTo("https://example.com").block();
    System.out.println("Provider working: " + result.isPassed());
}
```

### Provider Comparison

|    Provider    | Dependencies |    System Req     | Complexity |    Recommended     |
|----------------|--------------|-------------------|------------|--------------------|
| **Playwright** | 1 Maven dep  | Node.js (setup)   | Low        | ⭐ Yes              |
| **Selenium**   | 1 Maven dep  | Browser drivers   | Low        | ✅ Yes              |
| **HtmlUnit**   | 1 Maven dep  | None              | Very Low   | ✅ For simple tests |
| **Puppeteer**  | 1+ Maven dep | Chrome + Bridge   | Medium     | ⚠️ Advanced use    |
| **Cypress**    | REST wrapper | Node.js + service | High       | ⚠️ Advanced use    |
| **Custom**     | Your choice  | Depends           | Varies     | For custom tools   |

### Troubleshooting

**"No provider implementation found"**
- Add the Maven dependency for your chosen provider
- Run `mvn clean install` to download dependencies

**"Browser not found" (Playwright)**
- Run: `mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"`

**"WebDriver not found" (Selenium)**
- Install browser drivers or add WebDriverManager dependency

**Provider stub returns placeholder results:**
- This is expected - implement the provider or choose a different one

---

## Overview

The Web Test Tool is a **Tool Provider** implementation using a **provider/service pattern** that enables AI agents to perform web application testing with multiple backend providers.

**Purpose:** Provide flexible, powerful web testing capabilities:
- Automate browser interactions
- Verify web application behavior
- Capture screenshots and state
- Test across different browsers
- Choose optimal provider per scenario
- Extend with custom implementations

**Key Benefits:**
- ✅ **Provider/Service Pattern** - Pluggable backends
- ✅ **Six Providers** - Playwright, Selenium, HtmlUnit, Puppeteer, Cypress, Custom
- ✅ **Runtime Switching** - Change provider dynamically
- ✅ **Consistent API** - Same interface across all providers
- ✅ **Async/Reactive** - Mono-based API
- ✅ **Agent-Ready** - `@Tool` annotation
- ✅ **Security Controls** - Domain whitelist/blacklist
- ✅ **Configuration Presets** - Quick setup for common scenarios
- ✅ **Extensible** - Create custom provider implementations

---

## Provider/Service Pattern

### Architecture

```
WebTestTool (@Tool)
    │
    ├─→ WebTestProvider (Interface)
    │       ├─→ PlaywrightWebTestProvider
    │       ├─→ SeleniumWebTestProvider
    │       ├─→ HtmlUnitWebTestProvider
    │       ├─→ PuppeteerWebTestProvider
    │       ├─→ CypressWebTestProvider
    │       └─→ CustomWebTestProvider (Abstract Base)
    │
    └─→ WebTestConfig (Provider Selection)
```

### How It Works

**1. Unified Interface:**

```java
public interface WebTestProvider {
    Mono<TestResult> navigateTo(String url);
    Mono<TestResult> click(String selector);
    Mono<TestResult> fillInput(String selector, String text);
    Mono<Screenshot> takeScreenshot();
    // ... 40+ methods
}
```

**2. Provider Implementations:**
- Each provider implements the same interface
- Provider-specific logic encapsulated
- Consistent behavior across all providers

**3. Dynamic Provider Selection:**

```java
WebTestConfig config = WebTestConfig.builder()
    .provider(TestProvider.PLAYWRIGHT)  // Choose provider
    .build();

webTest.setConfig(config);  // Switch provider at runtime
```

### Benefits of This Pattern

✅ **Flexibility** - Choose best provider for each test
✅ **Extensibility** - Add new providers without changing API
✅ **Consistency** - Same API regardless of backend
✅ **Future-Proof** - Support new tools as they emerge
✅ **Framework Pattern** - Matches TextGenerationProvider design

---

## Providers

### Playwright (Recommended Default)

**Technology:** Microsoft Playwright
**Status:** Stub Implementation (TODO: Add Playwright dependency)

**Strengths:**
- ⚡ Fast and reliable
- ✅ Auto-wait capabilities (no flaky tests)
- ✅ Network interception
- ✅ Multi-browser (Chromium, Firefox, WebKit)
- ✅ Modern web app support
- ✅ Screenshot and video capture

**Best For:**
- Modern web applications
- E2E testing
- Reliable automated tests
- Production test suites

**Configuration:**

```java
webTest.setConfig(WebTestConfig.playwright());
```

**Dependencies (TODO):**

```xml
<dependency>
    <groupId>com.microsoft.playwright</groupId>
    <artifactId>playwright</artifactId>
    <version>1.40.0</version>
</dependency>
```

---

### Selenium (Cross-Browser)

**Technology:** Selenium WebDriver
**Status:** Stub Implementation (TODO: Add Selenium dependency)

**Strengths:**
- ✅ Most mature and widely used
- ✅ Supports all major browsers
- ✅ Large ecosystem and community
- ✅ Extensive documentation
- ✅ Grid support for parallel testing

**Best For:**
- Cross-browser compatibility testing
- Legacy application testing
- When wide browser support needed
- Teams familiar with Selenium

**Configuration:**

```java
webTest.setConfig(WebTestConfig.selenium());

// Or specific browser
webTest.setConfig(WebTestConfig.builder()
    .provider(TestProvider.SELENIUM)
    .browser(BrowserType.FIREFOX)
    .build());
```

**Dependencies (TODO):**

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.15.0</version>
</dependency>
```

---

### HtmlUnit (Lightweight)

**Technology:** HtmlUnit Headless Browser
**Status:** Stub Implementation (TODO: Add HtmlUnit dependency)

**Strengths:**
- ⚡⚡⚡ Extremely fast (no real browser)
- ✅ Lightweight (no browser overhead)
- ✅ No dependencies (pure Java)
- ✅ Good for simple tests
- ⚠️ Limited JavaScript support

**Best For:**
- Fast smoke tests
- Simple form submissions
- API-like testing
- CI/CD quick checks
- Resource-constrained environments

**Limitations:**
- No screenshot support
- Limited JavaScript rendering
- Not suitable for complex SPAs

**Configuration:**

```java
webTest.setConfig(WebTestConfig.lightweight());
```

**Dependencies (TODO):**

```xml
<dependency>
    <groupId>net.sourceforge.htmlunit</groupId>
    <artifactId>htmlunit</artifactId>
    <version>3.8.0</version>
</dependency>
```

---

### Puppeteer (Chrome DevTools Protocol)

**Technology:** Google's Puppeteer (Node.js)
**Status:** Stub Implementation (TODO: Add Java-Node.js bridge or CDP client)

**Strengths:**
- ✅ Chrome DevTools Protocol access
- ✅ PDF generation from pages
- ✅ Performance analysis and metrics
- ✅ Network interception and mocking
- ✅ Screenshot and video capture
- ✅ Lighthouse integration

**Best For:**
- Chrome DevTools Protocol features
- PDF generation
- Performance testing
- Network analysis
- Advanced Chrome automation

**Implementation Options:**
1. **CDP4J** - Chrome DevTools Protocol for Java
2. **Node.js REST wrapper** - HTTP service wrapping Puppeteer
3. **GraalVM polyglot** - Run Node.js code from Java

**Configuration:**

```java
webTest.setConfig(WebTestConfig.puppeteer());
```

**Dependencies (TODO):**

```xml
<!-- Option 1: CDP4J -->
<dependency>
    <groupId>com.github.kklisura.cdt</groupId>
    <artifactId>cdt-java-client</artifactId>
    <version>4.0.0</version>
</dependency>

<!-- OR Option 2: Create Node.js REST wrapper -->
<!-- OR Option 3: GraalVM with Node.js support -->
```

---

### Cypress (JavaScript E2E Framework)

**Technology:** Cypress.io (JavaScript/Node.js)
**Status:** Stub Implementation (TODO: Add REST wrapper or GraalVM bridge)

**Strengths:**
- ✅ Developer-friendly API and tooling
- ✅ Time-travel debugging with snapshots
- ✅ Automatic waiting and retry logic
- ✅ Real-time reloading
- ✅ Network stubbing and mocking
- ✅ Screenshot and video recording

**Best For:**
- Modern E2E testing
- Developer-friendly workflows
- Time-travel debugging
- Network mocking
- Component testing

**Implementation Approach:**
Create lightweight Node.js REST service wrapping Cypress commands:

```javascript
// Node.js service
app.post('/api/cypress/visit', (req, res) => {
  cy.visit(req.body.url);
  res.json({ success: true });
});
```

**Configuration:**

```java
webTest.setConfig(WebTestConfig.cypress());
```

**Dependencies (TODO):**
- Node.js REST service wrapping Cypress API
- Or GraalVM polyglot for Node.js integration

---

### Custom (User Implementations)

**Technology:** User-provided
**Status:** Abstract base class ready for extension

**Use Cases:**
- Proprietary or internal testing tools
- Custom browser automation solutions
- Specialized testing frameworks
- Integration with existing test infrastructure
- Testing tools not yet supported by framework

**Implementation:**

```java
public class MyCustomTestProvider extends CustomWebTestProvider {

    private MyTestingTool tool;

    public MyCustomTestProvider(WebTestConfig config) {
        super(config);
    }

    @Override
    public Mono<Void> start() {
        return Mono.fromRunnable(() -> {
            tool = new MyTestingTool();
            tool.initialize(config);
            running = true;
        });
    }

    @Override
    public Mono<TestResult> navigateTo(String url) {
        return Mono.fromCallable(() -> {
            validateUrl(url);
            tool.navigate(url);
            return createSuccessResult(OperationType.NAVIGATE, "Navigated: " + url);
        });
    }

    // Implement other methods...
}

// Usage
WebTestTool webTest = new WebTestTool();
webTest.setProvider(new MyCustomTestProvider(config));
```

**Benefits:**
- ✅ Full control over implementation
- ✅ Access to base provider utilities (validation, logging, result creation)
- ✅ Consistent API with other providers
- ✅ Automatic integration with WebTestTool

---

## Quick Start

### 1. Inject the Tool

```java
import dev.adeengineer.adentic.tool.webtest.WebTestTool;

@Component
public class MyTestService {

    @Inject
    private WebTestTool webTest;
}
```

### 2. Basic Test

```java
// Default provider (Playwright)
webTest.navigateTo("https://example.com").block();
webTest.click("#login-button").block();
webTest.fillInput("#username", "testuser").block();
webTest.fillInput("#password", "password123").block();
webTest.submit("#login-form").block();

// Verify
TestResult result = webTest.assertTextContains("h1", "Welcome").block();
assert result.isPassed();
```

### 3. Switch Providers

```java
// Use Playwright for modern app
webTest.setConfig(WebTestConfig.playwright());
webTest.navigateTo("https://modern-spa.com").block();

// Switch to Selenium for cross-browser
webTest.setConfig(WebTestConfig.selenium());
webTest.navigateTo("https://legacy-app.com").block();

// Switch to HtmlUnit for speed
webTest.setConfig(WebTestConfig.lightweight());
webTest.navigateTo("https://simple-form.com").block();
```

### 4. Configuration Presets

```java
// Default (Playwright, headless, 1280x720)
webTest.setConfig(WebTestConfig.defaults());

// Visible browser (debugging)
webTest.setConfig(WebTestConfig.visible());

// Debug mode (slow, logs, network capture)
webTest.setConfig(WebTestConfig.debug());

// Mobile viewport
webTest.setConfig(WebTestConfig.mobile());

// Desktop viewport
webTest.setConfig(WebTestConfig.desktop());

// Fast (HtmlUnit, 10s timeout)
webTest.setConfig(WebTestConfig.fast());

// Cross-browser (Selenium, Firefox)
webTest.setConfig(WebTestConfig.crossBrowser());
```

---

## Configuration

### Provider Selection

```java
WebTestConfig config = WebTestConfig.builder()
    .provider(TestProvider.PLAYWRIGHT)  // or SELENIUM, HTMLUNIT
    .browser(BrowserType.CHROMIUM)      // CHROMIUM, FIREFOX, WEBKIT, CHROME, EDGE
    .headless(true)
    .timeout(30000)
    .viewportWidth(1280)
    .viewportHeight(720)
    .build();

webTest.setConfig(config);
```

### Security Configuration

```java
Set<String> allowedDomains = Set.of("example.com", "test.com");
Set<String> blockedDomains = Set.of("malicious.com");

WebTestConfig config = WebTestConfig.builder()
    .allowedDomains(allowedDomains)
    .blockedDomains(blockedDomains)
    .acceptInsecureCerts(false)
    .build();
```

### Performance Configuration

```java
WebTestConfig config = WebTestConfig.builder()
    .headless(true)           // Faster
    .imagesEnabled(false)     // Skip image loading
    .javascriptEnabled(true)
    .slowMo(0)                // No delay
    .build();
```

---

## Operations

### Navigation

- `navigateTo(url)` - Go to URL
- `goBack()` - Browser back
- `goForward()` - Browser forward
- `refresh()` - Reload page

### Element Interaction

- `click(selector)` - Click element
- `doubleClick(selector)` - Double-click
- `rightClick(selector)` - Right-click
- `fillInput(selector, text)` - Type text
- `selectOption(selector, value)` - Select dropdown
- `check(selector)` - Check checkbox
- `hover(selector)` - Hover over element
- `submit(selector)` - Submit form

### Queries

- `getText(selector)` - Get element text
- `getAttribute(selector, attr)` - Get attribute
- `getElementInfo(selector)` - Full element info
- `findElements(selector)` - Find all matching

### Assertions

- `assertElementExists(selector)`
- `assertElementVisible(selector)`
- `assertElementEnabled(selector)`
- `assertTextContains(selector, text)`
- `assertTextEquals(selector, text)`
- `assertUrlMatches(pattern)`
- `assertTitleContains(text)`

### Waiting

- `waitForElement(selector)` - Wait for existence
- `waitForVisible(selector)` - Wait for visibility
- `waitForEnabled(selector)` - Wait for enabled
- `wait(milliseconds)` - Fixed delay

### Screenshots

- `takeScreenshot()` - Full page
- `takeScreenshot(selector)` - Element only
- `saveScreenshot(filePath)` - Save to file

### Advanced

- `executeScript(script)` - Run JavaScript
- `getPageState()` - Get full page state
- `setCookie(name, value)` - Set cookie
- `getCookies()` - Get all cookies

---

## Use Cases

### 1. E2E Testing Agent

```java
@AgentService
public class E2ETestAgent {
    @Inject private WebTestTool webTest;

    public Mono<TestReport> testLoginFlow(String appUrl) {
        // Use Playwright for modern app
        webTest.setConfig(WebTestConfig.playwright());

        return webTest.navigateTo(appUrl)
            .then(webTest.click("#login"))
            .then(webTest.fillInput("#username", "test@example.com"))
            .then(webTest.fillInput("#password", "password"))
            .then(webTest.submit("#login-form"))
            .then(webTest.waitForElement(".dashboard"))
            .then(webTest.assertTextContains("h1", "Dashboard"))
            .map(result -> new TestReport(result.isPassed()));
    }
}
```

### 2. Cross-Browser Testing Agent

```java
@AgentService
public class CrossBrowserAgent {
    @Inject private WebTestTool webTest;

    public Mono<List<TestResult>> testAcrossBrowsers(String url) {
        List<BrowserType> browsers = List.of(
            BrowserType.CHROME,
            BrowserType.FIREFOX,
            BrowserType.EDGE
        );

        return Flux.fromIterable(browsers)
            .flatMap(browser -> {
                webTest.setConfig(WebTestConfig.builder()
                    .provider(TestProvider.SELENIUM)
                    .browser(browser)
                    .build());

                return webTest.navigateTo(url)
                    .then(webTest.assertTitleContains("Example"));
            })
            .collectList();
    }
}
```

### 3. Fast Smoke Testing Agent

```java
@AgentService
public class SmokeTestAgent {
    @Inject private WebTestTool webTest;

    public Mono<Boolean> quickHealthCheck(List<String> urls) {
        // Use HtmlUnit for speed
        webTest.setConfig(WebTestConfig.fast());

        return Flux.fromIterable(urls)
            .flatMap(url -> webTest.navigateTo(url))
            .all(result -> result.isPassed());
    }
}
```

---

## Implementation Status

### ✅ Completed (Architecture)

- ✅ Provider/Service pattern architecture
- ✅ WebTestProvider interface (40+ methods)
- ✅ Base provider with common functionality
- ✅ Six provider stub implementations (Playwright, Selenium, HtmlUnit, Puppeteer, Cypress, Custom)
- ✅ WebTestConfig with 12+ presets
- ✅ WebTestTool main class with routing
- ✅ Models: TestResult, Screenshot, PageState, ElementInfo
- ✅ Security: Domain whitelist/blacklist
- ✅ Async/Reactive API (Mono-based)
- ✅ CustomWebTestProvider abstract base for user implementations

### 🚧 TODO (Full Implementation)

**PlaywrightWebTestProvider:**
- Add Playwright dependency
- Implement Browser, Page management
- Element locator strategies
- Screenshot capture
- Network interception
- Console log capture

**SeleniumWebTestProvider:**
- Add Selenium dependency
- WebDriver instance management
- WebElement location
- Explicit/implicit waits
- Screenshot capture

**HtmlUnitWebTestProvider:**
- Add HtmlUnit dependency
- WebClient instance management
- HtmlPage and HtmlElement handling
- Form submission

**PuppeteerWebTestProvider:**
- Choose implementation approach (CDP4J, REST wrapper, or GraalVM)
- Browser and Page management via CDP
- Network interception
- PDF generation
- Performance metrics capture

**CypressWebTestProvider:**
- Create Node.js REST wrapper service
- Map Cypress commands to REST endpoints
- Handle time-travel debugging features
- Network stubbing integration

**CustomWebTestProvider:**
- ✅ Abstract base class complete
- Documentation for custom implementations
- Example implementations

**Documentation:**
- architecture.md (design details)
- usage-guide.md (comprehensive examples)
- configuration.md (all configuration options)
- examples.md (real-world scenarios)

---

## Provider Comparison

|       Feature        |       Playwright        |   Selenium    |    HtmlUnit     |     Puppeteer      |        Cypress        |    Custom    |
|----------------------|-------------------------|---------------|-----------------|--------------------|-----------------------|--------------|
| **Speed**            | ⚡⚡⚡ Fast                | ⚡⚡ Moderate   | ⚡⚡⚡⚡ Very Fast  | ⚡⚡⚡ Fast           | ⚡⚡⚡ Fast              | Varies       |
| **Browser Support**  | Chrome, Firefox, WebKit | All major     | None (headless) | Chrome only        | Chrome, Firefox, Edge | Varies       |
| **JavaScript**       | ✅ Full                  | ✅ Full        | ⚠️ Limited      | ✅ Full             | ✅ Full                | Varies       |
| **Auto-Wait**        | ✅ Yes                   | ❌ No          | N/A             | ⚠️ Manual          | ✅ Yes                 | Varies       |
| **Network Control**  | ✅ Yes                   | ⚠️ Limited    | ❌ No            | ✅ Yes (CDP)        | ✅ Yes                 | Varies       |
| **Screenshots**      | ✅ Yes                   | ✅ Yes         | ❌ No            | ✅ Yes              | ✅ Yes                 | Varies       |
| **PDF Generation**   | ✅ Yes                   | ❌ No          | ❌ No            | ✅ Yes              | ❌ No                  | Varies       |
| **Time Travel**      | ❌ No                    | ❌ No          | N/A             | ❌ No               | ✅ Yes                 | Varies       |
| **Maturity**         | Modern                  | Very Mature   | Mature          | Mature             | Modern                | N/A          |
| **Java Integration** | ✅ Native                | ✅ Native      | ✅ Native        | ⚠️ Bridge required | ⚠️ Bridge required    | ✅ Custom     |
| **Best For**         | Modern apps, E2E        | Cross-browser | Fast tests      | CDP features, PDF  | E2E, debugging        | Custom needs |

---

## Dependencies (TODO)

To complete implementation, add dependencies for desired providers:

```xml
<!-- Playwright (Recommended) -->
<dependency>
    <groupId>com.microsoft.playwright</groupId>
    <artifactId>playwright</artifactId>
    <version>1.40.0</version>
</dependency>

<!-- Selenium -->
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.15.0</version>
</dependency>

<!-- HtmlUnit -->
<dependency>
    <groupId>net.sourceforge.htmlunit</groupId>
    <artifactId>htmlunit</artifactId>
    <version>3.8.0</version>
</dependency>

<!-- Puppeteer (Option 1: CDP4J) -->
<dependency>
    <groupId>com.github.kklisura.cdt</groupId>
    <artifactId>cdt-java-client</artifactId>
    <version>4.0.0</version>
</dependency>

<!-- Cypress: Requires Node.js REST wrapper service -->
<!-- Custom: Provide your own implementation -->
```

---

## Version History

| Version |    Date    |                         Changes                         |
|---------|------------|---------------------------------------------------------|
| 0.1.0   | 2025-10-25 | Initial architecture implementation                     |
|         |            | - Provider/Service pattern established                  |
|         |            | - Three provider stubs (Playwright, Selenium, HtmlUnit) |
|         |            | - Complete interface with 40+ methods                   |
|         |            | - Configuration with 10+ presets                        |
|         |            | - Models and security controls                          |
|         |            | - Full implementation TODO                              |

---

## License

Part of the Adentic Framework.
See main project [LICENSE](../../../LICENSE) for details.

---

*Last Updated: 2025-10-25*
*Version: 0.1.0*
*Status: Architecture Implemented (Provider Stubs)*
