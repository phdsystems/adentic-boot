package dev.adeengineer.adentic.tool.calculator.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

/**
 * Result of a calculator operation.
 *
 * @since 0.3.0
 */
@Data
@Builder
public class CalculationResult {

  /** Type of calculation performed. */
  private CalculationRequest.CalculationType type;

  /** Result of the calculation. */
  private BigDecimal result;

  /** Whether the calculation was successful. */
  private boolean success;

  /** Error message if calculation failed. */
  @Builder.Default private String errorMessage = null;

  /** Formatted result as string. */
  @Builder.Default private String formattedResult = null;

  /** Timestamp of calculation. */
  @Builder.Default private Instant timestamp = Instant.now();

  /** Calculation steps (for debugging/explanation). */
  @Builder.Default private String steps = null;

  /**
   * Creates a successful calculation result.
   *
   * @param type calculation type
   * @param result calculation result
   * @return CalculationResult
   */
  public static CalculationResult success(
      CalculationRequest.CalculationType type, BigDecimal result) {
    return CalculationResult.builder()
        .type(type)
        .result(result)
        .success(true)
        .formattedResult(result.toString())
        .build();
  }

  /**
   * Creates a successful calculation result with formatted output.
   *
   * @param type calculation type
   * @param result calculation result
   * @param formatted formatted result string
   * @return CalculationResult
   */
  public static CalculationResult success(
      CalculationRequest.CalculationType type, BigDecimal result, String formatted) {
    return CalculationResult.builder()
        .type(type)
        .result(result)
        .success(true)
        .formattedResult(formatted)
        .build();
  }

  /**
   * Creates a failed calculation result.
   *
   * @param type calculation type
   * @param error error message
   * @return CalculationResult
   */
  public static CalculationResult error(CalculationRequest.CalculationType type, String error) {
    return CalculationResult.builder().type(type).success(false).errorMessage(error).build();
  }
}
