# Web Test Tool - LLM Integration Guide

**Version:** 0.1.0
**Category:** Tool Integration
**Status:** Architecture Complete (Playwright Stub Implementation)
**Date:** 2025-10-25

---

## TL;DR

**Web Test Tool enables LLMs to automate browser testing and web interactions through natural language**. Agents analyze user requests, select appropriate browser operations (navigate, click, fill forms, assert, screenshot), extract parameters, and execute web automation safely. **Benefits**: Natural language → browser actions, multi-provider support (Playwright/Selenium/HtmlUnit/Puppeteer/Cypress), element interaction, assertions, screenshots. **Use cases**: E2E testing, web scraping, form automation, visual regression testing.

---

## Table of Contents

- [Overview](#overview)
- [Integration Architecture](#integration-architecture)
- [Tool Registration](#tool-registration)
- [LLM Workflow Examples](#llm-workflow-examples)
- [Tool Descriptor Format](#tool-descriptor-format)
- [Parameter Mapping](#parameter-mapping)
- [Error Handling](#error-handling)
- [Security Considerations](#security-considerations)
- [Use Cases](#use-cases)
- [Best Practices](#best-practices)
- [Complete Integration Example](#complete-integration-example)

---

## Overview

The Web Test Tool integrates with LLM-based agents to provide browser automation capabilities through natural language. The tool handles the complexity of browser interaction, element selection, and assertion verification while the LLM focuses on understanding user intent.

### Integration Flow

```
┌──────────────────────────────────────────────────────────────┐
│                         User Input                            │
│   "Go to Google and search for 'Adentic framework'"          │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                      LLM/AI Agent                             │
│  • Analyzes intent: "navigate and search"                    │
│  • Identifies steps: navigate → fill input → submit          │
│  • Selects tool: web-test                                    │
│  • Extracts parameters: url, selector, text                  │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                    WebTestTool                                │
│  • Method: navigateTo(url)                                   │
│  • Provider: PlaywrightWebTestProvider                       │
│  • Execution: Browser automation with smart waits            │
│  • Operations: Navigate, fill, submit                        │
│  • Browser: Chromium/Firefox/WebKit (configurable)           │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                     TestResult                                │
│  • passed: true                                              │
│  • operation: NAVIGATE                                       │
│  • url: "https://google.com"                                 │
│  • executionTime: 850ms                                      │
│  • details: "Page loaded successfully"                       │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                      LLM Response                             │
│  "I've navigated to Google and searched for 'Adentic         │
│   framework'. The search completed successfully in 2.3       │
│   seconds and returned 10 results."                          │
└──────────────────────────────────────────────────────────────┘
```

---

## Integration Architecture

### Component Roles

**1. LLM/AI Agent**
- Understands natural language test instructions
- Selects appropriate browser operations
- Extracts parameters (URL, selector, text, timeout)
- Formats results for user presentation
- Handles multi-step test workflows

**2. WebTestTool (@Tool)**
- Provides consistent API for browser automation
- Routes to appropriate provider (Playwright, Selenium, etc.)
- Returns structured results (TestResult, ElementInfo, Screenshot)

**3. WebTestProvider (Playwright, Selenium, HtmlUnit, etc.)**
- Executes browser operations with smart waiting
- Manages browser lifecycle and sessions
- Handles element interaction and assertions
- Captures screenshots and page state

**4. Result Models**
- **TestResult** - Operation results with pass/fail status
- **ElementInfo** - Element properties and state
- **Screenshot** - Screenshot data (base64/file path)
- **PageState** - Current page state (URL, title, cookies)

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│                      LLM/AI Agent                        │
│  • Natural language understanding                       │
│  • Intent classification                                │
│  • Parameter extraction                                 │
│  • Result synthesis                                     │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│              WebTestTool (@Tool)                         │
│  • navigateTo(url)                                      │
│  • click(selector)                                      │
│  • fillInput(selector, text)                            │
│  • assertElementVisible(selector)                       │
│  • takeScreenshot()                                     │
└────────────────────┬────────────────────────────────────┘
                     │
          ┌──────────┼──────────┬──────────┬──────────┐
          │          │          │          │          │
          ▼          ▼          ▼          ▼          ▼
    ┌─────────┐┌─────────┐┌─────────┐┌─────────┐┌─────────┐
    │Playwright││Selenium ││HtmlUnit ││Puppeteer││ Cypress │
    │Provider  ││Provider ││Provider ││Provider ││Provider │
    └─────────┘└─────────┘└─────────┘└─────────┘└─────────┘
```

---

## Tool Registration

The Web Test Tool is registered with the `@Tool` annotation:

```java
@Tool(name = "web-test")
public class WebTestTool {
    // Registered for LLM agent discovery
    // Agents can invoke browser automation operations
}
```

**Registration details:**
- **Tool name:** `web-test`
- **Capabilities:** Browser automation, element interaction, assertions, screenshots
- **Providers:** Playwright (default), Selenium, HtmlUnit, Puppeteer, Cypress
- **Reactive API:** All methods return `Mono<T>` for async execution

---

## LLM Workflow Examples

### Example 1: Login Flow Automation

**User Request:**

> "Log in to the application with username 'test@example.com' and password 'password123'"

**LLM Analysis:**

```json
{
  "intent": "login_automation",
  "steps": [
    {
      "operation": "navigate",
      "url": "https://app.example.com/login"
    },
    {
      "operation": "fill_input",
      "selector": "#username",
      "text": "test@example.com"
    },
    {
      "operation": "fill_input",
      "selector": "#password",
      "text": "password123"
    },
    {
      "operation": "click",
      "selector": "#login-button"
    },
    {
      "operation": "wait_for_element",
      "selector": "#dashboard"
    }
  ]
}
```

**Agent Code:**

```java
// Step 1: Navigate to login page
TestResult navResult = webTestTool.navigateTo("https://app.example.com/login").block();

// Step 2: Fill username
TestResult usernameResult = webTestTool.fillInput("#username", "test@example.com").block();

// Step 3: Fill password
TestResult passwordResult = webTestTool.fillInput("#password", "password123").block();

// Step 4: Click login button
TestResult loginResult = webTestTool.click("#login-button").block();

// Step 5: Wait for dashboard
TestResult waitResult = webTestTool.waitForElement("#dashboard").block();
```

**Execution Result:**

```json
{
  "passed": true,
  "operation": "NAVIGATE",
  "url": "https://app.example.com/login",
  "executionTime": "850ms"
}
```

**LLM Response:**

> "I've successfully logged in to the application. The login process completed in 3.2 seconds and the dashboard is now visible."

---

### Example 2: Form Submission with Assertions

**User Request:**

> "Fill out the contact form and verify the success message appears"

**LLM Analysis:**

```json
{
  "intent": "form_submission_with_validation",
  "steps": [
    {
      "operation": "navigate",
      "url": "https://example.com/contact"
    },
    {
      "operation": "fill_inputs",
      "fields": {
        "#name": "John Doe",
        "#email": "john@example.com",
        "#message": "Hello world"
      }
    },
    {
      "operation": "click",
      "selector": "#submit-button"
    },
    {
      "operation": "assert_text_contains",
      "selector": ".success-message",
      "expected": "Thank you"
    }
  ]
}
```

**Agent Code:**

```java
// Navigate to contact form
webTestTool.navigateTo("https://example.com/contact").block();

// Fill form fields
webTestTool.fillInput("#name", "John Doe").block();
webTestTool.fillInput("#email", "john@example.com").block();
webTestTool.fillInput("#message", "Hello world").block();

// Submit form
webTestTool.click("#submit-button").block();

// Assert success message
TestResult assertionResult = webTestTool.assertTextContains(
    ".success-message",
    "Thank you"
).block();
```

**Result:**

```json
{
  "passed": true,
  "operation": "ASSERT_TEXT",
  "selector": ".success-message",
  "expected": "Thank you",
  "actual": "Thank you for your message!",
  "executionTime": "150ms"
}
```

**LLM Response:**

> "The contact form has been submitted successfully. The success message 'Thank you for your message!' is now displayed."

---

### Example 3: Visual Regression Testing

**User Request:**

> "Take a screenshot of the homepage and compare it with the baseline"

**LLM Analysis:**

```json
{
  "intent": "visual_regression_test",
  "steps": [
    {
      "operation": "navigate",
      "url": "https://example.com"
    },
    {
      "operation": "wait_for_visible",
      "selector": "#main-content"
    },
    {
      "operation": "take_screenshot",
      "filePath": "/screenshots/homepage-current.png"
    }
  ]
}
```

**Agent Code:**

```java
// Navigate to homepage
webTestTool.navigateTo("https://example.com").block();

// Wait for content to be visible
webTestTool.waitForVisible("#main-content").block();

// Take screenshot
Screenshot screenshot = webTestTool.saveScreenshot("/screenshots/homepage-current.png").block();
```

**Result:**

```json
{
  "filePath": "/screenshots/homepage-current.png",
  "base64Data": "iVBORw0KGgoAAAANSUhEUgAA...",
  "width": 1920,
  "height": 1080,
  "format": "PNG",
  "size": 245678
}
```

**LLM Response:**

> "I've captured a screenshot of the homepage (1920x1080, 245KB). The screenshot has been saved to /screenshots/homepage-current.png and is ready for visual comparison."

---

### Example 4: Data Extraction (Web Scraping)

**User Request:**

> "Extract all product names and prices from the catalog page"

**LLM Analysis:**

```json
{
  "intent": "data_extraction",
  "steps": [
    {
      "operation": "navigate",
      "url": "https://shop.example.com/catalog"
    },
    {
      "operation": "find_elements",
      "selector": ".product-card"
    },
    {
      "operation": "extract_data",
      "fields": {
        "name": ".product-name",
        "price": ".product-price"
      }
    }
  ]
}
```

**Agent Code:**

```java
// Navigate to catalog
webTestTool.navigateTo("https://shop.example.com/catalog").block();

// Find all product cards
List<ElementInfo> products = webTestTool.findElements(".product-card").block();

// Extract data from each product
List<Map<String, String>> productData = products.stream()
    .map(product -> {
        String name = webTestTool.getText(product.getSelector() + " .product-name").block();
        String price = webTestTool.getText(product.getSelector() + " .product-price").block();
        return Map.of("name", name, "price", price);
    })
    .toList();
```

**Result:**

```json
[
  {
    "name": "Laptop Pro X1",
    "price": "$1,299.99"
  },
  {
    "name": "Wireless Mouse",
    "price": "$29.99"
  },
  {
    "name": "Mechanical Keyboard",
    "price": "$149.99"
  }
]
```

**LLM Response:**

> "I found 3 products on the catalog page:
>
>> 1. Laptop Pro X1 - $1,299.99
>> 2. Wireless Mouse - $29.99
>> 3. Mechanical Keyboard - $149.99"

---

### Example 5: Multi-Step E2E Test

**User Request:**

> "Test the complete checkout flow from product selection to order confirmation"

**LLM Analysis:**

```json
{
  "intent": "e2e_checkout_test",
  "steps": [
    "navigate_to_product",
    "add_to_cart",
    "proceed_to_checkout",
    "fill_shipping_info",
    "complete_payment",
    "verify_confirmation"
  ]
}
```

**Agent Code:**

```java
// 1. Navigate to product page
webTestTool.navigateTo("https://shop.example.com/product/123").block();
webTestTool.assertElementVisible("#product-details").block();

// 2. Add to cart
webTestTool.click("#add-to-cart-button").block();
webTestTool.waitForVisible(".cart-notification").block();

// 3. Proceed to checkout
webTestTool.click("#checkout-button").block();
webTestTool.waitForElement("#checkout-form").block();

// 4. Fill shipping information
webTestTool.fillInput("#shipping-name", "John Doe").block();
webTestTool.fillInput("#shipping-address", "123 Main St").block();
webTestTool.fillInput("#shipping-city", "New York").block();

// 5. Complete payment
webTestTool.fillInput("#card-number", "4111111111111111").block();
webTestTool.fillInput("#card-expiry", "12/25").block();
webTestTool.fillInput("#card-cvv", "123").block();
webTestTool.click("#place-order-button").block();

// 6. Verify confirmation
TestResult confirmationResult = webTestTool.waitForElement("#order-confirmation").block();
String orderNumber = webTestTool.getText("#order-number").block();
```

**Result:**

```json
{
  "passed": true,
  "operation": "WAIT",
  "selector": "#order-confirmation",
  "url": "https://shop.example.com/order/confirmation",
  "executionTime": "2500ms"
}
```

**LLM Response:**

> "The checkout flow completed successfully! Order #ORD-2025-1234 has been placed. The entire process took 8.7 seconds from product selection to order confirmation."

---

## Tool Descriptor Format

### OpenAI Function Calling Format

LLMs use these tool descriptors to understand available operations:

#### 1. Navigate to URL

```json
{
  "name": "webtest_navigate",
  "description": "Navigate the browser to a specific URL",
  "parameters": {
    "type": "object",
    "properties": {
      "url": {
        "type": "string",
        "description": "The URL to navigate to (must start with http:// or https://)"
      }
    },
    "required": ["url"]
  }
}
```

#### 2. Click Element

```json
{
  "name": "webtest_click",
  "description": "Click on a web page element",
  "parameters": {
    "type": "object",
    "properties": {
      "selector": {
        "type": "string",
        "description": "CSS selector or XPath to identify the element (e.g., '#submit-button', '.nav-link')"
      }
    },
    "required": ["selector"]
  }
}
```

#### 3. Fill Input Field

```json
{
  "name": "webtest_fill_input",
  "description": "Fill text into an input field",
  "parameters": {
    "type": "object",
    "properties": {
      "selector": {
        "type": "string",
        "description": "CSS selector for the input field"
      },
      "text": {
        "type": "string",
        "description": "Text to enter into the input field"
      }
    },
    "required": ["selector", "text"]
  }
}
```

#### 4. Assert Element Visible

```json
{
  "name": "webtest_assert_visible",
  "description": "Assert that an element is visible on the page",
  "parameters": {
    "type": "object",
    "properties": {
      "selector": {
        "type": "string",
        "description": "CSS selector for the element to check"
      }
    },
    "required": ["selector"]
  }
}
```

#### 5. Take Screenshot

```json
{
  "name": "webtest_screenshot",
  "description": "Take a screenshot of the entire page or a specific element",
  "parameters": {
    "type": "object",
    "properties": {
      "selector": {
        "type": "string",
        "description": "Optional CSS selector for element screenshot. If omitted, captures full page."
      },
      "filePath": {
        "type": "string",
        "description": "Optional file path to save screenshot. If omitted, returns base64 data."
      }
    },
    "required": []
  }
}
```

#### 6. Execute JavaScript

```json
{
  "name": "webtest_execute_script",
  "description": "Execute JavaScript code in the browser context",
  "parameters": {
    "type": "object",
    "properties": {
      "script": {
        "type": "string",
        "description": "JavaScript code to execute"
      },
      "args": {
        "type": "array",
        "description": "Optional arguments to pass to the script",
        "items": {
          "type": "object"
        }
      }
    },
    "required": ["script"]
  }
}
```

#### 7. Wait for Element

```json
{
  "name": "webtest_wait_for_element",
  "description": "Wait for an element to exist in the DOM",
  "parameters": {
    "type": "object",
    "properties": {
      "selector": {
        "type": "string",
        "description": "CSS selector for the element to wait for"
      },
      "timeout": {
        "type": "number",
        "description": "Maximum wait time in milliseconds (default: 30000)",
        "default": 30000
      }
    },
    "required": ["selector"]
  }
}
```

---

## Parameter Mapping

### Selector Types

The Web Test Tool supports multiple selector types:

| Selector Type  |         Example          |              Description              |
|----------------|--------------------------|---------------------------------------|
| CSS ID         | `#login-button`          | Element with ID 'login-button'        |
| CSS Class      | `.form-input`            | Elements with class 'form-input'      |
| CSS Attribute  | `[type="submit"]`        | Elements with type attribute 'submit' |
| CSS Descendant | `form .submit-btn`       | Button inside a form                  |
| XPath          | `//button[@id='submit']` | XPath expression                      |
| Data Attribute | `[data-test-id="login"]` | Element with data-test-id attribute   |

### Wait Strategies

Different wait strategies for element interactions:

|    Strategy     |           Method           |              Use Case              |
|-----------------|----------------------------|------------------------------------|
| Element exists  | `waitForElement(selector)` | Wait for element to appear in DOM  |
| Element visible | `waitForVisible(selector)` | Wait for element to be visible     |
| Element enabled | `waitForEnabled(selector)` | Wait for element to be interactive |
| Fixed duration  | `wait(milliseconds)`       | Wait for specific time period      |

### Provider Selection

Select different browser automation providers:

|  Provider  |         Configuration         |                Use Case                |
|------------|-------------------------------|----------------------------------------|
| Playwright | `WebTestConfig.defaults()`    | Modern, fast, auto-wait (recommended)  |
| Selenium   | `WebTestConfig.selenium()`    | Cross-browser compatibility            |
| HtmlUnit   | `WebTestConfig.lightweight()` | Headless, no browser (fast tests)      |
| Puppeteer  | `WebTestConfig.puppeteer()`   | Chrome DevTools Protocol features      |
| Cypress    | `WebTestConfig.cypress()`     | E2E testing with time-travel debugging |

---

## Error Handling

### Common Error Scenarios

**1. Element Not Found**

```json
{
  "passed": false,
  "operation": "CLICK",
  "error": "Element not found: #missing-button",
  "selector": "#missing-button",
  "executionTime": "30000ms"
}
```

**LLM Handling:**
- Retry with alternative selector
- Wait longer for dynamic content
- Inform user element doesn't exist
- Suggest checking page structure

**2. Timeout Error**

```json
{
  "passed": false,
  "operation": "WAIT",
  "error": "Timeout waiting for element: .loading-spinner",
  "selector": ".loading-spinner",
  "executionTime": "30000ms"
}
```

**LLM Handling:**
- Increase timeout for slow-loading pages
- Check if page loaded correctly
- Verify network conditions
- Inform user of delay

**3. Navigation Error**

```json
{
  "passed": false,
  "operation": "NAVIGATE",
  "error": "Failed to navigate: ERR_NAME_NOT_RESOLVED",
  "url": "https://invalid-domain.example",
  "executionTime": "5000ms"
}
```

**LLM Handling:**
- Verify URL is correct
- Check network connectivity
- Suggest alternative URL
- Inform user domain doesn't exist

**4. Assertion Failure**

```json
{
  "passed": false,
  "operation": "ASSERT_TEXT",
  "selector": ".error-message",
  "expected": "Login successful",
  "actual": "Invalid credentials",
  "error": "Text assertion failed",
  "executionTime": "100ms"
}
```

**LLM Handling:**
- Report test failure clearly
- Compare expected vs actual
- Suggest corrective actions
- Log for debugging

---

## Security Considerations

### 1. Domain Whitelisting

**Problem:** Prevent navigation to malicious or unauthorized domains

**Solution:** Configure allowed domains in `WebTestConfig`

```java
WebTestConfig config = WebTestConfig.builder()
    .allowedDomains(List.of("example.com", "app.example.com"))
    .build();
```

**LLM Guidance:**
- Only navigate to whitelisted domains
- Reject requests for unauthorized URLs
- Validate URLs before navigation

### 2. Input Sanitization

**Problem:** Prevent XSS through form inputs

**Solution:** Sanitize user-provided text before filling inputs

```java
// Agent should sanitize input before filling
String sanitizedInput = sanitizeHtml(userInput);
webTestTool.fillInput("#search", sanitizedInput).block();
```

**LLM Guidance:**
- Validate and sanitize all user inputs
- Escape special characters
- Warn about potentially dangerous inputs

### 3. JavaScript Execution Restrictions

**Problem:** Arbitrary JavaScript execution can be dangerous

**Solution:** Whitelist allowed JavaScript operations

```java
// Only allow specific, safe JavaScript operations
if (isAllowedScript(script)) {
    webTestTool.executeScript(script).block();
} else {
    throw new SecurityException("JavaScript execution not allowed");
}
```

**LLM Guidance:**
- Limit JavaScript execution to trusted scripts
- Avoid executing user-provided JavaScript
- Log all JavaScript executions

### 4. Screenshot Privacy

**Problem:** Screenshots may contain sensitive information

**Solution:** Sanitize or redact sensitive elements before screenshots

```java
// Hide sensitive elements before screenshot
webTestTool.executeScript(
    "document.querySelectorAll('.sensitive-data').forEach(el => el.style.visibility = 'hidden')"
).block();

Screenshot screenshot = webTestTool.takeScreenshot().block();
```

**LLM Guidance:**
- Redact sensitive information in screenshots
- Respect privacy policies
- Secure screenshot storage

---

## Use Cases

### 1. End-to-End Testing

**Description:** Test complete user workflows from start to finish

**LLM Role:**
- Understand test scenarios from natural language
- Generate test steps automatically
- Execute multi-step test flows
- Report test results with context

**Example:**

```
User: "Test the user registration flow"
LLM: Navigates → Fills form → Submits → Verifies email confirmation
```

### 2. Web Scraping / Data Extraction

**Description:** Extract structured data from web pages

**LLM Role:**
- Identify relevant page elements
- Navigate pagination
- Extract and structure data
- Handle dynamic content loading

**Example:**

```
User: "Extract all product reviews from the page"
LLM: Finds review elements → Extracts ratings and text → Formats as JSON
```

### 3. Form Automation

**Description:** Automatically fill and submit web forms

**LLM Role:**
- Map user data to form fields
- Handle different input types (text, select, checkbox)
- Submit forms and verify success
- Handle form validation errors

**Example:**

```
User: "Fill out the contact form with my details"
LLM: Extracts user data → Maps to fields → Fills form → Submits → Verifies
```

### 4. Visual Regression Testing

**Description:** Detect visual changes in UI

**LLM Role:**
- Navigate to pages systematically
- Capture screenshots at different viewports
- Compare with baseline images
- Report visual differences

**Example:**

```
User: "Check if the homepage looks correct"
LLM: Navigates → Takes screenshots → Compares → Reports differences
```

### 5. Performance Testing

**Description:** Measure page load times and performance metrics

**LLM Role:**
- Navigate to pages and measure timing
- Extract performance metrics via JavaScript
- Analyze and report performance data
- Identify bottlenecks

**Example:**

```
User: "How fast does the checkout page load?"
LLM: Navigates → Measures load time → Analyzes metrics → Reports findings
```

### 6. Accessibility Testing

**Description:** Verify web accessibility compliance

**LLM Role:**
- Check for ARIA attributes
- Verify keyboard navigation
- Test screen reader compatibility
- Report accessibility issues

**Example:**

```
User: "Check if the form is accessible"
LLM: Inspects elements → Tests keyboard nav → Verifies ARIA → Reports issues
```

---

## Best Practices

### For LLM Developers

**1. Use Smart Waits**

```java
// ❌ BAD: Fixed waits
webTestTool.wait(5000).block();
webTestTool.click("#button").block();

// ✅ GOOD: Smart waits
webTestTool.waitForVisible("#button").block();
webTestTool.click("#button").block();
```

**2. Handle Dynamic Content**

```java
// ✅ Wait for AJAX content to load
webTestTool.waitForElement(".ajax-content").block();
webTestTool.assertElementVisible(".ajax-content").block();
```

**3. Use Descriptive Selectors**

```java
// ❌ BAD: Generic selectors
webTestTool.click("button").block();

// ✅ GOOD: Specific selectors
webTestTool.click("#submit-payment-button").block();
```

**4. Provide Clear Error Messages**

```java
if (!result.isPassed()) {
    String errorMsg = String.format(
        "Failed to %s: %s (selector: %s, expected: %s, actual: %s)",
        result.getOperation(),
        result.getError(),
        result.getSelector(),
        result.getExpected(),
        result.getActual()
    );
    // Return clear error to user
}
```

**5. Use Appropriate Provider**

```java
// For fast tests: HtmlUnit (headless)
webTestTool.setConfig(WebTestConfig.lightweight());

// For visual testing: Playwright (modern)
webTestTool.setConfig(WebTestConfig.defaults());

// For cross-browser: Selenium
webTestTool.setConfig(WebTestConfig.selenium());
```

### For Agent Implementers

**1. Implement Retry Logic**

```java
@Override
public TaskResult executeTask(TaskRequest request) {
    int maxRetries = 3;
    for (int i = 0; i < maxRetries; i++) {
        try {
            TestResult result = webTestTool.click("#button").block();
            if (result.isPassed()) {
                return TaskResult.success(...);
            }
        } catch (Exception e) {
            if (i == maxRetries - 1) {
                return TaskResult.failure(...);
            }
            // Wait before retry
            Thread.sleep(1000 * (i + 1));
        }
    }
}
```

**2. Validate Selectors**

```java
private boolean isValidSelector(String selector) {
    // CSS selector validation
    if (selector.matches("^[#.]?[a-zA-Z0-9_-]+.*$")) {
        return true;
    }

    // XPath validation
    if (selector.startsWith("//")) {
        return true;
    }

    return false;
}
```

**3. Aggregate Multi-Step Results**

```java
List<TestResult> stepResults = new ArrayList<>();

stepResults.add(webTestTool.navigateTo(url).block());
stepResults.add(webTestTool.fillInput("#username", username).block());
stepResults.add(webTestTool.click("#login").block());

boolean allPassed = stepResults.stream().allMatch(TestResult::isPassed);

return TaskResult.builder()
    .success(allPassed)
    .output(formatResults(stepResults))
    .build();
```

**4. Handle Provider-Specific Capabilities**

```java
// Check provider capabilities
if (config.getProvider() == TestProvider.PUPPETEER) {
    // Use Puppeteer-specific features (CDP, PDF generation)
    webTestTool.executeScript("return performance.timing").block();
} else if (config.getProvider() == TestProvider.CYPRESS) {
    // Use Cypress-specific features (time-travel debugging)
}
```

**5. Implement Timeout Configuration**

```java
WebTestConfig config = WebTestConfig.builder()
    .defaultTimeout(30000)  // 30 seconds
    .pageLoadTimeout(60000) // 60 seconds for slow pages
    .build();

webTestTool.setConfig(config);
```

---

## Complete Integration Example

### Scenario: Login and Dashboard Verification

**User Request:**

> "Log in to the admin panel and verify the dashboard shows today's statistics"

**Complete Agent Implementation:**

```java
@Tool(name = "web-automation-agent")
public class WebAutomationAgent implements Agent {

    @Inject
    private WebTestTool webTestTool;

    @Override
    public TaskResult executeTask(TaskRequest request) {
        long startTime = System.currentTimeMillis();
        List<TestResult> stepResults = new ArrayList<>();

        try {
            // Step 1: Navigate to login page
            log.info("Navigating to login page...");
            TestResult navResult = webTestTool.navigateTo("https://admin.example.com/login").block();
            stepResults.add(navResult);

            if (!navResult.isPassed()) {
                return TaskResult.failure("admin-agent", request.task(),
                    "Failed to navigate to login page: " + navResult.getError());
            }

            // Step 2: Fill username
            log.info("Filling username...");
            TestResult usernameResult = webTestTool.fillInput(
                "#username",
                "admin@example.com"
            ).block();
            stepResults.add(usernameResult);

            // Step 3: Fill password
            log.info("Filling password...");
            TestResult passwordResult = webTestTool.fillInput(
                "#password",
                System.getenv("ADMIN_PASSWORD")  // Secure password handling
            ).block();
            stepResults.add(passwordResult);

            // Step 4: Click login button
            log.info("Clicking login button...");
            TestResult loginResult = webTestTool.click("#login-button").block();
            stepResults.add(loginResult);

            // Step 5: Wait for dashboard to load
            log.info("Waiting for dashboard...");
            TestResult dashboardResult = webTestTool.waitForVisible("#dashboard").block();
            stepResults.add(dashboardResult);

            if (!dashboardResult.isPassed()) {
                // Take screenshot for debugging
                Screenshot screenshot = webTestTool.takeScreenshot().block();
                return TaskResult.failure("admin-agent", request.task(),
                    "Dashboard did not load. Screenshot saved to: " + screenshot.getFilePath());
            }

            // Step 6: Verify today's statistics are displayed
            log.info("Verifying statistics...");
            TestResult statsResult = webTestTool.assertElementVisible(".today-statistics").block();
            stepResults.add(statsResult);

            // Step 7: Extract statistics
            String statsText = webTestTool.getText(".today-statistics").block();

            // Step 8: Take success screenshot
            Screenshot finalScreenshot = webTestTool.saveScreenshot(
                "/screenshots/dashboard-success.png"
            ).block();

            // Calculate execution time
            long executionTime = System.currentTimeMillis() - startTime;

            // Format success response
            String output = String.format(
                """
                Successfully logged in to admin panel and verified dashboard:

                ✅ Navigation: %s
                ✅ Login: Completed in %dms
                ✅ Dashboard: Loaded successfully
                ✅ Statistics: %s

                Screenshot: %s
                Total execution time: %dms
                """,
                navResult.getUrl(),
                loginResult.getExecutionTime().toMillis(),
                statsText,
                finalScreenshot.getFilePath(),
                executionTime
            );

            return TaskResult.success("admin-agent", request.task(), output,
                Map.of(
                    "steps", stepResults.size(),
                    "allPassed", stepResults.stream().allMatch(TestResult::isPassed),
                    "screenshot", finalScreenshot.getFilePath(),
                    "statistics", statsText
                ),
                executionTime
            );

        } catch (Exception e) {
            log.error("Test execution failed", e);

            // Take failure screenshot
            try {
                Screenshot errorScreenshot = webTestTool.takeScreenshot().block();
                return TaskResult.failure("admin-agent", request.task(),
                    "Test failed with error: " + e.getMessage() +
                    ". Screenshot: " + errorScreenshot.getFilePath());
            } catch (Exception screenshotError) {
                return TaskResult.failure("admin-agent", request.task(),
                    "Test failed: " + e.getMessage());
            }
        } finally {
            // Cleanup: close browser
            webTestTool.close().block();
        }
    }

    @Override
    public String getName() {
        return "web-automation-agent";
    }

    @Override
    public String getDescription() {
        return "Automates web testing and browser interactions";
    }

    @Override
    public List<String> getCapabilities() {
        return List.of(
            "Browser automation",
            "Form filling",
            "Element interaction",
            "Assertions",
            "Screenshot capture",
            "JavaScript execution"
        );
    }
}
```

**Expected Output:**

```
Successfully logged in to admin panel and verified dashboard:

✅ Navigation: https://admin.example.com/login
✅ Login: Completed in 1250ms
✅ Dashboard: Loaded successfully
✅ Statistics: Today's Revenue: $12,345 | Orders: 87 | Active Users: 234

Screenshot: /screenshots/dashboard-success.png
Total execution time: 3847ms
```

---

*Last Updated: 2025-10-25*
