package dev.adeengineer.adentic.tool.filesystem.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import dev.adeengineer.adentic.tool.filesystem.config.FileSystemConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import org.junit.jupiter.api.*;

/**
 * Comprehensive tests for PathValidator covering:
 *
 * <ul>
 *   <li>Path validation and sanitization
 *   <li>Security checks (directory traversal, symlinks)
 *   <li>Allowed paths checking
 *   <li>Path normalization
 *   <li>Error handling
 * </ul>
 */
@DisplayName("PathValidator Tests")
class PathValidatorTest {

  private PathValidator validator;
  private Path testDir;
  private Path testFile;

  @BeforeEach
  void setUp() throws IOException {
    testDir = Files.createTempDirectory("pathValidatorTest");
    testFile = testDir.resolve("test.txt");
    Files.writeString(testFile, "Test content");
  }

  @AfterEach
  void tearDown() throws IOException {
    if (Files.exists(testFile)) {
      Files.delete(testFile);
    }
    if (Files.exists(testDir)) {
      Files.delete(testDir);
    }
  }

  @Nested
  @DisplayName("Constructor and Initialization")
  class ConstructorTests {

    @Test
    @DisplayName("Should initialize with default configuration")
    void shouldInitializeWithDefaultConfig() {
      FileSystemConfig config = FileSystemConfig.defaults();
      validator = new PathValidator(config);

      assertNotNull(validator);
    }

    @Test
    @DisplayName("Should initialize with custom configuration")
    void shouldInitializeWithCustomConfig() {
      FileSystemConfig config =
          FileSystemConfig.builder().validatePaths(true).allowRead(true).allowWrite(true).build();
      validator = new PathValidator(config);

      assertNotNull(validator);
    }
  }

  @Nested
  @DisplayName("Path Validation Tests")
  class PathValidationTests {

    @BeforeEach
    void setUpValidator() {
      FileSystemConfig config =
          FileSystemConfig.builder().validatePaths(true).allowedRoots(Set.of(testDir)).build();
      validator = new PathValidator(config);
    }

    @Test
    @DisplayName("Should validate allowed path")
    void shouldValidateAllowedPath() {
      assertDoesNotThrow(() -> validator.validatePath(testFile));
    }

    @Test
    @DisplayName("Should reject path outside allowed roots")
    void shouldRejectPathOutsideAllowedRoots() {
      Path outsidePath = Paths.get("/tmp/outside");

      assertThatThrownBy(() -> validator.validatePath(outsidePath))
          .isInstanceOf(SecurityException.class)
          .hasMessageContaining("not allowed by configuration");
    }

    @Test
    @DisplayName("Should allow paths when validation is disabled")
    void shouldAllowPathsWhenValidationDisabled() {
      FileSystemConfig config = FileSystemConfig.builder().validatePaths(false).build();
      validator = new PathValidator(config);

      assertDoesNotThrow(() -> validator.validatePath(Paths.get("/any/path")));
    }

    @Test
    @DisplayName("Should reject path with null bytes")
    void shouldRejectPathWithNullBytes() {
      // Path with null bytes will throw InvalidPathException when creating the Path object
      // This is the expected behavior - the OS rejects it before we can validate
      assertThatThrownBy(() -> Paths.get("/path/with\0null"))
          .isInstanceOf(java.nio.file.InvalidPathException.class)
          .hasMessageContaining("Nul character");
    }

    @Test
    @DisplayName("Should reject excessively long paths")
    void shouldRejectExcessivelyLongPaths() {
      FileSystemConfig config = FileSystemConfig.builder().validatePaths(true).build();
      validator = new PathValidator(config);

      String longPath = "/path/" + "a".repeat(5000);

      assertThatThrownBy(() -> validator.validatePath(Paths.get(longPath)))
          .isInstanceOf(SecurityException.class)
          .hasMessageContaining("too long");
    }

