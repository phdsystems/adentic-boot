# Service Discovery System Design

## TL;DR

The service discovery system provides a lightweight, annotation-based Service Provider Interface (SPI) mechanism that leverages Java's ServiceLoader for plugin-style provider discovery and dependency injection. It enables compile-time registration of pluggable implementations through the `@Provider` meta-annotation pattern, supporting automatic field injection, priority-based selection, and domain-specific provider annotations without requiring heavy frameworks like Spring.

## Overview

The service discovery system in adentic-se provides a framework for discovering and managing provider implementations at runtime. It combines Java's ServiceLoader mechanism with a custom annotation-based configuration system to enable:

- Automatic discovery of provider implementations via `META-INF/services/` files
- Priority-based provider selection when multiple implementations exist
- Compile-time field injection via annotation processing
- Domain-specific provider annotations through meta-annotation pattern
- Zero-configuration provider lookup through the `Services` facade

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Application Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │
│  │   Service    │  │   Service    │  │   Service    │             │
│  │   Class A    │  │   Class B    │  │   Class C    │             │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘             │
│         │ @Provider        │ Services         │ Services           │
│         │ field            │ .get()           │ .getAll()          │
└─────────┼──────────────────┼──────────────────┼─────────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Service Facade Layer                           │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                    Services (Facade)                         │   │
│  │  • get(Class<T>)         - Get highest priority provider    │   │
│  │  • get(Class<T>, String) - Get provider by name             │   │
│  │  • getAll(Class<T>)      - Get all providers by priority    │   │
│  │  • inject(Object, Class) - Inject @Provider fields          │   │
│  └─────────────────────────┬───────────────────────────────────┘   │
└────────────────────────────┼───────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Service Discovery Layer                          │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                  ServiceDiscovery (Registry)                 │   │
│  │  ┌───────────────────────────────────────────────────────┐  │   │
│  │  │  Provider Cache (ConcurrentHashMap)                   │  │   │
│  │  │  • providerInstances: Map<Class<?>, List<?>>         │  │   │
│  │  │  • singleProviders: Map<Class<?>, Object>            │  │   │
│  │  │  • serviceLoaderInitialized: Set<Class<?>>           │  │   │
│  │  └───────────────────────────────────────────────────────┘  │   │
│  │                                                               │   │
│  │  • loadFromServiceLoader() - Discover via ServiceLoader     │   │
│  │  • getPriority()           - Extract priority from @Provider│   │
│  │  • getProviderName()       - Extract name from @Provider    │   │
│  └─────────────────────────┬───────────────────────────────────┘   │
└────────────────────────────┼───────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   Java ServiceLoader Layer                          │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │         ServiceLoader.load(ProviderInterface.class)          │   │
│  │                                                               │   │
│  │  Reads: META-INF/services/<interface.fully.qualified.name>  │   │
│  │                                                               │   │
│  │  Example:                                                     │   │
│  │    META-INF/services/dev.engineeringlab.vcs.VcsProvider      │   │
│  │    ┌─────────────────────────────────────────────────────┐  │   │
│  │    │ dev.engineeringlab.vcs.git.GitVcsProvider          │  │   │
│  │    │ dev.engineeringlab.vcs.gitea.GiteaVcsProvider      │  │   │
│  │    └─────────────────────────────────────────────────────┘  │   │
│  └─────────────────────────┬───────────────────────────────────┘   │
└────────────────────────────┼───────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Provider Implementation Layer                    │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐  │
│  │ @Provider        │  │ @TextGeneration  │  │ @WebSearch      │  │
│  │ GitVcsProvider   │  │ Provider         │  │ Provider        │  │
│  │                  │  │ AnthropicProvider│  │ SerperProvider  │  │
│  │ priority=10      │  │ priority=15      │  │ priority=10     │  │
│  └──────────────────┘  └──────────────────┘  └─────────────────┘  │
│                                                                     │
│  Each implements the respective provider interface                 │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                    Annotation Processing (Compile Time)             │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │           @Provider Annotation Processor                     │   │
│  │                                                               │   │
│  │  1. Scans for @Provider and meta-annotated classes          │   │
│  │  2. Generates META-INF/services/<interface> files           │   │
│  │  3. Generates field initializer classes (optional)          │   │
│  │                                                               │   │
│  │  Input:  @Provider class FooProvider implements Foo         │   │
│  │  Output: META-INF/services/com.example.Foo                  │   │
│  │          └─ com.example.FooProvider                         │   │
│  └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

