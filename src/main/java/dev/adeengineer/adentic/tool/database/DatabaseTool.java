package dev.adeengineer.adentic.tool.database;

import dev.adeengineer.adentic.boot.annotations.provider.Tool;
import dev.adeengineer.adentic.tool.database.config.DatabaseConfig;
import dev.adeengineer.adentic.tool.database.model.ConnectionInfo;
import dev.adeengineer.adentic.tool.database.model.QueryResult;
import dev.adeengineer.adentic.tool.database.model.TransactionResult;
import dev.adeengineer.adentic.tool.database.provider.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Tool for database operations using multiple provider backends.
 *
 * <p>Supports five database providers:
 *
 * <ul>
 *   <li>**PostgreSQL** - Advanced open-source SQL database (production-ready)
 *   <li>**MySQL** - Popular web application database (widely compatible)
 *   <li>**SQLite** - Lightweight file-based database (embedded, mobile)
 *   <li>**MongoDB** - Document-oriented NoSQL database (flexible schemas)
 *   <li>**H2** - Fast in-memory SQL database (testing, development)
 * </ul>
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Provider/service pattern for flexibility
 *   <li>Runtime provider switching
 *   <li>Comprehensive CRUD operations
 *   <li>Transaction support
 *   <li>Schema introspection
 *   <li>Async/reactive API (Mono-based)
 *   <li>Connection pooling
 *   <li>Query parameter binding (SQL injection prevention)
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Inject
 * private DatabaseTool db;
 *
 * // Configure PostgreSQL
 * db.setConfig(DatabaseConfig.postgresql("localhost", 5432, "mydb", "user", "pass"));
 *
 * // Connect and query
 * db.connect().block();
 * QueryResult result = db.executeQuery("SELECT * FROM users WHERE age > ?", 18).block();
 *
 * // Insert data
 * db.insert("users", Map.of("name", "John", "age", 30)).block();
 *
 * // Use H2 for testing
 * db.setConfig(DatabaseConfig.h2Memory());
 * }</pre>
 *
 * @see DatabaseConfig
 * @see DatabaseProvider
 */
@Tool(name = "database")
@Slf4j
public class DatabaseTool {

  private DatabaseConfig config;

  private DatabaseProvider provider;

  /** Default constructor with H2 in-memory database. */
  public DatabaseTool() {
    this(DatabaseConfig.defaults());
  }

  /** Constructor with custom configuration. */
  public DatabaseTool(DatabaseConfig config) {
    this.config = config;
    this.provider = createProvider(config);
  }

  /** Set configuration and recreate provider. */
  public void setConfig(DatabaseConfig config) {
    // Disconnect existing provider if connected
    if (provider != null && provider.isConnected()) {
      provider.disconnect().block();
    }

    this.config = config;
    this.provider = createProvider(config);

    log.info("Switched to {} provider", config.getProvider());
  }

  /** Create provider based on configuration. */
  private DatabaseProvider createProvider(DatabaseConfig config) {
    return switch (config.getProvider()) {
      case POSTGRESQL -> new PostgreSQLDatabaseProvider(config);
      case MYSQL -> new MySQLDatabaseProvider(config);
      case SQLITE -> new SQLiteDatabaseProvider(config);
      case MONGODB -> new MongoDBDatabaseProvider(config);
      case H2 -> new H2DatabaseProvider(config);
      case CUSTOM ->
          throw new IllegalStateException(
              "CUSTOM provider requires manual instantiation. "
                  + "Extend BaseDatabaseProvider and provide your implementation.");
    };
  }

  // ========== LIFECYCLE ==========

  /** Connect to the database. */
  public Mono<Void> connect() {
    return provider.connect();
  }

  /** Disconnect from the database. */
  public Mono<Void> disconnect() {
    return provider.disconnect();
  }

  /** Check if connected to the database. */
  public boolean isConnected() {
    return provider.isConnected();
  }

  /** Test the database connection. */
  public Mono<Boolean> testConnection() {
    return provider.testConnection();
  }

  /** Get connection information. */
  public ConnectionInfo getConnectionInfo() {
    return provider.getConnectionInfo();
  }

