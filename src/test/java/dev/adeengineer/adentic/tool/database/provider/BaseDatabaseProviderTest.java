package dev.adeengineer.adentic.tool.database.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import dev.adeengineer.adentic.tool.database.config.DatabaseConfig;
import dev.adeengineer.adentic.tool.database.model.ConnectionInfo;
import dev.adeengineer.adentic.tool.database.model.DatabaseType;
import dev.adeengineer.adentic.tool.database.model.QueryResult;
import dev.adeengineer.adentic.tool.database.model.TransactionResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

/**
 * Tests for BaseDatabaseProvider functionality.
 *
 * <p>Tests the base functionality provided to all database providers: - Configuration management -
 * Connection state tracking - Helper methods for result creation - Validation utilities - Logging
 * and sanitization
 */
@DisplayName("BaseDatabaseProvider Tests")
class BaseDatabaseProviderTest {

  @Nested
  @DisplayName("Constructor and Initialization")
  class ConstructorAndInitializationTests {

    @Test
    @DisplayName("Should initialize with valid configuration")
    void shouldInitializeWithValidConfiguration() {
      // Given
      DatabaseConfig config = DatabaseConfig.h2Memory();

      // When
      TestDatabaseProvider provider = new TestDatabaseProvider(config);

      // Then
      assertNotNull(provider);
      assertNotNull(provider.getConnectionInfo());
      assertFalse(provider.isConnected());
      assertEquals("h2", provider.getProviderName());
    }

    @Test
    @DisplayName("Should build connection info from configuration")
    void shouldBuildConnectionInfoFromConfiguration() {
      // Given
      DatabaseConfig config =
          DatabaseConfig.builder()
              .host("testhost")
              .port(5432)
              .database("testdb")
              .username("testuser")
              .password("testpass")
              .poolSize(20)
              .connectionTimeout(5000)
              .useSSL(true)
              .validateSSL(false)
              .build();

      // When
      TestDatabaseProvider provider = new TestDatabaseProvider(config);
      ConnectionInfo connectionInfo = provider.getConnectionInfo();

      // Then
      assertNotNull(connectionInfo);
      assertEquals("testhost", connectionInfo.getHost());
      assertEquals(5432, connectionInfo.getPort());
      assertEquals("testdb", connectionInfo.getDatabase());
      assertEquals("testuser", connectionInfo.getUsername());
      assertEquals("testpass", connectionInfo.getPassword());
      assertEquals(20, connectionInfo.getPoolSize());
      assertEquals(5000, connectionInfo.getConnectionTimeout());
      assertTrue(connectionInfo.isUseSSL());
      assertFalse(connectionInfo.isValidateSSL());
      assertFalse(connectionInfo.isConnected());
    }
  }

  @Nested
  @DisplayName("Connection State Management")
  class ConnectionStateManagementTests {

    @Test
    @DisplayName("Should track connected state")
    void shouldTrackConnectedState() {
      // Given
      TestDatabaseProvider provider = new TestDatabaseProvider(DatabaseConfig.h2Memory());

      // When - Initially not connected
      assertFalse(provider.isConnected());

      // When - Set connected
      provider.setConnectedInternal(true);

      // Then
      assertTrue(provider.isConnected());
    }

    @Test
    @DisplayName("Should update connection info state")
    void shouldUpdateConnectionInfoState() {
      // Given
      TestDatabaseProvider provider = new TestDatabaseProvider(DatabaseConfig.h2Memory());
      ConnectionInfo info = provider.getConnectionInfo();

      // When
      info.setConnected(true);
      info.setJdbcUrl("jdbc:h2:mem:test");

      // Then
      assertTrue(info.isConnected());
      assertEquals("jdbc:h2:mem:test", info.getJdbcUrl());
    }
  }

  @Nested
  @DisplayName("Result Creation Helpers")
  class ResultCreationHelpersTests {

    @Test
    @DisplayName("Should create successful query result")
    void shouldCreateSuccessfulQueryResult() {
      // Given
      TestDatabaseProvider provider = new TestDatabaseProvider(DatabaseConfig.h2Memory());
      List<Map<String, Object>> rows =
          List.of(Map.of("id", 1, "name", "Alice"), Map.of("id", 2, "name", "Bob"));
      long executionTime = 150L;

      // When
      QueryResult result = provider.createSuccessResult(rows, executionTime);

      // Then
      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(2, result.getRowCount());
      assertEquals(rows, result.getRows());
      assertEquals(150L, result.getExecutionTimeMs());
    }

