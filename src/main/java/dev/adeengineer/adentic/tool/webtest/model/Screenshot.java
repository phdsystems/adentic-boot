package dev.adeengineer.adentic.tool.webtest.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

/** Represents a screenshot captured during testing. */
@Data
@Builder
public class Screenshot {
  /** Screenshot data as bytes (PNG format). */
  private byte[] data;

  /** Screenshot data as Base64 string. */
  private String base64;

  /** File path if saved to disk. */
  private String filePath;

  /** Width in pixels. */
  private int width;

  /** Height in pixels. */
  private int height;

  /** Page URL when screenshot was taken. */
  private String url;

  /** Timestamp when screenshot was taken. */
  @Builder.Default private Instant timestamp = Instant.now();

  /** Whether the screenshot was successful. */
  @Builder.Default private boolean success = true;

  /** Error message if screenshot failed. */
  private String error;

  /** Get file size in bytes. */
  public long getSize() {
    return data != null ? data.length : 0;
  }

  @Override
  public String toString() {
    if (!success) {
      return String.format("❌ Screenshot failed: %s", error);
    }

    return String.format(
        "📸 Screenshot: %dx%d (%s bytes) - %s", width, height, formatSize(getSize()), url);
  }

  private String formatSize(long bytes) {
    if (bytes < 1024) {
      return bytes + " B";
    }
    if (bytes < 1024 * 1024) {
      return String.format("%.2f KB", bytes / 1024.0);
    }
    return String.format("%.2f MB", bytes / (1024.0 * 1024));
  }
}
