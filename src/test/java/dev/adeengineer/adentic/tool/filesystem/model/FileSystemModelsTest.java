package dev.adeengineer.adentic.tool.filesystem.model;

import static org.junit.jupiter.api.Assertions.*;

import dev.adeengineer.adentic.tool.filesystem.model.FileOperationResult.OperationType;
import dev.adeengineer.adentic.tool.filesystem.model.FileSearchRequest.SearchType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Comprehensive tests for filesystem model classes. */
@DisplayName("FileSystem Models Tests")
class FileSystemModelsTest {

  @Nested
  @DisplayName("FileSearchRequest Tests")
  class FileSearchRequestTests {

    @Test
    @DisplayName("Should create request with builder")
    void testBuilder() {
      Path directory = Paths.get("/home/user/test");
      String[] extensions = {".java", ".xml"};
      Instant modifiedAfter = Instant.parse("2025-01-01T00:00:00Z");
      Instant modifiedBefore = Instant.parse("2025-12-31T23:59:59Z");

      FileSearchRequest request =
          FileSearchRequest.builder()
              .directory(directory)
              .pattern("*.java")
              .contentPattern("public class.*")
              .recursive(true)
              .maxDepth(5)
              .includeHidden(true)
              .minSize(1024L)
              .maxSize(1048576L)
              .modifiedAfter(modifiedAfter)
              .modifiedBefore(modifiedBefore)
              .extensions(extensions)
              .maxResults(500)
              .searchType(SearchType.FILES)
              .build();

      assertEquals(directory, request.getDirectory());
      assertEquals("*.java", request.getPattern());
      assertEquals("public class.*", request.getContentPattern());
      assertTrue(request.isRecursive());
      assertEquals(5, request.getMaxDepth());
      assertTrue(request.isIncludeHidden());
      assertEquals(1024L, request.getMinSize());
      assertEquals(1048576L, request.getMaxSize());
      assertEquals(modifiedAfter, request.getModifiedAfter());
      assertEquals(modifiedBefore, request.getModifiedBefore());
      assertArrayEquals(extensions, request.getExtensions());
      assertEquals(500, request.getMaxResults());
      assertEquals(SearchType.FILES, request.getSearchType());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      FileSearchRequest request =
          FileSearchRequest.builder().directory(Paths.get("/tmp")).pattern("*.txt").build();

      assertNull(request.getContentPattern());
      assertFalse(request.isRecursive());
      assertEquals(-1, request.getMaxDepth());
      assertFalse(request.isIncludeHidden());
      assertNull(request.getMinSize());
      assertNull(request.getMaxSize());
      assertNull(request.getModifiedAfter());
      assertNull(request.getModifiedBefore());
      assertNull(request.getExtensions());
      assertEquals(1000, request.getMaxResults());
      assertEquals(SearchType.FILES, request.getSearchType());
    }

    @Test
    @DisplayName("Should support recursive search")
    void testRecursiveSearch() {
      FileSearchRequest request =
          FileSearchRequest.builder()
              .directory(Paths.get("/home"))
              .pattern("*.log")
              .recursive(true)
              .maxDepth(3)
              .build();

      assertTrue(request.isRecursive());
      assertEquals(3, request.getMaxDepth());
    }

    @Test
    @DisplayName("Should support file size filtering")
    void testSizeFiltering() {
      FileSearchRequest request =
          FileSearchRequest.builder()
              .directory(Paths.get("/var/log"))
              .minSize(1024L)
              .maxSize(1048576L)
              .build();

      assertEquals(1024L, request.getMinSize());
      assertEquals(1048576L, request.getMaxSize());
    }

    @Test
    @DisplayName("Should support date filtering")
    void testDateFiltering() {
      Instant after = Instant.parse("2025-01-01T00:00:00Z");
      Instant before = Instant.parse("2025-12-31T23:59:59Z");

      FileSearchRequest request =
          FileSearchRequest.builder()
              .directory(Paths.get("/tmp"))
              .modifiedAfter(after)
              .modifiedBefore(before)
              .build();

      assertEquals(after, request.getModifiedAfter());
      assertEquals(before, request.getModifiedBefore());
    }

    @Test
    @DisplayName("Should support extension filtering")
    void testExtensionFiltering() {
      String[] extensions = {".java", ".xml", ".properties"};

      FileSearchRequest request =
          FileSearchRequest.builder().directory(Paths.get("/src")).extensions(extensions).build();

      assertArrayEquals(extensions, request.getExtensions());
      assertEquals(3, request.getExtensions().length);
    }

