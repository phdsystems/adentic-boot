# ADR-0003: Domain-Specific Annotation Module

**Date:** 2025-10-25
**Status:** Accepted
**Deciders:** Architecture Team

---

## Context

During the modular architecture split (creating `adentic-se-annotation` as a lightweight annotation module), we needed to decide which annotations belong in the shared annotation module vs the framework runtime.

**Initial State:**
`adentic-se-annotation` contained both:
- **Domain-specific annotations** - @LLM, @Messaging, @Storage, @DomainService, @AgentService (Agentic concepts)
- **Generic framework annotations** - @Component, @Service, @RestController, @Inject (generic DI/web concepts)

**Key Questions:**
1. Should `adentic-se-annotation` be domain-specific or a complete framework replacement?
2. Should we provide generic DI/web annotations or rely on existing frameworks?
3. How do we balance framework-agnosticism with completeness?

---

## Decision

**We decided to make `adentic-se-annotation` a DOMAIN-SPECIFIC module containing ONLY Agentic concepts.**

**Moved to `adentic-boot`:**
- @Component
- @Service
- @RestController
- @Inject
- @AgenticBootApplication

**Kept in `adentic-se-annotation`:**
- Provider annotations: @LLM, @Infrastructure, @Storage, @Messaging, @Orchestration, @Memory, @Queue, @Tool, @Evaluation
- Service annotations: @DomainService, @AgentService

---

## Rationale

### 1. Clear Separation of Concerns

**Domain vs Framework:**

```
adentic-se-annotation (DOMAIN)     adentic-boot (FRAMEWORK)
├── @LLM                            ├── @Component
├── @Messaging                      ├── @Service
├── @Storage                        ├── @RestController
├── @DomainService                  ├── @Inject
└── @AgentService                   └── @AgenticBootApplication
```

**Benefits:**
- ✅ Clear purpose: "What is this module for?" → "Agentic domain concepts"
- ✅ Easy to explain: "Annotations for LLM providers, messaging, storage, etc."
- ❌ Confusing if mixed: "Is this for Agentic or general DI?"

### 2. Framework-Agnostic Design

Users can choose their preferred framework for generic concerns:

**Spring Boot:**

```java
@Component              // ← Spring's annotation
@LLM(name = "openai")   // ← Adentic's domain annotation
public class OpenAIProvider implements TextGenerationProvider { }
```

**Quarkus:**

```java
@ApplicationScoped      // ← Quarkus's annotation
@LLM(name = "openai")   // ← Adentic's domain annotation
public class OpenAIProvider implements TextGenerationProvider { }
```

**Pure AgenticBoot:**

```java
@Component              // ← adentic-boot's annotation
@LLM(name = "openai")   // ← adentic-se-annotation's domain annotation
public class OpenAIProvider implements TextGenerationProvider { }
```

**Benefits:**
- ✅ Works with Spring, Quarkus, Micronaut
- ✅ Users can mix: Adentic domain + their preferred DI framework
- ✅ Doesn't lock users into AgenticBoot's DI system

### 3. Industry Alignment

**Jakarta EE Pattern:**

```
jakarta.persistence-api     → hibernate-core (NOT: javax.component-api)
    (@Entity, @Table)       → (implementation)
```

Jakarta provides **domain-specific** annotations (JPA concepts), not generic DI annotations.

**Spring Framework:**

```
spring-data-jpa             → Uses Spring's @Component (NOT: custom @JpaComponent)
    (@Repository, @Query)   → Uses existing DI framework
```

Spring Data provides **domain-specific** annotations for data access, relies on core Spring for DI.

**AgenticBoot (this decision):**

```
adentic-se-annotation      → adentic-core (implementations)
    (@LLM, @Messaging)      → (OpenAI, Kafka providers)

Generic DI:
    @Component, @Service    → adentic-boot (OR Spring/Quarkus)
```

**Lesson:** Successful frameworks focus on domain value-add, not reimplementing DI.

### 4. Avoid Scope Creep

**If we keep generic annotations in adentic-se-annotation:**

Users expect feature parity with Spring/Jakarta:
- ❌ Need @Transactional → implement transaction management
- ❌ Need @Configuration → implement bean configuration
- ❌ Need @Scheduled → implement task scheduling
- ❌ Need @Async → implement async execution
- ❌ Need @Validated → implement validation framework

