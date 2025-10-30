# Architecture - Documentation Link Verifier Tool

**Date:** 2025-10-25
**Version:** 0.3.0

---

## TL;DR

**4-layer architecture**: Tool Provider (@Tool) → Validators (HTTP, Anchor) → Extractors (Regex) → Models (Results). **Async/reactive** using Mono. **Stateless** with optional HTTP caching. **Configurable** via LinkVerifierConfig.

---

## Component Architecture

```
┌────────────────────────────────────────────────────────────────┐
│ DocumentationLinkVerifierTool (@Tool, @Component)              │
│ - Main entry point                                              │
│ - Coordinates validation                                        │
│ - Manages configuration                                         │
└────────────────┬───────────────────────────────────────────────┘
                 │
                 ├── Uses ────> LinkVerifierConfig
                 │              - HTTP timeout, retries
                 │              - Feature flags
                 │              - Caching settings
                 │
                 ├── Uses ────> MarkdownLinkExtractor
                 │              - Regex-based extraction
                 │              - Link type detection
                 │              - Line number tracking
                 │
                 ├── Uses ────> HttpLinkValidator
                 │              - HEAD requests
                 │              - Timeout handling
                 │              - Retry logic
                 │              - Result caching
                 │
                 └── Uses ────> AnchorValidator
                                - Heading extraction
                                - Anchor normalization
                                - Section verification
```

---

## Class Diagram

```
┌─────────────────────────────────────┐
│ DocumentationLinkVerifierTool       │
│ ────────────────────────────────────│
│ + verifyFile(path): Mono<Result>    │
│ + verifyDirectory(...): Mono<Result>│
│ + extractLinks(...): Mono<List>     │
│ + setConfig(config): void           │
└─────────────────┬───────────────────┘
                  │
        ┌─────────┼─────────┬─────────┐
        │         │         │         │
        ▼         ▼         ▼         ▼
┌──────────┐ ┌─────────┐ ┌──────────┐ ┌──────────┐
│MarkdownL │ │HttpLink │ │AnchorVal │ │LinkVerif │
│inkExtrac │ │Validato │ │idator    │ │ierConfig │
│tor       │ │r        │ │          │ │          │
└──────────┘ └─────────┘ └──────────┘ └──────────┘

Models:
┌──────────────┐  ┌─────────────┐  ┌──────────────────────┐
│ MarkdownLink │  │ BrokenLink  │  │ LinkVerificationResult│
├──────────────┤  ├─────────────┤  ├──────────────────────┤
│ - text       │  │ - link      │  │ - filePath            │
│ - target     │  │ - reason    │  │ - totalLinks          │
│ - lineNumber │  │ - suggestion│  │ - brokenLinks         │
│ - type       │  │ - httpStatus│  │ - valid               │
│ - anchor     │  └─────────────┘  └──────────────────────┘
└──────────────┘
```

---

## Verification Flow

### Single File Verification

```
verifyFile(path)
      │
      ▼
[Read file content]
      │
      ▼
[Extract all links] ────> MarkdownLinkExtractor
      │                   (Regex parsing)
      ▼
[For each link]
      │
      ├─> FILE link? ────> Check file exists
      │                    Check anchor (if present)
      │
      ├─> HTTP link? ────> HttpLinkValidator
      │                    - HEAD request
      │                    - Check cache
      │                    - Retry on failure
      │
      └─> ANCHOR link? ──> AnchorValidator
                           - Extract headings
                           - Normalize anchor
                           - Match exists?
      │
      ▼
[Collect broken links]
      │
      ▼
[Return Result]
  - totalLinks
  - brokenLinks[]
  - statistics
```

### Directory Verification

```
verifyDirectory(path, recursive)
      │
      ▼
[Find all .md files]
  (recursive walk if enabled)
      │
      ▼
[For each file in parallel]
      │
      ├─> verifyFile(file1) ──> Result1
      ├─> verifyFile(file2) ──> Result2
      └─> verifyFile(fileN) ──> ResultN
      │
      ▼
[Aggregate results]
      │
      ▼
[DirectoryVerificationResult]
  - totalFiles
  - totalLinks
  - totalBroken
  - fileResults[]
```

---

## Key Design Decisions

### 1. Async/Reactive (Mono)

**Decision:** Use `Mono<T>` return types

**Rationale:**
- Non-blocking I/O for HTTP requests
- Composable with other reactive components
- Enables parallel processing
- Framework standard (Reactor Core)

**Example:**

```java
Mono<LinkVerificationResult> result = verifier.verifyFile("doc.md");
```

### 2. Stateless with Caching

**Decision:** Tool is stateless except optional HTTP cache

**Rationale:**
- Thread-safe
- No state corruption
- Easy to test
- Cache improves performance for repeated verifications

**Cache Implementation:**

```java
private final Map<String, CacheEntry> cache = new HashMap<>();

static class CacheEntry {
    BrokenLink result;
    long expiryTime;
}
```

### 3. Configuration via Builder Pattern

**Decision:** Use LinkVerifierConfig with presets

**Rationale:**
- Flexible configuration
- Preset patterns (fast, thorough, filesOnly)
- Immutable config objects
- Clear intent

**Example:**

```java
LinkVerifierConfig config = LinkVerifierConfig.builder()
    .httpTimeoutMs(5000)
    .httpRetries(2)
    .build();
```

