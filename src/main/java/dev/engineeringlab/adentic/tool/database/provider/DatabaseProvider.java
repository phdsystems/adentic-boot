package dev.engineeringlab.adentic.tool.database.provider;

import dev.engineeringlab.adentic.tool.database.model.ConnectionInfo;
import dev.engineeringlab.adentic.tool.database.model.QueryResult;
import dev.engineeringlab.adentic.tool.database.model.TransactionResult;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import reactor.core.publisher.Mono;

/**
 * Interface for database provider implementations.
 *
 * <p>Providers implement database-specific connection and query logic while presenting a unified
 * API to the DatabaseTool.
 */
public interface DatabaseProvider {

  // ========== LIFECYCLE ==========

  /**
   * Connect to the database.
   *
   * @return Mono completing when connected
   */
  Mono<Void> connect();

  /**
   * Disconnect from the database.
   *
   * @return Mono completing when disconnected
   */
  Mono<Void> disconnect();

  /**
   * Check if connected to the database.
   *
   * @return true if connected
   */
  boolean isConnected();

  /**
   * Test the database connection.
   *
   * @return Mono emitting true if connection is healthy
   */
  Mono<Boolean> testConnection();

  // ========== QUERY OPERATIONS ==========

  /**
   * Execute a SELECT query.
   *
   * @param query SQL/NoSQL query string
   * @return Mono emitting query results
   */
  Mono<QueryResult> executeQuery(String query);

  /**
   * Execute a parameterized SELECT query.
   *
   * @param query SQL/NoSQL query string
   * @param parameters Query parameters
   * @return Mono emitting query results
   */
  Mono<QueryResult> executeQuery(String query, Map<String, Object> parameters);

  /**
   * Execute a parameterized SELECT query with positional parameters.
   *
   * @param query SQL/NoSQL query string
   * @param parameters Positional parameters
   * @return Mono emitting query results
   */
  Mono<QueryResult> executeQuery(String query, Object... parameters);

  // ========== UPDATE OPERATIONS ==========

  /**
   * Execute an UPDATE/DELETE statement.
   *
   * @param statement SQL/NoSQL statement
   * @return Mono emitting number of affected rows
   */
  Mono<Long> executeUpdate(String statement);

  /**
   * Execute a parameterized UPDATE/DELETE statement.
   *
   * @param statement SQL/NoSQL statement
   * @param parameters Statement parameters
   * @return Mono emitting number of affected rows
   */
  Mono<Long> executeUpdate(String statement, Map<String, Object> parameters);

  /**
   * Execute a parameterized UPDATE/DELETE statement with positional parameters.
   *
   * @param statement SQL/NoSQL statement
   * @param parameters Positional parameters
   * @return Mono emitting number of affected rows
   */
  Mono<Long> executeUpdate(String statement, Object... parameters);

  // ========== INSERT OPERATIONS ==========

  /**
   * Insert a single record.
   *
   * @param table Table/collection name
   * @param data Record data
   * @return Mono emitting generated ID (if applicable)
   */
  Mono<Object> insert(String table, Map<String, Object> data);

  /**
   * Insert multiple records in batch.
   *
   * @param table Table/collection name
   * @param records List of records to insert
   * @return Mono emitting number of records inserted
   */
  Mono<Long> insertBatch(String table, List<Map<String, Object>> records);

  // ========== DELETE OPERATIONS ==========

  /**
   * Delete a record by ID.
   *
   * @param table Table/collection name
   * @param id Record ID
   * @return Mono emitting true if deleted
   */
  Mono<Boolean> delete(String table, Object id);

  /**
   * Delete records matching criteria.
   *
   * @param table Table/collection name
   * @param criteria Delete criteria
   * @return Mono emitting number of deleted records
   */
  Mono<Long> deleteWhere(String table, Map<String, Object> criteria);

  // ========== UPDATE OPERATIONS (by ID) ==========

  /**
   * Update a record by ID.
   *
   * @param table Table/collection name
   * @param id Record ID
   * @param updates Fields to update
   * @return Mono emitting true if updated
   */
  Mono<Boolean> update(String table, Object id, Map<String, Object> updates);

  /**
   * Update records matching criteria.
   *
   * @param table Table/collection name
   * @param criteria Update criteria
   * @param updates Fields to update
   * @return Mono emitting number of updated records
   */
  Mono<Long> updateWhere(String table, Map<String, Object> criteria, Map<String, Object> updates);

  // ========== RETRIEVAL OPERATIONS ==========

  /**
   * Get a single record by ID.
   *
   * @param table Table/collection name
   * @param id Record ID
   * @return Mono emitting the record, or empty if not found
   */
  Mono<Map<String, Object>> findById(String table, Object id);

  /**
   * Find all records in a table/collection.
   *
   * @param table Table/collection name
   * @return Mono emitting query results
   */
  Mono<QueryResult> findAll(String table);

  /**
   * Find records matching criteria.
   *
   * @param table Table/collection name
   * @param criteria Search criteria
   * @return Mono emitting query results
   */
  Mono<QueryResult> findWhere(String table, Map<String, Object> criteria);

  /**
   * Count records in a table/collection.
   *
   * @param table Table/collection name
   * @return Mono emitting record count
   */
  Mono<Long> count(String table);

  /**
   * Count records matching criteria.
   *
   * @param table Table/collection name
   * @param criteria Search criteria
   * @return Mono emitting record count
   */
  Mono<Long> countWhere(String table, Map<String, Object> criteria);

  // ========== TRANSACTION SUPPORT ==========

  /**
   * Execute operations within a transaction.
   *
   * @param operations Function containing transactional operations
   * @return Mono emitting transaction result
   */
  Mono<TransactionResult> executeTransaction(Function<DatabaseProvider, Mono<Void>> operations);

  /**
   * Begin a transaction manually.
   *
   * @return Mono completing when transaction started
   */
  Mono<Void> beginTransaction();

  /**
   * Commit the current transaction.
   *
   * @return Mono completing when transaction committed
   */
  Mono<Void> commit();

  /**
   * Rollback the current transaction.
   *
   * @return Mono completing when transaction rolled back
   */
  Mono<Void> rollback();

  // ========== SCHEMA OPERATIONS ==========

  /**
   * List all tables/collections.
   *
   * @return Mono emitting list of table/collection names
   */
  Mono<List<String>> listTables();

  /**
   * Get table/collection schema.
   *
   * @param table Table/collection name
   * @return Mono emitting schema information
   */
  Mono<Map<String, Object>> getSchema(String table);

  /**
   * Check if a table/collection exists.
   *
   * @param table Table/collection name
   * @return Mono emitting true if exists
   */
  Mono<Boolean> tableExists(String table);

  // ========== METADATA ==========

  /**
   * Get connection information.
   *
   * @return Connection info
   */
  ConnectionInfo getConnectionInfo();

  /**
   * Get provider name.
   *
   * @return Provider name (e.g., "postgresql", "mongodb")
   */
  String getProviderName();

  /**
   * Get database type.
   *
   * @return Database type (SQL, NOSQL, etc.)
   */
  dev.adeengineer.adentic.tool.database.model.DatabaseType getDatabaseType();
}
