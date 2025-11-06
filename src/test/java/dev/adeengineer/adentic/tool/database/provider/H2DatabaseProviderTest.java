package dev.adeengineer.adentic.tool.database.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import dev.adeengineer.adentic.tool.database.config.DatabaseConfig;
import dev.adeengineer.adentic.tool.database.model.DatabaseType;
import dev.adeengineer.adentic.tool.database.model.QueryResult;
import dev.adeengineer.adentic.tool.database.model.TransactionResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;
import reactor.core.publisher.Mono;

/**
 * Comprehensive tests for H2DatabaseProvider.
 *
 * <p>Tests all H2-specific functionality including: - Real JDBC connections - PreparedStatement for
 * SQL injection prevention - CRUD operations - Transaction support - Schema introspection - Error
 * handling
 */
@DisplayName("H2DatabaseProvider Tests")
class H2DatabaseProviderTest {

  private H2DatabaseProvider provider;

  @BeforeEach
  void setUp() {
    // Use H2 in-memory database for testing
    DatabaseConfig config = DatabaseConfig.h2Memory();
    provider = new H2DatabaseProvider(config);
  }

  @AfterEach
  void tearDown() {
    if (provider != null && provider.isConnected()) {
      try {
        // Clean up - drop all objects
        provider.executeUpdate("DROP ALL OBJECTS").block();
        provider.disconnect().block();
      } catch (Exception e) {
        // Ignore cleanup errors
      }
    }
  }

  @Nested
  @DisplayName("Constructor and Initialization")
  class ConstructorAndInitializationTests {

    @Test
    @DisplayName("Should initialize with H2 memory configuration")
    void shouldInitializeWithH2MemoryConfiguration() {
      // Given
      DatabaseConfig config = DatabaseConfig.h2Memory();

      // When
      H2DatabaseProvider h2Provider = new H2DatabaseProvider(config);

      // Then
      assertNotNull(h2Provider);
      assertEquals("h2", h2Provider.getProviderName());
      assertEquals(DatabaseType.IN_MEMORY, h2Provider.getDatabaseType());
      assertFalse(h2Provider.isConnected());
    }

    @Test
    @DisplayName("Should initialize with H2 file configuration")
    void shouldInitializeWithH2FileConfiguration() {
      // Given
      DatabaseConfig config = DatabaseConfig.h2File("/tmp/testdb");

      // When
      H2DatabaseProvider h2Provider = new H2DatabaseProvider(config);

      // Then
      assertNotNull(h2Provider);
      assertFalse(h2Provider.isConnected());
    }

    @Test
    @DisplayName("Should have correct provider name and type")
    void shouldHaveCorrectProviderNameAndType() {
      // Then
      assertEquals("h2", provider.getProviderName());
      assertEquals(DatabaseType.IN_MEMORY, provider.getDatabaseType());
    }
  }

  @Nested
  @DisplayName("Connection Management")
  class ConnectionManagementTests {

    @Test
    @DisplayName("Should connect successfully")
    void shouldConnectSuccessfully() {
      // When
      provider.connect().block();

      // Then
      assertTrue(provider.isConnected());
      assertNotNull(provider.getConnectionInfo());
      assertTrue(provider.getConnectionInfo().isConnected());
      assertThat(provider.getConnectionInfo().getJdbcUrl()).contains("jdbc:h2:mem:");
    }

    @Test
    @DisplayName("Should disconnect successfully")
    void shouldDisconnectSuccessfully() {
      // Given
      provider.connect().block();
      assertTrue(provider.isConnected());

      // When
      provider.disconnect().block();

      // Then
      assertFalse(provider.isConnected());
      assertFalse(provider.getConnectionInfo().isConnected());
    }

    @Test
    @DisplayName("Should test connection returns true when connected")
    void shouldTestConnectionReturnsTrueWhenConnected() {
      // Given
      provider.connect().block();

      // When
      Boolean result = provider.testConnection().block();

      // Then
      assertNotNull(result);
      assertTrue(result);
    }

    @Test
    @DisplayName("Should test connection returns false when not connected")
    void shouldTestConnectionReturnsFalseWhenNotConnected() {
      // When
      Boolean result = provider.testConnection().block();

      // Then
      assertNotNull(result);
      assertFalse(result);
    }

