package dev.engineeringlab.adentic.tool.webtest.provider;

import dev.engineeringlab.adentic.tool.webtest.config.WebTestConfig;

/**
 * Abstract base class for custom web test provider implementations.
 *
 * <p>Allows users to create their own web testing provider implementations by extending this class
 * and implementing the WebTestProvider interface.
 *
 * <p>Use cases:
 *
 * <ul>
 *   <li>Proprietary or internal testing tools
 *   <li>Custom browser automation solutions
 *   <li>Specialized testing frameworks
 *   <li>Integration with existing test infrastructure
 *   <li>Testing tools not yet supported by framework
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * public class MyCustomTestProvider extends CustomWebTestProvider {
 *
 *     private MyTestingTool tool;
 *
 *     public MyCustomTestProvider(WebTestConfig config) {
 *         super(config);
 *     }
 *
 *     @Override
 *     public Mono<Void> start() {
 *         return Mono.fromRunnable(() -> {
 *             tool = new MyTestingTool();
 *             tool.initialize(config);
 *             running = true;
 *         });
 *     }
 *
 *     @Override
 *     public Mono<TestResult> navigateTo(String url) {
 *         return Mono.fromCallable(() -> {
 *             validateUrl(url);
 *             tool.navigate(url);
 *             return createSuccessResult(OperationType.NAVIGATE, "Navigated: " + url);
 *         });
 *     }
 *
 *     // Implement other methods...
 * }
 *
 * // Usage
 * WebTestTool webTest = new WebTestTool();
 * webTest.setProvider(new MyCustomTestProvider(config));
 * }</pre>
 *
 * <p>Benefits:
 *
 * <ul>
 *   <li>Full control over implementation
 *   <li>Access to base provider utilities (validation, logging, result creation)
 *   <li>Consistent API with other providers
 *   <li>Automatic integration with WebTestTool
 * </ul>
 *
 * <p>Implementation checklist:
 *
 * <ol>
 *   <li>Extend CustomWebTestProvider
 *   <li>Implement all abstract methods from WebTestProvider
 *   <li>Use validateUrl() for security
 *   <li>Use createSuccessResult() and createFailureResult() for consistent results
 *   <li>Use logOperation() for debugging
 *   <li>Handle errors appropriately
 *   <li>Manage provider lifecycle (start/close)
 * </ol>
 *
 * @see WebTestProvider
 * @see BaseWebTestProvider
 */
public abstract class CustomWebTestProvider extends BaseWebTestProvider {

  /**
   * Constructor for custom provider implementations.
   *
   * @param config the web test configuration
   */
  public CustomWebTestProvider(WebTestConfig config) {
    super(config);
  }

  /**
   * Get provider name for logging and identification. Override this to provide a custom name.
   *
   * @return provider name
   */
  public String getProviderName() {
    return this.getClass().getSimpleName();
  }

  /**
   * Validate provider-specific configuration. Override this to add custom validation logic.
   *
   * @throws IllegalArgumentException if configuration is invalid
   */
  protected void validateConfiguration() {
    // Default: no additional validation
    // Override to add custom validation
  }

  /** Hook called after provider starts. Override to add custom initialization logic. */
  protected void onStart() {
    // Default: no-op
    // Override for custom start logic
  }

  /** Hook called before provider closes. Override to add custom cleanup logic. */
  protected void onClose() {
    // Default: no-op
    // Override for custom cleanup logic
  }
}
