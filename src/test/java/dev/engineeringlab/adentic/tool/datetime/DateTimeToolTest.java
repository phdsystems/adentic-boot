package dev.engineeringlab.adentic.tool.datetime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import dev.engineeringlab.adentic.tool.datetime.config.DateTimeConfig;
import dev.engineeringlab.adentic.tool.datetime.model.DateTimeOperation;
import dev.engineeringlab.adentic.tool.datetime.model.DateTimeRequest;
import dev.engineeringlab.adentic.tool.datetime.model.DateTimeResult;
import java.time.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for DateTimeTool covering:
 *
 * <ul>
 *   <li>Parsing and formatting operations
 *   <li>Timezone operations
 *   <li>Date arithmetic operations
 *   <li>Duration calculations
 *   <li>Calendar operations
 *   <li>Information operations
 *   <li>Utility operations
 *   <li>Edge cases and error handling
 * </ul>
 */
@DisplayName("DateTimeTool Tests")
class DateTimeToolTest {

  private DateTimeTool dateTimeTool;
  private static final String TEST_DATETIME = "2025-10-31T12:00:00Z";

  @BeforeEach
  void setUp() {
    dateTimeTool = new DateTimeTool();
  }

  @Nested
  @DisplayName("Parsing and Formatting Operations")
  class ParsingFormattingTests {

