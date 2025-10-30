# Architecture Decision Records (ADR)

This directory contains Architecture Decision Records (ADRs) for the ADE Agent SDK project.

## What are ADRs?

Architecture Decision Records document important architectural decisions made throughout the project lifecycle. Each ADR captures:
- **Context:** Why was this decision needed?
- **Decision:** What was decided?
- **Alternatives:** What other options were considered?
- **Consequences:** What are the impacts (positive and negative)?

## ADR Index

|                        ADR                        |             Title             |    Status     |    Date    |
|---------------------------------------------------|-------------------------------|---------------|------------|
| [ADR-0001](0001-adopt-spotless-code-formatter.md) | Adopt Spotless Code Formatter | Accepted      | 2025-10-21 |
| [ADR-0002](0002-code-formatter-comparison.md)     | Code Formatter Comparison     | Informational | 2025-10-21 |

## ADR Lifecycle

- **Proposed** - Under discussion
- **Accepted** - Decision approved and implemented
- **Deprecated** - No longer recommended but still in use
- **Superseded** - Replaced by another ADR (link to new ADR)
- **Rejected** - Proposal not accepted

## Creating a new ADR

1. Copy the template from the most recent ADR
2. Increment the ADR number (e.g., 0001 → 0002)
3. Use descriptive kebab-case filename: `NNNN-brief-description.md`
4. Include: Context, Decision, Alternatives, Consequences
5. Update this index with the new entry
6. Commit with message: `docs(adr): add ADR-NNNN for <topic>`

## References

- [ADR GitHub Organization](https://adr.github.io/)
- [Documenting Architecture Decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
- [ADR Tools](https://github.com/npryce/adr-tools)

---

**Last Updated:** 2025-10-21