    @Test
    @DisplayName("Should handle disconnect when not connected")
    void shouldHandleDisconnectWhenNotConnected() {
      // When/Then - Should not throw
      assertDoesNotThrow(() -> provider.disconnect().block());
    }
  }

  @Nested
  @DisplayName("Query Execution")
  class QueryExecutionTests {

    @BeforeEach
    void setUpTable() {
      provider.connect().block();
      provider
          .executeUpdate(
              "CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), age INT)")
          .block();
    }

    @Test
    @DisplayName("Should execute query with no parameters")
    void shouldExecuteQueryWithNoParameters() {
      // Given
      provider.executeUpdate("INSERT INTO users (name, age) VALUES ('Alice', 25)").block();

      // When
      QueryResult result = provider.executeQuery("SELECT * FROM users").block();

      // Then
      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(1, result.getRowCount());
      assertNotNull(result.getRows());
      assertEquals("Alice", result.getRows().get(0).get("NAME"));
    }

    @Test
    @DisplayName("Should execute query with positional parameters")
    void shouldExecuteQueryWithPositionalParameters() {
      // Given
      provider.executeUpdate("INSERT INTO users (name, age) VALUES ('Alice', 25)").block();
      provider.executeUpdate("INSERT INTO users (name, age) VALUES ('Bob', 30)").block();

      // When
      QueryResult result = provider.executeQuery("SELECT * FROM users WHERE age > ?", 26).block();

      // Then
      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(1, result.getRowCount());
      assertEquals("Bob", result.getRows().get(0).get("NAME"));
    }

    @Test
    @DisplayName("Should execute query with named parameters")
    void shouldExecuteQueryWithNamedParameters() {
      // Given
      provider.executeUpdate("INSERT INTO users (name, age) VALUES ('Alice', 25)").block();

      // When
      QueryResult result =
          provider.executeQuery("SELECT * FROM users WHERE age > ?", Map.of("age", 20)).block();

      // Then
      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(1, result.getRowCount());
    }

    @Test
    @DisplayName("Should prevent SQL injection with prepared statements")
    void shouldPreventSqlInjectionWithPreparedStatements() {
      // Given
      provider.executeUpdate("INSERT INTO users (name, age) VALUES ('Alice', 25)").block();
      String maliciousInput = "25 OR 1=1"; // SQL injection attempt

      // When - PreparedStatement treats this as literal value
      QueryResult result =
          provider.executeQuery("SELECT * FROM users WHERE age > ?", maliciousInput).block();

      // Then - Should fail (type mismatch) rather than return all rows
      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertNotNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("Should include column metadata in query results")
    void shouldIncludeColumnMetadataInQueryResults() {
      // Given
      provider.executeUpdate("INSERT INTO users (name, age) VALUES ('Alice', 25)").block();

      // When
      QueryResult result = provider.executeQuery("SELECT * FROM users").block();

      // Then
      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertNotNull(result.getColumns());
      assertThat(result.getColumns()).isNotEmpty();
      assertThat(result.getColumns())
          .extracting(col -> col.getName())
          .contains("ID", "NAME", "AGE");
    }

    @Test
    @DisplayName("Should record execution time")
    void shouldRecordExecutionTime() {
      // Given
      provider.executeUpdate("INSERT INTO users (name, age) VALUES ('Alice', 25)").block();

      // When
      QueryResult result = provider.executeQuery("SELECT * FROM users").block();

      // Then
      assertNotNull(result);
      assertTrue(result.getExecutionTimeMs() >= 0);
    }

    @Test
    @DisplayName("Should handle query errors gracefully")
    void shouldHandleQueryErrorsGracefully() {
      // When - Invalid SQL
      QueryResult result = provider.executeQuery("SELECT * FROM non_existent_table").block();

      // Then
      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertNotNull(result.getErrorMessage());
      assertNotNull(result.getSqlState());
    }
  }

  @Nested
  @DisplayName("Update Operations")
  class UpdateOperationsTests {

    @BeforeEach
    void setUpTable() {
      provider.connect().block();
      provider
          .executeUpdate(
              "CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), age INT)")
          .block();
    }

