package dev.adeengineer.adentic.tool.calculator.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Request for calculator operations.
 *
 * @since 0.3.0
 */
@Data
@Builder
public class CalculationRequest {

  /** Type of calculation to perform. */
  private CalculationType type;

  /** Primary operand for unary/binary operations. */
  private BigDecimal operand1;

  /** Secondary operand for binary operations. */
  @Builder.Default private BigDecimal operand2 = null;

  /** List of operands for variadic operations (sum, product, etc.). */
  @Builder.Default private List<BigDecimal> operands = null;

  /** Number of decimal places for rounding (default: 2). */
  @Builder.Default private int precision = 2;

  /** Additional parameters for specific operations (e.g., rate for financial calculations). */
  @Builder.Default private BigDecimal parameter = null;

  /** Number of periods for financial calculations. */
  @Builder.Default private Integer periods = null;

  /** Type of calculation. */
  public enum CalculationType {
    // Basic arithmetic
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    MODULO,
    POWER,
    SQRT,
    ABS,
    NEGATE,

    // Scientific
    SIN,
    COS,
    TAN,
    ASIN,
    ACOS,
    ATAN,
    LOG,
    LOG10,
    EXP,
    CEIL,
    FLOOR,
    ROUND,

    // Statistical (variadic)
    SUM,
    AVERAGE,
    MIN,
    MAX,
    MEDIAN,
    VARIANCE,
    STDDEV,

    // Financial
    COMPOUND_INTEREST, // Future value with compound interest
    SIMPLE_INTEREST, // Simple interest calculation
    PRESENT_VALUE, // Present value of future amount
    ANNUITY, // Annuity payment calculation
    NPV, // Net present value
    IRR // Internal rate of return
  }
}
