package dev.adeengineer.adentic.tool.datetime;

import dev.adeengineer.adentic.tool.datetime.config.DateTimeConfig;
import dev.adeengineer.adentic.tool.datetime.model.DateTimeOperation;
import dev.adeengineer.adentic.tool.datetime.model.DateTimeRequest;
import dev.adeengineer.adentic.tool.datetime.model.DateTimeResult;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * DateTime Tool for date/time operations, timezone conversions, and calendar utilities.
 *
 * <p>Supports:
 *
 * <ul>
 *   <li><b>Parsing/Formatting</b>: parse, format, ISO 8601
 *   <li><b>Timezone</b>: conversions, offset queries
 *   <li><b>Arithmetic</b>: add/subtract days, months, years, hours, minutes
 *   <li><b>Duration</b>: calculate differences between datetimes
 *   <li><b>Calendar</b>: start/end of day, week, month, year
 *   <li><b>Information</b>: day of week, leap year, weekend check
 * </ul>
 *
 * @since 0.3.0
 */
@Slf4j
public class DateTimeTool {

  private final DateTimeConfig config;

  public DateTimeTool(DateTimeConfig config) {
    this.config = config;
  }

  public DateTimeTool() {
    this(DateTimeConfig.defaults());
  }

  /**
   * Performs a datetime operation.
   *
   * @param request datetime request
   * @return Mono emitting datetime result
   */
  public Mono<DateTimeResult> execute(DateTimeRequest request) {
    return Mono.fromCallable(
        () -> {
          try {
            log.debug("Performing datetime operation: {}", request.getOperation());

            return switch (request.getOperation()) {
              case PARSE -> parse(request);
              case FORMAT -> format(request);
              case TO_ISO8601 -> toIso8601(request);
              case FROM_ISO8601 -> fromIso8601(request);
              case CONVERT_TIMEZONE -> convertTimezone(request);
              case GET_TIMEZONE_OFFSET -> getTimezoneOffset(request);
              case LIST_TIMEZONES -> listTimezones();
              case ADD_DAYS -> addDays(request);
              case ADD_MONTHS -> addMonths(request);
              case ADD_YEARS -> addYears(request);
              case ADD_HOURS -> addHours(request);
              case ADD_MINUTES -> addMinutes(request);
              case ADD_SECONDS -> addSeconds(request);
              case SUBTRACT_DAYS -> subtractDays(request);
              case SUBTRACT_MONTHS -> subtractMonths(request);
              case SUBTRACT_YEARS -> subtractYears(request);
              case DURATION_BETWEEN -> durationBetween(request);
              case DAYS_BETWEEN -> daysBetween(request);
              case HOURS_BETWEEN -> hoursBetween(request);
              case MINUTES_BETWEEN -> minutesBetween(request);
              case SECONDS_BETWEEN -> secondsBetween(request);
              case START_OF_DAY -> startOfDay(request);
              case END_OF_DAY -> endOfDay(request);
              case START_OF_WEEK -> startOfWeek(request);
              case END_OF_WEEK -> endOfWeek(request);
              case START_OF_MONTH -> startOfMonth(request);
              case END_OF_MONTH -> endOfMonth(request);
              case START_OF_YEAR -> startOfYear(request);
              case END_OF_YEAR -> endOfYear(request);
              case DAY_OF_WEEK -> dayOfWeek(request);
              case DAY_OF_MONTH -> dayOfMonth(request);
              case DAY_OF_YEAR -> dayOfYear(request);
              case WEEK_OF_YEAR -> weekOfYear(request);
              case IS_LEAP_YEAR -> isLeapYear(request);
              case IS_WEEKEND -> isWeekend(request);
              case NOW -> now();
              case TODAY -> today();
              case TIMESTAMP -> timestamp();
              case EPOCH_MILLIS -> epochMillis(request);
              case FROM_EPOCH_MILLIS -> fromEpochMillis(request);
            };

          } catch (Exception e) {
            log.error("DateTime operation failed: {}", e.getMessage());
            return DateTimeResult.error(request.getOperation(), e.getMessage());
          }
        });
  }

  // ==================== Parsing and Formatting ====================