    @Test
    @DisplayName("Should handle path traversal attempts with ..")
    void shouldHandlePathTraversalAttempts() {
      FileSystemConfig config =
          FileSystemConfig.builder().validatePaths(true).allowedRoots(Set.of(testDir)).build();
      validator = new PathValidator(config);

      // This should be normalized and checked
      Path traversalPath = testDir.resolve("subdir/../file.txt");
      assertDoesNotThrow(() -> validator.validatePath(traversalPath));
    }
  }

  @Nested
  @DisplayName("Read Validation Tests")
  class ReadValidationTests {

    @BeforeEach
    void setUpValidator() {
      FileSystemConfig config =
          FileSystemConfig.builder()
              .validatePaths(true)
              .allowRead(true)
              .allowedRoots(Set.of(testDir))
              .followSymlinks(false)
              .build();
      validator = new PathValidator(config);
    }

    @Test
    @DisplayName("Should validate read operation on existing file")
    void shouldValidateReadOnExistingFile() {
      assertDoesNotThrow(() -> validator.validateRead(testFile));
    }

    @Test
    @DisplayName("Should reject read when not allowed")
    void shouldRejectReadWhenNotAllowed() {
      FileSystemConfig config = FileSystemConfig.builder().allowRead(false).build();
      validator = new PathValidator(config);

      assertThatThrownBy(() -> validator.validateRead(testFile))
          .isInstanceOf(SecurityException.class)
          .hasMessageContaining("Read operations are not allowed");
    }

    @Test
    @DisplayName("Should reject read on non-existent file")
    void shouldRejectReadOnNonExistentFile() {
      Path nonExistent = testDir.resolve("nonexistent.txt");

      assertThatThrownBy(() -> validator.validateRead(nonExistent))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("does not exist");
    }

    @Test
    @DisplayName("Should reject symbolic links when not allowed")
    void shouldRejectSymlinksWhenNotAllowed() throws IOException {
      Path target = testDir.resolve("target.txt");
      Path link = testDir.resolve("link.txt");
      Files.writeString(target, "content");

      try {
        Files.createSymbolicLink(link, target);

        assertThatThrownBy(() -> validator.validateRead(link))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Symbolic links are not allowed");
      } catch (UnsupportedOperationException e) {
        // Symlinks not supported on this system, skip test
      } finally {
        Files.deleteIfExists(link);
        Files.deleteIfExists(target);
      }
    }

    @Test
    @DisplayName("Should allow symbolic links when configured")
    void shouldAllowSymlinksWhenConfigured() throws IOException {
      FileSystemConfig config =
          FileSystemConfig.builder()
              .validatePaths(true)
              .allowRead(true)
              .allowedRoots(Set.of(testDir))
              .followSymlinks(true)
              .build();
      validator = new PathValidator(config);

      Path target = testDir.resolve("target.txt");
      Path link = testDir.resolve("link.txt");
      Files.writeString(target, "content");

      try {
        Files.createSymbolicLink(link, target);
        assertDoesNotThrow(() -> validator.validateRead(link));
      } catch (UnsupportedOperationException e) {
        // Symlinks not supported on this system, skip test
      } finally {
        Files.deleteIfExists(link);
        Files.deleteIfExists(target);
      }
    }
  }

  @Nested
  @DisplayName("Write Validation Tests")
  class WriteValidationTests {

    @BeforeEach
    void setUpValidator() {
      FileSystemConfig config =
          FileSystemConfig.builder()
              .validatePaths(true)
              .allowWrite(true)
              .allowedRoots(Set.of(testDir))
              .blockedExtensions(Set.of(".exe", ".dll"))
              .build();
      validator = new PathValidator(config);
    }

    @Test
    @DisplayName("Should validate write operation")
    void shouldValidateWriteOperation() {
      Path newFile = testDir.resolve("new.txt");
      assertDoesNotThrow(() -> validator.validateWrite(newFile));
    }

