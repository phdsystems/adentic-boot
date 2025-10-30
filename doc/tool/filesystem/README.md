# File System Tool

**Version:** 0.1.0
**Category:** File System Tools
**Status:** Implemented
**Date:** 2025-10-25

---

## TL;DR

**Tool Provider for secure file system operations**. Read/write files, manage directories, search/find files with comprehensive security controls. **Benefits**: Enables AI agents to interact with file systems safely with path validation, size limits, and sandboxing. **Use cases**: Code analysis, documentation management, data processing, build automation, testing workflows.

**Quick start:**

```java
@Inject
private FileSystemTool fileSystem;

FileContent content = fileSystem.readFile("/workspace/config.json").block();
fileSystem.writeFile("/workspace/output.txt", "Hello World").block();
DirectoryListing listing = fileSystem.listDirectory("/workspace").block();
```

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Overview](#overview)
- [Features](#features)
- [Quick Start](#quick-start)
- [Security](#security)
- [Operations](#operations)
- [Configuration](#configuration)
- [Use Cases](#use-cases)
- [Best Practices](#best-practices)

---

## Prerequisites

**Status:** ✅ No additional dependencies required - works out of the box!

The File System Tool is fully functional with the standard Adentic framework dependencies and Java's built-in NIO (New I/O) libraries. No extra Maven dependencies or external tools are needed.

### Required (Already Included)

These are satisfied by the Adentic framework and Java:

- ✅ **Java 21** - Language requirement with NIO.2 (java.nio.file package)
- ✅ **Project Reactor** - For reactive Mono/Flux API (provided by adentic-core)
- ✅ **SLF4J + Logback** - For logging (provided by adentic-boot)

### Operating System Support

The tool uses Java NIO, which works consistently across all platforms:

- ✅ **Linux** - Full support for all file operations
- ✅ **macOS** - Full support for all file operations
- ✅ **Windows** - Full support (paths automatically normalized)

### File System Permissions

The tool respects operating system file permissions:

- **Read operations:** Require read permission on files/directories
- **Write operations:** Require write permission on target location
- **Delete operations:** Require delete permission on files/directories
- **Execute operations:** Require execute permission (for directory traversal)

### Security Considerations

**Important:** The tool includes security controls, but you must configure them:

```java
// Example: Sandboxed configuration
FileSystemConfig config = FileSystemConfig.sandboxed("/workspace");
fileSystem.setConfig(config);
```

**Default configuration allows access to all readable paths.** For production:
1. Use `sandboxed()` or `secure()` presets
2. Configure `allowedRoots` to restrict access
3. Set `blockedPaths` for sensitive directories
4. Enable `validatePaths` (enabled by default)

### Quick Validation

Test that the tool works:

```java
@Inject
private FileSystemTool fileSystem;

public void testFileSystem() throws Exception {
    // Create temp file
    Path tempFile = Files.createTempFile("test", ".txt");
    String path = tempFile.toString();

    // Test write
    fileSystem.writeFile(path, "Hello World").block();

    // Test read
    FileContent content = fileSystem.readFile(path).block();
    System.out.println("File system working: " + content.isSuccess());

    // Cleanup
    Files.delete(tempFile);
}
```

### No External Dependencies

Unlike other file system tools that may require:
- ❌ Apache Commons IO
- ❌ Google Guava Files
- ❌ External CLI tools

This tool uses **only Java's standard library**, ensuring:
- ✅ Zero additional dependencies
- ✅ Minimal attack surface
- ✅ Maximum compatibility
- ✅ Predictable behavior

---

## Overview

The File System Tool is a **Tool Provider** implementation in the Adentic Framework that enables AI agents to perform file system operations with comprehensive security controls.

**Purpose:** Provide safe, controlled file system access for:
- Reading and analyzing files
- Creating and modifying content
- Managing directories
- Searching and filtering files
- Processing data files

**Key Benefits:**
- ✅ Comprehensive operations (read, write, delete, copy, move, search)
- ✅ Security controls (path validation, size limits, sandboxing)
- ✅ Async/reactive API (Mono-based)
- ✅ Configuration presets (defaults, readOnly, permissive, sandboxed)
- ✅ Agent-accessible (via @Tool annotation)
- ✅ Path traversal protection
- ✅ Whitelist/blacklist directories
- ✅ Operation timeouts and limits

---

## Features

### File Operations

|    Operation    |           Method            |         Description         |
|-----------------|-----------------------------|-----------------------------|
| **Read**        | `readFile(path)`            | Read file content as string |
| **Read Binary** | `readFileBytes(path)`       | Read file content as bytes  |
| **Write**       | `writeFile(path, content)`  | Write content to file       |
| **Append**      | `appendFile(path, content)` | Append content to file      |
| **Delete**      | `deleteFile(path)`          | Delete a file               |
| **Copy**        | `copyFile(source, target)`  | Copy file to new location   |
| **Move**        | `moveFile(source, target)`  | Move/rename file            |

### Directory Operations

| Operation  |               Method               |       Description       |
|------------|------------------------------------|-------------------------|
| **List**   | `listDirectory(path)`              | List directory contents |
| **Create** | `createDirectory(path, parents)`   | Create directory        |
| **Delete** | `deleteDirectory(path, recursive)` | Delete directory        |

### Information Operations

| Operation  |       Method        |     Description      |
|------------|---------------------|----------------------|
| **Info**   | `getFileInfo(path)` | Get file metadata    |
| **Exists** | `exists(path)`      | Check if path exists |

### Search Operations

| Operation |        Method        |             Description             |
|-----------|----------------------|-------------------------------------|
| **Find**  | `findFiles(request)` | Search files by pattern, size, date |

---

## Quick Start

### 1. Inject the Tool

```java
import dev.adeengineer.adentic.tool.filesystem.FileSystemTool;

@Component
public class MyService {

    @Inject
    private FileSystemTool fileSystem;
}
```

### 2. Read Files

```java
// Read text file
FileContent content = fileSystem.readFile("/workspace/data.txt").block();
System.out.println(content.getContent());

// Read binary file
FileContent binary = fileSystem.readFileBytes("/workspace/image.png").block();
byte[] bytes = binary.getBytes();
```

### 3. Write Files

```java
// Write new file
fileSystem.writeFile("/workspace/output.txt", "Hello World").block();

// Append to file
fileSystem.appendFile("/workspace/log.txt", "New log entry\n").block();
```

### 4. Directory Operations

```java
// List directory
DirectoryListing listing = fileSystem.listDirectory("/workspace").block();
System.out.println(listing.getSummary());

// Create directory
fileSystem.createDirectory("/workspace/new-dir", true).block();
```

### 5. Search Files

```java
FileSearchRequest request = FileSearchRequest.builder()
    .directory(Paths.get("/workspace"))
    .pattern("*.java")
    .recursive(true)
    .build();

List<FileInfo> files = fileSystem.findFiles(request).block();
```

---

## Security

### Security Features

**Path Validation:**
- Path traversal protection (no `../..`)
- Absolute path normalization
- Null byte detection

**Access Control:**
- Whitelist allowed directories
- Blacklist system directories
- Per-operation permissions (read, write, delete)

**Limits:**
- Max file size (read: 10 MB, write: 10 MB)
- Max search results (1,000 items)
- Max recursion depth (10 levels)
- Operation timeouts (30 seconds)

**Extension Blocking:**
- Block dangerous file types
- Configurable extension blacklist

### Configuration Presets

```java
// Default: Balanced security
fileSystem.setConfig(FileSystemConfig.defaults());

// Read-only: No modifications
fileSystem.setConfig(FileSystemConfig.readOnly());

// Permissive: Higher limits, all operations
fileSystem.setConfig(FileSystemConfig.permissive());

// Sandboxed: Restrict to specific directory
fileSystem.setConfig(FileSystemConfig.sandboxed("/workspace"));

// Secure: Maximum restrictions
fileSystem.setConfig(FileSystemConfig.secure());

// Temp directory: Restrict to system temp
fileSystem.setConfig(FileSystemConfig.tempDirectory());
```

---

## Operations

### File Read/Write

```java
// Read file
FileContent content = fileSystem.readFile("/path/to/file.txt").block();

if (content.isSuccess()) {
    System.out.println("Content: " + content.getContent());
    System.out.println("Size: " + content.getSize() + " bytes");
    System.out.println("Modified: " + content.getLastModified());
}

// Write file
FileOperationResult result = fileSystem
    .writeFile("/path/to/output.txt", "Hello World")
    .block();

System.out.println(result);  // ✅ Wrote file: /path/to/output.txt
```

### Directory Management

```java
// List directory
DirectoryListing listing = fileSystem.listDirectory("/workspace").block();

System.out.println(listing.getDetailedReport());
// Output:
// 📁 workspace: 5 directories, 12 files (1.23 MB)
//
// Directories:
//   📁 src
//   📁 doc
//   ...
//
// Files:
//   📄 README.md (4.5 KB)
//   📄 pom.xml (2.1 KB)
//   ...
```

### File Search

```java
// Find Java files modified in last 7 days
FileSearchRequest request = FileSearchRequest.builder()
    .directory(Paths.get("/workspace/src"))
    .pattern("*.java")
    .recursive(true)
    .modifiedAfter(Instant.now().minus(7, ChronoUnit.DAYS))
    .maxResults(100)
    .build();

List<FileInfo> files = fileSystem.findFiles(request).block();

for (FileInfo file : files) {
    System.out.println(file.getName() + " - " + file.getSize() + " bytes");
}
```

---

## Configuration

### Custom Configuration

```java
FileSystemConfig config = FileSystemConfig.builder()
    .allowedRoots(Set.of(Paths.get("/workspace")))
    .allowRead(true)
    .allowWrite(true)
    .allowDelete(false)                    // Disable delete
    .maxReadSize(50 * 1024 * 1024)         // 50 MB
    .maxWriteSize(10 * 1024 * 1024)        // 10 MB
    .maxResults(5000)
    .maxDepth(20)
    .calculateHashes(true)                 // Enable SHA-256 hashing
    .blockedExtensions(Set.of(".exe", ".dll"))
    .build();

fileSystem.setConfig(config);
```

---

## Use Cases

### 1. Code Analysis Agent

```java
@AgentService
public class CodeAnalyzer {
    @Inject private FileSystemTool fileSystem;

    public Mono<AnalysisReport> analyzeCodebase(String projectPath) {
        return fileSystem.findFiles(FileSearchRequest.builder()
            .directory(Paths.get(projectPath))
            .pattern("*.java")
            .recursive(true)
            .build())
            .map(files -> analyzeFiles(files));
    }
}
```

### 2. Documentation Generator

```java
@AgentService
public class DocGenerator {
    @Inject private FileSystemTool fileSystem;
    @Inject @LLM private TextGenerationProvider llm;

    public Mono<Void> generateDocs(String sourceDir, String outputDir) {
        return fileSystem.findFiles(/* search for source files */)
            .flatMap(files -> generateDocsForFiles(files))
            .flatMap(docs -> fileSystem.writeFile(outputDir + "/docs.md", docs));
    }
}
```

### 3. Data Processing Agent

```java
@AgentService
public class DataProcessor {
    @Inject private FileSystemTool fileSystem;

    public Mono<Void> processCSVFiles(String inputDir, String outputDir) {
        return fileSystem.findFiles(FileSearchRequest.builder()
            .directory(Paths.get(inputDir))
            .pattern("*.csv")
            .build())
            .flatMapMany(Flux::fromIterable)
            .flatMap(file -> processFile(file, outputDir))
            .then();
    }
}
```

---

## Best Practices

### 1. Use Sandboxing

```java
// Restrict agent to specific directory
fileSystem.setConfig(FileSystemConfig.sandboxed("/workspace/agent-data"));
```

### 2. Read-Only for Analysis

```java
// Use read-only config for code analysis
fileSystem.setConfig(FileSystemConfig.readOnly());
```

### 3. Validate Paths

```java
// Always validate user-provided paths
try {
    FileContent content = fileSystem.readFile(userPath).block();
} catch (SecurityException e) {
    log.error("Access denied: {}", e.getMessage());
}
```

### 4. Handle Errors

```java
FileContent content = fileSystem.readFile(path).block();

if (!content.isSuccess()) {
    log.error("Failed to read file: {}", content.getError());
    return;
}

// Process content
String text = content.getContent();
```

### 5. Limit Search Results

```java
// Always set maxResults to prevent memory issues
FileSearchRequest request = FileSearchRequest.builder()
    .directory(Paths.get("/large-directory"))
    .maxResults(1000)  // Limit results
    .build();
```

---

## Version History

| Version |    Date    |                           Changes                           |
|---------|------------|-------------------------------------------------------------|
| 0.1.0   | 2025-10-25 | Initial implementation                                      |
|         |            | - File operations (read, write, append, delete, copy, move) |
|         |            | - Directory operations (list, create, delete)               |
|         |            | - Search operations (find by pattern, size, date)           |
|         |            | - Security controls (path validation, sandboxing, limits)   |
|         |            | - Configuration presets                                     |

---

## License

Part of the Adentic Framework.
See main project [LICENSE](../../../LICENSE) for details.

---

*Last Updated: 2025-10-25*
*Version: 0.1.0*
*Status: Implemented*
