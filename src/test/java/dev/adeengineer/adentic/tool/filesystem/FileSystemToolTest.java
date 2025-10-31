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
}