    @Test
    @DisplayName("Should reject write when not allowed")
    void shouldRejectWriteWhenNotAllowed() {
      FileSystemConfig config = FileSystemConfig.builder().allowWrite(false).build();
      validator = new PathValidator(config);

      assertThatThrownBy(() -> validator.validateWrite(testFile))
          .isInstanceOf(SecurityException.class)
          .hasMessageContaining("Write operations are not allowed");
    }

    @Test
    @DisplayName("Should reject blocked file extensions")
    void shouldRejectBlockedExtensions() {
      Path exeFile = testDir.resolve("malware.exe");

      assertThatThrownBy(() -> validator.validateWrite(exeFile))
          .isInstanceOf(SecurityException.class)
          .hasMessageContaining("extension")
          .hasMessageContaining("not allowed");
    }

    @Test
    @DisplayName("Should allow non-blocked extensions")
    void shouldAllowNonBlockedExtensions() {
      Path txtFile = testDir.resolve("document.txt");
      assertDoesNotThrow(() -> validator.validateWrite(txtFile));
    }

    @Test
    @DisplayName("Should reject write when parent directory does not exist")
    void shouldRejectWriteWhenParentNotExists() {
      Path fileInNonExistent = testDir.resolve("nonexistent/file.txt");

      assertThatThrownBy(() -> validator.validateWrite(fileInNonExistent))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Parent directory does not exist");
    }

    @Test
    @DisplayName("Should handle files without extensions")
    void shouldHandleFilesWithoutExtensions() {
      Path noExtension = testDir.resolve("noextension");
      assertDoesNotThrow(() -> validator.validateWrite(noExtension));
    }
  }

  @Nested
  @DisplayName("Delete Validation Tests")
  class DeleteValidationTests {

    @BeforeEach
    void setUpValidator() {
      FileSystemConfig config =
          FileSystemConfig.builder()
              .validatePaths(true)
              .allowDelete(true)
              .allowedRoots(Set.of(testDir))
              .build();
      validator = new PathValidator(config);
    }

    @Test
    @DisplayName("Should validate delete operation")
    void shouldValidateDeleteOperation() {
      assertDoesNotThrow(() -> validator.validateDelete(testFile));
    }

    @Test
    @DisplayName("Should reject delete when not allowed")
    void shouldRejectDeleteWhenNotAllowed() {
      FileSystemConfig config = FileSystemConfig.builder().allowDelete(false).build();
      validator = new PathValidator(config);

      assertThatThrownBy(() -> validator.validateDelete(testFile))
          .isInstanceOf(SecurityException.class)
          .hasMessageContaining("Delete operations are not allowed");
    }

    @Test
    @DisplayName("Should reject delete of non-existent path")
    void shouldRejectDeleteOfNonExistentPath() {
      Path nonExistent = testDir.resolve("nonexistent.txt");

      assertThatThrownBy(() -> validator.validateDelete(nonExistent))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("does not exist");
    }
  }

  @Nested
  @DisplayName("Create Directory Validation Tests")
  class CreateDirectoryValidationTests {

    @BeforeEach
    void setUpValidator() {
      FileSystemConfig config =
          FileSystemConfig.builder()
              .validatePaths(true)
              .allowCreateDirectory(true)
              .allowedRoots(Set.of(testDir))
              .build();
      validator = new PathValidator(config);
    }

    @Test
    @DisplayName("Should validate directory creation")
    void shouldValidateDirectoryCreation() {
      Path newDir = testDir.resolve("newdir");
      assertDoesNotThrow(() -> validator.validateCreateDirectory(newDir));
    }

    @Test
    @DisplayName("Should reject directory creation when not allowed")
    void shouldRejectCreateDirectoryWhenNotAllowed() {
      FileSystemConfig config = FileSystemConfig.builder().allowCreateDirectory(false).build();
      validator = new PathValidator(config);

      Path newDir = testDir.resolve("newdir");

      assertThatThrownBy(() -> validator.validateCreateDirectory(newDir))
          .isInstanceOf(SecurityException.class)
          .hasMessageContaining("Create directory operations are not allowed");
    }