    @Test
    @DisplayName("Should create successful update result")
    void shouldCreateSuccessfulUpdateResult() {
      // Given
      TestDatabaseProvider provider = new TestDatabaseProvider(DatabaseConfig.h2Memory());

      // When
      QueryResult result = provider.createUpdateResult(5L, 250L);

      // Then
      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(5L, result.getAffectedRows());
      assertEquals(250L, result.getExecutionTimeMs());
    }

    @Test
    @DisplayName("Should create failure result with error message")
    void shouldCreateFailureResultWithErrorMessage() {
      // Given
      TestDatabaseProvider provider = new TestDatabaseProvider(DatabaseConfig.h2Memory());
      String query = "SELECT * FROM non_existent_table";
      String error = "Table not found";

      // When
      QueryResult result = provider.createFailureResult(query, error);

      // Then
      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertEquals(query, result.getQuery());
      assertEquals(error, result.getErrorMessage());
      assertNull(result.getSqlState());
    }

    @Test
    @DisplayName("Should create failure result with SQL state")
    void shouldCreateFailureResultWithSqlState() {
      // Given
      TestDatabaseProvider provider = new TestDatabaseProvider(DatabaseConfig.h2Memory());
      String query = "INSERT INTO users VALUES (1)";
      String error = "Constraint violation";
      String sqlState = "23505";

      // When
      QueryResult result = provider.createFailureResult(query, error, sqlState);

      // Then
      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertEquals(query, result.getQuery());
      assertEquals(error, result.getErrorMessage());
      assertEquals(sqlState, result.getSqlState());
    }
  }

  @Nested
  @DisplayName("Validation Methods")
  class ValidationMethodsTests {

    @Test
    @DisplayName("Should validate valid table names")
    void shouldValidateValidTableNames() {
      // Given
      TestDatabaseProvider provider = new TestDatabaseProvider(DatabaseConfig.h2Memory());

      // When/Then - Should not throw
      assertDoesNotThrow(() -> provider.validateTableName("users"));
      assertDoesNotThrow(() -> provider.validateTableName("order_items"));
      assertDoesNotThrow(() -> provider.validateTableName("Table123"));
    }