  private DateTimeResult parse(DateTimeRequest request) {
    DateTimeFormatter formatter =
        request.getInputFormat() != null
            ? DateTimeFormatter.ofPattern(request.getInputFormat())
            : DateTimeFormatter.ISO_DATE_TIME;

    ZonedDateTime dateTime = ZonedDateTime.parse(request.getDateTime(), formatter);
    return DateTimeResult.success(DateTimeOperation.PARSE, dateTime);
  }

  private DateTimeResult format(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    DateTimeFormatter formatter =
        request.getOutputFormat() != null
            ? DateTimeFormatter.ofPattern(request.getOutputFormat())
            : DateTimeFormatter.ofPattern(config.getDefaultDateTimeFormat());

    String formatted = dateTime.format(formatter);
    return DateTimeResult.success(DateTimeOperation.FORMAT, formatted);
  }

  private DateTimeResult toIso8601(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    String iso8601 = dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    return DateTimeResult.success(DateTimeOperation.TO_ISO8601, iso8601);
  }

  private DateTimeResult fromIso8601(DateTimeRequest request) {
    ZonedDateTime dateTime = ZonedDateTime.parse(request.getDateTime());
    return DateTimeResult.success(DateTimeOperation.FROM_ISO8601, dateTime);
  }

  // ==================== Timezone Operations ====================

  private DateTimeResult convertTimezone(DateTimeRequest request) {
    ZonedDateTime dateTime =
        parseDateTime(request.getDateTime()).withZoneSameInstant(request.getSourceTimezone());
    ZonedDateTime converted = dateTime.withZoneSameInstant(request.getTargetTimezone());
    return DateTimeResult.success(DateTimeOperation.CONVERT_TIMEZONE, converted);
  }

  private DateTimeResult getTimezoneOffset(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZoneOffset offset = dateTime.getOffset();
    String result = "UTC" + offset.getId();
    return DateTimeResult.success(DateTimeOperation.GET_TIMEZONE_OFFSET, result);
  }

  private DateTimeResult listTimezones() {
    List<String> timezones =
        ZoneId.getAvailableZoneIds().stream().sorted().limit(50).collect(Collectors.toList());
    String result = String.join(", ", timezones) + " (showing first 50)";
    return DateTimeResult.success(DateTimeOperation.LIST_TIMEZONES, result);
  }

  // ==================== Date Arithmetic ====================

  private DateTimeResult addDays(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZonedDateTime result = dateTime.plusDays(request.getAmount());
    return DateTimeResult.success(DateTimeOperation.ADD_DAYS, result);
  }

  private DateTimeResult addMonths(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZonedDateTime result = dateTime.plusMonths(request.getAmount());
    return DateTimeResult.success(DateTimeOperation.ADD_MONTHS, result);
  }

  private DateTimeResult addYears(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZonedDateTime result = dateTime.plusYears(request.getAmount());
    return DateTimeResult.success(DateTimeOperation.ADD_YEARS, result);
  }

  private DateTimeResult addHours(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZonedDateTime result = dateTime.plusHours(request.getAmount());
    return DateTimeResult.success(DateTimeOperation.ADD_HOURS, result);
  }

  private DateTimeResult addMinutes(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZonedDateTime result = dateTime.plusMinutes(request.getAmount());
    return DateTimeResult.success(DateTimeOperation.ADD_MINUTES, result);
  }

  private DateTimeResult addSeconds(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZonedDateTime result = dateTime.plusSeconds(request.getAmount());
    return DateTimeResult.success(DateTimeOperation.ADD_SECONDS, result);
  }

  private DateTimeResult subtractDays(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZonedDateTime result = dateTime.minusDays(request.getAmount());
    return DateTimeResult.success(DateTimeOperation.SUBTRACT_DAYS, result);
  }

  private DateTimeResult subtractMonths(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZonedDateTime result = dateTime.minusMonths(request.getAmount());
    return DateTimeResult.success(DateTimeOperation.SUBTRACT_MONTHS, result);
  }

  private DateTimeResult subtractYears(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZonedDateTime result = dateTime.minusYears(request.getAmount());
    return DateTimeResult.success(DateTimeOperation.SUBTRACT_YEARS, result);
  }

