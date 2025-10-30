# Known Issues - Adentic Boot Framework

**Date:** 2025-10-22
**Status:** Namespace migration complete, compilation issues remain

## Issues Fixed ✅

### 1. Namespace Migration (COMPLETED)

- **Issue:** Incomplete migration from `dev.adeengineer.platform` to `dev.adeengineer.adentic`
- **Resolution:** All 188+ Java files migrated to new namespace
  - Production code: `dev.adeengineer.adentic.*`
  - Framework-specific: `dev.adeengineer.adentic.{spring|quarkus|micronaut}.*`
  - Test utilities: `dev.adeengineer.adentic.testutil.*`
- **Files Updated:**
  - All `.java` files (package declarations and imports)
  - All `.yml`/`.yaml` configuration files
  - All `.md` documentation files
  - All `pom.xml` Maven configuration files

### 2. Maven GroupId Inconsistency (COMPLETED)

- **Issue:** Parent pom used `adeengineer.dev` while dependencies used `dev.adeengineer`
- **Resolution:** Standardized all groupIds to `dev.adeengineer` across all pom.xml files

### 3. Spotless Import Order (COMPLETED)

- **Issue:** Import order configuration didn't match namespace structure
- **Resolution:** Updated from `adeengineer` to `dev` in import order configuration

### 4. Missing Module Reference (COMPLETED)

- **Issue:** Parent pom referenced non-existent `ade-agent-platform-agentunit` module
- **Resolution:** Removed module from parent pom, commented out dependencies in child modules with TODO notes

### 5. Directory Structure (COMPLETED)

- **Resolution:** Moved all source files from `/dev/adeengineer/platform/` to `/dev/adeengineer/adentic/`

## Current Issues ⚠️

### 1. Missing External Dependencies (BLOCKING COMPILATION)

The codebase references external SDK dependencies that are not yet published or available:

**Missing ADE Agent SDK:**

```xml
<groupId>dev.adeengineer</groupId>
<artifactId>ade-agent</artifactId>
<artifactId>ade-async</artifactId>
<artifactId>ade-composition</artifactId>
<artifactId>ade-monitoring</artifactId>
```

**Status:** Commented out in `ade-platform-core/pom.xml` with TODO notes

**Files Affected by Missing Dependencies:**
1. `LLMEvaluationProvider.java` - 42 compilation errors
- Missing: `dev.adeengineer.evaluation.*` classes
- Missing: `dev.adeengineer.llm.*` classes

2. `ParallelAgentExecutor.java` - 32 compilation errors
   - Missing: Agent SDK core classes
3. `RoleManager.java` - 20 compilation errors
   - Missing: Agent SDK role management classes
4. `DomainLoader.java` - 20 compilation errors
   - Missing: Agent SDK domain classes
5. `ConfigurableAgent.java` - 18 compilation errors
   - Missing: Agent SDK base classes
6. `WorkflowEngine.java` - 16 compilation errors
   - Missing: Workflow orchestration classes
7. `NoOpLLMProviderFactory.java` - 16 compilation errors
   - Missing: LLM provider interfaces

**Total Compilation Errors:** 180+ across 10 files

### 2. Missing AdenticUnit Test Framework

The test framework module `ade-agent-platform-agentunit` is referenced but not implemented.

**Status:** Dependencies commented out with TODO notes

## Recommendations 📋

### Short Term (To Get Build Working)

1. **Option A: Stub Implementation**
   - Create minimal stub classes for missing external dependencies
   - Allow compilation to proceed
   - Mark with @Deprecated and TODO comments
2. **Option B: Build ade-agent SDK First**
   - Implement the ade-agent SDK modules
   - Install to local Maven repository
   - Uncomment dependencies in pom.xml
3. **Option C: Make Features Optional** (RECOMMENDED)
   - Refactor code to make external SDK dependencies truly optional
   - Use ServiceLoader pattern for pluggable providers
   - Core framework works without external SDKs

### Long Term

1. **Implement ade-agent SDK**
   - Create `ade-agent` core module
   - Implement async, composition, and monitoring extensions
   - Publish to Maven Central or internal repository
2. **Implement AdenticUnit Test Framework**
   - Create testing framework for agent-based applications
   - Similar to JUnit for traditional Java testing
3. **Complete Integration**
   - Wire up inferencestr8a (already built and available)
   - Implement missing provider classes
   - Add proper error handling for missing optional dependencies

## Build Status

**Maven Build:** ❌ FAILING
- **Reason:** Missing external dependencies cause compilation failures
- **Next Step:** Choose one of the three options above

**Namespace Migration:** ✅ COMPLETE
- All namespaces updated to `dev.adeengineer.adentic`
- All imports updated
- All configuration files updated
- All documentation updated

## Files Changed

**Summary:**
- 188+ Java source files (namespaces and imports)
- 10+ YAML configuration files
- 15+ Markdown documentation files
- 5 Maven pom.xml files
- Complete directory structure reorganization

## Next Actions

1. **Choose approach** for handling missing dependencies (A, B, or C above)
2. **Implement chosen approach**
3. **Verify build succeeds**
4. **Run test suite**
5. **Update documentation** with final architecture decisions
