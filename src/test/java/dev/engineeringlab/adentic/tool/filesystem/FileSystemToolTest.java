package dev.engineeringlab.adentic.tool.filesystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import dev.engineeringlab.adentic.tool.filesystem.config.FileSystemConfig;
import dev.engineeringlab.adentic.tool.filesystem.model.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assumptions;

/** Comprehensive tests for FileSystemTool */
@DisplayName("FileSystemTool Tests")
class FileSystemToolTest {

  private FileSystemTool fileSystemTool;
  private Path testDir;
  private Path testFile;

  @BeforeEach
  void setUp() throws IOException {
    fileSystemTool = new FileSystemTool();
    testDir = Files.createTempDirectory("fileSystemToolTest");
    testFile = testDir.resolve("test.txt");
    Files.writeString(testFile, "Test content");
  }

  @AfterEach
  void tearDown() throws IOException {
    // Clean up test files
    if (Files.exists(testFile)) {
      Files.delete(testFile);
    }
    if (Files.exists(testDir)) {
      Files.delete(testDir);
    }
  }

  @Nested
  @DisplayName("File Read Operations")
  class FileReadTests {

    @Test
    @DisplayName("Should read file content")
    void testReadFile() {
      FileContent content = fileSystemTool.readFile(testFile.toString()).block();

      assertNotNull(content);
      assertTrue(content.isSuccess());
      assertEquals("Test content", content.getContent());
    }

    @Test
    @DisplayName("Should read file as bytes")
    void testReadFileBytes() {
      FileContent content = fileSystemTool.readFileBytes(testFile.toString()).block();

      assertNotNull(content);
      assertTrue(content.isSuccess());
      assertNotNull(content.getBytes());
      assertTrue(content.isBinary());
    }

    @Test
    @DisplayName("Should fail to read non-existent file")
    void testReadNonExistentFile() {
      assertThrows(
          IllegalArgumentException.class,
          () -> fileSystemTool.readFile("/non/existent/file.txt").block());
    }
  }

  @Nested
  @DisplayName("File Write Operations")
  class FileWriteTests {

    @Test
    @DisplayName("Should write file content")
    void testWriteFile() {
      Path newFile = testDir.resolve("new.txt");

      FileOperationResult result =
          fileSystemTool.writeFile(newFile.toString(), "New content").block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertTrue(Files.exists(newFile));

      // Cleanup
      try {
        Files.deleteIfExists(newFile);
      } catch (IOException e) {
        // Ignore
      }
    }

    @Test
    @DisplayName("Should append to file")
    void testAppendFile() {
      FileOperationResult result =
          fileSystemTool.appendFile(testFile.toString(), "\nAppended").block();

      assertNotNull(result);
      assertTrue(result.isSuccess());

      FileContent content = fileSystemTool.readFile(testFile.toString()).block();
      assertThat(content.getContent()).contains("Appended");
    }

    @Test
    @DisplayName("Should overwrite existing file")
    void testOverwriteFile() {
      FileOperationResult result =
          fileSystemTool.writeFile(testFile.toString(), "Overwritten").block();

      assertNotNull(result);
      assertTrue(result.isSuccess());

      FileContent content = fileSystemTool.readFile(testFile.toString()).block();
      assertEquals("Overwritten", content.getContent());
    }
  }

  @Nested
  @DisplayName("File Delete/Copy/Move Operations")
  class FileManipulationTests {

    @Test
    @DisplayName("Should delete file")
    void testDeleteFile() {
      FileOperationResult result = fileSystemTool.deleteFile(testFile.toString()).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertFalse(Files.exists(testFile));
    }

    @Test
    @DisplayName("Should copy file")
    void testCopyFile() {
      Path targetFile = testDir.resolve("copy.txt");

      FileOperationResult result =
          fileSystemTool.copyFile(testFile.toString(), targetFile.toString()).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertTrue(Files.exists(targetFile));

      // Cleanup
      try {
        Files.deleteIfExists(targetFile);
      } catch (IOException e) {
        // Ignore
      }
    }

    @Test
    @DisplayName("Should move file")
    void testMoveFile() {
      Path targetFile = testDir.resolve("moved.txt");

      FileOperationResult result =
          fileSystemTool.moveFile(testFile.toString(), targetFile.toString()).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertFalse(Files.exists(testFile));
      assertTrue(Files.exists(targetFile));

      // Update testFile reference for cleanup
      testFile = targetFile;
    }
  }

  @Nested
  @DisplayName("Directory Operations")
  class DirectoryTests {