  // ==================== Duration Calculations ====================

  private DateTimeResult durationBetween(DateTimeRequest request) {
    ZonedDateTime dateTime1 = parseDateTime(request.getDateTime());
    ZonedDateTime dateTime2 = parseDateTime(request.getSecondDateTime());
    Duration duration = Duration.between(dateTime1, dateTime2);
    String result =
        String.format(
            "%d days, %d hours, %d minutes, %d seconds",
            duration.toDays(),
            duration.toHoursPart(),
            duration.toMinutesPart(),
            duration.toSecondsPart());
    return DateTimeResult.success(DateTimeOperation.DURATION_BETWEEN, result);
  }

  private DateTimeResult daysBetween(DateTimeRequest request) {
    ZonedDateTime dateTime1 = parseDateTime(request.getDateTime());
    ZonedDateTime dateTime2 = parseDateTime(request.getSecondDateTime());
    long days = ChronoUnit.DAYS.between(dateTime1, dateTime2);
    return DateTimeResult.success(DateTimeOperation.DAYS_BETWEEN, String.valueOf(days));
  }

  private DateTimeResult hoursBetween(DateTimeRequest request) {
    ZonedDateTime dateTime1 = parseDateTime(request.getDateTime());
    ZonedDateTime dateTime2 = parseDateTime(request.getSecondDateTime());
    long hours = ChronoUnit.HOURS.between(dateTime1, dateTime2);
    return DateTimeResult.success(DateTimeOperation.HOURS_BETWEEN, String.valueOf(hours));
  }

  private DateTimeResult minutesBetween(DateTimeRequest request) {
    ZonedDateTime dateTime1 = parseDateTime(request.getDateTime());
    ZonedDateTime dateTime2 = parseDateTime(request.getSecondDateTime());
    long minutes = ChronoUnit.MINUTES.between(dateTime1, dateTime2);
    return DateTimeResult.success(DateTimeOperation.MINUTES_BETWEEN, String.valueOf(minutes));
  }

  private DateTimeResult secondsBetween(DateTimeRequest request) {
    ZonedDateTime dateTime1 = parseDateTime(request.getDateTime());
    ZonedDateTime dateTime2 = parseDateTime(request.getSecondDateTime());
    long seconds = ChronoUnit.SECONDS.between(dateTime1, dateTime2);
    return DateTimeResult.success(DateTimeOperation.SECONDS_BETWEEN, String.valueOf(seconds));
  }

  // ==================== Calendar Operations ====================

  private DateTimeResult startOfDay(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZonedDateTime result = dateTime.truncatedTo(ChronoUnit.DAYS);
    return DateTimeResult.success(DateTimeOperation.START_OF_DAY, result);
  }

  private DateTimeResult endOfDay(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZonedDateTime result = dateTime.truncatedTo(ChronoUnit.DAYS).plusDays(1).minusNanos(1);
    return DateTimeResult.success(DateTimeOperation.END_OF_DAY, result);
  }

  private DateTimeResult startOfWeek(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZonedDateTime result = dateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    return DateTimeResult.success(DateTimeOperation.START_OF_WEEK, result);
  }

  private DateTimeResult endOfWeek(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZonedDateTime result = dateTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    return DateTimeResult.success(DateTimeOperation.END_OF_WEEK, result);
  }

  private DateTimeResult startOfMonth(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZonedDateTime result = dateTime.with(TemporalAdjusters.firstDayOfMonth());
    return DateTimeResult.success(DateTimeOperation.START_OF_MONTH, result);
  }

  private DateTimeResult endOfMonth(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZonedDateTime result = dateTime.with(TemporalAdjusters.lastDayOfMonth());
    return DateTimeResult.success(DateTimeOperation.END_OF_MONTH, result);
  }

  private DateTimeResult startOfYear(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZonedDateTime result = dateTime.with(TemporalAdjusters.firstDayOfYear());
    return DateTimeResult.success(DateTimeOperation.START_OF_YEAR, result);
  }

  private DateTimeResult endOfYear(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    ZonedDateTime result = dateTime.with(TemporalAdjusters.lastDayOfYear());
    return DateTimeResult.success(DateTimeOperation.END_OF_YEAR, result);
  }