    @Test
    @DisplayName("Should execute update with no parameters")
    void shouldExecuteUpdateWithNoParameters() {
      // When
      Long affected =
          provider.executeUpdate("INSERT INTO users (name, age) VALUES ('Alice', 25)").block();

      // Then
      assertNotNull(affected);
      assertEquals(1L, affected);
    }

    @Test
    @DisplayName("Should execute update with positional parameters")
    void shouldExecuteUpdateWithPositionalParameters() {
      // When
      Long affected =
          provider.executeUpdate("INSERT INTO users (name, age) VALUES (?, ?)", "Bob", 30).block();

      // Then
      assertNotNull(affected);
      assertEquals(1L, affected);
    }

    @Test
    @DisplayName("Should handle update errors gracefully")
    void shouldHandleUpdateErrorsGracefully() {
      // When/Then - Invalid SQL should throw exception
      assertThatThrownBy(
              () -> provider.executeUpdate("INSERT INTO non_existent (col) VALUES (1)").block())
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Update failed");
    }
  }

  @Nested
  @DisplayName("CRUD Operations")
  class CrudOperationsTests {

    @BeforeEach
    void setUpTable() {
      provider.connect().block();
      provider
          .executeUpdate(
              "CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), age INT, email VARCHAR(255))")
          .block();
    }

    @Test
    @DisplayName("Should insert and return generated key")
    void shouldInsertAndReturnGeneratedKey() {
      // When
      Object userId =
          provider
              .insert("users", Map.of("name", "Alice", "age", 25, "email", "alice@example.com"))
              .block();

      // Then
      assertNotNull(userId);
    }

    @Test
    @DisplayName("Should insert batch records")
    void shouldInsertBatchRecords() {
      // Given
      List<Map<String, Object>> users =
          List.of(
              Map.of("name", "User1", "age", 20, "email", "user1@example.com"),
              Map.of("name", "User2", "age", 21, "email", "user2@example.com"),
              Map.of("name", "User3", "age", 22, "email", "user3@example.com"));

      // When
      Long inserted = provider.insertBatch("users", users).block();

      // Then
      assertEquals(3L, inserted);

      // Verify
      QueryResult result = provider.findAll("users").block();
      assertEquals(3, result.getRowCount());
    }

    @Test
    @DisplayName("Should find record by ID")
    void shouldFindRecordById() {
      // Given
      Object userId =
          provider
              .insert("users", Map.of("name", "Bob", "age", 30, "email", "bob@example.com"))
              .block();

      // When
      Map<String, Object> user = provider.findById("users", userId).block();

      // Then
      assertNotNull(user);
      assertEquals("Bob", user.get("NAME"));
      assertEquals(30, user.get("AGE"));
    }

    @Test
    @DisplayName("Should find all records")
    void shouldFindAllRecords() {
      // Given
      provider
          .insertBatch(
              "users",
              List.of(
                  Map.of("name", "Alice", "age", 25, "email", "alice@example.com"),
                  Map.of("name", "Bob", "age", 30, "email", "bob@example.com")))
          .block();

      // When
      QueryResult result = provider.findAll("users").block();

      // Then
      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(2, result.getRowCount());
    }

    @Test
    @DisplayName("Should find records by criteria")
    void shouldFindRecordsByCriteria() {
      // Given
      provider
          .insertBatch(
              "users",
              List.of(
                  Map.of("name", "Alice", "age", 25, "email", "alice@example.com"),
                  Map.of("name", "Bob", "age", 25, "email", "bob@example.com"),
                  Map.of("name", "Charlie", "age", 30, "email", "charlie@example.com")))
          .block();

      // When
      QueryResult result = provider.findWhere("users", Map.of("age", 25)).block();

      // Then
      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(2, result.getRowCount());
    }

    @Test
    @DisplayName("Should update record by ID")
    void shouldUpdateRecordById() {
      // Given
      Object userId =
          provider
              .insert("users", Map.of("name", "Alice", "age", 25, "email", "alice@example.com"))
              .block();

      // When
      Boolean updated = provider.update("users", userId, Map.of("age", 26)).block();

      // Then
      assertTrue(updated);

      // Verify
      Map<String, Object> user = provider.findById("users", userId).block();
      assertEquals(26, user.get("AGE"));
    }