**Result:** Competing with Spring Boot (reinventing the wheel)

**If we keep domain-specific only:**

Clear scope:
- ✅ Focus on LLM orchestration
- ✅ Focus on agentic workflows
- ✅ Focus on provider abstraction
- ✅ Let Spring/Quarkus handle generic concerns

**Result:** Focused, maintainable, valuable

### 5. Smaller JAR Size

**Before (mixed):**
- 16 annotation files
- 12KB JAR
- Contains both domain and framework concepts

**After (domain-only):**
- 11 annotation files
- **8.7KB JAR** (28% reduction!)
- Contains only Agentic concepts

**Benefits:**
- ✅ Faster downloads
- ✅ Smaller classpath
- ✅ Clearer dependency graph

### 6. Focus on Value-Add

**Where Adentic adds value:**
- ✅ **@LLM** - LLM provider discovery and abstraction
- ✅ **@Messaging** - Message broker abstraction (Kafka, Redis, RabbitMQ)
- ✅ **@Storage** - Storage provider abstraction
- ✅ **@DomainService** - Domain service orchestration
- ✅ **@AgentService** - Agent service registration

**Where Spring/Quarkus already excel:**
- ❌ @Component - Solved problem
- ❌ @Service - Solved problem
- ❌ @RestController - Solved problem
- ❌ @Inject - Solved problem (Jakarta CDI)

**Decision:** Focus resources on what makes Adentic unique.

### 7. Bean Scopes and Lifecycle Management

**Question:** Should we include scope annotations (@Singleton, @ApplicationScoped, @RequestScoped, @SessionScoped)?

**Decision:** **NO** - We will NOT include scope management annotations.

**Rationale:**

**Scoping is a solved problem:**
- Spring: `@Scope("singleton")`, `@RequestScope`, `@SessionScope`
- Quarkus/CDI: `@ApplicationScoped`, `@RequestScoped`, `@SessionScoped`, `@Dependent`
- Jakarta EE: Complete CDI specification with lifecycle management

**Our use cases don't require complex scoping:**
- 95% of Adentic components are stateless singletons
- LLM providers (OpenAI, Anthropic) - singleton
- Message brokers (Kafka, Redis) - singleton
- Storage providers - singleton
- Memory providers - singleton
- Request-scoped state is rare in backend AI orchestration

**Default behavior: Everything is a singleton**

```java
@Component           // Implicitly singleton (no annotation needed)
@LLM(name = "openai")
public class OpenAIProvider implements TextGenerationProvider {
    // Single instance, shared across application
}
```

**Users who need scoping should use their framework:**

**Spring Boot:**

```java
@RequestScope              // Spring handles scoping
@LLM(name = "openai")      // Adentic handles provider registration
public class RequestScopedProvider implements TextGenerationProvider {
    // New instance per HTTP request
}
```

**Quarkus:**

```java
@RequestScoped             // Quarkus/CDI handles scoping
@LLM(name = "openai")      // Adentic handles provider registration
public class RequestScopedProvider implements TextGenerationProvider {
    // New instance per HTTP request
}
```

**Benefits of NOT including scope management:**
- ✅ Avoid reinventing complex CDI lifecycle management
- ✅ Users can use their framework's mature scoping implementation
- ✅ Simpler DependencyInjector (singleton-only is trivial)
- ✅ Focus on Agentic value-add, not generic container features
- ✅ No feature parity expectations (@Transactional, @PostConstruct, @PreDestroy, etc.)

**What about adentic-boot standalone users?**
- Default singleton behavior is sufficient for 95% of use cases
- If they need complex scoping, they should use Spring/Quarkus instead
- adentic-boot is intentionally simple (not a full CDI replacement)

**Scope creep we're avoiding:**
- ❌ @Singleton, @ApplicationScoped, @RequestScoped, @SessionScoped
- ❌ @Dependent (prototype scope)
- ❌ @PostConstruct, @PreDestroy (lifecycle callbacks)
- ❌ @ConversationScoped, @ViewScoped (web-specific)
- ❌ Custom scope implementations
- ❌ Proxy generation for request/session scopes
- ❌ Thread-local context management