    @Test
    @DisplayName("Should parse ISO 8601 datetime")
    void testParseIso8601() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.PARSE)
              .dateTime(TEST_DATETIME)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertNotNull(result.getZonedDateTime());
    }

    @Test
    @DisplayName("Should format datetime with custom format")
    void testFormat() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.FORMAT)
              .dateTime(TEST_DATETIME)
              .outputFormat("yyyy-MM-dd HH:mm:ss")
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertThat(result.getResult()).contains("2025-10-31");
    }

    @Test
    @DisplayName("Should convert to ISO 8601")
    void testToIso8601() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.TO_ISO8601)
              .dateTime(TEST_DATETIME)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertThat(result.getResult()).contains("2025-10-31");
    }

    @Test
    @DisplayName("Should parse from ISO 8601")
    void testFromIso8601() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.FROM_ISO8601)
              .dateTime(TEST_DATETIME)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertNotNull(result.getZonedDateTime());
    }
  }

  @Nested
  @DisplayName("Timezone Operations")
  class TimezoneTests {

    @Test
    @DisplayName("Should convert between timezones")
    void testConvertTimezone() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.CONVERT_TIMEZONE)
              .dateTime(TEST_DATETIME)
              .sourceTimezone(ZoneId.of("UTC"))
              .targetTimezone(ZoneId.of("America/New_York"))
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertNotNull(result.getZonedDateTime());
    }

    @Test
    @DisplayName("Should get timezone offset")
    void testGetTimezoneOffset() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.GET_TIMEZONE_OFFSET)
              .dateTime(TEST_DATETIME)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertThat(result.getResult()).contains("UTC");
    }

    @Test
    @DisplayName("Should list available timezones")
    void testListTimezones() {
      DateTimeRequest request =
          DateTimeRequest.builder().operation(DateTimeOperation.LIST_TIMEZONES).build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertThat(result.getResult()).contains("Africa");
    }
  }

  @Nested
  @DisplayName("Date Arithmetic Operations")
  class ArithmeticTests {

    @Test
    @DisplayName("Should add days")
    void testAddDays() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.ADD_DAYS)
              .dateTime(TEST_DATETIME)
              .amount(5L)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertNotNull(result.getZonedDateTime());
      assertEquals(5, result.getZonedDateTime().getDayOfMonth());
    }

    @Test
    @DisplayName("Should add months")
    void testAddMonths() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.ADD_MONTHS)
              .dateTime(TEST_DATETIME)
              .amount(2L)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertNotNull(result.getZonedDateTime());
    }

    @Test
    @DisplayName("Should add years")
    void testAddYears() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.ADD_YEARS)
              .dateTime(TEST_DATETIME)
              .amount(1L)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(2026, result.getZonedDateTime().getYear());
    }

    @Test
    @DisplayName("Should add hours")
    void testAddHours() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.ADD_HOURS)
              .dateTime(TEST_DATETIME)
              .amount(3L)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(15, result.getZonedDateTime().getHour());
    }

    @Test
    @DisplayName("Should add minutes")
    void testAddMinutes() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.ADD_MINUTES)
              .dateTime(TEST_DATETIME)
              .amount(30L)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(30, result.getZonedDateTime().getMinute());
    }

    @Test
    @DisplayName("Should add seconds")
    void testAddSeconds() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.ADD_SECONDS)
              .dateTime(TEST_DATETIME)
              .amount(45L)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(45, result.getZonedDateTime().getSecond());
    }

    @Test
    @DisplayName("Should subtract days")
    void testSubtractDays() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.SUBTRACT_DAYS)
              .dateTime(TEST_DATETIME)
              .amount(5L)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(26, result.getZonedDateTime().getDayOfMonth());
    }

    @Test
    @DisplayName("Should subtract months")
    void testSubtractMonths() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.SUBTRACT_MONTHS)
              .dateTime(TEST_DATETIME)
              .amount(2L)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(8, result.getZonedDateTime().getMonthValue());
    }

    @Test
    @DisplayName("Should subtract years")
    void testSubtractYears() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.SUBTRACT_YEARS)
              .dateTime(TEST_DATETIME)
              .amount(1L)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(2024, result.getZonedDateTime().getYear());
    }
  }

  @Nested
  @DisplayName("Duration Calculations")
  class DurationTests {

    @Test
    @DisplayName("Should calculate duration between two datetimes")
    void testDurationBetween() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.DURATION_BETWEEN)
              .dateTime(TEST_DATETIME)
              .secondDateTime("2025-11-01T12:00:00Z")
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertThat(result.getResult()).contains("1 days");
    }

    @Test
    @DisplayName("Should calculate days between")
    void testDaysBetween() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.DAYS_BETWEEN)
              .dateTime(TEST_DATETIME)
              .secondDateTime("2025-11-05T12:00:00Z")
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals("5", result.getResult());
    }

    @Test
    @DisplayName("Should calculate hours between")
    void testHoursBetween() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.HOURS_BETWEEN)
              .dateTime(TEST_DATETIME)
              .secondDateTime("2025-10-31T18:00:00Z")
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals("6", result.getResult());
    }

    @Test
    @DisplayName("Should calculate minutes between")
    void testMinutesBetween() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.MINUTES_BETWEEN)
              .dateTime(TEST_DATETIME)
              .secondDateTime("2025-10-31T13:00:00Z")
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals("60", result.getResult());
    }

    @Test
    @DisplayName("Should calculate seconds between")
    void testSecondsBetween() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.SECONDS_BETWEEN)
              .dateTime(TEST_DATETIME)
              .secondDateTime("2025-10-31T12:01:00Z")
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals("60", result.getResult());
    }
  }

  @Nested
  @DisplayName("Calendar Operations")
  class CalendarTests {

    @Test
    @DisplayName("Should get start of day")
    void testStartOfDay() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.START_OF_DAY)
              .dateTime(TEST_DATETIME)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, result.getZonedDateTime().getHour());
      assertEquals(0, result.getZonedDateTime().getMinute());
    }

    @Test
    @DisplayName("Should get end of day")
    void testEndOfDay() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.END_OF_DAY)
              .dateTime(TEST_DATETIME)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(23, result.getZonedDateTime().getHour());
    }

    @Test
    @DisplayName("Should get start of week")
    void testStartOfWeek() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.START_OF_WEEK)
              .dateTime(TEST_DATETIME)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(DayOfWeek.MONDAY, result.getZonedDateTime().getDayOfWeek());
    }

    @Test
    @DisplayName("Should get end of week")
    void testEndOfWeek() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.END_OF_WEEK)
              .dateTime(TEST_DATETIME)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(DayOfWeek.SUNDAY, result.getZonedDateTime().getDayOfWeek());
    }

    @Test
    @DisplayName("Should get start of month")
    void testStartOfMonth() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.START_OF_MONTH)
              .dateTime(TEST_DATETIME)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(1, result.getZonedDateTime().getDayOfMonth());
    }

    @Test
    @DisplayName("Should get end of month")
    void testEndOfMonth() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.END_OF_MONTH)
              .dateTime(TEST_DATETIME)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(31, result.getZonedDateTime().getDayOfMonth());
    }

    @Test
    @DisplayName("Should get start of year")
    void testStartOfYear() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.START_OF_YEAR)
              .dateTime(TEST_DATETIME)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(1, result.getZonedDateTime().getDayOfYear());
    }

    @Test
    @DisplayName("Should get end of year")
    void testEndOfYear() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.END_OF_YEAR)
              .dateTime(TEST_DATETIME)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(12, result.getZonedDateTime().getMonthValue());
      assertEquals(31, result.getZonedDateTime().getDayOfMonth());
    }
  }

  @Nested
  @DisplayName("Information Operations")
  class InformationTests {

    @Test
    @DisplayName("Should get day of week")
    void testDayOfWeek() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.DAY_OF_WEEK)
              .dateTime(TEST_DATETIME)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals("FRIDAY", result.getResult());
    }

    @Test
    @DisplayName("Should get day of month")
    void testDayOfMonth() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.DAY_OF_MONTH)
              .dateTime(TEST_DATETIME)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals("31", result.getResult());
    }

    @Test
    @DisplayName("Should get day of year")
    void testDayOfYear() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.DAY_OF_YEAR)
              .dateTime(TEST_DATETIME)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertThat(result.getResult()).isNotEmpty();
    }

    @Test
    @DisplayName("Should get week of year")
    void testWeekOfYear() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.WEEK_OF_YEAR)
              .dateTime(TEST_DATETIME)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertThat(result.getResult()).isNotEmpty();
    }

    @Test
    @DisplayName("Should check if leap year")
    void testIsLeapYear() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.IS_LEAP_YEAR)
              .dateTime("2024-01-01T00:00:00Z")
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals("true", result.getResult());
    }

    @Test
    @DisplayName("Should check if weekend")
    void testIsWeekend() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.IS_WEEKEND)
              .dateTime("2025-11-01T00:00:00Z") // Saturday
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals("true", result.getResult());
    }
  }

  @Nested
  @DisplayName("Utility Operations")
  class UtilityTests {

    @Test
    @DisplayName("Should get current datetime")
    void testNow() {
      DateTimeRequest request = DateTimeRequest.builder().operation(DateTimeOperation.NOW).build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertNotNull(result.getZonedDateTime());
    }

    @Test
    @DisplayName("Should get today")
    void testToday() {
      DateTimeRequest request =
          DateTimeRequest.builder().operation(DateTimeOperation.TODAY).build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals(0, result.getZonedDateTime().getHour());
    }

    @Test
    @DisplayName("Should get timestamp")
    void testTimestamp() {
      DateTimeRequest request =
          DateTimeRequest.builder().operation(DateTimeOperation.TIMESTAMP).build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertThat(result.getResult()).isNotEmpty();
    }

    @Test
    @DisplayName("Should convert to epoch millis")
    void testEpochMillis() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.EPOCH_MILLIS)
              .dateTime(TEST_DATETIME)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertThat(result.getEpochMillis()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should convert from epoch millis")
    void testFromEpochMillis() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.FROM_EPOCH_MILLIS)
              .epochMillis(1730376000000L)
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertNotNull(result.getZonedDateTime());
    }
  }

  @Nested
  @DisplayName("Convenience Methods")
  class ConvenienceTests {

    @Test
    @DisplayName("Should get current datetime using convenience method")
    void testGetCurrentDateTime() {
      DateTimeResult result = dateTimeTool.getCurrentDateTime().block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertNotNull(result.getZonedDateTime());
    }

    @Test
    @DisplayName("Should convert to UTC using convenience method")
    void testConvertToUtc() {
      DateTimeResult result = dateTimeTool.convertToUtc(TEST_DATETIME).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertNotNull(result.getZonedDateTime());
    }

    @Test
    @DisplayName("Should format as ISO 8601 using convenience method")
    void testFormatAsIso8601() {
      DateTimeResult result = dateTimeTool.formatAsIso8601(TEST_DATETIME).block();

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertThat(result.getResult()).contains("2025-10-31");
    }
  }

  @Nested
  @DisplayName("Configuration Tests")
  class ConfigurationTests {

    @Test
    @DisplayName("Should use default configuration")
    void testDefaultConfig() {
      DateTimeConfig config = DateTimeConfig.defaults();

      assertNotNull(config);
      assertNotNull(config.getDefaultTimezone());
      assertNotNull(config.getDefaultDateTimeFormat());
    }

    @Test
    @DisplayName("Should create with no-arg constructor")
    void testNoArgConstructor() {
      DateTimeTool tool = new DateTimeTool();
      assertNotNull(tool);

      DateTimeResult result = tool.getCurrentDateTime().block();
      assertNotNull(result);
      assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Should create with custom config")
    void testCustomConfigConstructor() {
      DateTimeConfig config =
          DateTimeConfig.builder()
              .defaultTimezone(ZoneId.of("America/New_York"))
              .defaultDateTimeFormat("yyyy-MM-dd")
              .build();

      DateTimeTool tool = new DateTimeTool(config);
      assertNotNull(tool);

      DateTimeResult result = tool.getCurrentDateTime().block();
      assertNotNull(result);
      assertTrue(result.isSuccess());
    }
  }

  @Nested
  @DisplayName("Edge Cases and Error Handling")
  class EdgeCasesTests {

    @Test
    @DisplayName("Should handle invalid datetime string")
    void testInvalidDateTimeString() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.PARSE)
              .dateTime("invalid-datetime")
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getErrorMessage()).isNotEmpty();
    }

    @Test
    @DisplayName("Should handle null datetime")
    void testNullDateTime() {
      DateTimeRequest request =
          DateTimeRequest.builder().operation(DateTimeOperation.PARSE).dateTime(null).build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should handle exception in operation")
    void testExceptionHandling() {
      DateTimeRequest request =
          DateTimeRequest.builder()
              .operation(DateTimeOperation.FORMAT)
              .dateTime("malformed")
              .outputFormat("invalid{format")
              .build();

      DateTimeResult result = dateTimeTool.execute(request).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getErrorMessage()).isNotEmpty();
    }
  }
}