    @Test
    @DisplayName("Should support different search types")
    void testSearchTypes() {
      FileSearchRequest filesOnly =
          FileSearchRequest.builder()
              .directory(Paths.get("/tmp"))
              .searchType(SearchType.FILES)
              .build();

      FileSearchRequest dirsOnly =
          FileSearchRequest.builder()
              .directory(Paths.get("/tmp"))
              .searchType(SearchType.DIRECTORIES)
              .build();

      FileSearchRequest both =
          FileSearchRequest.builder()
              .directory(Paths.get("/tmp"))
              .searchType(SearchType.BOTH)
              .build();

      assertEquals(SearchType.FILES, filesOnly.getSearchType());
      assertEquals(SearchType.DIRECTORIES, dirsOnly.getSearchType());
      assertEquals(SearchType.BOTH, both.getSearchType());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      FileSearchRequest request = FileSearchRequest.builder().directory(Paths.get("/tmp")).build();

      request.setPattern("*.json");
      request.setRecursive(true);
      request.setMaxDepth(10);
      request.setMaxResults(100);

      assertEquals("*.json", request.getPattern());
      assertTrue(request.isRecursive());
      assertEquals(10, request.getMaxDepth());
      assertEquals(100, request.getMaxResults());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      FileSearchRequest request1 =
          FileSearchRequest.builder()
              .directory(Paths.get("/tmp"))
              .pattern("*.txt")
              .recursive(true)
              .build();

      FileSearchRequest request2 =
          FileSearchRequest.builder()
              .directory(Paths.get("/tmp"))
              .pattern("*.txt")
              .recursive(true)
              .build();

      assertEquals(request1, request2);
      assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      FileSearchRequest request =
          FileSearchRequest.builder()
              .directory(Paths.get("/tmp"))
              .pattern("*.txt")
              .recursive(true)
              .build();

      String str = request.toString();
      assertTrue(str.contains("/tmp"));
      assertTrue(str.contains("*.txt"));
      assertTrue(str.contains("true"));
    }
  }

  @Nested
  @DisplayName("SearchType Tests")
  class SearchTypeTests {

    @Test
    @DisplayName("Should have all search types")
    void testSearchTypes() {
      SearchType[] types = SearchType.values();
      assertEquals(3, types.length);

      assertNotNull(SearchType.valueOf("FILES"));
      assertNotNull(SearchType.valueOf("DIRECTORIES"));
      assertNotNull(SearchType.valueOf("BOTH"));
    }

    @Test
    @DisplayName("Should throw exception for invalid type")
    void testInvalidType() {
      assertThrows(IllegalArgumentException.class, () -> SearchType.valueOf("INVALID"));
    }

    @Test
    @DisplayName("Should return correct name")
    void testTypeName() {
      assertEquals("FILES", SearchType.FILES.name());
      assertEquals("DIRECTORIES", SearchType.DIRECTORIES.name());
      assertEquals("BOTH", SearchType.BOTH.name());
    }
  }

  @Nested
  @DisplayName("FileOperationResult Tests")
  class FileOperationResultTests {

    @Test
    @DisplayName("Should create result with builder")
    void testBuilder() {
      Path source = Paths.get("/tmp/source.txt");
      Path target = Paths.get("/tmp/target.txt");

      FileOperationResult result =
          FileOperationResult.builder()
              .operation(OperationType.COPY)
              .source(source)
              .target(target)
              .success(true)
              .error(null)
              .itemsAffected(5)
              .details("Copied successfully")
              .build();

      assertEquals(OperationType.COPY, result.getOperation());
      assertEquals(source, result.getSource());
      assertEquals(target, result.getTarget());
      assertTrue(result.isSuccess());
      assertNull(result.getError());
      assertEquals(5, result.getItemsAffected());
      assertEquals("Copied successfully", result.getDetails());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      FileOperationResult result =
          FileOperationResult.builder()
              .operation(OperationType.READ)
              .source(Paths.get("/tmp/file.txt"))
              .build();

      assertTrue(result.isSuccess());
      assertNull(result.getError());
      assertEquals(1, result.getItemsAffected());
      assertNull(result.getDetails());
    }

    @Test
    @DisplayName("Should create success result for read operation")
    void testReadSuccess() {
      FileOperationResult result =
          FileOperationResult.builder()
              .operation(OperationType.READ)
              .source(Paths.get("/tmp/file.txt"))
              .success(true)
              .build();

      assertEquals(OperationType.READ, result.getOperation());
      assertTrue(result.isSuccess());
      assertTrue(result.toString().contains("Read file"));
    }