**Decision:** Delegate scope management to existing frameworks. Focus on domain value-add.

---

## Consequences

### Positive

1. **✅ Clear Module Purpose**
   - `adentic-se-annotation` = "Agentic domain concepts"
   - `adentic-boot` = "Framework runtime (optional)"
2. **✅ Framework Agnostic**
   - Works seamlessly with Spring Boot
   - Works seamlessly with Quarkus
   - Works seamlessly with Micronaut
   - Can use adentic-boot standalone
3. **✅ Smaller Dependencies**
   - 8.7KB annotation module (was 12KB)
   - Only domain concepts, nothing extra
4. **✅ Industry Aligned**
   - Follows Jakarta EE pattern (domain-specific APIs)
   - Follows Spring Data pattern (domain + framework)
5. **✅ Focused Scope**
   - No scope creep into generic DI/web concerns
   - Clear value proposition: Agentic orchestration
6. **✅ Maintainability**
   - Smaller surface area to maintain
   - Clear separation of responsibilities
7. **✅ No Scope Management Complexity**
   - Default singleton behavior (simple, sufficient for 95% of use cases)
   - Users can leverage Spring/Quarkus scoping when needed
   - Avoids reimplementing complex CDI lifecycle (@PostConstruct, @PreDestroy, proxy generation)
   - Simpler DependencyInjector implementation
   - No feature parity expectations with Spring/Jakarta

### Negative

1. **⚠️ Users Need Two Annotations**

   ```java
   @Component              // Framework annotation (Spring/Quarkus/adentic-boot)
   @LLM(name = "openai")   // Domain annotation (adentic-se-annotation)
   ```

   **Mitigation:** Standard pattern in Spring ecosystem (e.g., @Component + @Transactional)

2. **⚠️ adentic-boot Users Need Framework Annotations**

   **Mitigation:** adentic-boot provides @Component, @Service, etc. in its source code

3. **⚠️ Learning Curve**

   Users must understand: "Use @LLM for provider, use @Component (from framework) for DI"

   **Mitigation:** Clear documentation and examples

4. **⚠️ No Built-in Scope Management**

   adentic-boot standalone users have only singleton scope (no @RequestScoped, @SessionScoped, etc.)

   **Mitigation:**
   - Singleton is sufficient for 95% of Agentic use cases (stateless providers)
   - Users needing complex scoping should use Spring/Quarkus
   - Framework-agnostic design allows mixing frameworks (Spring scoping + Adentic providers)

---

## Implementation

### Phase 1: Move Generic Annotations ✅

```bash
# Moved from adentic-se-annotation to adentic-boot
mv Component.java Service.java RestController.java Inject.java AgenticBootApplication.java \
   adentic-boot/src/main/java/dev/adeengineer/adentic/boot/annotations/
```

### Phase 2: Rebuild Modules ✅

```bash
# Rebuild adentic-se-annotation (domain-only)
cd adentic-se-annotation
mvn clean install
# Result: 8.7KB JAR with 11 domain annotations

# Rebuild adentic-boot (with local annotations)
cd adentic-boot
mvn clean test
# Result: All 25 tests passing
```

### Phase 3: Update Documentation ✅

Created this ADR documenting the decision and rationale.

---

## Alternatives Considered

### Alternative 1: Keep All Annotations in adentic-se-annotation

**Pros:**
- Single annotation module
- Users only need one dependency

**Cons:**
- ❌ Mixed domain/framework concerns
- ❌ Scope creep (expected @Transactional, @Configuration, etc.)
- ❌ Not framework-agnostic
- ❌ Competing with Spring Boot
- ❌ Larger JAR (12KB vs 8.7KB)

**Rejected:** Violates separation of concerns

### Alternative 2: Create Three Modules (api-annotation-domain, api-annotation-framework, api-annotation)

```
adentic-se-annotation-domain      (domain annotations)
adentic-se-annotation-framework   (generic framework annotations)
adentic-se-annotation             (uber module, depends on both)
```

**Pros:**
- Maximum separation
- Users can choose domain-only or both

**Cons:**
- ❌ Over-engineering
- ❌ More modules to maintain
- ❌ Confusing dependency structure

