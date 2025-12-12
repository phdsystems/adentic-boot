# AgenticBoot Backlog

**Last Updated:** 2025-12-12

---

## Overview

This document tracks planned improvements, technical debt, and future enhancements for AgenticBoot.

---

## Backlog Items

### High Priority

| ID | Item | Description | Status |
|----|------|-------------|--------|
| BL-001 | BOM-based dependency management | When adentic-se is stable, switch to BOM-based management for centralized version control | Planned |

### Medium Priority

| ID | Item | Description | Status |
|----|------|-------------|--------|
| BL-002 | Re-enable integration tests | Restore AgentIntegrationTest, AgentRestApiIntegrationTest when ee-core is available | Planned |
| BL-003 | Re-enable provider factories | Restore LLMClientFactory, InfrastructureProviderFactory when dependencies available | Planned |

### Low Priority

| ID | Item | Description | Status |
|----|------|-------------|--------|
| BL-004 | Add prototype scope to DI | Support prototype (non-singleton) beans in AgenticContext | Backlog |
| BL-005 | Add @PatchMapping support | Complete REST method coverage | Backlog |
| BL-006 | Configuration profiles | Support dev/prod/test configuration profiles | Backlog |

---

## Item Details

### BL-001: BOM-based Dependency Management

**Priority:** High
**Status:** Planned
**Blocked by:** adentic-se stability

**Description:**

Currently using property-based version management (standalone mode). When adentic-se ecosystem is stable, migrate to BOM-based management for better version consistency.

**Current approach:**
```xml
<properties>
  <engineeringlab.version>0.2.0-SNAPSHOT</engineeringlab.version>
</properties>

<dependency>
  <groupId>dev.engineeringlab</groupId>
  <artifactId>llm</artifactId>
  <version>${engineeringlab.version}</version>
</dependency>
```

**Target approach:**
```xml
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

<dependencies>
  <dependency>
    <groupId>dev.engineeringlab</groupId>
    <artifactId>llm</artifactId>
    <!-- version inherited from BOM -->
  </dependency>
</dependencies>
```

**Benefits:**
- Centralized version management across all EngineeringLab modules
- Guaranteed compatible versions
- Simpler dependency declarations (no version tags)
- Easier upgrades

**Prerequisites:**
- [ ] adentic-se builds successfully without errors
- [ ] engineeringlab-bom artifact published to repository
- [ ] All module versions aligned

**Related:** [Dependency Management Strategy](../3-design/dependency-management.md)

---

### BL-002: Re-enable Integration Tests

**Priority:** Medium
**Status:** Planned
**Blocked by:** ee-core module

**Description:**

Integration tests were removed during standalone migration due to missing Agent API dependencies:
- `AgentIntegrationTest.java`
- `AgentRestApiIntegrationTest.java`
- `AgentEventBusIntegrationTest.java`
- `TestAgent.java`

**Action:** Restore tests when `ee-core` module is available and Agent interface is properly defined.

---

### BL-003: Re-enable Provider Factories

**Priority:** Medium
**Status:** Planned
**Blocked by:** adentic-core, adentic-ai-client modules

**Description:**

Provider factories were removed during standalone migration:
- `LLMClientFactory.java` - OpenAI, Anthropic, Gemini, vLLM, Ollama clients
- `InfrastructureProviderFactory.java` - Task queues, orchestration, storage, messaging
- `MessagingProviderFactory.java` - Kafka, RabbitMQ brokers
- `ObservabilityProviderFactory.java` - Metrics, health checks, Prometheus
- `RAGProviderFactory.java` - Embedding service, memory providers
- `ResilienceProviderFactory.java` - Resilience4j circuit breakers
- `OpenAIEmbeddingService.java` - OpenAI embeddings implementation

**Action:** Restore factories when external dependencies are available.

---

## Completed Items

| ID | Item | Completed | Notes |
|----|------|-----------|-------|
| - | Standalone build | 2025-12-12 | Framework builds without external deps |
| - | LLM module integration | 2025-12-12 | Added llm module from adentic-se |
| - | Version management docs | 2025-12-12 | Created dependency-management.md |

---

## References

- [Dependency Management](../3-design/dependency-management.md)
- [Architecture](../3-design/architecture.md)
- [Requirements](requirements.md)
