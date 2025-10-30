package dev.adeengineer.adentic.tool.calculator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;

import dev.adeengineer.adentic.tool.calculator.config.CalculatorConfig;
import dev.adeengineer.adentic.tool.calculator.model.CalculationRequest;
import dev.adeengineer.adentic.tool.calculator.model.CalculationRequest.CalculationType;
import dev.adeengineer.adentic.tool.calculator.model.CalculationResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for CalculatorTool covering:
 *
 * <ul>
 *   <li>Basic arithmetic operations (add, subtract, multiply, divide, modulo, power, sqrt, abs,
 *       negate)
 *   <li>Scientific operations (trig functions, log, exp, rounding)
 *   <li>Statistical operations (sum, average, min, max, median, variance, stddev)
 *   <li>Financial operations (interest, present value, annuity, NPV)
 *   <li>Edge cases (division by zero, negative sqrt, disabled operations)
 *   <li>Configuration options
 * </ul>
 */
@DisplayName("CalculatorTool Tests")
class CalculatorToolTest {

  private CalculatorTool calculator;

  @BeforeEach
  void setUp() {
    calculator = new CalculatorTool();
  }

  @Nested
  @DisplayName("Basic Arithmetic Operations")
  class BasicArithmeticTests {

    @Test
    @DisplayName("Should add two numbers")
    void testAddition() {
      CalculationResult result =
          calculator.add(BigDecimal.valueOf(10), BigDecimal.valueOf(5)).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(15).compareTo(result.getResult()));
      assertEquals(CalculationType.ADD, result.getType());
    }

    @Test
    @DisplayName("Should subtract two numbers")
    void testSubtraction() {
      CalculationResult result =
          calculator.subtract(BigDecimal.valueOf(10), BigDecimal.valueOf(5)).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(5).compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should multiply two numbers")
    void testMultiplication() {
      CalculationResult result =
          calculator.multiply(BigDecimal.valueOf(10), BigDecimal.valueOf(5)).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(50).compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should divide two numbers")
    void testDivision() {
      CalculationResult result =
          calculator.divide(BigDecimal.valueOf(10), BigDecimal.valueOf(2)).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(5).compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should handle division by zero")
    void testDivisionByZero() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.DIVIDE)
              .operand1(BigDecimal.valueOf(10))
              .operand2(BigDecimal.ZERO)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getErrorMessage()).contains("Division by zero");
    }

