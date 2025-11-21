package dev.engineeringlab.adentic.boot.annotations.provider;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a caching provider implementation.
 *
 * <p>Cache providers handle in-memory and distributed caching, cache eviction strategies,
 * time-to-live (TTL) management, and cache persistence. They enable high-performance data access,
 * reduced database load, and improved application responsiveness.
 *
 * <p>The framework discovers all {@code @CacheProvider} annotated classes at runtime and registers
 * them for auto-configuration and dependency injection.
 *
 * <p><b>Supported Providers:</b>
 *
 * <ul>
 *   <li><b>Redis:</b> Distributed in-memory data store
 *   <li><b>Memcached:</b> High-performance distributed memory object caching
 *   <li><b>Hazelcast:</b> In-memory data grid with distributed caching
 *   <li><b>Caffeine:</b> High-performance Java 8+ in-memory cache
 *   <li><b>Ehcache:</b> Java distributed cache with persistence
 *   <li><b>Guava Cache:</b> Google's in-memory cache library
 * </ul>
 *
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheProvider {

  /**
   * Unique name for this cache provider.
   *
   * <p>Used for provider selection and configuration. Should be lowercase and descriptive.
   *
   * <p>Examples: "redis", "memcached", "caffeine", "ehcache", "hazelcast"
   *
   * @return provider name
   */
  String name();

  /**
   * Whether this provider supports distributed caching.
   *
   * @return true if distributed caching is supported
   */
  boolean supportsDistributed() default false;

  /**
   * Whether this provider supports time-to-live (TTL) for cache entries.
   *
   * @return true if TTL is supported
   */
  boolean supportsTTL() default true;

  /**
   * Whether this provider supports cache persistence to disk.
   *
   * @return true if persistence is supported
   */
  boolean supportsPersistence() default false;

  /**
   * Default time-to-live for cache entries in seconds.
   *
   * <p>Set to -1 for no expiration (manual eviction only).
   *
   * @return default TTL in seconds
   */
  int defaultTTL() default 3600;

  /**
   * Maximum number of entries in the cache.
   *
   * <p>Set to -1 for unlimited size (use with caution).
   *
   * @return maximum cache entries
   */
  int maxEntries() default 10000;

  /**
   * Cache eviction strategy.
   *
   * <p>Common strategies: lru, lfu, fifo, random, ttl, none
   *
   * @return eviction strategy identifier
   */
  String evictionStrategy() default "lru";

  /**
   * Human-readable description of this cache provider.
   *
   * @return provider description
   */
  String description() default "";

  /**
   * Selection priority when multiple providers are available.
   *
   * <p>Higher values indicate higher priority.
   *
   * @return selection priority
   */
  int priority() default 0;

  /**
   * Whether this provider is enabled by default.
   *
   * @return true if enabled by default
   */
  boolean enabledByDefault() default true;
}