## Component Descriptions

### 1. annotations-core Module

**Location**: `/home/adentic/swe-framework/adentic-se/common/annotations/annotations-core`

The core module provides the foundational annotations and runtime infrastructure for the service discovery system.

**Key Components**:
- `@Provider` - Meta-annotation for marking provider implementations and fields
- `Services` - Facade for service lookup and field injection
- `ServiceDiscovery` - Registry managing ServiceLoader integration and caching
- `ProviderException` - Exception thrown when providers cannot be found or injected

**Dependencies**:
- Java ServiceLoader (JDK built-in)
- SLF4J for logging

### 2. @Provider Meta-Annotation

**Location**: `dev.engineeringlab.common.annotation.Provider`

The `@Provider` annotation serves three distinct purposes:

#### Purpose 1: Class Annotation (Provider Implementation)

Marks a class as a provider implementation that should be discovered via ServiceLoader.

**Attributes**:
- `name` (String): Unique identifier for the provider (e.g., "git", "anthropic", "redis")
- `description` (String): Human-readable description for documentation and UI
- `priority` (int): Selection priority when multiple providers exist (default: 0)
  - 20+ Production providers (cloud-managed, enterprise)
  - 15-19 Production providers (self-hosted, standard)
  - 10-14 Development providers (local, lightweight)
  - 5-9 Testing providers (mock, in-memory)
  - 0-4 No-op or fallback providers
- `enabledByDefault` (boolean): Whether provider is enabled without explicit configuration (default: true)

**Target**: `ElementType.TYPE`

#### Purpose 2: Field Annotation (Provider Injection)

Marks a field for automatic provider injection via annotation processor or manual initialization.

**Target**: `ElementType.FIELD`

#### Purpose 3: Meta-Annotation (Domain-Specific Provider Annotations)

Used as a meta-annotation to create domain-specific provider annotations that inherit provider behavior.

**Target**: `ElementType.ANNOTATION_TYPE`

### 3. Services Facade

**Location**: `dev.engineeringlab.common.provider.Services`

The `Services` class provides a simplified API for service discovery and injection. It delegates to `ServiceDiscovery` for the actual implementation but provides a cleaner interface for consumers.

**Key Methods**:

```java
// Get highest priority provider for a type
<T> T get(Class<T> type)

// Get provider by name
<T> Optional<T> get(Class<T> type, String name)

// Get all providers sorted by priority (highest first)
<T> List<T> getAll(Class<T> type)

// Check if provider exists for type
boolean exists(Class<?> type)

// Inject @Provider fields into target instance
<T> void inject(T target, Class<T> type)

// Reset registry (for testing)
void reset()
```

**Usage Pattern**:

The recommended approach is direct field initialization:

```java
public class MyService {
    // Service initialized directly - works out of the box
    private final CacheProvider cache = Services.get(CacheProvider.class);

    public void doWork() {
        cache.put("key", "value");
    }
}
```

### 4. ServiceDiscovery (ServiceLoader Integration)

**Location**: `dev.engineeringlab.common.provider.ServiceDiscovery`

The `ServiceDiscovery` class manages the integration with Java's ServiceLoader mechanism and provides caching for discovered services.

**Internal Caches**:
- `serviceLoaderInitialized` (Set<Class<?>>): Tracks which types have been loaded
- `providerInstances` (Map<Class<?>, List<?>>): Cache of all providers per interface
- `singleProviders` (Map<Class<?>, Object>): Cache of highest priority provider per interface

**Discovery Process**:

1. First call to `get(Class<T>)` triggers ServiceLoader discovery
2. `ServiceLoader.load(type)` reads `META-INF/services/<interface-fqn>`
3. Each implementation class is instantiated via no-arg constructor
4. Providers are sorted by priority (descending order)
5. Results are cached for subsequent lookups

**Priority Resolution**:

The `getPriority()` method extracts priority from:
1. Direct `@Provider` annotation on the class
2. Meta-annotated domain-specific annotations (e.g., `@TextGenerationProvider`)

**Error Handling**:

- Providers that fail to instantiate are logged and skipped
- Missing providers throw `ProviderException` with descriptive messages
- ServiceLoader errors are caught and logged for graceful degradation

## Domain-Specific Annotations Pattern

