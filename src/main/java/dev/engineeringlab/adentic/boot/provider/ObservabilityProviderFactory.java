package dev.engineeringlab.adentic.boot.provider;

import dev.engineeringlab.adentic.observability.monitoring.DefaultMetricsCollector;
import dev.engineeringlab.adentic.observability.monitoring.HealthCheckService;
import dev.engineeringlab.adentic.observability.monitoring.MetricsCollector;
import dev.engineeringlab.adentic.observability.providers.prometheus.PrometheusMetricsProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating observability provider instances.
 *
 * <p>Creates pre-configured observability providers from adentic-core with:
 *
 * <ul>
 *   <li>Metrics collection (Prometheus, custom collectors)
 *   <li>Health checks (liveness, readiness probes)
 *   <li>Environment-based configuration
 *   <li>Ready for registration in ProviderRegistry
 * </ul>
 *
 * <h2>Supported Capabilities</h2>
 *
 * <ul>
 *   <li><strong>Metrics:</strong> Prometheus-compatible metrics collection
 *   <li><strong>Health Checks:</strong> Kubernetes-style health probes
 *   <li><strong>Tracing:</strong> Distributed tracing support (future)
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Create metrics collector
 * MetricsCollector metrics = ObservabilityProviderFactory.createMetricsCollector();
 * registry.registerProvider("default", "metrics", metrics);
 *
 * // Create health check service
 * HealthCheckService health = ObservabilityProviderFactory.createHealthCheckService();
 * registry.registerProvider("default", "health", health);
 *
 * // Create Prometheus metrics provider
 * if (ObservabilityProviderFactory.isPrometheusAvailable()) {
 *   PrometheusMetricsProvider prom = ObservabilityProviderFactory.createPrometheusProvider();
 *   registry.registerProvider("prometheus", "metrics", prom);
 * }
 * }</pre>
 */
@Slf4j
public final class ObservabilityProviderFactory {

  private ObservabilityProviderFactory() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Create default metrics collector.
   *
   * <p>Provides comprehensive metrics collection for:
   *
   * <ul>
   *   <li>Agent execution (count, duration, success/failure rates)
   *   <li>LLM calls (latency, token usage, costs)
   *   <li>Infrastructure (queue depth, message rates)
   * </ul>
   *
   * @return configured metrics collector
   */
  /**
   * Create health check service.
   *
   * <p>Provides Kubernetes-compatible health endpoints:
   *
   * <ul>
   *   <li>/health/live - Liveness probe (is app running?)
   *   <li>/health/ready - Readiness probe (can app serve traffic?)
   *   <li>Component-level health checks
   * </ul>
   *
   * @return configured health check service
   */
  public static HealthCheckService createHealthCheckService() {
    log.info("Creating health check service");
    HealthCheckService service = new HealthCheckService(java.util.List.of());
    log.info("Health check service created successfully");
    return service;
  }

  /**
   * Create metrics collector.
   *
   * <p>Provides comprehensive metrics collection for:
   *
   * <ul>
   *   <li>Agent execution (count, duration, success/failure rates)
   *   <li>LLM calls (latency, token usage, costs)
   *   <li>Infrastructure (queue depth, message rates)
   * </ul>
   *
   * @return configured metrics collector
   */
  public static MetricsCollector createMetricsCollector() {
    log.info("Creating default metrics collector");
    // DefaultMetricsCollector requires HealthCheckService as dependency
    HealthCheckService healthService = createHealthCheckService();
    DefaultMetricsCollector collector = new DefaultMetricsCollector(healthService);
    log.info("Metrics collector created successfully");
    return collector;
  }

  /**
   * Create Prometheus metrics provider.
   *
   * <p>Configuration via environment variables:
   *
   * <ul>
   *   <li>PROMETHEUS_ENABLED - Enable Prometheus (default: false)
   *   <li>PROMETHEUS_PORT - Metrics endpoint port (default: 9090)
   *   <li>PROMETHEUS_PATH - Metrics endpoint path (default: /metrics)
   * </ul>
   *
   * @return configured Prometheus provider
   */
  public static PrometheusMetricsProvider createPrometheusProvider() {
    log.info("Creating Prometheus metrics provider");

    boolean enabled =
        Boolean.parseBoolean(System.getenv().getOrDefault("PROMETHEUS_ENABLED", "false"));

    if (!enabled) {
      log.warn("Prometheus is not enabled. Set PROMETHEUS_ENABLED=true to enable.");
    }

    // PrometheusMetricsProvider requires MetricsAggregator
    dev.adeengineer.adentic.observability.metrics.MetricsAggregator aggregator =
        new dev.adeengineer.adentic.observability.metrics.MetricsAggregator();

    PrometheusMetricsProvider provider = new PrometheusMetricsProvider(aggregator);
    log.info("Prometheus provider created successfully");
    return provider;
  }

  /**
   * Check if Prometheus provider should be created.
   *
   * @return true if PROMETHEUS_ENABLED is true
   */
  public static boolean isPrometheusAvailable() {
    return Boolean.parseBoolean(System.getenv().getOrDefault("PROMETHEUS_ENABLED", "false"));
  }

  /**
   * Check if observability providers are available.
   *
   * @return always true (default metrics/health always available)
   */
  public static boolean isObservabilityAvailable() {
    return true; // Default collectors always available
  }
}
