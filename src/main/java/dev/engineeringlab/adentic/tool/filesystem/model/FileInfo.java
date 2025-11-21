package dev.engineeringlab.adentic.tool.filesystem.model;

import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

/** Metadata information about a file or directory. */
@Data
@Builder
public class FileInfo {
  /** The file/directory path. */
  private Path path;

  /** File name. */
  private String name;

  /** File extension (null for directories). */
  private String extension;

  /** File size in bytes. */
  private long size;

  /** Whether this is a directory. */
  private boolean directory;

  /** Whether this is a regular file. */
  private boolean file;

  /** Whether this is a symbolic link. */
  private boolean symlink;

  /** Whether the file is readable. */
  private boolean readable;

  /** Whether the file is writable. */
  private boolean writable;

  /** Whether the file is executable. */
  private boolean executable;

  /** Whether the file is hidden. */
  private boolean hidden;

  /** Last modified timestamp. */
  private Instant lastModified;

  /** Creation timestamp. */
  private Instant created;

  /** Last accessed timestamp. */
  private Instant lastAccessed;

  /** File owner (if available). */
  private String owner;

  /** POSIX permissions (if available). */
  private Set<PosixFilePermission> permissions;

  /** MIME type (if detectable). */
  private String mimeType;

  /** File hash (SHA-256, if calculated). */
  private String hash;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%s %s%n", directory ? "[DIR]" : "[FILE]", name));

    if (!directory) {
      sb.append(String.format("  Size: %s%n", formatSize(size)));
    }

    sb.append(String.format("  Modified: %s%n", lastModified));
    sb.append(
        String.format(
            "  Permissions: %s%s%s",
            readable ? "r" : "-", writable ? "w" : "-", executable ? "x" : "-"));

    return sb.toString();
  }

  private String formatSize(long bytes) {
    if (bytes < 1024) {
      return bytes + " B";
    }
    if (bytes < 1024 * 1024) {
      return String.format("%.2f KB", bytes / 1024.0);
    }
    if (bytes < 1024 * 1024 * 1024) {
      return String.format("%.2f MB", bytes / (1024.0 * 1024));
    }
    return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
  }
}
