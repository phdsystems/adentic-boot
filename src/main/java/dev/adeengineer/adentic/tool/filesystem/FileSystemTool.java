package dev.adeengineer.adentic.tool.filesystem;

import dev.adeengineer.adentic.tool.filesystem.config.FileSystemConfig;
import dev.adeengineer.adentic.tool.filesystem.model.*;
import dev.adeengineer.adentic.tool.filesystem.security.PathValidator;
import dev.adeengineer.annotation.provider.ToolProvider;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Tool for file system operations with security controls.
 *
 * <p>Provides comprehensive file and directory operations:
 *
 * <ul>
 *   <li>File operations: read, write, append, delete, copy, move
 *   <li>Directory operations: list, create, delete, tree
 *   <li>Information operations: metadata, exists, size
 *   <li>Search operations: find by pattern, search content
 * </ul>
 *
 * <p>Security features:
 *
 * <ul>
 *   <li>Path validation and traversal protection
 *   <li>Whitelist/blacklist directory access
 *   <li>File size limits
 *   <li>Operation timeouts
 *   <li>Extension blocking
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Inject
 * private FileSystemTool fileSystem;
 *
 * FileContent content = fileSystem.readFile("/path/to/file.txt").block();
 * }</pre>
 *
 * @see FileSystemConfig
 * @see PathValidator
 */
@ToolProvider(name = "file-system")
@Slf4j
public class FileSystemTool {

  @Setter private FileSystemConfig config;

  private PathValidator validator;

  /** Default constructor with default configuration. */
  public FileSystemTool() {
    this(FileSystemConfig.defaults());
  }

  /** Constructor with custom configuration. */
  public FileSystemTool(FileSystemConfig config) {
    this.config = config;
    this.validator = new PathValidator(config);
  }

  // ========== FILE READ OPERATIONS ==========

  /**
   * Read file content as string.
   *
   * @param pathStr the file path
   * @return Mono containing file content
   */
  public Mono<FileContent> readFile(String pathStr) {
    return Mono.fromCallable(
        () -> {
          Path path = Paths.get(pathStr);
          validator.validateRead(path);
          validator.validateReadSize(path);

          try {
            String content = Files.readString(path, Charset.forName(config.getDefaultEncoding()));
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

            return FileContent.builder()
                .path(path)
                .content(content)
                .size(attrs.size())
                .lastModified(attrs.lastModifiedTime().toInstant())
                .extension(getExtension(path))
                .binary(false)
                .encoding(config.getDefaultEncoding())
                .success(true)
                .build();

          } catch (Exception e) {
            log.error("Failed to read file: {}", path, e);
            return FileContent.builder().path(path).success(false).error(e.getMessage()).build();
          }
        });
  }

  /**
   * Read file content as bytes.
   *
   * @param pathStr the file path
   * @return Mono containing file content
   */
  public Mono<FileContent> readFileBytes(String pathStr) {
    return Mono.fromCallable(
        () -> {
          Path path = Paths.get(pathStr);
          validator.validateRead(path);
          validator.validateReadSize(path);

          try {
            byte[] bytes = Files.readAllBytes(path);
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

            return FileContent.builder()
                .path(path)
                .bytes(bytes)
                .size(attrs.size())
                .lastModified(attrs.lastModifiedTime().toInstant())
                .extension(getExtension(path))
                .binary(true)
                .success(true)
                .build();

          } catch (Exception e) {
            log.error("Failed to read file bytes: {}", path, e);
            return FileContent.builder().path(path).success(false).error(e.getMessage()).build();
          }
        });
  }

  // ========== FILE WRITE OPERATIONS ==========

  /**
   * Write content to a file.
   *
   * @param pathStr the file path
   * @param content the content to write
   * @return Mono containing operation result
   */
  public Mono<FileOperationResult> writeFile(String pathStr, String content) {
    return Mono.fromCallable(
        () -> {
          Path path = Paths.get(pathStr);
          validator.validateWrite(path);
          validator.validateWriteSize(content.getBytes(config.getDefaultEncoding()).length);

          try {
            Files.writeString(path, content, Charset.forName(config.getDefaultEncoding()));

            return FileOperationResult.builder()
                .operation(FileOperationResult.OperationType.WRITE)
                .source(path)
                .success(true)
                .details(String.format("Wrote %d bytes", content.length()))
                .build();

          } catch (Exception e) {
            log.error("Failed to write file: {}", path, e);
            return FileOperationResult.builder()
                .operation(FileOperationResult.OperationType.WRITE)
                .source(path)
                .success(false)
                .error(e.getMessage())
                .build();
          }
        });
  }