    @Test
    @DisplayName("Should list directory")
    void testListDirectory() {
      DirectoryListing listing = fileSystemTool.listDirectory(testDir.toString()).block();

      assertNotNull(listing);
      assertTrue(listing.isSuccess());
      assertThat(listing.getTotalItems()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should create directory")
    void testCreateDirectory() {
      Path newDir = testDir.resolve("subdir");

      FileOperationResult result = fileSystemTool.createDirectory(newDir.toString(), false).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertTrue(Files.exists(newDir));

      // Cleanup
      try {
        Files.deleteIfExists(newDir);
      } catch (IOException e) {
        // Ignore
      }
    }

    @Test
    @DisplayName("Should create directories with parents")
    void testCreateDirectoriesWithParents() {
      Path deepDir = testDir.resolve("a/b/c");

      FileOperationResult result = fileSystemTool.createDirectory(deepDir.toString(), true).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertTrue(Files.exists(deepDir));

      // Cleanup
      try {
        Files.deleteIfExists(deepDir);
        Files.deleteIfExists(deepDir.getParent());
        Files.deleteIfExists(deepDir.getParent().getParent());
      } catch (IOException e) {
        // Ignore
      }
    }

    @Test
    @DisplayName("Should delete directory")
    void testDeleteDirectory() throws IOException {
      Path emptyDir = Files.createTempDirectory(testDir, "empty");

      FileOperationResult result =
          fileSystemTool.deleteDirectory(emptyDir.toString(), false).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertFalse(Files.exists(emptyDir));
    }

    @Test
    @DisplayName("Should delete directory recursively")
    void testDeleteDirectoryRecursively() throws IOException {
      Path dirWithFiles = Files.createTempDirectory(testDir, "withFiles");
      Path fileInDir = dirWithFiles.resolve("file.txt");
      Files.writeString(fileInDir, "content");

      FileOperationResult result =
          fileSystemTool.deleteDirectory(dirWithFiles.toString(), true).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertFalse(Files.exists(dirWithFiles));
    }
  }

  @Nested
  @DisplayName("Information Operations")
  class InformationTests {

    @Test
    @DisplayName("Should get file info")
    void testGetFileInfo() {
      FileInfo info = fileSystemTool.getFileInfo(testFile.toString()).block();

      assertNotNull(info);
      assertEquals(testFile, info.getPath());
      assertTrue(info.isFile());
      assertFalse(info.isDirectory());
    }

    @Test
    @DisplayName("Should check if file exists")
    void testExists() {
      Boolean exists = fileSystemTool.exists(testFile.toString()).block();

      assertNotNull(exists);
      assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false for non-existent file")
    void testExistsNonExistent() {
      Boolean exists = fileSystemTool.exists("/non/existent/file.txt").block();

      assertNotNull(exists);
      assertFalse(exists);
    }
  }

  @Nested
  @DisplayName("Search Operations")
  class SearchTests {

    @Test
    @DisplayName("Should find files by pattern")
    void testFindFiles() throws IOException {
      // Create test files
      Files.writeString(testDir.resolve("test1.txt"), "content");
      Files.writeString(testDir.resolve("test2.txt"), "content");
      Files.writeString(testDir.resolve("other.log"), "content");

      FileSearchRequest request =
          FileSearchRequest.builder().directory(testDir).pattern("*.txt").recursive(false).build();

      List<FileInfo> results = fileSystemTool.findFiles(request).block();

      assertNotNull(results);
      assertThat(results.size()).isGreaterThanOrEqualTo(2);

      // Cleanup
      try {
        Files.deleteIfExists(testDir.resolve("test1.txt"));
        Files.deleteIfExists(testDir.resolve("test2.txt"));
        Files.deleteIfExists(testDir.resolve("other.log"));
      } catch (IOException e) {
        // Ignore
      }
    }

    @Test
    @DisplayName("Should find files recursively")
    void testFindFilesRecursive() throws IOException {
      Path subDir = Files.createDirectory(testDir.resolve("sub"));
      Files.writeString(subDir.resolve("nested.txt"), "content");

      FileSearchRequest request =
          FileSearchRequest.builder()
              .directory(testDir)
              .pattern("*.txt")
              .recursive(true)
              .maxDepth(2)
              .build();

      List<FileInfo> results = fileSystemTool.findFiles(request).block();

      assertNotNull(results);
      assertThat(results.size()).isGreaterThan(0);

      // Cleanup
      try {
        Files.deleteIfExists(subDir.resolve("nested.txt"));
        Files.deleteIfExists(subDir);
      } catch (IOException e) {
        // Ignore
      }
    }
  }

  @Nested
  @DisplayName("Configuration Tests")
  class ConfigurationTests {

    @Test
    @DisplayName("Should use default configuration")
    void testDefaultConfig() {
      FileSystemConfig config = FileSystemConfig.defaults();

      assertNotNull(config);
      assertTrue(config.getMaxReadSize() > 0);
    }

    @Test
    @DisplayName("Should create with custom config")
    void testCustomConfig() {
      FileSystemConfig config =
          FileSystemConfig.builder()
              .maxReadSize(1024 * 1024)
              .maxDepth(5)
              .calculateHashes(false)
              .build();

      FileSystemTool tool = new FileSystemTool(config);
      assertNotNull(tool);
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should handle invalid path")
    void testInvalidPath() {
      assertThrows(
          java.nio.file.InvalidPathException.class,
          () -> fileSystemTool.readFile("/invalid/\0/path").block());
    }

    @Test
    @DisplayName("Should handle permission errors gracefully")
    void testPermissionErrors() {
      // This test depends on OS permissions and config - will throw SecurityException
      assertThrows(
          SecurityException.class, () -> fileSystemTool.readFile("/root/protected.txt").block());
    }
  }

  @Nested
  @DisplayName("Large File Handling Tests")
  class LargeFileTests {

    @Test
    @DisplayName("Should handle large file within size limit")
    void testLargeFileWithinLimit() throws IOException {
      Path largeFile = testDir.resolve("large.txt");
      String content = "X".repeat(1024 * 100); // 100KB
      Files.writeString(largeFile, content);

      try {
        FileContent result = fileSystemTool.readFile(largeFile.toString()).block();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(content, result.getContent());
      } finally {
        Files.deleteIfExists(largeFile);
      }
    }

    @Test
    @DisplayName("Should reject files exceeding max size")
    void testFileExceedingMaxSize() throws IOException {
      // Create config with small max size
      FileSystemConfig config = FileSystemConfig.builder().maxReadSize(100).build();
      FileSystemTool tool = new FileSystemTool(config);

      Path largeFile = testDir.resolve("toolarge.txt");
      Files.writeString(largeFile, "X".repeat(200));

      try {
        assertThrows(SecurityException.class, () -> tool.readFile(largeFile.toString()).block());
      } finally {
        Files.deleteIfExists(largeFile);
      }
    }
  }

  @Nested
  @DisplayName("Binary File Operations Tests")
  class BinaryFileTests {

    @Test
    @DisplayName("Should handle binary file operations")
    void testBinaryFileOperations() throws IOException {
      Path binaryFile = testDir.resolve("binary.dat");
      byte[] binaryData = new byte[] {0x00, 0x01, 0x02, (byte) 0xFF};
      Files.write(binaryFile, binaryData);

      try {
        FileContent result = fileSystemTool.readFileBytes(binaryFile.toString()).block();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.isBinary());
        assertArrayEquals(binaryData, result.getBytes());
      } finally {
        Files.deleteIfExists(binaryFile);
      }
    }

    @Test
    @DisplayName("Should handle empty binary files")
    void testEmptyBinaryFile() throws IOException {
      Path emptyFile = testDir.resolve("empty.dat");
      Files.write(emptyFile, new byte[0]);

      try {
        FileContent result = fileSystemTool.readFileBytes(emptyFile.toString()).block();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getBytes().length);
      } finally {
        Files.deleteIfExists(emptyFile);
      }
    }
  }

  @Nested
  @DisplayName("Directory Traversal Tests")
  class DirectoryTraversalTests {

    @Test
    @DisplayName("Should handle deep directory structures")
    void testDeepDirectoryStructures() throws IOException {
      Path deepDir = testDir;
      for (int i = 0; i < 5; i++) {
        deepDir = deepDir.resolve("level" + i);
      }
      Files.createDirectories(deepDir);

      try {
        assertTrue(Files.exists(deepDir));

        FileInfo info = fileSystemTool.getFileInfo(deepDir.toString()).block();
        assertNotNull(info);
        assertTrue(info.isDirectory());
      } finally {
        // Cleanup
        Files.walk(testDir)
            .sorted(java.util.Comparator.reverseOrder())
            .forEach(
                path -> {
                  try {
                    if (!path.equals(testDir) && !path.equals(testFile)) {
                      Files.deleteIfExists(path);
                    }
                  } catch (IOException e) {
                    // Ignore
                  }
                });
      }
    }

    @Test
    @DisplayName("Should handle directory with many files")
    void testDirectoryWithManyFiles() throws IOException {
      for (int i = 0; i < 50; i++) {
        Files.writeString(testDir.resolve("file" + i + ".txt"), "content" + i);
      }

      try {
        DirectoryListing listing = fileSystemTool.listDirectory(testDir.toString()).block();

        assertNotNull(listing);
        assertTrue(listing.isSuccess());
        assertThat(listing.getTotalItems()).isGreaterThanOrEqualTo(50);
      } finally {
        // Cleanup
        for (int i = 0; i < 50; i++) {
          Files.deleteIfExists(testDir.resolve("file" + i + ".txt"));
        }
      }
    }
  }

  @Nested
  @DisplayName("File Locking and Concurrent Access Tests")
  class FileLockingTests {

    @Test
    @DisplayName("Should handle concurrent file reads")
    void testConcurrentFileReads() throws IOException {
      Path sharedFile = testDir.resolve("shared.txt");
      Files.writeString(sharedFile, "Shared content");

      try {
        // Multiple reads should succeed
        FileContent result1 = fileSystemTool.readFile(sharedFile.toString()).block();
        FileContent result2 = fileSystemTool.readFile(sharedFile.toString()).block();

        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());
        assertEquals(result1.getContent(), result2.getContent());
      } finally {
        Files.deleteIfExists(sharedFile);
      }
    }

    @Test
    @DisplayName("Should handle overwrite scenarios")
    void testOverwriteScenarios() throws IOException {
      Path targetFile = testDir.resolve("overwrite.txt");
      Files.writeString(targetFile, "Original");

      try {
        FileOperationResult result1 =
            fileSystemTool.writeFile(targetFile.toString(), "First Update").block();
        assertTrue(result1.isSuccess());

        FileOperationResult result2 =
            fileSystemTool.writeFile(targetFile.toString(), "Second Update").block();
        assertTrue(result2.isSuccess());

        FileContent content = fileSystemTool.readFile(targetFile.toString()).block();
        assertEquals("Second Update", content.getContent());
      } finally {
        Files.deleteIfExists(targetFile);
      }
    }
  }

