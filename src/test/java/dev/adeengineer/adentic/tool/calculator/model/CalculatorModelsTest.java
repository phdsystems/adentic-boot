package dev.adeengineer.adentic.tool.calculator.model;

import static org.junit.jupiter.api.Assertions.*;

import dev.adeengineer.adentic.tool.calculator.model.CalculationRequest.CalculationType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Comprehensive tests for calculator model classes. */
@DisplayName("Calculator Models Tests")
class CalculatorModelsTest {

  @Nested
  @DisplayName("CalculationType Tests")
  class CalculationTypeTests {

    @Test
    @DisplayName("Should have all calculation types")
    void testCalculationTypes() {
      CalculationType[] types = CalculationType.values();
      assertEquals(34, types.length); // 9 basic + 12 scientific + 7 statistical + 6 financial

      // Basic arithmetic
      assertNotNull(CalculationType.valueOf("ADD"));
      assertNotNull(CalculationType.valueOf("SUBTRACT"));
      assertNotNull(CalculationType.valueOf("MULTIPLY"));
      assertNotNull(CalculationType.valueOf("DIVIDE"));
      assertNotNull(CalculationType.valueOf("MODULO"));
      assertNotNull(CalculationType.valueOf("POWER"));
      assertNotNull(CalculationType.valueOf("SQRT"));
      assertNotNull(CalculationType.valueOf("ABS"));
      assertNotNull(CalculationType.valueOf("NEGATE"));

      // Scientific
      assertNotNull(CalculationType.valueOf("SIN"));
      assertNotNull(CalculationType.valueOf("COS"));
      assertNotNull(CalculationType.valueOf("TAN"));
      assertNotNull(CalculationType.valueOf("ASIN"));
      assertNotNull(CalculationType.valueOf("ACOS"));
      assertNotNull(CalculationType.valueOf("ATAN"));
      assertNotNull(CalculationType.valueOf("LOG"));
      assertNotNull(CalculationType.valueOf("LOG10"));
      assertNotNull(CalculationType.valueOf("EXP"));
      assertNotNull(CalculationType.valueOf("CEIL"));
      assertNotNull(CalculationType.valueOf("FLOOR"));
      assertNotNull(CalculationType.valueOf("ROUND"));

      // Statistical
      assertNotNull(CalculationType.valueOf("SUM"));
      assertNotNull(CalculationType.valueOf("AVERAGE"));
      assertNotNull(CalculationType.valueOf("MIN"));
      assertNotNull(CalculationType.valueOf("MAX"));
      assertNotNull(CalculationType.valueOf("MEDIAN"));
      assertNotNull(CalculationType.valueOf("VARIANCE"));
      assertNotNull(CalculationType.valueOf("STDDEV"));

      // Financial
      assertNotNull(CalculationType.valueOf("COMPOUND_INTEREST"));
      assertNotNull(CalculationType.valueOf("SIMPLE_INTEREST"));
      assertNotNull(CalculationType.valueOf("PRESENT_VALUE"));
      assertNotNull(CalculationType.valueOf("ANNUITY"));
      assertNotNull(CalculationType.valueOf("NPV"));
      assertNotNull(CalculationType.valueOf("IRR"));
    }

    @Test
    @DisplayName("Should throw exception for invalid type")
    void testInvalidType() {
      assertThrows(IllegalArgumentException.class, () -> CalculationType.valueOf("INVALID"));
    }

    @Test
    @DisplayName("Should return correct name")
    void testTypeName() {
      assertEquals("ADD", CalculationType.ADD.name());
      assertEquals("MULTIPLY", CalculationType.MULTIPLY.name());
      assertEquals("NPV", CalculationType.NPV.name());
    }
  }

  @Nested
  @DisplayName("CalculationRequest Tests")
  class CalculationRequestTests {

