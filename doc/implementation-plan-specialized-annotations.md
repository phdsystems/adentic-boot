# Implementation Plan: Specialized Annotations (Clean Migration - Option 2)

**Date:** 2025-10-24
**Version:** 1.0
**Approach:** Full migration to specialized annotations (no backward compatibility required)

---

## TL;DR

**Strategy**: Clean break from generic `@Component` to specialized annotations (`@LLMProvider`, `@InfrastructureProvider`, etc.). **Benefits**: Simpler implementation, clearer semantics, no legacy code maintenance. **Trade-off**: All 20+ providers must be migrated in single release. **Timeline**: 2-3 weeks for complete migration.

---

## Table of Contents

1. [Approach Overview](#approach-overview)
2. [Simplified Design](#simplified-design)
3. [Implementation Steps](#implementation-steps)
4. [Migration Strategy](#migration-strategy)
5. [Testing Plan](#testing-plan)
6. [Rollout Plan](#rollout-plan)

---

## Approach Overview

### Decision: Option 2 (Clean Migration)

**What This Means:**
- ✅ Create standalone specialized annotations (no meta-annotation pattern)
- ✅ Update all 20+ providers to use new annotations in single PR
- ✅ Update scanner to recognize all specialized annotations
- ✅ Cleaner, simpler codebase
- ⚠️ Breaking change (requires version bump)

**What We're NOT Doing:**
- ❌ No meta-annotation pattern (@Component on specialized annotations)
- ❌ No backward compatibility layer
- ❌ No mixed mode (old + new annotations together)
- ❌ No gradual migration

### Benefits of Clean Migration

1. **Simplicity**: No meta-annotation complexity
2. **Clarity**: Each annotation is self-contained
3. **Performance**: Scanner can be optimized for specific annotations
4. **Maintainability**: No legacy code paths to support
5. **Clean Architecture**: Clear separation of concerns

---

## Simplified Design

### Annotation Structure

```
Specialized Annotations (standalone, NOT meta-annotated)
│
├── @LLMProvider          - LLM inference providers
├── @InfrastructureProvider - Infrastructure services
├── @StorageProvider      - Storage services
├── @MessagingProvider    - Message brokers
├── @OrchestrationProvider - Workflow orchestration
├── @MemoryProvider       - Agent memory
├── @QueueProvider        - Task queues
├── @ToolProvider         - Agent tools
├── @EvaluationProvider   - LLM evaluation
├── @DomainService        - Domain-specific services
└── @AgentService         - Agent lifecycle services

Keep for Web/General Use:
├── @RestController       - HTTP endpoints (unchanged)
└── @Service              - General business logic (unchanged)
```

### Core Annotations

**1. Provider Annotations (9 types)**

```java
package dev.adeengineer.adentic.boot.annotations.providers;

import java.lang.annotation.*;

/**
 * Marks a class as an LLM inference provider.
 *
 * <p>Automatically registered in AgenticContext and ProviderRegistry.
 * Providers implement text generation, embeddings, or chat interfaces.
 *
 * <p>Example:
 * <pre>{@code
 * @LLMProvider(name = "openai")
 * public class OpenAITextGenerationProvider implements TextGenerationProvider {
 *     @Inject
 *     public OpenAITextGenerationProvider(ConfigService config) { }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LLMProvider {

    /**
     * Provider name (e.g., "openai", "anthropic", "ollama").
     * Used for registration in ProviderRegistry.
     * Defaults to decapitalized class name.
     */
    String name() default "";

    /**
     * Provider type (e.g., "text-generation", "embedding", "chat").
     */
    String type() default "text-generation";

    /**
     * Whether this provider is enabled by default.
     */
    boolean enabled() default true;
}

/**
 * Marks a class as an infrastructure provider.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InfrastructureProvider {
    String name() default "";
    String type() default ""; // "local", "docker", "kubernetes"
    boolean enabled() default true;
}

/**
 * Marks a class as a storage provider.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StorageProvider {
    String name() default "";
    String type() default ""; // "local", "s3", "azure"
    boolean enabled() default true;
}

/**
 * Marks a class as a messaging provider.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessagingProvider {
    String name() default "";
    String type() default ""; // "kafka", "rabbitmq", "redis"
    boolean enabled() default true;
}

/**
 * Marks a class as an orchestration provider.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OrchestrationProvider {
    String name() default "";
    boolean enabled() default true;
}

/**
 * Marks a class as a memory provider.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MemoryProvider {
    String name() default "";
    String type() default ""; // "in-memory", "redis", "postgres"
    boolean enabled() default true;
}

/**
 * Marks a class as a queue provider.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueueProvider {
    String name() default "";
    String type() default ""; // "in-memory", "redis", "sqs"
    boolean enabled() default true;
}

/**
 * Marks a class as a tool provider.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ToolProvider {
    String name() default "";
    boolean enabled() default true;
}

/**
 * Marks a class as an evaluation provider.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EvaluationProvider {
    String name() default "";
    boolean enabled() default true;
}
```

**2. Service Annotations (2 types)**

```java
package dev.adeengineer.adentic.boot.annotations.services;

/**
 * Marks a class as a domain-specific service.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DomainService {
    String name() default "";
    String domain() default ""; // "finance", "media", "research"
}

/**
 * Marks a class as an agent lifecycle service.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AgentService {
    String name() default "";
}
```

### Updated Scanner

```java
package dev.adeengineer.adentic.boot.scanner;

import dev.adeengineer.adentic.boot.annotations.providers.*;
import dev.adeengineer.adentic.boot.annotations.services.*;
import dev.adeengineer.adentic.boot.annotations.*;

/**
 * Scans classpath for specialized component annotations.
 *
 * <p>Recognizes:
 * - Provider annotations: @LLMProvider, @InfrastructureProvider, etc.
 * - Service annotations: @DomainService, @AgentService, @Service
 * - Controller annotations: @RestController
 */
@Slf4j
public class ComponentScanner {

    private static final List<Class<? extends Annotation>> COMPONENT_ANNOTATIONS = List.of(
        // Provider annotations
        LLMProvider.class,
        InfrastructureProvider.class,
        StorageProvider.class,
        MessagingProvider.class,
        OrchestrationProvider.class,
        MemoryProvider.class,
        QueueProvider.class,
        ToolProvider.class,
        EvaluationProvider.class,
        // Service annotations
        DomainService.class,
        AgentService.class,
        Service.class,
        // Controller annotations
        RestController.class
    );

    // Existing fields...

    /**
     * Scan for all component types.
     */
    public Set<Class<?>> scan() {
        Set<Class<?>> allComponents = new HashSet<>();

        for (Class<? extends Annotation> annotationType : COMPONENT_ANNOTATIONS) {
            Set<Class<?>> components = scanForAnnotation(annotationType);
            allComponents.addAll(components);
            log.debug("Found {} components with @{}",
                components.size(), annotationType.getSimpleName());
        }

        return allComponents;
    }

    /**
     * Scan for specific annotation type.
     */
    public Set<Class<?>> scanForAnnotation(Class<? extends Annotation> annotationType) {
        // Existing implementation from scan() method
        // ...
    }

    /**
     * Scan for providers only.
     */
    public Map<String, Set<Class<?>>> scanProviders() {
        Map<String, Set<Class<?>>> providersByType = new LinkedHashMap<>();

        providersByType.put("llm", scanForAnnotation(LLMProvider.class));
        providersByType.put("infrastructure", scanForAnnotation(InfrastructureProvider.class));
        providersByType.put("storage", scanForAnnotation(StorageProvider.class));
        providersByType.put("messaging", scanForAnnotation(MessagingProvider.class));
        providersByType.put("orchestration", scanForAnnotation(OrchestrationProvider.class));
        providersByType.put("memory", scanForAnnotation(MemoryProvider.class));
        providersByType.put("queue", scanForAnnotation(QueueProvider.class));
        providersByType.put("tool", scanForAnnotation(ToolProvider.class));
        providersByType.put("evaluation", scanForAnnotation(EvaluationProvider.class));

        return providersByType;
    }

    /**
     * Check if class has any component annotation.
     */
    private boolean isComponent(Class<?> clazz) {
        for (Class<? extends Annotation> annotationType : COMPONENT_ANNOTATIONS) {
            if (clazz.isAnnotationPresent(annotationType)) {
                return true;
            }
        }
        return false;
    }
}
```

### Provider Registry

```java
package dev.adeengineer.adentic.boot.registry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for all providers.
 *
 * <p>Automatically populated during component scanning.
 * Provides type-safe access to providers by category and name.
 */
public class ProviderRegistry {

    private final Map<String, Map<String, Object>> providers = new ConcurrentHashMap<>();

    /**
     * Register a provider.
     */
    public void register(String category, String name, Object provider) {
        providers.computeIfAbsent(category, k -> new ConcurrentHashMap<>())
                 .put(name, provider);
    }

    /**
     * Get provider by category and name.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String category, String name, Class<T> type) {
        Object provider = providers.getOrDefault(category, Map.of()).get(name);
        return type.cast(provider);
    }

    /**
     * Get all providers in a category.
     */
    public Map<String, Object> getAll(String category) {
        return new HashMap<>(providers.getOrDefault(category, Map.of()));
    }

    // Convenience methods for each category

    public Map<String, Object> getLLMProviders() {
        return getAll("llm");
    }

    public Map<String, Object> getInfrastructureProviders() {
        return getAll("infrastructure");
    }

    public Map<String, Object> getStorageProviders() {
        return getAll("storage");
    }

    public Map<String, Object> getMessagingProviders() {
        return getAll("messaging");
    }

    // ... similar for other categories
}
```

---

## Implementation Steps

### Step 1: Create Annotation Package Structure

```
src/main/java/dev/adeengineer/adentic/boot/annotations/
├── providers/
│   ├── LLMProvider.java
│   ├── InfrastructureProvider.java
│   ├── StorageProvider.java
│   ├── MessagingProvider.java
│   ├── OrchestrationProvider.java
│   ├── MemoryProvider.java
│   ├── QueueProvider.java
│   ├── ToolProvider.java
│   └── EvaluationProvider.java
├── services/
│   ├── DomainService.java
│   └── AgentService.java
├── Inject.java (existing)
├── Service.java (existing)
├── RestController.java (existing)
└── AgenticBootApplication.java (existing)
```

### Step 2: Update ComponentScanner

```java
// Update to recognize all new annotation types
// Add scanProviders() method
// Add convenience methods for each provider type
```

### Step 3: Create ProviderRegistry

```java
// New class for provider management
// Auto-populated during startup
// Type-safe provider access
```

### Step 4: Update AgenticContext Integration

```java
public class AgenticApplication {

    public static AgenticApplication run(Class<?> mainClass, String... args) {
        // Existing setup...

        // Scan for components
        ComponentScanner scanner = new ComponentScanner(basePackage);
        Set<Class<?>> components = scanner.scan();

        // Register ProviderRegistry
        ProviderRegistry providerRegistry = new ProviderRegistry();
        context.registerSingleton(ProviderRegistry.class, providerRegistry);

        // Register all components
        for (Class<?> component : components) {
            context.registerBean(component);

            // Auto-register providers in registry
            registerInProviderRegistry(component, providerRegistry, context);
        }

        return app;
    }

    private void registerInProviderRegistry(
            Class<?> componentClass,
            ProviderRegistry registry,
            AgenticContext context) {

        // Check for provider annotations and register accordingly
        if (componentClass.isAnnotationPresent(LLMProvider.class)) {
            LLMProvider anno = componentClass.getAnnotation(LLMProvider.class);
            String name = anno.name().isEmpty()
                ? decapitalize(componentClass.getSimpleName())
                : anno.name();
            Object instance = context.getBean(componentClass);
            registry.register("llm", name, instance);
        }

        // Similar for other provider types...
    }
}
```

---

## Migration Strategy

### Phase 1: Create Annotations (Day 1-2)

**Tasks:**
1. Create 9 provider annotation files
2. Create 2 service annotation files
3. Add Javadoc with examples
4. Add unit tests

**Files Created:**
- 11 new annotation files
- Test suite for annotations

### Phase 2: Update Infrastructure (Day 3-4)

**Tasks:**
1. Update `ComponentScanner` to recognize all annotations
2. Create `ProviderRegistry` class
3. Update `AgenticApplication` to auto-register providers
4. Add integration tests

**Files Modified:**
- `ComponentScanner.java`
- `AgenticApplication.java`
- New: `ProviderRegistry.java`

### Phase 3: Migrate Providers (Day 5-10)

**Tasks:**
1. Update all 20+ provider implementations
2. Remove old `@Component` annotations
3. Add specialized annotations with proper metadata
4. Test each provider after migration

**Provider Migration Checklist:**

**LLM Providers (8 files):**
- [ ] `OpenAITextGenerationProvider` → `@LLMProvider(name = "openai")`
- [ ] `AnthropicTextGenerationProvider` → `@LLMProvider(name = "anthropic")`
- [ ] `OllamaTextGenerationProvider` → `@LLMProvider(name = "ollama")`
- [ ] `GroqTextGenerationProvider` → `@LLMProvider(name = "groq")`
- [ ] `HuggingFaceTextGenerationProvider` → `@LLMProvider(name = "huggingface")`
- [ ] `ReplicateTextGenerationProvider` → `@LLMProvider(name = "replicate")`
- [ ] `TogetherAITextGenerationProvider` → `@LLMProvider(name = "togetherai")`
- [ ] `FireworksAITextGenerationProvider` → `@LLMProvider(name = "fireworksai")`

**Infrastructure Providers (2 files):**
- [ ] `LocalInfrastructureProvider` → `@InfrastructureProvider(name = "local", type = "local")`
- [ ] `DockerInfrastructureProvider` → `@InfrastructureProvider(name = "docker", type = "docker")`

**Messaging Providers (4 files):**
- [ ] `InMemoryMessageBroker` → `@MessagingProvider(name = "in-memory", type = "in-memory")`
- [ ] `KafkaMessageBroker` → `@MessagingProvider(name = "kafka", type = "kafka")`
- [ ] `RabbitMQMessageBroker` → `@MessagingProvider(name = "rabbitmq", type = "rabbitmq")`
- [ ] `RedisMessageBroker` → `@MessagingProvider(name = "redis", type = "redis")`

**Storage Providers (1 file):**
- [ ] `LocalStorageProvider` → `@StorageProvider(name = "local", type = "local")`

**Memory Providers (1 file):**
- [ ] `InMemoryMemoryProvider` → `@MemoryProvider(name = "in-memory", type = "in-memory")`

**Queue Providers (1 file):**
- [ ] `InMemoryTaskQueueProvider` → `@QueueProvider(name = "in-memory", type = "in-memory")`

**Orchestration Providers (1 file):**
- [ ] `SimpleOrchestrationProvider` → `@OrchestrationProvider(name = "simple")`

**Tool Providers (1 file):**
- [ ] `SimpleToolProvider` → `@ToolProvider(name = "simple")`

**Evaluation Providers (1 file):**
- [ ] `LLMEvaluationProvider` → `@EvaluationProvider(name = "llm")`

**Total: 20 providers to migrate**

### Phase 4: Update Documentation (Day 11-12)

**Tasks:**
1. Update developer guide with new annotations
2. Create migration examples
3. Update API documentation
4. Add usage examples for ProviderRegistry

### Phase 5: Testing and Validation (Day 13-14)

**Tasks:**
1. Run full test suite
2. Test provider discovery
3. Test ProviderRegistry functionality
4. Integration tests for all providers
5. Performance testing

---

## Testing Plan

### Unit Tests

```java
@Test
public void testLLMProviderAnnotation() {
    Class<?> clazz = OpenAITextGenerationProvider.class;
    assertTrue(clazz.isAnnotationPresent(LLMProvider.class));

    LLMProvider anno = clazz.getAnnotation(LLMProvider.class);
    assertEquals("openai", anno.name());
    assertEquals("text-generation", anno.type());
}

@Test
public void testComponentScanner_findsAllLLMProviders() {
    ComponentScanner scanner = new ComponentScanner("dev.adeengineer.adentic.providers");
    Set<Class<?>> llmProviders = scanner.scanForAnnotation(LLMProvider.class);

    assertEquals(8, llmProviders.size());
    assertTrue(llmProviders.contains(OpenAITextGenerationProvider.class));
}

@Test
public void testProviderRegistry_autoRegistration() {
    AgenticApplication app = AgenticApplication.run(TestApp.class);
    ProviderRegistry registry = app.getContext().getBean(ProviderRegistry.class);

    Map<String, Object> llmProviders = registry.getLLMProviders();
    assertNotNull(llmProviders.get("openai"));
    assertNotNull(llmProviders.get("anthropic"));
    assertEquals(8, llmProviders.size());
}
```

### Integration Tests

```java
@Test
public void testProviderDiscovery_endToEnd() {
    // Start application
    AgenticApplication app = AgenticApplication.run(TestApp.class);
    ProviderRegistry registry = app.getContext().getBean(ProviderRegistry.class);

    // Verify all provider categories populated
    assertEquals(8, registry.getLLMProviders().size());
    assertEquals(2, registry.getInfrastructureProviders().size());
    assertEquals(4, registry.getMessagingProviders().size());

    // Verify type-safe access
    TextGenerationProvider openai = registry.get(
        "llm", "openai", TextGenerationProvider.class
    );
    assertNotNull(openai);
    assertTrue(openai instanceof OpenAITextGenerationProvider);
}
```

---

## Rollout Plan

### Version Bump

**Current**: 0.2.0-SNAPSHOT
**Next**: 0.3.0 (Breaking change - minor version bump)

### Release Notes

```markdown
## Version 0.3.0 - Specialized Annotations

### Breaking Changes

- Replaced generic `@Component` with specialized provider annotations
- All providers now use type-specific annotations (@LLMProvider, @InfrastructureProvider, etc.)
- ComponentScanner now recognizes 11 specialized annotation types

### Migration Required

All provider implementations must update their annotations:

**Before:**
```java
@Component
public class OpenAITextGenerationProvider { }
```

**After:**

```java
@LLMProvider(name = "openai")
public class OpenAITextGenerationProvider { }
```

### New Features

- **ProviderRegistry**: Central registry for provider discovery
- **Type-safe provider access**: `registry.getLLMProviders()`
- **Metadata support**: Providers include name and type metadata
- **Enhanced scanner**: Specialized scanning methods for each provider type

### Files Changed

- 20+ provider implementations updated
- New: 11 specialized annotation types
- New: ProviderRegistry class
- Updated: ComponentScanner
- Updated: AgenticApplication

See migration guide: doc/migration-guide-v0.3.0.md

```

### Communication Plan

1. **Internal team notification** - 1 week before merge
2. **PR review** - All team members review changes
3. **Documentation update** - Complete before merge
4. **Release** - Tag v0.3.0 and deploy
5. **Follow-up support** - Monitor for issues

---

## Timeline

**Total Duration: 14 days (2 weeks)**

| Phase | Duration | Tasks |
|-------|----------|-------|
| **Phase 1**: Annotations | Days 1-2 | Create 11 annotation files |
| **Phase 2**: Infrastructure | Days 3-4 | Update scanner, create registry |
| **Phase 3**: Migration | Days 5-10 | Update 20 providers |
| **Phase 4**: Documentation | Days 11-12 | Update all docs |
| **Phase 5**: Testing | Days 13-14 | Full validation |

---

## Success Criteria

- [ ] All 11 specialized annotations created
- [ ] ComponentScanner recognizes all annotation types
- [ ] ProviderRegistry functional and tested
- [ ] All 20 providers migrated
- [ ] Zero test failures
- [ ] Documentation complete
- [ ] Migration guide published
- [ ] PR approved by team
- [ ] Version 0.3.0 released

---

**Last Updated:** 2025-10-24
**Version:** 1.0
**Status:** Ready for Implementation
```
