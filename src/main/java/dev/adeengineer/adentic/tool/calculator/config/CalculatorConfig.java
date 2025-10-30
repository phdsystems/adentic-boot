package dev.adeengineer.adentic.tool.calculator.config;

import java.math.MathContext;
import java.math.RoundingMode;
import lombok.Builder;
import lombok.Data;

/**
 * Configuration for Calculator Tool.
 *
 * @since 0.3.0
 */
@Data
@Builder
public class CalculatorConfig {

  /** Default precision (decimal places). */
  @Builder.Default private int defaultPrecision = 2;

  /** Maximum precision allowed. */
  @Builder.Default private int maxPrecision = 10;

  /** Rounding mode for operations. */
  @Builder.Default private RoundingMode roundingMode = RoundingMode.HALF_UP;

  /** Math context for BigDecimal operations. */
  @Builder.Default private MathContext mathContext = MathContext.DECIMAL128;

  /** Whether to enable scientific operations. */
  @Builder.Default private boolean enableScientific = true;

  /** Whether to enable financial operations. */
  @Builder.Default private boolean enableFinancial = true;

  /** Whether to enable statistical operations. */
  @Builder.Default private boolean enableStatistical = true;

  /** Maximum number of operands for variadic operations. */
  @Builder.Default private int maxOperands = 1000;

  /**
   * Creates default calculator configuration.
   *
   * @return default config
   */
  public static CalculatorConfig defaults() {
    return CalculatorConfig.builder().build();
  }

  /**
   * Creates basic calculator configuration (arithmetic only).
   *
   * @return basic config
   */
  public static CalculatorConfig basic() {
    return CalculatorConfig.builder()
        .enableScientific(false)
        .enableFinancial(false)
        .enableStatistical(false)
        .build();
  }

  /**
   * Creates high-precision calculator configuration.
   *
   * @return high precision config
   */
  public static CalculatorConfig highPrecision() {
    return CalculatorConfig.builder()
        .defaultPrecision(10)
        .maxPrecision(34)
        .mathContext(MathContext.DECIMAL128)
        .build();
  }
}