Domain-specific provider annotations are meta-annotated with `@Provider` to inherit provider discovery behavior while adding domain-specific metadata.

### Example: @TextGenerationProvider

**Location**: `dev.engineeringlab.llm.text.annotation.TextGenerationProvider`

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Provider  // Meta-annotation
public @interface TextGenerationProvider {
    String name();
    String model();
    boolean supportsStreaming() default false;
    int maxTokens() default -1;
    int contextWindow() default -1;
    boolean isLocal() default false;
    String description() default "";
    int priority() default 0;
    boolean enabledByDefault() default true;
}
```

**Benefits of Meta-Annotation Pattern**:

1. **Type Safety**: Domain-specific attributes (e.g., `model`, `supportsStreaming`) are compile-time validated
2. **Self-Documentation**: Annotation attributes serve as inline documentation
3. **Reusable Infrastructure**: All domain annotations inherit provider discovery behavior
4. **Separation of Concerns**: Domain logic separated from discovery mechanism

### Other Domain-Specific Annotations

**@WorkflowProvider**:
- Location: `dev.engineeringlab.workflow.orchestration.annotation.WorkflowProvider`
- Domain: Workflow orchestration engines (Temporal, Airflow, Camunda)
- Attributes: Standard provider attributes

**@WebSearchProvider**:
- Location: `dev.engineeringlab.websearch.annotation.WebSearchProvider`
- Domain: Web search engines and APIs
- Attributes: `supportsRealTime`, `supportsImages`, `supportsNews`, `optimizedForAI`, `privacyFocused`, `maxResultsPerQuery`, `rateLimitPerMonth`, `requiresApiKey`

## Auto-Injection Mechanism

The system supports two approaches for provider injection:

### Approach 1: Direct Initialization (Recommended)

The simplest approach with zero configuration:

```java
public class MyService {
    private final CacheProvider cache = Providers.get(CacheProvider.class);
    private final MetricsProvider metrics = Providers.get(MetricsProvider.class);

    public void execute() {
        cache.put("key", "value");
        metrics.increment("executions");
    }
}
```

**Advantages**:
- No build configuration required
- Works out of the box
- Clear and explicit
- Compatible with all build tools

### Approach 2: Field Injection (AspectJ-based)

Automatic injection of `@Provider` annotated fields (requires AspectJ configuration):

```java
public class MyService {
    @Provider
    CacheProvider cache;

    @Provider
    MetricsProvider metrics;

    // Fields automatically injected after construction
}
```

**Requirements**:
- AspectJ Maven plugin configuration
- Load-time or compile-time weaving
- `ProviderInjectionAspect` on classpath

**Implementation**:

The `Services.inject()` method scans all fields (including inherited fields) for `@Provider` annotations and injects the highest priority service:

```java
public static <T> void inject(T target, Class<T> type) {
    Class<?> clazz = target.getClass();

    // Process this class and all superclasses
    while (clazz != null && clazz != Object.class) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Provider.class)) {
                Object service = ServiceDiscovery.get(field.getType());
                field.setAccessible(true);
                field.set(target, service);
            }
        }
        clazz = clazz.getSuperclass();
    }
}
```

## ServiceLoader Integration

The system uses Java's standard ServiceLoader mechanism for provider discovery.

### Service File Format

Service files are located at: `META-INF/services/<fully.qualified.interface.name>`

**Example**: `META-INF/services/dev.engineeringlab.vcs.VcsProvider`

```
dev.engineeringlab.vcs.git.GitVcsProvider
dev.engineeringlab.vcs.gitea.GiteaVcsProvider
```

### Automatic Service File Generation

While service files can be created manually, they are typically auto-generated by annotation processors that scan for `@Provider` annotated classes.

**Build Time Process**:

1. Annotation processor scans for classes with `@Provider` or meta-annotated annotations
2. For each provider implementation, determines the provider interface it implements
3. Generates or updates `META-INF/services/<interface>` file with implementation class name
4. Service files are packaged into JAR during build

### ServiceLoader Discovery

At runtime, when a provider is first requested:

```java
CacheProvider cache = Services.get(CacheProvider.class);
```

The following occurs:

1. `ServiceDiscovery.getAll(CacheProvider.class)` is called
2. `ServiceLoader.load(CacheProvider.class)` reads `META-INF/services/...CacheProvider`
3. Each listed implementation class is instantiated via reflection
4. Classes that fail to instantiate are logged and skipped
5. Successfully instantiated providers are sorted by priority
6. Results are cached for future lookups

## Priority System

The priority system enables deterministic provider selection when multiple implementations are available.

### Priority Levels

**Recommended Ranges**:

| Range | Category | Examples |
|-------|----------|----------|
| 20+ | Production (Enterprise) | AWS S3, Google Cloud Storage, Azure Blob |
| 15-19 | Production (Standard) | PostgreSQL, Redis, Kafka |
| 10-14 | Development | H2 Database, Embedded Redis, Local File Storage |
| 5-9 | Testing | In-Memory Cache, Mock Services |
| 0-4 | Fallback | No-op implementations, Default stubs |

### Priority Resolution

When `Services.get(Class<T>)` is called:

1. All providers are loaded via ServiceLoader
2. Providers are sorted by priority in **descending order** (highest first)
3. The first provider (highest priority) is returned
4. If multiple providers have the same priority, order is non-deterministic

**Example**:

```java
@Provider(name = "anthropic", priority = 15)
public class AnthropicTextGenerationProvider implements TextGenerationProvider { }

