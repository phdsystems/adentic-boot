package dev.engineeringlab.adentic.tool.filesystem.security;

import dev.engineeringlab.adentic.tool.filesystem.config.FileSystemConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

/**
 * Validates file system paths for security. Prevents path traversal attacks and enforces access
 * restrictions.
 */
@Slf4j
public class PathValidator {

  private final FileSystemConfig config;

  public PathValidator(FileSystemConfig config) {
    this.config = config;
  }

  /**
   * Validate a path for any operation.
   *
   * @param path the path to validate
   * @throws SecurityException if the path is not allowed
   */
  public void validatePath(Path path) {
    if (!config.isValidatePaths()) {
      return; // Validation disabled
    }

    Path normalized = normalizePath(path);

    // Check for path traversal attempts
    checkPathTraversal(path, normalized);

    // Check if path is allowed by configuration
    if (!config.isPathAllowed(normalized)) {
      throw new SecurityException(
          String.format("Access denied: Path '%s' is not allowed by configuration", path));
    }

    log.debug("Path validated: {}", normalized);
  }

  /** Validate a path for read operations. */
  public void validateRead(Path path) {
    if (!config.isAllowRead()) {
      throw new SecurityException("Read operations are not allowed");
    }

    validatePath(path);

    // Check if file exists
    if (!Files.exists(path)) {
      throw new IllegalArgumentException(String.format("Path does not exist: %s", path));
    }

    // Check if readable
    if (!Files.isReadable(path)) {
      throw new SecurityException(String.format("Path is not readable: %s", path));
    }

    // Check symlinks
    if (!config.isFollowSymlinks() && Files.isSymbolicLink(path)) {
      throw new SecurityException(String.format("Symbolic links are not allowed: %s", path));
    }
  }

  /** Validate a path for write operations. */
  public void validateWrite(Path path) {
    if (!config.isAllowWrite()) {
      throw new SecurityException("Write operations are not allowed");
    }

    validatePath(path);

    // Check file extension
    String extension = getExtension(path);
    if (extension != null && config.getBlockedExtensions().contains(extension.toLowerCase())) {
      throw new SecurityException(String.format("File extension '%s' is not allowed", extension));
    }

    // Check if parent directory exists and is writable
    Path parent = path.getParent();
    if (parent != null) {
      if (!Files.exists(parent)) {
        throw new IllegalArgumentException(
            String.format("Parent directory does not exist: %s", parent));
      }

      if (!Files.isWritable(parent)) {
        throw new SecurityException(String.format("Parent directory is not writable: %s", parent));
      }
    }
  }

  /** Validate a path for delete operations. */
  public void validateDelete(Path path) {
    if (!config.isAllowDelete()) {
      throw new SecurityException("Delete operations are not allowed");
    }

    validatePath(path);

    if (!Files.exists(path)) {
      throw new IllegalArgumentException(String.format("Path does not exist: %s", path));
    }
  }

  /** Validate a path for directory creation. */
  public void validateCreateDirectory(Path path) {
    if (!config.isAllowCreateDirectory()) {
      throw new SecurityException("Create directory operations are not allowed");
    }

    validatePath(path);

    if (Files.exists(path)) {
      throw new IllegalArgumentException(String.format("Path already exists: %s", path));
    }
  }

  /** Validate file size for read operations. */
  public void validateReadSize(Path path) throws IOException {
    long size = Files.size(path);
    if (size > config.getMaxReadSize()) {
      throw new SecurityException(
          String.format(
              "File size (%d bytes) exceeds maximum allowed size (%d bytes)",
              size, config.getMaxReadSize()));
    }
  }

  /** Validate file size for write operations. */
  public void validateWriteSize(long size) {
    if (size > config.getMaxWriteSize()) {
      throw new SecurityException(
          String.format(
              "Content size (%d bytes) exceeds maximum allowed size (%d bytes)",
              size, config.getMaxWriteSize()));
    }
  }

  /** Normalize a path to absolute form. */
  private Path normalizePath(Path path) {
    try {
      // Convert to absolute path and normalize (removes . and ..)
      return path.toAbsolutePath().normalize();
    } catch (Exception e) {
      throw new IllegalArgumentException(String.format("Invalid path: %s", path), e);
    }
  }

  /** Check for path traversal attacks. */
  private void checkPathTraversal(Path original, Path normalized) {
    String originalStr = original.toString();

    // Check for suspicious patterns
    if (originalStr.contains("..")) {
      // Verify that normalization didn't change the path significantly
      // (which would indicate a traversal attempt)
      log.warn("Path contains '..' sequence: {}", originalStr);
    }

    // Check for null bytes (common attack vector)
    if (originalStr.contains("\0")) {
      throw new SecurityException("Path contains null bytes (security risk)");
    }

    // Check for excessive path length
    if (originalStr.length() > 4096) {
      throw new SecurityException("Path is too long (possible attack)");
    }
  }

  /** Get file extension from path. */
  private String getExtension(Path path) {
    Path fileNamePath = path.getFileName();
    if (fileNamePath == null) {
      return null;
    }

    String filename = fileNamePath.toString();
    int lastDot = filename.lastIndexOf('.');

    if (lastDot > 0 && lastDot < filename.length() - 1) {
      return filename.substring(lastDot);
    }

    return null;
  }

  /** Validate search depth. */
  public void validateDepth(int depth) {
    if (depth > config.getMaxDepth()) {
      throw new SecurityException(
          String.format(
              "Search depth (%d) exceeds maximum allowed depth (%d)", depth, config.getMaxDepth()));
    }
  }

  /** Validate result count. */
  public void validateResultCount(int count) {
    if (count > config.getMaxResults()) {
      log.warn(
          "Result count ({}) exceeds maximum ({}), results will be truncated",
          count,
          config.getMaxResults());
    }
  }
}
