package dev.engineeringlab.adentic.tool.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.engineeringlab.adentic.tool.database.config.DatabaseConfig;
import dev.engineeringlab.adentic.tool.database.model.QueryResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

/**
 * Tests for H2DatabaseProvider demonstrating:
 *
 * <ul>
 *   <li>✅ Real JDBC connections
 *   <li>✅ PreparedStatement for SQL injection prevention
 *   <li>✅ CRUD operations
 *   <li>✅ Transaction support
 *   <li>✅ Schema introspection
 * </ul>
 */
class H2DatabaseProviderTest {

  private DatabaseTool db;

  @BeforeEach
  void setUp() {
    // Use H2 in-memory database (default)
    db = new DatabaseTool(DatabaseConfig.h2Memory());
    db.connect().block();

    // Create test table
    db.executeUpdate(
            "CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), age INT, email VARCHAR(255))")
        .block();
  }

  @AfterEach
  void tearDown() {
    if (db != null && db.isConnected()) {
      // Drop all tables to ensure test isolation
      try {
        db.executeUpdate("DROP ALL OBJECTS").block();
      } catch (Exception e) {
        // Ignore errors during cleanup
      }
      db.disconnect().block();
    }
  }

  @Test
  void testConnection() {
    // Test connection is established
    assertTrue(db.isConnected());
    assertTrue(db.testConnection().block());

    // Test connection info
    assertNotNull(db.getConnectionInfo());
    assertTrue(db.getConnectionInfo().isConnected());
    assertTrue(db.getConnectionInfo().getJdbcUrl().contains("jdbc:h2:mem:"));
  }

  @Test
  void testPreparedStatementWithParameters() {
    // Insert test data
    db.executeUpdate(
            "INSERT INTO users (name, age, email) VALUES (?, ?, ?)",
            "Alice",
            25,
            "alice@example.com")
        .block();
    db.executeUpdate(
            "INSERT INTO users (name, age, email) VALUES (?, ?, ?)", "Bob", 30, "bob@example.com")
        .block();
    db.executeUpdate(
            "INSERT INTO users (name, age, email) VALUES (?, ?, ?)",
            "Charlie",
            35,
            "charlie@example.com")
        .block();

    // ✅ SQL INJECTION PREVENTION: This query uses PreparedStatement
    QueryResult result = db.executeQuery("SELECT * FROM users WHERE age > ?", 26).block();

    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals(2, result.getRowCount()); // Bob (30) and Charlie (35)

    // Verify results
    assertThat(result.getRows())
        .extracting(row -> row.get("NAME"))
        .containsExactlyInAnyOrder("Bob", "Charlie");
  }

  @Test
  void testSqlInjectionPrevention() {
    // Insert test data
    db.executeUpdate(
            "INSERT INTO users (name, age, email) VALUES (?, ?, ?)",
            "Admin",
            40,
            "admin@example.com")
        .block();

    // Attempt SQL injection (would be dangerous with string concatenation)
    String maliciousInput = "25 OR 1=1"; // This would bypass WHERE clause if concatenated

    // ✅ SAFE: PreparedStatement treats this as a literal value, not SQL code
    QueryResult result =
        db.executeQuery("SELECT * FROM users WHERE age > ?", maliciousInput).block();

    // The malicious input is safely treated as a string value, not SQL
    // H2 will try to convert "25 OR 1=1" to integer and fail gracefully
    assertNotNull(result);
    assertFalse(result.isSuccess()); // Should fail due to type mismatch
  }

  @Test
  void testInsertAndRetrieve() {
    // Insert using DatabaseTool convenience method
    Object userId =
        db.insert("users", Map.of("name", "David", "age", 28, "email", "david@example.com"))
            .block();

    assertNotNull(userId);

    // Retrieve by ID
    Map<String, Object> user = db.findById("users", userId).block();

    assertNotNull(user);
    assertEquals("David", user.get("NAME"));
    assertEquals(28, user.get("AGE"));
    assertEquals("david@example.com", user.get("EMAIL"));
  }

  @Test
  void testBatchInsert() {
    // Batch insert multiple records
    List<Map<String, Object>> users =
        List.of(
            Map.of("name", "User1", "age", 20, "email", "user1@example.com"),
            Map.of("name", "User2", "age", 21, "email", "user2@example.com"),
            Map.of("name", "User3", "age", 22, "email", "user3@example.com"));

    Long insertedCount = db.insertBatch("users", users).block();

    assertEquals(3L, insertedCount);

    // Verify all inserted
    QueryResult result = db.findAll("users").block();
    assertEquals(3, result.getRowCount());
  }

