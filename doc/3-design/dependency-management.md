# Dependency Management Strategy

**Date:** 2025-12-12
**Version:** 1.0
**Status:** Active

---

## TL;DR

AgenticBoot uses **property-based version management** for all dependencies. Versions are defined once in `<properties>` and referenced via `${property.name}`. This minimizes impact when versions change and ensures consistency across the project.

---

## Overview

### Goals

1. **Single Point of Change** - Update version in one place, affects all usages
2. **Clear Organization** - Versions grouped by category (internal, third-party, test, plugins)
3. **Self-Documenting** - Comments explain what each version controls
4. **Easy Upgrades** - Version updates are simple property changes
5. **Consistency** - All modules use the same dependency versions

---

## Version Property Structure

```xml
<properties>
  <!-- =========================== -->
  <!-- EngineeringLab Versions     -->
  <!-- =========================== -->
  <!-- Internal modules from adentic-se ecosystem -->
  <engineeringlab.version>0.2.0-SNAPSHOT</engineeringlab.version>

  <!-- =========================== -->
  <!-- Third-Party Versions        -->
  <!-- =========================== -->
  <!-- Web/HTTP -->
  <javalin.version>6.1.3</javalin.version>

  <!-- JSON/Serialization -->
  <jackson.version>2.17.2</jackson.version>

  <!-- Logging -->
  <slf4j.version>2.0.16</slf4j.version>
  <logback.version>1.5.6</logback.version>

  <!-- Reactive -->
  <reactor.version>3.6.7</reactor.version>

  <!-- =========================== -->
  <!-- Test Versions               -->
  <!-- =========================== -->
  <junit.version>5.10.2</junit.version>
  <assertj.version>3.25.3</assertj.version>
  <mockito.version>5.8.0</mockito.version>

  <!-- =========================== -->
  <!-- Plugin Versions             -->
  <!-- =========================== -->
  <maven-compiler-plugin.version>3.13.0</maven-compiler-plugin.version>
  <!-- ... -->
</properties>
```

---

## Usage Pattern

### Defining Dependencies

```xml
<dependency>
  <groupId>dev.engineeringlab</groupId>
  <artifactId>llm</artifactId>
  <version>${engineeringlab.version}</version>  <!-- References property -->
</dependency>

<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-databind</artifactId>
  <version>${jackson.version}</version>
</dependency>
```

### Adding New EngineeringLab Modules

When adding a new module from the adentic-se ecosystem:

```xml
<!-- No new property needed - reuse existing -->
<dependency>
  <groupId>dev.engineeringlab</groupId>
  <artifactId>cache</artifactId>
  <version>${engineeringlab.version}</version>
</dependency>

<dependency>
  <groupId>dev.engineeringlab</groupId>
  <artifactId>messaging</artifactId>
  <version>${engineeringlab.version}</version>
</dependency>
```

### Version Updates

To update a version, change only the property:

```xml
<!-- Before -->
<engineeringlab.version>0.2.0-SNAPSHOT</engineeringlab.version>

<!-- After -->
<engineeringlab.version>0.3.0-SNAPSHOT</engineeringlab.version>
```

All dependencies using `${engineeringlab.version}` are automatically updated.

---

## Category Guidelines

### 1. EngineeringLab Versions

For internal modules from the adentic-se ecosystem:

| Property | Modules |
|----------|---------|
| `engineeringlab.version` | llm, cache, messaging, storage, email, notification, etc. |

**Rule:** All adentic-se modules should use the same version to ensure compatibility.

### 2. Third-Party Versions

For external libraries, organized by function:

| Category | Properties |
|----------|-----------|
| Web/HTTP | `javalin.version` |
| JSON | `jackson.version` |
| Logging | `slf4j.version`, `logback.version` |
| Reactive | `reactor.version` |
| Metrics | `micrometer.version` |
| Database | `h2.version` |
| Code Gen | `lombok.version` |

### 3. Test Versions

For testing frameworks:

| Property | Library |
|----------|---------|
| `junit.version` | JUnit 5 Jupiter |
| `assertj.version` | AssertJ |
| `mockito.version` | Mockito |

### 4. Plugin Versions

For Maven plugins:

| Property | Plugin |
|----------|--------|
| `maven-compiler-plugin.version` | Compiler |
| `maven-surefire-plugin.version` | Test execution |
| `spotless-maven-plugin.version` | Code formatting |
| `checkstyle.version` | Code quality |
| `jacoco.version` | Code coverage |

---

## Alternative Approaches

### BOM (Bill of Materials)

For multi-module projects or ecosystems, a BOM provides centralized version management:

```xml
<!-- In parent pom or importing project -->
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>dev.engineeringlab</groupId>
      <artifactId>engineeringlab-bom</artifactId>
      <version>${engineeringlab.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<!-- Then omit versions in dependencies -->
<dependencies>
  <dependency>
    <groupId>dev.engineeringlab</groupId>
    <artifactId>llm</artifactId>
    <!-- version inherited from BOM -->
  </dependency>
</dependencies>
```

**When to use BOM:**
- Multi-module Maven projects
- Shared dependency sets across projects
- Organization-wide version standardization

**Current status:** BOM available at `engineeringlab-bom` but not used in standalone mode.

### Parent POM

Inherit versions from a parent:

```xml
<parent>
  <groupId>dev.engineeringlab</groupId>
  <artifactId>adentic-se-parent</artifactId>
  <version>0.2.0-SNAPSHOT</version>
</parent>
```

**When to use:**
- Organization-wide build standards
- Shared plugin configurations
- Enforced coding standards

---

## Best Practices

### DO

1. **Use properties for all versions** - Never hardcode versions in `<dependency>` tags
2. **Group related properties** - Use comments to organize by category
3. **Keep versions aligned** - All modules from same project should use same version
4. **Document version requirements** - Note minimum versions or compatibility issues
5. **Test after updates** - Run full test suite after version changes

### DON'T

1. **Don't mix version strategies** - Pick one approach and stick with it
2. **Don't use version ranges** - They cause non-reproducible builds
3. **Don't skip major versions** - Test intermediate versions for breaking changes
4. **Don't ignore deprecation warnings** - Plan migration before removal

---

## Version Update Checklist

When updating versions:

- [ ] Update property in `pom.xml`
- [ ] Run `mvn clean compile` to verify compilation
- [ ] Run `mvn test` to verify tests pass
- [ ] Check for deprecation warnings in logs
- [ ] Update this document if new patterns emerge
- [ ] Commit with message: `deps: update <library> to <version>`

---

## Current Versions (as of 2025-12-12)

| Category | Property | Version |
|----------|----------|---------|
| **EngineeringLab** | `engineeringlab.version` | 0.2.0-SNAPSHOT |
| **Web** | `javalin.version` | 6.1.3 |
| **JSON** | `jackson.version` | 2.17.2 |
| **Logging** | `slf4j.version` | 2.0.16 |
| **Logging** | `logback.version` | 1.5.6 |
| **Reactive** | `reactor.version` | 3.6.7 |
| **Metrics** | `micrometer.version` | 1.13.0 |
| **Database** | `h2.version` | 2.2.224 |
| **Code Gen** | `lombok.version` | 1.18.34 |
| **Test** | `junit.version` | 5.10.2 |
| **Test** | `assertj.version` | 3.25.3 |
| **Test** | `mockito.version` | 5.8.0 |

---

## References

- [Maven Dependency Mechanism](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html)
- [Maven BOM](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#bill-of-materials-bom-poms)
- [Architecture Overview](architecture.md)
- [Framework Architecture](framework-architecture-overview.md)

---

*Last Updated: 2025-12-12*