    @Test
    @DisplayName("Should create success result for write operation")
    void testWriteSuccess() {
      FileOperationResult result =
          FileOperationResult.builder()
              .operation(OperationType.WRITE)
              .source(Paths.get("/tmp/output.txt"))
              .success(true)
              .build();

      assertEquals(OperationType.WRITE, result.getOperation());
      assertTrue(result.isSuccess());
      assertTrue(result.toString().contains("Wrote file"));
    }

    @Test
    @DisplayName("Should create success result for copy operation")
    void testCopySuccess() {
      FileOperationResult result =
          FileOperationResult.builder()
              .operation(OperationType.COPY)
              .source(Paths.get("/tmp/source.txt"))
              .target(Paths.get("/tmp/dest.txt"))
              .success(true)
              .build();

      assertEquals(OperationType.COPY, result.getOperation());
      assertTrue(result.isSuccess());
      String str = result.toString();
      assertTrue(str.contains("Copied"));
      assertTrue(str.contains("source.txt"));
      assertTrue(str.contains("dest.txt"));
    }

    @Test
    @DisplayName("Should create success result for move operation")
    void testMoveSuccess() {
      FileOperationResult result =
          FileOperationResult.builder()
              .operation(OperationType.MOVE)
              .source(Paths.get("/tmp/old.txt"))
              .target(Paths.get("/tmp/new.txt"))
              .success(true)
              .build();

      assertEquals(OperationType.MOVE, result.getOperation());
      assertTrue(result.isSuccess());
      assertTrue(result.toString().contains("Moved"));
    }

    @Test
    @DisplayName("Should create success result for delete operation")
    void testDeleteSuccess() {
      FileOperationResult result =
          FileOperationResult.builder()
              .operation(OperationType.DELETE)
              .source(Paths.get("/tmp/file.txt"))
              .success(true)
              .build();

      assertEquals(OperationType.DELETE, result.getOperation());
      assertTrue(result.isSuccess());
      assertTrue(result.toString().contains("Deleted file"));
    }

    @Test
    @DisplayName("Should create success result for directory operations")
    void testDirectoryOperations() {
      FileOperationResult createDir =
          FileOperationResult.builder()
              .operation(OperationType.CREATE_DIR)
              .source(Paths.get("/tmp/newdir"))
              .success(true)
              .build();

      FileOperationResult deleteDir =
          FileOperationResult.builder()
              .operation(OperationType.DELETE_DIR)
              .source(Paths.get("/tmp/olddir"))
              .itemsAffected(10)
              .success(true)
              .build();

      assertTrue(createDir.toString().contains("Created directory"));
      assertTrue(deleteDir.toString().contains("Deleted directory"));
      assertTrue(deleteDir.toString().contains("10 items"));
    }

    @Test
    @DisplayName("Should create success result for list operation")
    void testListSuccess() {
      FileOperationResult result =
          FileOperationResult.builder()
              .operation(OperationType.LIST)
              .source(Paths.get("/tmp"))
              .itemsAffected(25)
              .success(true)
              .build();

      assertEquals(OperationType.LIST, result.getOperation());
      assertTrue(result.isSuccess());
      assertEquals(25, result.getItemsAffected());
      assertTrue(result.toString().contains("Listed directory"));
      assertTrue(result.toString().contains("25 items"));
    }

    @Test
    @DisplayName("Should create success result for search operation")
    void testSearchSuccess() {
      FileOperationResult result =
          FileOperationResult.builder()
              .operation(OperationType.SEARCH)
              .itemsAffected(42)
              .success(true)
              .build();

      assertEquals(OperationType.SEARCH, result.getOperation());
      assertTrue(result.isSuccess());
      assertEquals(42, result.getItemsAffected());
      assertTrue(result.toString().contains("Search found 42 items"));
    }

    @Test
    @DisplayName("Should create error result")
    void testErrorResult() {
      FileOperationResult result =
          FileOperationResult.builder()
              .operation(OperationType.READ)
              .source(Paths.get("/tmp/missing.txt"))
              .success(false)
              .error("File not found")
              .build();

      assertEquals(OperationType.READ, result.getOperation());
      assertFalse(result.isSuccess());
      assertEquals("File not found", result.getError());
      String str = result.toString();
      assertTrue(str.contains("failed"));
      assertTrue(str.contains("File not found"));
    }

