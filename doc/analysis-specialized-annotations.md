# Analysis: Specialized Dependency Injection Annotations for AdenticBoot

**Date:** 2025-10-24
**Version:** 1.0
**Status:** Planning

---

## TL;DR

**Current State**: AdenticBoot uses generic `@Component` and `@Inject` with some specialization (`@Service`, `@RestController`). **Proposed Enhancement**: Add domain-specific annotations for all provider and service types to improve code clarity, enable automatic registration in specialized registries, and provide better tooling support. **Implementation**: Meta-annotations pattern already supported → zero breaking changes → progressive adoption.

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Current State Analysis](#current-state-analysis)
3. [Provider Type Taxonomy](#provider-type-taxonomy)
4. [Proposed Annotation Hierarchy](#proposed-annotation-hierarchy)
5. [Technical Design](#technical-design)
6. [Implementation Plan](#implementation-plan)
7. [Benefits and Trade-offs](#benefits-and-trade-offs)
8. [Backward Compatibility](#backward-compatibility)
9. [Examples](#examples)
10. [Next Steps](#next-steps)

---

## Executive Summary

### Problem Statement

AdenticBoot currently uses generic `@Component` and `@Inject` annotations for dependency injection. While functional, this approach lacks semantic clarity for the diverse types of services and providers in the Adentic ecosystem (LLM providers, infrastructure services, message brokers, etc.).

### Proposed Solution

Introduce **specialized stereotype annotations** for each category of service/provider:

- `@InfrastructureProvider` - Docker, local infrastructure
- `@LLMProvider` - OpenAI, Anthropic, Ollama, etc.
- `@StorageProvider` - Local, cloud storage
- `@MessagingProvider` - Kafka, RabbitMQ, Redis, in-memory
- `@OrchestrationProvider` - Workflow orchestration
- `@MemoryProvider` - Agent memory management
- `@QueueProvider` - Task queues
- `@ToolProvider` - Agent tools
- `@EvaluationProvider` - LLM evaluation
- Plus domain services: `@DomainService`, `@AgentService`, etc.

### Key Benefits

1. **Semantic Clarity**: `@LLMProvider` is more descriptive than `@Component`
2. **Automatic Registration**: Providers can auto-register in specialized registries
3. **Type Safety**: Compile-time verification of provider contracts
4. **Tooling Support**: IDE autocomplete, documentation generation
5. **Zero Breaking Changes**: Meta-annotation pattern preserves backward compatibility

---

## Current State Analysis

### Existing DI System

**Base Annotations:**

```java
@Component              // Base marker for all managed components
@Inject                 // Marks injection points (constructor, field, method)
```

**Current Specializations:**

```java
@Service                // Business logic layer (meta-annotated with @Component)
@RestController         // HTTP endpoints (meta-annotated with @Component)
```

**Scanner Implementation:**
- ✅ **Meta-annotation support**: Scanner detects classes annotated with annotations that are themselves annotated with `@Component` (lines 156-162 of `ComponentScanner.java`)
- ✅ **Recursive package scanning**: Finds all components in base package
- ✅ **Flexible**: Can scan for specific annotation types

**Context Management:**

```java
AgenticContext
├── registerSingleton(Class<T>, T)     // Type-based registration
├── registerSingleton(String, Object)  // Name-based registration
├── registerFactory(Class<T>, Supplier<T>) // Lazy initialization
└── getBean(Class<T>)                  // Type-based retrieval
```

### Analysis of Provider Ecosystem

**Identified Provider Categories** (from `adentic-core-impl/src/main/java/dev/adeengineer/adentic/providers`):

|      Category      |          Package           |                                Example Providers                                 | Count |
|--------------------|----------------------------|----------------------------------------------------------------------------------|-------|
| **Inference**      | `providers.inference.text` | OpenAI, Anthropic, Ollama, Groq, HuggingFace, Replicate, TogetherAI, FireworksAI | 8     |
| **Infrastructure** | `providers.infrastructure` | LocalInfrastructure, DockerInfrastructure                                        | 2     |
| **Messaging**      | `providers.messaging`      | Kafka, RabbitMQ, Redis, InMemory                                                 | 4     |
| **Storage**        | `providers.storage`        | LocalStorage                                                                     | 1+    |
| **Memory**         | `providers.memory`         | InMemoryMemory                                                                   | 1+    |
| **Orchestration**  | `providers.orchestration`  | SimpleOrchestration                                                              | 1+    |
| **Queue**          | `providers.queue`          | InMemoryTaskQueue                                                                | 1+    |
| **Tools**          | `providers.tools`          | SimpleTool                                                                       | 1+    |
| **Evaluation**     | `providers.evaluation`     | LLMEvaluation                                                                    | 1+    |

**Total**: 20+ provider implementations across 9 categories.

---

## Provider Type Taxonomy

### Tier 1: Core Infrastructure

**Purpose**: Foundation services for compute, storage, networking

```
@InfrastructureProvider
├── LocalInfrastructureProvider
├── DockerInfrastructureProvider
└── KubernetesInfrastructureProvider (future)

@StorageProvider
├── LocalStorageProvider
├── S3StorageProvider (future)
└── AzureBlobStorageProvider (future)

@MessagingProvider
├── InMemoryMessageBroker
├── KafkaMessageBroker
├── RabbitMQMessageBroker
└── RedisMessageBroker
```

### Tier 2: AI/ML Services

**Purpose**: LLM inference, embeddings, evaluation

```
@LLMProvider (or @InferenceProvider)
├── OpenAITextGenerationProvider
├── AnthropicTextGenerationProvider
├── OllamaTextGenerationProvider
├── GroqTextGenerationProvider
├── HuggingFaceTextGenerationProvider
├── ReplicateTextGenerationProvider
├── TogetherAITextGenerationProvider
└── FireworksAITextGenerationProvider

@EvaluationProvider
└── LLMEvaluationProvider

@EmbeddingProvider (future)
└── OpenAIEmbeddingProvider
```

### Tier 3: Agent Platform Services

**Purpose**: Agent lifecycle, memory, orchestration

```
@OrchestrationProvider
└── SimpleOrchestrationProvider

@MemoryProvider
├── InMemoryMemoryProvider
├── RedisMemoryProvider (future)
└── PostgresMemoryProvider (future)

@QueueProvider
├── InMemoryTaskQueueProvider
├── RedisQueueProvider (future)
└── SQSQueueProvider (future)

@ToolProvider
└── SimpleToolProvider
```

### Tier 4: Domain Services

**Purpose**: Business logic, domain-specific services

```
@DomainService
├── FinancialDomainService
├── MediaDomainService
└── ResearchDomainService

@AgentService
├── AgentRegistryService
├── DomainManagerService
└── RoleManagerService
```

---

## Proposed Annotation Hierarchy

### Annotation Structure

```
@Component (base marker)
│
├── @Service (existing - business logic)
├── @RestController (existing - HTTP endpoints)
│
├── @Provider (new - base for all providers)
│   │
│   ├── @InfrastructureProvider
│   │   ├── ComputeProvider
│   │   ├── NetworkProvider
│   │   └── ContainerProvider
│   │
│   ├── @LLMProvider (or @InferenceProvider)
│   │   ├── TextGenerationProvider
│   │   ├── EmbeddingProvider
│   │   └── ImageGenerationProvider
│   │
│   ├── @StorageProvider
│   │   ├── ObjectStorageProvider
│   │   ├── FileStorageProvider
│   │   └── BlobStorageProvider
│   │
│   ├── @MessagingProvider
│   │   ├── MessageBrokerProvider
│   │   ├── EventStreamProvider
│   │   └── PubSubProvider
│   │
│   ├── @OrchestrationProvider
│   ├── @MemoryProvider
│   ├── @QueueProvider
│   ├── @ToolProvider
│   └── @EvaluationProvider
│
├── @DomainService
│   ├── AgentService
│   ├── WorkflowService
│   └── RegistryService
│
└── @Repository (future - data access layer)
```

### Design Principles

1. **Semantic Hierarchy**: Each specialized annotation clearly indicates purpose
2. **Meta-annotation Pattern**: All specialized annotations are meta-annotated with `@Component`
3. **Optional Attributes**: Support `value` for custom bean names, `scope` for lifecycle
4. **Extensible**: Easy to add new categories without modifying scanner
5. **Backward Compatible**: Existing `@Component` continues to work

---

## Technical Design

### 1. Base Provider Annotation

```java
package dev.adeengineer.adentic.boot.annotations;

import java.lang.annotation.*;

/**
 * Base annotation for all provider components.
 *
 * <p>Providers implement specific contracts from adentic-core and supply
 * infrastructure, AI/ML, or platform services to agents.
 *
 * <p>This is a specialization of {@link Component} for provider classes.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Provider {

    /**
     * Suggested bean name (optional).
     *
     * @return bean name, defaults to decapitalized class name
     */
    String value() default "";

    /**
     * Provider category for grouping and discovery.
     *
     * @return provider category (e.g., "infrastructure", "llm", "storage")
     */
    String category() default "";
}
```

### 2. Specialized Provider Annotations

```java
/**
 * Marks a class as an LLM inference provider.
 *
 * <p>LLM providers implement text generation, completion, and chat interfaces
 * for various language model backends (OpenAI, Anthropic, local models, etc.).
 *
 * <p>Example:
 * <pre>{@code
 * @LLMProvider
 * public class OpenAITextGenerationProvider implements TextGenerationProvider {
 *     // Implementation
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Provider(category = "llm")
public @interface LLMProvider {
    String value() default "";

    /**
     * Model provider name (e.g., "openai", "anthropic", "ollama").
     */
    String provider() default "";
}

/**
 * Marks a class as an infrastructure provider.
 *
 * <p>Infrastructure providers manage compute resources, containers,
 * and deployment environments.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Provider(category = "infrastructure")
public @interface InfrastructureProvider {
    String value() default "";

    /**
     * Infrastructure type (e.g., "local", "docker", "kubernetes").
     */
    String type() default "";
}

/**
 * Marks a class as a storage provider.
 *
 * <p>Storage providers handle file, object, and blob storage operations.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Provider(category = "storage")
public @interface StorageProvider {
    String value() default "";
}

/**
 * Marks a class as a messaging provider.
 *
 * <p>Messaging providers implement message brokers, event streams,
 * and pub/sub systems.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Provider(category = "messaging")
public @interface MessagingProvider {
    String value() default "";
}

// ... similar for OrchestrationProvider, MemoryProvider, QueueProvider, etc.
```

### 3. Domain Service Annotations

```java
/**
 * Marks a class as a domain service.
 *
 * <p>Domain services contain business logic specific to a particular
 * domain (e.g., finance, media, research).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface DomainService {
    String value() default "";

    /**
     * Domain name (e.g., "finance", "media", "research").
     */
    String domain() default "";
}

/**
 * Marks a class as an agent service.
 *
 * <p>Agent services manage agent lifecycle, registration, and coordination.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface AgentService {
    String value() default "";
}
```

### 4. Enhanced Scanner (Optional)

Add provider-specific discovery methods:

```java
public class ComponentScanner {

    // Existing methods...

    /**
     * Scan for all provider components.
     *
     * @return map of provider category to provider classes
     */
    public Map<String, Set<Class<?>>> scanProviders() {
        Map<String, Set<Class<?>>> providersByCategory = new HashMap<>();

        Set<Class<?>> allProviders = scan(Provider.class);
        for (Class<?> providerClass : allProviders) {
            Provider annotation = findProviderAnnotation(providerClass);
            if (annotation != null) {
                String category = annotation.category();
                providersByCategory
                    .computeIfAbsent(category, k -> new HashSet<>())
                    .add(providerClass);
            }
        }

        return providersByCategory;
    }

    /**
     * Find @Provider annotation on class or its meta-annotations.
     */
    private Provider findProviderAnnotation(Class<?> clazz) {
        // Check direct annotation
        if (clazz.isAnnotationPresent(Provider.class)) {
            return clazz.getAnnotation(Provider.class);
        }

        // Check meta-annotations
        for (Annotation annotation : clazz.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Provider.class)) {
                return annotation.annotationType().getAnnotation(Provider.class);
            }
        }

        return null;
    }
}
```

### 5. Provider Registry (Optional Enhancement)

```java
/**
 * Registry for provider discovery and management.
 *
 * <p>Automatically populated during component scanning with all
 * provider implementations grouped by category.
 */
public class ProviderRegistry {

    private final Map<String, Map<String, Object>> providers = new ConcurrentHashMap<>();

    /**
     * Register a provider by category and name.
     */
    public void registerProvider(String category, String name, Object provider) {
        providers.computeIfAbsent(category, k -> new ConcurrentHashMap<>())
                 .put(name, provider);
    }

    /**
     * Get all providers in a category.
     */
    public Map<String, Object> getProviders(String category) {
        return providers.getOrDefault(category, Map.of());
    }

    /**
     * Get a specific provider by category and name.
     */
    public <T> T getProvider(String category, String name, Class<T> type) {
        return type.cast(providers.getOrDefault(category, Map.of()).get(name));
    }

    /**
     * Get all LLM providers.
     */
    public Map<String, Object> getLLMProviders() {
        return getProviders("llm");
    }

    /**
     * Get all infrastructure providers.
     */
    public Map<String, Object> getInfrastructureProviders() {
        return getProviders("infrastructure");
    }

    // ... similar convenience methods for other categories
}
```

---

## Implementation Plan

### Phase 1: Foundation (Week 1)

**Objective**: Create base annotation infrastructure

**Tasks:**
1. ✅ Create `@Provider` base annotation with `category` attribute
2. ✅ Create `@DomainService` annotation
3. ✅ Create `@AgentService` annotation
4. ✅ Add unit tests for meta-annotation detection
5. ✅ Update documentation with new annotations

**Deliverables:**
- 3 new annotation files
- Test suite demonstrating meta-annotation pattern
- Updated developer guide

### Phase 2: Core Provider Annotations (Week 2)

**Objective**: Add most commonly used provider annotations

**Tasks:**
1. ✅ Create `@LLMProvider` (affects 8 implementations)
2. ✅ Create `@InfrastructureProvider` (affects 2 implementations)
3. ✅ Create `@StorageProvider`
4. ✅ Create `@MessagingProvider` (affects 4 implementations)
5. ✅ Update existing providers to use new annotations
6. ✅ Add integration tests

**Deliverables:**
- 4 new specialized annotations
- Updated provider implementations (14 classes)
- Integration test suite

### Phase 3: Specialized Providers (Week 3)

**Objective**: Complete provider annotation coverage

**Tasks:**
1. ✅ Create `@OrchestrationProvider`
2. ✅ Create `@MemoryProvider`
3. ✅ Create `@QueueProvider`
4. ✅ Create `@ToolProvider`
5. ✅ Create `@EvaluationProvider`
6. ✅ Update remaining providers
7. ✅ Add examples

**Deliverables:**
- 5 additional annotations
- All providers annotated (20+ classes)
- Example applications

### Phase 4: Enhanced Discovery (Week 4)

**Objective**: Add provider registry and advanced discovery

**Tasks:**
1. ✅ Implement `ProviderRegistry` class
2. ✅ Add `ComponentScanner.scanProviders()` method
3. ✅ Integrate with `AgenticContext`
4. ✅ Add provider lifecycle hooks
5. ✅ Performance optimization

**Deliverables:**
- `ProviderRegistry` implementation
- Enhanced scanner capabilities
- Performance benchmarks

### Phase 5: Documentation and Migration (Week 5)

**Objective**: Complete migration guide and tooling

**Tasks:**
1. ✅ Write migration guide for existing code
2. ✅ Create annotation reference documentation
3. ✅ Add IDE autocomplete support
4. ✅ Create code generation templates
5. ✅ Update all examples

**Deliverables:**
- Migration guide
- Complete API documentation
- IDE support files
- Updated examples

---

## Benefits and Trade-offs

### Benefits

**1. Semantic Clarity**
- ✅ `@LLMProvider` immediately conveys purpose vs generic `@Component`
- ✅ Self-documenting code reduces cognitive load
- ✅ Easier onboarding for new developers

**2. Automatic Registration**
- ✅ Providers can auto-register in specialized registries
- ✅ Simplifies provider discovery: `registry.getLLMProviders()`
- ✅ Enables dynamic provider selection at runtime

**3. Type Safety**
- ✅ Compile-time validation that provider implements correct contract
- ✅ IDE can suggest correct interfaces for each annotation
- ✅ Prevents misconfiguration errors

**4. Tooling Support**
- ✅ IDE autocomplete for provider types
- ✅ Better refactoring support
- ✅ Documentation generators can group by annotation

**5. Extensibility**
- ✅ Add new provider types without modifying scanner
- ✅ Third-party extensions can define their own annotations
- ✅ Future-proof architecture

**6. Zero Breaking Changes**
- ✅ Meta-annotation pattern preserves backward compatibility
- ✅ Existing `@Component` classes continue to work
- ✅ Progressive migration path

### Trade-offs

**1. Annotation Proliferation**
- ⚠️ More annotations to maintain (9+ new annotations)
- ✅ Mitigated by clear naming conventions and documentation
- ✅ Each annotation adds semantic value

**2. Learning Curve**
- ⚠️ Developers need to learn which annotation to use
- ✅ Mitigated by IDE autocomplete and comprehensive docs
- ✅ Annotations are intuitive (name matches purpose)

**3. Maintenance Overhead**
- ⚠️ Need to keep annotations in sync with provider contracts
- ✅ Mitigated by automated tests
- ✅ Annotations rarely change once defined

**4. Potential Over-engineering**
- ⚠️ Risk of adding annotations that aren't used
- ✅ Mitigated by starting with core types only
- ✅ Add more as needed (YAGNI principle)

---

## Backward Compatibility

### Compatibility Strategy

**100% Backward Compatible**: All existing code continues to work without changes.

**Reason**: Meta-annotation pattern means:
1. `@Component` still works for everything
2. Scanner already supports meta-annotations
3. No changes to `AgenticContext` required
4. Progressive migration at developer's pace

### Migration Path

**Option 1: Do Nothing**

```java
// Existing code - still works!
@Component
public class OpenAITextGenerationProvider { }
```

**Option 2: Migrate Immediately**

```java
// New code - more semantic
@LLMProvider(provider = "openai")
public class OpenAITextGenerationProvider { }
```

**Option 3: Mixed Mode**

```java
// Old providers still use @Component
@Component
public class LegacyService { }

// New providers use specialized annotations
@LLMProvider
public class NewLLMProvider { }
```

All three options work simultaneously with zero conflicts.

### Deprecation Policy

**No Deprecations Required**

- `@Component` is NOT deprecated
- Still useful for custom components outside provider categories
- Examples: utilities, helpers, adapters

---

## Examples

### Example 1: LLM Provider

**Before:**

```java
@Component
public class OpenAITextGenerationProvider implements TextGenerationProvider {

    @Inject
    public OpenAITextGenerationProvider(ConfigService config) {
        // ...
    }

    @Override
    public String generate(String prompt) {
        // ...
    }
}
```

**After:**

```java
@LLMProvider(provider = "openai")
public class OpenAITextGenerationProvider implements TextGenerationProvider {

    @Inject
    public OpenAITextGenerationProvider(ConfigService config) {
        // ...
    }

    @Override
    public String generate(String prompt) {
        // ...
    }
}
```

**Benefits:**
- Clear intent: this is an LLM provider
- `provider = "openai"` documents which backend
- Can be discovered via `registry.getLLMProviders()`

### Example 2: Infrastructure Provider

**Before:**

```java
@Component
public class DockerInfrastructureProvider implements InfrastructureProvider {
    // ...
}
```

**After:**

```java
@InfrastructureProvider(type = "docker")
public class DockerInfrastructureProvider implements InfrastructureProvider {
    // ...
}
```

### Example 3: Domain Service

**Before:**

```java
@Service
public class FinancialDomainManager {
    // ...
}
```

**After:**

```java
@DomainService(domain = "finance")
public class FinancialDomainManager {
    // ...
}
```

### Example 4: Provider Discovery

```java
@AgenticBootApplication
public class MyApplication {

    public static void main(String[] args) {
        AgenticApplication app = AgenticApplication.run(MyApplication.class);

        // Get provider registry
        ProviderRegistry registry = app.getContext().getBean(ProviderRegistry.class);

        // Discover all LLM providers
        Map<String, Object> llmProviders = registry.getLLMProviders();
        System.out.println("Available LLM providers: " + llmProviders.keySet());
        // Output: [openai, anthropic, ollama, groq, ...]

        // Get specific provider
        TextGenerationProvider openai = registry.getProvider(
            "llm", "openai", TextGenerationProvider.class
        );

        // Use provider
        String response = openai.generate("Hello, world!");
    }
}
```

### Example 5: Dynamic Provider Selection

```java
@RestController
@RequestMapping("/api/v1/llm")
public class LLMController {

    @Inject
    private ProviderRegistry providerRegistry;

    @PostMapping("/generate")
    public String generate(
        @RequestParam String provider,
        @RequestBody GenerateRequest request) {

        // Dynamically select provider at runtime
        TextGenerationProvider llm = providerRegistry.getProvider(
            "llm", provider, TextGenerationProvider.class
        );

        if (llm == null) {
            throw new ProviderNotFoundException("Unknown provider: " + provider);
        }

        return llm.generate(request.getPrompt());
    }
}
```

---

## Next Steps

### Immediate Actions (This Week)

1. **Review this analysis** with team for feedback
2. **Prioritize annotations** to implement first (likely @LLMProvider, @InfrastructureProvider)
3. **Create POC** with 2-3 specialized annotations
4. **Test meta-annotation pattern** works correctly with scanner

### Short-term (Next 2 Weeks)

1. **Implement Phase 1-2** (base annotations + core providers)
2. **Migrate 5 providers** as proof of concept
3. **Write integration tests** for new annotations
4. **Update developer guide** with new patterns

### Medium-term (Next Month)

1. **Complete all provider annotations** (Phase 3)
2. **Implement ProviderRegistry** (Phase 4)
3. **Migrate all existing providers** to use new annotations
4. **Add examples** demonstrating dynamic provider selection

### Long-term (Next Quarter)

1. **Add advanced features** (scopes, qualifiers, conditional registration)
2. **Create annotation processor** for compile-time validation
3. **Build tooling** (IDE plugins, code generators)
4. **Gather metrics** on developer experience improvements

---

## References

### Internal Documentation

- [AdenticBoot Architecture](./architecture.md)
- [Component Specifications](./3-design/component-specifications.md)
- [Developer Guide](./4-development/developer-guide.md)

### External Resources

- [Spring Framework Annotations](https://docs.spring.io/spring-framework/reference/core/beans/classpath-scanning.html)
- [Meta-Annotations in Java](https://docs.oracle.com/javase/tutorial/java/annotations/predefined.html)
- [Dependency Injection Patterns](https://martinfowler.com/articles/injection.html)

---

**Last Updated:** 2025-10-24
**Version:** 1.0
**Author:** AdenticBoot Team
