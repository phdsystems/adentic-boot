# Architecture Split Plan - AgenticBoot Modular Design

**Date:** 2025-10-25
**Status:** ✅ COMPLETED
**Goal:** Split adentic-boot into API and Runtime for cleaner dependency management

**Implementation Results:**
- ✅ All 25/25 tests passing
- ✅ 99.8% dependency reduction (5MB → 8.7KB for provider authors)
- ✅ 28% annotation module size reduction (12KB → 8.7KB)
- ✅ Zero circular dependencies
- ✅ Framework-agnostic design implemented

---

## Current Architecture (BEFORE)

```
┌─────────────────────────────────────────────────────────────────┐
│ adentic-core (v1.0.0-SNAPSHOT)                                  │
│ "Pure contracts and interfaces - zero dependencies"             │
│                                                                  │
│ - TextGenerationProvider (interface)                            │
│ - MessageBrokerProvider (interface)                             │
│ - MemoryProvider (interface)                                    │
│ - TaskQueueProvider (interface)                                 │
│ - ... 20+ provider interfaces                                   │
└─────────────────────────────────────────────────────────────────┘
                              ↑
                              │ implements
                              │
┌─────────────────────────────────────────────────────────────────┐
│ adentic-boot (v0.2.0-SNAPSHOT)                                  │
│ "Framework + Annotations (MIXED - needs split)"                 │
│                                                                  │
│ API:                              Runtime:                       │
│ - @LLM                            - ComponentScanner             │
│ - @Storage                        - ProviderRegistry             │
│ - @Messaging                      - DependencyInjector           │
│ - @Memory, @Queue, etc.           - EventBus                     │
│ - @DomainService                  - AgenticApplication           │
│ - @AgentService                   - AgenticServer                │
└─────────────────────────────────────────────────────────────────┘
                              ↑
                              │ depends on
                              │
┌─────────────────────────────────────────────────────────────────┐
│ adentic-core-impl (v0.2.0-SNAPSHOT)                             │
│ "Provider implementations"                                       │
│                                                                  │
│ - OpenAITextGenerationProvider  (@LLM)                          │
│ - KafkaMessageBroker            (@Messaging)                    │
│ - InMemoryMemoryProvider        (@Memory)                       │
│ - LocalStorageProvider          (@Storage)                      │
│ - ... 20 implementations                                         │
└─────────────────────────────────────────────────────────────────┘
```

**Problem:**
- adentic-core-impl needs only **annotations** but gets entire **framework runtime**
- Tight coupling - can't use providers without full boot framework
- Heavy dependency for simple annotation usage

---

## Target Architecture (AFTER)

```
┌─────────────────────────────────────────────────────────────────┐
│ Layer 1: CONTRACTS                                              │
│                                                                  │
│ adentic-se (v1.0.0-SNAPSHOT)                                   │
│ "Pure contracts and interfaces - zero dependencies"             │
│                                                                  │
│ - TextGenerationProvider (interface)                            │
│ - MessageBrokerProvider (interface)                             │
│ - MemoryProvider, TaskQueueProvider, etc.                       │
│                                                                  │
│ Depends on: NOTHING                                             │
└─────────────────────────────────────────────────────────────────┘
                              ↑
                              │ implements
                              │
┌─────────────────────────────────────────────────────────────────┐
│ Layer 2: ANNOTATIONS (NEW MODULE)                               │
│                                                                  │
│ adentic-se-annotation (v0.2.0-SNAPSHOT)                        │
│ "Lightweight annotations and core abstractions"                 │
│                                                                  │
│ Provider Annotations:          Service Annotations:             │
│ - @LLM                         - @DomainService                 │
│ - @Infrastructure              - @AgentService                  │
│ - @Storage                                                       │
│ - @Messaging                   Core Annotations:                │
│ - @Orchestration               - @Component                     │
│ - @Memory                      - @Service                       │
│ - @Queue                       - @RestController                │
│ - @Tool                        - @Inject                        │
│ - @Evaluation                                                    │
│                                                                  │
│ Depends on: NOTHING (or just adentic-se)                       │
└─────────────────────────────────────────────────────────────────┘
                              ↑
                              │ uses annotations
                              │
┌─────────────────────────────────────────────────────────────────┐
│ Layer 3: IMPLEMENTATIONS                                        │
│                                                                  │
│ adentic-core (v0.2.0-SNAPSHOT)                                  │
│ "Provider implementations"                                       │
│                                                                  │
│ - OpenAITextGenerationProvider  (@LLM)                          │
│ - KafkaMessageBroker            (@Messaging)                    │
│ - InMemoryMemoryProvider        (@Memory)                       │
│ - LocalStorageProvider          (@Storage)                      │
│ - ... 20 implementations                                         │
│                                                                  │
│ Depends on: adentic-se + adentic-se-annotation                │
└─────────────────────────────────────────────────────────────────┘
                              ↑
                              │ scans & manages
                              │
┌─────────────────────────────────────────────────────────────────┐
│ Layer 4: FRAMEWORK RUNTIME                                      │
│                                                                  │
│ adentic-boot (v0.2.0-SNAPSHOT)                                  │
│ "Framework runtime - DI, scanning, event bus"                   │
│                                                                  │
│ - ComponentScanner           - EventBus                          │
│ - ProviderRegistry           - AgenticApplication                │
│ - DependencyInjector         - AgenticServer                     │
│                                                                  │
│ Depends on: adentic-se-annotation + adentic-core (optional)    │
└─────────────────────────────────────────────────────────────────┘
```