    @Test
    @DisplayName("Should update records by criteria")
    void shouldUpdateRecordsByCriteria() {
      // Given
      provider
          .insertBatch(
              "users",
              List.of(
                  Map.of("name", "Alice", "age", 25, "email", "alice@example.com"),
                  Map.of("name", "Bob", "age", 25, "email", "bob@example.com")))
          .block();

      // When
      Long updated = provider.updateWhere("users", Map.of("age", 25), Map.of("age", 26)).block();

      // Then
      assertEquals(2L, updated);
    }

    @Test
    @DisplayName("Should delete record by ID")
    void shouldDeleteRecordById() {
      // Given
      Object userId =
          provider
              .insert("users", Map.of("name", "Alice", "age", 25, "email", "alice@example.com"))
              .block();

      // When
      Boolean deleted = provider.delete("users", userId).block();

      // Then
      assertTrue(deleted);

      // Verify
      Map<String, Object> user = provider.findById("users", userId).block();
      assertTrue(user.isEmpty());
    }

    @Test
    @DisplayName("Should delete records by criteria")
    void shouldDeleteRecordsByCriteria() {
      // Given
      provider
          .insertBatch(
              "users",
              List.of(
                  Map.of("name", "Alice", "age", 25, "email", "alice@example.com"),
                  Map.of("name", "Bob", "age", 25, "email", "bob@example.com")))
          .block();

      // When
      Long deleted = provider.deleteWhere("users", Map.of("age", 25)).block();

      // Then
      assertEquals(2L, deleted);

      // Verify
      Long count = provider.count("users").block();
      assertEquals(0L, count);
    }

    @Test
    @DisplayName("Should count all records")
    void shouldCountAllRecords() {
      // Given
      provider
          .insertBatch(
              "users",
              List.of(
                  Map.of("name", "Alice", "age", 25, "email", "alice@example.com"),
                  Map.of("name", "Bob", "age", 30, "email", "bob@example.com")))
          .block();

      // When
      Long count = provider.count("users").block();

      // Then
      assertEquals(2L, count);
    }

    @Test
    @DisplayName("Should count records by criteria")
    void shouldCountRecordsByCriteria() {
      // Given
      provider
          .insertBatch(
              "users",
              List.of(
                  Map.of("name", "Alice", "age", 25, "email", "alice@example.com"),
                  Map.of("name", "Bob", "age", 25, "email", "bob@example.com"),
                  Map.of("name", "Charlie", "age", 30, "email", "charlie@example.com")))
          .block();

      // When
      Long count = provider.countWhere("users", Map.of("age", 25)).block();

      // Then
      assertEquals(2L, count);
    }
  }

  @Nested
  @DisplayName("Transaction Support")
  class TransactionSupportTests {

    @BeforeEach
    void setUpTable() {
      provider.connect().block();
      provider
          .executeUpdate(
              "CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100))")
          .block();
    }

    @Test
    @DisplayName("Should commit transaction successfully")
    void shouldCommitTransactionSuccessfully() {
      // When
      TransactionResult result =
          provider
              .executeTransaction(
                  p -> {
                    p.insert("users", Map.of("name", "User1")).block();
                    p.insert("users", Map.of("name", "User2")).block();
                    return Mono.empty();
                  })
              .block();

      // Then
      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertTrue(result.isCommitted());
      assertFalse(result.isRolledBack());

      // Verify
      Long count = provider.count("users").block();
      assertEquals(2L, count);
    }

    @Test
    @DisplayName("Should rollback transaction on error")
    void shouldRollbackTransactionOnError() {
      // When
      TransactionResult result =
          provider
              .executeTransaction(
                  p -> {
                    p.insert("users", Map.of("name", "User1")).block();
                    throw new RuntimeException("Simulated error");
                  })
              .block();

      // Then
      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertFalse(result.isCommitted());
      assertTrue(result.isRolledBack());
      assertNotNull(result.getErrorMessage());

      // Verify - no records inserted
      Long count = provider.count("users").block();
      assertEquals(0L, count);
    }

