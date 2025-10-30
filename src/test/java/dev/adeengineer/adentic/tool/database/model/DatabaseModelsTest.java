package dev.adeengineer.adentic.tool.database.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Comprehensive tests for database model classes. */
@DisplayName("Database Models Tests")
class DatabaseModelsTest {

  @Nested
  @DisplayName("DatabaseType Tests")
  class DatabaseTypeTests {

    @Test
    @DisplayName("Should have all database types")
    void testDatabaseTypes() {
      DatabaseType[] types = DatabaseType.values();
      assertEquals(4, types.length);
      assertNotNull(DatabaseType.valueOf("SQL"));
      assertNotNull(DatabaseType.valueOf("NOSQL"));
      assertNotNull(DatabaseType.valueOf("IN_MEMORY"));
      assertNotNull(DatabaseType.valueOf("CUSTOM"));
    }

    @Test
    @DisplayName("Should throw exception for invalid type")
    void testInvalidType() {
      assertThrows(IllegalArgumentException.class, () -> DatabaseType.valueOf("INVALID"));
    }
  }

  @Nested
  @DisplayName("DatabaseProvider Tests")
  class DatabaseProviderTests {

    @Test
    @DisplayName("Should have all database providers")
    void testDatabaseProviders() {
      DatabaseProvider[] providers = DatabaseProvider.values();
      assertEquals(6, providers.length);
      assertNotNull(DatabaseProvider.valueOf("POSTGRESQL"));
      assertNotNull(DatabaseProvider.valueOf("MYSQL"));
      assertNotNull(DatabaseProvider.valueOf("SQLITE"));
      assertNotNull(DatabaseProvider.valueOf("MONGODB"));
      assertNotNull(DatabaseProvider.valueOf("H2"));
      assertNotNull(DatabaseProvider.valueOf("CUSTOM"));
    }

    @Test
    @DisplayName("Should throw exception for invalid provider")
    void testInvalidProvider() {
      assertThrows(IllegalArgumentException.class, () -> DatabaseProvider.valueOf("INVALID"));
    }

    @Test
    @DisplayName("Should return correct name")
    void testProviderName() {
      assertEquals("POSTGRESQL", DatabaseProvider.POSTGRESQL.name());
      assertEquals("MONGODB", DatabaseProvider.MONGODB.name());
    }
  }

  @Nested
  @DisplayName("ConnectionInfo Tests")
  class ConnectionInfoTests {

    @Test
    @DisplayName("Should create connection info with builder")
    void testBuilder() {
      Map<String, String> props = Map.of("option", "value");

      ConnectionInfo info =
          ConnectionInfo.builder()
              .provider(DatabaseProvider.POSTGRESQL)
              .host("localhost")
              .port(5432)
              .database("testdb")
              .username("user")
              .password("pass")
              .jdbcUrl("jdbc:postgresql://localhost:5432/testdb")
              .poolSize(20)
              .connectionTimeout(60000)
              .useSSL(true)
              .validateSSL(false)
              .connected(true)
              .properties(props)
              .build();

      assertEquals(DatabaseProvider.POSTGRESQL, info.getProvider());
      assertEquals("localhost", info.getHost());
      assertEquals(5432, info.getPort());
      assertEquals("testdb", info.getDatabase());
      assertEquals("user", info.getUsername());
      assertEquals("pass", info.getPassword());
      assertEquals("jdbc:postgresql://localhost:5432/testdb", info.getJdbcUrl());
      assertEquals(20, info.getPoolSize());
      assertEquals(60000, info.getConnectionTimeout());
      assertTrue(info.isUseSSL());
      assertFalse(info.isValidateSSL());
      assertTrue(info.isConnected());
      assertEquals(props, info.getProperties());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      ConnectionInfo info =
          ConnectionInfo.builder().provider(DatabaseProvider.H2).database("test").build();

      assertEquals(10, info.getPoolSize());
      assertEquals(30000, info.getConnectionTimeout());
      assertFalse(info.isUseSSL());
      assertTrue(info.isValidateSSL());
      assertFalse(info.isConnected());
    }