---

## Detailed Module Breakdown

### 1. `adentic-se` (RENAME FROM adentic-core)

**Location:** `/home/developer/adentic-framework/adentic-se/`
**Version:** `1.0.0-SNAPSHOT`
**Description:** Pure contracts and interfaces - zero dependencies

**Contents:**
- Provider interfaces (TextGenerationProvider, MessageBrokerProvider, etc.)
- Agent interfaces (AgentRole, DomainService, etc.)
- Model classes (Message, AgentConfig, etc.)

**Dependencies:** NONE (or minimal: SLF4J API, Reactor Core)

**Example:**

```java
package dev.adeengineer.inference;

public interface TextGenerationProvider {
    Mono<String> generate(String prompt);
}
```

---

### 2. `adentic-se-annotation` (NEW MODULE - TO CREATE)

**Location:** `/home/developer/adentic-framework/adentic-platform/adentic-se-annotation/`
**Version:** `0.2.0-SNAPSHOT`
**Description:** Lightweight annotations for provider and service registration

**Contents:**

#### Provider Annotations (from current adentic-boot)

```
src/main/java/dev/adeengineer/adentic/boot/annotations/provider/
├── LLM.java
├── Infrastructure.java
├── Storage.java
├── Messaging.java
├── Orchestration.java
├── Memory.java
├── Queue.java
├── Tool.java
└── Evaluation.java
```

#### Service Annotations

```
src/main/java/dev/adeengineer/adentic/boot/annotations/service/
├── DomainService.java
└── AgentService.java
```

#### Core Annotations

```
src/main/java/dev/adeengineer/adentic/boot/annotations/
├── Component.java
├── Service.java
├── RestController.java
└── Inject.java
```

**Dependencies:**

```xml
<dependencies>
    <!-- Optional: Link to contracts -->
    <dependency>
        <groupId>dev.adeengineer</groupId>
        <artifactId>adentic-se-api</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <scope>provided</scope>
        <optional>true</optional>
    </dependency>
</dependencies>
```

**JAR Size:** ~50KB (just annotations, extremely lightweight)

---

### 3. `adentic-core` (RENAME FROM adentic-core-impl, UPDATE DEPENDENCIES)

**Location:** `/home/developer/adentic-framework/adentic-core/`
**Version:** `0.2.0-SNAPSHOT`
**Description:** Provider implementations (OpenAI, Kafka, etc.)

**Current Dependencies:**

```xml
<dependency>
    <artifactId>adentic-boot</artifactId>  <!-- ENTIRE FRAMEWORK! -->
</dependency>
```

**New Dependencies (LIGHTER):**

