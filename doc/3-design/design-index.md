# Design Documentation Index - AgenticBoot Framework

**Date:** 2025-10-25
**Version:** 2.0 (Post-Modular Refactoring)
**Status:** Current

---

## Overview

This directory contains all design documentation for the AgenticBoot framework, including architecture specifications, API designs, data models, and Architecture Decision Records (ADRs).

**Current Architecture:** 4-layer modular design (API → Annotations → Core → Framework)

**Quick Links:**
- **Start Here:** [Framework Architecture Overview](framework-architecture-overview.md) - Complete architecture specification
- **Migration:** [Modular Split Plan](architecture-modular-split.md) - How we got here
- **Decisions:** [ADR Directory](decisions/) - All architectural decisions

---

## Table of Contents

- [Core Architecture Documents](#core-architecture-documents)
- [API and Component Design](#api-and-component-design)
- [Data and Workflow](#data-and-workflow)
- [Architecture Decision Records (ADRs)](#architecture-decision-records-adrs)
- [Implementation Planning](#implementation-planning)
- [Quick Navigation by Topic](#quick-navigation-by-topic)

---

## Core Architecture Documents

### 1. Framework Architecture Overview

**File:** [framework-architecture-overview.md](framework-architecture-overview.md)
**Status:** ✅ Current (v2.0)
**Description:** Comprehensive overview of the 4-layer modular architecture

**Contents:**
- Layer specifications (API, Annotations, Core, Boot)
- Module dependencies and dependency graph
- Usage patterns (3 scenarios)
- Benefits analysis (99.8% dependency reduction)
- Industry alignment (Jakarta EE, SLF4J, Spring)

**Read this if:** You want to understand the complete framework architecture

**Time to Read:** ~15 minutes

---

### 2. Modular Architecture Split

**File:** [architecture-modular-split.md](architecture-modular-split.md)
**Status:** ✅ Implemented and Tested
**Description:** Design document for splitting monolithic adentic-boot into 4 layers

**Contents:**
- Problem statement (circular dependencies, heavy dependencies)
- Solution design (4-layer architecture)
- Migration strategy (6 phases)
- Benefits analysis (framework-agnostic, lighter dependencies)
- Industry alignment and naming conventions

**Read this if:** You want to understand why and how we split the architecture

**Time to Read:** ~20 minutes

---

### 3. Architecture (Legacy/Application-Specific)

**File:** [architecture.md](architecture.md)
**Status:** Application-specific (Role Manager App)
**Description:** System architecture for Role Manager App (multi-agent AI system)

**Contents:**
- Component diagrams
- Layer responsibilities
- Technology stack

**Note:** This is application-specific, not framework architecture. See [framework-architecture-overview.md](framework-architecture-overview.md) for framework architecture.

**Time to Read:** ~10 minutes

---

### 4. ADE Architecture Tiers

**File:** [ade-architecture-tiers.md](ade-architecture-tiers.md)
**Status:** Conceptual (ADE Agent SDK)
**Description:** Tiered architecture for ADE Agent SDK

**Contents:**
- Multi-tier system design
- Agent SDK concepts

**Note:** This is specific to the ADE Agent SDK use case.

**Time to Read:** ~8 minutes

---

## API and Component Design

### 5. API Design

**File:** [api-design.md](api-design.md)
**Status:** Current
**Description:** API contracts and endpoints

**Contents:**
- REST API specifications
- Provider interfaces
- Request/response formats

**Read this if:** You're implementing API clients or providers

**Time to Read:** ~12 minutes

---

### 6. Component Specifications

**File:** [component-specifications.md](component-specifications.md)
**Status:** Current
**Description:** Detailed specifications for framework components

**Contents:**
- ComponentScanner
- ProviderRegistry
- DependencyInjector
- EventBus
- AgenticServer

**Read this if:** You're working on framework internals

**Time to Read:** ~15 minutes

---

## Data and Workflow

### 7. Data Model

**File:** [data-model.md](data-model.md)
**Status:** Current
**Description:** Data structures and entity relationships

**Contents:**
- Entity models
- Relationships
- Database schema

**Read this if:** You're working with data persistence or domain models

**Time to Read:** ~10 minutes

---

### 8. Dataflow

**File:** [dataflow.md](dataflow.md)
**Status:** Current
**Description:** Data movement and transformations through the system

**Contents:**
- Data pipelines
- Transformation logic
- Integration points

**Read this if:** You need to understand how data flows through the system

**Time to Read:** ~12 minutes

---

### 9. Workflow

**File:** [workflow.md](workflow.md)
**Status:** Current
**Description:** Step-by-step processes and workflows

**Contents:**
- Business processes
- Workflow orchestration
- State transitions

**Read this if:** You need to understand system processes and workflows

**Time to Read:** ~10 minutes

---

### 10. Error Handling Strategy

**File:** [error-handling-strategy.md](error-handling-strategy.md)
**Status:** Current
**Description:** Error handling patterns and strategies

**Contents:**
- Exception hierarchy
- Retry policies
- Error propagation

**Read this if:** You're implementing error handling or debugging issues

**Time to Read:** ~8 minutes

---

## Architecture Decision Records (ADRs)

### ADR Directory

**Location:** [decisions/](decisions/)
**Description:** All architectural decisions with rationale

**ADRs:**

#### ADR-0003: Domain-Specific Annotation Module ✅ CRITICAL

**File:** [decisions/0003-domain-specific-annotation-module.md](decisions/0003-domain-specific-annotation-module.md)
**Status:** ✅ Accepted and Implemented
**Date:** 2025-10-25

**Decision:** Keep `adentic-se-annotation` domain-specific (Agentic concepts only), move generic framework annotations to `adentic-boot`

**Why:**
- Clear separation of concerns (domain vs framework)
- Framework-agnostic design (works with Spring, Quarkus, Micronaut)
- Avoid scope creep (no reimplementing @Entity, @Repository, @Transactional)
- 28% JAR size reduction (12KB → 8.7KB)

**Impact:**
- ✅ adentic-se-annotation: 8.7KB (11 domain annotations)
- ✅ adentic-boot: Contains @Component, @Service, @RestController, @Inject
- ✅ Framework-agnostic providers

**Read this if:** You want to understand why annotations are split across modules

**Time to Read:** ~10 minutes

---

#### ADR/Decision 001: Code Formatting Tool Selection

**File:** [decisions/001-code-formatting-tool-selection.md](decisions/001-code-formatting-tool-selection.md)
**Status:** Accepted
**Description:** Selection of code formatting tool (Spotless)

---

#### ADR/Decision 002: Checkstyle Configuration Update

**File:** [decisions/002-checkstyle-configuration-update.md](decisions/002-checkstyle-configuration-update.md)
**Status:** Accepted
**Description:** Checkstyle configuration decisions

---

#### Legacy ADR Directory

**Location:** [adr/](adr/)
**Description:** Legacy ADR format (being migrated to decisions/)

**Contents:**
- ADR-0001: Adopt Spotless Code Formatter
- ADR-0002: Code Formatter Comparison

**Note:** New ADRs should be created in `decisions/` following the pattern of ADR-0003.

---

## Implementation Planning

### 11. Generic Refactoring Plan

**File:** [generic-refactoring-plan.md](generic-refactoring-plan.md)
**Status:** Planning
**Description:** Template for refactoring projects

**Contents:**
- Refactoring methodology
- Step-by-step templates
- Best practices

**Read this if:** You're planning a major refactoring

**Time to Read:** ~8 minutes

---

### 12. Implementation Test Strategy

**File:** [implementation-test-strategy.md](implementation-test-strategy.md)
**Status:** Current
**Description:** Testing strategy for implementations

**Contents:**
- Unit testing approach
- Integration testing
- Test coverage requirements

**Read this if:** You're writing tests or verifying quality

**Time to Read:** ~10 minutes

---

## Quick Navigation by Topic

### Understanding the Framework

1. [Framework Architecture Overview](framework-architecture-overview.md) - Start here
2. [Modular Architecture Split](architecture-modular-split.md) - Migration story
3. [Component Specifications](component-specifications.md) - Component details

### Working with Annotations

1. [ADR-0003: Domain-Specific Annotation Module](decisions/0003-domain-specific-annotation-module.md) - Why annotations are split
2. [Framework Architecture Overview](framework-architecture-overview.md) - Layer 2 specification

### Building Providers

1. [API Design](api-design.md) - Provider interfaces
2. [Framework Architecture Overview](framework-architecture-overview.md) - Layer 3 specification
3. [Component Specifications](component-specifications.md) - Registry and scanning

### Data and Workflows

1. [Data Model](data-model.md) - Entity structures
2. [Dataflow](dataflow.md) - Data movement
3. [Workflow](workflow.md) - Business processes

### Architecture Decisions

1. [ADR-0003: Domain-Specific Annotation Module](decisions/0003-domain-specific-annotation-module.md) - Latest critical decision
2. [ADR Directory](decisions/) - All decisions
3. [Legacy ADR Directory](adr/) - Historical decisions

---

## Document Metadata

|              Document              |    Status     | Version | Last Updated | Lines | Time to Read |
|------------------------------------|---------------|---------|--------------|-------|--------------|
| framework-architecture-overview.md | ✅ Current     | 2.0     | 2025-10-25   | ~500  | 15 min       |
| architecture-modular-split.md      | ✅ Implemented | 2.0     | 2025-10-25   | ~980  | 20 min       |
| ADR-0003                           | ✅ Accepted    | 1.0     | 2025-10-25   | ~380  | 10 min       |
| api-design.md                      | Current       | -       | -            | -     | 12 min       |
| component-specifications.md        | Current       | -       | -            | -     | 15 min       |
| data-model.md                      | Current       | -       | -            | -     | 10 min       |
| dataflow.md                        | Current       | -       | -            | -     | 12 min       |
| workflow.md                        | Current       | -       | -            | -     | 10 min       |
| error-handling-strategy.md         | Current       | -       | -            | -     | 8 min        |

**Total Estimated Reading Time:** ~2 hours (core architecture documents only)

---

## File Organization

```
doc/3-design/
├── design-index.md                          # ← YOU ARE HERE
├── framework-architecture-overview.md       # ✅ Primary architecture doc
├── architecture-modular-split.md            # ✅ Migration design
├── architecture.md                          # Application-specific (Role Manager)
├── ade-architecture-tiers.md                # ADE SDK-specific
├── api-design.md                            # API specifications
├── component-specifications.md              # Component details
├── data-model.md                            # Data structures
├── dataflow.md                              # Data movement
├── workflow.md                              # Business processes
├── error-handling-strategy.md               # Error handling
├── generic-refactoring-plan.md              # Refactoring templates
├── implementation-test-strategy.md          # Testing strategy
├── decisions/                               # ✅ Active ADR directory
│   ├── 0003-domain-specific-annotation-module.md  # ✅ Latest ADR
│   ├── 001-code-formatting-tool-selection.md
│   └── 002-checkstyle-configuration-update.md
└── adr/                                     # Legacy ADR directory
    ├── README.md
    ├── 0001-adopt-spotless-code-formatter.md
    └── 0002-code-formatter-comparison.md
```

---

## Recommended Reading Path

### New Developer Onboarding

1. [Framework Architecture Overview](framework-architecture-overview.md) - Understand the structure
2. [ADR-0003](decisions/0003-domain-specific-annotation-module.md) - Understand annotation split
3. [Component Specifications](component-specifications.md) - Understand components
4. [API Design](api-design.md) - Understand interfaces

**Time:** ~50 minutes

### Provider Author

1. [Framework Architecture Overview](framework-architecture-overview.md) - Layer 2 & 3
2. [ADR-0003](decisions/0003-domain-specific-annotation-module.md) - Annotation usage
3. [API Design](api-design.md) - Implement contracts

**Time:** ~35 minutes

### Framework Developer

1. [Framework Architecture Overview](framework-architecture-overview.md) - Complete architecture
2. [Modular Architecture Split](architecture-modular-split.md) - Migration history
3. [ADR-0003](decisions/0003-domain-specific-annotation-module.md) - Design rationale
4. [Component Specifications](component-specifications.md) - Implementation details

**Time:** ~60 minutes

---

## Contributing to Design Documentation

### Creating New ADRs

Follow the pattern of [ADR-0003](decisions/0003-domain-specific-annotation-module.md):

1. **File Location:** `doc/3-design/decisions/NNNN-short-title.md`
2. **Numbering:** Use 4-digit format (0003, 0004, etc.)
3. **Required Sections:**
   - Context
   - Decision
   - Rationale (detailed)
   - Consequences (positive and negative)
   - Alternatives Considered
   - Implementation
   - Metrics (if applicable)

### Updating This Index

When adding new design documents:
1. Add entry in appropriate section
2. Update Quick Navigation by Topic
3. Update Document Metadata table
4. Update File Organization tree
5. Update Recommended Reading Path (if applicable)

---

## Version History

| Version |    Date    |                           Changes                            |
|---------|------------|--------------------------------------------------------------|
| 2.0     | 2025-10-25 | Created comprehensive design index after modular refactoring |

---

*Last Updated: 2025-10-25*
*Version: 2.0 (Post-Modular Refactoring)*
*Status: Current*