  // ========== QUERY OPERATIONS ==========

  /** Execute a SELECT query. */
  public Mono<QueryResult> executeQuery(String query) {
    return provider.executeQuery(query);
  }

  /** Execute a parameterized SELECT query. */
  public Mono<QueryResult> executeQuery(String query, Map<String, Object> parameters) {
    return provider.executeQuery(query, parameters);
  }

  /** Execute a parameterized SELECT query with positional parameters. */
  public Mono<QueryResult> executeQuery(String query, Object... parameters) {
    return provider.executeQuery(query, parameters);
  }

  // ========== UPDATE OPERATIONS ==========

  /** Execute an UPDATE/DELETE statement. */
  public Mono<Long> executeUpdate(String statement) {
    return provider.executeUpdate(statement);
  }

  /** Execute a parameterized UPDATE/DELETE statement. */
  public Mono<Long> executeUpdate(String statement, Map<String, Object> parameters) {
    return provider.executeUpdate(statement, parameters);
  }

  /** Execute a parameterized UPDATE/DELETE statement with positional parameters. */
  public Mono<Long> executeUpdate(String statement, Object... parameters) {
    return provider.executeUpdate(statement, parameters);
  }

  // ========== INSERT OPERATIONS ==========

  /** Insert a single record. */
  public Mono<Object> insert(String table, Map<String, Object> data) {
    return provider.insert(table, data);
  }

  /** Insert multiple records in batch. */
  public Mono<Long> insertBatch(String table, List<Map<String, Object>> records) {
    return provider.insertBatch(table, records);
  }

  // ========== DELETE OPERATIONS ==========

  /** Delete a record by ID. */
  public Mono<Boolean> delete(String table, Object id) {
    return provider.delete(table, id);
  }

  /** Delete records matching criteria. */
  public Mono<Long> deleteWhere(String table, Map<String, Object> criteria) {
    return provider.deleteWhere(table, criteria);
  }

  // ========== UPDATE OPERATIONS (by ID) ==========

  /** Update a record by ID. */
  public Mono<Boolean> update(String table, Object id, Map<String, Object> updates) {
    return provider.update(table, id, updates);
  }

  /** Update records matching criteria. */
  public Mono<Long> updateWhere(
      String table, Map<String, Object> criteria, Map<String, Object> updates) {
    return provider.updateWhere(table, criteria, updates);
  }

  // ========== RETRIEVAL OPERATIONS ==========

  /** Get a single record by ID. */
  public Mono<Map<String, Object>> findById(String table, Object id) {
    return provider.findById(table, id);
  }

  /** Find all records in a table/collection. */
  public Mono<QueryResult> findAll(String table) {
    return provider.findAll(table);
  }

  /** Find records matching criteria. */
  public Mono<QueryResult> findWhere(String table, Map<String, Object> criteria) {
    return provider.findWhere(table, criteria);
  }

  /** Count records in a table/collection. */
  public Mono<Long> count(String table) {
    return provider.count(table);
  }

  /** Count records matching criteria. */
  public Mono<Long> countWhere(String table, Map<String, Object> criteria) {
    return provider.countWhere(table, criteria);
  }

  // ========== TRANSACTION SUPPORT ==========

  /** Execute operations within a transaction. */
  public Mono<TransactionResult> executeTransaction(
      Function<DatabaseProvider, Mono<Void>> operations) {
    return provider.executeTransaction(operations);
  }

  /** Begin a transaction manually. */
  public Mono<Void> beginTransaction() {
    return provider.beginTransaction();
  }

  /** Commit the current transaction. */
  public Mono<Void> commit() {
    return provider.commit();
  }

  /** Rollback the current transaction. */
  public Mono<Void> rollback() {
    return provider.rollback();
  }

  // ========== SCHEMA OPERATIONS ==========

  /** List all tables/collections. */
  public Mono<List<String>> listTables() {
    return provider.listTables();
  }

  /** Get table/collection schema. */
  public Mono<Map<String, Object>> getSchema(String table) {
    return provider.getSchema(table);
  }

  /** Check if a table/collection exists. */
  public Mono<Boolean> tableExists(String table) {
    return provider.tableExists(table);
  }
}
