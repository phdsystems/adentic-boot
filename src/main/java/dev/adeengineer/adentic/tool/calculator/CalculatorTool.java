package dev.adeengineer.adentic.tool.calculator;

import dev.adeengineer.adentic.tool.calculator.config.CalculatorConfig;
import dev.adeengineer.adentic.tool.calculator.model.CalculationRequest;
import dev.adeengineer.adentic.tool.calculator.model.CalculationRequest.CalculationType;
import dev.adeengineer.adentic.tool.calculator.model.CalculationResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Calculator Tool providing basic, scientific, and financial operations.
 *
 * <p>Supports:
 *
 * <ul>
 *   <li><b>Basic</b>: add, subtract, multiply, divide, power, sqrt, abs
 *   <li><b>Scientific</b>: trig functions, log, exp, rounding
 *   <li><b>Statistical</b>: sum, average, min, max, median, variance, stddev
 *   <li><b>Financial</b>: interest, present value, annuity, NPV
 * </ul>
 *
 * @since 0.3.0
 */
@Slf4j
public class CalculatorTool {

  private final CalculatorConfig config;

  public CalculatorTool(CalculatorConfig config) {
    this.config = config;
  }

  public CalculatorTool() {
    this(CalculatorConfig.defaults());
  }

  /**
   * Performs a calculation based on the request.
   *
   * @param request calculation request
   * @return Mono emitting calculation result
   */
  public Mono<CalculationResult> calculate(CalculationRequest request) {
    return Mono.fromCallable(
        () -> {
          try {
            log.debug("Performing calculation: {}", request.getType());

            BigDecimal result =
                switch (request.getType()) {
                  case ADD -> doAdd(request.getOperand1(), request.getOperand2());
                  case SUBTRACT -> doSubtract(request.getOperand1(), request.getOperand2());
                  case MULTIPLY -> doMultiply(request.getOperand1(), request.getOperand2());
                  case DIVIDE -> doDivide(request.getOperand1(), request.getOperand2(), request);
                  case MODULO -> modulo(request.getOperand1(), request.getOperand2());
                  case POWER -> power(request.getOperand1(), request.getOperand2());
                  case SQRT -> sqrt(request.getOperand1());
                  case ABS -> abs(request.getOperand1());
                  case NEGATE -> negate(request.getOperand1());

                    // Scientific
                  case SIN -> scientificOp(request, Math::sin);
                  case COS -> scientificOp(request, Math::cos);
                  case TAN -> scientificOp(request, Math::tan);
                  case ASIN -> scientificOp(request, Math::asin);
                  case ACOS -> scientificOp(request, Math::acos);
                  case ATAN -> scientificOp(request, Math::atan);
                  case LOG -> scientificOp(request, Math::log);
                  case LOG10 -> scientificOp(request, Math::log10);
                  case EXP -> scientificOp(request, Math::exp);
                  case CEIL -> ceil(request.getOperand1());
                  case FLOOR -> floor(request.getOperand1());
                  case ROUND -> round(request.getOperand1(), request.getPrecision());

                    // Statistical
                  case SUM -> sum(request.getOperands());
                  case AVERAGE -> average(request.getOperands());
                  case MIN -> min(request.getOperands());
                  case MAX -> max(request.getOperands());
                  case MEDIAN -> median(request.getOperands());
                  case VARIANCE -> variance(request.getOperands());
                  case STDDEV -> stddev(request.getOperands());

                    // Financial
                  case COMPOUND_INTEREST -> compoundInterest(request);
                  case SIMPLE_INTEREST -> simpleInterest(request);
                  case PRESENT_VALUE -> presentValue(request);
                  case ANNUITY -> annuity(request);
                  case NPV -> npv(request);
                  case IRR -> irr(request);
                };

            // Round to precision
            result = result.setScale(request.getPrecision(), config.getRoundingMode());

            return CalculationResult.success(request.getType(), result);

          } catch (Exception e) {
            log.error("Calculation failed: {}", e.getMessage());
            return CalculationResult.error(request.getType(), e.getMessage());
          }
        });
  }

  // ==================== Basic Operations ====================

  private BigDecimal doAdd(BigDecimal a, BigDecimal b) {
    return a.add(b);
  }

  private BigDecimal doSubtract(BigDecimal a, BigDecimal b) {
    return a.subtract(b);
  }

  private BigDecimal doMultiply(BigDecimal a, BigDecimal b) {
    return a.multiply(b);
  }

  private BigDecimal doDivide(BigDecimal a, BigDecimal b, CalculationRequest request) {
    if (b.compareTo(BigDecimal.ZERO) == 0) {
      throw new ArithmeticException("Division by zero");
    }
    return a.divide(b, request.getPrecision() + 2, config.getRoundingMode());
  }

