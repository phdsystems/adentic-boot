# Usage Guide - Documentation Link Verifier Tool

**Date:** 2025-10-25
**Version:** 0.3.0

---

## TL;DR

**Complete API usage guide**. Covers injection, basic verification, advanced patterns, error handling, async operations, and integration with agents. **Quick ref**: `verifyFile()` for single files, `verifyDirectory()` for bulk, `setConfig()` for customization.

---

## Table of Contents

- [Getting Started](#getting-started)
- [Basic Operations](#basic-operations)
- [Advanced Usage](#advanced-usage)
- [Error Handling](#error-handling)
- [Async Operations](#async-operations)
- [Agent Integration](#agent-integration)
- [Performance Tuning](#performance-tuning)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)

---

## Getting Started

### Step 1: Dependency Injection

The tool is automatically discovered and registered when using Adentic Boot.

**Spring Boot:**

```java
import dev.adeengineer.adentic.tool.DocumentationLinkVerifierTool;

@Component
public class MyService {

    @Inject
    private DocumentationLinkVerifierTool linkVerifier;

    // Use the tool
}
```

**Quarkus:**

```java
@ApplicationScoped
public class MyService {

    @Inject
    DocumentationLinkVerifierTool linkVerifier;

    // Use the tool
}
```

**Pure Adentic Boot:**

```java
@Component
public class MyService {

    @Inject
    private DocumentationLinkVerifierTool linkVerifier;

    // Use the tool
}
```

### Step 2: Basic Verification

```java
// Verify a single file
LinkVerificationResult result = linkVerifier
    .verifyFile("doc/architecture.md")
    .block();

// Check if valid
if (result.isValid()) {
    System.out.println("✅ All links valid!");
} else {
    System.out.println("❌ Found broken links:");
    result.getBrokenLinks().forEach(System.out::println);
}
```

### Step 3: Handle Results

```java
// Get summary
System.out.println(result.getSummary());
// Output: ✅ doc/architecture.md: All 15 links valid (files: 10, HTTP: 3, anchors: 2) [123ms]

// Get detailed report
System.out.println(result.getDetailedReport());

// Access statistics
int total = result.getTotalLinks();
int fileLinks = result.getFileLinks();
int httpLinks = result.getHttpLinks();
int anchorLinks = result.getAnchorLinks();
```

---

## Basic Operations

### Verify Single File

```java
/**
 * Verifies all links in a single markdown file.
 *
 * @param filePath Path to markdown file (relative or absolute)
 * @return LinkVerificationResult with statistics and broken links
 */
Mono<LinkVerificationResult> verifyFile(String filePath)
```

**Example:**

```java
// Relative path
LinkVerificationResult result1 = linkVerifier
    .verifyFile("doc/architecture.md")
    .block();

// Absolute path
LinkVerificationResult result2 = linkVerifier
    .verifyFile("/home/user/project/doc/guide.md")
    .block();

// Check results
if (!result1.isValid()) {
    System.err.println("Broken links found in architecture.md:");
    result1.getBrokenLinks().forEach(broken -> {
        System.err.println("  " + broken.getLink().getTarget()
            + " at line " + broken.getLink().getLineNumber());
        System.err.println("  Reason: " + broken.getReason());
        if (broken.getSuggestion() != null) {
            System.err.println("  Suggestion: " + broken.getSuggestion());
        }
    });
}
```

### Verify Directory

```java
/**
 * Verifies all markdown files in a directory.
 *
 * @param directoryPath Path to directory
 * @param recursive Whether to scan subdirectories
 * @return DirectoryVerificationResult with aggregated statistics
 */
Mono<DirectoryVerificationResult> verifyDirectory(String directoryPath, boolean recursive)
```

**Example:**

```java
// Non-recursive (only files in directory)
DirectoryVerificationResult result1 = linkVerifier
    .verifyDirectory("doc/", false)
    .block();

// Recursive (all markdown files in tree)
DirectoryVerificationResult result2 = linkVerifier
    .verifyDirectory("doc/", true)
    .block();

// Print summary
System.out.println(result2.getSummary());
// Output: ✅ Verified 47 files (124 links total) - All valid! [2341ms]

// Get files with broken links
List<LinkVerificationResult> broken = result2.getFilesWithBrokenLinks();
if (!broken.isEmpty()) {
    System.out.println("\nFiles with broken links:");
    broken.forEach(fileResult -> {
        System.out.println("  " + fileResult.getFilePath()
            + " (" + fileResult.getBrokenLinks().size() + " broken)");
    });
}
```

### Extract Links (Without Validation)

```java
/**
 * Extracts all links from markdown content without validation.
 *
 * @param content Markdown content
 * @return List of MarkdownLink objects
 */
Mono<List<MarkdownLink>> extractLinks(String content)
```

**Example:**

```java
String markdown = """
    # Documentation

    See [Architecture](architecture.md) and [API](https://api.example.com).
    Jump to [Overview](#overview) section.
    """;

List<MarkdownLink> links = linkVerifier
    .extractLinks(markdown)
    .block();

links.forEach(link -> {
    System.out.println("Found link:");
    System.out.println("  Text: " + link.getText());
    System.out.println("  Target: " + link.getTarget());
    System.out.println("  Type: " + link.getType());
    System.out.println("  Line: " + link.getLineNumber());
});

// Output:
// Found link:
//   Text: Architecture
//   Target: architecture.md
//   Type: FILE
//   Line: 3
// Found link:
//   Text: API
//   Target: https://api.example.com
//   Type: HTTP
//   Line: 3
// Found link:
//   Text: Overview
//   Target: #overview
//   Type: ANCHOR
//   Line: 4
```

### Validate Single Link

```java
/**
 * Validates a single link target.
 *
 * @param fromFile Source file path (for relative path resolution)
 * @param targetLink Target link to validate
 * @return true if valid, false if broken
 */
Mono<Boolean> validateLink(String fromFile, String targetLink)
```

**Example:**

```java
// Check if a specific link is valid
boolean isValid = linkVerifier
    .validateLink("doc/architecture.md", "decisions/0001.md")
    .block();

if (isValid) {
    System.out.println("✅ Link is valid");
} else {
    System.out.println("❌ Link is broken");
}
```

---

## Advanced Usage

### Custom Configuration

```java
import dev.adeengineer.adentic.tool.config.LinkVerifierConfig;

// Create custom configuration
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .httpTimeoutMs(10000)           // 10 second timeout
    .httpRetries(3)                 // 3 retry attempts
    .verifyHttpLinks(true)          // Verify HTTP links
    .verifyFileLinks(true)          // Verify file links
    .verifyAnchorLinks(true)        // Verify anchor links
    .followRedirects(true)          // Follow HTTP redirects
    .maxRedirects(5)                // Max 5 redirects
    .cacheHttpResults(true)         // Cache HTTP results
    .cacheTtlMs(600000)             // 10 minute cache TTL
    .userAgent("MyApp/1.0")         // Custom user agent
    .build();

// Apply configuration
linkVerifier.setConfig(config);

// Now all verifications use this configuration
DirectoryVerificationResult result = linkVerifier
    .verifyDirectory("doc/", true)
    .block();
```

### Using Configuration Presets

```java
// Fast mode - skip HTTP verification, 1s timeout
linkVerifier.setConfig(LinkVerifierConfig.fast());
LinkVerificationResult fastResult = linkVerifier
    .verifyFile("doc/guide.md")
    .block();

// Thorough mode - 10s timeout, 3 retries
linkVerifier.setConfig(LinkVerifierConfig.thorough());
DirectoryVerificationResult thoroughResult = linkVerifier
    .verifyDirectory("doc/", true)
    .block();

// Files only - no HTTP verification
linkVerifier.setConfig(LinkVerifierConfig.filesOnly());
DirectoryVerificationResult filesOnly = linkVerifier
    .verifyDirectory("doc/", true)
    .block();

// Reset to defaults
linkVerifier.setConfig(LinkVerifierConfig.defaults());
```

### Selective Verification

```java
// Only verify HTTP links, skip files and anchors
LinkVerifierConfig httpOnly = LinkVerifierConfig.builder()
    .verifyHttpLinks(true)
    .verifyFileLinks(false)
    .verifyAnchorLinks(false)
    .build();

linkVerifier.setConfig(httpOnly);

// Only verify external links (HTTP/HTTPS)
LinkVerifierConfig externalOnly = LinkVerifierConfig.builder()
    .verifyExternalLinks(true)
    .verifyFileLinks(false)
    .verifyAnchorLinks(false)
    .build();

linkVerifier.setConfig(externalOnly);
```

### Cache Management

```java
// Clear HTTP cache (forces fresh validation)
linkVerifier.clearCache();

// Verify with fresh cache
DirectoryVerificationResult result = linkVerifier
    .verifyDirectory("doc/", true)
    .block();

// Disable caching for one-time verification
LinkVerifierConfig noCache = LinkVerifierConfig.builder()
    .cacheHttpResults(false)
    .build();

linkVerifier.setConfig(noCache);
```

---

## Error Handling

### Handling Broken Links

```java
LinkVerificationResult result = linkVerifier
    .verifyFile("doc/architecture.md")
    .block();

if (!result.isValid()) {
    result.getBrokenLinks().forEach(broken -> {
        MarkdownLink link = broken.getLink();

        System.out.println("Broken Link Details:");
        System.out.println("  Text: " + link.getText());
        System.out.println("  Target: " + link.getTarget());
        System.out.println("  Type: " + link.getType());
        System.out.println("  Line: " + link.getLineNumber());
        System.out.println("  Reason: " + broken.getReason());

        if (broken.getSuggestion() != null) {
            System.out.println("  Suggestion: " + broken.getSuggestion());
        }

        if (broken.getHttpStatusCode() != null) {
            System.out.println("  HTTP Status: " + broken.getHttpStatusCode());
        }
    });
}
```

### Handling Verification Errors

```java
try {
    LinkVerificationResult result = linkVerifier
        .verifyFile("nonexistent.md")
        .block();
} catch (Exception e) {
    System.err.println("Verification failed: " + e.getMessage());

    if (e instanceof java.io.IOException) {
        System.err.println("File not found or not readable");
    }
}
```

### Error Types and Handling

```java
result.getBrokenLinks().forEach(broken -> {
    switch (broken.getLink().getType()) {
        case FILE:
        case FILE_WITH_ANCHOR:
            System.err.println("Broken file link: " + broken.getTarget());
            // Suggestion: Check if file was moved or renamed
            break;

        case HTTP:
            if (broken.getHttpStatusCode() != null) {
                int status = broken.getHttpStatusCode();
                if (status == 404) {
                    System.err.println("URL not found (404): " + broken.getTarget());
                } else if (status >= 500) {
                    System.err.println("Server error (" + status + "): " + broken.getTarget());
                    // May be temporary, retry later
                }
            } else {
                System.err.println("HTTP request failed: " + broken.getReason());
                // Timeout or network error
            }
            break;

        case ANCHOR:
            System.err.println("Anchor not found: " + broken.getAnchor());
            // Suggestion: Check if section heading exists
            break;
    }
});
```

---

## Async Operations

### Reactive Composition

```java
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

// Chain multiple verifications
Mono<String> result = linkVerifier.verifyFile("doc/architecture.md")
    .flatMap(archResult -> {
        if (archResult.isValid()) {
            return linkVerifier.verifyFile("doc/guide.md");
        } else {
            return Mono.error(new RuntimeException("Architecture doc has broken links"));
        }
    })
    .map(guideResult -> {
        if (guideResult.isValid()) {
            return "✅ All documentation valid";
        } else {
            return "❌ Guide has " + guideResult.getBrokenLinks().size() + " broken links";
        }
    });

String message = result.block();
System.out.println(message);
```

### Parallel Verification

```java
import reactor.core.publisher.Flux;
import java.util.List;

// Verify multiple files in parallel
List<String> files = List.of(
    "doc/architecture.md",
    "doc/guide.md",
    "doc/api.md",
    "doc/examples.md"
);

Flux<LinkVerificationResult> results = Flux.fromIterable(files)
    .flatMap(file -> linkVerifier.verifyFile(file))
    .doOnNext(result -> {
        System.out.println(result.getSummary());
    });

// Collect results
List<LinkVerificationResult> allResults = results.collectList().block();

// Check if any failed
boolean anyBroken = allResults.stream()
    .anyMatch(r -> !r.isValid());

if (anyBroken) {
    System.err.println("❌ Some files have broken links");
} else {
    System.out.println("✅ All files verified successfully");
}
```

### Non-Blocking Verification

```java
// Start verification without blocking
linkVerifier.verifyDirectory("doc/", true)
    .subscribe(
        result -> {
            // onSuccess
            System.out.println("Verification complete: " + result.getSummary());
        },
        error -> {
            // onError
            System.err.println("Verification failed: " + error.getMessage());
        }
    );

// Continue with other work while verification runs
System.out.println("Verification started in background...");
```

---

## Agent Integration

### Documentation Maintenance Agent

```java
import dev.adeengineer.adentic.boot.annotations.AgentService;

@AgentService
public class DocumentationMaintenanceAgent {

    @Inject
    private DocumentationLinkVerifierTool linkVerifier;

    @Inject
    @LLM(name = "claude")
    private TextGenerationProvider llm;

    public Mono<AgentResult> validateAndFixDocumentation(String docsPath) {
        return linkVerifier.verifyDirectory(docsPath, true)
            .flatMap(result -> {
                if (result.isValid()) {
                    return Mono.just(AgentResult.success(
                        "All documentation links are valid"
                    ));
                }

                // Build prompt for LLM
                String prompt = buildFixPrompt(result);

                // Ask LLM to suggest fixes
                return llm.generate(prompt)
                    .map(suggestions -> AgentResult.withSuggestions(
                        "Found broken links",
                        suggestions
                    ));
            });
    }

    private String buildFixPrompt(DirectoryVerificationResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("Found broken links in documentation:\n\n");

        result.getFilesWithBrokenLinks().forEach(fileResult -> {
            sb.append("File: ").append(fileResult.getFilePath()).append("\n");
            fileResult.getBrokenLinks().forEach(broken -> {
                sb.append("  - Line ").append(broken.getLink().getLineNumber())
                  .append(": ").append(broken.getLink().getTarget())
                  .append(" (").append(broken.getReason()).append(")\n");
            });
            sb.append("\n");
        });

        sb.append("Please suggest fixes for these broken links.");
        return sb.toString();
    }
}
```

### Scheduled Verification Agent

```java
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class ScheduledDocVerificationAgent {

    @Inject
    DocumentationLinkVerifierTool linkVerifier;

    @Inject
    NotificationService notificationService;

    @Scheduled(cron = "0 0 2 * * ?")  // Daily at 2 AM
    public void nightlyVerification() {
        DirectoryVerificationResult result = linkVerifier
            .verifyDirectory("docs/", true)
            .block();

        if (!result.isValid()) {
            String report = result.getDetailedReport();
            notificationService.sendAlert(
                "Broken Documentation Links",
                report
            );
        }
    }
}
```

---

## Performance Tuning

### Fast Pre-Commit Verification

```java
// For pre-commit hooks - verify quickly, no HTTP
linkVerifier.setConfig(LinkVerifierConfig.fast());

DirectoryVerificationResult result = linkVerifier
    .verifyDirectory("doc/", true)
    .block();

if (!result.isValid()) {
    System.exit(1);  // Reject commit
}
```

### Thorough Nightly Verification

```java
// For nightly CI/CD - thorough verification with HTTP
linkVerifier.setConfig(LinkVerifierConfig.thorough());

DirectoryVerificationResult result = linkVerifier
    .verifyDirectory("doc/", true)
    .block();

// Generate report
Files.writeString(
    Path.of("link-verification-report.txt"),
    result.getDetailedReport()
);
```

### Optimizing HTTP Verification

```java
// Enable caching for repeated verifications
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .cacheHttpResults(true)
    .cacheTtlMs(600000)  // 10 minute cache
    .httpTimeoutMs(3000) // Shorter timeout
    .httpRetries(1)      // Fewer retries
    .build();

linkVerifier.setConfig(config);

// First run - slow (validates HTTP)
DirectoryVerificationResult run1 = linkVerifier
    .verifyDirectory("doc/", true)
    .block();
System.out.println("First run: " + run1.getTotalVerificationTimeMs() + "ms");

// Second run - fast (uses cache)
DirectoryVerificationResult run2 = linkVerifier
    .verifyDirectory("doc/", true)
    .block();
System.out.println("Second run: " + run2.getTotalVerificationTimeMs() + "ms");
```

---

## Best Practices

### 1. Choose Appropriate Configuration

```java
// Pre-commit hooks
linkVerifier.setConfig(LinkVerifierConfig.fast());

// CI/CD pipelines
linkVerifier.setConfig(LinkVerifierConfig.thorough());

// Production monitoring
linkVerifier.setConfig(LinkVerifierConfig.defaults());
```

### 2. Handle Errors Gracefully

```java
try {
    LinkVerificationResult result = linkVerifier
        .verifyFile("doc/architecture.md")
        .block();

    if (!result.isValid()) {
        // Log but don't fail
        log.warn("Found {} broken links in architecture.md",
            result.getBrokenLinks().size());
    }
} catch (Exception e) {
    log.error("Verification failed", e);
    // Don't let verification failures break the build
}
```

### 3. Use Appropriate Timeouts

```java
// For external documentation
LinkVerifierConfig external = LinkVerifierConfig.builder()
    .httpTimeoutMs(10000)  // 10s for external sites
    .httpRetries(3)
    .build();

// For internal documentation
LinkVerifierConfig internal = LinkVerifierConfig.builder()
    .httpTimeoutMs(2000)   // 2s for internal sites
    .httpRetries(1)
    .build();
```

### 4. Clear Cache When Needed

```java
// Clear cache before critical verification
linkVerifier.clearCache();

DirectoryVerificationResult result = linkVerifier
    .verifyDirectory("doc/", true)
    .block();
```

### 5. Use Reactive Patterns

```java
// Don't block unnecessarily
linkVerifier.verifyDirectory("doc/", true)
    .doOnSuccess(result -> log.info("Verification complete: {}", result.getSummary()))
    .doOnError(error -> log.error("Verification failed", error))
    .subscribe();
```

---

## Troubleshooting

### Issue: "File not found" errors

**Problem:** Links are reported as broken but files exist

**Solution:**

```java
// Check current working directory
System.out.println("Working directory: " + System.getProperty("user.dir"));

// Use absolute paths
linkVerifier.verifyFile(
    Path.of("doc/architecture.md").toAbsolutePath().toString()
).block();
```

### Issue: HTTP timeouts

**Problem:** HTTP verification is too slow

**Solutions:**

```java
// Option 1: Increase timeout
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .httpTimeoutMs(15000)  // 15 seconds
    .build();

// Option 2: Skip HTTP verification
linkVerifier.setConfig(LinkVerifierConfig.filesOnly());

// Option 3: Use fast mode
linkVerifier.setConfig(LinkVerifierConfig.fast());
```

### Issue: False positives for valid links

**Problem:** Valid HTTP links reported as broken

**Solutions:**

```java
// 1. Check if site blocks automated requests
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    .build();

// 2. Disable HTTP verification for specific domains
// (requires custom implementation)
```

### Issue: Anchor links not found

**Problem:** Anchors exist but reported as broken

**Check:** GitHub markdown anchor normalization rules

```java
// Heading: "## 7. Bean Scopes and Lifecycle"
// Anchor: #7-bean-scopes-and-lifecycle (lowercase, hyphens, no special chars)

// Correct:  [Link](#7-bean-scopes-and-lifecycle)
// Incorrect: [Link](#7-bean-scopes-and-lifecycle)
```

---

*Last Updated: 2025-10-25*
*Version: 0.3.0*