    @Test
    @DisplayName("Should calculate modulo")
    void testModulo() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.MODULO)
              .operand1(BigDecimal.valueOf(10))
              .operand2(BigDecimal.valueOf(3))
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(1).compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should calculate power")
    void testPower() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.POWER)
              .operand1(BigDecimal.valueOf(2))
              .operand2(BigDecimal.valueOf(3))
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(8).compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should calculate square root")
    void testSquareRoot() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.SQRT)
              .operand1(BigDecimal.valueOf(16))
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(4).compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should handle negative square root")
    void testNegativeSquareRoot() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.SQRT)
              .operand1(BigDecimal.valueOf(-16))
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getErrorMessage()).contains("square root of negative number");
    }

    @Test
    @DisplayName("Should calculate absolute value")
    void testAbsoluteValue() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.ABS)
              .operand1(BigDecimal.valueOf(-42))
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(42).compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should negate a number")
    void testNegate() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.NEGATE)
              .operand1(BigDecimal.valueOf(42))
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(-42).compareTo(result.getResult()));
    }
  }

  @Nested
  @DisplayName("Scientific Operations")
  class ScientificOperationsTests {

    @Test
    @DisplayName("Should calculate sine")
    void testSine() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.SIN)
              .operand1(BigDecimal.ZERO)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.ZERO.compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should calculate cosine")
    void testCosine() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.COS)
              .operand1(BigDecimal.ZERO)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.ONE.compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should calculate tangent")
    void testTangent() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.TAN)
              .operand1(BigDecimal.ZERO)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.ZERO.compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should calculate arcsine")
    void testArcSine() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.ASIN)
              .operand1(BigDecimal.ZERO)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should calculate arccosine")
    void testArcCosine() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.ACOS)
              .operand1(BigDecimal.ONE)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should calculate arctangent")
    void testArcTangent() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.ATAN)
              .operand1(BigDecimal.ZERO)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should calculate natural logarithm")
    void testNaturalLog() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.LOG)
              .operand1(BigDecimal.ONE)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.ZERO.compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should calculate base-10 logarithm")
    void testLog10() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.LOG10)
              .operand1(BigDecimal.TEN)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.ONE.compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should calculate exponential")
    void testExponential() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.EXP)
              .operand1(BigDecimal.ZERO)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.ONE.compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should calculate ceiling")
    void testCeiling() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.CEIL)
              .operand1(BigDecimal.valueOf(3.2))
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(4).compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should calculate floor")
    void testFloor() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.FLOOR)
              .operand1(BigDecimal.valueOf(3.8))
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(3).compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should round number")
    void testRound() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.ROUND)
              .operand1(BigDecimal.valueOf(3.14159))
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(3.14).compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should fail when scientific operations are disabled")
    void testScientificOperationsDisabled() {
      CalculatorConfig config = CalculatorConfig.basic();
      CalculatorTool basicCalculator = new CalculatorTool(config);

      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.SIN)
              .operand1(BigDecimal.ZERO)
              .precision(2)
              .build();

      CalculationResult result = basicCalculator.calculate(request).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getErrorMessage()).contains("Scientific operations are disabled");
    }
  }

  @Nested
  @DisplayName("Statistical Operations")
  class StatisticalOperationsTests {

    @Test
    @DisplayName("Should calculate sum")
    void testSum() {
      List<BigDecimal> operands = List.of(BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(5));

      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.SUM)
              .operands(operands)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(16).compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should calculate average")
    void testAverage() {
      List<BigDecimal> operands =
          List.of(BigDecimal.valueOf(10), BigDecimal.valueOf(20), BigDecimal.valueOf(30));

      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.AVERAGE)
              .operands(operands)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(20).compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should find minimum")
    void testMin() {
      List<BigDecimal> operands =
          List.of(BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(20));

      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.MIN)
              .operands(operands)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(5).compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should find maximum")
    void testMax() {
      List<BigDecimal> operands =
          List.of(BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(20));

      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.MAX)
              .operands(operands)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(20).compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should calculate median with odd number of elements")
    void testMedianOdd() {
      List<BigDecimal> operands =
          List.of(BigDecimal.valueOf(1), BigDecimal.valueOf(5), BigDecimal.valueOf(3));

      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.MEDIAN)
              .operands(operands)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(3).compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should calculate median with even number of elements")
    void testMedianEven() {
      List<BigDecimal> operands =
          List.of(
              BigDecimal.valueOf(1),
              BigDecimal.valueOf(2),
              BigDecimal.valueOf(3),
              BigDecimal.valueOf(4));

      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.MEDIAN)
              .operands(operands)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      // Median of [1,2,3,4] is (2+3)/2 = 2.5
      assertThat(result.getResult().doubleValue()).isCloseTo(2.5, within(0.01));
    }

    @Test
    @DisplayName("Should calculate variance")
    void testVariance() {
      List<BigDecimal> operands =
          List.of(BigDecimal.valueOf(2), BigDecimal.valueOf(4), BigDecimal.valueOf(6));

      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.VARIANCE)
              .operands(operands)
              .precision(4)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      // Variance = ((2-4)^2 + (4-4)^2 + (6-4)^2) / 3 = (4 + 0 + 4) / 3 = 2.6667
      assertThat(result.getResult().doubleValue()).isCloseTo(2.6667, within(0.01));
    }

    @Test
    @DisplayName("Should calculate standard deviation")
    void testStandardDeviation() {
      List<BigDecimal> operands =
          List.of(BigDecimal.valueOf(2), BigDecimal.valueOf(4), BigDecimal.valueOf(6));

      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.STDDEV)
              .operands(operands)
              .precision(4)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      // StdDev = sqrt(2.6667) ≈ 1.633
      assertThat(result.getResult().doubleValue()).isCloseTo(1.633, within(0.01));
    }

    @Test
    @DisplayName("Should fail with null operands list")
    void testNullOperands() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.SUM)
              .operands(null)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getErrorMessage()).contains("cannot be null or empty");
    }

    @Test
    @DisplayName("Should fail with empty operands list")
    void testEmptyOperands() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.SUM)
              .operands(List.of())
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getErrorMessage()).contains("cannot be null or empty");
    }

    @Test
    @DisplayName("Should fail when statistical operations are disabled")
    void testStatisticalOperationsDisabled() {
      CalculatorConfig config = CalculatorConfig.basic();
      CalculatorTool basicCalculator = new CalculatorTool(config);

      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.SUM)
              .operands(List.of(BigDecimal.ONE, BigDecimal.TEN))
              .precision(2)
              .build();

      CalculationResult result = basicCalculator.calculate(request).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getErrorMessage()).contains("Statistical operations are disabled");
    }
  }

  @Nested
  @DisplayName("Financial Operations")
  class FinancialOperationsTests {

    @Test
    @DisplayName("Should calculate compound interest")
    void testCompoundInterest() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.COMPOUND_INTEREST)
              .operand1(BigDecimal.valueOf(1000)) // Principal
              .parameter(BigDecimal.valueOf(0.05)) // 5% annual rate
              .periods(10) // 10 years
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      // FV = 1000 * (1.05)^10 ≈ 1628.89
      assertThat(result.getResult().doubleValue()).isCloseTo(1628.89, within(1.0));
    }

    @Test
    @DisplayName("Should calculate simple interest")
    void testSimpleInterest() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.SIMPLE_INTEREST)
              .operand1(BigDecimal.valueOf(1000)) // Principal
              .parameter(BigDecimal.valueOf(0.05)) // 5% annual rate
              .periods(10) // 10 years
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      // I = 1000 * 0.05 * 10 = 500
      assertEquals(0, BigDecimal.valueOf(500).compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should calculate present value")
    void testPresentValue() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.PRESENT_VALUE)
              .operand1(BigDecimal.valueOf(1000)) // Future value
              .parameter(BigDecimal.valueOf(0.05)) // 5% discount rate
              .periods(5) // 5 years
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      // PV = 1000 / (1.05)^5 ≈ 783.53
      assertThat(result.getResult().doubleValue()).isCloseTo(783.53, within(1.0));
    }

    @Test
    @DisplayName("Should calculate annuity payment")
    void testAnnuity() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.ANNUITY)
              .operand1(BigDecimal.valueOf(10000)) // Present value (loan amount)
              .parameter(BigDecimal.valueOf(0.05)) // 5% interest rate
              .periods(5) // 5 periods
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      // Payment should be positive number
      assertThat(result.getResult().doubleValue()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should calculate NPV")
    void testNPV() {
      List<BigDecimal> cashFlows =
          List.of(
              BigDecimal.valueOf(-1000), // Initial investment
              BigDecimal.valueOf(300),
              BigDecimal.valueOf(300),
              BigDecimal.valueOf(300),
              BigDecimal.valueOf(300));

      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.NPV)
              .operands(cashFlows)
              .parameter(BigDecimal.valueOf(0.1)) // 10% discount rate
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      // NPV should be close to -49.25
      assertThat(result.getResult().doubleValue()).isCloseTo(-49.25, within(5.0));
    }

    @Test
    @DisplayName("Should fail IRR calculation (not implemented)")
    void testIRR() {
      List<BigDecimal> cashFlows =
          List.of(
              BigDecimal.valueOf(-1000),
              BigDecimal.valueOf(300),
              BigDecimal.valueOf(300),
              BigDecimal.valueOf(300),
              BigDecimal.valueOf(300));

      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.IRR)
              .operands(cashFlows)
              .precision(2)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getErrorMessage()).contains("IRR calculation not yet implemented");
    }

    @Test
    @DisplayName("Should fail when financial operations are disabled")
    void testFinancialOperationsDisabled() {
      CalculatorConfig config = CalculatorConfig.basic();
      CalculatorTool basicCalculator = new CalculatorTool(config);

      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.SIMPLE_INTEREST)
              .operand1(BigDecimal.valueOf(1000))
              .parameter(BigDecimal.valueOf(0.05))
              .periods(10)
              .precision(2)
              .build();

      CalculationResult result = basicCalculator.calculate(request).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getErrorMessage()).contains("Financial operations are disabled");
    }
  }

  @Nested
  @DisplayName("Configuration Tests")
  class ConfigurationTests {

    @Test
    @DisplayName("Should use default configuration")
    void testDefaultConfig() {
      CalculatorConfig config = CalculatorConfig.defaults();

      assertEquals(2, config.getDefaultPrecision());
      assertEquals(10, config.getMaxPrecision());
      assertEquals(RoundingMode.HALF_UP, config.getRoundingMode());
      assertTrue(config.isEnableScientific());
      assertTrue(config.isEnableFinancial());
      assertTrue(config.isEnableStatistical());
    }

    @Test
    @DisplayName("Should use basic configuration")
    void testBasicConfig() {
      CalculatorConfig config = CalculatorConfig.basic();

      assertFalse(config.isEnableScientific());
      assertFalse(config.isEnableFinancial());
      assertFalse(config.isEnableStatistical());
    }

    @Test
    @DisplayName("Should use high precision configuration")
    void testHighPrecisionConfig() {
      CalculatorConfig config = CalculatorConfig.highPrecision();

      assertEquals(10, config.getDefaultPrecision());
      assertEquals(34, config.getMaxPrecision());
    }

    @Test
    @DisplayName("Should respect custom precision")
    void testCustomPrecision() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.DIVIDE)
              .operand1(BigDecimal.valueOf(10))
              .operand2(BigDecimal.valueOf(3))
              .precision(4)
              .build();

      CalculationResult result = calculator.calculate(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(4, result.getResult().scale());
    }

    @Test
    @DisplayName("Should create calculator with no-arg constructor")
    void testNoArgConstructor() {
      CalculatorTool calc = new CalculatorTool();
      assertNotNull(calc);

      CalculationResult result = calc.add(BigDecimal.ONE, BigDecimal.ONE).block();
      assertNotNull(result);
      assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should create calculator with custom config")
    void testCustomConfigConstructor() {
      CalculatorConfig config =
          CalculatorConfig.builder().defaultPrecision(5).roundingMode(RoundingMode.DOWN).build();

      CalculatorTool calc = new CalculatorTool(config);
      assertNotNull(calc);

      CalculationResult result = calc.add(BigDecimal.ONE, BigDecimal.ONE).block();
      assertNotNull(result);
      assertTrue(result.isSuccess());
    }
  }

  @Nested
  @DisplayName("Result Model Tests")
  class ResultModelTests {

    @Test
    @DisplayName("Should create success result")
    void testSuccessResult() {
      CalculationResult result = CalculationResult.success(CalculationType.ADD, BigDecimal.TEN);

      assertTrue(result.isSuccess());
      assertEquals(CalculationType.ADD, result.getType());
      assertEquals(0, BigDecimal.TEN.compareTo(result.getResult()));
      assertEquals("10", result.getFormattedResult());
      assertNotNull(result.getTimestamp());
      assertNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("Should create success result with formatted output")
    void testSuccessResultWithFormatting() {
      CalculationResult result =
          CalculationResult.success(CalculationType.ADD, BigDecimal.valueOf(10.5), "$10.50");

      assertTrue(result.isSuccess());
      assertEquals("$10.50", result.getFormattedResult());
    }

    @Test
    @DisplayName("Should create error result")
    void testErrorResult() {
      CalculationResult result =
          CalculationResult.error(CalculationType.DIVIDE, "Division by zero");

      assertFalse(result.isSuccess());
      assertEquals(CalculationType.DIVIDE, result.getType());
      assertEquals("Division by zero", result.getErrorMessage());
      assertNull(result.getResult());
    }

    @Test
    @DisplayName("Should create result with builder")
    void testBuilderPattern() {
      CalculationResult result =
          CalculationResult.builder()
              .type(CalculationType.MULTIPLY)
              .result(BigDecimal.valueOf(42))
              .success(true)
              .formattedResult("42.00")
              .steps("10 * 4.2 = 42")
              .build();

      assertTrue(result.isSuccess());
      assertEquals(CalculationType.MULTIPLY, result.getType());
      assertEquals("42.00", result.getFormattedResult());
      assertEquals("10 * 4.2 = 42", result.getSteps());
    }
  }

  @Nested
  @DisplayName("Edge Cases and Error Handling")
  class EdgeCasesTests {

    @Test
    @DisplayName("Should handle very large numbers")
    void testLargeNumbers() {
      BigDecimal largeNumber = new BigDecimal("999999999999999999999999999999");

      CalculationResult result = calculator.add(largeNumber, BigDecimal.ONE).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle very small numbers")
    void testSmallNumbers() {
      BigDecimal smallNumber = new BigDecimal("0.000000000000000001");

      CalculationResult result = calculator.add(smallNumber, smallNumber).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle too many operands")
    void testTooManyOperands() {
      CalculatorConfig config = CalculatorConfig.builder().maxOperands(10).build();
      CalculatorTool calc = new CalculatorTool(config);

      // Create 11 operands (exceeds max of 10)
      List<BigDecimal> operands =
          List.of(
              BigDecimal.ONE,
              BigDecimal.ONE,
              BigDecimal.ONE,
              BigDecimal.ONE,
              BigDecimal.ONE,
              BigDecimal.ONE,
              BigDecimal.ONE,
              BigDecimal.ONE,
              BigDecimal.ONE,
              BigDecimal.ONE,
              BigDecimal.ONE);

      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.SUM)
              .operands(operands)
              .precision(2)
              .build();

      CalculationResult result = calc.calculate(request).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getErrorMessage()).contains("Too many operands");
    }

    @Test
    @DisplayName("Should handle addition with negative numbers")
    void testAdditionWithNegatives() {
      CalculationResult result =
          calculator.add(BigDecimal.valueOf(-10), BigDecimal.valueOf(5)).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.valueOf(-5).compareTo(result.getResult()));
    }

    @Test
    @DisplayName("Should handle zero operations")
    void testZeroOperations() {
      CalculationResult result = calculator.add(BigDecimal.ZERO, BigDecimal.ZERO).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, BigDecimal.ZERO.compareTo(result.getResult()));
    }
  }
}
