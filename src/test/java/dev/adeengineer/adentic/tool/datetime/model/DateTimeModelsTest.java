package dev.adeengineer.adentic.tool.datetime.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Comprehensive tests for datetime model classes. */
@DisplayName("DateTime Models Tests")
class DateTimeModelsTest {

  @Nested
  @DisplayName("DateTimeOperation Tests")
  class DateTimeOperationTests {

    @Test
    @DisplayName("Should have all datetime operations")
    void testDateTimeOperations() {
      DateTimeOperation[] ops = DateTimeOperation.values();
      assertEquals(40, ops.length);

      // Parsing and formatting
      assertNotNull(DateTimeOperation.valueOf("PARSE"));
      assertNotNull(DateTimeOperation.valueOf("FORMAT"));
      assertNotNull(DateTimeOperation.valueOf("TO_ISO8601"));
      assertNotNull(DateTimeOperation.valueOf("FROM_ISO8601"));

      // Timezone operations
      assertNotNull(DateTimeOperation.valueOf("CONVERT_TIMEZONE"));
      assertNotNull(DateTimeOperation.valueOf("GET_TIMEZONE_OFFSET"));
      assertNotNull(DateTimeOperation.valueOf("LIST_TIMEZONES"));

      // Date arithmetic
      assertNotNull(DateTimeOperation.valueOf("ADD_DAYS"));
      assertNotNull(DateTimeOperation.valueOf("ADD_MONTHS"));
      assertNotNull(DateTimeOperation.valueOf("ADD_YEARS"));
      assertNotNull(DateTimeOperation.valueOf("ADD_HOURS"));
      assertNotNull(DateTimeOperation.valueOf("ADD_MINUTES"));
      assertNotNull(DateTimeOperation.valueOf("ADD_SECONDS"));
      assertNotNull(DateTimeOperation.valueOf("SUBTRACT_DAYS"));
      assertNotNull(DateTimeOperation.valueOf("SUBTRACT_MONTHS"));
      assertNotNull(DateTimeOperation.valueOf("SUBTRACT_YEARS"));

      // Duration calculations
      assertNotNull(DateTimeOperation.valueOf("DURATION_BETWEEN"));
      assertNotNull(DateTimeOperation.valueOf("DAYS_BETWEEN"));
      assertNotNull(DateTimeOperation.valueOf("HOURS_BETWEEN"));
      assertNotNull(DateTimeOperation.valueOf("MINUTES_BETWEEN"));
      assertNotNull(DateTimeOperation.valueOf("SECONDS_BETWEEN"));

      // Calendar operations
      assertNotNull(DateTimeOperation.valueOf("START_OF_DAY"));
      assertNotNull(DateTimeOperation.valueOf("END_OF_DAY"));
      assertNotNull(DateTimeOperation.valueOf("START_OF_WEEK"));
      assertNotNull(DateTimeOperation.valueOf("END_OF_WEEK"));
      assertNotNull(DateTimeOperation.valueOf("START_OF_MONTH"));
      assertNotNull(DateTimeOperation.valueOf("END_OF_MONTH"));
      assertNotNull(DateTimeOperation.valueOf("START_OF_YEAR"));
      assertNotNull(DateTimeOperation.valueOf("END_OF_YEAR"));

      // Information
      assertNotNull(DateTimeOperation.valueOf("DAY_OF_WEEK"));
      assertNotNull(DateTimeOperation.valueOf("DAY_OF_MONTH"));
      assertNotNull(DateTimeOperation.valueOf("DAY_OF_YEAR"));
      assertNotNull(DateTimeOperation.valueOf("WEEK_OF_YEAR"));
      assertNotNull(DateTimeOperation.valueOf("IS_LEAP_YEAR"));
      assertNotNull(DateTimeOperation.valueOf("IS_WEEKEND"));

      // Utilities
      assertNotNull(DateTimeOperation.valueOf("NOW"));
      assertNotNull(DateTimeOperation.valueOf("TODAY"));
      assertNotNull(DateTimeOperation.valueOf("TIMESTAMP"));
      assertNotNull(DateTimeOperation.valueOf("EPOCH_MILLIS"));
      assertNotNull(DateTimeOperation.valueOf("FROM_EPOCH_MILLIS"));
    }

    @Test
    @DisplayName("Should throw exception for invalid operation")
    void testInvalidOperation() {
      assertThrows(IllegalArgumentException.class, () -> DateTimeOperation.valueOf("INVALID"));
    }

