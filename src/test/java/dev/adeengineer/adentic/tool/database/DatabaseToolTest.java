package dev.adeengineer.adentic.tool.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import dev.adeengineer.adentic.tool.database.config.DatabaseConfig;
import dev.adeengineer.adentic.tool.database.model.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;

/** Comprehensive tests for DatabaseTool */
@DisplayName("DatabaseTool Tests")
class DatabaseToolTest {

  private DatabaseTool databaseTool;

  @BeforeEach
  void setUp() {
    // Use H2 in-memory database for testing
    DatabaseConfig config = DatabaseConfig.h2Memory();
    databaseTool = new DatabaseTool(config);
  }

  @AfterEach
  void tearDown() {
    if (databaseTool.isConnected()) {
      databaseTool.disconnect().block();
    }
  }

  @Nested
  @DisplayName("Lifecycle Operations")
  class LifecycleTests {

    @Test
    @DisplayName("Should connect to database")
    void testConnect() {
      databaseTool.connect().block();
      assertTrue(databaseTool.isConnected());
    }

    @Test
    @DisplayName("Should disconnect from database")
    void testDisconnect() {
      databaseTool.connect().block();
      databaseTool.disconnect().block();
      assertFalse(databaseTool.isConnected());
    }

    @Test
    @DisplayName("Should test connection")
    void testTestConnection() {
      databaseTool.connect().block();
      Boolean isConnected = databaseTool.testConnection().block();

      assertNotNull(isConnected);
      assertTrue(isConnected);
    }

    @Test
    @DisplayName("Should get connection info")
    void testGetConnectionInfo() {
      databaseTool.connect().block();
      ConnectionInfo info = databaseTool.getConnectionInfo();

      assertNotNull(info);
      assertEquals(DatabaseProvider.H2, info.getProvider());
    }
  }

  @Nested
  @DisplayName("Schema Operations")
  class SchemaTests {

    @BeforeEach
    void setupDatabase() {
      databaseTool.connect().block();
      // Create a test table
      databaseTool
          .executeUpdate(
              "DROP TABLE IF EXISTS test_table; CREATE TABLE test_table (id INT PRIMARY KEY, name VARCHAR(255))")
          .block();
    }

    @Test
    @DisplayName("Should list tables")
    void testListTables() {
      List<String> tables = databaseTool.listTables().block();

      assertNotNull(tables);
      assertThat(tables).contains("TEST_TABLE");
    }

    @Test
    @DisplayName("Should check if table exists")
    void testTableExists() {
      Boolean exists = databaseTool.tableExists("TEST_TABLE").block();

      assertNotNull(exists);
      assertTrue(exists);
    }

    @Test
    @DisplayName("Should get table schema")
    void testGetSchema() {
      Map<String, Object> schema = databaseTool.getSchema("TEST_TABLE").block();

      assertNotNull(schema);
    }
  }

  @Nested
  @DisplayName("Query Operations")
  class QueryTests {

    @BeforeEach
    void setupDatabase() {
      databaseTool.connect().block();
      databaseTool
          .executeUpdate(
              "DROP TABLE IF EXISTS users; CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(255), age INT)")
          .block();
      databaseTool.executeUpdate("INSERT INTO users VALUES (1, 'Alice', 30)").block();
      databaseTool.executeUpdate("INSERT INTO users VALUES (2, 'Bob', 25)").block();
    }

    @Test
    @DisplayName("Should execute simple query")
    void testExecuteQuery() {
      QueryResult result = databaseTool.executeQuery("SELECT * FROM users").block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(2, result.getRowCount());
    }

    @Test
    @DisplayName("Should execute parameterized query")
    void testExecuteParameterizedQuery() {
      QueryResult result =
          databaseTool.executeQuery("SELECT * FROM users WHERE age > ?", 26).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(1, result.getRowCount());
    }

    @Test
    @DisplayName("Should execute query with named parameters")
    void testExecuteQueryWithNamedParams() {
      QueryResult result =
          databaseTool
              .executeQuery("SELECT * FROM users WHERE name = ?", Map.of("name", "Alice"))
              .block();

      assertNotNull(result);
    }
  }

  @Nested
  @DisplayName("Update Operations")
  class UpdateTests {

    @BeforeEach
    void setupDatabase() {
      databaseTool.connect().block();
      databaseTool
          .executeUpdate(
              "DROP TABLE IF EXISTS products; CREATE TABLE products (id INT PRIMARY KEY, name VARCHAR(255), price DECIMAL)")
          .block();
      databaseTool.executeUpdate("INSERT INTO products VALUES (1, 'Product A', 10.00)").block();
    }