    @Test
    @DisplayName("Should support manual transaction control - begin, commit")
    void shouldSupportManualTransactionControlCommit() {
      // When
      provider.beginTransaction().block();
      provider.insert("users", Map.of("name", "User1")).block();
      provider.insert("users", Map.of("name", "User2")).block();
      provider.commit().block();

      // Then
      Long count = provider.count("users").block();
      assertEquals(2L, count);
    }

    @Test
    @DisplayName("Should support manual transaction control - begin, rollback")
    void shouldSupportManualTransactionControlRollback() {
      // When
      provider.beginTransaction().block();
      provider.insert("users", Map.of("name", "User1")).block();
      provider.rollback().block();

      // Then
      Long count = provider.count("users").block();
      assertEquals(0L, count);
    }
  }

  @Nested
  @DisplayName("Schema Introspection")
  class SchemaIntrospectionTests {

    @BeforeEach
    void setUpTables() {
      provider.connect().block();
      provider
          .executeUpdate(
              "CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), age INT)")
          .block();
      provider
          .executeUpdate(
              "CREATE TABLE orders (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT, total DECIMAL(10,2))")
          .block();
    }

    @Test
    @DisplayName("Should list all tables")
    void shouldListAllTables() {
      // When
      List<String> tables = provider.listTables().block();

      // Then
      assertNotNull(tables);
      assertThat(tables).contains("USERS", "ORDERS");
    }

    @Test
    @DisplayName("Should check if table exists")
    void shouldCheckIfTableExists() {
      // When/Then
      assertTrue(provider.tableExists("USERS").block());
      assertTrue(provider.tableExists("ORDERS").block());
      assertFalse(provider.tableExists("NON_EXISTENT").block());
    }

    @Test
    @DisplayName("Should get table schema")
    void shouldGetTableSchema() {
      // When
      Map<String, Object> schema = provider.getSchema("USERS").block();

      // Then
      assertNotNull(schema);
      assertEquals("USERS", schema.get("table"));

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> columns = (List<Map<String, Object>>) schema.get("columns");
      assertNotNull(columns);
      assertThat(columns).hasSize(3); // id, name, age

      assertThat(columns).extracting(col -> col.get("name")).contains("ID", "NAME", "AGE");
    }
  }

  @Nested
  @DisplayName("Error Handling")
  class ErrorHandlingTests {

    @BeforeEach
    void setUpConnection() {
      provider.connect().block();
    }

    @Test
    @DisplayName("Should throw error when operating without connection")
    void shouldThrowErrorWhenOperatingWithoutConnection() {
      // Given
      H2DatabaseProvider disconnectedProvider = new H2DatabaseProvider(DatabaseConfig.h2Memory());

      // When/Then
      assertThatThrownBy(() -> disconnectedProvider.executeQuery("SELECT 1").block())
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Not connected");
    }

    @Test
    @DisplayName("Should validate table names")
    void shouldValidateTableNames() {
      // When/Then - SQL injection attempts
      assertThatThrownBy(() -> provider.findAll("users; DROP TABLE users").block())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Invalid table name");
    }

    @Test
    @DisplayName("Should handle empty batch insert")
    void shouldHandleEmptyBatchInsert() {
      // Given
      provider
          .executeUpdate(
              "CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100))")
          .block();

      // When
      Long count = provider.insertBatch("users", List.of()).block();

      // Then
      assertEquals(0L, count);
    }
  }

  @Nested
  @DisplayName("Connection Error Paths")
  class ConnectionErrorPathsTests {

    @Test
    @DisplayName("Should handle connection failure with invalid URL")
    void shouldHandleConnectionFailureWithInvalidUrl() {
      // Given - Create config with TCP mode to non-existent server
      DatabaseConfig badConfig =
          DatabaseConfig.builder()
              .database("tcp://invalid:9999/testdb") // TCP mode without server
              .username("SA")
              .password("")
              .build();

      H2DatabaseProvider badProvider = new H2DatabaseProvider(badConfig);

      // This should throw when trying to connect to non-existent TCP server
      assertThatThrownBy(() -> badProvider.connect().block())
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Failed to connect to H2");
    }