  /**
   * Append content to a file.
   *
   * @param pathStr the file path
   * @param content the content to append
   * @return Mono containing operation result
   */
  public Mono<FileOperationResult> appendFile(String pathStr, String content) {
    return Mono.fromCallable(
        () -> {
          Path path = Paths.get(pathStr);
          validator.validateWrite(path);

          try {
            Files.writeString(
                path,
                content,
                Charset.forName(config.getDefaultEncoding()),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);

            return FileOperationResult.builder()
                .operation(FileOperationResult.OperationType.APPEND)
                .source(path)
                .success(true)
                .details(String.format("Appended %d bytes", content.length()))
                .build();

          } catch (Exception e) {
            log.error("Failed to append to file: {}", path, e);
            return FileOperationResult.builder()
                .operation(FileOperationResult.OperationType.APPEND)
                .source(path)
                .success(false)
                .error(e.getMessage())
                .build();
          }
        });
  }

  // ========== FILE DELETE/COPY/MOVE OPERATIONS ==========

  /**
   * Delete a file.
   *
   * @param pathStr the file path
   * @return Mono containing operation result
   */
  public Mono<FileOperationResult> deleteFile(String pathStr) {
    return Mono.fromCallable(
        () -> {
          Path path = Paths.get(pathStr);
          validator.validateDelete(path);

          try {
            Files.delete(path);

            return FileOperationResult.builder()
                .operation(FileOperationResult.OperationType.DELETE)
                .source(path)
                .success(true)
                .build();

          } catch (Exception e) {
            log.error("Failed to delete file: {}", path, e);
            return FileOperationResult.builder()
                .operation(FileOperationResult.OperationType.DELETE)
                .source(path)
                .success(false)
                .error(e.getMessage())
                .build();
          }
        });
  }

