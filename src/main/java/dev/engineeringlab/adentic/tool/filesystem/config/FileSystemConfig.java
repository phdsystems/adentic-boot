package dev.engineeringlab.adentic.tool.filesystem.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

/** Configuration for FileSystemTool behavior and security. */
@Data
@Builder
public class FileSystemConfig {
  /**
   * Allowed root directories (whitelist). If empty, all directories are allowed (not recommended).
   */
  @Builder.Default private Set<Path> allowedRoots = new HashSet<>();

  /** Blocked directories (blacklist). */
  @Builder.Default private Set<Path> blockedPaths = new HashSet<>();

  /** Whether to allow read operations. */
  @Builder.Default private boolean allowRead = true;

  /** Whether to allow write operations. */
  @Builder.Default private boolean allowWrite = true;

  /** Whether to allow delete operations. */
  @Builder.Default private boolean allowDelete = true;

  /** Whether to allow creating directories. */
  @Builder.Default private boolean allowCreateDirectory = true;

  /** Whether to follow symbolic links. */
  @Builder.Default private boolean followSymlinks = false;

  /** Maximum file size for read operations (in bytes). Default: 10 MB */
  @Builder.Default private long maxReadSize = 10 * 1024 * 1024;

  /** Maximum file size for write operations (in bytes). Default: 10 MB */
  @Builder.Default private long maxWriteSize = 10 * 1024 * 1024;

  /** Maximum number of files to return in search/list operations. */
  @Builder.Default private int maxResults = 1000;

  /** Maximum depth for recursive operations. */
  @Builder.Default private int maxDepth = 10;

  /** Operation timeout in milliseconds. */
  @Builder.Default private long operationTimeoutMs = 30000;

  /** Whether to validate paths for security (path traversal protection). */
  @Builder.Default private boolean validatePaths = true;

  /** Whether to calculate file hashes. */
  @Builder.Default private boolean calculateHashes = false;

  /** Default encoding for text files. */
  @Builder.Default private String defaultEncoding = "UTF-8";

  /** Blocked file extensions (e.g., [".exe", ".dll"]). */
  @Builder.Default private Set<String> blockedExtensions = new HashSet<>();

  /**
   * Default configuration preset. - Read and write allowed - Delete allowed - No specific allowed
   * roots (all accessible) - System directories blocked - 10 MB max file size - Path validation
   * enabled
   */
  public static FileSystemConfig defaults() {
    return FileSystemConfig.builder().blockedPaths(getDefaultBlockedPaths()).build();
  }

  /**
   * Read-only configuration preset. - Only read operations allowed - No write or delete - System
   * directories blocked
   */
  public static FileSystemConfig readOnly() {
    return FileSystemConfig.builder()
        .allowWrite(false)
        .allowDelete(false)
        .allowCreateDirectory(false)
        .blockedPaths(getDefaultBlockedPaths())
        .build();
  }

  /**
   * Permissive configuration preset. - All operations allowed - Higher size limits (100 MB) - More
   * results (10,000) - Deeper recursion (20 levels) - Still blocks system directories
   */
  public static FileSystemConfig permissive() {
    return FileSystemConfig.builder()
        .maxReadSize(100 * 1024 * 1024)
        .maxWriteSize(100 * 1024 * 1024)
        .maxResults(10000)
        .maxDepth(20)
        .blockedPaths(getDefaultBlockedPaths())
        .build();
  }

  /**
   * Sandboxed configuration preset. - Restricts all operations to a specific directory - No access
   * outside the sandbox - All operations allowed within sandbox
   *
   * @param sandboxPath the directory to restrict access to
   */
  public static FileSystemConfig sandboxed(String sandboxPath) {
    Set<Path> allowedRoots = new HashSet<>();
    allowedRoots.add(Paths.get(sandboxPath).toAbsolutePath().normalize());

    return FileSystemConfig.builder()
        .allowedRoots(allowedRoots)
        .blockedPaths(new HashSet<>()) // No additional blocks needed in sandbox
        .build();
  }

  /**
   * Secure configuration preset. - Read-only - Restricted file size (1 MB) - Limited results (100)
   * - No symlinks - No hidden files - Hash calculation enabled
   */
  public static FileSystemConfig secure() {
    return FileSystemConfig.builder()
        .allowWrite(false)
        .allowDelete(false)
        .allowCreateDirectory(false)
        .followSymlinks(false)
        .maxReadSize(1024 * 1024)
        .maxResults(100)
        .maxDepth(5)
        .calculateHashes(true)
        .blockedPaths(getDefaultBlockedPaths())
        .build();
  }

  /**
   * Temporary directory configuration preset. - Restricts to system temp directory - All operations
   * allowed - Suitable for temporary file operations
   */
  public static FileSystemConfig tempDirectory() {
    String tempDir = System.getProperty("java.io.tmpdir");
    return sandboxed(tempDir);
  }

  /** Get default blocked paths (system directories). */
  private static Set<Path> getDefaultBlockedPaths() {
    Set<Path> blocked = new HashSet<>();

    // Unix/Linux system directories
    blocked.add(Paths.get("/etc"));
    blocked.add(Paths.get("/sys"));
    blocked.add(Paths.get("/proc"));
    blocked.add(Paths.get("/dev"));
    blocked.add(Paths.get("/boot"));
    blocked.add(Paths.get("/root"));

    // Windows system directories
    blocked.add(Paths.get("C:\\Windows"));
    blocked.add(Paths.get("C:\\Program Files"));
    blocked.add(Paths.get("C:\\Program Files (x86)"));
    blocked.add(Paths.get("C:\\ProgramData"));

    // macOS system directories
    blocked.add(Paths.get("/System"));
    blocked.add(Paths.get("/Library"));
    blocked.add(Paths.get("/private"));

    return blocked;
  }

  /** Check if a path is allowed based on configuration. */
  public boolean isPathAllowed(Path path) {
    Path normalized = path.toAbsolutePath().normalize();

    // Check if path is in blocked paths
    for (Path blocked : blockedPaths) {
      if (normalized.startsWith(blocked)) {
        return false;
      }
    }

    // If allowedRoots is empty, all paths are allowed (unless blocked)
    if (allowedRoots.isEmpty()) {
      return true;
    }

    // Check if path is under an allowed root
    for (Path allowed : allowedRoots) {
      if (normalized.startsWith(allowed)) {
        return true;
      }
    }

    return false;
  }
}
