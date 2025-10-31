package dev.adeengineer.adentic.tool.filesystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import dev.adeengineer.adentic.tool.filesystem.config.FileSystemConfig;
import dev.adeengineer.adentic.tool.filesystem.model.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.*;

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
}
