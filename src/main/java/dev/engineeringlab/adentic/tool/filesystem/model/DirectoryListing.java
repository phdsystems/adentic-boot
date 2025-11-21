package dev.engineeringlab.adentic.tool.filesystem.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** Represents a directory listing with files and subdirectories. */
@Data
@Builder
public class DirectoryListing {
  /** The directory path. */
  private Path path;

  /** List of files in the directory. */
  @Builder.Default private List<FileInfo> files = new ArrayList<>();

  /** List of subdirectories. */
  @Builder.Default private List<FileInfo> directories = new ArrayList<>();

  /** Total number of items (files + directories). */
  private int totalItems;

  /** Total size of all files in bytes. */
  private long totalSize;

  /** Whether the listing was successful. */
  @Builder.Default private boolean success = true;

  /** Error message if listing failed. */
  private String error;

  /** Get all items (files and directories). */
  public List<FileInfo> getAllItems() {
    List<FileInfo> all = new ArrayList<>();
    all.addAll(directories);
    all.addAll(files);
    return all;
  }

  /** Get summary of the directory listing. */
  public String getSummary() {
    if (!success) {
      return String.format("❌ Failed to list %s: %s", path, error);
    }

    return String.format(
        "📁 %s: %d directories, %d files (%s)",
        path.getFileName() != null ? path.getFileName() : path,
        directories.size(),
        files.size(),
        formatSize(totalSize));
  }

  /** Get detailed report of directory contents. */
  public String getDetailedReport() {
    StringBuilder sb = new StringBuilder();
    sb.append(getSummary()).append("\n\n");

    if (!success) {
      return sb.toString();
    }

    if (!directories.isEmpty()) {
      sb.append("Directories:\n");
      for (FileInfo dir : directories) {
        sb.append("  📁 ").append(dir.getName()).append("\n");
      }
      sb.append("\n");
    }

    if (!files.isEmpty()) {
      sb.append("Files:\n");
      for (FileInfo file : files) {
        sb.append(String.format("  📄 %s (%s)%n", file.getName(), formatSize(file.getSize())));
      }
    }

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

  @Override
  public String toString() {
    return getSummary();
  }
}
