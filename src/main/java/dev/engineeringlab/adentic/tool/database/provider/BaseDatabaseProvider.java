package dev.engineeringlab.adentic.tool.database.provider;

import dev.engineeringlab.adentic.tool.database.config.DatabaseConfig;
import dev.engineeringlab.adentic.tool.database.model.ConnectionInfo;
import dev.engineeringlab.adentic.tool.database.model.QueryResult;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for database provider implementations.
 *
 * <p>Provides common functionality for all database providers: - Configuration management -
 * Connection state tracking - Logging utilities - Result creation helpers - Query parameter
 * validation
 */
@Slf4j
public abstract class BaseDatabaseProvider implements DatabaseProvider {

  private final DatabaseConfig config;
  private ConnectionInfo connectionInfo;
  private boolean connected = false;

  protected BaseDatabaseProvider(DatabaseConfig databaseConfig) {
    this.config = databaseConfig;
    this.connectionInfo = buildConnectionInfo(databaseConfig);
  }

  /** Get database configuration. */
  protected DatabaseConfig getConfig() {
    return config;
  }

  /** Set connection information. */
  protected void setConnectionInfo(ConnectionInfo info) {
    this.connectionInfo = info;
  }

  /** Set connected state. */
  protected void setConnectedInternal(boolean isConnected) {
    this.connected = isConnected;
  }

  /**
   * Build connection info from configuration.
   *
   * @param databaseConfig Database configuration
   * @return Connection information
   */
  protected final ConnectionInfo buildConnectionInfo(DatabaseConfig databaseConfig) {
    return ConnectionInfo.builder()
        .provider(databaseConfig.getProvider())
        .host(databaseConfig.getHost())
        .port(databaseConfig.getPort())
        .database(databaseConfig.getDatabase())
        .username(databaseConfig.getUsername())
        .password(databaseConfig.getPassword())
        .poolSize(databaseConfig.getPoolSize())
        .connectionTimeout(databaseConfig.getConnectionTimeout())
        .useSSL(databaseConfig.isUseSSL())
        .validateSSL(databaseConfig.isValidateSSL())
        .connected(false)
        .build();
  }

  @Override
  public boolean isConnected() {
    return connected;
  }

  @Override
  public ConnectionInfo getConnectionInfo() {
    return connectionInfo;
  }

  /**
   * Log a database operation.
   *
   * @param operation Operation name
   * @param details Operation details
   */
  protected void logOperation(String operation, String details) {
    if (config.isEnableQueryLogging()) {
      log.debug("[{}] {} - {}", getProviderName(), operation, details);
    }
  }

  /**
   * Create a successful query result.
   *
   * @param rows Result rows
   * @param executionTimeMs Execution time
   * @return Query result
   */
  protected QueryResult createSuccessResult(
      java.util.List<Map<String, Object>> rows, long executionTimeMs) {
    return QueryResult.builder()
        .success(true)
        .rows(rows)
        .rowCount(rows != null ? rows.size() : 0)
        .executionTimeMs(executionTimeMs)
        .build();
  }

  /**
   * Create a successful update result.
   *
   * @param affectedRows Number of affected rows
   * @param executionTimeMs Execution time
   * @return Query result
   */
  protected QueryResult createUpdateResult(long affectedRows, long executionTimeMs) {
    return QueryResult.builder()
        .success(true)
        .affectedRows(affectedRows)
        .executionTimeMs(executionTimeMs)
        .build();
  }

  /**
   * Create a failed query result.
   *
   * @param query The query that failed
   * @param error Error message
   * @return Query result
   */
  protected QueryResult createFailureResult(String query, String error) {
    log.error("[{}] Query failed: {} - Error: {}", getProviderName(), query, error);
    return QueryResult.builder().success(false).query(query).errorMessage(error).build();
  }

  /**
   * Create a failed query result with SQL state.
   *
   * @param query The query that failed
   * @param error Error message
   * @param sqlState SQL state code
   * @return Query result
   */
  protected QueryResult createFailureResult(String query, String error, String sqlState) {
    log.error(
        "[{}] Query failed: {} - Error: {} (SQLState: {})",
        getProviderName(),
        query,
        error,
        sqlState);
    return QueryResult.builder()
        .success(false)
        .query(query)
        .errorMessage(error)
        .sqlState(sqlState)
        .build();
  }

  /**
   * Validate table name to prevent SQL injection.
   *
   * @param table Table name
   * @throws IllegalArgumentException if table name is invalid
   */
  protected void validateTableName(String table) {
    if (table == null || table.trim().isEmpty()) {
      throw new IllegalArgumentException("Table name cannot be null or empty");
    }

    // Basic SQL injection prevention
    if (table.contains(";") || table.contains("--") || table.contains("/*")) {
      throw new IllegalArgumentException("Invalid table name: " + table);
    }
  }

  /**
   * Validate column name to prevent SQL injection.
   *
   * @param column Column name
   * @throws IllegalArgumentException if column name is invalid
   */
  protected void validateColumnName(String column) {
    if (column == null || column.trim().isEmpty()) {
      throw new IllegalArgumentException("Column name cannot be null or empty");
    }

    // Basic SQL injection prevention
    if (column.contains(";") || column.contains("--") || column.contains("/*")) {
      throw new IllegalArgumentException("Invalid column name: " + column);
    }
  }

  /**
   * Sanitize a value for logging (mask passwords, etc.).
   *
   * @param key Parameter key
   * @param value Parameter value
   * @return Sanitized value
   */
  protected String sanitizeForLogging(String key, Object value) {
    if (key != null
        && (key.toLowerCase().contains("password")
            || key.toLowerCase().contains("secret")
            || key.toLowerCase().contains("token"))) {
      return "***";
    }
    return String.valueOf(value);
  }

  /**
   * Build a parameterized query string for logging.
   *
   * @param query Query template
   * @param parameters Parameters
   * @return Formatted query string
   */
  protected String formatQueryForLogging(String query, Map<String, Object> parameters) {
    if (parameters == null || parameters.isEmpty()) {
      return query;
    }

    StringBuilder sb = new StringBuilder(query);
    sb.append(" [Parameters: ");
    parameters.forEach(
        (k, v) -> sb.append(k).append("=").append(sanitizeForLogging(k, v)).append(", "));
    sb.setLength(sb.length() - 2); // Remove trailing ", "
    sb.append("]");
    return sb.toString();
  }

  /**
   * Check if query logging is enabled.
   *
   * @return true if query logging is enabled
   */
  protected boolean isQueryLoggingEnabled() {
    return config.isEnableQueryLogging();
  }

  /**
   * Get connection timeout in milliseconds.
   *
   * @return Connection timeout
   */
  protected long getConnectionTimeout() {
    return config.getConnectionTimeout();
  }

  /**
   * Get query timeout in milliseconds.
   *
   * @return Query timeout
   */
  protected long getQueryTimeout() {
    return config.getQueryTimeout();
  }
}
