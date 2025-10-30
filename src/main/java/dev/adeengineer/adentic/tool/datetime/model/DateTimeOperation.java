package dev.adeengineer.adentic.tool.datetime.model;

/** DateTime operation types. */
public enum DateTimeOperation {
  // Parsing and formatting
  PARSE,
  FORMAT,
  TO_ISO8601,
  FROM_ISO8601,

  // Timezone operations
  CONVERT_TIMEZONE,
  GET_TIMEZONE_OFFSET,
  LIST_TIMEZONES,

  // Date arithmetic
  ADD_DAYS,
  ADD_MONTHS,
  ADD_YEARS,
  ADD_HOURS,
  ADD_MINUTES,
  ADD_SECONDS,
  SUBTRACT_DAYS,
  SUBTRACT_MONTHS,
  SUBTRACT_YEARS,

  // Duration calculations
  DURATION_BETWEEN,
  DAYS_BETWEEN,
  HOURS_BETWEEN,
  MINUTES_BETWEEN,
  SECONDS_BETWEEN,

  // Calendar operations
  START_OF_DAY,
  END_OF_DAY,
  START_OF_WEEK,
  END_OF_WEEK,
  START_OF_MONTH,
  END_OF_MONTH,
  START_OF_YEAR,
  END_OF_YEAR,

  // Information
  DAY_OF_WEEK,
  DAY_OF_MONTH,
  DAY_OF_YEAR,
  WEEK_OF_YEAR,
  IS_LEAP_YEAR,
  IS_WEEKEND,

  // Utilities
  NOW,
  TODAY,
  TIMESTAMP,
  EPOCH_MILLIS,
  FROM_EPOCH_MILLIS
}