@Provider(name = "openai", priority = 10)
public class OpenAITextGenerationProvider implements TextGenerationProvider { }

@Provider(name = "ollama", priority = 5)
public class OllamaTextGenerationProvider implements TextGenerationProvider { }

// Returns AnthropicTextGenerationProvider (highest priority)
TextGenerationProvider provider = Services.get(TextGenerationProvider.class);
```

### Selecting Specific Provider by Name

To bypass priority and select a specific provider:

```java
// Get by name
Optional<TextGenerationProvider> ollama =
    Services.get(TextGenerationProvider.class, "ollama");

if (ollama.isPresent()) {
    TextGenerationResponse response = ollama.get().generate(request);
}
```

### Getting All Providers

To retrieve all providers sorted by priority:

```java
List<TextGenerationProvider> allProviders =
    Services.getAll(TextGenerationProvider.class);

// allProviders[0] = highest priority
// allProviders[n] = lowest priority
```

## Usage Examples

### Example 1: Class Annotation (Provider Implementation)

**Define Provider Interface**:

```java
package dev.engineeringlab.vcs;

public interface VcsProvider {
    String getName();
    void clone(String url, Path destination);
    void commit(String message);
}
```

**Implement Provider**:

```java
package dev.engineeringlab.vcs.git;

import dev.engineeringlab.common.annotation.Provider;

@Provider(
    name = "git",
    description = "Git version control system",
    priority = 10
)
public class GitVcsProvider implements VcsProvider {

    public GitVcsProvider() {
        // No-arg constructor required for ServiceLoader
    }

    @Override
    public String getName() {
        return "git";
    }

    @Override
    public void clone(String url, Path destination) {
        // Git clone implementation
    }

    @Override
    public void commit(String message) {
        // Git commit implementation
    }
}
```

**Create Service File** (typically auto-generated):

`META-INF/services/dev.engineeringlab.vcs.VcsProvider`
```
dev.engineeringlab.vcs.git.GitVcsProvider
```

### Example 2: Field Injection

**Using Direct Initialization** (Recommended):

```java
public class CodebaseManager {
    private final VcsProvider vcs = Services.get(VcsProvider.class);
    private final CacheProvider cache = Services.get(CacheProvider.class);

    public void cloneRepository(String url, Path destination) {
        vcs.clone(url, destination);
        cache.put("lastClone", url);
    }
}
```

**Using @Provider Field Annotation** (Requires AspectJ):

```java
public class CodebaseManager {
    @Provider
    VcsProvider vcs;

    @Provider
    CacheProvider cache;

    public void cloneRepository(String url, Path destination) {
        vcs.clone(url, destination);  // Injected automatically
        cache.put("lastClone", url);
    }
}
```

### Example 3: Facade Usage

**Get Highest Priority Provider**:

```java
TextGenerationProvider llm = Services.get(TextGenerationProvider.class);
TextGenerationResponse response = llm.generate(request);
```

**Get Provider by Name**:

```java
Optional<TextGenerationProvider> anthropic =
    Services.get(TextGenerationProvider.class, "anthropic");