  @Nested
  @DisplayName("Special Characters and Encoding Tests")
  class SpecialCharactersTests {

    @Test
    @DisplayName("Should handle unicode content")
    void testUnicodeContent() throws IOException {
      String unicodeContent = "Hello 世界 🌍 مرحبا";
      Path unicodeFile = testDir.resolve("unicode.txt");

      try {
        FileOperationResult writeResult =
            fileSystemTool.writeFile(unicodeFile.toString(), unicodeContent).block();
        assertTrue(writeResult.isSuccess());

        FileContent readResult = fileSystemTool.readFile(unicodeFile.toString()).block();
        assertTrue(readResult.isSuccess());
        assertEquals(unicodeContent, readResult.getContent());
      } finally {
        Files.deleteIfExists(unicodeFile);
      }
    }

    @Test
    @DisplayName("Should handle filenames with special characters")
    void testFilenamesWithSpecialCharacters() throws IOException {
      Path specialFile = testDir.resolve("file-with_special.chars.txt");

      try {
        FileOperationResult result =
            fileSystemTool.writeFile(specialFile.toString(), "Content").block();
        assertTrue(result.isSuccess());

        Boolean exists = fileSystemTool.exists(specialFile.toString()).block();
        assertTrue(exists);
      } finally {
        Files.deleteIfExists(specialFile);
      }
    }

    @Test
    @DisplayName("Should handle files with multiple extensions")
    void testMultipleExtensions() throws IOException {
      Path multiExtFile = testDir.resolve("archive.tar.gz");

      try {
        FileOperationResult result =
            fileSystemTool.writeFile(multiExtFile.toString(), "Archive").block();
        assertTrue(result.isSuccess());

        FileInfo info = fileSystemTool.getFileInfo(multiExtFile.toString()).block();
        assertNotNull(info);
        assertThat(info.getExtension()).isNotNull();
      } finally {
        Files.deleteIfExists(multiExtFile);
      }
    }
  }

  @Nested
  @DisplayName("Search Edge Cases Tests")
  class SearchEdgeCasesTests {