  @Test
  void testUpdateOperations() {
    // Insert test user
    Object userId =
        db.insert("users", Map.of("name", "Eve", "age", 30, "email", "eve@example.com")).block();

    // Update by ID
    Boolean updated = db.update("users", userId, Map.of("age", 31)).block();
    assertTrue(updated);

    // Verify update
    Map<String, Object> user = db.findById("users", userId).block();
    assertEquals(31, user.get("AGE"));

    // Update where
    Long updatedCount =
        db.updateWhere("users", Map.of("name", "Eve"), Map.of("email", "eve.updated@example.com"))
            .block();
    assertEquals(1L, updatedCount);
  }

  @Test
  void testDeleteOperations() {
    // Insert test users
    Object userId1 =
        db.insert("users", Map.of("name", "Frank", "age", 25, "email", "frank@example.com"))
            .block();
    db.insert("users", Map.of("name", "Grace", "age", 26, "email", "grace@example.com")).block();

    // Delete by ID
    Boolean deleted = db.delete("users", userId1).block();
    assertTrue(deleted);

    // Verify deletion
    Map<String, Object> user = db.findById("users", userId1).block();
    assertTrue(user.isEmpty());

    // Delete where
    Long deletedCount = db.deleteWhere("users", Map.of("name", "Grace")).block();
    assertEquals(1L, deletedCount);

    // Verify all deleted
    Long count = db.count("users").block();
    assertEquals(0L, count);
  }

  @Test
  void testFindOperations() {
    // Insert test data
    db.insertBatch(
            "users",
            List.of(
                Map.of("name", "Alice", "age", 25, "email", "alice@example.com"),
                Map.of("name", "Bob", "age", 30, "email", "bob@example.com"),
                Map.of("name", "Charlie", "age", 25, "email", "charlie@example.com")))
        .block();

    // Find all
    QueryResult all = db.findAll("users").block();
    assertEquals(3, all.getRowCount());

    // Find where
    QueryResult aged25 = db.findWhere("users", Map.of("age", 25)).block();
    assertEquals(2, aged25.getRowCount());

    // Count
    Long count = db.count("users").block();
    assertEquals(3L, count);

    // Count where
    Long count25 = db.countWhere("users", Map.of("age", 25)).block();
    assertEquals(2L, count25);
  }

  @Test
  void testTransactionCommit() {
    // Execute transaction
    var result =
        db.executeTransaction(
                provider -> {
                  provider
                      .insert(
                          "users",
                          Map.of("name", "TxUser1", "age", 40, "email", "txuser1@example.com"))
                      .block();
                  provider
                      .insert(
                          "users",
                          Map.of("name", "TxUser2", "age", 41, "email", "txuser2@example.com"))
                      .block();
                  return Mono.empty();
                })
            .block();

    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertTrue(result.isCommitted());
    assertFalse(result.isRolledBack());

    // Verify both records inserted
    Long count = db.count("users").block();
    assertEquals(2L, count);
  }

  @Test
  void testTransactionRollback() {
    // Execute transaction that throws exception
    var result =
        db.executeTransaction(
                provider -> {
                  provider
                      .insert(
                          "users",
                          Map.of("name", "TxUser1", "age", 40, "email", "txuser1@example.com"))
                      .block();
                  // Simulate error
                  throw new RuntimeException("Transaction failed!");
                })
            .block();

    assertNotNull(result);
    assertFalse(result.isSuccess());
    assertFalse(result.isCommitted());
    assertTrue(result.isRolledBack());

    // Verify no records inserted (rolled back)
    Long count = db.count("users").block();
    assertEquals(0L, count);
  }

  @Test
  void testSchemaIntrospection() {
    // List tables
    List<String> tables = db.listTables().block();
    assertThat(tables).contains("USERS");

    // Table exists
    Boolean exists = db.tableExists("USERS").block();
    assertTrue(exists);

    // Get schema
    Map<String, Object> schema = db.getSchema("USERS").block();
    assertNotNull(schema);
    assertEquals("USERS", schema.get("table"));

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> columns = (List<Map<String, Object>>) schema.get("columns");
    assertThat(columns).hasSize(4); // id, name, age, email

    // Verify column names
    assertThat(columns)
        .extracting(col -> col.get("name"))
        .containsExactlyInAnyOrder("ID", "NAME", "AGE", "EMAIL");
  }

  @Test
  void testQueryMetadata() {
    // Insert test data
    db.insert("users", Map.of("name", "MetaTest", "age", 35, "email", "meta@example.com")).block();

    // Execute query and check metadata
    QueryResult result = db.executeQuery("SELECT * FROM users WHERE name = ?", "MetaTest").block();

    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getRowCount());

    // Check column metadata
    assertThat(result.getColumns()).isNotEmpty();
    assertThat(result.getColumns())
        .extracting(col -> col.getName())
        .contains("ID", "NAME", "AGE", "EMAIL");

    // Check execution time
    assertTrue(result.getExecutionTimeMs() >= 0);

    // Check query is recorded
    assertNotNull(result.getQuery());
    assertTrue(result.getQuery().contains("SELECT"));
  }
}