    @Test
    @DisplayName("Should include details in toString")
    void testToStringWithDetails() {
      FileOperationResult result =
          FileOperationResult.builder()
              .operation(OperationType.COPY)
              .source(Paths.get("/tmp/source.txt"))
              .target(Paths.get("/tmp/dest.txt"))
              .success(true)
              .details("1024 bytes copied in 5ms")
              .build();

      String str = result.toString();
      assertTrue(str.contains("1024 bytes copied in 5ms"));
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      FileOperationResult result =
          FileOperationResult.builder().operation(OperationType.READ).build();

      result.setOperation(OperationType.WRITE);
      result.setSource(Paths.get("/tmp/file.txt"));
      result.setSuccess(false);
      result.setError("Permission denied");
      result.setItemsAffected(0);

      assertEquals(OperationType.WRITE, result.getOperation());
      assertEquals(Paths.get("/tmp/file.txt"), result.getSource());
      assertFalse(result.isSuccess());
      assertEquals("Permission denied", result.getError());
      assertEquals(0, result.getItemsAffected());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      FileOperationResult result1 =
          FileOperationResult.builder()
              .operation(OperationType.READ)
              .source(Paths.get("/tmp/file.txt"))
              .success(true)
              .build();

      FileOperationResult result2 =
          FileOperationResult.builder()
              .operation(OperationType.READ)
              .source(Paths.get("/tmp/file.txt"))
              .success(true)
              .build();

      assertEquals(result1, result2);
      assertEquals(result1.hashCode(), result2.hashCode());
    }
  }

  @Nested
  @DisplayName("OperationType Tests")
  class OperationTypeTests {

    @Test
    @DisplayName("Should have all operation types")
    void testOperationTypes() {
      OperationType[] types = OperationType.values();
      assertEquals(10, types.length);

      assertNotNull(OperationType.valueOf("READ"));
      assertNotNull(OperationType.valueOf("WRITE"));
      assertNotNull(OperationType.valueOf("APPEND"));
      assertNotNull(OperationType.valueOf("DELETE"));
      assertNotNull(OperationType.valueOf("COPY"));
      assertNotNull(OperationType.valueOf("MOVE"));
      assertNotNull(OperationType.valueOf("CREATE_DIR"));
      assertNotNull(OperationType.valueOf("DELETE_DIR"));
      assertNotNull(OperationType.valueOf("LIST"));
      assertNotNull(OperationType.valueOf("SEARCH"));
    }

    @Test
    @DisplayName("Should throw exception for invalid type")
    void testInvalidType() {
      assertThrows(IllegalArgumentException.class, () -> OperationType.valueOf("INVALID"));
    }

    @Test
    @DisplayName("Should return correct name")
    void testTypeName() {
      assertEquals("READ", OperationType.READ.name());
      assertEquals("WRITE", OperationType.WRITE.name());
      assertEquals("SEARCH", OperationType.SEARCH.name());
    }
  }

  @Nested
  @DisplayName("FileInfo Tests")
  class FileInfoTests {

    @Test
    @DisplayName("Should create file info with builder")
    void testBuilder() {
      Path path = Paths.get("/home/user/document.pdf");
      Instant now = Instant.now();
      Instant created = now.minusSeconds(86400);
      Set<PosixFilePermission> permissions =
          Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);

      FileInfo info =
          FileInfo.builder()
              .path(path)
              .name("document.pdf")
              .extension(".pdf")
              .size(1048576L)
              .directory(false)
              .file(true)
              .symlink(false)
              .readable(true)
              .writable(true)
              .executable(false)
              .hidden(false)
              .lastModified(now)
              .created(created)
              .lastAccessed(now)
              .owner("user")
              .permissions(permissions)
              .mimeType("application/pdf")
              .hash("abc123def456")
              .build();

      assertEquals(path, info.getPath());
      assertEquals("document.pdf", info.getName());
      assertEquals(".pdf", info.getExtension());
      assertEquals(1048576L, info.getSize());
      assertFalse(info.isDirectory());
      assertTrue(info.isFile());
      assertFalse(info.isSymlink());
      assertTrue(info.isReadable());
      assertTrue(info.isWritable());
      assertFalse(info.isExecutable());
      assertFalse(info.isHidden());
      assertEquals(now, info.getLastModified());
      assertEquals(created, info.getCreated());
      assertEquals(now, info.getLastAccessed());
      assertEquals("user", info.getOwner());
      assertEquals(permissions, info.getPermissions());
      assertEquals("application/pdf", info.getMimeType());
      assertEquals("abc123def456", info.getHash());
    }