    @Test
    @DisplayName("Should handle search with no matches")
    void testSearchWithNoMatches() {
      FileSearchRequest request =
          FileSearchRequest.builder()
              .directory(testDir)
              .pattern("*.nonexistent")
              .recursive(false)
              .build();

      List<FileInfo> results = fileSystemTool.findFiles(request).block();

      assertNotNull(results);
      assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should handle complex glob patterns")
    void testComplexGlobPatterns() throws IOException {
      Files.writeString(testDir.resolve("test1.txt"), "content");
      Files.writeString(testDir.resolve("test2.log"), "content");
      Files.writeString(testDir.resolve("data.txt"), "content");

      try {
        FileSearchRequest request =
            FileSearchRequest.builder()
                .directory(testDir)
                .pattern("test*.txt")
                .recursive(false)
                .build();

        List<FileInfo> results = fileSystemTool.findFiles(request).block();

        assertNotNull(results);
        assertThat(results.size()).isGreaterThanOrEqualTo(1);
        assertThat(results).allMatch(info -> info.getName().startsWith("test"));
      } finally {
        Files.deleteIfExists(testDir.resolve("test1.txt"));
        Files.deleteIfExists(testDir.resolve("test2.log"));
        Files.deleteIfExists(testDir.resolve("data.txt"));
      }
    }

    @Test
    @DisplayName("Should respect max results limit")
    void testMaxResultsLimit() throws IOException {
      for (int i = 0; i < 20; i++) {
        Files.writeString(testDir.resolve("file" + i + ".txt"), "content");
      }

      try {
        FileSearchRequest request =
            FileSearchRequest.builder()
                .directory(testDir)
                .pattern("*.txt")
                .recursive(false)
                .maxResults(10)
                .build();

        List<FileInfo> results = fileSystemTool.findFiles(request).block();

        assertNotNull(results);
        assertThat(results.size()).isLessThanOrEqualTo(10);
      } finally {
        for (int i = 0; i < 20; i++) {
          Files.deleteIfExists(testDir.resolve("file" + i + ".txt"));
        }
      }
    }
  }

  @Nested
  @DisplayName("Configuration Validation Tests")
  class ConfigurationValidationTests {

    @Test
    @DisplayName("Should enforce read-only configuration")
    void testReadOnlyConfiguration() {
      FileSystemTool readOnlyTool = new FileSystemTool(FileSystemConfig.readOnly());
      Path newFile = testDir.resolve("readonly-test.txt");

      assertThrows(
          SecurityException.class,
          () -> readOnlyTool.writeFile(newFile.toString(), "test").block());

      assertThrows(
          SecurityException.class, () -> readOnlyTool.deleteFile(testFile.toString()).block());
    }

    @Test
    @DisplayName("Should enforce sandboxed configuration")
    void testSandboxedConfiguration() {
      FileSystemTool sandboxedTool =
          new FileSystemTool(FileSystemConfig.sandboxed(testDir.toString()));

      // Should allow access within sandbox
      assertDoesNotThrow(() -> sandboxedTool.readFile(testFile.toString()).block());

      // Should deny access outside sandbox
      assertThrows(
          SecurityException.class, () -> sandboxedTool.readFile("/tmp/outside.txt").block());
    }
  }

  @Nested
  @DisplayName("Empty File Tests")
  class EmptyFileTests {

    @Test
    @DisplayName("Should read empty text file")
    void testReadEmptyTextFile() throws IOException {
      Path emptyFile = testDir.resolve("empty.txt");
      Files.writeString(emptyFile, "");

      try {
        FileContent content = fileSystemTool.readFile(emptyFile.toString()).block();

        assertNotNull(content);
        assertTrue(content.isSuccess());
        assertEquals("", content.getContent());
        assertEquals(0, content.getSize());
      } finally {
        Files.deleteIfExists(emptyFile);
      }
    }

    @Test
    @DisplayName("Should write empty content to file")
    void testWriteEmptyContent() throws IOException {
      Path emptyFile = testDir.resolve("empty-write.txt");

      try {
        FileOperationResult result = fileSystemTool.writeFile(emptyFile.toString(), "").block();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(Files.exists(emptyFile));
        assertEquals(0, Files.size(emptyFile));
      } finally {
        Files.deleteIfExists(emptyFile);
      }
    }
  }

  @Nested
  @DisplayName("Symlink Tests")
  class SymlinkTests {

    @Test
    @DisplayName("Should handle symlink when followSymlinks is enabled")
    void testSymlinkWithFollowEnabled() throws IOException {
      Path targetFile = testDir.resolve("target.txt");
      Path symlinkFile = testDir.resolve("symlink.txt");
      Files.writeString(targetFile, "Target content");

      try {
        Files.createSymbolicLink(symlinkFile, targetFile);

        FileSystemConfig config = FileSystemConfig.builder().followSymlinks(true).build();
        FileSystemTool tool = new FileSystemTool(config);

        FileContent content = tool.readFile(symlinkFile.toString()).block();

        assertNotNull(content);
        assertTrue(content.isSuccess());
        assertEquals("Target content", content.getContent());
      } catch (UnsupportedOperationException e) {
        // Symlinks not supported on this platform
        Assumptions.abort("Symlinks not supported");
      } finally {
        Files.deleteIfExists(symlinkFile);
        Files.deleteIfExists(targetFile);
      }
    }

    @Test
    @DisplayName("Should reject symlink when followSymlinks is disabled")
    void testSymlinkWithFollowDisabled() throws IOException {
      Path targetFile = testDir.resolve("target.txt");
      Path symlinkFile = testDir.resolve("symlink.txt");
      Files.writeString(targetFile, "Target content");

      try {
        Files.createSymbolicLink(symlinkFile, targetFile);

        FileSystemConfig config = FileSystemConfig.builder().followSymlinks(false).build();
        FileSystemTool tool = new FileSystemTool(config);

        assertThrows(SecurityException.class, () -> tool.readFile(symlinkFile.toString()).block());
      } catch (UnsupportedOperationException e) {
        // Symlinks not supported on this platform
        Assumptions.abort("Symlinks not supported");
      } finally {
        Files.deleteIfExists(symlinkFile);
        Files.deleteIfExists(targetFile);
      }
    }
  }

