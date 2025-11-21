package dev.engineeringlab.adentic.tool.database.model;

import lombok.Builder;
import lombok.Data;

/** Database connection information. */
@Data
@Builder
public class ConnectionInfo {
  /** Database provider. */
  private DatabaseProvider provider;

  /** Host/server address. */
  private String host;

  /** Port number. */
  private int port;

  /** Database name. */
  private String database;

  /** Username. */
  private String username;

  /** Password (sensitive). */
  private String password;

  /** JDBC URL (for SQL databases). */
  private String jdbcUrl;

  /** MongoDB URI (for MongoDB). */
  private String mongoUri;

  /** Connection pool size. */
  @Builder.Default private int poolSize = 10;

  /** Connection timeout in milliseconds. */
  @Builder.Default private long connectionTimeout = 30000;

  /** Whether the connection is encrypted (SSL/TLS). */
  @Builder.Default private boolean useSSL = false;

  /** Whether to validate SSL certificates. */
  @Builder.Default private boolean validateSSL = true;

  /** Whether connection is currently active. */
  @Builder.Default private boolean connected = false;

  /** Additional connection properties. */
  private java.util.Map<String, String> properties;
}