  // ==================== Information ====================

  private DateTimeResult dayOfWeek(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    String result = dateTime.getDayOfWeek().toString();
    return DateTimeResult.success(DateTimeOperation.DAY_OF_WEEK, result);
  }

  private DateTimeResult dayOfMonth(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    String result = String.valueOf(dateTime.getDayOfMonth());
    return DateTimeResult.success(DateTimeOperation.DAY_OF_MONTH, result);
  }

  private DateTimeResult dayOfYear(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    String result = String.valueOf(dateTime.getDayOfYear());
    return DateTimeResult.success(DateTimeOperation.DAY_OF_YEAR, result);
  }

  private DateTimeResult weekOfYear(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    WeekFields weekFields = WeekFields.of(Locale.getDefault());
    int week = dateTime.get(weekFields.weekOfWeekBasedYear());
    return DateTimeResult.success(DateTimeOperation.WEEK_OF_YEAR, String.valueOf(week));
  }

  private DateTimeResult isLeapYear(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    boolean isLeap = Year.isLeap(dateTime.getYear());
    return DateTimeResult.success(DateTimeOperation.IS_LEAP_YEAR, String.valueOf(isLeap));
  }

  private DateTimeResult isWeekend(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
    boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    return DateTimeResult.success(DateTimeOperation.IS_WEEKEND, String.valueOf(isWeekend));
  }

  // ==================== Utilities ====================

  private DateTimeResult now() {
    ZonedDateTime now = ZonedDateTime.now(config.getDefaultTimezone());
    return DateTimeResult.success(DateTimeOperation.NOW, now);
  }

  private DateTimeResult today() {
    ZonedDateTime today =
        ZonedDateTime.now(config.getDefaultTimezone()).truncatedTo(ChronoUnit.DAYS);
    return DateTimeResult.success(DateTimeOperation.TODAY, today);
  }

  private DateTimeResult timestamp() {
    Instant now = Instant.now();
    return DateTimeResult.success(DateTimeOperation.TIMESTAMP, now.toString());
  }

  private DateTimeResult epochMillis(DateTimeRequest request) {
    ZonedDateTime dateTime = parseDateTime(request.getDateTime());
    long millis = dateTime.toInstant().toEpochMilli();
    return DateTimeResult.builder()
        .operation(DateTimeOperation.EPOCH_MILLIS)
        .success(true)
        .epochMillis(millis)
        .result(String.valueOf(millis))
        .build();
  }

  private DateTimeResult fromEpochMillis(DateTimeRequest request) {
    Instant instant = Instant.ofEpochMilli(request.getEpochMillis());
    ZonedDateTime dateTime = instant.atZone(config.getDefaultTimezone());
    return DateTimeResult.success(DateTimeOperation.FROM_EPOCH_MILLIS, dateTime);
  }

  // ==================== Helpers ====================

  private ZonedDateTime parseDateTime(String dateTimeString) {
    try {
      return ZonedDateTime.parse(dateTimeString);
    } catch (Exception e) {
      // Try parsing as LocalDateTime and add default timezone
      try {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString);
        return localDateTime.atZone(config.getDefaultTimezone());
      } catch (Exception e2) {
        // Try parsing with custom format
        DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern(config.getDefaultDateTimeFormat());
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);
        return localDateTime.atZone(config.getDefaultTimezone());
      }
    }
  }

  /**
   * Convenience methods for common operations.
   *
   * @return Mono emitting result
   */
  public Mono<DateTimeResult> getCurrentDateTime() {
    return execute(DateTimeRequest.builder().operation(DateTimeOperation.NOW).build());
  }

  public Mono<DateTimeResult> convertToUtc(String dateTime) {
    return execute(
        DateTimeRequest.convertTimezone(dateTime, config.getDefaultTimezone(), ZoneId.of("UTC")));
  }

  public Mono<DateTimeResult> formatAsIso8601(String dateTime) {
    return execute(
        DateTimeRequest.builder()
            .operation(DateTimeOperation.TO_ISO8601)
            .dateTime(dateTime)
            .build());
  }
}
