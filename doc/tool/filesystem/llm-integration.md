# File System Tool - LLM Integration Guide

**Version:** 0.1.0
**Category:** Tool Integration
**Status:** Fully Implemented
**Date:** 2025-10-25

---

## TL;DR

**File System Tool enables LLMs to read, write, and manage files through natural language**. Agents convert file operations requests to safe file system calls with path traversal protection, execute operations via Java NIO, and format responses for users. **Benefits**: No external dependencies, path security, recursive operations, content filtering. **Use cases**: File management, code generation, data export, log analysis.

---

## Table of Contents

- [Overview](#overview)
- [Integration Architecture](#integration-architecture)
- [Tool Registration](#tool-registration)
- [LLM Workflow Examples](#llm-workflow-examples)
- [Tool Descriptor Format](#tool-descriptor-format)
- [Parameter Mapping](#parameter-mapping)
- [Security Considerations](#security-considerations)
- [Error Handling](#error-handling)
- [Use Cases](#use-cases)
- [Best Practices](#best-practices)

---

## Overview

The File System Tool integrates with LLM-based agents to provide file system access through natural language. The tool handles path validation, security checks, and file operations while the LLM focuses on understanding user intent and formatting results.

### Integration Flow

```
┌──────────────────────────────────────────────────────────────┐
│                         User Input                            │
│     "Create a Python script that prints Hello World"         │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                      LLM/AI Agent                             │
│  • Analyzes intent: "create file with content"              │
│  • Generates content: Python hello world script             │
│  • Determines path: /workspace/hello.py                     │
│  • Selects tool: filesystem                                 │
│  • Extracts parameters: path, content                       │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                    FileSystemTool                             │
│  • Method: writeFile(path, content)                          │
│  • Security: Path traversal check                           │
│  • Validation: Path exists, writable                        │
│  • Execution: Files.writeString() via Java NIO              │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                     FileOperationResult                       │
│  • success: true                                             │
│  • path: /workspace/hello.py                                │
│  • operation: CREATE                                         │
│  • bytesWritten: 52                                          │
│  • message: "File created successfully"                     │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                      LLM Response                             │
│  "I've created a Python script at /workspace/hello.py        │
│   that prints 'Hello, World!' when executed.                │
│   You can run it with: python hello.py"                     │
└──────────────────────────────────────────────────────────────┘
```

---

## Integration Architecture

### Component Roles

**1. LLM/AI Agent**
- Understands file operation requests
- Generates file content (code, data, etc.)
- Determines appropriate file paths
- Validates user permissions
- Formats results for user presentation

**2. FileSystemTool (@Tool)**
- Provides consistent API for file operations
- Validates paths (no traversal attacks)
- Executes operations via Java NIO
- Returns structured results (FileOperationResult)

**3. Java NIO (Files API)**
- Performs actual file I/O
- Handles permissions and exceptions
- Cross-platform compatibility

### Tool Registration

The File System Tool is registered via the `@Tool` annotation:

```java
@Tool(name = "filesystem")
public class FileSystemTool {
    // 20+ methods for file operations
}
```

This makes it discoverable by the ToolProvider system for LLM agents.

---

## LLM Workflow Examples

### Example 1: Create File with Content

**User Request:**

```
"Create a README file for my project explaining it's a To-Do app"
```

**LLM Analysis:**

```json
{
  "intent": "create_file",
  "operation": "writeFile",
  "parameters": {
    "path": "/workspace/README.md",
    "content": "# To-Do App\n\nA simple task management application...",
    "encoding": "UTF-8"
  }
}
```

**Agent Code:**

```java
String readmeContent = generateReadme(projectContext);

FileOperationResult result = fileSystemTool.writeFile(
    "/workspace/README.md",
    readmeContent,
    StandardCharsets.UTF_8
).block();

// Response to LLM
if (result.isSuccess()) {
    return "Created README.md with project documentation (" +
           result.getBytesProcessed() + " bytes)";
}
```

**Generated Content:**

```markdown
# To-Do App

A simple task management application built with Java and Spring Boot.

## Features
- Add, edit, and delete tasks
- Mark tasks as complete
- Filter tasks by status
...
```

### Example 2: Read and Analyze File

**User Request:**

```
"What does the config.properties file contain?"
```

**LLM Analysis:**

```json
{
  "intent": "read_file",
  "operation": "readFile",
  "parameters": {
    "path": "/workspace/config.properties"
  }
}
```

**Agent Code:**

```java
String content = fileSystemTool.readFile("/workspace/config.properties").block();

// LLM analyzes and summarizes
String summary = analyzeConfigFile(content);
```

**LLM Response:**

```
The config.properties file contains:

**Database Settings:**
- database.url: jdbc:mysql://localhost:3306/myapp
- database.username: app_user

**Server Configuration:**
- server.port: 8080
- server.host: localhost

**Application Settings:**
- app.name: MyApplication
- app.version: 1.0.0
- app.debug: false

[3 categories, 7 total properties]
```

### Example 3: List and Filter Files

**User Request:**

```
"Show me all Java files in the src directory"
```

**LLM Analysis:**

```json
{
  "intent": "list_files",
  "operation": "listFiles",
  "parameters": {
    "directory": "/workspace/src",
    "recursive": true,
    "filter": "*.java"
  }
}
```

**Agent Code:**

```java
List<Path> javaFiles = fileSystemTool.listFiles(
    Path.of("/workspace/src"),
    "*.java",
    true  // recursive
).block();

// LLM organizes by package
Map<String, List<Path>> byPackage = organizeByPackage(javaFiles);
```

**LLM Response:**

```
Found 23 Java files in src/:

**com.example.controller** (5 files)
- UserController.java
- ProductController.java
- OrderController.java
- AuthController.java
- ApiController.java

**com.example.service** (8 files)
- UserService.java
- ProductService.java
...

**com.example.repository** (6 files)
...

**com.example.model** (4 files)
...
```

### Example 4: Copy Files with Filtering

**User Request:**

```
"Copy all markdown files from docs/ to backup/docs/"
```

**LLM Analysis:**

```json
{
  "intent": "copy_files",
  "operation": "copyDirectory",
  "parameters": {
    "source": "/workspace/docs",
    "destination": "/workspace/backup/docs",
    "filter": "*.md",
    "overwrite": false
  }
}
```

**Agent Code:**

```java
FileOperationResult result = fileSystemTool.copyDirectory(
    Path.of("/workspace/docs"),
    Path.of("/workspace/backup/docs"),
    "*.md",
    false  // don't overwrite
).block();
```

**LLM Response:**

```
Successfully copied 12 markdown files from docs/ to backup/docs/:

✓ README.md (5.2 KB)
✓ CONTRIBUTING.md (3.1 KB)
✓ API.md (12.7 KB)
✓ SETUP.md (2.8 KB)
... (8 more files)

Total: 87.3 KB copied
```

### Example 5: Search File Content

**User Request:**

```
"Find all files containing the word 'TODO' in the src directory"
```

**LLM Analysis:**

```json
{
  "intent": "search_files",
  "operation": "searchInFiles",
  "parameters": {
    "directory": "/workspace/src",
    "pattern": "TODO",
    "fileFilter": "*.java"
  }
}
```

**Agent Code:**

```java
Map<Path, List<String>> matches = fileSystemTool.searchInFiles(
    Path.of("/workspace/src"),
    "TODO",
    "*.java"
).block();

// LLM formats findings
String report = formatSearchResults(matches);
```

**LLM Response:**

```
Found 'TODO' in 5 files:

**UserService.java** (Line 42)
   // TODO: Add email validation

**ProductController.java** (Line 89, 156)
   // TODO: Implement pagination
   // TODO: Add error handling

**OrderService.java** (Line 23)
   // TODO: Optimize database query

**AuthController.java** (Line 67, 88)
   // TODO: Add OAuth support
   // TODO: Implement rate limiting

Total: 7 TODO items across 5 files
```

---

## Tool Descriptor Format

LLMs use tool descriptors to understand available file operations. Here's the OpenAI function calling format:

### Read File

```json
{
  "name": "filesystem_read_file",
  "description": "Read the contents of a file",
  "parameters": {
    "type": "object",
    "properties": {
      "path": {
        "type": "string",
        "description": "Absolute path to the file"
      },
      "encoding": {
        "type": "string",
        "description": "Character encoding (default: UTF-8)",
        "default": "UTF-8"
      }
    },
    "required": ["path"]
  }
}
```

### Write File

```json
{
  "name": "filesystem_write_file",
  "description": "Write content to a file (creates if doesn't exist)",
  "parameters": {
    "type": "object",
    "properties": {
      "path": {
        "type": "string",
        "description": "Absolute path to the file"
      },
      "content": {
        "type": "string",
        "description": "Content to write"
      },
      "encoding": {
        "type": "string",
        "description": "Character encoding (default: UTF-8)",
        "default": "UTF-8"
      },
      "append": {
        "type": "boolean",
        "description": "Append to existing file (default: false)",
        "default": false
      }
    },
    "required": ["path", "content"]
  }
}
```

### List Files

```json
{
  "name": "filesystem_list_files",
  "description": "List files in a directory with optional filtering",
  "parameters": {
    "type": "object",
    "properties": {
      "directory": {
        "type": "string",
        "description": "Directory path to list"
      },
      "filter": {
        "type": "string",
        "description": "File pattern (e.g., *.java, *.md)",
        "default": "*"
      },
      "recursive": {
        "type": "boolean",
        "description": "Include subdirectories",
        "default": false
      }
    },
    "required": ["directory"]
  }
}
```

### Delete File/Directory

```json
{
  "name": "filesystem_delete",
  "description": "Delete a file or directory",
  "parameters": {
    "type": "object",
    "properties": {
      "path": {
        "type": "string",
        "description": "Path to delete"
      },
      "recursive": {
        "type": "boolean",
        "description": "Delete directory recursively (required for non-empty dirs)",
        "default": false
      }
    },
    "required": ["path"]
  }
}
```

### Copy Files

```json
{
  "name": "filesystem_copy",
  "description": "Copy a file or directory",
  "parameters": {
    "type": "object",
    "properties": {
      "source": {
        "type": "string",
        "description": "Source path"
      },
      "destination": {
        "type": "string",
        "description": "Destination path"
      },
      "overwrite": {
        "type": "boolean",
        "description": "Overwrite if exists",
        "default": false
      }
    },
    "required": ["source", "destination"]
  }
}
```

---

## Parameter Mapping

### Path Resolution

LLMs should normalize and validate paths:

```java
// User: "save it in the docs folder"
// LLM resolves to: "/workspace/docs/filename.ext"

// User: "put it in ../parent"
// LLM validates: Path traversal attempt → REJECT

// User: "save as hello.py"
// LLM resolves to: "/workspace/hello.py" (current dir)
```

### File Pattern Matching

| User Expression |          Pattern          |        Example Matches        |
|-----------------|---------------------------|-------------------------------|
| "Java files"    | `*.java`                  | Main.java, User.java          |
| "All files"     | `*`                       | Every file                    |
| "Config files"  | `*.{properties,yaml,yml}` | app.properties, config.yaml   |
| "Test files"    | `*Test.java`              | UserTest.java, OrderTest.java |
| "Hidden files"  | `.*`                      | .gitignore, .env              |

---

## Security Considerations

### Path Traversal Prevention

**✅ PROTECTED: All paths validated against traversal attacks**

```java
// User input: "../../etc/passwd"
// FileSystemTool validation:
private void validatePath(Path path) {
    Path normalized = path.normalize();
    if (normalized.startsWith("..")) {
        throw new SecurityException("Path traversal attempt detected");
    }
}

// Rejected with error
// LLM informs user: "Cannot access paths outside workspace"
```

### Sandbox Mode

```java
// Configure workspace root
FileSystemConfig config = FileSystemConfig.builder()
    .workspaceRoot("/workspace")
    .allowedExtensions(Set.of(".java", ".md", ".txt", ".json"))
    .maxFileSize(10 * 1024 * 1024)  // 10 MB
    .build();

fileSystemTool.setConfig(config);
```

### Permission Checks

```java
// LLM checks before operations
if (isReadOperation(operation)) {
    if (!Files.isReadable(path)) {
        return "File is not readable. Check permissions.";
    }
}

if (isWriteOperation(operation)) {
    if (Files.exists(path) && !Files.isWritable(path)) {
        return "File exists but is not writable.";
    }
}
```

### Destructive Operation Confirmation

```java
// LLM confirms before deletion
if (operation == DELETE && isImportantFile(path)) {
    boolean confirmed = confirmWithUser(
        "Are you sure you want to delete " + path + "?"
    );

    if (!confirmed) {
        return "Operation cancelled by user.";
    }
}
```

---

## Error Handling

### Common Error Scenarios

**1. File Not Found**

```java
FileOperationResult result = fileSystemTool.readFile("/missing.txt").block();

if (!result.isSuccess()) {
    // result.getErrorMessage() = "File not found: /missing.txt"
    // LLM response:
    return "The file '/missing.txt' doesn't exist. Would you like me to create it?";
}
```

**2. Permission Denied**

```java
// result.getErrorMessage() = "Access denied: /system/protected.conf"
// LLM response:
return "I don't have permission to access that file. It may be a system file.";
```

**3. Disk Space**

```java
// result.getErrorMessage() = "Insufficient disk space"
// LLM response:
return "Cannot write file: Not enough disk space available.";
```

**4. Invalid Path**

```java
// result.getErrorMessage() = "Invalid path: contains illegal characters"
// LLM response:
return "The filename contains invalid characters. Please use only letters, numbers, and basic punctuation.";
```

---

## Use Cases

### 1. Code Generation

**User:** "Create a Java REST controller for users"

**LLM Workflow:**
1. Generate UserController.java content
2. Determine package structure
3. Write file to correct location
4. Inform user of file creation

```java
fileSystemTool.writeFile(
    "/workspace/src/main/java/com/example/controller/UserController.java",
    generatedCode
).block();
```

### 2. Configuration Management

**User:** "Update the database password in application.properties"

**LLM Workflow:**
1. Read existing application.properties
2. Parse properties
3. Update database.password value
4. Write updated content back

```java
String content = fileSystemTool.readFile("application.properties").block();
String updated = updateProperty(content, "database.password", newPassword);
fileSystemTool.writeFile("application.properties", updated).block();
```

### 3. Log Analysis

**User:** "Find all ERROR entries in today's log"

**LLM Workflow:**
1. Read log file
2. Filter lines containing "ERROR"
3. Parse and categorize errors
4. Present summary to user

```java
String logContent = fileSystemTool.readFile("/logs/app.log").block();
List<String> errors = extractErrors(logContent);
String summary = summarizeErrors(errors);
```

### 4. Project Scaffolding

**User:** "Set up a basic Spring Boot project structure"

**LLM Workflow:**
1. Create directory structure
2. Generate pom.xml
3. Create package directories
4. Generate starter classes
5. Create README

```java
// Create directories
fileSystemTool.createDirectory("/workspace/src/main/java/com/example").block();
fileSystemTool.createDirectory("/workspace/src/main/resources").block();
fileSystemTool.createDirectory("/workspace/src/test/java").block();

// Write files
fileSystemTool.writeFile("/workspace/pom.xml", pomContent).block();
fileSystemTool.writeFile("/workspace/README.md", readmeContent).block();
```

### 5. Data Export

**User:** "Export the user list to a CSV file"

**LLM Workflow:**
1. Query data from database
2. Format as CSV
3. Write to file
4. Inform user of export location

```java
String csvContent = formatAsCSV(userList);
fileSystemTool.writeFile("/workspace/exports/users.csv", csvContent).block();
```

---

## Best Practices

### For LLM Developers

**1. Path Normalization**

```java
// Always normalize paths
Path normalized = Path.of(userPath).normalize();

// Validate before operations
if (!isWithinWorkspace(normalized)) {
    throw new SecurityException("Path outside workspace");
}
```

**2. Confirm Destructive Operations**

```java
if (operation == DELETE || operation == OVERWRITE) {
    String confirmation = askUser(
        "This will " + operation + " " + path + ". Continue? (yes/no)"
    );

    if (!confirmation.equalsIgnoreCase("yes")) {
        return "Operation cancelled.";
    }
}
```

**3. Provide Context**

```java
// Don't just say "File created"
// Provide full context:
"Created UserController.java (1.2 KB) at:
 /workspace/src/main/java/com/example/controller/UserController.java

 You can now:
 - Add endpoints to the controller
 - Write tests in UserControllerTest.java
 - Run with: mvn spring-boot:run"
```

**4. Handle Encoding Properly**

```java
// Always specify encoding for text files
fileSystemTool.writeFile(path, content, StandardCharsets.UTF_8);

// For binary files, use byte operations
fileSystemTool.writeBytes(path, bytes);
```

**5. Size Awareness**

```java
// Check file size before reading large files
FileInfo info = fileSystemTool.getFileInfo(path).block();

if (info.getSize() > 10 * 1024 * 1024) {  // 10 MB
    return "File is very large (" + formatSize(info.getSize()) + "). " +
           "Would you like me to read just the first 1000 lines?";
}
```

### For Agent Implementers

**1. Batch Operations**

```java
// For multiple files, use batch operations
List<Mono<FileOperationResult>> operations = files.stream()
    .map(file -> fileSystemTool.writeFile(file.path, file.content))
    .collect(Collectors.toList());

Mono.zip(operations, results -> results)
    .block();
```

**2. Atomic Operations**

```java
// Write to temp file, then move (atomic)
Path tempFile = Files.createTempFile("temp", ".tmp");
fileSystemTool.writeFile(tempFile, content).block();
fileSystemTool.moveFile(tempFile, targetPath).block();
```

**3. Error Recovery**

```java
@Retry(maxAttempts = 3)
public Mono<FileOperationResult> writeFileWithRetry(Path path, String content) {
    return fileSystemTool.writeFile(path, content);
}
```

---

## Integration Example

Complete example showing LLM agent using File System Tool:

```java
@Component
public class FileSystemAgent implements Agent {

    @Inject
    private FileSystemTool fileSystemTool;

    @Override
    public TaskResult executeTask(TaskRequest request) {
        String task = request.task().toLowerCase();

        try {
            if (task.contains("create") || task.contains("write")) {
                return handleFileCreation(request);
            } else if (task.contains("read") || task.contains("show")) {
                return handleFileReading(request);
            } else if (task.contains("list") || task.contains("show files")) {
                return handleFileListing(request);
            } else if (task.contains("delete") || task.contains("remove")) {
                return handleFileDeletion(request);
            } else if (task.contains("search") || task.contains("find")) {
                return handleFileSearch(request);
            }

            return TaskResult.failure(getName(), task, "Unknown file operation");

        } catch (Exception e) {
            return TaskResult.failureWithException(getName(), task, e);
        }
    }

    private TaskResult handleFileCreation(TaskRequest request) {
        // Extract file path and content from request
        Path filePath = extractPath(request.task());
        String content = generateContent(request);

        // Validate path
        if (!isValidPath(filePath)) {
            return TaskResult.failure(getName(), request.task(),
                "Invalid or unsafe file path");
        }

        // Write file
        FileOperationResult result = fileSystemTool.writeFile(
            filePath,
            content,
            StandardCharsets.UTF_8
        ).block();

        if (result.isSuccess()) {
            String message = formatSuccessMessage(result);
            return TaskResult.success(getName(), request.task(), message);
        } else {
            return TaskResult.failure(getName(), request.task(),
                result.getErrorMessage());
        }
    }

    private String formatSuccessMessage(FileOperationResult result) {
        return String.format(
            "Successfully %s file: %s (%s)",
            result.getOperation(),
            result.getPath(),
            formatBytes(result.getBytesProcessed())
        );
    }

    // Additional methods...
}
```

---

## Conclusion

The File System Tool provides LLM agents with secure file system access through natural language. With path traversal protection, comprehensive file operations, and Java NIO integration, it enables code generation, configuration management, log analysis, and more.

**Key Takeaways:**
- ✅ Natural language → File operations
- ✅ Path traversal protection (security)
- ✅ No external dependencies (Java NIO)
- ✅ Recursive operations (copy, list, delete)
- ✅ Pattern matching (*.java, *.md, etc.)
- ✅ Cross-platform compatibility

---

*Last Updated: 2025-10-25*
*Version: 0.1.0*
*Status: Fully Implemented*
