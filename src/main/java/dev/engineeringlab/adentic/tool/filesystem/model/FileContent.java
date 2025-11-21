package dev.engineeringlab.adentic.tool.filesystem.model;

import java.nio.file.Path;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

/** Represents the content of a file along with metadata. */
@Data
@Builder
public class FileContent {
  /** The file path. */
  private Path path;

  /** The file content as string (for text files). */
  private String content;

  /** The file content as bytes (for binary files). */
  private byte[] bytes;

  /** File size in bytes. */
  private long size;

  /** Last modified timestamp. */
  private Instant lastModified;

  /** File extension. */
  private String extension;

  /** Whether the file is binary. */
  @Builder.Default private boolean binary = false;

  /** Character encoding (for text files). */
  @Builder.Default private String encoding = "UTF-8";

  /** Whether the read was successful. */
  @Builder.Default private boolean success = true;

  /** Error message if read failed. */
  private String error;

  @Override
  public String toString() {
    if (!success) {
      return String.format("Failed to read %s: %s", path, error);
    }

    return String.format(
        "File: %s%nSize: %d bytes%nModified: %s%nBinary: %s",
        path.getFileName(), size, lastModified, binary ? "yes" : "no");
  }
}