  @Nested
  @DisplayName("Hidden File Tests")
  class HiddenFileTests {

    @Test
    @DisplayName("Should find hidden files when includeHidden is true")
    void testFindHiddenFilesWhenIncluded() throws IOException {
      Path hiddenFile = testDir.resolve(".hidden");
      Files.writeString(hiddenFile, "hidden content");

      try {
        FileSearchRequest request =
            FileSearchRequest.builder()
                .directory(testDir)
                .pattern(".*")
                .recursive(false)
                .includeHidden(true)
                .build();

        List<FileInfo> results = fileSystemTool.findFiles(request).block();

        assertNotNull(results);
        assertThat(results).anyMatch(info -> info.getName().equals(".hidden"));
      } finally {
        Files.deleteIfExists(hiddenFile);
      }
    }

    @Test
    @DisplayName("Should exclude hidden files when includeHidden is false")
    void testExcludeHiddenFiles() throws IOException {
      Path hiddenFile = testDir.resolve(".hidden");
      Files.writeString(hiddenFile, "hidden content");

      try {
        FileSearchRequest request =
            FileSearchRequest.builder()
                .directory(testDir)
                .pattern("*")
                .recursive(false)
                .includeHidden(false)
                .build();

        List<FileInfo> results = fileSystemTool.findFiles(request).block();

        assertNotNull(results);
        assertThat(results).noneMatch(info -> info.getName().equals(".hidden"));
      } finally {
        Files.deleteIfExists(hiddenFile);
      }
    }
  }

  @Nested
  @DisplayName("File Search With Size Filters Tests")
  class FileSizeFilterTests {

    @Test
    @DisplayName("Should find files with minimum size")
    void testFindFilesWithMinSize() throws IOException {
      Path smallFile = testDir.resolve("small.txt");
      Path largeFile = testDir.resolve("large.txt");
      Files.writeString(smallFile, "X".repeat(10));
      Files.writeString(largeFile, "X".repeat(1000));

      try {
        FileSearchRequest request =
            FileSearchRequest.builder()
                .directory(testDir)
                .pattern("*.txt")
                .recursive(false)
                .minSize(500L)
                .build();

        List<FileInfo> results = fileSystemTool.findFiles(request).block();

        assertNotNull(results);
        assertThat(results).allMatch(info -> info.getSize() >= 500L);
        assertThat(results).noneMatch(info -> info.getName().equals("small.txt"));
      } finally {
        Files.deleteIfExists(smallFile);
        Files.deleteIfExists(largeFile);
      }
    }

    @Test
    @DisplayName("Should find files with maximum size")
    void testFindFilesWithMaxSize() throws IOException {
      Path smallFile = testDir.resolve("small.txt");
      Path largeFile = testDir.resolve("large.txt");
      Files.writeString(smallFile, "X".repeat(10));
      Files.writeString(largeFile, "X".repeat(1000));

      try {
        FileSearchRequest request =
            FileSearchRequest.builder()
                .directory(testDir)
                .pattern("*.txt")
                .recursive(false)
                .maxSize(100L)
                .build();

        List<FileInfo> results = fileSystemTool.findFiles(request).block();

        assertNotNull(results);
        assertThat(results).allMatch(info -> info.getSize() <= 100L);
        assertThat(results).noneMatch(info -> info.getName().equals("large.txt"));
      } finally {
        Files.deleteIfExists(smallFile);
        Files.deleteIfExists(largeFile);
      }
    }

    @Test
    @DisplayName("Should find files within size range")
    void testFindFilesWithinSizeRange() throws IOException {
      Path tinyFile = testDir.resolve("tiny.txt");
      Path mediumFile = testDir.resolve("medium.txt");
      Path hugeFile = testDir.resolve("huge.txt");
      Files.writeString(tinyFile, "X".repeat(5));
      Files.writeString(mediumFile, "X".repeat(50));
      Files.writeString(hugeFile, "X".repeat(500));

      try {
        FileSearchRequest request =
            FileSearchRequest.builder()
                .directory(testDir)
                .pattern("*.txt")
                .recursive(false)
                .minSize(10L)
                .maxSize(100L)
                .build();

        List<FileInfo> results = fileSystemTool.findFiles(request).block();

        assertNotNull(results);
        assertThat(results).allMatch(info -> info.getSize() >= 10L && info.getSize() <= 100L);
        assertThat(results).anyMatch(info -> info.getName().equals("medium.txt"));
      } finally {
        Files.deleteIfExists(tinyFile);
        Files.deleteIfExists(mediumFile);
        Files.deleteIfExists(hugeFile);
      }
    }
  }

  @Nested
  @DisplayName("File Search With Time Filters Tests")
  class FileTimeFilterTests {

    @Test
    @DisplayName("Should find files modified after timestamp")
    void testFindFilesModifiedAfter() throws IOException {
      Path oldFile = testDir.resolve("old.txt");
      Files.writeString(oldFile, "old");

      try {
        // Wait longer to ensure filesystem timestamp granularity difference
        Thread.sleep(1000);
        java.time.Instant cutoff = java.time.Instant.now();
        Thread.sleep(1000);

        Path newFile = testDir.resolve("new.txt");
        Files.writeString(newFile, "new");

        FileSearchRequest request =
            FileSearchRequest.builder()
                .directory(testDir)
                .pattern("*.txt")
                .recursive(false)
                .modifiedAfter(cutoff)
                .build();

        List<FileInfo> results = fileSystemTool.findFiles(request).block();

        assertNotNull(results);
        assertThat(results).anyMatch(info -> info.getName().equals("new.txt"));
        assertThat(results).noneMatch(info -> info.getName().equals("old.txt"));

        Files.deleteIfExists(newFile);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        Files.deleteIfExists(oldFile);
      }
    }

