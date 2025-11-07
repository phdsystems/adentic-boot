package dev.adeengineer.adentic.boot.provider;

import dev.adeengineer.resilience4j.Resilience4jProxyFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating resilience provider instances using Resilience4j.
 *
 * <p>Creates pre-configured resilience providers from adentic-resilience4j with:
 *
 * <ul>
 *   <li>Circuit breakers (prevent cascading failures)
 *   <li>Retry logic (automatic retry with backoff)
 *   <li>Bulkhead patterns (limit concurrent calls)
 *   <li>Rate limiting (control request rates)
 *   <li>Time limiters (request timeouts)
 * </ul>
 *
 * <h2>Supported Patterns</h2>
 *
 * <ul>
 *   <li><strong>Circuit Breaker:</strong> Open circuit after N failures, auto-recover
 *   <li><strong>Retry:</strong> Exponential backoff retry with max attempts
 *   <li><strong>Bulkhead:</strong> Limit concurrent executions (thread pool isolation)
 *   <li><strong>Rate Limiter:</strong> Limit requests per time period
 *   <li><strong>Time Limiter:</strong> Timeout protection
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Create resilience proxy factory
 * Resilience4jProxyFactory resilienceFactory =
 *     ResilienceProviderFactory.createResilienceProxyFactory();
 *
 * // Wrap service with resilience patterns using annotations
 * // On your service methods, use:
 * // @CircuitBreaker(name = "my-service")
 * // @Retryable(name = "my-service", maxAttempts = 3)
 * // @Bulkhead(name = "my-service", maxConcurrent = 10)
 *
 * MyService resilientService = resilienceFactory.create(MyService.class, myService);
 * }</pre>
 *
 * <h2>Configuration via Annotations</h2>
 *
 * <p>The adentic-resilience4j module uses annotations from adentic-commons:
 *
 * <pre>{@code
 * import dev.adeengineer.commons.annotation.resilience.*;
 *
 * public class MyService {
 *     @CircuitBreaker(name = "external-api")
 *     @Retryable(name = "external-api", maxAttempts = 3)
 *     public String callExternalAPI() {
 *         // Your logic here
 *     }
 *
 *     @Bulkhead(name = "heavy-operation", maxConcurrent = 5)
 *     public void performHeavyOperation() {
 *         // Your logic here
 *     }
 * }
 * }</pre>
 */
@Slf4j
public final class ResilienceProviderFactory {

  private ResilienceProviderFactory() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Create Resilience4j proxy factory.
   *
   * <p>Returns a factory that can wrap services with resilience patterns using annotations.
   *
   * @return configured resilience proxy factory
   */
  public static Resilience4jProxyFactory createResilienceProxyFactory() {
    log.info("Creating Resilience4j proxy factory");

    Resilience4jProxyFactory factory = new Resilience4jProxyFactory();
    log.info("Resilience4j proxy factory created successfully");
    log.info(
        "Use @CircuitBreaker, @Retryable, @Bulkhead annotations on service methods for resilience");

    return factory;
  }

  /**
   * Check if resilience patterns are available.
   *
   * @return always true (Resilience4j always available)
   */
  public static boolean isResilienceAvailable() {
    return true; // Resilience4j always available
  }
}