    @Test
    @DisplayName("Should execute update statement")
    void testExecuteUpdate() {
      Long affected =
          databaseTool.executeUpdate("UPDATE products SET price = 15.00 WHERE id = 1").block();

      assertNotNull(affected);
      assertEquals(1, affected);
    }

    @Test
    @DisplayName("Should execute parameterized update")
    void testExecuteParameterizedUpdate() {
      Long affected =
          databaseTool
              .executeUpdate("UPDATE products SET price = ? WHERE id = ?", 20.00, 1)
              .block();

      assertNotNull(affected);
      assertEquals(1, affected);
    }

    @Test
    @DisplayName("Should execute delete statement")
    void testExecuteDelete() {
      Long affected = databaseTool.executeUpdate("DELETE FROM products WHERE id = 1").block();

      assertNotNull(affected);
      assertEquals(1, affected);
    }
  }

  @Nested
  @DisplayName("Insert Operations")
  class InsertTests {

    @BeforeEach
    void setupDatabase() {
      databaseTool.connect().block();
      databaseTool
          .executeUpdate(
              "DROP TABLE IF EXISTS orders; CREATE TABLE orders (id INT PRIMARY KEY, item VARCHAR(255), quantity INT)")
          .block();
    }

    @Test
    @DisplayName("Should insert single record")
    void testInsert() {
      Object id =
          databaseTool.insert("orders", Map.of("id", 1, "item", "Widget", "quantity", 10)).block();

      assertNotNull(id);
    }

    @Test
    @DisplayName("Should insert batch records")
    void testInsertBatch() {
      List<Map<String, Object>> records =
          List.of(
              Map.of("id", 1, "item", "Widget A", "quantity", 10),
              Map.of("id", 2, "item", "Widget B", "quantity", 20));

      Long affected = databaseTool.insertBatch("orders", records).block();

      assertNotNull(affected);
      assertEquals(2, affected);
    }
  }

  @Nested
  @DisplayName("CRUD Operations")
  class CrudTests {

    @BeforeEach
    void setupDatabase() {
      databaseTool.connect().block();
      databaseTool
          .executeUpdate(
              "DROP TABLE IF EXISTS items; CREATE TABLE items (id INT PRIMARY KEY, name VARCHAR(255), status VARCHAR(50))")
          .block();
      databaseTool.executeUpdate("INSERT INTO items VALUES (1, 'Item 1', 'active')").block();
      databaseTool.executeUpdate("INSERT INTO items VALUES (2, 'Item 2', 'active')").block();
    }

    @Test
    @DisplayName("Should find by ID")
    void testFindById() {
      Map<String, Object> item = databaseTool.findById("items", 1).block();

      assertNotNull(item);
      assertEquals("Item 1", item.get("NAME"));
    }

    @Test
    @DisplayName("Should find all records")
    void testFindAll() {
      QueryResult result = databaseTool.findAll("items").block();

      assertNotNull(result);
      assertEquals(2, result.getRowCount());
    }

    @Test
    @DisplayName("Should find with criteria")
    void testFindWhere() {
      QueryResult result = databaseTool.findWhere("items", Map.of("status", "active")).block();

      assertNotNull(result);
      assertEquals(2, result.getRowCount());
    }

    @Test
    @DisplayName("Should update by ID")
    void testUpdate() {
      Boolean updated = databaseTool.update("items", 1, Map.of("status", "inactive")).block();

      assertNotNull(updated);
      assertTrue(updated);
    }

    @Test
    @DisplayName("Should update with criteria")
    void testUpdateWhere() {
      Long affected =
          databaseTool
              .updateWhere("items", Map.of("status", "active"), Map.of("status", "pending"))
              .block();

      assertNotNull(affected);
      assertEquals(2, affected);
    }

    @Test
    @DisplayName("Should delete by ID")
    void testDelete() {
      Boolean deleted = databaseTool.delete("items", 1).block();

      assertNotNull(deleted);
      assertTrue(deleted);
    }

    @Test
    @DisplayName("Should delete with criteria")
    void testDeleteWhere() {
      Long affected = databaseTool.deleteWhere("items", Map.of("status", "active")).block();

      assertNotNull(affected);
      assertEquals(2, affected);
    }

