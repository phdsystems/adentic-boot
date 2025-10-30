package dev.adeengineer.adentic.tool.webtest.provider;

import dev.adeengineer.adentic.tool.webtest.config.WebTestConfig;
import dev.adeengineer.adentic.tool.webtest.model.TestResult;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/** Base class for web test providers with common functionality. */
@Slf4j
@SuppressWarnings("unused")
public abstract class BaseWebTestProvider implements WebTestProvider {

  // CHECKSTYLE:OFF VisibilityModifier - protected fields for subclass access
  protected final WebTestConfig config;
  protected volatile boolean running = false;

  // CHECKSTYLE:ON

  // CHECKSTYLE:OFF HiddenField - constructor parameter shadows field by design
  public BaseWebTestProvider(WebTestConfig config) {
    this.config = config;
  }

  // CHECKSTYLE:ON

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public WebTestConfig getConfig() {
    return config;
  }

  /** Create a success test result. */
  protected TestResult createSuccessResult(TestResult.OperationType operation, String details) {
    return TestResult.builder()
        .operation(operation)
        .passed(true)
        .details(details)
        .timestamp(Instant.now())
        .build();
  }

  /** Create a failure test result. */
  protected TestResult createFailureResult(TestResult.OperationType operation, String error) {
    return TestResult.builder()
        .operation(operation)
        .passed(false)
        .error(error)
        .timestamp(Instant.now())
        .build();
  }

  /** Create a success test result with timing. */
  protected TestResult createTimedResult(
      TestResult.OperationType operation, Instant start, String details) {
    Duration executionTime = Duration.between(start, Instant.now());

    return TestResult.builder()
        .operation(operation)
        .passed(true)
        .details(details)
        .executionTime(executionTime)
        .timestamp(start)
        .build();
  }

  /** Validate URL against allowed/blocked domains. */
  protected void validateUrl(String url) {
    try {
      java.net.URL parsedUrl = java.net.URI.create(url).toURL();
      String domain = parsedUrl.getHost();

      if (!config.isDomainAllowed(domain)) {
        throw new SecurityException(String.format("Domain not allowed: %s", domain));
      }
    } catch (java.net.MalformedURLException e) {
      throw new IllegalArgumentException("Invalid URL: " + url, e);
    }
  }

  /** Wait with slow-mo if configured. */
  protected Mono<Void> applySlowMo() {
    if (config.getSlowMo() > 0) {
      return Mono.delay(Duration.ofMillis(config.getSlowMo())).then();
    }
    return Mono.empty();
  }

  /** Log operation. */
  protected void logOperation(String operation, String details) {
    log.info("[{}] {}: {}", config.getProvider(), operation, details);
  }
}
