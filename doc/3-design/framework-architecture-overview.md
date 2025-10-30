# AgenticBoot Framework - Architecture Overview

**Date:** 2025-10-25
**Version:** 2.0 (Post-Modular Refactoring)
**Status:** Implemented

---

## TL;DR

**Architecture**: 4-layer modular design following Jakarta EE pattern. **Layers**: API (contracts) → API-Annotation (domain metadata, 8.7KB) → Core (20+ implementations) → Boot (framework runtime). **Key benefit**: 99% dependency reduction for provider authors (8.7KB vs 5MB). **Pattern**: Framework-agnostic, works with Spring, Quarkus, or standalone.

**Quick decision**: Need providers? → `adentic-core`. Need annotations? → `adentic-se-annotation` (8.7KB). Need full framework? → `adentic-boot`.

---

## Table of Contents

- [Architecture Layers](#architecture-layers)
- [Module Specifications](#module-specifications)
- [Dependency Graph](#dependency-graph)
- [Design Decisions](#design-decisions)
- [Benefits](#benefits)
- [Usage Patterns](#usage-patterns)
- [References](#references)

---

## Architecture Layers

### Layer Overview

```
┌─────────────────────────────────────────────────────────────────┐
│ Layer 1: CONTRACTS (adentic-se)                                │
│ Pure interfaces - zero dependencies                              │
│ TextGenerationProvider, MessageBrokerProvider, etc.              │
│ Version: 1.0.0-SNAPSHOT                                          │
└────────────────────────┬────────────────────────────────────────┘
                         │ implements
    ┌────────────────────┴────────────────────┐
    │                                          │
    ↓                                          ↓
┌─────────────────────────────┐  ┌────────────────────────────────┐
│ Layer 2a: CONTRACTS         │  │ Layer 2b: ANNOTATIONS           │
│ (adentic-se)               │  │ (adentic-se-annotation)        │
│ Interfaces, Models          │  │ Domain-Specific Only            │
│ 1.0.0-SNAPSHOT              │  │ 0.2.0-SNAPSHOT | 8.7KB          │
│                             │  │                                 │
│                             │  │ Provider Annotations:            │
│                             │  │ @LLM, @Messaging, @Storage      │
│                             │  │ @Memory, @Queue, @Tool          │
│                             │  │                                 │
│                             │  │ Service Annotations:             │
│                             │  │ @DomainService, @AgentService   │
└─────────────┬───────────────┘  └────────────┬───────────────────┘
              │                               │
              └───────────┬───────────────────┘
                          │ uses
    ┌─────────────────────┴─────────────────────┐
    │                                            │
    ↓                                            ↓
┌──────────────────────────────┐  ┌────────────────────────────────┐
│ Layer 3: IMPLEMENTATIONS      │  │ Layer 4: FRAMEWORK RUNTIME      │
│ (adentic-core)                │  │ (adentic-boot)                  │
│ 0.2.0-SNAPSHOT                │  │ 0.2.0-SNAPSHOT                  │
│                               │  │                                 │
│ 20+ Provider Implementations: │  │ Framework Components:           │
│ - OpenAITextGeneration        │  │ - ComponentScanner              │
│ - KafkaMessageBroker          │  │ - ProviderRegistry              │
│ - RedisMessageBroker          │  │ - DependencyInjector            │
│ - InMemoryMemoryProvider      │  │ - EventBus                      │
│ - LocalStorageProvider        │  │ - AgenticServer (HTTP)          │
│ - Docker Infrastructure       │  │ - BeanFactory                   │
│ - FineTuningService           │  │                                 │
│ - WorkflowEngine              │  │ Framework Annotations:          │
│ - RateLimiters                │  │ @Component, @Service            │
│                               │  │ @RestController, @Inject        │
│                               │  │ @AgenticBootApplication         │
│                               │  │                                 │
│ Depends on: adentic-se +     │  │ Depends on: adentic-se-        │
│             adentic-se-      │  │             annotation +        │
│             annotation        │  │             adentic-core (opt)  │
└───────────────────────────────┘  └─────────────────────────────────┘
```

---

## Module Specifications

### 1. adentic-se (Layer 1: Contracts)

**Purpose:** Pure contracts and interfaces - zero dependencies

**Location:** `/home/developer/adentic-framework/adentic-se/`

**Version:** 1.0.0-SNAPSHOT

**JAR Size:** ~200KB

**Contents:**
- Provider interfaces: `TextGenerationProvider`, `MessageBrokerProvider`, `MemoryProvider`, etc.
- Agent interfaces: `AgentRole`, `DomainService`
- Model classes: `Message`, `AgentConfig`, `TaskResult`

**Dependencies:** Minimal (SLF4J API, Reactor Core)

**Example:**

```java
package dev.adeengineer.api.inference;

public interface TextGenerationProvider {
    Mono<String> generate(String prompt);
}
```

---

### 2. adentic-se-annotation (Layer 2: Domain Annotations)

**Purpose:** Lightweight domain-specific annotations ONLY

**Location:** `/home/developer/adentic-framework/adentic-platform/adentic-se-annotation/`

**Version:** 0.2.0-SNAPSHOT

**JAR Size:** **8.7KB** (domain-only, 28% reduction from 12KB)

**Contents:**

**Provider Annotations (9):**
- @LLM - LLM provider registration
- @Infrastructure - Infrastructure provider
- @Storage - Storage provider
- @Messaging - Message broker provider
- @Orchestration - Orchestration provider
- @Memory - Memory provider
- @Queue - Queue provider
- @Tool - Tool provider
- @Evaluation - Evaluation provider

**Service Annotations (2):**
- @DomainService - Domain service marker
- @AgentService - Agent service marker

**Dependencies:** ZERO (framework-agnostic)

**Example:**

```java
package dev.adeengineer.adentic.boot.annotations.provider;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LLM {
    String name() default "";
    String type() default "text-generation";
    boolean enabled() default true;
}
```

**Design Decision:** Domain-specific only (see [ADR-0003](decisions/0003-domain-specific-annotation-module.md))

---

### 3. adentic-core (Layer 3: Implementations)

**Purpose:** Provider and service implementations

**Location:** `/home/developer/adentic-framework/adentic-core/`

**Version:** 0.2.0-SNAPSHOT

**Contents:**
- **20+ Provider Implementations:**
- OpenAI, Anthropic LLM providers
- Kafka, Redis, RabbitMQ message brokers
- In-memory storage, memory providers
- Docker infrastructure provider

- **Service Implementations:**
  - FineTuningService
  - WorkflowEngine
  - RetryPolicy implementations
  - RateLimiters

**Dependencies:**

```xml
<dependencies>
    <dependency>
        <groupId>dev.adeengineer</groupId>
        <artifactId>adentic-se-api</artifactId>           <!-- Contracts -->
    </dependency>
    <dependency>
        <groupId>dev.adeengineer</groupId>
        <artifactId>adentic-se-annotation</artifactId> <!-- Annotations ONLY -->
    </dependency>
    <!-- Provider-specific: OpenAI SDK, Kafka client, etc. -->
</dependencies>
```

**Before Refactoring:** Depended on entire `adentic-boot` framework (5MB)
**After Refactoring:** Depends on annotations only (8.7KB)
**Benefit:** 99% dependency reduction

**Example Tool Provider:**

```java
@Tool(
    name = "doc-link-verifier",
    description = "Verifies markdown cross-references",
    category = "documentation"
)
@Component
public class DocumentationLinkVerifierTool {
    public Mono<LinkVerificationResult> verifyFile(String path) {
        // Validates file links, HTTP links, anchors
    }
}
```

**See:** [Documentation Link Verifier Tool](../../tool/documentation-link-verifier/README.md) - Complete real-world Tool Provider example

---

### 4. adentic-boot (Layer 4: Framework Runtime)

**Purpose:** Framework runtime - DI, scanning, event bus, HTTP server

**Location:** `/home/developer/adentic-framework/adentic-platform/adentic-boot/`

**Version:** 0.2.0-SNAPSHOT

**Contents:**

**Framework Components:**
- `ComponentScanner` - Discovers @LLM, @Messaging, @Component annotated classes
- `ProviderRegistry` - Manages providers by category
- `DependencyInjector` - Simple DI container
- `EventBus` - In-process event system
- `AgenticServer` - Lightweight HTTP server (Javalin)
- `BeanFactory` - Bean lifecycle management

**Framework Annotations (moved from adentic-se-annotation):**
- @Component
- @Service
- @RestController
- @Inject
- @AgenticBootApplication

**Bean Scopes:**
- **Default:** All beans are singletons (single instance per application)
- **No scope annotations:** We do NOT provide @Singleton, @ApplicationScoped, @RequestScoped, @SessionScoped
- **Rationale:** 95% of Adentic components are stateless singletons (LLM providers, message brokers, storage)
- **For complex scoping:** Use Spring (@RequestScope) or Quarkus (@RequestScoped) with Adentic domain annotations
- **See:** [ADR-0003, Section 7](decisions/0003-domain-specific-annotation-module.md#7-bean-scopes-and-lifecycle-management) for complete decision rationale

**Dependencies:**

```xml
<dependencies>
    <dependency>
        <groupId>dev.adeengineer</groupId>
        <artifactId>adentic-se-annotation</artifactId>  <!-- For scanning -->
    </dependency>
    <dependency>
        <groupId>dev.adeengineer</groupId>
        <artifactId>adentic-core</artifactId>            <!-- Optional, runtime -->
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>
    <!-- Javalin, Jackson, Logback -->
</dependencies>
```

**Test Results:** 25/25 tests passing ✅

---

## Dependency Graph

### Clean One-Way Dependencies

```
┌─────────────────┐
│   adentic-se   │  (interfaces)
│ 1.0.0-SNAPSHOT  │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ↓         ↓
┌────────┐  ┌────────────────────────┐
│adentic-│  │ adentic-se-annotation │  (annotations, 8.7KB)
│  api   │  │    0.2.0-SNAPSHOT      │
└───┬────┘  └────────┬───────────────┘
    │                │
    │         ┌──────┴──────┐
    │         │             │
    ↓         ↓             ↓
┌──────────────┐  ┌──────────────┐
│ adentic-core │  │ adentic-boot │  (framework)
│ 0.2.0-SNSHOT │  │ 0.2.0-SNSHOT │
└──────────────┘  └──────────────┘
```

**Benefits:**
- ✅ No circular dependencies
- ✅ Clear dependency flow
- ✅ Each layer can evolve independently
- ✅ Users can choose components à la carte

---

## Design Decisions

### ADR-0003: Domain-Specific Annotation Module

**Decision:** Keep `adentic-se-annotation` domain-specific (Agentic concepts only)

**Rationale:**
1. Clear separation: domain vs framework concerns
2. Framework-agnostic: works with Spring, Quarkus, Micronaut
3. Avoid scope creep: focus on Agentic value-add
4. Industry alignment: follows Jakarta EE pattern
5. Smaller dependencies: 8.7KB vs 12KB

**See:** [doc/3-design/decisions/0003-domain-specific-annotation-module.md](decisions/0003-domain-specific-annotation-module.md)

### Modular Architecture Split

**Decision:** Split monolithic adentic-boot into 4 layers

**Rationale:**
1. Lighter dependencies for provider authors (99% reduction)
2. Framework-agnostic design
3. Industry alignment (Jakarta EE, SLF4J patterns)
4. Independent evolution of layers

**See:** [architecture-modular-split.md](architecture-modular-split.md)

---

## Benefits

### 1. Lightweight Dependencies (99% Reduction)

**Before:**

```xml
<!-- Provider author -->
<dependency>
    <artifactId>adentic-boot</artifactId>  <!-- 5MB framework -->
</dependency>
```

**After:**

```xml
<!-- Provider author -->
<dependency>
    <artifactId>adentic-se-annotation</artifactId>  <!-- 8.7KB -->
</dependency>
```

**Savings:** 5MB → 8.7KB

### 2. Framework-Agnostic Design

**Spring Boot:**

```java
@Component              // Spring
@LLM(name = "openai")   // Adentic
public class OpenAIProvider implements TextGenerationProvider { }
```

**Quarkus:**

```java
@ApplicationScoped      // Quarkus
@LLM(name = "openai")   // Adentic
public class OpenAIProvider implements TextGenerationProvider { }
```

**Pure AgenticBoot:**

```java
@Component              // adentic-boot
@LLM(name = "openai")   // adentic-se-annotation
public class OpenAIProvider implements TextGenerationProvider { }
```

### 3. Industry Alignment

|       Pattern       |               Industry Example               |               AgenticBoot                |
|---------------------|----------------------------------------------|------------------------------------------|
| API Separation      | `jakarta.persistence-api` → `hibernate-core` | `adentic-se-annotation` → `adentic-core` |
| Interface/Impl      | `slf4j-api` → `logback-classic`              | `adentic-se` → `adentic-core`            |
| Annotation Scanning | `@Controller` (Spring)                       | `@LLM` (Adentic)                         |

### 4. Independent Evolution

|         Layer         | Stability |  Release Frequency  | Breaking Changes  |
|-----------------------|-----------|---------------------|-------------------|
| adentic-se            | High      | Rare (semver major) | Avoid             |
| adentic-se-annotation | High      | Rare                | Avoid             |
| adentic-core          | Medium    | Regular             | Non-breaking      |
| adentic-boot          | Low       | Frequent            | Framework updates |

### 5. Singleton-Only Simplicity

**Default Behavior:** All components are singletons (single instance per application)

**Why no scope annotations (@RequestScoped, @SessionScoped)?**
- 95% of Adentic components are stateless singletons:
- LLM providers (OpenAI, Anthropic) - singleton
- Message brokers (Kafka, Redis) - singleton
- Storage providers - singleton
- Memory providers - singleton
- Avoids reimplementing complex CDI lifecycle management (proxy generation, thread-local context)
- Users needing complex scoping can use Spring/Quarkus

**Example: Mixing scopes when needed:**

```java
@RequestScope              // Spring handles scoping
@LLM(name = "openai")      // Adentic handles provider registration
public class RequestScopedProvider implements TextGenerationProvider {
    // New instance per HTTP request
}
```

**Benefits:**
- ✅ Simpler DependencyInjector (no proxy generation, no thread-local management)
- ✅ No feature parity expectations (@PostConstruct, @PreDestroy, @Transactional)
- ✅ Focus on Agentic value-add, not generic container features
- ✅ Users leverage mature scoping from Spring/Quarkus when needed

**See:** [ADR-0003, Section 7](decisions/0003-domain-specific-annotation-module.md#7-bean-scopes-and-lifecycle-management)

---

## Usage Patterns

### Pattern 1: Full AgenticBoot Stack

```xml
<dependencies>
    <!-- Complete framework -->
    <dependency>
        <groupId>dev.adeengineer</groupId>
        <artifactId>adentic-boot</artifactId>
        <version>0.2.0-SNAPSHOT</version>
    </dependency>

    <!-- Built-in providers -->
    <dependency>
        <groupId>dev.adeengineer</groupId>
        <artifactId>adentic-core</artifactId>
        <version>0.2.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

**Use Case:** Startup, prototyping, want everything Adentic provides

---

### Pattern 2: Custom Providers (Spring Boot)

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>

    <!-- Adentic contracts -->
    <dependency>
        <groupId>dev.adeengineer</groupId>
        <artifactId>adentic-se-api</artifactId>
    </dependency>

    <!-- Adentic domain annotations -->
    <dependency>
        <groupId>dev.adeengineer</groupId>
        <artifactId>adentic-se-annotation</artifactId>
    </dependency>

    <!-- Your custom provider implementation -->
</dependencies>
```

**Use Case:** Enterprise with custom LLM integration, using Spring Boot

---

### Pattern 3: Lightweight Provider Author

```xml
<dependencies>
    <!-- Minimal dependencies -->
    <dependency>
        <groupId>dev.adeengineer</groupId>
        <artifactId>adentic-se-api</artifactId>        <!-- Contracts -->
    </dependency>
    <dependency>
        <groupId>dev.adeengineer</groupId>
        <artifactId>adentic-se-annotation</artifactId>  <!-- 8.7KB! -->
    </dependency>

    <!-- Provider-specific SDK (e.g., Azure OpenAI) -->
</dependencies>
```

**Use Case:** Third-party provider developer, minimal dependencies

---

## References

### Design Documents

- **[architecture-modular-split.md](architecture-modular-split.md)** - Complete modular architecture specification
- **[ADR-0003](decisions/0003-domain-specific-annotation-module.md)** - Domain-specific annotation decision
- **[ARCHITECTURE_SPLIT_PLAN.md](../../ARCHITECTURE_SPLIT_PLAN.md)** - Original migration plan

### Related Documentation

- **[doc/4-development/coding-standards.md](../4-development/coding-standards.md)** - Naming conventions (singular packages, short annotations)
- **[doc/CONTRIBUTING.md](../CONTRIBUTING.md)** - Contribution guidelines

### Industry Standards

- **Jakarta EE:** https://jakarta.ee/specifications/
- **SLF4J:** https://www.slf4j.org/manual.html
- **Spring Framework:** https://spring.io/projects/spring-framework

---

## Metrics

|          Metric          |                               Value                               |
|--------------------------|-------------------------------------------------------------------|
| Total Modules            | 4 (adentic-se, adentic-se-annotation, adentic-core, adentic-boot) |
| Smallest Module          | adentic-se-annotation (8.7KB)                                     |
| Domain Annotations       | 11 (9 providers + 2 services)                                     |
| Provider Implementations | 20+                                                               |
| Tests Passing            | 25/25 ✅                                                           |
| Dependency Reduction     | 99% (5MB → 8.7KB for provider authors)                            |
| JAR Size Reduction       | 28% (12KB → 8.7KB for annotation module)                          |

---

*Last Updated: 2025-10-25*
*Version: 2.0 (Post-Modular Refactoring)*
*Status: Implemented and Tested*