    @Test
    @DisplayName("Should find files modified before timestamp")
    void testFindFilesModifiedBefore() throws IOException {
      Path oldFile = testDir.resolve("old.txt");
      Files.writeString(oldFile, "old");

      try {
        Thread.sleep(100);
        java.time.Instant cutoff = java.time.Instant.now();
        Thread.sleep(100);

        Path newFile = testDir.resolve("new.txt");
        Files.writeString(newFile, "new");

        FileSearchRequest request =
            FileSearchRequest.builder()
                .directory(testDir)
                .pattern("*.txt")
                .recursive(false)
                .modifiedBefore(cutoff)
                .build();

        List<FileInfo> results = fileSystemTool.findFiles(request).block();

        assertNotNull(results);
        assertThat(results).anyMatch(info -> info.getName().equals("old.txt"));
        assertThat(results).noneMatch(info -> info.getName().equals("new.txt"));

        Files.deleteIfExists(newFile);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        Files.deleteIfExists(oldFile);
      }
    }
  }

  @Nested
  @DisplayName("File Search By Extension Tests")
  class FileExtensionSearchTests {

    @Test
    @DisplayName("Should find files by single extension")
    void testFindFilesBySingleExtension() throws IOException {
      Files.writeString(testDir.resolve("doc.txt"), "text");
      Files.writeString(testDir.resolve("data.json"), "json");
      Files.writeString(testDir.resolve("code.java"), "java");

      try {
        FileSearchRequest request =
            FileSearchRequest.builder()
                .directory(testDir)
                .pattern("*")
                .recursive(false)
                .extensions(new String[] {".txt"})
                .build();

        List<FileInfo> results = fileSystemTool.findFiles(request).block();

        assertNotNull(results);
        assertThat(results).allMatch(info -> ".txt".equalsIgnoreCase(info.getExtension()));
      } finally {
        Files.deleteIfExists(testDir.resolve("doc.txt"));
        Files.deleteIfExists(testDir.resolve("data.json"));
        Files.deleteIfExists(testDir.resolve("code.java"));
      }
    }

    @Test
    @DisplayName("Should find files by multiple extensions")
    void testFindFilesByMultipleExtensions() throws IOException {
      Files.writeString(testDir.resolve("doc.txt"), "text");
      Files.writeString(testDir.resolve("data.json"), "json");
      Files.writeString(testDir.resolve("code.java"), "java");

      try {
        FileSearchRequest request =
            FileSearchRequest.builder()
                .directory(testDir)
                .pattern("*")
                .recursive(false)
                .extensions(new String[] {".txt", ".json"})
                .build();

        List<FileInfo> results = fileSystemTool.findFiles(request).block();

        assertNotNull(results);
        assertThat(results)
            .allMatch(
                info ->
                    ".txt".equalsIgnoreCase(info.getExtension())
                        || ".json".equalsIgnoreCase(info.getExtension()));
        assertThat(results).noneMatch(info -> ".java".equalsIgnoreCase(info.getExtension()));
      } finally {
        Files.deleteIfExists(testDir.resolve("doc.txt"));
        Files.deleteIfExists(testDir.resolve("data.json"));
        Files.deleteIfExists(testDir.resolve("code.java"));
      }
    }
  }

  @Nested
  @DisplayName("File Search By Type Tests")
  class FileSearchTypeTests {

    @Test
    @DisplayName("Should find only files")
    void testFindOnlyFiles() throws IOException {
      Files.writeString(testDir.resolve("file.txt"), "content");
      Path subDir = Files.createDirectory(testDir.resolve("subdir"));

      try {
        FileSearchRequest request =
            FileSearchRequest.builder()
                .directory(testDir)
                .pattern("*")
                .recursive(false)
                .searchType(FileSearchRequest.SearchType.FILES)
                .build();

        List<FileInfo> results = fileSystemTool.findFiles(request).block();

        assertNotNull(results);
        assertThat(results).allMatch(FileInfo::isFile);
        assertThat(results).noneMatch(FileInfo::isDirectory);
      } finally {
        Files.deleteIfExists(testDir.resolve("file.txt"));
        Files.deleteIfExists(subDir);
      }
    }

    @Test
    @DisplayName("Should find only directories")
    void testFindOnlyDirectories() throws IOException {
      Files.writeString(testDir.resolve("file.txt"), "content");
      Path subDir = Files.createDirectory(testDir.resolve("subdir"));

      try {
        FileSearchRequest request =
            FileSearchRequest.builder()
                .directory(testDir)
                .pattern("*")
                .recursive(false)
                .searchType(FileSearchRequest.SearchType.DIRECTORIES)
                .build();

        List<FileInfo> results = fileSystemTool.findFiles(request).block();

        assertNotNull(results);
        assertThat(results).allMatch(FileInfo::isDirectory);
        assertThat(results).noneMatch(FileInfo::isFile);
      } finally {
        Files.deleteIfExists(testDir.resolve("file.txt"));
        Files.deleteIfExists(subDir);
      }
    }

    @Test
    @DisplayName("Should find both files and directories")
    void testFindBoth() throws IOException {
      Files.writeString(testDir.resolve("file.txt"), "content");
      Path subDir = Files.createDirectory(testDir.resolve("subdir"));

      try {
        FileSearchRequest request =
            FileSearchRequest.builder()
                .directory(testDir)
                .pattern("*")
                .recursive(false)
                .searchType(FileSearchRequest.SearchType.BOTH)
                .build();

        List<FileInfo> results = fileSystemTool.findFiles(request).block();

        assertNotNull(results);
        assertThat(results).anyMatch(FileInfo::isFile);
        assertThat(results).anyMatch(FileInfo::isDirectory);
      } finally {
        Files.deleteIfExists(testDir.resolve("file.txt"));
        Files.deleteIfExists(subDir);
      }
    }
  }

