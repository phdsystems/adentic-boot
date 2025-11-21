package dev.engineeringlab.adentic.tool.filesystem.model;

import java.nio.file.Path;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

/** Request parameters for searching files. */
@Data
@Builder
public class FileSearchRequest {
  /** The directory to search in. */
  private Path directory;

  /** File name pattern (glob, e.g., "*.java"). */
  private String pattern;

  /** Content to search for (regex). */
  private String contentPattern;

  /** Whether to search recursively. */
  @Builder.Default private boolean recursive = false;

  /** Maximum depth for recursive search (-1 for unlimited). */
  @Builder.Default private int maxDepth = -1;

  /** Include hidden files. */
  @Builder.Default private boolean includeHidden = false;

  /** Minimum file size in bytes. */
  private Long minSize;

  /** Maximum file size in bytes. */
  private Long maxSize;

  /** Files modified after this timestamp. */
  private Instant modifiedAfter;

  /** Files modified before this timestamp. */
  private Instant modifiedBefore;

  /** File extensions to include (e.g., [".java", ".xml"]). */
  private String[] extensions;

  /** Maximum number of results to return. */
  @Builder.Default private int maxResults = 1000;

  /** Search type. */
  @Builder.Default private SearchType searchType = SearchType.FILES;

  /** Search type enumeration. */
  public enum SearchType {
    FILES, // Search for files only
    DIRECTORIES, // Search for directories only
    BOTH // Search for both files and directories
  }
}
