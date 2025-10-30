# Configuration Guide - Documentation Link Verifier Tool

**Date:** 2025-10-25
**Version:** 0.3.0

---

## Table of Contents

- [Overview](#overview)
- [Quick Start](#quick-start)
- [Configuration Options Reference](#configuration-options-reference)
- [Preset Configurations](#preset-configurations)
- [HTTP Configuration](#http-configuration)
- [Caching Configuration](#caching-configuration)
- [Feature Flags](#feature-flags)
- [Common Scenarios](#common-scenarios)
- [Best Practices](#best-practices)
- [FAQ](#faq)

---

## Overview

The Documentation Link Verifier Tool is highly configurable to support different verification scenarios, from fast pre-commit checks to thorough nightly CI/CD runs.

**Configuration Method:** `LinkVerifierConfig` class with builder pattern

**Preset Configurations:**
- `defaults()` - Balanced for most use cases
- `fast()` - Quick local verification (skip HTTP)
- `thorough()` - Comprehensive validation (longer timeouts)
- `filesOnly()` - Only verify file links (no HTTP, no anchors)

**Custom Configuration:** Build your own with `LinkVerifierConfig.builder()`

---

## Quick Start

### Use a Preset

```java
@Inject
private DocumentationLinkVerifierTool verifier;

public void configure() {
    // Fast mode for pre-commit hooks
    verifier.setConfig(LinkVerifierConfig.fast());

    // Thorough mode for CI/CD
    verifier.setConfig(LinkVerifierConfig.thorough());

    // Files only for quick checks
    verifier.setConfig(LinkVerifierConfig.filesOnly());
}
```

### Custom Configuration

```java
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .httpTimeoutMs(10000)       // 10 second timeout
    .httpRetries(3)             // 3 retry attempts
    .verifyHttpLinks(true)      // Enable HTTP verification
    .cacheHttpResults(true)     // Cache results for 5 minutes
    .build();

verifier.setConfig(config);
```

---

## Configuration Options Reference

### Complete Configuration Options

```java
LinkVerifierConfig config = LinkVerifierConfig.builder()
    // HTTP Configuration
    .httpTimeoutMs(5000)           // HTTP request timeout (milliseconds)
    .httpRetries(2)                // Number of retry attempts
    .followRedirects(true)         // Follow HTTP redirects (3xx)
    .userAgent("Doc-Link-Verifier/1.0")  // Custom User-Agent header

    // Feature Flags
    .verifyHttpLinks(true)         // Enable/disable HTTP link verification
    .verifyFileLinks(true)         // Enable/disable file link verification
    .verifyAnchorLinks(true)       // Enable/disable anchor link verification

    // Caching Configuration
    .cacheHttpResults(true)        // Enable/disable HTTP result caching
    .cacheTtlMs(300000)            // Cache TTL in milliseconds (5 minutes)

    .build();
```

### Option Details

|       Option        |  Type   |         Default         |                    Description                    |
|---------------------|---------|-------------------------|---------------------------------------------------|
| `httpTimeoutMs`     | int     | 5000                    | HTTP request timeout in milliseconds              |
| `httpRetries`       | int     | 2                       | Number of retry attempts for failed HTTP requests |
| `followRedirects`   | boolean | true                    | Whether to follow HTTP 3xx redirects              |
| `userAgent`         | String  | "Doc-Link-Verifier/1.0" | Custom User-Agent header for HTTP requests        |
| `verifyHttpLinks`   | boolean | true                    | Enable HTTP/HTTPS link verification               |
| `verifyFileLinks`   | boolean | true                    | Enable file link verification                     |
| `verifyAnchorLinks` | boolean | true                    | Enable anchor (#section) verification             |
| `cacheHttpResults`  | boolean | true                    | Enable HTTP result caching                        |
| `cacheTtlMs`        | long    | 300000                  | Cache TTL in milliseconds (5 minutes)             |

---

## Preset Configurations

### 1. Default Configuration

**Use Case:** Balanced for most scenarios

```java
LinkVerifierConfig.defaults()
```

**Settings:**
- HTTP timeout: 5 seconds
- HTTP retries: 2 attempts
- Verify all link types: ✅
- HTTP caching: ✅ (5 minutes)
- Follow redirects: ✅

**When to Use:**
- General documentation verification
- Interactive CLI usage
- Development workflows

**Performance:** Medium (depends on HTTP link count)

---

### 2. Fast Configuration

**Use Case:** Quick local checks, pre-commit hooks

```java
LinkVerifierConfig.fast()
```

**Settings:**
- HTTP timeout: 1 second
- HTTP retries: 0 attempts
- Verify HTTP links: ❌ (skipped)
- Verify file links: ✅
- Verify anchor links: ✅
- HTTP caching: ✅

**When to Use:**
- Pre-commit hooks (must be fast)
- Local development validation
- Quick sanity checks
- Large documentation sets

**Performance:** Very fast (no network requests)

**Trade-offs:**
- ✅ Extremely fast (milliseconds)
- ❌ Won't catch broken HTTP links
- ✅ Still validates file structure
- ✅ Still validates anchors

**Example:**

```java
// Pre-commit hook configuration
verifier.setConfig(LinkVerifierConfig.fast());

DirectoryVerificationResult result = verifier
    .verifyDirectory("doc/", true)
    .block();

// Fast execution: ~45ms for 47 files
```

---

### 3. Thorough Configuration

**Use Case:** Comprehensive validation, nightly CI/CD

```java
LinkVerifierConfig.thorough()
```

**Settings:**
- HTTP timeout: 10 seconds
- HTTP retries: 3 attempts
- Verify all link types: ✅
- HTTP caching: ✅ (5 minutes)
- Follow redirects: ✅

**When to Use:**
- Nightly CI/CD pipelines
- Release validation
- Comprehensive audits
- External link validation

**Performance:** Slow (depends on HTTP link count and network)

**Trade-offs:**
- ✅ Most comprehensive validation
- ✅ Catches transient network issues
- ❌ Slowest configuration
- ✅ Best for production docs

**Example:**

```java
// Nightly CI/CD configuration
verifier.setConfig(LinkVerifierConfig.thorough());

DirectoryVerificationResult result = verifier
    .verifyDirectory("doc/", true)
    .block();

// Thorough execution: ~5-10 seconds for 47 files with HTTP links
```

---

### 4. Files Only Configuration

**Use Case:** File structure validation only

```java
LinkVerifierConfig.filesOnly()
```

**Settings:**
- Verify HTTP links: ❌ (skipped)
- Verify file links: ✅
- Verify anchor links: ❌ (skipped)
- HTTP caching: N/A

**When to Use:**
- File structure verification
- Refactoring validation
- Documentation reorganization
- Quick structure checks

**Performance:** Very fast (no network, no anchor parsing)

**Example:**

```java
// Files only configuration
verifier.setConfig(LinkVerifierConfig.filesOnly());

LinkVerificationResult result = verifier
    .verifyFile("doc/architecture.md")
    .block();

// Only checks if linked files exist
```

---

## HTTP Configuration

### Timeout Configuration

**Purpose:** Control how long to wait for HTTP responses

```java
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .httpTimeoutMs(10000)  // 10 seconds
    .build();
```

**Recommendations:**

|    Scenario     | Timeout |            Rationale            |
|-----------------|---------|---------------------------------|
| Pre-commit hook | 1000ms  | Must be fast, skip slow links   |
| Interactive CLI | 5000ms  | Balanced (default)              |
| Nightly CI/CD   | 10000ms | Thorough, wait for slow servers |
| External docs   | 15000ms | Some sites are very slow        |

**Trade-offs:**
- **Short timeout (1s):** Fast, may miss valid but slow links
- **Long timeout (10s+):** Catches all valid links, slow overall

---

### Retry Configuration

**Purpose:** Retry failed HTTP requests to handle transient errors

```java
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .httpRetries(3)  // 3 retry attempts
    .build();
```

**Retry Strategy:** Exponential backoff
- Attempt 1: Immediate
- Attempt 2: Wait 500ms
- Attempt 3: Wait 1000ms
- Attempt 4: Wait 1500ms

**Recommendations:**

|    Scenario     | Retries |             Rationale             |
|-----------------|---------|-----------------------------------|
| Pre-commit hook | 0       | No time for retries               |
| Interactive CLI | 2       | Default, handles transient errors |
| Nightly CI/CD   | 3       | Thorough, maximizes success rate  |
| Flaky networks  | 5       | Handle unreliable connections     |

**Trade-offs:**
- **No retries (0):** Fast, may report valid links as broken
- **Many retries (3+):** High success rate, slow for broken links

---

### Redirect Configuration

**Purpose:** Control whether to follow HTTP 3xx redirects

```java
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .followRedirects(true)  // Follow redirects
    .build();
```

**Behavior:**
- `true`: 3xx responses are considered valid (follows redirect chain)
- `false`: 3xx responses are considered broken

**Recommendations:**
- **Follow redirects (true):** Default, most links redirect (http→https, shortened URLs)
- **Don't follow (false):** Strict validation, want to update to canonical URLs

**Example:**

```java
// Follow redirects (default)
verifier.setConfig(LinkVerifierConfig.builder()
    .followRedirects(true)
    .build());

// http://example.com → https://example.com (valid)
```

---

### User-Agent Configuration

**Purpose:** Set custom User-Agent header for HTTP requests

```java
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .userAgent("MyProject-LinkChecker/1.0")
    .build();
```

**Recommendations:**
- **Default:** "Doc-Link-Verifier/1.0"
- **Custom:** Include project name and version
- **Why:** Some sites block requests without User-Agent or block default Java user agents

**Example:**

```java
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .userAgent("AdenticFramework-DocVerifier/0.3.0 (+https://adentic.dev)")
    .build();
```

---

## Caching Configuration

### HTTP Result Caching

**Purpose:** Cache HTTP verification results to avoid redundant requests

```java
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .cacheHttpResults(true)   // Enable caching
    .cacheTtlMs(300000)       // 5 minutes TTL
    .build();
```

**How It Works:**
1. First HTTP request: Performs HEAD request, caches result
2. Subsequent requests (within TTL): Returns cached result
3. After TTL expires: Performs new HEAD request

**Cache Key:** HTTP URL (e.g., "https://example.com/page")

**Benefits:**
- ✅ Faster for repeated verifications
- ✅ Reduces network requests
- ✅ Handles duplicate links efficiently

**When to Enable:**
- ✅ Repeated verifications in same session
- ✅ Large documentation sets with duplicate links
- ✅ Interactive CLI usage

**When to Disable:**
- ❌ One-time verification
- ❌ CI/CD (fresh verification each run)
- ❌ Want real-time validation

---

### Cache TTL Configuration

**Purpose:** Control how long cached results are valid

```java
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .cacheTtlMs(600000)  // 10 minutes
    .build();
```

**Recommendations:**

|       Scenario       |        TTL        |       Rationale       |
|----------------------|-------------------|-----------------------|
| Interactive session  | 300000ms (5 min)  | Default, good balance |
| Long session         | 900000ms (15 min) | Longer session work   |
| Real-time validation | 60000ms (1 min)   | Recent results only   |
| Disable cache        | 0ms               | No caching            |

**Example:**

```java
// 10-minute cache for long documentation work
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .cacheHttpResults(true)
    .cacheTtlMs(600000)  // 10 minutes
    .build();

verifier.setConfig(config);

// First verification: Performs HTTP requests
verifier.verifyDirectory("doc/", true).block();

// Second verification (within 10 min): Uses cache
verifier.verifyDirectory("doc/", true).block();  // Much faster
```

---

### Clear Cache Manually

```java
// Clear HTTP cache manually
verifier.clearCache();

// Useful when:
// - Config changed
// - Want fresh validation
// - Known HTTP link was fixed
```

---

## Feature Flags

### Verify HTTP Links

**Purpose:** Enable/disable HTTP/HTTPS link verification

```java
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .verifyHttpLinks(true)  // Enable
    .build();
```

**When to Disable:**
- Pre-commit hooks (too slow)
- File structure validation only
- No HTTP links in documentation

**Example:**

```java
// Disable HTTP verification for speed
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .verifyHttpLinks(false)
    .build();

verifier.setConfig(config);

// HTTP links are skipped, considered valid
```

---

### Verify File Links

**Purpose:** Enable/disable file link verification

```java
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .verifyFileLinks(true)  // Enable
    .build();
```

**When to Disable:**
- HTTP-only verification
- External documentation validation
- Rarely needed (usually want this)

---

### Verify Anchor Links

**Purpose:** Enable/disable anchor (#section) verification

```java
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .verifyAnchorLinks(true)  // Enable
    .build();
```

**When to Disable:**
- Quick file structure checks
- Performance optimization
- Anchor parsing too slow

**Example:**

```java
// Disable anchor verification for speed
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .verifyAnchorLinks(false)
    .build();

verifier.setConfig(config);

// [Section](#overview) is not verified (considered valid)
```

---

## Common Scenarios

### Scenario 1: Pre-Commit Hook (Fast)

**Requirements:**
- Must be very fast (< 1 second)
- Verify file structure
- Skip HTTP (too slow)

**Configuration:**

```java
verifier.setConfig(LinkVerifierConfig.fast());

DirectoryVerificationResult result = verifier
    .verifyDirectory("doc/", true)
    .block();

if (!result.isValid()) {
    System.err.println("❌ Broken links found!");
    System.exit(1);
}
```

**Performance:** ~45ms for 47 files

---

### Scenario 2: Nightly CI/CD (Thorough)

**Requirements:**
- Comprehensive validation
- Verify all link types
- Can be slow (minutes OK)

**Configuration:**

```java
verifier.setConfig(LinkVerifierConfig.thorough());

DirectoryVerificationResult result = verifier
    .verifyDirectory("doc/", true)
    .block();

// Generate detailed report
System.out.println(result.getDetailedReport());

// Fail build if broken links
if (!result.isValid()) {
    throw new RuntimeException("Documentation has broken links!");
}
```

**Performance:** ~5-10 seconds for 47 files (with HTTP)

---

### Scenario 3: Interactive CLI (Balanced)

**Requirements:**
- Reasonable speed
- Full validation
- Caching for repeated checks

**Configuration:**

```java
verifier.setConfig(LinkVerifierConfig.defaults());

// First run: Full validation
DirectoryVerificationResult result = verifier
    .verifyDirectory("doc/", true)
    .block();

System.out.println(result.getSummary());

// Second run: Uses cache (much faster)
result = verifier.verifyDirectory("doc/", true).block();
```

**Performance:** ~2-5 seconds (first run), ~200ms (cached)

---

### Scenario 4: External Documentation Validation

**Requirements:**
- Only verify HTTP links
- Skip file links (external docs)
- Long timeout (slow servers)

**Configuration:**

```java
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .verifyHttpLinks(true)
    .verifyFileLinks(false)      // Skip files
    .verifyAnchorLinks(false)    // Skip anchors
    .httpTimeoutMs(15000)        // 15 seconds
    .httpRetries(3)
    .build();

verifier.setConfig(config);

LinkVerificationResult result = verifier
    .verifyFile("external-resources.md")
    .block();
```

---

### Scenario 5: Documentation Refactoring

**Requirements:**
- Verify file structure only
- No HTTP or anchors
- Very fast

**Configuration:**

```java
verifier.setConfig(LinkVerifierConfig.filesOnly());

DirectoryVerificationResult result = verifier
    .verifyDirectory("doc/", true)
    .block();

// Only checks if files exist, not anchors or HTTP
```

**Performance:** ~30ms for 47 files

---

### Scenario 6: Flaky Network Environment

**Requirements:**
- Handle unreliable connections
- More retries
- Longer timeout

**Configuration:**

```java
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .httpTimeoutMs(10000)   // 10 seconds
    .httpRetries(5)         // 5 attempts
    .cacheHttpResults(true) // Cache successes
    .build();

verifier.setConfig(config);
```

---

## Best Practices

### 1. Choose the Right Preset

**Don't customize unless needed:**

```java
// ✅ Good: Use preset for common scenarios
verifier.setConfig(LinkVerifierConfig.fast());

// ❌ Avoid: Unnecessarily complex custom config
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .verifyHttpLinks(false)
    .verifyFileLinks(true)
    .verifyAnchorLinks(true)
    .build();  // This is just .fast()
```

---

### 2. Different Configs for Different Contexts

```java
@Component
public class DocumentationService {
    @Inject
    private DocumentationLinkVerifierTool verifier;

    public void preCommitCheck() {
        verifier.setConfig(LinkVerifierConfig.fast());
        // Fast validation
    }

    public void nightlyValidation() {
        verifier.setConfig(LinkVerifierConfig.thorough());
        // Comprehensive validation
    }
}
```

---

### 3. Clear Cache When Config Changes

```java
// Change configuration
verifier.setConfig(LinkVerifierConfig.thorough());

// Clear old cached results
verifier.clearCache();

// Fresh validation with new config
verifier.verifyDirectory("doc/", true).block();
```

---

### 4. Use Caching for Interactive Work

```java
// Interactive session
verifier.setConfig(LinkVerifierConfig.builder()
    .cacheHttpResults(true)
    .cacheTtlMs(900000)  // 15 minutes for long session
    .build());

// Work, make changes, re-verify frequently
// Cache speeds up repeated checks
```

---

### 5. Log Configuration in CI/CD

```java
@Slf4j
public class CIValidation {
    public void validate() {
        LinkVerifierConfig config = LinkVerifierConfig.thorough();
        log.info("Using configuration: {}", config);

        verifier.setConfig(config);
        DirectoryVerificationResult result = verifier
            .verifyDirectory("doc/", true)
            .block();

        log.info("Validation result: {}", result.getSummary());
    }
}
```

---

## FAQ

### Q: What's the difference between `fast()` and `filesOnly()`?

**A:**
- `fast()` - Verifies files AND anchors, skips HTTP
- `filesOnly()` - Verifies ONLY files, skips HTTP AND anchors

```java
// fast() - Checks file exists + anchor exists
LinkVerifierConfig.fast()

// filesOnly() - Only checks file exists
LinkVerifierConfig.filesOnly()
```

---

### Q: Should I cache HTTP results in CI/CD?

**A:** Generally **NO** for CI/CD pipelines.

**Rationale:**
- CI/CD should perform fresh validation each run
- Cache doesn't persist across CI/CD runs
- You want to catch newly broken links

**Exception:** If you run multiple verification steps in same CI/CD job, caching between steps is OK.

```java
// CI/CD: Don't cache (each run is fresh)
verifier.setConfig(LinkVerifierConfig.builder()
    .cacheHttpResults(false)  // No cache in CI/CD
    .build());
```

---

### Q: How do I handle very slow HTTP links?

**A:** Three options:

**Option 1: Increase timeout**

```java
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .httpTimeoutMs(15000)  // 15 seconds
    .build();
```

**Option 2: Skip HTTP in fast checks**

```java
verifier.setConfig(LinkVerifierConfig.fast());
// HTTP links skipped
```

**Option 3: Allowlist slow domains**

```java
// Future enhancement: Domain-specific timeouts
// Not currently implemented
```

---

### Q: Can I configure per-file or per-link?

**A:** Not currently. Configuration is **global** for the tool instance.

**Workaround:** Use multiple tool instances with different configs:

```java
@Component
public class MultiConfigService {
    @Inject
    private DocumentationLinkVerifierTool fastVerifier;

    @Inject
    private DocumentationLinkVerifierTool thoroughVerifier;

    @PostConstruct
    public void configure() {
        fastVerifier.setConfig(LinkVerifierConfig.fast());
        thoroughVerifier.setConfig(LinkVerifierConfig.thorough());
    }
}
```

---

### Q: What happens if I don't set config?

**A:** Uses `LinkVerifierConfig.defaults()` automatically.

```java
@Inject
private DocumentationLinkVerifierTool verifier;

// No setConfig() call
// Uses defaults: 5s timeout, 2 retries, verify all, cache enabled
verifier.verifyFile("doc.md").block();
```

---

### Q: Can I change config between verifications?

**A:** Yes, but **clear the cache** if changing HTTP settings.

```java
// First verification: fast
verifier.setConfig(LinkVerifierConfig.fast());
verifier.verifyDirectory("doc/", true).block();

// Change to thorough
verifier.setConfig(LinkVerifierConfig.thorough());
verifier.clearCache();  // Important!
verifier.verifyDirectory("doc/", true).block();
```

---

### Q: How do I know which config to use?

**Decision Tree:**

```
Is this a pre-commit hook?
├─ YES → Use .fast()
└─ NO
   └─ Is this CI/CD nightly job?
      ├─ YES → Use .thorough()
      └─ NO
         └─ Is this interactive CLI?
            ├─ YES → Use .defaults()
            └─ NO → Custom config
```

---

### Q: Can I disable all verification?

**A:** Yes, but why? 😄

```java
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .verifyHttpLinks(false)
    .verifyFileLinks(false)
    .verifyAnchorLinks(false)
    .build();

// Everything is "valid" (no verification)
```

---

## Configuration Examples Summary

|      Scenario       |     Preset     |               Custom Settings                |
|---------------------|----------------|----------------------------------------------|
| Pre-commit hook     | `.fast()`      | -                                            |
| Nightly CI/CD       | `.thorough()`  | -                                            |
| Interactive CLI     | `.defaults()`  | -                                            |
| File structure only | `.filesOnly()` | -                                            |
| External docs       | Custom         | `verifyFileLinks=false, httpTimeoutMs=15000` |
| Flaky network       | Custom         | `httpRetries=5, httpTimeoutMs=10000`         |
| No HTTP             | `.fast()`      | -                                            |
| Long session        | `.defaults()`  | `cacheTtlMs=900000`                          |

---

*Last Updated: 2025-10-25*
*Version: 0.3.0*