    @Test
    @DisplayName("Should create file info for directory")
    void testDirectoryInfo() {
      FileInfo info =
          FileInfo.builder()
              .path(Paths.get("/home/user/documents"))
              .name("documents")
              .directory(true)
              .file(false)
              .readable(true)
              .writable(true)
              .executable(true)
              .lastModified(Instant.now())
              .build();

      assertTrue(info.isDirectory());
      assertFalse(info.isFile());
      assertNull(info.getExtension());
      String str = info.toString();
      assertTrue(str.contains("[DIR]"));
      assertTrue(str.contains("documents"));
    }

    @Test
    @DisplayName("Should create file info for regular file")
    void testRegularFileInfo() {
      FileInfo info =
          FileInfo.builder()
              .path(Paths.get("/tmp/test.txt"))
              .name("test.txt")
              .extension(".txt")
              .size(2048L)
              .directory(false)
              .file(true)
              .readable(true)
              .writable(true)
              .executable(false)
              .lastModified(Instant.now())
              .build();

      assertFalse(info.isDirectory());
      assertTrue(info.isFile());
      assertEquals(".txt", info.getExtension());
      assertEquals(2048L, info.getSize());
      String str = info.toString();
      assertTrue(str.contains("[FILE]"));
      assertTrue(str.contains("test.txt"));
      assertTrue(str.contains("KB")); // Size formatting
    }

    @Test
    @DisplayName("Should format file sizes correctly")
    void testSizeFormatting() {
      FileInfo bytes =
          FileInfo.builder()
              .name("tiny.txt")
              .size(512L)
              .lastModified(Instant.now())
              .directory(false)
              .readable(true)
              .writable(true)
              .executable(false)
              .build();

      FileInfo kb =
          FileInfo.builder()
              .name("small.txt")
              .size(2048L)
              .lastModified(Instant.now())
              .directory(false)
              .readable(true)
              .writable(true)
              .executable(false)
              .build();

      FileInfo mb =
          FileInfo.builder()
              .name("medium.dat")
              .size(5242880L)
              .lastModified(Instant.now())
              .directory(false)
              .readable(true)
              .writable(true)
              .executable(false)
              .build();

      FileInfo gb =
          FileInfo.builder()
              .name("large.iso")
              .size(2147483648L)
              .lastModified(Instant.now())
              .directory(false)
              .readable(true)
              .writable(true)
              .executable(false)
              .build();

      assertTrue(bytes.toString().contains("B"));
      assertTrue(kb.toString().contains("KB"));
      assertTrue(mb.toString().contains("MB"));
      assertTrue(gb.toString().contains("GB"));
    }

    @Test
    @DisplayName("Should show permissions in toString")
    void testPermissionsFormatting() {
      FileInfo readable =
          FileInfo.builder()
              .name("readonly.txt")
              .readable(true)
              .writable(false)
              .executable(false)
              .lastModified(Instant.now())
              .directory(false)
              .build();

      FileInfo executable =
          FileInfo.builder()
              .name("script.sh")
              .readable(true)
              .writable(true)
              .executable(true)
              .lastModified(Instant.now())
              .directory(false)
              .build();

      String readableStr = readable.toString();
      String executableStr = executable.toString();

      assertTrue(readableStr.contains("r-")); // readable, not writable
      assertTrue(executableStr.contains("rwx")); // all permissions
    }