**Rejected:** Unnecessary complexity

### Alternative 3: Remove Generic Annotations Entirely

Don't provide @Component, @Service, etc. at all. Force users to use Spring/Quarkus.

**Pros:**
- Simplest approach
- Forces framework-agnostic design

**Cons:**
- ❌ adentic-boot can't function standalone
- ❌ Loses "Spring Boot replacement" value proposition

**Rejected:** Reduces adentic-boot's usefulness

### Alternative 4: Include Scope Annotations (@Singleton, @ApplicationScoped, @RequestScoped)

Add scope management annotations to adentic-boot or adentic-se-annotation.

**Pros:**
- More feature-complete DI container
- Users can control bean lifecycle
- Request-scoped and session-scoped beans possible

**Cons:**
- ❌ Scope creep - Complex CDI lifecycle management (proxy generation, thread-local context)
- ❌ Not our value-add - Spring/Quarkus already provide mature scoping
- ❌ Feature parity expectations - Users would expect @PostConstruct, @PreDestroy, @Transactional
- ❌ Unnecessary complexity - 95% of Agentic components are stateless singletons
- ❌ Competing with Jakarta CDI specification

**Rejected:** Singleton-only is sufficient for our use cases. Users needing complex scoping should use Spring/Quarkus.

---

## Related Decisions

- **ADR-0001:** Adopt Spotless Code Formatter
- **Architecture Split Plan:** Modular architecture design (adentic-se, adentic-se-annotation, adentic-core, adentic-boot)

---

## References

### Industry Patterns

1. **Jakarta EE**
   - `jakarta.persistence-api` - Domain-specific (@Entity, @Table)
   - `jakarta.inject` - Generic DI (@Inject, @Singleton)
   - Pattern: Separate domain and framework concerns
2. **Spring Data**
   - `spring-data-jpa` - Domain-specific (@Repository, @Query)
   - `spring-context` - Generic DI (@Component, @Service)
   - Pattern: Domain modules rely on core framework for DI
3. **Quarkus**
   - `quarkus-hibernate-orm` - Domain-specific (JPA annotations)
   - `quarkus-arc` - Generic DI (@ApplicationScoped)
   - Pattern: Extensions provide domain value, Arc provides DI

### Best Practices

- **Single Responsibility Principle:** Module should have one clear purpose
- **Open/Closed Principle:** Open for extension (new providers), closed for modification (framework concerns)
- **Dependency Inversion:** Depend on abstractions (use Spring/Quarkus), not concretions (custom DI)

---

## Metrics

**Before Refactoring:**
- adentic-se-annotation: 16 files, 12KB
- Mixed domain + framework annotations

**After Refactoring:**
- adentic-se-annotation: 11 files, **8.7KB** (domain-only)
- adentic-boot: +5 annotation files (framework annotations)
- **28% size reduction** in annotation module
- **100% test pass rate** (25/25 tests)

---

## Conclusion

By keeping `adentic-se-annotation` focused on **domain-specific Agentic concepts** and moving generic framework annotations to `adentic-boot`, we achieved:

✅ **Clear separation of concerns** - Domain vs framework
✅ **Framework-agnostic design** - Works with Spring, Quarkus, Micronaut
✅ **Industry alignment** - Follows Jakarta EE and Spring patterns
✅ **Smaller dependencies** - 8.7KB vs 12KB
✅ **Focused scope** - No scope creep into generic DI/web/lifecycle management
✅ **Maintainability** - Clear module purposes
✅ **Singleton-only simplicity** - No complex scope management (sufficient for 95% of use cases)

This decision positions `adentic-se-annotation` as a lightweight, focused module that provides value through Agentic domain abstractions, not by reimplementing solved problems like dependency injection or bean lifecycle management.

**Key architectural principles:**
1. **Domain-specific only** - Annotations for LLM, messaging, storage, memory providers
2. **No generic DI** - Users bring their own framework (@Component from Spring/Quarkus/adentic-boot)
3. **No scope management** - Default singleton behavior, delegate complex scoping to mature frameworks
4. **Focus on value-add** - Agentic orchestration, not generic container features

---

*Last Updated: 2025-10-25*
*Status: Implemented and Tested*