    @Test
    @DisplayName("Should reject directory creation when path already exists")
    void shouldRejectCreateDirectoryWhenExists() {
      assertThatThrownBy(() -> validator.validateCreateDirectory(testDir))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("already exists");
    }
  }

  @Nested
  @DisplayName("File Size Validation Tests")
  class FileSizeValidationTests {

    @BeforeEach
    void setUpValidator() {
      FileSystemConfig config =
          FileSystemConfig.builder()
              .maxReadSize(100)
              .maxWriteSize(200)
              .allowedRoots(Set.of(testDir))
              .build();
      validator = new PathValidator(config);
    }

    @Test
    @DisplayName("Should validate read size within limit")
    void shouldValidateReadSizeWithinLimit() throws IOException {
      Path smallFile = testDir.resolve("small.txt");
      Files.writeString(smallFile, "Small");

      try {
        assertDoesNotThrow(() -> validator.validateReadSize(smallFile));
      } finally {
        Files.deleteIfExists(smallFile);
      }
    }

    @Test
    @DisplayName("Should reject read of file exceeding max size")
    void shouldRejectReadExceedingMaxSize() throws IOException {
      Path largeFile = testDir.resolve("large.txt");
      Files.writeString(largeFile, "X".repeat(200));

      try {
        assertThatThrownBy(() -> validator.validateReadSize(largeFile))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("exceeds maximum allowed size");
      } finally {
        Files.deleteIfExists(largeFile);
      }
    }

    @Test
    @DisplayName("Should validate write size within limit")
    void shouldValidateWriteSizeWithinLimit() {
      assertDoesNotThrow(() -> validator.validateWriteSize(100));
    }

    @Test
    @DisplayName("Should reject write exceeding max size")
    void shouldRejectWriteExceedingMaxSize() {
      assertThatThrownBy(() -> validator.validateWriteSize(300))
          .isInstanceOf(SecurityException.class)
          .hasMessageContaining("exceeds maximum allowed size");
    }
  }

  @Nested
  @DisplayName("Depth and Result Count Validation Tests")
  class DepthAndCountValidationTests {

    @BeforeEach
    void setUpValidator() {
      FileSystemConfig config = FileSystemConfig.builder().maxDepth(5).maxResults(100).build();
      validator = new PathValidator(config);
    }

    @Test
    @DisplayName("Should validate depth within limit")
    void shouldValidateDepthWithinLimit() {
      assertDoesNotThrow(() -> validator.validateDepth(3));
    }

    @Test
    @DisplayName("Should reject depth exceeding maximum")
    void shouldRejectDepthExceedingMaximum() {
      assertThatThrownBy(() -> validator.validateDepth(10))
          .isInstanceOf(SecurityException.class)
          .hasMessageContaining("exceeds maximum allowed depth");
    }

    @Test
    @DisplayName("Should validate result count within limit")
    void shouldValidateResultCountWithinLimit() {
      // This logs a warning but doesn't throw
      assertDoesNotThrow(() -> validator.validateResultCount(50));
    }

    @Test
    @DisplayName("Should warn when result count exceeds maximum")
    void shouldWarnWhenResultCountExceedsMaximum() {
      // This should log warning but not throw
      assertDoesNotThrow(() -> validator.validateResultCount(200));
    }
  }

  @Nested
  @DisplayName("Path Normalization Tests")
  class PathNormalizationTests {

    @BeforeEach
    void setUpValidator() {
      FileSystemConfig config =
          FileSystemConfig.builder().validatePaths(true).allowedRoots(Set.of(testDir)).build();
      validator = new PathValidator(config);
    }

    @Test
    @DisplayName("Should normalize relative paths")
    void shouldNormalizeRelativePaths() {
      Path relative = testDir.resolve("./subdir/../file.txt");
      assertDoesNotThrow(() -> validator.validatePath(relative));
    }

    @Test
    @DisplayName("Should normalize paths with multiple separators")
    void shouldNormalizePathsWithMultipleSeparators() {
      String pathStr = testDir.toString() + "//subdir//file.txt";
      Path path = Paths.get(pathStr);
      assertDoesNotThrow(() -> validator.validatePath(path));
    }