  private BigDecimal modulo(BigDecimal a, BigDecimal b) {
    return a.remainder(b);
  }

  private BigDecimal power(BigDecimal base, BigDecimal exponent) {
    return BigDecimal.valueOf(Math.pow(base.doubleValue(), exponent.doubleValue()));
  }

  private BigDecimal sqrt(BigDecimal value) {
    if (value.compareTo(BigDecimal.ZERO) < 0) {
      throw new ArithmeticException("Cannot calculate square root of negative number");
    }
    return BigDecimal.valueOf(Math.sqrt(value.doubleValue()));
  }

  private BigDecimal abs(BigDecimal value) {
    return value.abs();
  }

  private BigDecimal negate(BigDecimal value) {
    return value.negate();
  }

  // ==================== Scientific Operations ====================

  @FunctionalInterface
  private interface MathFunction {
    double apply(double value);
  }

  private BigDecimal scientificOp(CalculationRequest request, MathFunction function) {
    if (!config.isEnableScientific()) {
      throw new UnsupportedOperationException("Scientific operations are disabled");
    }
    double result = function.apply(request.getOperand1().doubleValue());
    return BigDecimal.valueOf(result);
  }

  private BigDecimal ceil(BigDecimal value) {
    return value.setScale(0, RoundingMode.CEILING);
  }

  private BigDecimal floor(BigDecimal value) {
    return value.setScale(0, RoundingMode.FLOOR);
  }

  private BigDecimal round(BigDecimal value, int precision) {
    return value.setScale(precision, config.getRoundingMode());
  }

  // ==================== Statistical Operations ====================