anthropic.ifPresent(provider -> {
    TextGenerationResponse response = provider.generate(request);
});
```

**Get All Providers**:

```java
List<TextGenerationProvider> allLLMs =
    Services.getAll(TextGenerationProvider.class);

for (TextGenerationProvider provider : allLLMs) {
    System.out.println("Found: " + provider.getProviderName()
        + " (priority: " + getPriority(provider) + ")");
}
```

**Check Provider Existence**:

```java
if (Services.exists(CacheProvider.class)) {
    CacheProvider cache = Services.get(CacheProvider.class);
    cache.put("key", "value");
} else {
    System.out.println("No cache provider available");
}
```

### Example 4: Domain-Specific Provider Annotation

**Using @TextGenerationProvider**:

```java
package dev.engineeringlab.llm.provider.anthropic;

import dev.engineeringlab.llm.text.annotation.TextGenerationProvider;

@TextGenerationProvider(
    name = "anthropic",
    model = "claude-3-5-sonnet-20241022",
    supportsStreaming = true,
    maxTokens = 8192,
    contextWindow = 200000,
    description = "Anthropic Claude text generation provider",
    priority = 15,
    enabledByDefault = true
)
public class AnthropicTextGenerationProvider implements TextGenerationProvider {

    public AnthropicTextGenerationProvider() {
        // Load configuration from model-registry.yaml or environment
        // Required for ServiceLoader instantiation
    }

    @Override
    public Mono<TextGenerationResponse> generate(TextGenerationRequest request) {
        // Implementation
    }