    @Test
    @DisplayName("Should reject null or empty table names")
    void shouldRejectNullOrEmptyTableNames() {
      // Given
      TestDatabaseProvider provider = new TestDatabaseProvider(DatabaseConfig.h2Memory());

      // When/Then
      assertThatThrownBy(() -> provider.validateTableName(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("cannot be null or empty");

      assertThatThrownBy(() -> provider.validateTableName(""))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("cannot be null or empty");

      assertThatThrownBy(() -> provider.validateTableName("   "))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("cannot be null or empty");
    }

    @Test
    @DisplayName("Should reject table names with SQL injection attempts")
    void shouldRejectTableNamesWithSqlInjection() {
      // Given
      TestDatabaseProvider provider = new TestDatabaseProvider(DatabaseConfig.h2Memory());

      // When/Then - Semicolon
      assertThatThrownBy(() -> provider.validateTableName("users; DROP TABLE users"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Invalid table name");

      // When/Then - Comment
      assertThatThrownBy(() -> provider.validateTableName("users--"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Invalid table name");

      // When/Then - Block comment
      assertThatThrownBy(() -> provider.validateTableName("users/**/"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Invalid table name");
    }

    @Test
    @DisplayName("Should validate valid column names")
    void shouldValidateValidColumnNames() {
      // Given
      TestDatabaseProvider provider = new TestDatabaseProvider(DatabaseConfig.h2Memory());

      // When/Then - Should not throw
      assertDoesNotThrow(() -> provider.validateColumnName("id"));
      assertDoesNotThrow(() -> provider.validateColumnName("user_name"));
      assertDoesNotThrow(() -> provider.validateColumnName("Column123"));
    }

    @Test
    @DisplayName("Should reject null or empty column names")
    void shouldRejectNullOrEmptyColumnNames() {
      // Given
      TestDatabaseProvider provider = new TestDatabaseProvider(DatabaseConfig.h2Memory());

      // When/Then
      assertThatThrownBy(() -> provider.validateColumnName(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("cannot be null or empty");

      assertThatThrownBy(() -> provider.validateColumnName(""))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("cannot be null or empty");
    }

    @Test
    @DisplayName("Should reject column names with SQL injection attempts")
    void shouldRejectColumnNamesWithSqlInjection() {
      // Given
      TestDatabaseProvider provider = new TestDatabaseProvider(DatabaseConfig.h2Memory());

      // When/Then
      assertThatThrownBy(() -> provider.validateColumnName("id; DROP TABLE"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Invalid column name");
    }
  }

  @Nested
  @DisplayName("Sanitization and Logging")
  class SanitizationAndLoggingTests {

    @Test
    @DisplayName("Should sanitize sensitive parameter values")
    void shouldSanitizeSensitiveParameterValues() {
      // Given
      TestDatabaseProvider provider = new TestDatabaseProvider(DatabaseConfig.h2Memory());

      // When/Then - Password should be masked
      assertEquals("***", provider.sanitizeForLogging("password", "secret123"));
      assertEquals("***", provider.sanitizeForLogging("user_password", "mypass"));
      assertEquals("***", provider.sanitizeForLogging("PASSWORD", "admin"));

      // When/Then - Secret should be masked
      assertEquals("***", provider.sanitizeForLogging("api_secret", "key123"));
      assertEquals("***", provider.sanitizeForLogging("SECRET_KEY", "token"));

      // When/Then - Token should be masked
      assertEquals("***", provider.sanitizeForLogging("access_token", "bearer_xyz"));
      assertEquals("***", provider.sanitizeForLogging("TOKEN", "jwt_abc"));
    }

    @Test
    @DisplayName("Should not sanitize non-sensitive values")
    void shouldNotSanitizeNonSensitiveValues() {
      // Given
      TestDatabaseProvider provider = new TestDatabaseProvider(DatabaseConfig.h2Memory());

      // When/Then
      assertEquals("Alice", provider.sanitizeForLogging("username", "Alice"));
      assertEquals("25", provider.sanitizeForLogging("age", 25));
      assertEquals("alice@example.com", provider.sanitizeForLogging("email", "alice@example.com"));
    }

    @Test
    @DisplayName("Should format query with parameters for logging")
    void shouldFormatQueryWithParametersForLogging() {
      // Given
      TestDatabaseProvider provider = new TestDatabaseProvider(DatabaseConfig.h2Memory());
      String query = "SELECT * FROM users WHERE age > ? AND name = ?";
      Map<String, Object> params = new HashMap<>();
      params.put("age", 25);
      params.put("name", "Alice");

      // When
      String formatted = provider.formatQueryForLogging(query, params);

      // Then
      assertNotNull(formatted);
      assertThat(formatted).contains("SELECT * FROM users");
      assertThat(formatted).contains("age=25");
      assertThat(formatted).contains("name=Alice");
    }

    @Test
    @DisplayName("Should format query without parameters for logging")
    void shouldFormatQueryWithoutParametersForLogging() {
      // Given
      TestDatabaseProvider provider = new TestDatabaseProvider(DatabaseConfig.h2Memory());
      String query = "SELECT * FROM users";

      // When
      String formatted = provider.formatQueryForLogging(query, null);

      // Then
      assertEquals(query, formatted);

      // When - Empty parameters
      formatted = provider.formatQueryForLogging(query, Map.of());

      // Then
      assertEquals(query, formatted);
    }

    @Test
    @DisplayName("Should sanitize sensitive parameters in formatted query")
    void shouldSanitizeSensitiveParametersInFormattedQuery() {
      // Given
      TestDatabaseProvider provider = new TestDatabaseProvider(DatabaseConfig.h2Memory());
      String query = "INSERT INTO users (username, password) VALUES (?, ?)";
      Map<String, Object> params = new HashMap<>();
      params.put("username", "alice");
      params.put("password", "secret123");

      // When
      String formatted = provider.formatQueryForLogging(query, params);

      // Then
      assertThat(formatted).contains("username=alice");
      assertThat(formatted).contains("password=***");
      assertThat(formatted).doesNotContain("secret123");
    }
  }

  @Nested
  @DisplayName("Configuration Access")
  class ConfigurationAccessTests {

    @Test
    @DisplayName("Should provide access to query logging setting")
    void shouldProvideAccessToQueryLoggingSetting() {
      // Given - Enabled
      DatabaseConfig enabledConfig = DatabaseConfig.builder().enableQueryLogging(true).build();
      TestDatabaseProvider enabledProvider = new TestDatabaseProvider(enabledConfig);

      // Then
      assertTrue(enabledProvider.isQueryLoggingEnabled());

      // Given - Disabled
      DatabaseConfig disabledConfig = DatabaseConfig.builder().enableQueryLogging(false).build();
      TestDatabaseProvider disabledProvider = new TestDatabaseProvider(disabledConfig);

      // Then
      assertFalse(disabledProvider.isQueryLoggingEnabled());
    }

    @Test
    @DisplayName("Should provide access to connection timeout")
    void shouldProvideAccessToConnectionTimeout() {
      // Given
      DatabaseConfig config = DatabaseConfig.builder().connectionTimeout(15000).build();
      TestDatabaseProvider provider = new TestDatabaseProvider(config);

      // When/Then
      assertEquals(15000, provider.getConnectionTimeout());
    }

    @Test
    @DisplayName("Should provide access to query timeout")
    void shouldProvideAccessToQueryTimeout() {
      // Given
      DatabaseConfig config = DatabaseConfig.builder().queryTimeout(30000).build();
      TestDatabaseProvider provider = new TestDatabaseProvider(config);

      // When/Then
      assertEquals(30000, provider.getQueryTimeout());
    }
  }

  // ========== TEST IMPLEMENTATION OF BaseDatabaseProvider ==========

  /**
   * Test implementation of BaseDatabaseProvider for testing base class functionality.
   *
   * <p>This is a minimal stub implementation that exposes protected methods for testing.
   */
  private static class TestDatabaseProvider extends BaseDatabaseProvider {

    public TestDatabaseProvider(DatabaseConfig config) {
      super(config);
    }

    @Override
    public Mono<Void> connect() {
      setConnectedInternal(true);
      getConnectionInfo().setConnected(true);
      return Mono.empty();
    }

    @Override
    public Mono<Void> disconnect() {
      setConnectedInternal(false);
      getConnectionInfo().setConnected(false);
      return Mono.empty();
    }

    @Override
    public Mono<Boolean> testConnection() {
      return Mono.just(isConnected());
    }

    @Override
    public Mono<QueryResult> executeQuery(String query) {
      return Mono.just(createSuccessResult(List.of(), 0));
    }

    @Override
    public Mono<QueryResult> executeQuery(String query, Map<String, Object> parameters) {
      return Mono.just(createSuccessResult(List.of(), 0));
    }

    @Override
    public Mono<QueryResult> executeQuery(String query, Object... parameters) {
      return Mono.just(createSuccessResult(List.of(), 0));
    }

    @Override
    public Mono<Long> executeUpdate(String statement) {
      return Mono.just(0L);
    }

    @Override
    public Mono<Long> executeUpdate(String statement, Map<String, Object> parameters) {
      return Mono.just(0L);
    }

    @Override
    public Mono<Long> executeUpdate(String statement, Object... parameters) {
      return Mono.just(0L);
    }

    @Override
    public Mono<Object> insert(String table, Map<String, Object> data) {
      return Mono.just(1);
    }

    @Override
    public Mono<Long> insertBatch(String table, List<Map<String, Object>> records) {
      return Mono.just((long) records.size());
    }

    @Override
    public Mono<Boolean> delete(String table, Object id) {
      return Mono.just(true);
    }

    @Override
    public Mono<Long> deleteWhere(String table, Map<String, Object> criteria) {
      return Mono.just(0L);
    }

    @Override
    public Mono<Boolean> update(String table, Object id, Map<String, Object> updates) {
      return Mono.just(true);
    }

    @Override
    public Mono<Long> updateWhere(
        String table, Map<String, Object> criteria, Map<String, Object> updates) {
      return Mono.just(0L);
    }

    @Override
    public Mono<Map<String, Object>> findById(String table, Object id) {
      return Mono.just(Map.of());
    }

    @Override
    public Mono<QueryResult> findAll(String table) {
      return Mono.just(createSuccessResult(List.of(), 0));
    }

    @Override
    public Mono<QueryResult> findWhere(String table, Map<String, Object> criteria) {
      return Mono.just(createSuccessResult(List.of(), 0));
    }

    @Override
    public Mono<Long> count(String table) {
      return Mono.just(0L);
    }

    @Override
    public Mono<Long> countWhere(String table, Map<String, Object> criteria) {
      return Mono.just(0L);
    }

    @Override
    public Mono<TransactionResult> executeTransaction(
        Function<DatabaseProvider, Mono<Void>> operations) {
      return Mono.just(TransactionResult.builder().success(true).committed(true).build());
    }

    @Override
    public Mono<Void> beginTransaction() {
      return Mono.empty();
    }

    @Override
    public Mono<Void> commit() {
      return Mono.empty();
    }

    @Override
    public Mono<Void> rollback() {
      return Mono.empty();
    }

    @Override
    public Mono<List<String>> listTables() {
      return Mono.just(List.of());
    }

    @Override
    public Mono<Map<String, Object>> getSchema(String table) {
      return Mono.just(Map.of());
    }

    @Override
    public Mono<Boolean> tableExists(String table) {
      return Mono.just(false);
    }

    @Override
    public String getProviderName() {
      return "h2";
    }

    @Override
    public DatabaseType getDatabaseType() {
      return DatabaseType.IN_MEMORY;
    }
  }
}
