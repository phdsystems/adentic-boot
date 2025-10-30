package dev.adeengineer.adentic.tool.database.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/** Result of a database transaction. */
@Data
@Builder
public class TransactionResult {
  /** Whether the transaction succeeded. */
  @Builder.Default private boolean success = false;

  /** Whether the transaction was committed. */
  @Builder.Default private boolean committed = false;

  /** Whether the transaction was rolled back. */
  @Builder.Default private boolean rolledBack = false;

  /** Number of operations in the transaction. */
  @Builder.Default private int operationCount = 0;

  /** Total rows affected by the transaction. */
  @Builder.Default private long totalAffectedRows = 0;

  /** Transaction execution time in milliseconds. */
  @Builder.Default private long executionTimeMs = 0;

  /** Error message if transaction failed. */
  private String errorMessage;

  /** Individual operation results. */
  private List<QueryResult> operationResults;

  /** Transaction ID (if supported by database). */
  private String transactionId;
}
