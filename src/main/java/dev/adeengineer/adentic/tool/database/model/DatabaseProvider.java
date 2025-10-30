package dev.adeengineer.adentic.tool.database.model;

/** Supported database providers. */
public enum DatabaseProvider {
  /**
   * PostgreSQL - Advanced open-source SQL database. Best for: Production apps, complex queries,
   * ACID compliance.
   */
  POSTGRESQL,

  /**
   * MySQL - Popular open-source SQL database. Best for: Web applications, read-heavy workloads,
   * wide compatibility.
   */
  MYSQL,

  /**
   * SQLite - Lightweight embedded SQL database. Best for: Local storage, mobile apps, testing,
   * single-user apps.
   */
  SQLITE,

  /**
   * MongoDB - Document-oriented NoSQL database. Best for: Flexible schemas, JSON-like documents,
   * scalability.
   */
  MONGODB,

  /**
   * H2 - Fast in-memory SQL database. Best for: Testing, development, temporary data, embedded use.
   */
  H2,

  /**
   * Custom - User-provided custom implementation. Best for: Specialized databases, custom
   * integrations, proprietary systems.
   */
  CUSTOM
}