    @Test
    @DisplayName("Should return correct name")
    void testOperationName() {
      assertEquals("PARSE", DateTimeOperation.PARSE.name());
      assertEquals("ADD_DAYS", DateTimeOperation.ADD_DAYS.name());
      assertEquals("CONVERT_TIMEZONE", DateTimeOperation.CONVERT_TIMEZONE.name());
    }
  }

  @Nested
  @DisplayName("DateTimeRequest Tests")
  class DateTimeRequestTests {

    @Test
    @DisplayName("Should create request with builder")
    void testBuilder() {
      ZoneId utc = ZoneId.of("UTC");
      ZoneId est = ZoneId.of("America/New_York");

      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.CONVERT_TIMEZONE)
              .dateTime("2024-01-15T10:30:00")
              .inputFormat("yyyy-MM-dd'T'HH:mm:ss")
              .outputFormat("MM/dd/yyyy HH:mm")
              .sourceTimezone(utc)
              .targetTimezone(est)
              .amount(5L)
              .secondDateTime("2024-01-20T10:30:00")
              .epochMillis(1705318200000L)
              .build();

      assertEquals(DateTimeOperation.CONVERT_TIMEZONE, request.getOperation());
      assertEquals("2024-01-15T10:30:00", request.getDateTime());
      assertEquals("yyyy-MM-dd'T'HH:mm:ss", request.getInputFormat());
      assertEquals("MM/dd/yyyy HH:mm", request.getOutputFormat());
      assertEquals(utc, request.getSourceTimezone());
      assertEquals(est, request.getTargetTimezone());
      assertEquals(5L, request.getAmount());
      assertEquals("2024-01-20T10:30:00", request.getSecondDateTime());
      assertEquals(1705318200000L, request.getEpochMillis());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      DateTimeRequest request = DateTimeRequest.builder().operation(DateTimeOperation.NOW).build();

      assertNull(request.getDateTime());
      assertNull(request.getInputFormat());
      assertNull(request.getOutputFormat());
      assertEquals(ZoneId.systemDefault(), request.getSourceTimezone());
      assertEquals(ZoneId.systemDefault(), request.getTargetTimezone());
      assertEquals(0L, request.getAmount());
      assertNull(request.getSecondDateTime());
      assertNull(request.getEpochMillis());
    }

    @Test
    @DisplayName("Should create parse request")
    void testParseFactory() {
      DateTimeRequest request = DateTimeRequest.parse("2024-01-15", "yyyy-MM-dd");

      assertEquals(DateTimeOperation.PARSE, request.getOperation());
      assertEquals("2024-01-15", request.getDateTime());
      assertEquals("yyyy-MM-dd", request.getInputFormat());
    }

    @Test
    @DisplayName("Should create format request")
    void testFormatFactory() {
      DateTimeRequest request = DateTimeRequest.format("2024-01-15T10:30:00", "MM/dd/yyyy HH:mm");

      assertEquals(DateTimeOperation.FORMAT, request.getOperation());
      assertEquals("2024-01-15T10:30:00", request.getDateTime());
      assertEquals("MM/dd/yyyy HH:mm", request.getOutputFormat());
    }

    @Test
    @DisplayName("Should create timezone conversion request")
    void testConvertTimezoneFactory() {
      ZoneId utc = ZoneId.of("UTC");
      ZoneId pst = ZoneId.of("America/Los_Angeles");

      DateTimeRequest request = DateTimeRequest.convertTimezone("2024-01-15T10:30:00", utc, pst);

      assertEquals(DateTimeOperation.CONVERT_TIMEZONE, request.getOperation());
      assertEquals("2024-01-15T10:30:00", request.getDateTime());
      assertEquals(utc, request.getSourceTimezone());
      assertEquals(pst, request.getTargetTimezone());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      DateTimeRequest request = DateTimeRequest.builder().operation(DateTimeOperation.NOW).build();

      request.setOperation(DateTimeOperation.ADD_DAYS);
      request.setDateTime("2024-01-15");
      request.setAmount(7L);
      request.setOutputFormat("yyyy-MM-dd");

      assertEquals(DateTimeOperation.ADD_DAYS, request.getOperation());
      assertEquals("2024-01-15", request.getDateTime());
      assertEquals(7L, request.getAmount());
      assertEquals("yyyy-MM-dd", request.getOutputFormat());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      DateTimeRequest request1 =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.PARSE)
              .dateTime("2024-01-15")
              .build();

