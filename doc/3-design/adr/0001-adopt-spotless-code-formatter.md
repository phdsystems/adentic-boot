# ADR-0001: Adopt Spotless Code Formatter

**Date:** 2025-10-21
**Status:** Accepted
**Decision Makers:** Development Team

---

## Context

The project uses Checkstyle for code quality checks, which identifies style violations such as:
- Missing Javadoc comments
- Unused imports
- Inconsistent formatting
- Line length violations

However, Checkstyle only **detects** issues but does not **fix** them automatically. This creates additional manual work for developers to address formatting issues identified during builds.

### Current Pain Points

1. **Manual fixing required** - Developers must manually fix Checkstyle warnings
2. **Time-consuming** - Reformatting code takes time away from feature development
3. **Inconsistent style** - Different developers may format code differently
4. **CI/CD friction** - Formatting issues can block builds in validation phase

### Requirements

- Auto-fix code formatting issues
- Remove unused imports automatically
- Integrate with existing Maven build process
- Support Google Java Format style guide
- Work alongside Checkstyle (complementary, not replacement)

---

## Decision

We will adopt **Spotless Maven Plugin** (v2.43.0) with **Google Java Format** (v1.22.0) as our automatic code formatter.

### Configuration

The plugin will:
1. Use Google Java Format style (industry standard)
2. Remove unused imports automatically
3. Organize imports in standard order: `java` → `javax` → `org` → `com` → `dev` → `adeengineer`
4. Trim trailing whitespace
5. Ensure files end with newline
6. Run during `validate` phase (before compilation)

### Usage

```bash
# Check formatting (fails build if not formatted)
mvn spotless:check

# Auto-fix formatting issues
mvn spotless:apply

# Format is checked automatically during build
mvn compile
```

---

## Alternatives Considered

### 1. google-java-format-maven-plugin

**Pros:**
- Official Google formatter
- Simple configuration
- Fast execution

**Cons:**
- Less flexible (opinionated)
- Doesn't handle imports organization
- No unused import removal
- Limited to formatting only

**Decision:** Rejected - Too limited in scope

### 2. formatter-maven-plugin (Eclipse Formatter)

**Pros:**
- Highly configurable
- Supports custom Eclipse formatter profiles
- Mature and stable

**Cons:**
- Requires separate Eclipse formatter XML configuration
- More complex setup
- Eclipse-specific (less universal)

**Decision:** Rejected - Unnecessary complexity

### 3. Manual formatting with IDE plugins

**Pros:**
- No build dependency
- Developer control
- IDE-integrated

**Cons:**
- Inconsistent across team (different IDEs/settings)
- Not enforced in CI/CD
- Requires manual action
- No automation

**Decision:** Rejected - Does not solve consistency problem

### 4. Checkstyle only (current state)

**Pros:**
- Already configured
- No additional dependencies
- Detects issues

**Cons:**
- No auto-fixing capability
- Manual remediation required
- Time-consuming for developers

**Decision:** Keep Checkstyle, but complement with Spotless for auto-fixing

---

## Consequences

### Positive

✅ **Automatic formatting** - `mvn spotless:apply` fixes issues instantly
✅ **Consistent code style** - Google Java Format ensures uniformity
✅ **Reduced manual work** - Developers focus on logic, not formatting
✅ **CI/CD integration** - Formatting checked automatically in validate phase
✅ **Import cleanup** - Unused imports removed automatically
✅ **Complementary to Checkstyle** - Spotless fixes what Checkstyle detects
✅ **Industry standard** - Google Java Format is widely adopted
✅ **Simple commands** - Easy for developers to use

### Negative

⚠️ **Build time increase** - Additional validation step (minimal ~2-3 seconds)
⚠️ **Initial reformatting** - Entire codebase needs formatting on first run
⚠️ **Learning curve** - Developers must run `mvn spotless:apply` when needed
⚠️ **Potential conflicts** - Google style may differ from personal preferences

### Mitigation Strategies

1. **Build time** - Spotless is fast; impact is negligible
2. **Initial reformatting** - One-time operation, document in commit message
3. **Learning curve** - Add to developer onboarding guide and CONTRIBUTING.md
4. **Style conflicts** - Google Java Format is industry standard, non-negotiable

---

## Compliance

This decision aligns with:
- **CLAUDE.md Engineering Standards** - Production-ready solutions, no manual workarounds
- **PHD Systems Standards** - Consistent tooling across projects
- **Best Practices** - Automated quality gates in CI/CD pipeline

---

## Implementation Checklist

- [x] Add Spotless plugin to `pom.xml`
- [x] Configure Google Java Format
- [x] Enable unused import removal
- [x] Configure import ordering
- [x] Add to validate phase
- [ ] Run `mvn spotless:apply` on entire codebase
- [ ] Document usage in CONTRIBUTING.md
- [ ] Update developer onboarding guide
- [ ] Add to CI/CD pipeline checks

---

## References

- [Spotless Maven Plugin](https://github.com/diffplug/spotless/tree/main/plugin-maven)
- [Google Java Format](https://github.com/google/google-java-format)
- [Checkstyle Configuration](../../checkstyle.xml)
- [PHD Systems Engineering Standards](https://github.com/phdsystems)

---

**Last Updated:** 2025-10-21
**Version:** 1.0.0