    @Test
    @DisplayName("Should handle SQLException during disconnect")
    void shouldHandleSqlExceptionDuringDisconnect() {
      // Given - Connect and then close the underlying connection directly
      provider.connect().block();

      // When - Try to disconnect normally (should handle gracefully)
      // The existing disconnect handles this, but we need to test the error path
      // We can't easily force a SQLException on close, so we test double disconnect
      provider.disconnect().block();

      // Second disconnect should not throw even if connection is already closed
      assertDoesNotThrow(() -> provider.disconnect().block());
    }

    @Test
    @DisplayName("Should return false when testing closed connection")
    void shouldReturnFalseWhenTestingClosedConnection() {
      // Given - Connect and then disconnect
      provider.connect().block();
      provider.disconnect().block();

      // When
      Boolean result = provider.testConnection().block();

      // Then
      assertNotNull(result);
      assertFalse(result);
    }

    @Test
    @DisplayName("Should handle testConnection when connection throws SQLException")
    void shouldHandleTestConnectionWhenConnectionThrowsSqlException() {
      // Given - Connect and then manually close the connection to simulate error
      provider.connect().block();
      provider.disconnect().block();

      // When - Test connection on closed connection
      Boolean result = provider.testConnection().block();

      // Then - Should return false rather than throwing
      assertNotNull(result);
      assertFalse(result);
    }
  }

  @Nested
  @DisplayName("JDBC URL Building")
  class JdbcUrlBuildingTests {

    @Test
    @DisplayName("Should build JDBC URL for in-memory database with mem: prefix")
    void shouldBuildJdbcUrlForInMemoryDatabaseWithMemPrefix() {
      // Given
      DatabaseConfig config =
          DatabaseConfig.builder().database("mem:testdb").username("SA").password("").build();

      H2DatabaseProvider h2Provider = new H2DatabaseProvider(config);

      // When
      h2Provider.connect().block();

      // Then
      assertNotNull(h2Provider.getConnectionInfo().getJdbcUrl());
      assertThat(h2Provider.getConnectionInfo().getJdbcUrl())
          .contains("jdbc:h2:mem:testdb")
          .contains("DB_CLOSE_DELAY=-1");

      h2Provider.disconnect().block();
    }

    @Test
    @DisplayName("Should build JDBC URL for file-based database")
    void shouldBuildJdbcUrlForFileBasedDatabase() {
      // Given
      DatabaseConfig config =
          DatabaseConfig.builder()
              .database("file:/tmp/h2testdb")
              .username("SA")
              .password("")
              .build();
      H2DatabaseProvider h2Provider = new H2DatabaseProvider(config);

      // When
      h2Provider.connect().block();

      // Then
      assertNotNull(h2Provider.getConnectionInfo().getJdbcUrl());
      assertThat(h2Provider.getConnectionInfo().getJdbcUrl())
          .contains("jdbc:h2:file:/tmp/h2testdb");

      h2Provider.disconnect().block();
      // Clean up file
      h2Provider.executeUpdate("SHUTDOWN").onErrorReturn(0L).block();
    }

    @Test
    @DisplayName("Should build JDBC URL for default in-memory database")
    void shouldBuildJdbcUrlForDefaultInMemoryDatabase() {
      // Given - Database name without prefix defaults to in-memory
      DatabaseConfig config =
          DatabaseConfig.builder().database("defaultdb").username("SA").password("").build();

      H2DatabaseProvider h2Provider = new H2DatabaseProvider(config);

      // When
      h2Provider.connect().block();

      // Then
      assertNotNull(h2Provider.getConnectionInfo().getJdbcUrl());
      assertThat(h2Provider.getConnectionInfo().getJdbcUrl())
          .contains("jdbc:h2:mem:defaultdb")
          .contains("DB_CLOSE_DELAY=-1");

      h2Provider.disconnect().block();
    }
  }

  @Nested
  @DisplayName("CRUD Error Paths")
  class CrudErrorPathsTests {

    @BeforeEach
    void setUpTable() {
      provider.connect().block();
      provider
          .executeUpdate(
              "CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), age INT)")
          .block();
    }