    @Test
    @DisplayName("Should create symlink info")
    void testSymlinkInfo() {
      FileInfo info =
          FileInfo.builder()
              .path(Paths.get("/tmp/link"))
              .name("link")
              .symlink(true)
              .file(false)
              .directory(false)
              .readable(true)
              .lastModified(Instant.now())
              .build();

      assertTrue(info.isSymlink());
      assertFalse(info.isFile());
      assertFalse(info.isDirectory());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      FileInfo info = FileInfo.builder().name("test.txt").build();

      info.setSize(4096L);
      info.setReadable(true);
      info.setWritable(false);
      info.setExecutable(false);
      info.setHidden(true);

      assertEquals(4096L, info.getSize());
      assertTrue(info.isReadable());
      assertFalse(info.isWritable());
      assertFalse(info.isExecutable());
      assertTrue(info.isHidden());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      Instant now = Instant.now();

      FileInfo info1 =
          FileInfo.builder()
              .path(Paths.get("/tmp/test.txt"))
              .name("test.txt")
              .size(1024L)
              .lastModified(now)
              .build();

      FileInfo info2 =
          FileInfo.builder()
              .path(Paths.get("/tmp/test.txt"))
              .name("test.txt")
              .size(1024L)
              .lastModified(now)
              .build();

      assertEquals(info1, info2);
      assertEquals(info1.hashCode(), info2.hashCode());
    }
  }

  @Nested
  @DisplayName("FileContent Tests")
  class FileContentTests {

    @Test
    @DisplayName("Should create file content with builder")
    void testBuilder() {
      Path path = Paths.get("/tmp/test.txt");
      Instant modified = Instant.now();
      byte[] bytes = "Hello World".getBytes();

      FileContent content =
          FileContent.builder()
              .path(path)
              .content("Hello World")
              .bytes(bytes)
              .size(11L)
              .lastModified(modified)
              .extension(".txt")
              .binary(false)
              .encoding("UTF-8")
              .success(true)
              .error(null)
              .build();

      assertEquals(path, content.getPath());
      assertEquals("Hello World", content.getContent());
      assertArrayEquals(bytes, content.getBytes());
      assertEquals(11L, content.getSize());
      assertEquals(modified, content.getLastModified());
      assertEquals(".txt", content.getExtension());
      assertFalse(content.isBinary());
      assertEquals("UTF-8", content.getEncoding());
      assertTrue(content.isSuccess());
      assertNull(content.getError());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      FileContent content =
          FileContent.builder().path(Paths.get("/tmp/test.txt")).content("test").build();

      assertFalse(content.isBinary());
      assertEquals("UTF-8", content.getEncoding());
      assertTrue(content.isSuccess());
      assertNull(content.getError());
    }

    @Test
    @DisplayName("Should create text file content")
    void testTextContent() {
      FileContent content =
          FileContent.builder()
              .path(Paths.get("/tmp/document.txt"))
              .content("This is a text file")
              .size(19L)
              .lastModified(Instant.now())
              .extension(".txt")
              .binary(false)
              .encoding("UTF-8")
              .success(true)
              .build();

      assertEquals("This is a text file", content.getContent());
      assertFalse(content.isBinary());
      assertEquals("UTF-8", content.getEncoding());
      assertTrue(content.isSuccess());
    }

    @Test
    @DisplayName("Should create binary file content")
    void testBinaryContent() {
      byte[] data = {0x00, 0x01, 0x02, 0x03, (byte) 0xFF};

      FileContent content =
          FileContent.builder()
              .path(Paths.get("/tmp/data.bin"))
              .bytes(data)
              .size(5L)
              .lastModified(Instant.now())
              .extension(".bin")
              .binary(true)
              .success(true)
              .build();

      assertArrayEquals(data, content.getBytes());
      assertTrue(content.isBinary());
      assertTrue(content.isSuccess());
      assertNull(content.getContent());
    }

    @Test
    @DisplayName("Should create success result toString")
    void testSuccessToString() {
      FileContent content =
          FileContent.builder()
              .path(Paths.get("/tmp/test.txt"))
              .content("Hello")
              .size(5L)
              .lastModified(Instant.parse("2025-01-15T10:30:00Z"))
              .binary(false)
              .success(true)
              .build();

      String str = content.toString();
      assertTrue(str.contains("test.txt"));
      assertTrue(str.contains("5 bytes"));
      assertTrue(str.contains("2025-01-15"));
      assertTrue(str.contains("no")); // not binary
    }

    @Test
    @DisplayName("Should create error result toString")
    void testErrorToString() {
      FileContent content =
          FileContent.builder()
              .path(Paths.get("/tmp/missing.txt"))
              .success(false)
              .error("File not found")
              .build();

      String str = content.toString();
      assertTrue(str.contains("Failed to read"));
      assertTrue(str.contains("missing.txt"));
      assertTrue(str.contains("File not found"));
    }

    @Test
    @DisplayName("Should handle different encodings")
    void testEncodings() {
      FileContent utf8 =
          FileContent.builder()
              .path(Paths.get("/tmp/utf8.txt"))
              .content("Hello")
              .encoding("UTF-8")
              .build();

      FileContent ascii =
          FileContent.builder()
              .path(Paths.get("/tmp/ascii.txt"))
              .content("Hello")
              .encoding("ASCII")
              .build();

      FileContent utf16 =
          FileContent.builder()
              .path(Paths.get("/tmp/utf16.txt"))
              .content("Hello")
              .encoding("UTF-16")
              .build();

      assertEquals("UTF-8", utf8.getEncoding());
      assertEquals("ASCII", ascii.getEncoding());
      assertEquals("UTF-16", utf16.getEncoding());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      FileContent content =
          FileContent.builder().path(Paths.get("/tmp/test.txt")).content("test").build();

      content.setContent("updated");
      content.setSize(7L);
      content.setBinary(true);
      content.setEncoding("ASCII");
      content.setSuccess(false);
      content.setError("Read error");

      assertEquals("updated", content.getContent());
      assertEquals(7L, content.getSize());
      assertTrue(content.isBinary());
      assertEquals("ASCII", content.getEncoding());
      assertFalse(content.isSuccess());
      assertEquals("Read error", content.getError());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      Instant now = Instant.now();

      FileContent content1 =
          FileContent.builder()
              .path(Paths.get("/tmp/test.txt"))
              .content("Hello")
              .size(5L)
              .lastModified(now)
              .build();

      FileContent content2 =
          FileContent.builder()
              .path(Paths.get("/tmp/test.txt"))
              .content("Hello")
              .size(5L)
              .lastModified(now)
              .build();

      assertEquals(content1, content2);
      assertEquals(content1.hashCode(), content2.hashCode());
    }
  }

  @Nested
  @DisplayName("DirectoryListing Tests")
  class DirectoryListingTests {

    @Test
    @DisplayName("Should create directory listing with builder")
    void testBuilder() {
      Path path = Paths.get("/home/user/documents");
      List<FileInfo> files = new ArrayList<>();
      files.add(FileInfo.builder().name("file1.txt").size(1024L).build());
      files.add(FileInfo.builder().name("file2.pdf").size(2048L).build());

      List<FileInfo> directories = new ArrayList<>();
      directories.add(FileInfo.builder().name("subdir1").directory(true).build());
      directories.add(FileInfo.builder().name("subdir2").directory(true).build());

      DirectoryListing listing =
          DirectoryListing.builder()
              .path(path)
              .files(files)
              .directories(directories)
              .totalItems(4)
              .totalSize(3072L)
              .success(true)
              .error(null)
              .build();

      assertEquals(path, listing.getPath());
      assertEquals(files, listing.getFiles());
      assertEquals(directories, listing.getDirectories());
      assertEquals(4, listing.getTotalItems());
      assertEquals(3072L, listing.getTotalSize());
      assertTrue(listing.isSuccess());
      assertNull(listing.getError());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      DirectoryListing listing = DirectoryListing.builder().path(Paths.get("/tmp")).build();

      assertNotNull(listing.getFiles());
      assertNotNull(listing.getDirectories());
      assertTrue(listing.getFiles().isEmpty());
      assertTrue(listing.getDirectories().isEmpty());
      assertTrue(listing.isSuccess());
      assertNull(listing.getError());
    }

    @Test
    @DisplayName("Should get all items")
    void testGetAllItems() {
      List<FileInfo> files = new ArrayList<>();
      files.add(FileInfo.builder().name("file1.txt").build());
      files.add(FileInfo.builder().name("file2.txt").build());

      List<FileInfo> directories = new ArrayList<>();
      directories.add(FileInfo.builder().name("dir1").directory(true).build());

      DirectoryListing listing =
          DirectoryListing.builder()
              .path(Paths.get("/tmp"))
              .files(files)
              .directories(directories)
              .build();

      List<FileInfo> allItems = listing.getAllItems();
      assertEquals(3, allItems.size());
      // Directories come first
      assertEquals("dir1", allItems.get(0).getName());
      assertEquals("file1.txt", allItems.get(1).getName());
      assertEquals("file2.txt", allItems.get(2).getName());
    }

    @Test
    @DisplayName("Should get summary")
    void testGetSummary() {
      List<FileInfo> files = new ArrayList<>();
      files.add(FileInfo.builder().name("file1.txt").size(1024L).build());
      files.add(FileInfo.builder().name("file2.txt").size(2048L).build());

      List<FileInfo> directories = new ArrayList<>();
      directories.add(FileInfo.builder().name("subdir").directory(true).build());

      DirectoryListing listing =
          DirectoryListing.builder()
              .path(Paths.get("/home/user/docs"))
              .files(files)
              .directories(directories)
              .totalSize(3072L)
              .success(true)
              .build();

      String summary = listing.getSummary();
      assertTrue(summary.contains("docs"));
      assertTrue(summary.contains("1 directories"));
      assertTrue(summary.contains("2 files"));
      assertTrue(summary.contains("KB")); // Size formatting
    }

    @Test
    @DisplayName("Should get summary for error")
    void testGetSummaryError() {
      DirectoryListing listing =
          DirectoryListing.builder()
              .path(Paths.get("/restricted"))
              .success(false)
              .error("Permission denied")
              .build();

      String summary = listing.getSummary();
      assertTrue(summary.contains("Failed to list"));
      assertTrue(summary.contains("/restricted"));
      assertTrue(summary.contains("Permission denied"));
    }

    @Test
    @DisplayName("Should get detailed report")
    void testGetDetailedReport() {
      List<FileInfo> files = new ArrayList<>();
      files.add(FileInfo.builder().name("readme.txt").size(512L).build());
      files.add(FileInfo.builder().name("data.json").size(1024L).build());

      List<FileInfo> directories = new ArrayList<>();
      directories.add(FileInfo.builder().name("src").directory(true).build());
      directories.add(FileInfo.builder().name("test").directory(true).build());

      DirectoryListing listing =
          DirectoryListing.builder()
              .path(Paths.get("/project"))
              .files(files)
              .directories(directories)
              .totalSize(1536L)
              .success(true)
              .build();

      String report = listing.getDetailedReport();
      assertTrue(report.contains("project"));
      assertTrue(report.contains("Directories:"));
      assertTrue(report.contains("src"));
      assertTrue(report.contains("test"));
      assertTrue(report.contains("Files:"));
      assertTrue(report.contains("readme.txt"));
      assertTrue(report.contains("data.json"));
      assertTrue(report.contains("512 B"));
      assertTrue(report.contains("1.00 KB"));
    }

    @Test
    @DisplayName("Should get detailed report for empty directory")
    void testGetDetailedReportEmpty() {
      DirectoryListing listing =
          DirectoryListing.builder()
              .path(Paths.get("/empty"))
              .files(new ArrayList<>())
              .directories(new ArrayList<>())
              .totalSize(0L)
              .success(true)
              .build();

      String report = listing.getDetailedReport();
      assertTrue(report.contains("empty"));
      assertTrue(report.contains("0 directories"));
      assertTrue(report.contains("0 files"));
    }

    @Test
    @DisplayName("Should get detailed report for error")
    void testGetDetailedReportError() {
      DirectoryListing listing =
          DirectoryListing.builder()
              .path(Paths.get("/error"))
              .success(false)
              .error("Access denied")
              .build();

      String report = listing.getDetailedReport();
      assertTrue(report.contains("Failed to list"));
      assertTrue(report.contains("Access denied"));
      assertFalse(report.contains("Directories:"));
      assertFalse(report.contains("Files:"));
    }

    @Test
    @DisplayName("Should format sizes correctly")
    void testSizeFormatting() {
      DirectoryListing bytes =
          DirectoryListing.builder().path(Paths.get("/tiny")).totalSize(512L).success(true).build();

      DirectoryListing kb =
          DirectoryListing.builder()
              .path(Paths.get("/small"))
              .totalSize(2048L)
              .success(true)
              .build();

      DirectoryListing mb =
          DirectoryListing.builder()
              .path(Paths.get("/medium"))
              .totalSize(5242880L)
              .success(true)
              .build();

      DirectoryListing gb =
          DirectoryListing.builder()
              .path(Paths.get("/large"))
              .totalSize(2147483648L)
              .success(true)
              .build();

      assertTrue(bytes.getSummary().contains("B"));
      assertTrue(kb.getSummary().contains("KB"));
      assertTrue(mb.getSummary().contains("MB"));
      assertTrue(gb.getSummary().contains("GB"));
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      DirectoryListing listing = DirectoryListing.builder().path(Paths.get("/tmp")).build();

      List<FileInfo> files = new ArrayList<>();
      files.add(FileInfo.builder().name("new.txt").build());

      listing.setFiles(files);
      listing.setTotalItems(1);
      listing.setTotalSize(100L);
      listing.setSuccess(false);
      listing.setError("Test error");

      assertEquals(1, listing.getFiles().size());
      assertEquals(1, listing.getTotalItems());
      assertEquals(100L, listing.getTotalSize());
      assertFalse(listing.isSuccess());
      assertEquals("Test error", listing.getError());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      List<FileInfo> files = new ArrayList<>();
      files.add(FileInfo.builder().name("file1.txt").size(1024L).build());

      DirectoryListing listing =
          DirectoryListing.builder()
              .path(Paths.get("/test"))
              .files(files)
              .totalSize(1024L)
              .success(true)
              .build();

      String str = listing.toString();
      assertEquals(listing.getSummary(), str);
      assertTrue(str.contains("test"));
      assertTrue(str.contains("1 files"));
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      DirectoryListing listing1 =
          DirectoryListing.builder().path(Paths.get("/tmp")).totalSize(1024L).totalItems(5).build();

      DirectoryListing listing2 =
          DirectoryListing.builder().path(Paths.get("/tmp")).totalSize(1024L).totalItems(5).build();

      assertEquals(listing1, listing2);
      assertEquals(listing1.hashCode(), listing2.hashCode());
    }
  }
}