  private BigDecimal sum(List<BigDecimal> operands) {
    if (!config.isEnableStatistical()) {
      throw new UnsupportedOperationException("Statistical operations are disabled");
    }
    validateOperands(operands);
    return operands.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal average(List<BigDecimal> operands) {
    if (!config.isEnableStatistical()) {
      throw new UnsupportedOperationException("Statistical operations are disabled");
    }
    validateOperands(operands);
    BigDecimal sum = sum(operands);
    return sum.divide(
        BigDecimal.valueOf(operands.size()), config.getMaxPrecision(), config.getRoundingMode());
  }

  private BigDecimal min(List<BigDecimal> operands) {
    validateOperands(operands);
    return operands.stream().min(Comparator.naturalOrder()).orElseThrow();
  }

  private BigDecimal max(List<BigDecimal> operands) {
    validateOperands(operands);
    return operands.stream().max(Comparator.naturalOrder()).orElseThrow();
  }

  private BigDecimal median(List<BigDecimal> operands) {
    if (!config.isEnableStatistical()) {
      throw new UnsupportedOperationException("Statistical operations are disabled");
    }
    validateOperands(operands);
    List<BigDecimal> sorted = operands.stream().sorted().collect(Collectors.toList());
    int size = sorted.size();
    if (size % 2 == 0) {
      BigDecimal mid1 = sorted.get(size / 2 - 1);
      BigDecimal mid2 = sorted.get(size / 2);
      return mid1.add(mid2).divide(BigDecimal.valueOf(2), config.getRoundingMode());
    } else {
      return sorted.get(size / 2);
    }
  }

  private BigDecimal variance(List<BigDecimal> operands) {
    if (!config.isEnableStatistical()) {
      throw new UnsupportedOperationException("Statistical operations are disabled");
    }
    validateOperands(operands);
    BigDecimal mean = average(operands);
    BigDecimal sumSquaredDiff =
        operands.stream()
            .map(val -> val.subtract(mean).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return sumSquaredDiff.divide(
        BigDecimal.valueOf(operands.size()), config.getMaxPrecision(), config.getRoundingMode());
  }

  private BigDecimal stddev(List<BigDecimal> operands) {
    if (!config.isEnableStatistical()) {
      throw new UnsupportedOperationException("Statistical operations are disabled");
    }
    BigDecimal var = variance(operands);
    return sqrt(var);
  }

  // ==================== Financial Operations ====================

  private BigDecimal compoundInterest(CalculationRequest request) {
    if (!config.isEnableFinancial()) {
      throw new UnsupportedOperationException("Financial operations are disabled");
    }
    // FV = PV * (1 + r)^n
    BigDecimal principal = request.getOperand1();
    BigDecimal rate = request.getParameter(); // Annual interest rate
    int periods = request.getPeriods();

    BigDecimal onePlusRate = BigDecimal.ONE.add(rate);
    BigDecimal futureValue = principal.multiply(power(onePlusRate, BigDecimal.valueOf(periods)));

    return futureValue;
  }

  private BigDecimal simpleInterest(CalculationRequest request) {
    if (!config.isEnableFinancial()) {
      throw new UnsupportedOperationException("Financial operations are disabled");
    }
    // I = P * r * t
    BigDecimal principal = request.getOperand1();
    BigDecimal rate = request.getParameter();
    int periods = request.getPeriods();

    return principal.multiply(rate).multiply(BigDecimal.valueOf(periods));
  }

  private BigDecimal presentValue(CalculationRequest request) {
    if (!config.isEnableFinancial()) {
      throw new UnsupportedOperationException("Financial operations are disabled");
    }
    // PV = FV / (1 + r)^n
    BigDecimal futureValue = request.getOperand1();
    BigDecimal rate = request.getParameter();
    int periods = request.getPeriods();

    BigDecimal onePlusRate = BigDecimal.ONE.add(rate);
    BigDecimal divisor = power(onePlusRate, BigDecimal.valueOf(periods));

    return futureValue.divide(divisor, config.getMaxPrecision(), config.getRoundingMode());
  }

  private BigDecimal annuity(CalculationRequest request) {
    if (!config.isEnableFinancial()) {
      throw new UnsupportedOperationException("Financial operations are disabled");
    }
    // PMT = PV * [r * (1 + r)^n] / [(1 + r)^n - 1]
    BigDecimal presentValue = request.getOperand1();
    BigDecimal rate = request.getParameter();
    int periods = request.getPeriods();

    BigDecimal onePlusRate = BigDecimal.ONE.add(rate);
    BigDecimal onePlusRatePowerN = power(onePlusRate, BigDecimal.valueOf(periods));

    BigDecimal numerator = presentValue.multiply(rate).multiply(onePlusRatePowerN);
    BigDecimal denominator = onePlusRatePowerN.subtract(BigDecimal.ONE);

    return numerator.divide(denominator, config.getMaxPrecision(), config.getRoundingMode());
  }

  private BigDecimal npv(CalculationRequest request) {
    if (!config.isEnableFinancial()) {
      throw new UnsupportedOperationException("Financial operations are disabled");
    }
    // NPV = Σ [CFt / (1 + r)^t]
    List<BigDecimal> cashFlows = request.getOperands();
    BigDecimal rate = request.getParameter();

    BigDecimal npv = BigDecimal.ZERO;
    for (int t = 0; t < cashFlows.size(); t++) {
      BigDecimal cashFlow = cashFlows.get(t);
      BigDecimal divisor = power(BigDecimal.ONE.add(rate), BigDecimal.valueOf(t));
      BigDecimal pv = cashFlow.divide(divisor, config.getMaxPrecision(), config.getRoundingMode());
      npv = npv.add(pv);
    }

    return npv;
  }

  private BigDecimal irr(CalculationRequest request) {
    if (!config.isEnableFinancial()) {
      throw new UnsupportedOperationException("Financial operations are disabled");
    }
    // Simplified IRR using Newton-Raphson method
    // This is a basic implementation - production use would need more robust calculation
    throw new UnsupportedOperationException("IRR calculation not yet implemented");
  }

  // ==================== Validation ====================

  private void validateOperands(List<BigDecimal> operands) {
    if (operands == null || operands.isEmpty()) {
      throw new IllegalArgumentException("Operands list cannot be null or empty");
    }
    if (operands.size() > config.getMaxOperands()) {
      throw new IllegalArgumentException(
          "Too many operands: " + operands.size() + " (max: " + config.getMaxOperands() + ")");
    }
  }

  /**
   * Quick calculation methods for common operations.
   *
   * @param a first operand
   * @param b second operand
   * @return calculation result
   */
  public Mono<CalculationResult> add(BigDecimal a, BigDecimal b) {
    return calculate(
        CalculationRequest.builder()
            .type(CalculationType.ADD)
            .operand1(a)
            .operand2(b)
            .precision(config.getDefaultPrecision())
            .build());
  }

  public Mono<CalculationResult> subtract(BigDecimal a, BigDecimal b) {
    return calculate(
        CalculationRequest.builder()
            .type(CalculationType.SUBTRACT)
            .operand1(a)
            .operand2(b)
            .precision(config.getDefaultPrecision())
            .build());
  }

  public Mono<CalculationResult> multiply(BigDecimal a, BigDecimal b) {
    return calculate(
        CalculationRequest.builder()
            .type(CalculationType.MULTIPLY)
            .operand1(a)
            .operand2(b)
            .precision(config.getDefaultPrecision())
            .build());
  }

  public Mono<CalculationResult> divide(BigDecimal a, BigDecimal b) {
    return calculate(
        CalculationRequest.builder()
            .type(CalculationType.DIVIDE)
            .operand1(a)
            .operand2(b)
            .precision(config.getDefaultPrecision())
            .build());
  }
}