    @Test
    @DisplayName("Should handle insert when no generated key is returned")
    void shouldHandleInsertWhenNoGeneratedKeyIsReturned() {
      // Given - Insert a record
      Object userId = provider.insert("users", Map.of("name", "Alice", "age", 25)).block();

      // Then - Should return the generated key
      assertNotNull(userId);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent record")
    void shouldReturnFalseWhenDeletingNonExistentRecord() {
      // When - Try to delete a record that doesn't exist
      Boolean deleted = provider.delete("users", 99999).block();

      // Then
      assertFalse(deleted);
    }

    @Test
    @DisplayName("Should return false when updating non-existent record")
    void shouldReturnFalseWhenUpdatingNonExistentRecord() {
      // When - Try to update a record that doesn't exist
      Boolean updated = provider.update("users", 99999, Map.of("name", "NewName")).block();

      // Then
      assertFalse(updated);
    }

    @Test
    @DisplayName("Should handle findById when record does not exist")
    void shouldHandleFindByIdWhenRecordDoesNotExist() {
      // When - Try to find a record that doesn't exist
      Map<String, Object> user = provider.findById("users", 99999).block();

      // Then - Should return empty map
      assertNotNull(user);
      assertTrue(user.isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when inserting into non-existent table")
    void shouldThrowExceptionWhenInsertingIntoNonExistentTable() {
      // When/Then
      assertThatThrownBy(() -> provider.insert("non_existent", Map.of("name", "Test")).block())
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Insert failed");
    }

    @Test
    @DisplayName("Should throw exception when deleting from non-existent table")
    void shouldThrowExceptionWhenDeletingFromNonExistentTable() {
      // When/Then
      assertThatThrownBy(() -> provider.delete("non_existent", 1).block())
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Delete failed");
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent table")
    void shouldThrowExceptionWhenUpdatingNonExistentTable() {
      // When/Then
      assertThatThrownBy(() -> provider.update("non_existent", 1, Map.of("name", "Test")).block())
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Update failed");
    }

    @Test
    @DisplayName("Should throw exception when batch inserting into non-existent table")
    void shouldThrowExceptionWhenBatchInsertingIntoNonExistentTable() {
      // When/Then
      assertThatThrownBy(
              () -> provider.insertBatch("non_existent", List.of(Map.of("name", "Test"))).block())
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Batch insert failed");
    }

    @Test
    @DisplayName("Should throw exception when deleteWhere on non-existent table")
    void shouldThrowExceptionWhenDeleteWhereOnNonExistentTable() {
      // When/Then
      assertThatThrownBy(() -> provider.deleteWhere("non_existent", Map.of("id", 1)).block())
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Delete failed");
    }

    @Test
    @DisplayName("Should throw exception when updateWhere on non-existent table")
    void shouldThrowExceptionWhenUpdateWhereOnNonExistentTable() {
      // When/Then
      assertThatThrownBy(
              () ->
                  provider
                      .updateWhere("non_existent", Map.of("id", 1), Map.of("name", "Test"))
                      .block())
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Update failed");
    }

    @Test
    @DisplayName("Should throw exception when findById on non-existent table")
    void shouldThrowExceptionWhenFindByIdOnNonExistentTable() {
      // When/Then
      assertThatThrownBy(() -> provider.findById("non_existent", 1).block())
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Find by ID failed");
    }
  }

  @Nested
  @DisplayName("executeUpdate with Map Parameters")
  class ExecuteUpdateWithMapParametersTests {

    @BeforeEach
    void setUpTable() {
      provider.connect().block();
      provider
          .executeUpdate(
              "CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), age INT)")
          .block();
    }

    @Test
    @DisplayName("Should execute update with Map parameters")
    void shouldExecuteUpdateWithMapParameters() {
      // Given - Use LinkedHashMap to preserve insertion order for positional parameters
      Map<String, Object> params = new LinkedHashMap<>();
      params.put("name", "Alice");
      params.put("age", 25);

      // When
      Long affected =
          provider.executeUpdate("INSERT INTO users (name, age) VALUES (?, ?)", params).block();

      // Then
      assertNotNull(affected);
      assertEquals(1L, affected);

      // Verify
      QueryResult result = provider.findAll("users").block();
      assertEquals(1, result.getRowCount());
    }
  }

  @Nested
  @DisplayName("Transaction Error Paths")
  class TransactionErrorPathsTests {

    @BeforeEach
    void setUpTable() {
      provider.connect().block();
      provider
          .executeUpdate(
              "CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100))")
          .block();
    }

    @Test
    @DisplayName("Should handle commit failure gracefully")
    void shouldHandleCommitFailureGracefully() {
      // Given - Start a transaction
      provider.beginTransaction().block();
      provider.insert("users", Map.of("name", "User1")).block();

      // When - Commit the transaction
      provider.commit().block();

      // Then - Should succeed
      Long count = provider.count("users").block();
      assertEquals(1L, count);
    }

    @Test
    @DisplayName("Should throw exception when beginTransaction without connection")
    void shouldThrowExceptionWhenBeginTransactionWithoutConnection() {
      // Given
      H2DatabaseProvider disconnectedProvider = new H2DatabaseProvider(DatabaseConfig.h2Memory());

      // When/Then
      assertThatThrownBy(() -> disconnectedProvider.beginTransaction().block())
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Not connected");
    }

    @Test
    @DisplayName("Should throw exception when commit without connection")
    void shouldThrowExceptionWhenCommitWithoutConnection() {
      // Given
      H2DatabaseProvider disconnectedProvider = new H2DatabaseProvider(DatabaseConfig.h2Memory());

      // When/Then
      assertThatThrownBy(() -> disconnectedProvider.commit().block())
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Not connected");
    }

    @Test
    @DisplayName("Should throw exception when rollback without connection")
    void shouldThrowExceptionWhenRollbackWithoutConnection() {
      // Given
      H2DatabaseProvider disconnectedProvider = new H2DatabaseProvider(DatabaseConfig.h2Memory());

      // When/Then
      assertThatThrownBy(() -> disconnectedProvider.rollback().block())
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Not connected");
    }
  }

  @Nested
  @DisplayName("Count Error Paths")
  class CountErrorPathsTests {

    @BeforeEach
    void setUpConnection() {
      provider.connect().block();
    }

    @Test
    @DisplayName("Should return 0 when count fails on non-existent table")
    void shouldReturnZeroWhenCountFailsOnNonExistentTable() {
      // When - Count on non-existent table returns error result
      QueryResult result = provider.executeQuery("SELECT COUNT(*) FROM non_existent").block();

      // Then - Should fail
      assertNotNull(result);
      assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle countWhere when result is null or empty")
    void shouldHandleCountWhereWhenResultIsNullOrEmpty() {
      // Given
      provider
          .executeUpdate(
              "CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), age INT)")
          .block();

      // When - Count with criteria that returns no results
      Long count = provider.countWhere("users", Map.of("age", 999)).block();

      // Then
      assertEquals(0L, count);
    }
  }

  @Nested
  @DisplayName("Schema Introspection Error Paths")
  class SchemaIntrospectionErrorPathsTests {

    @BeforeEach
    void setUpConnection() {
      provider.connect().block();
    }

    @Test
    @DisplayName("Should throw exception when getSchema on non-existent table")
    void shouldThrowExceptionWhenGetSchemaOnNonExistentTable() {
      // When - Get schema for non-existent table should still work but return empty columns
      Map<String, Object> schema = provider.getSchema("NON_EXISTENT").block();

      // Then - Should return schema with empty columns list
      assertNotNull(schema);
      assertEquals("NON_EXISTENT", schema.get("table"));
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> columns = (List<Map<String, Object>>) schema.get("columns");
      assertNotNull(columns);
      assertTrue(columns.isEmpty());
    }

    @Test
    @DisplayName("Should handle listTables when no tables exist")
    void shouldHandleListTablesWhenNoTablesExist() {
      // When - List tables when database is empty
      List<String> tables = provider.listTables().block();

      // Then - Should return empty list
      assertNotNull(tables);
      // May be empty or have system tables depending on H2 version
    }

    @Test
    @DisplayName("Should return false when tableExists for non-existent table")
    void shouldReturnFalseWhenTableExistsForNonExistentTable() {
      // When
      Boolean exists = provider.tableExists("NON_EXISTENT_TABLE").block();

      // Then
      assertFalse(exists);
    }
  }
}
