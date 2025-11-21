package dev.engineeringlab.adentic.tool.database.model;

/** Database types supported by the database tool. */
public enum DatabaseType {
  /** SQL databases (PostgreSQL, MySQL, SQLite, H2). */
  SQL,

  /** NoSQL databases (MongoDB, DynamoDB, Cassandra). */
  NOSQL,

  /** In-memory databases (H2, SQLite in-memory). */
  IN_MEMORY,

  /** Custom user-provided database implementation. */
  CUSTOM
}