      DateTimeRequest request2 =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.PARSE)
              .dateTime("2024-01-15")
              .build();

      assertEquals(request1, request2);
      assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.PARSE)
              .dateTime("2024-01-15")
              .build();
      String str = request.toString();

      assertTrue(str.contains("PARSE"));
      assertTrue(str.contains("2024-01-15"));
    }
  }

  @Nested
  @DisplayName("DateTimeResult Tests")
  class DateTimeResultTests {

    @Test
    @DisplayName("Should create result with builder")
    void testBuilder() {
      ZonedDateTime zdt = ZonedDateTime.now();
      Instant instant = Instant.now();

      DateTimeResult result =
          DateTimeResult.builder()
              .operation(DateTimeOperation.PARSE)
              .result("2024-01-15T10:30:00")
              .zonedDateTime(zdt)
              .instant(instant)
              .epochMillis(1705318200000L)
              .success(true)
              .errorMessage(null)
              .metadata("UTC timezone")
              .build();

      assertEquals(DateTimeOperation.PARSE, result.getOperation());
      assertEquals("2024-01-15T10:30:00", result.getResult());
      assertEquals(zdt, result.getZonedDateTime());
      assertEquals(instant, result.getInstant());
      assertEquals(1705318200000L, result.getEpochMillis());
      assertTrue(result.isSuccess());
      assertNull(result.getErrorMessage());
      assertEquals("UTC timezone", result.getMetadata());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      DateTimeResult result =
          DateTimeResult.builder().operation(DateTimeOperation.NOW).success(true).build();

      assertNull(result.getResult());
      assertNull(result.getZonedDateTime());
      assertNull(result.getInstant());
      assertNull(result.getEpochMillis());
      assertNull(result.getErrorMessage());
      assertNull(result.getMetadata());
    }

    @Test
    @DisplayName("Should create success result with string")
    void testSuccessFactoryWithString() {
      DateTimeResult result = DateTimeResult.success(DateTimeOperation.FORMAT, "01/15/2024 10:30");

      assertEquals(DateTimeOperation.FORMAT, result.getOperation());
      assertTrue(result.isSuccess());
      assertEquals("01/15/2024 10:30", result.getResult());
      assertNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("Should create success result with ZonedDateTime")
    void testSuccessFactoryWithZonedDateTime() {
      ZonedDateTime zdt = ZonedDateTime.now();
      DateTimeResult result = DateTimeResult.success(DateTimeOperation.PARSE, zdt);

      assertEquals(DateTimeOperation.PARSE, result.getOperation());
      assertTrue(result.isSuccess());
      assertEquals(zdt, result.getZonedDateTime());
      assertEquals(zdt.toString(), result.getResult());
    }

    @Test
    @DisplayName("Should create error result")
    void testErrorFactory() {
      DateTimeResult result = DateTimeResult.error(DateTimeOperation.PARSE, "Invalid date format");

      assertEquals(DateTimeOperation.PARSE, result.getOperation());
      assertFalse(result.isSuccess());
      assertEquals("Invalid date format", result.getErrorMessage());
      assertNull(result.getResult());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      DateTimeResult result =
          DateTimeResult.builder().operation(DateTimeOperation.NOW).success(true).build();

      result.setOperation(DateTimeOperation.FORMAT);
      result.setResult("01/15/2024");
      result.setSuccess(false);
      result.setErrorMessage("Format error");
      result.setEpochMillis(1705318200000L);
      result.setMetadata("Additional info");

      assertEquals(DateTimeOperation.FORMAT, result.getOperation());
      assertEquals("01/15/2024", result.getResult());
      assertFalse(result.isSuccess());
      assertEquals("Format error", result.getErrorMessage());
      assertEquals(1705318200000L, result.getEpochMillis());
      assertEquals("Additional info", result.getMetadata());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      DateTimeResult result1 = DateTimeResult.success(DateTimeOperation.FORMAT, "01/15/2024");

      DateTimeResult result2 = DateTimeResult.success(DateTimeOperation.FORMAT, "01/15/2024");

      assertEquals(result1, result2);
      assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      DateTimeResult result = DateTimeResult.success(DateTimeOperation.FORMAT, "01/15/2024");
      String str = result.toString();

      assertTrue(str.contains("FORMAT"));
      assertTrue(str.contains("true")); // success
    }
  }
}