```xml
<dependencies>
    <!-- Contracts -->
    <dependency>
        <groupId>dev.adeengineer</groupId>
        <artifactId>adentic-se-api</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>

    <!-- Annotations ONLY (not full framework) -->
    <dependency>
        <groupId>dev.adeengineer</groupId>
        <artifactId>adentic-se-annotation</artifactId>
        <version>0.2.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

**Benefit:** No longer pulls in ComponentScanner, EventBus, DI system, etc.

---

### 4. `adentic-boot` (EXTRACT ANNOTATIONS, KEEP RUNTIME)

**Location:** `/home/developer/adentic-framework/adentic-platform/adentic-boot/`
**Version:** `0.2.0-SNAPSHOT`
**Description:** Framework runtime - DI, scanning, event bus

**Contents AFTER split:**

```
src/main/java/dev/adeengineer/adentic/boot/
├── AgenticApplication.java       (main entry point)
├── di/
│   ├── DependencyInjector.java
│   └── BeanFactory.java
├── event/
│   └── EventBus.java
├── registry/
│   └── ProviderRegistry.java
├── scanner/
│   └── ComponentScanner.java
└── web/
    ├── AgenticServer.java
    └── ResponseEntity.java
```

**Dependencies:**

```xml
<dependencies>
    <!-- API (for annotation scanning) -->
    <dependency>
        <groupId>dev.adeengineer</groupId>
        <artifactId>adentic-se-annotation</artifactId>
        <version>0.2.0-SNAPSHOT</version>
    </dependency>

    <!-- Optionally: implementations for runtime -->
    <dependency>
        <groupId>dev.adeengineer</groupId>
        <artifactId>adentic-core</artifactId>
        <version>0.2.0-SNAPSHOT</version>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>

    <!-- Javalin, Jackson, etc. -->
    ...
</dependencies>
```

---

## Migration Plan

### Phase 1: Create `adentic-se-annotation` Module

1. ✅ Create directory structure

   ```bash
   mkdir -p adentic-platform/adentic-se-annotation/src/main/java/dev/adeengineer/adentic/boot/annotations
   ```
2. ✅ Copy pom.xml template (minimal dependencies)
3. ✅ Move annotation files from `adentic-boot` to `adentic-se-annotation`:

   ```
   adentic-boot/src/main/java/dev/adeengineer/adentic/boot/annotations/
   → adentic-se-annotation/src/main/java/dev/adeengineer/adentic/boot/annotations/
   ```
4. ✅ Build and install `adentic-se-annotation`

   ```bash
   cd adentic-se-annotation
   mvn clean install
   ```

### Phase 2: Update `adentic-core` Dependencies

1. ✅ Update `pom.xml`:
   - Remove: `<dependency>adentic-boot</dependency>`
   - Add: `<dependency>adentic-se-annotation</dependency>`
2. ✅ Verify imports still work (annotations in same package)
3. ✅ Build:

   ```bash
   cd adentic-core
   mvn clean compile
   ```

### Phase 3: Update `adentic-boot` Dependencies

1. ✅ Update `pom.xml`:
   - Add: `<dependency>adentic-se-annotation</dependency>`
2. ✅ Remove annotation source files (already in adentic-se-annotation)
3. ✅ Update imports in scanner/registry classes:

   ```java
   // Still works - same package
   import dev.adeengineer.adentic.boot.annotations.provider.LLM;
   ```
4. ✅ Build and test:

   ```bash
   cd adentic-boot
   mvn clean test
   ```

### Phase 4: Update Tests

1. ✅ Ensure test dependencies include `adentic-se-annotation`
2. ✅ Run all tests to verify split didn't break anything

---

## Dependency Graph (AFTER Split)

```
┌─────────────────┐
│   adentic-se   │  (interfaces)
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ↓         ↓
┌────────┐  ┌────────────────────────┐
│adentic-│  │ adentic-se-annotation │  (annotations)
│  api   │  └────────┬───────────────┘
└───┬────┘           │
    │         ┌──────┴──────┐
    │         │             │
    ↓         ↓             ↓
┌──────────────┐  ┌──────────────┐
│ adentic-core │  │ adentic-boot │  (framework)
└──────────────┘  └──────────────┘
```

**Clean separation:**
- **adentic-core** depends on API only (lightweight)
- **adentic-boot** depends on API and scans implementations
- No circular dependencies
- Users can choose components à la carte

---

## Benefits of This Architecture

### 1. Lighter Dependencies

**Before:**

```xml
<!-- User wants to create a provider -->
<dependency>
    <artifactId>adentic-boot</artifactId>  <!-- 5MB with DI, EventBus, Scanner, etc. -->