  /**
   * Copy a file.
   *
   * @param sourceStr the source path
   * @param targetStr the target path
   * @return Mono containing operation result
   */
  public Mono<FileOperationResult> copyFile(String sourceStr, String targetStr) {
    return Mono.fromCallable(
        () -> {
          Path source = Paths.get(sourceStr);
          Path target = Paths.get(targetStr);

          validator.validateRead(source);
          validator.validateWrite(target);

          try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

            return FileOperationResult.builder()
                .operation(FileOperationResult.OperationType.COPY)
                .source(source)
                .target(target)
                .success(true)
                .build();

          } catch (Exception e) {
            log.error("Failed to copy file: {} -> {}", source, target, e);
            return FileOperationResult.builder()
                .operation(FileOperationResult.OperationType.COPY)
                .source(source)
                .target(target)
                .success(false)
                .error(e.getMessage())
                .build();
          }
        });
  }

  /**
   * Move/rename a file.
   *
   * @param sourceStr the source path
   * @param targetStr the target path
   * @return Mono containing operation result
   */
  public Mono<FileOperationResult> moveFile(String sourceStr, String targetStr) {
    return Mono.fromCallable(
        () -> {
          Path source = Paths.get(sourceStr);
          Path target = Paths.get(targetStr);

          validator.validateRead(source);
          validator.validateWrite(target);
          validator.validateDelete(source);

          try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

            return FileOperationResult.builder()
                .operation(FileOperationResult.OperationType.MOVE)
                .source(source)
                .target(target)
                .success(true)
                .build();

          } catch (Exception e) {
            log.error("Failed to move file: {} -> {}", source, target, e);
            return FileOperationResult.builder()
                .operation(FileOperationResult.OperationType.MOVE)
                .source(source)
                .target(target)
                .success(false)
                .error(e.getMessage())
                .build();
          }
        });
  }

  // ========== DIRECTORY OPERATIONS ==========

  /**
   * List directory contents.
   *
   * @param pathStr the directory path
   * @return Mono containing directory listing
   */
  public Mono<DirectoryListing> listDirectory(String pathStr) {
    return Mono.fromCallable(
        () -> {
          Path path = Paths.get(pathStr);
          validator.validateRead(path);

          try {
            if (!Files.isDirectory(path)) {
              throw new IllegalArgumentException("Path is not a directory: " + path);
            }

            List<FileInfo> files = new ArrayList<>();
            List<FileInfo> directories = new ArrayList<>();
            long totalSize = 0;

            try (Stream<Path> stream = Files.list(path)) {
              for (Path entry : stream.toList()) {
                FileInfo info = buildFileInfo(entry);

                if (info.isDirectory()) {
                  directories.add(info);
                } else {
                  files.add(info);
                  totalSize += info.getSize();
                }
              }
            }

            return DirectoryListing.builder()
                .path(path)
                .files(files)
                .directories(directories)
                .totalItems(files.size() + directories.size())
                .totalSize(totalSize)
                .success(true)
                .build();

          } catch (Exception e) {
            log.error("Failed to list directory: {}", path, e);
            return DirectoryListing.builder()
                .path(path)
                .success(false)
                .error(e.getMessage())
                .build();
          }
        });
  }

  /**
   * Create a directory.
   *
   * @param pathStr the directory path
   * @param createParents whether to create parent directories
   * @return Mono containing operation result
   */
  public Mono<FileOperationResult> createDirectory(String pathStr, boolean createParents) {
    return Mono.fromCallable(
        () -> {
          Path path = Paths.get(pathStr);
          validator.validateCreateDirectory(path);

          try {
            if (createParents) {
              Files.createDirectories(path);
            } else {
              Files.createDirectory(path);
            }

            return FileOperationResult.builder()
                .operation(FileOperationResult.OperationType.CREATE_DIR)
                .source(path)
                .success(true)
                .details(createParents ? "Created with parents" : "Created single directory")
                .build();

          } catch (Exception e) {
            log.error("Failed to create directory: {}", path, e);
            return FileOperationResult.builder()
                .operation(FileOperationResult.OperationType.CREATE_DIR)
                .source(path)
                .success(false)
                .error(e.getMessage())
                .build();
          }
        });
  }

  /**
   * Delete a directory.
   *
   * @param pathStr the directory path
   * @param recursive whether to delete recursively
   * @return Mono containing operation result
   */
  public Mono<FileOperationResult> deleteDirectory(String pathStr, boolean recursive) {
    return Mono.fromCallable(
        () -> {
          Path path = Paths.get(pathStr);
          validator.validateDelete(path);

          try {
            int itemsDeleted = 0;

            if (recursive) {
              itemsDeleted = deleteRecursively(path);
            } else {
              Files.delete(path);
              itemsDeleted = 1;
            }

            return FileOperationResult.builder()
                .operation(FileOperationResult.OperationType.DELETE_DIR)
                .source(path)
                .success(true)
                .itemsAffected(itemsDeleted)
                .build();

          } catch (Exception e) {
            log.error("Failed to delete directory: {}", path, e);
            return FileOperationResult.builder()
                .operation(FileOperationResult.OperationType.DELETE_DIR)
                .source(path)
                .success(false)
                .error(e.getMessage())
                .build();
          }
        });
  }

  // ========== INFORMATION OPERATIONS ==========

  /**
   * Get file metadata.
   *
   * @param pathStr the file path
   * @return Mono containing file info
   */
  public Mono<FileInfo> getFileInfo(String pathStr) {
    return Mono.fromCallable(
        () -> {
          Path path = Paths.get(pathStr);
          validator.validateRead(path);

          return buildFileInfo(path);
        });
  }

  /**
   * Check if file exists.
   *
   * @param pathStr the file path
   * @return Mono containing boolean
   */
  public Mono<Boolean> exists(String pathStr) {
    return Mono.fromCallable(
        () -> {
          Path path = Paths.get(pathStr);
          validator.validatePath(path);
          return Files.exists(path);
        });
  }

  // ========== SEARCH OPERATIONS ==========

  /**
   * Find files by pattern.
   *
   * @param request the search request
   * @return Mono containing list of matching files
   */
  public Mono<List<FileInfo>> findFiles(FileSearchRequest request) {
    return Mono.fromCallable(
        () -> {
          validator.validateRead(request.getDirectory());

          if (request.isRecursive()) {
            int depth = request.getMaxDepth() == -1 ? config.getMaxDepth() : request.getMaxDepth();
            validator.validateDepth(depth);
          }

          List<FileInfo> results = new ArrayList<>();

          try {
            PathMatcher matcher =
                request.getPattern() != null
                    ? FileSystems.getDefault().getPathMatcher("glob:" + request.getPattern())
                    : null;

            int maxDepth =
                request.isRecursive()
                    ? (request.getMaxDepth() == -1 ? Integer.MAX_VALUE : request.getMaxDepth())
                    : 1;

            Files.walkFileTree(
                request.getDirectory(),
                java.util.EnumSet.noneOf(FileVisitOption.class),
                maxDepth,
                new SimpleFileVisitor<>() {
                  @Override
                  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (results.size() >= request.getMaxResults()) {
                      return FileVisitResult.TERMINATE;
                    }

                    if (matchesSearchCriteria(file, attrs, request, matcher)) {
                      results.add(buildFileInfo(file));
                    }

                    return FileVisitResult.CONTINUE;
                  }
                });

          } catch (IOException e) {
            log.error("Failed to search files in: {}", request.getDirectory(), e);
          }

          validator.validateResultCount(results.size());
          return results;
        });
  }

  // ========== HELPER METHODS ==========

  private FileInfo buildFileInfo(Path path) {
    try {
      BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

      FileInfo.FileInfoBuilder builder =
          FileInfo.builder()
              .path(path)
              .name(path.getFileName() != null ? path.getFileName().toString() : path.toString())
              .size(attrs.size())
              .directory(attrs.isDirectory())
              .file(attrs.isRegularFile())
              .symlink(attrs.isSymbolicLink())
              .readable(Files.isReadable(path))
              .writable(Files.isWritable(path))
              .executable(Files.isExecutable(path))
              .hidden(Files.isHidden(path))
              .lastModified(attrs.lastModifiedTime().toInstant())
              .created(attrs.creationTime().toInstant())
              .lastAccessed(attrs.lastAccessTime().toInstant());

      if (!attrs.isDirectory()) {
        builder.extension(getExtension(path));
      }

      // Try to get owner (may not be available on all systems)
      try {
        builder.owner(Files.getOwner(path).getName());
      } catch (Exception e) {
        // Owner not available
      }

      // Calculate hash if enabled
      if (config.isCalculateHashes() && attrs.isRegularFile()) {
        try {
          builder.hash(calculateFileHash(path));
        } catch (Exception e) {
          log.debug("Failed to calculate hash for: {}", path, e);
        }
      }

      return builder.build();

    } catch (IOException e) {
      log.error("Failed to read file info: {}", path, e);
      throw new RuntimeException("Failed to read file info: " + path, e);
    }
  }

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

  private int deleteRecursively(Path path) throws IOException {
    final int[] count = {0};

    Files.walkFileTree(
        path,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.delete(file);
            count[0]++;
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            count[0]++;
            return FileVisitResult.CONTINUE;
          }
        });

    return count[0];
  }

  private boolean matchesSearchCriteria(
      Path file, BasicFileAttributes attrs, FileSearchRequest request, PathMatcher matcher) {
    // Check search type
    if (request.getSearchType() == FileSearchRequest.SearchType.FILES && !attrs.isRegularFile()) {
      return false;
    }
    if (request.getSearchType() == FileSearchRequest.SearchType.DIRECTORIES
        && !attrs.isDirectory()) {
      return false;
    }

    // Check pattern
    if (matcher != null && !matcher.matches(file.getFileName())) {
      return false;
    }

    // Check hidden files
    try {
      if (!request.isIncludeHidden() && Files.isHidden(file)) {
        return false;
      }
    } catch (IOException e) {
      // Ignore
    }

    // Check size
    if (request.getMinSize() != null && attrs.size() < request.getMinSize()) {
      return false;
    }
    if (request.getMaxSize() != null && attrs.size() > request.getMaxSize()) {
      return false;
    }

    // Check modification time
    Instant lastModified = attrs.lastModifiedTime().toInstant();
    if (request.getModifiedAfter() != null && lastModified.isBefore(request.getModifiedAfter())) {
      return false;
    }
    if (request.getModifiedBefore() != null && lastModified.isAfter(request.getModifiedBefore())) {
      return false;
    }

    // Check extensions
    if (request.getExtensions() != null && request.getExtensions().length > 0) {
      String extension = getExtension(file);
      if (extension == null) {
        return false;
      }

      boolean matchesExtension = false;
      for (String ext : request.getExtensions()) {
        if (extension.equalsIgnoreCase(ext)) {
          matchesExtension = true;
          break;
        }
      }

      if (!matchesExtension) {
        return false;
      }
    }

    return true;
  }

  private String calculateFileHash(Path path) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] bytes = Files.readAllBytes(path);
    byte[] hash = digest.digest(bytes);

    StringBuilder hexString = new StringBuilder();
    for (byte b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }

    return hexString.toString();
  }
}
