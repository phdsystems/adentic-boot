package dev.adeengineer.adentic.tool.database.model;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** Result of a database query operation. */
@Data
@Builder
public class QueryResult {
  /** Whether the query succeeded. */
  @Builder.Default private boolean success = false;

  /** Query result rows. */
  private List<Map<String, Object>> rows;

  /** Number of rows returned. */
  @Builder.Default private int rowCount = 0;

  /** Number of rows affected (for INSERT/UPDATE/DELETE). */
  @Builder.Default private long affectedRows = 0;

  /** Generated keys (for INSERT with auto-increment). */
  private List<Object> generatedKeys;

  /** Column metadata. */
  private List<ColumnMetadata> columns;

  /** Execution time in milliseconds. */
  @Builder.Default private long executionTimeMs = 0;

  /** Error message if query failed. */
  private String errorMessage;

  /** SQL state code (for SQL errors). */
  private String sqlState;

  /** Query that was executed. */
  private String query;

  /** Query parameters. */
  private Map<String, Object> parameters;

  /** Whether more results are available (pagination). */
  @Builder.Default private boolean hasMore = false;

  /** Column metadata. */
  @Data
  @Builder
  public static class ColumnMetadata {
    private String name;
    private String type;
    private boolean nullable;
    private int size;
    private int precision;
    private int scale;
  }
}