    @Test
    @DisplayName("Should support MongoDB URI")
    void testMongoUri() {
      ConnectionInfo info =
          ConnectionInfo.builder()
              .provider(DatabaseProvider.MONGODB)
              .mongoUri("mongodb://localhost:27017/testdb")
              .build();

      assertEquals("mongodb://localhost:27017/testdb", info.getMongoUri());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      ConnectionInfo info = ConnectionInfo.builder().provider(DatabaseProvider.H2).build();

      info.setHost("newhost");
      info.setPort(3306);
      info.setDatabase("newdb");
      info.setUsername("newuser");
      info.setPassword("newpass");
      info.setPoolSize(50);
      info.setConnected(true);

      assertEquals("newhost", info.getHost());
      assertEquals(3306, info.getPort());
      assertEquals("newdb", info.getDatabase());
      assertEquals("newuser", info.getUsername());
      assertEquals("newpass", info.getPassword());
      assertEquals(50, info.getPoolSize());
      assertTrue(info.isConnected());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      ConnectionInfo info1 =
          ConnectionInfo.builder()
              .provider(DatabaseProvider.H2)
              .database("test")
              .jdbcUrl("jdbc:h2:mem:test")
              .build();

      ConnectionInfo info2 =
          ConnectionInfo.builder()
              .provider(DatabaseProvider.H2)
              .database("test")
              .jdbcUrl("jdbc:h2:mem:test")
              .build();

      assertEquals(info1, info2);
      assertEquals(info1.hashCode(), info2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      ConnectionInfo info =
          ConnectionInfo.builder().provider(DatabaseProvider.H2).database("test").build();
      String str = info.toString();

      assertTrue(str.contains("H2"));
      assertTrue(str.contains("test"));
    }
  }

  @Nested
  @DisplayName("QueryResult Tests")
  class QueryResultTests {

    @Test
    @DisplayName("Should create query result with builder")
    void testBuilder() {
      List<Map<String, Object>> rows = List.of(Map.of("id", 1, "name", "test"));
      List<Object> keys = List.of(1, 2, 3);
      Map<String, Object> params = Map.of("limit", 10);

      QueryResult.ColumnMetadata col =
          QueryResult.ColumnMetadata.builder()
              .name("id")
              .type("INTEGER")
              .nullable(false)
              .size(11)
              .precision(10)
              .scale(0)
              .build();

      List<QueryResult.ColumnMetadata> columns = List.of(col);

      QueryResult result =
          QueryResult.builder()
              .success(true)
              .rows(rows)
              .rowCount(1)
              .affectedRows(5)
              .generatedKeys(keys)
              .columns(columns)
              .executionTimeMs(100)
              .errorMessage(null)
              .sqlState(null)
              .query("SELECT * FROM test")
              .parameters(params)
              .hasMore(true)
              .build();

      assertTrue(result.isSuccess());
      assertEquals(rows, result.getRows());
      assertEquals(1, result.getRowCount());
      assertEquals(5, result.getAffectedRows());
      assertEquals(keys, result.getGeneratedKeys());
      assertEquals(columns, result.getColumns());
      assertEquals(100, result.getExecutionTimeMs());
      assertNull(result.getErrorMessage());
      assertNull(result.getSqlState());
      assertEquals("SELECT * FROM test", result.getQuery());
      assertEquals(params, result.getParameters());
      assertTrue(result.isHasMore());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      QueryResult result = QueryResult.builder().query("SELECT 1").build();

      assertFalse(result.isSuccess());
      assertEquals(0, result.getRowCount());
      assertEquals(0, result.getAffectedRows());
      assertEquals(0, result.getExecutionTimeMs());
      assertFalse(result.isHasMore());
    }

    @Test
    @DisplayName("Should support error result")
    void testErrorResult() {
      QueryResult result =
          QueryResult.builder()
              .success(false)
              .errorMessage("Syntax error")
              .sqlState("42000")
              .query("SELECT * FORM test")
              .build();

      assertFalse(result.isSuccess());
      assertEquals("Syntax error", result.getErrorMessage());
      assertEquals("42000", result.getSqlState());
    }