    @Override
    public Flux<TextStreamEvent> generateStream(TextGenerationRequest request) {
        // Streaming implementation
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    @Override
    public String getProviderName() {
        return "anthropic";
    }

    @Override
    public String getModel() {
        return "claude-3-5-sonnet-20241022";
    }
}
```

**Service File**:

`META-INF/services/dev.engineeringlab.llm.text.TextGenerationProvider`
```
dev.engineeringlab.llm.provider.anthropic.AnthropicTextGenerationProvider
dev.engineeringlab.llm.provider.openai.OpenAITextGenerationProvider
dev.engineeringlab.llm.provider.ollama.OllamaTextGenerationProvider
```

**Usage**:

```java
// Get highest priority (Anthropic, priority=15)
TextGenerationProvider llm = Services.get(TextGenerationProvider.class);

// Or get specific provider
Optional<TextGenerationProvider> ollama =
    Services.get(TextGenerationProvider.class, "ollama");
```

## Design Rationale

### Why Not Spring Framework?

The service discovery system deliberately avoids Spring Framework dependency for several reasons:

1. **Lightweight**: No heavyweight framework overhead for simple provider discovery
2. **Build Tool Agnostic**: Works with Maven, Gradle, Bazel without special plugins
3. **Startup Performance**: ServiceLoader is faster than Spring's component scanning
4. **JDK Standard**: Uses built-in Java ServiceLoader mechanism
5. **Explicit Dependencies**: Clear, traceable provider resolution without "magic"

### Why ServiceLoader Over Manual Registration?

1. **Standard Mechanism**: Java standard since JDK 1.6, well-understood and supported
2. **Classpath Isolation**: Providers from different JARs automatically discovered
3. **No Central Registry**: Decentralized registration via `META-INF/services/` files
4. **Module System Compatible**: Works with Java Platform Module System (JPMS)

### Why Priority-Based Selection?

1. **Deterministic**: Predictable provider selection in multi-implementation scenarios
2. **Environment-Specific**: Different priorities for dev/test/prod providers
3. **Cost Optimization**: Prefer cheaper or faster providers automatically
4. **Graceful Degradation**: Fallback to lower-priority providers if primary fails

### Why Meta-Annotation Pattern?

1. **Type Safety**: Domain-specific attributes validated at compile time
2. **Discoverability**: Domain annotations self-document provider capabilities
3. **Extensibility**: New provider types without modifying core framework
4. **Separation of Concerns**: Domain logic isolated from discovery infrastructure

## Comparison with Other Approaches

| Feature | adentic-se Provider System | Spring Framework | Google Guice | Java ServiceLoader (Raw) |
|---------|---------------------------|------------------|--------------|--------------------------|
| Dependency Injection | Manual/AspectJ | Automatic | Automatic | Manual |
| Configuration | Annotations | XML/Annotations | Modules | Service Files |
| Startup Time | Fast | Slow | Medium | Fast |
| Priority Support | Yes | @Order/@Priority | Custom | No |
| Meta-Annotations | Yes | Limited | No | No |
| Field Injection | Optional | Yes | Yes | No |
| Testing Support | Built-in reset() | @MockBean | Custom | Manual |
| Framework Overhead | Minimal | Heavy | Medium | None |
| Learning Curve | Low | High | Medium | Low |

## Best Practices

### 1. Provider Implementation

- **Always provide no-arg constructor** for ServiceLoader instantiation
- **Fail gracefully** if configuration (API keys, URLs) is missing
- **Implement health checks** to indicate provider readiness
- **Document configuration** requirements in JavaDoc
- **Use appropriate priority** based on production vs development usage

### 2. Provider Discovery

- **Prefer `Services.get()`** over direct ServiceLoader usage
- **Cache service references** in fields to avoid repeated lookups
- **Handle `ProviderException`** when service might not be available
- **Use `Services.exists()`** to check availability before assuming presence

### 3. Priority Assignment

- **Be consistent** with priority ranges across similar providers
- **Higher priority for production** cloud providers (20+)
- **Medium priority for standard** self-hosted providers (15-19)
- **Lower priority for development** lightweight providers (10-14)
- **Lowest priority for testing** mock/in-memory providers (5-9)

### 4. Testing

- **Use `Services.reset()`** between tests to clear cache
- **Create test-specific services** with high priority for overriding
- **Register test services** in `META-INF/services/` in test resources
- **Verify service selection** with `Services.getAll()` in tests

### 5. Error Handling

- **Catch `ProviderException`** when provider is optional
- **Provide meaningful error messages** with provider name and type
- **Log provider discovery failures** at debug level
- **Fall back gracefully** when preferred provider unavailable

## Troubleshooting

### Provider Not Found

**Symptom**: `ProviderException: No provider found for: com.example.MyProvider`

**Solutions**:
1. Verify `META-INF/services/com.example.MyProvider` file exists in classpath
2. Check service file contains correct implementation class name (fully qualified)
3. Ensure provider class has public no-arg constructor
4. Verify provider is on classpath (check JAR contents)
5. Check for typos in service file (common issue!)

### Wrong Provider Selected

**Symptom**: Lower priority provider selected instead of expected one

**Solutions**:
1. Verify `@Provider` annotation has correct `priority` value
2. Check if higher priority service fails health check
3. Use `Services.getAll()` to inspect all services and their order
4. Look for provider instantiation errors in logs (failed providers are skipped)

### Field Not Injected

**Symptom**: `@Provider` field is null at runtime

**Solutions**:
1. Verify AspectJ weaving is configured (if using field injection)
2. Use direct initialization instead: `= Providers.get(Type.class)`
3. Check AspectJ agent is loaded (for load-time weaving)
4. Verify `ProviderInjectionAspect` is on classpath

### ServiceLoader Performance Issues

**Symptom**: Slow startup or repeated provider instantiation

**Solutions**:
1. Cache service references in fields: `private final Provider p = Services.get(...)`
2. Avoid calling `Services.get()` in hot paths or loops
3. Use `Services.reset()` sparingly (only in tests)
4. Profile with `-XX:+TraceClassLoading` to identify repeated loading

## Future Enhancements

Potential improvements to the service discovery system:

1. **Configuration-Based Priority Override**: Allow runtime priority override via configuration files
2. **Provider Capabilities Matching**: Select provider based on required capabilities, not just priority
3. **Lazy Proxy Injection**: Delay provider instantiation until first use
4. **Provider Metrics**: Track provider usage, failures, latency for monitoring
5. **Health-Based Selection**: Automatically skip unhealthy providers
6. **Provider Lifecycle Hooks**: Support initialization and cleanup callbacks
7. **Multi-Provider Strategies**: Round-robin, random, or custom selection strategies

## Related Documentation

- **Component Architecture**: `/home/adentic/adentic-boot/doc/3-design/architecture.md`
- **Dependency Management**: `/home/adentic/adentic-boot/doc/3-design/dependency-management.md`
- **Implementation Guide**: Provider implementation guides in module-specific documentation

## References

- [Java ServiceLoader Documentation](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html)
- [Service Provider Interface Pattern](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html)
- [Meta-Annotations in Java](https://docs.oracle.com/javase/tutorial/java/annotations/basics.html)