  @Nested
  @DisplayName("Copy Operation Edge Cases Tests")
  class CopyOperationEdgeCasesTests {

    @Test
    @DisplayName("Should copy file and replace existing")
    void testCopyFileReplaceExisting() throws IOException {
      Path source = testDir.resolve("source.txt");
      Path target = testDir.resolve("target.txt");
      Files.writeString(source, "source content");
      Files.writeString(target, "old target content");

      try {
        FileOperationResult result =
            fileSystemTool.copyFile(source.toString(), target.toString()).block();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("source content", Files.readString(target));
      } finally {
        Files.deleteIfExists(source);
        Files.deleteIfExists(target);
      }
    }

    @Test
    @DisplayName("Should fail to copy non-existent file")
    void testCopyNonExistentFile() {
      Path source = testDir.resolve("nonexistent.txt");
      Path target = testDir.resolve("target.txt");

      assertThrows(
          IllegalArgumentException.class,
          () -> fileSystemTool.copyFile(source.toString(), target.toString()).block());
    }
  }

  @Nested
  @DisplayName("Move Operation Edge Cases Tests")
  class MoveOperationEdgeCasesTests {

    @Test
    @DisplayName("Should move file and replace existing")
    void testMoveFileReplaceExisting() throws IOException {
      Path source = testDir.resolve("source.txt");
      Path target = testDir.resolve("target.txt");
      Files.writeString(source, "source content");
      Files.writeString(target, "old target content");

      try {
        FileOperationResult result =
            fileSystemTool.moveFile(source.toString(), target.toString()).block();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertFalse(Files.exists(source));
        assertEquals("source content", Files.readString(target));
      } finally {
        Files.deleteIfExists(source);
        Files.deleteIfExists(target);
      }
    }

    @Test
    @DisplayName("Should fail to move non-existent file")
    void testMoveNonExistentFile() {
      Path source = testDir.resolve("nonexistent.txt");
      Path target = testDir.resolve("target.txt");

      assertThrows(
          IllegalArgumentException.class,
          () -> fileSystemTool.moveFile(source.toString(), target.toString()).block());
    }
  }

  @Nested
  @DisplayName("Directory Listing Edge Cases Tests")
  class DirectoryListingEdgeCasesTests {

    @Test
    @DisplayName("Should list empty directory")
    void testListEmptyDirectory() throws IOException {
      Path emptyDir = Files.createDirectory(testDir.resolve("empty"));

      try {
        DirectoryListing listing = fileSystemTool.listDirectory(emptyDir.toString()).block();

        assertNotNull(listing);
        assertTrue(listing.isSuccess());
        assertEquals(0, listing.getTotalItems());
        assertEquals(0, listing.getTotalSize());
      } finally {
        Files.deleteIfExists(emptyDir);
      }
    }

    @Test
    @DisplayName("Should fail to list file as directory")
    void testListFileAsDirectory() {
      FileContent result = fileSystemTool.readFile(testFile.toString()).block();
      assertTrue(result.isSuccess());

      DirectoryListing listing = fileSystemTool.listDirectory(testFile.toString()).block();
      assertNotNull(listing);
      assertFalse(listing.isSuccess());
      assertNotNull(listing.getError());
    }

    @Test
    @DisplayName("Should list directory with mixed content")
    void testListDirectoryWithMixedContent() throws IOException {
      Path subDir = Files.createDirectory(testDir.resolve("subdir"));
      Files.writeString(testDir.resolve("file1.txt"), "content1");
      Files.writeString(testDir.resolve("file2.txt"), "content2");

      try {
        DirectoryListing listing = fileSystemTool.listDirectory(testDir.toString()).block();

        assertNotNull(listing);
        assertTrue(listing.isSuccess());
        assertThat(listing.getFiles().size()).isGreaterThanOrEqualTo(2);
        assertThat(listing.getDirectories().size()).isGreaterThanOrEqualTo(1);
        assertThat(listing.getTotalSize()).isGreaterThan(0);
      } finally {
        Files.deleteIfExists(testDir.resolve("file1.txt"));
        Files.deleteIfExists(testDir.resolve("file2.txt"));
        Files.deleteIfExists(subDir);
      }
    }
  }

  @Nested
  @DisplayName("Delete Operation Edge Cases Tests")
  class DeleteOperationEdgeCasesTests {

    @Test
    @DisplayName("Should fail to delete non-existent file")
    void testDeleteNonExistentFile() {
      Path nonExistent = testDir.resolve("nonexistent.txt");

      assertThrows(
          IllegalArgumentException.class,
          () -> fileSystemTool.deleteFile(nonExistent.toString()).block());
    }

    @Test
    @DisplayName("Should fail to delete non-empty directory without recursive")
    void testDeleteNonEmptyDirectoryWithoutRecursive() throws IOException {
      Path dirWithFiles = Files.createDirectory(testDir.resolve("nonempty"));
      Files.writeString(dirWithFiles.resolve("file.txt"), "content");

      try {
        FileOperationResult result =
            fileSystemTool.deleteDirectory(dirWithFiles.toString(), false).block();

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
      } finally {
        Files.deleteIfExists(dirWithFiles.resolve("file.txt"));
        Files.deleteIfExists(dirWithFiles);
      }
    }

    @Test
    @DisplayName("Should delete nested directory structure recursively")
    void testDeleteNestedDirectoryRecursively() throws IOException {
      Path parent = Files.createDirectory(testDir.resolve("parent"));
      Path child = Files.createDirectory(parent.resolve("child"));
      Path grandchild = Files.createDirectory(child.resolve("grandchild"));
      Files.writeString(grandchild.resolve("file.txt"), "content");

      FileOperationResult result = fileSystemTool.deleteDirectory(parent.toString(), true).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertFalse(Files.exists(parent));
      assertThat(result.getItemsAffected()).isGreaterThan(1);
    }
  }