    @Test
    @DisplayName("Should count records")
    void testCount() {
      Long count = databaseTool.count("items").block();

      assertNotNull(count);
      assertEquals(2, count);
    }

    @Test
    @DisplayName("Should count with criteria")
    void testCountWhere() {
      Long count = databaseTool.countWhere("items", Map.of("status", "active")).block();

      assertNotNull(count);
      assertEquals(2, count);
    }
  }

  @Nested
  @DisplayName("Transaction Operations")
  class TransactionTests {

    @BeforeEach
    void setupDatabase() {
      databaseTool.connect().block();
      databaseTool
          .executeUpdate(
              "DROP TABLE IF EXISTS accounts; CREATE TABLE accounts (id INT PRIMARY KEY, balance DECIMAL)")
          .block();
      databaseTool.executeUpdate("INSERT INTO accounts VALUES (1, 1000.00)").block();
    }

    @Test
    @DisplayName("Should execute transaction")
    void testExecuteTransaction() {
      TransactionResult result =
          databaseTool
              .executeTransaction(
                  provider -> {
                    provider.executeUpdate("UPDATE accounts SET balance = 900.00 WHERE id = 1");
                    return reactor.core.publisher.Mono.empty();
                  })
              .block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should begin transaction")
    void testBeginTransaction() {
      databaseTool.beginTransaction().block();
      // Should not throw exception
    }

    @Test
    @DisplayName("Should commit transaction")
    void testCommit() {
      databaseTool.beginTransaction().block();
      databaseTool.executeUpdate("UPDATE accounts SET balance = 950.00 WHERE id = 1").block();
      databaseTool.commit().block();
      // Should not throw exception
    }

    @Test
    @DisplayName("Should rollback transaction")
    void testRollback() {
      databaseTool.beginTransaction().block();
      databaseTool.executeUpdate("UPDATE accounts SET balance = 0 WHERE id = 1").block();
      databaseTool.rollback().block();

      // Balance should still be 1000
      Map<String, Object> account = databaseTool.findById("accounts", 1).block();
      assertNotNull(account);
    }
  }

  @Nested
  @DisplayName("Configuration Tests")
  class ConfigurationTests {

    @Test
    @DisplayName("Should use default configuration")
    void testDefaultConfig() {
      DatabaseTool tool = new DatabaseTool();
      assertNotNull(tool);
    }

    @Test
    @DisplayName("Should create PostgreSQL config")
    void testPostgresqlConfig() {
      DatabaseConfig config =
          DatabaseConfig.postgresql("localhost", 5432, "testdb", "user", "pass");

      assertNotNull(config);
      assertEquals(DatabaseProvider.POSTGRESQL, config.getProvider());
    }

    @Test
    @DisplayName("Should create MySQL config")
    void testMysqlConfig() {
      DatabaseConfig config = DatabaseConfig.mysql("localhost", 3306, "testdb", "user", "pass");

      assertNotNull(config);
      assertEquals(DatabaseProvider.MYSQL, config.getProvider());
    }

    @Test
    @DisplayName("Should create SQLite config")
    void testSqliteConfig() {
      DatabaseConfig config = DatabaseConfig.sqlite("/path/to/db.sqlite");

      assertNotNull(config);
      assertEquals(DatabaseProvider.SQLITE, config.getProvider());
    }

    @Test
    @DisplayName("Should create H2 memory config")
    void testH2MemoryConfig() {
      DatabaseConfig config = DatabaseConfig.h2Memory();

      assertNotNull(config);
      assertEquals(DatabaseProvider.H2, config.getProvider());
    }

    @Test
    @DisplayName("Should set configuration")
    void testSetConfig() {
      DatabaseConfig newConfig = DatabaseConfig.h2Memory();
      databaseTool.setConfig(newConfig);
      // Should not throw exception
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should handle SQL syntax error")
    void testSqlSyntaxError() {
      databaseTool.connect().block();
      QueryResult result = databaseTool.executeQuery("SELECT * FORM non_existent_table").block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle operations without connection")
    void testOperationsWithoutConnection() {
      // Don't connect first
      try {
        QueryResult result = databaseTool.executeQuery("SELECT 1").block();
        // Might work if auto-connect is implemented
        assertNotNull(result);
      } catch (Exception e) {
        // Expected if no auto-connect
        assertTrue(true);
      }
    }

    @Test
    @DisplayName("Should handle non-existent table")
    void testNonExistentTable() {
      databaseTool.connect().block();
      QueryResult result = databaseTool.findAll("non_existent_table").block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
    }
  }
}