    @Test
    @DisplayName("Should support column metadata")
    void testColumnMetadata() {
      QueryResult.ColumnMetadata col =
          QueryResult.ColumnMetadata.builder()
              .name("email")
              .type("VARCHAR")
              .nullable(true)
              .size(255)
              .precision(0)
              .scale(0)
              .build();

      assertEquals("email", col.getName());
      assertEquals("VARCHAR", col.getType());
      assertTrue(col.isNullable());
      assertEquals(255, col.getSize());
      assertEquals(0, col.getPrecision());
      assertEquals(0, col.getScale());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      QueryResult result = QueryResult.builder().build();

      result.setSuccess(true);
      result.setRowCount(10);
      result.setAffectedRows(5);
      result.setExecutionTimeMs(200);
      result.setHasMore(true);

      assertTrue(result.isSuccess());
      assertEquals(10, result.getRowCount());
      assertEquals(5, result.getAffectedRows());
      assertEquals(200, result.getExecutionTimeMs());
      assertTrue(result.isHasMore());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      QueryResult result1 =
          QueryResult.builder().success(true).query("SELECT 1").rowCount(1).build();

      QueryResult result2 =
          QueryResult.builder().success(true).query("SELECT 1").rowCount(1).build();

      assertEquals(result1, result2);
      assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      QueryResult result = QueryResult.builder().success(true).rowCount(5).build();
      String str = result.toString();

      assertTrue(str.contains("true"));
      assertTrue(str.contains("5"));
    }
  }

  @Nested
  @DisplayName("TransactionResult Tests")
  class TransactionResultTests {

    @Test
    @DisplayName("Should create transaction result with builder")
    void testBuilder() {
      List<QueryResult> opResults =
          List.of(QueryResult.builder().success(true).affectedRows(1).build());

      TransactionResult result =
          TransactionResult.builder()
              .success(true)
              .committed(true)
              .rolledBack(false)
              .operationCount(3)
              .totalAffectedRows(10)
              .executionTimeMs(500)
              .errorMessage(null)
              .operationResults(opResults)
              .transactionId("tx-123")
              .build();

      assertTrue(result.isSuccess());
      assertTrue(result.isCommitted());
      assertFalse(result.isRolledBack());
      assertEquals(3, result.getOperationCount());
      assertEquals(10, result.getTotalAffectedRows());
      assertEquals(500, result.getExecutionTimeMs());
      assertNull(result.getErrorMessage());
      assertEquals(opResults, result.getOperationResults());
      assertEquals("tx-123", result.getTransactionId());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      TransactionResult result = TransactionResult.builder().transactionId("tx-1").build();

      assertFalse(result.isSuccess());
      assertFalse(result.isCommitted());
      assertFalse(result.isRolledBack());
      assertEquals(0, result.getOperationCount());
      assertEquals(0, result.getTotalAffectedRows());
      assertEquals(0, result.getExecutionTimeMs());
    }

    @Test
    @DisplayName("Should support rolled back transaction")
    void testRolledBack() {
      TransactionResult result =
          TransactionResult.builder()
              .success(false)
              .committed(false)
              .rolledBack(true)
              .errorMessage("Constraint violation")
              .build();

      assertFalse(result.isSuccess());
      assertFalse(result.isCommitted());
      assertTrue(result.isRolledBack());
      assertEquals("Constraint violation", result.getErrorMessage());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      TransactionResult result = TransactionResult.builder().build();

      result.setSuccess(true);
      result.setCommitted(true);
      result.setRolledBack(false);
      result.setOperationCount(5);
      result.setTotalAffectedRows(100);
      result.setExecutionTimeMs(1000);

      assertTrue(result.isSuccess());
      assertTrue(result.isCommitted());
      assertFalse(result.isRolledBack());
      assertEquals(5, result.getOperationCount());
      assertEquals(100, result.getTotalAffectedRows());
      assertEquals(1000, result.getExecutionTimeMs());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      TransactionResult result1 =
          TransactionResult.builder().success(true).transactionId("tx-1").build();

      TransactionResult result2 =
          TransactionResult.builder().success(true).transactionId("tx-1").build();

      assertEquals(result1, result2);
      assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      TransactionResult result = TransactionResult.builder().success(true).committed(true).build();
      String str = result.toString();

      assertTrue(str.contains("true"));
    }
  }
}