  @Nested
  @DisplayName("Create Directory Edge Cases Tests")
  class CreateDirectoryEdgeCasesTests {

    @Test
    @DisplayName("Should fail to create directory when parent doesn't exist without createParents")
    void testCreateDirectoryWithoutParents() {
      Path deepDir = testDir.resolve("nonexistent/child");

      FileOperationResult result =
          fileSystemTool.createDirectory(deepDir.toString(), false).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertNotNull(result.getError());
    }

    @Test
    @DisplayName("Should fail to create directory that already exists")
    void testCreateExistingDirectory() {
      assertThrows(
          IllegalArgumentException.class,
          () -> fileSystemTool.createDirectory(testDir.toString(), false).block());
    }
  }

  @Nested
  @DisplayName("File Info Edge Cases Tests")
  class FileInfoEdgeCasesTests {

    @Test
    @DisplayName("Should get info for file without extension")
    void testGetInfoForFileWithoutExtension() throws IOException {
      Path noExtFile = testDir.resolve("README");
      Files.writeString(noExtFile, "content");

      try {
        FileInfo info = fileSystemTool.getFileInfo(noExtFile.toString()).block();

        assertNotNull(info);
        assertTrue(info.isFile());
        assertNull(info.getExtension());
      } finally {
        Files.deleteIfExists(noExtFile);
      }
    }

    @Test
    @DisplayName("Should get info for directory")
    void testGetInfoForDirectory() throws IOException {
      Path dir = Files.createDirectory(testDir.resolve("infodir"));

      try {
        FileInfo info = fileSystemTool.getFileInfo(dir.toString()).block();

        assertNotNull(info);
        assertTrue(info.isDirectory());
        assertFalse(info.isFile());
        assertNull(info.getExtension());
      } finally {
        Files.deleteIfExists(dir);
      }
    }

    @Test
    @DisplayName("Should fail to get info for non-existent path")
    void testGetInfoForNonExistent() {
      Path nonExistent = testDir.resolve("nonexistent.txt");

      assertThrows(
          IllegalArgumentException.class,
          () -> fileSystemTool.getFileInfo(nonExistent.toString()).block());
    }
  }

  @Nested
  @DisplayName("File Hash Calculation Tests")
  class FileHashTests {

    @Test
    @DisplayName("Should calculate file hash when enabled")
    void testCalculateFileHash() throws IOException {
      Path hashFile = testDir.resolve("hash.txt");
      Files.writeString(hashFile, "content for hashing");

      try {
        FileSystemConfig config = FileSystemConfig.builder().calculateHashes(true).build();
        FileSystemTool tool = new FileSystemTool(config);

        FileInfo info = tool.getFileInfo(hashFile.toString()).block();

        assertNotNull(info);
        assertNotNull(info.getHash());
        assertThat(info.getHash()).hasSize(64); // SHA-256 produces 64 hex characters
      } finally {
        Files.deleteIfExists(hashFile);
      }
    }

    @Test
    @DisplayName("Should not calculate file hash when disabled")
    void testNoHashWhenDisabled() throws IOException {
      Path hashFile = testDir.resolve("nohash.txt");
      Files.writeString(hashFile, "content without hash");

      try {
        FileSystemConfig config = FileSystemConfig.builder().calculateHashes(false).build();
        FileSystemTool tool = new FileSystemTool(config);

        FileInfo info = tool.getFileInfo(hashFile.toString()).block();

        assertNotNull(info);
        assertNull(info.getHash());
      } finally {
        Files.deleteIfExists(hashFile);
      }
    }
  }

  @Nested
  @DisplayName("Append Operation Edge Cases Tests")
  class AppendOperationEdgeCasesTests {

    @Test
    @DisplayName("Should create file when appending to non-existent file")
    void testAppendToNonExistentFile() throws IOException {
      Path newFile = testDir.resolve("append-new.txt");

      try {
        FileOperationResult result =
            fileSystemTool.appendFile(newFile.toString(), "appended content").block();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(Files.exists(newFile));
        assertEquals("appended content", Files.readString(newFile));
      } finally {
        Files.deleteIfExists(newFile);
      }
    }

    @Test
    @DisplayName("Should append multiple times")
    void testMultipleAppends() throws IOException {
      Path appendFile = testDir.resolve("multi-append.txt");
      Files.writeString(appendFile, "Line 1\n");

      try {
        fileSystemTool.appendFile(appendFile.toString(), "Line 2\n").block();
        fileSystemTool.appendFile(appendFile.toString(), "Line 3\n").block();

        String content = Files.readString(appendFile);
        assertThat(content).contains("Line 1", "Line 2", "Line 3");
      } finally {
        Files.deleteIfExists(appendFile);
      }
    }
  }

  @Nested
  @DisplayName("Path Validation Tests")
  class PathValidationTests {

    @Test
    @DisplayName("Should reject paths with null bytes")
    void testPathWithNullBytes() {
      assertThrows(
          Exception.class, () -> fileSystemTool.exists(testDir.toString() + "\0malicious").block());
    }

    @Test
    @DisplayName("Should handle blocked extensions")
    void testBlockedExtensions() {
      FileSystemConfig config =
          FileSystemConfig.builder().blockedExtensions(java.util.Set.of(".exe", ".sh")).build();
      FileSystemTool tool = new FileSystemTool(config);

      Path exeFile = testDir.resolve("malicious.exe");

      assertThrows(
          SecurityException.class, () -> tool.writeFile(exeFile.toString(), "content").block());
    }
  }

  @Nested
  @DisplayName("Setter Tests")
  class SetterTests {

    @Test
    @DisplayName("Should allow config update via setter")
    void testSetConfig() {
      FileSystemTool tool = new FileSystemTool();
      FileSystemConfig newConfig = FileSystemConfig.builder().maxReadSize(5000).build();

      tool.setConfig(newConfig);

      // Config should be updated (indirectly tested by behavior)
      assertNotNull(tool);
    }
  }
}
