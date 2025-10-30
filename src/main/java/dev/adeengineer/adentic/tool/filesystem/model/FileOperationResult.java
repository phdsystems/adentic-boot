package dev.adeengineer.adentic.tool.filesystem.model;

import java.nio.file.Path;
import lombok.Builder;
import lombok.Data;

/** Result of a file system operation (create, delete, copy, move, etc.). */
@Data
@Builder
public class FileOperationResult {
  /** The operation type. */
  private OperationType operation;

  /** The source path. */
  private Path source;

  /** The target path (for copy/move operations). */
  private Path target;

  /** Whether the operation was successful. */
  @Builder.Default private boolean success = true;

  /** Error message if operation failed. */
  private String error;

  /** Number of items affected (for batch operations). */
  @Builder.Default private int itemsAffected = 1;

  /** Additional operation details. */
  private String details;

  /** File operation types. */
  public enum OperationType {
    READ,
    WRITE,
    APPEND,
    DELETE,
    COPY,
    MOVE,
    CREATE_DIR,
    DELETE_DIR,
    LIST,
    SEARCH
  }

  @Override
  public String toString() {
    if (!success) {
      return String.format("❌ %s failed: %s", operation, error);
    }

    String message =
        switch (operation) {
          case READ -> String.format("✅ Read file: %s", source);
          case WRITE -> String.format("✅ Wrote file: %s", source);
          case APPEND -> String.format("✅ Appended to file: %s", source);
          case DELETE -> String.format("✅ Deleted file: %s", source);
          case COPY -> String.format("✅ Copied %s → %s", source, target);
          case MOVE -> String.format("✅ Moved %s → %s", source, target);
          case CREATE_DIR -> String.format("✅ Created directory: %s", source);
          case DELETE_DIR ->
              String.format("✅ Deleted directory: %s (%d items)", source, itemsAffected);
          case LIST -> String.format("✅ Listed directory: %s (%d items)", source, itemsAffected);
          case SEARCH -> String.format("✅ Search found %d items", itemsAffected);
        };

    if (details != null && !details.isEmpty()) {
      message += "\n  " + details;
    }

    return message;
  }
}