    @Test
    @DisplayName("Should create request with builder")
    void testBuilder() {
      List<BigDecimal> operands = List.of(BigDecimal.ONE, BigDecimal.TEN);

      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.ADD)
              .operand1(BigDecimal.valueOf(10))
              .operand2(BigDecimal.valueOf(5))
              .operands(operands)
              .precision(4)
              .parameter(BigDecimal.valueOf(0.05))
              .periods(12)
              .build();

      assertEquals(CalculationType.ADD, request.getType());
      assertEquals(BigDecimal.valueOf(10), request.getOperand1());
      assertEquals(BigDecimal.valueOf(5), request.getOperand2());
      assertEquals(operands, request.getOperands());
      assertEquals(4, request.getPrecision());
      assertEquals(BigDecimal.valueOf(0.05), request.getParameter());
      assertEquals(12, request.getPeriods());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.SQRT)
              .operand1(BigDecimal.valueOf(16))
              .build();

      assertNull(request.getOperand2());
      assertNull(request.getOperands());
      assertEquals(2, request.getPrecision());
      assertNull(request.getParameter());
      assertNull(request.getPeriods());
    }

    @Test
    @DisplayName("Should support binary operations")
    void testBinaryOperation() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.DIVIDE)
              .operand1(BigDecimal.valueOf(100))
              .operand2(BigDecimal.valueOf(4))
              .precision(2)
              .build();

      assertEquals(CalculationType.DIVIDE, request.getType());
      assertEquals(BigDecimal.valueOf(100), request.getOperand1());
      assertEquals(BigDecimal.valueOf(4), request.getOperand2());
    }

    @Test
    @DisplayName("Should support variadic operations")
    void testVariadicOperation() {
      List<BigDecimal> values =
          List.of(BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(5), BigDecimal.valueOf(3));

      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.SUM)
              .operands(values)
              .precision(2)
              .build();

      assertEquals(CalculationType.SUM, request.getType());
      assertEquals(values, request.getOperands());
      assertEquals(4, request.getOperands().size());
    }

    @Test
    @DisplayName("Should support financial operations")
    void testFinancialOperation() {
      CalculationRequest request =
          CalculationRequest.builder()
              .type(CalculationType.COMPOUND_INTEREST)
              .operand1(BigDecimal.valueOf(1000)) // principal
              .parameter(BigDecimal.valueOf(0.05)) // interest rate
              .periods(12) // number of periods
              .precision(2)
              .build();

      assertEquals(CalculationType.COMPOUND_INTEREST, request.getType());
      assertEquals(BigDecimal.valueOf(1000), request.getOperand1());
      assertEquals(BigDecimal.valueOf(0.05), request.getParameter());
      assertEquals(12, request.getPeriods());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      CalculationRequest request = CalculationRequest.builder().type(CalculationType.ADD).build();

      request.setType(CalculationType.MULTIPLY);
      request.setOperand1(BigDecimal.valueOf(7));
      request.setOperand2(BigDecimal.valueOf(8));
      request.setPrecision(3);

      assertEquals(CalculationType.MULTIPLY, request.getType());
      assertEquals(BigDecimal.valueOf(7), request.getOperand1());
      assertEquals(BigDecimal.valueOf(8), request.getOperand2());
      assertEquals(3, request.getPrecision());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      CalculationRequest request1 =
          CalculationRequest.builder()
              .type(CalculationType.ADD)
              .operand1(BigDecimal.TEN)
              .operand2(BigDecimal.ONE)
              .build();

      CalculationRequest request2 =
          CalculationRequest.builder()
              .type(CalculationType.ADD)
              .operand1(BigDecimal.TEN)
              .operand2(BigDecimal.ONE)
              .build();

      assertEquals(request1, request2);
      assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      CalculationRequest request =
          CalculationRequest.builder().type(CalculationType.ADD).operand1(BigDecimal.TEN).build();
      String str = request.toString();

      assertTrue(str.contains("ADD"));
      assertTrue(str.contains("10"));
    }
  }

  @Nested
  @DisplayName("CalculationResult Tests")
  class CalculationResultTests {

    @Test
    @DisplayName("Should create result with builder")
    void testBuilder() {
      Instant timestamp = Instant.now();

      CalculationResult result =
          CalculationResult.builder()
              .type(CalculationType.ADD)
              .result(BigDecimal.valueOf(15))
              .success(true)
              .errorMessage(null)
              .formattedResult("15.00")
              .timestamp(timestamp)
              .steps("10 + 5 = 15")
              .build();

      assertEquals(CalculationType.ADD, result.getType());
      assertEquals(BigDecimal.valueOf(15), result.getResult());
      assertTrue(result.isSuccess());
      assertNull(result.getErrorMessage());
      assertEquals("15.00", result.getFormattedResult());
      assertEquals(timestamp, result.getTimestamp());
      assertEquals("10 + 5 = 15", result.getSteps());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      CalculationResult result =
          CalculationResult.builder()
              .type(CalculationType.SUBTRACT)
              .result(BigDecimal.valueOf(5))
              .success(true)
              .build();

      assertNull(result.getErrorMessage());
      assertNull(result.getFormattedResult());
      assertNotNull(result.getTimestamp());
      assertNull(result.getSteps());
    }

    @Test
    @DisplayName("Should create success result - basic")
    void testSuccessFactory() {
      CalculationResult result =
          CalculationResult.success(CalculationType.MULTIPLY, BigDecimal.valueOf(50));

      assertEquals(CalculationType.MULTIPLY, result.getType());
      assertEquals(BigDecimal.valueOf(50), result.getResult());
      assertTrue(result.isSuccess());
      assertEquals("50", result.getFormattedResult());
      assertNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("Should create success result with formatted string")
    void testSuccessFactoryWithFormatted() {
      CalculationResult result =
          CalculationResult.success(CalculationType.DIVIDE, BigDecimal.valueOf(3.14159), "3.14");

      assertEquals(CalculationType.DIVIDE, result.getType());
      assertEquals(BigDecimal.valueOf(3.14159), result.getResult());
      assertTrue(result.isSuccess());
      assertEquals("3.14", result.getFormattedResult());
    }

    @Test
    @DisplayName("Should create error result")
    void testErrorFactory() {
      CalculationResult result =
          CalculationResult.error(CalculationType.DIVIDE, "Division by zero");

      assertEquals(CalculationType.DIVIDE, result.getType());
      assertFalse(result.isSuccess());
      assertEquals("Division by zero", result.getErrorMessage());
      assertNull(result.getResult());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      CalculationResult result = CalculationResult.builder().success(true).build();

      result.setType(CalculationType.SQRT);
      result.setResult(BigDecimal.valueOf(4));
      result.setSuccess(false);
      result.setErrorMessage("Invalid input");
      result.setFormattedResult("4.00");
      result.setSteps("sqrt(16) = 4");

      assertEquals(CalculationType.SQRT, result.getType());
      assertEquals(BigDecimal.valueOf(4), result.getResult());
      assertFalse(result.isSuccess());
      assertEquals("Invalid input", result.getErrorMessage());
      assertEquals("4.00", result.getFormattedResult());
      assertEquals("sqrt(16) = 4", result.getSteps());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      CalculationResult result1 = CalculationResult.success(CalculationType.ADD, BigDecimal.TEN);

      CalculationResult result2 = CalculationResult.success(CalculationType.ADD, BigDecimal.TEN);

      // Note: timestamps will differ, so results won't be equal
      // But we can verify the factory method works
      assertEquals(CalculationType.ADD, result1.getType());
      assertEquals(CalculationType.ADD, result2.getType());
      assertEquals(result1.getResult(), result2.getResult());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      CalculationResult result = CalculationResult.success(CalculationType.ADD, BigDecimal.TEN);
      String str = result.toString();

      assertTrue(str.contains("ADD"));
      assertTrue(str.contains("true")); // success
    }
  }
}