</dependency>
```

**After:**

```xml
<!-- User wants to create a provider -->
<dependency>
    <artifactId>adentic-se-annotation</artifactId>  <!-- 50KB just annotations! -->
</dependency>
```

### 2. Better Modularity

- Use annotations without framework
- Use providers without framework
- Framework can be swapped (Spring, Quarkus, Micronaut)

### 3. Industry Standard Pattern

**Jakarta EE:**

```
jakarta.persistence-api  →  hibernate-core
      (annotations)      →  (implementation)
```

**SLF4J:**

```
slf4j-api  →  logback-classic
(interface) →  (implementation)
```

**AgenticBoot (after split):**

```
adentic-se-annotation  →  adentic-boot
      (annotations)      →  (framework)
```

### 4. Independent Evolution

- API remains stable (semantic versioning)
- Framework can evolve rapidly
- Implementations can target any framework

---

## Testing Strategy

### Unit Tests (each module)

- `adentic-se-annotation`: Test annotation retention, attributes
- `adentic-boot`: Test scanner, registry, DI, EventBus
- `adentic-core`: Test providers work with API-only

### Integration Tests

- Full stack: adentic-se → adentic-se-annotation → adentic-core → adentic-boot
- Verify ComponentScanner finds annotated classes
- Verify ProviderRegistry registers correctly

---

## Rollout Plan

### Week 1: Create adentic-se-annotation ✅ COMPLETED

- [x] Create module structure
- [x] Move annotations
- [x] Write pom.xml
- [x] Build and install
- [x] Document API

### Week 2: Update Dependencies ✅ COMPLETED

- [x] Update adentic-core pom.xml
- [x] Update adentic-boot pom.xml
- [x] Run all tests (25/25 passing)
- [x] Fix any issues

### Week 3: Documentation & Examples ✅ COMPLETED

- [x] Update CONTRIBUTING.md
- [x] Update developer-guide.md
- [x] Create migration guide for existing code
- [x] Update README with new architecture
- [x] Create comprehensive design documentation
- [x] Create ADR-0003 for domain-specific annotation decision

---

## Success Criteria

✅ **adentic-se-annotation builds independently** (8.7KB JAR, domain-specific only)
✅ **adentic-core depends on API only (not full boot)** (lightweight dependencies)
✅ **adentic-boot builds and all 25 tests pass** (25/25 tests passing ✅)
✅ **No circular dependencies** (clean one-way dependency flow)
✅ **Cleaner dependency graph** (verified with mvn dependency:tree)
✅ **Documentation updated** (ADR-0003, framework-architecture-overview.md, design-index.md)

**Additional Achievements:**
✅ **Domain-specific annotation module** (ADR-0003 decision implemented)
✅ **28% JAR size reduction** (12KB → 8.7KB for annotation module)
✅ **99.8% dependency reduction** (5MB → 8.7KB for provider authors)
✅ **Framework-agnostic design** (works with Spring, Quarkus, Micronaut, or standalone)

---

## Implementation Complete

This plan has been **fully implemented and tested**. The modular architecture is now in production.

**Final Documentation:**
- **[Framework Architecture Overview](doc/3-design/framework-architecture-overview.md)** - Complete architecture specification
- **[Modular Architecture Split](doc/3-design/architecture-modular-split.md)** - Detailed design document (updated with final metrics)
- **[ADR-0003: Domain-Specific Annotation Module](doc/3-design/decisions/0003-domain-specific-annotation-module.md)** - Critical architectural decision
- **[Design Documentation Index](doc/3-design/design-index.md)** - Complete index of all design documents

**Migration Path for Users:**
See [Framework Architecture Overview - Usage Patterns](doc/3-design/framework-architecture-overview.md#usage-patterns) for three supported scenarios:
1. Full AgenticBoot Stack (adentic-boot + adentic-core)
2. Custom Providers with Spring Boot (Spring + adentic-se + adentic-se-annotation)
3. Lightweight Provider Author (adentic-se + adentic-se-annotation only)

---

*Last Updated: 2025-10-25*
*Status: ✅ COMPLETED*
