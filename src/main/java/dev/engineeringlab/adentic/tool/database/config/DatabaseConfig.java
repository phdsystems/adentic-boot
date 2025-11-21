package dev.engineeringlab.adentic.tool.database.config;

import dev.engineeringlab.adentic.tool.database.model.DatabaseProvider;
import lombok.Builder;
import lombok.Data;

/** Configuration for DatabaseTool behavior and provider selection. */
@Data
@Builder
public class DatabaseConfig {
  /** Database provider to use. */
  @Builder.Default private DatabaseProvider provider = DatabaseProvider.H2;

  /** Database host/server address. */
  @Builder.Default private String host = "localhost";

  /** Database port. */
  @Builder.Default private int port = 5432;

  /** Database name. */
  @Builder.Default private String database = "testdb";

  /** Username. */
  @Builder.Default private String username = "user";

  /** Password. */
  @Builder.Default private String password = "password";

  /** Connection pool size. */
  @Builder.Default private int poolSize = 10;

  /** Connection timeout in milliseconds. */
  @Builder.Default private long connectionTimeout = 30000;

  /** Query timeout in milliseconds. */
  @Builder.Default private long queryTimeout = 60000;

  /** Maximum query result rows. */
  @Builder.Default private int maxResultRows = 1000;

  /** Enable SSL/TLS encryption. */
  @Builder.Default private boolean useSSL = false;

  /** Validate SSL certificates. */
  @Builder.Default private boolean validateSSL = true;

  /** Enable query logging. */
  @Builder.Default private boolean enableQueryLogging = true;

  /** Enable auto-commit (for SQL databases). */
  @Builder.Default private boolean autoCommit = true;

  /**
   * Transaction isolation level (for SQL databases). Values: READ_UNCOMMITTED, READ_COMMITTED,
   * REPEATABLE_READ, SERIALIZABLE
   */
  private String isolationLevel;

  /** JDBC URL (for SQL databases, optional). */
  private String jdbcUrl;

  /** MongoDB URI (for MongoDB, optional). */
  private String mongoUri;

  /** Additional connection properties. */
  private java.util.Map<String, String> properties;

  // ========== CONFIGURATION PRESETS ==========

  /**
   * PostgreSQL configuration preset. Production-ready relational database.
   *
   * @param host Database host
   * @param port Database port
   * @param database Database name
   * @param username Username
   * @param password Password
   * @return PostgreSQL configuration
   */
  public static DatabaseConfig postgresql(
      String host, int port, String database, String username, String password) {
    return DatabaseConfig.builder()
        .provider(DatabaseProvider.POSTGRESQL)
        .host(host)
        .port(port)
        .database(database)
        .username(username)
        .password(password)
        .build();
  }

  /**
   * PostgreSQL configuration preset with defaults.
   *
   * @return PostgreSQL configuration (localhost:5432)
   */
  public static DatabaseConfig postgresql() {
    return postgresql("localhost", 5432, "postgres", "postgres", "postgres");
  }

  /**
   * MySQL configuration preset. Popular web application database.
   *
   * @param host Database host
   * @param port Database port
   * @param database Database name
   * @param username Username
   * @param password Password
   * @return MySQL configuration
   */
  public static DatabaseConfig mysql(
      String host, int port, String database, String username, String password) {
    return DatabaseConfig.builder()
        .provider(DatabaseProvider.MYSQL)
        .host(host)
        .port(port)
        .database(database)
        .username(username)
        .password(password)
        .build();
  }

  /**
   * MySQL configuration preset with defaults.
   *
   * @return MySQL configuration (localhost:3306)
   */
  public static DatabaseConfig mysql() {
    return mysql("localhost", 3306, "mysql", "root", "password");
  }

  /**
   * SQLite configuration preset. Lightweight file-based database.
   *
   * @param databasePath Path to SQLite database file
   * @return SQLite configuration
   */
  public static DatabaseConfig sqlite(String databasePath) {
    return DatabaseConfig.builder()
        .provider(DatabaseProvider.SQLITE)
        .database(databasePath)
        .host(null)
        .port(0)
        .username(null)
        .password(null)
        .build();
  }

  /**
   * SQLite in-memory configuration preset. Fast temporary database.
   *
   * @return SQLite in-memory configuration
   */
  public static DatabaseConfig sqliteMemory() {
    return sqlite(":memory:");
  }

  /**
   * MongoDB configuration preset. Document-oriented NoSQL database.
   *
   * @param host MongoDB host
   * @param port MongoDB port
   * @param database Database name
   * @param username Username (optional)
   * @param password Password (optional)
   * @return MongoDB configuration
   */
  public static DatabaseConfig mongodb(
      String host, int port, String database, String username, String password) {
    return DatabaseConfig.builder()
        .provider(DatabaseProvider.MONGODB)
        .host(host)
        .port(port)
        .database(database)
        .username(username)
        .password(password)
        .build();
  }

  /**
   * MongoDB configuration preset with defaults.
   *
   * @return MongoDB configuration (localhost:27017)
   */
  public static DatabaseConfig mongodb() {
    return mongodb("localhost", 27017, "test", null, null);
  }

  /**
   * H2 in-memory configuration preset. Fast in-memory database for testing.
   *
   * @return H2 in-memory configuration
   */
  public static DatabaseConfig h2Memory() {
    return DatabaseConfig.builder()
        .provider(DatabaseProvider.H2)
        .database("mem:testdb")
        .host(null)
        .port(0)
        .username("sa")
        .password("")
        .autoCommit(true)
        .build();
  }

  /**
   * H2 file-based configuration preset.
   *
   * @param databasePath Path to H2 database file
   * @return H2 file configuration
   */
  public static DatabaseConfig h2File(String databasePath) {
    return DatabaseConfig.builder()
        .provider(DatabaseProvider.H2)
        .database("file:" + databasePath)
        .host(null)
        .port(0)
        .username("sa")
        .password("")
        .build();
  }

  /**
   * Default configuration preset. H2 in-memory for testing.
   *
   * @return Default H2 in-memory configuration
   */
  public static DatabaseConfig defaults() {
    return h2Memory();
  }

  /**
   * Development configuration preset. PostgreSQL with relaxed settings.
   *
   * @return Development configuration
   */
  public static DatabaseConfig development() {
    return DatabaseConfig.builder()
        .provider(DatabaseProvider.POSTGRESQL)
        .host("localhost")
        .port(5432)
        .database("dev_db")
        .username("dev_user")
        .password("dev_password")
        .enableQueryLogging(true)
        .useSSL(false)
        .build();
  }

  /**
   * Production configuration preset. PostgreSQL with security enabled.
   *
   * @param host Database host
   * @param database Database name
   * @param username Username
   * @param password Password
   * @return Production configuration
   */
  public static DatabaseConfig production(
      String host, String database, String username, String password) {
    return DatabaseConfig.builder()
        .provider(DatabaseProvider.POSTGRESQL)
        .host(host)
        .port(5432)
        .database(database)
        .username(username)
        .password(password)
        .useSSL(true)
        .validateSSL(true)
        .poolSize(20)
        .connectionTimeout(10000)
        .enableQueryLogging(false)
        .build();
  }

  /**
   * Testing configuration preset. H2 in-memory with auto-commit.
   *
   * @return Testing configuration
   */
  public static DatabaseConfig testing() {
    return h2Memory();
  }
}