### 4. Separation of Concerns

|   Component    |      Responsibility      |
|----------------|--------------------------|
| **Tool**       | Orchestration, file I/O  |
| **Extractor**  | Parsing markdown, regex  |
| **Validators** | Link-specific validation |
| **Models**     | Data structures, results |
| **Config**     | Behavior configuration   |

---

## HTTP Validation Strategy

### Request Flow

```
validate(link)
      │
      ▼
[Check cache]
      │
  Cached? ──Yes──> Return cached result
      │
      No
      │
      ▼
[Attempt 1: HEAD request]
      │
  Success? ──Yes──> Cache & return null
      │
      No (timeout/error)
      │
      ▼
[Wait 500ms]
      │
      ▼
[Attempt 2: HEAD request]
      │
  Success? ──Yes──> Cache & return null
      │
      No
      │
      ▼
[Wait 1000ms]
      │
      ▼
[Attempt 3: HEAD request]
      │
  Success? ──Yes──> Cache & return null
      │
      No
      │
      ▼
[Return BrokenLink with error]
```

**Exponential Backoff:** 500ms, 1000ms, 1500ms

**Status Codes:**
- 2xx: Valid
- 3xx: Valid (if following redirects)
- 4xx: Broken (client error)
- 5xx: Broken (server error)

---

## Anchor Normalization

GitHub markdown anchor rules:

```
Heading: "## 7. Bean Scopes and Lifecycle"

Normalization steps:
1. Lowercase:         "7. bean scopes and lifecycle"
2. Spaces to hyphens: "7.-bean-scopes-and-lifecycle"
3. Remove special:    "7-bean-scopes-and-lifecycle"
4. Collapse hyphens:  "7-bean-scopes-and-lifecycle"

Anchor: #7-bean-scopes-and-lifecycle
```

**Algorithm:**

```java
String anchor = heading
    .toLowerCase()
    .replaceAll("\\s+", "-")
    .replaceAll("[^a-z0-9\\-_]", "")
    .replaceAll("-+", "-")
    .replaceAll("^-|-$", "");
```

---

## Error Handling

### File Link Errors

```java
// File not found
BrokenLink.fileNotFound(link, path)
  └─> Suggestion: "Check if file was moved or renamed"

// File found, anchor missing
BrokenLink.anchorNotFound(link, anchor)
  └─> Suggestion: "Check if section heading exists"
```

### HTTP Link Errors

```java
// HTTP 404
BrokenLink.httpError(link, 404)
  └─> Suggestion: "URL may have moved or been removed"

// Timeout/Exception
BrokenLink.httpException(link, exception)
  └─> Suggestion: "Check if URL is accessible and valid"
```

---

## Performance Characteristics

### Time Complexity

|     Operation      |  Complexity  |               Notes                |
|--------------------|--------------|------------------------------------|
| Extract links      | O(n)         | n = file size                      |
| Validate file link | O(1)         | File system check                  |
| Validate HTTP link | O(1)         | Network request                    |
| Validate anchor    | O(m)         | m = target file size               |
| Verify file        | O(l × v)     | l = links, v = avg validation time |
| Verify directory   | O(f × l × v) | f = files                          |

### Space Complexity

|    Component    | Space |       Notes       |
|-----------------|-------|-------------------|
| Link extraction | O(l)  | l = links in file |
| HTTP cache      | O(u)  | u = unique URLs   |
| Results         | O(b)  | b = broken links  |

---

## Extension Points

### Custom Validators

```java
public interface LinkValidator {
    BrokenLink validate(MarkdownLink link, Path context);
}

// Custom validator for internal wiki links
public class WikiLinkValidator implements LinkValidator {
    @Override
    public BrokenLink validate(MarkdownLink link, Path context) {
        // Custom validation logic
    }
}
```

### Custom Link Types

```java
public enum LinkType {
    FILE, HTTP, ANCHOR, FILE_WITH_ANCHOR,
    WIKI,        // [[WikiPage]]
    JIRA,        // PROJ-123
    CUSTOM       // Your link type
}
```

---

## Testing Strategy

### Unit Tests

- MarkdownLinkExtractor: Regex parsing
- HttpLinkValidator: Mock HTTP responses
- AnchorValidator: Test heading normalization
- Config: Test presets and builders

### Integration Tests

- Real file system access
- Mock HTTP server for link testing
- End-to-end verification flow

### Test Coverage

Target: 80%+ coverage
- All validators: 100%
- Extractors: 95%
- Tool main class: 80%
- Models: 100%

---

## Future Enhancements

### Planned Features

- [ ] Support for image links (`![alt](image.png)`)
- [ ] Validate relative image paths
- [ ] JSON/XML output format
- [ ] Incremental verification (only changed files)
- [ ] Parallel HTTP requests
- [ ] Configurable HTTP methods (HEAD vs GET)
- [ ] Support for authentication (Bearer tokens)
- [ ] Link suggestion engine (fuzzy matching)

### Possible Optimizations

- [ ] Persistent cache (disk-based)
- [ ] Batch HTTP requests
- [ ] Async file I/O
- [ ] Worker thread pool for parallel validation

---

*Last Updated: 2025-10-25*
*Version: 0.3.0*