    @Test
    @DisplayName("Should handle paths with dot segments")
    void shouldHandlePathsWithDotSegments() {
      Path dotPath = testDir.resolve("./file.txt");
      assertDoesNotThrow(() -> validator.validatePath(dotPath));
    }
  }

  @Nested
  @DisplayName("Edge Cases and Error Handling")
  class EdgeCasesTests {

    @Test
    @DisplayName("Should handle empty allowed roots gracefully")
    void shouldHandleEmptyAllowedRoots() {
      FileSystemConfig config =
          FileSystemConfig.builder().validatePaths(true).allowedRoots(Set.of()).build();
      validator = new PathValidator(config);

      // With empty allowed roots, all paths should be allowed (unless blocked)
      assertDoesNotThrow(() -> validator.validatePath(testFile));
    }

    @Test
    @DisplayName("Should handle blocked paths")
    void shouldHandleBlockedPaths() {
      FileSystemConfig config =
          FileSystemConfig.builder().validatePaths(true).blockedPaths(Set.of(testDir)).build();
      validator = new PathValidator(config);

      assertThatThrownBy(() -> validator.validatePath(testFile))
          .isInstanceOf(SecurityException.class)
          .hasMessageContaining("not allowed by configuration");
    }

    @Test
    @DisplayName("Should validate case-insensitive extension blocking")
    void shouldValidateCaseInsensitiveExtensions() {
      FileSystemConfig config =
          FileSystemConfig.builder()
              .validatePaths(true)
              .allowWrite(true)
              .allowedRoots(Set.of(testDir))
              .blockedExtensions(Set.of(".exe"))
              .build();
      validator = new PathValidator(config);

      Path upperCaseExt = testDir.resolve("file.EXE");

      assertThatThrownBy(() -> validator.validateWrite(upperCaseExt))
          .isInstanceOf(SecurityException.class)
          .hasMessageContaining("not allowed");
    }

    @Test
    @DisplayName("Should handle root path validation")
    void shouldHandleRootPathValidation() {
      FileSystemConfig config =
          FileSystemConfig.builder()
              .validatePaths(true)
              .allowedRoots(Set.of(Paths.get("/")))
              .build();
      validator = new PathValidator(config);

      assertDoesNotThrow(() -> validator.validatePath(Paths.get("/")));
    }
  }

  @Nested
  @DisplayName("Configuration Preset Tests")
  class ConfigurationPresetTests {

    @Test
    @DisplayName("Should work with default configuration")
    void shouldWorkWithDefaultConfiguration() {
      validator = new PathValidator(FileSystemConfig.defaults());
      assertDoesNotThrow(() -> validator.validatePath(testFile));
    }

    @Test
    @DisplayName("Should work with read-only configuration")
    void shouldWorkWithReadOnlyConfiguration() {
      validator = new PathValidator(FileSystemConfig.readOnly());

      assertDoesNotThrow(() -> validator.validateRead(testFile));
      assertThatThrownBy(() -> validator.validateWrite(testFile))
          .isInstanceOf(SecurityException.class);
    }

    @Test
    @DisplayName("Should work with sandboxed configuration")
    void shouldWorkWithSandboxedConfiguration() {
      validator = new PathValidator(FileSystemConfig.sandboxed(testDir.toString()));

      assertDoesNotThrow(() -> validator.validatePath(testFile));

      Path outsidePath = Paths.get("/tmp/outside.txt");
      assertThatThrownBy(() -> validator.validatePath(outsidePath))
          .isInstanceOf(SecurityException.class);
    }

    @Test
    @DisplayName("Should work with secure configuration")
    void shouldWorkWithSecureConfiguration() {
      validator = new PathValidator(FileSystemConfig.secure());

      assertDoesNotThrow(() -> validator.validateRead(testFile));
      assertThatThrownBy(() -> validator.validateWrite(testFile))
          .isInstanceOf(SecurityException.class);
    }
  }
}
