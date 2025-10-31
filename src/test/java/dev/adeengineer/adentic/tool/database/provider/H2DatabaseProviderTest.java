package dev.adeengineer.adentic.tool.database.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import dev.adeengineer.adentic.tool.database.config.DatabaseConfig;
import dev.adeengineer.adentic.tool.database.model.DatabaseType;
import dev.adeengineer.adentic.tool.database.model.QueryResult;
import dev.adeengineer.adentic.tool.database.model.TransactionResult;
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
}
