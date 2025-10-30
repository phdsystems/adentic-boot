# Coding Standards - AgenticBoot Framework

**Date:** 2025-10-25
**Version:** 2.0

---

## TL;DR

**Key principles**: Follow industry standards from Spring, Jakarta EE, and Quarkus. **Package naming**: Singular nouns (provider not providers, service not services). **Annotation naming**: Short and concise - let package provide context (@LLM not @LLMProvider, @Storage not @StorageProvider). **Industry pattern**: Spring uses @Controller/@Service, Jakarta uses @Entity/@Inject, Quarkus follows same conventions.

**Quick rules**: Package = singular → annotation = concise → framework feels professional.

---

## Framework Package Naming Conventions

### Singular Package Names (Java Best Practice)

**RULE: Use singular nouns for all package names**

Following established Java conventions and industry standards (Oracle, Spring, Jakarta EE), all packages should use singular nouns to represent categories.

**Why Singular?**
- ✅ **Java Naming Conventions**: Oracle's official Java guidelines recommend singular package names
- ✅ **Industry Standard**: Spring (`org.springframework.stereotype`, not `stereotypes`), Jakarta EE (`jakarta.persistence`, not `persistences`)
- ✅ **Semantic Clarity**: Package represents a category/type, not a collection
- ✅ **Grammar**: "provider package" reads better than "providers package"

**Examples from Industry:**

|    Framework     |            Package Structure             |                 Pattern                  |
|------------------|------------------------------------------|------------------------------------------|
| Spring Framework | `org.springframework.stereotype`         | Singular (stereotype, not stereotypes)   |
| Spring Framework | `org.springframework.context.annotation` | Singular (annotation, not annotations)   |
| Jakarta EE       | `jakarta.persistence`                    | Singular (persistence, not persistences) |
| Jakarta EE       | `jakarta.validation.constraint`          | Singular (constraint, not constraints)   |
| Quarkus          | `io.quarkus.arc`                         | Singular (arc, not arcs)                 |
| Hibernate        | `org.hibernate.validator`                | Singular (validator, not validators)     |

**AgenticBoot Convention:**

```
✅ CORRECT:
dev.adeengineer.adentic.boot.annotations.provider
dev.adeengineer.adentic.boot.annotations.service
dev.adeengineer.adentic.boot.registry
dev.adeengineer.adentic.boot.scanner

❌ INCORRECT:
dev.adeengineer.adentic.boot.annotations.providers  (plural)
dev.adeengineer.adentic.boot.annotations.services   (plural)
dev.adeengineer.adentic.boot.registries             (plural)
dev.adeengineer.adentic.boot.scanners               (plural)
```

---

## Annotation Naming Conventions

### Short Annotations - Package Provides Context

**RULE: Annotations should be SHORT. Package provides the context.**

Following Spring, Jakarta EE, and Quarkus patterns, annotations should be concise domain names without redundant suffixes.

**Industry Standard Pattern:**

| Framework  |             Package              |      Annotation      |            NOT             |
|------------|----------------------------------|----------------------|----------------------------|
| Spring     | `org.springframework.stereotype` | `@Controller`        | ~~@ControllerComponent~~   |
| Spring     | `org.springframework.stereotype` | `@Service`           | ~~@ServiceComponent~~      |
| Spring     | `org.springframework.stereotype` | `@Repository`        | ~~@RepositoryComponent~~   |
| Jakarta EE | `jakarta.persistence`            | `@Entity`            | ~~@EntityClass~~           |
| Jakarta EE | `jakarta.inject`                 | `@Inject`            | ~~@InjectDependency~~      |
| Quarkus    | `io.quarkus.arc`                 | `@ApplicationScoped` | ~~@ApplicationScopedBean~~ |

**AgenticBoot Provider Annotations:**

```java
✅ CORRECT (following Spring/Jakarta pattern):
package dev.adeengineer.adentic.boot.annotations.provider;

@LLM                // NOT @LLMProvider
@Infrastructure     // NOT @InfrastructureProvider
@Storage            // NOT @StorageProvider
@Messaging          // NOT @MessagingProvider
@Orchestration      // NOT @OrchestrationProvider
@Memory             // NOT @MemoryProvider
@Queue              // NOT @QueueProvider
@Tool               // NOT @ToolProvider
@Evaluation         // NOT @EvaluationProvider
```

**AgenticBoot Service Annotations:**

```java
✅ CORRECT:
package dev.adeengineer.adentic.boot.annotations.service;

@DomainService      // Specific enough without suffix
@AgentService       // Specific enough without suffix
```

**Rationale:**
1. **Package context**: `dev.adeengineer.adentic.boot.annotations.provider.LLM` - package already says "provider"
2. **Clear in code**: `@LLM(name = "openai")` is cleaner than `@LLMProvider(name = "openai")`
3. **Industry alignment**: Matches Spring, Jakarta EE, Quarkus conventions
4. **Professional feel**: Framework looks mature and well-designed

---

## Java Style

- **Style Guide**: Google Java Style Guide
- **Line Length**: 120 characters max
- **Indentation**: 4 spaces (no tabs)
- **Naming**: camelCase for variables/methods, PascalCase for classes

---

## Code Organization

```java
// Order: static imports → regular imports
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.springframework.stereotype.Component;

// Order: constants → fields → constructor → public methods → private methods
@Component
public class ExampleClass {
    private static final int CONSTANT = 10;

    private final Dependency dep;

    public ExampleClass(Dependency dep) {
        this.dep = dep;
    }

    public void publicMethod() { }

    private void privateHelper() { }
}
```

---

## Lombok Usage

```java
@Data
@Builder
public class TaskResult {
    private String taskId;
    private String output;
}
```

---

## Testing Standards

- **Coverage**: Minimum 80%
- **Naming**: `methodName_shouldExpectedBehavior_whenCondition()`
- **AAA Pattern**: Arrange, Act, Assert

```java
@Test
void executeTask_shouldReturnFormattedOutput_whenValidRole() {
    // Arrange
    AgentRole agent = new DeveloperAgent(...);

    // Act
    String result = agent.executeTask("task", Map.of());

    // Assert
    assertThat(result).isNotEmpty();
}
```

---

## References

### Industry Standards

1. **Oracle Java Naming Conventions**
   - Package names: lowercase, singular nouns preferred
   - https://www.oracle.com/java/technologies/javase/codeconventions-namingconventions.html
2. **Spring Framework**
   - `org.springframework.stereotype` - @Controller, @Service, @Repository
   - `org.springframework.context.annotation` - @Configuration, @Bean
   - https://spring.io/
3. **Jakarta EE**
   - `jakarta.persistence` - @Entity, @Table
   - `jakarta.inject` - @Inject, @Singleton
   - https://jakarta.ee/
4. **Quarkus**
   - `io.quarkus.arc` - @ApplicationScoped, @RequestScoped
   - https://quarkus.io/
5. **Google Java Style Guide**
   - Package naming: all lowercase, no underscores
   - https://google.github.io/styleguide/javaguide.html

---

*Last Updated: 2025-10-25*
