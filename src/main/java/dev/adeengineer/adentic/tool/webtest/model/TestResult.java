package dev.adeengineer.adentic.tool.webtest.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** Result of a web test operation or assertion. */
@Data
@Builder
public class TestResult {
  /** Test operation type. */
  private OperationType operation;

  /** Whether the test passed. */
  @Builder.Default private boolean passed = true;

  /** Error message if test failed. */
  private String error;

  /** Expected value (for assertions). */
  private String expected;

  /** Actual value (for assertions). */
  private String actual;

  /** Element selector (if applicable). */
  private String selector;

  /** Page URL when test was executed. */
  private String url;

  /** Execution time. */
  private Duration executionTime;

  /** Timestamp when test was executed. */
  @Builder.Default private Instant timestamp = Instant.now();

  /** Additional details. */
  private String details;

  /** List of sub-results (for composite tests). */
  @Builder.Default private List<TestResult> subResults = new ArrayList<>();

  /** Test operation types. */
  public enum OperationType {
    NAVIGATE,
    CLICK,
    FILL_INPUT,
    SELECT_OPTION,
    SUBMIT,
    ASSERT_EXISTS,
    ASSERT_TEXT,
    ASSERT_VISIBLE,
    ASSERT_ENABLED,
    ASSERT_URL,
    SCREENSHOT,
    WAIT,
    EXECUTE_SCRIPT
  }

  @Override
  public String toString() {
    if (!passed) {
      return String.format("❌ %s failed: %s", operation, error);
    }

    String message =
        switch (operation) {
          case NAVIGATE -> String.format("✅ Navigated to: %s", url);
          case CLICK -> String.format("✅ Clicked: %s", selector);
          case FILL_INPUT -> String.format("✅ Filled input: %s", selector);
          case SELECT_OPTION -> String.format("✅ Selected option: %s", selector);
          case SUBMIT -> String.format("✅ Submitted form: %s", selector);
          case ASSERT_EXISTS -> String.format("✅ Element exists: %s", selector);
          case ASSERT_TEXT -> String.format("✅ Text matches: %s", selector);
          case ASSERT_VISIBLE -> String.format("✅ Element visible: %s", selector);
          case ASSERT_ENABLED -> String.format("✅ Element enabled: %s", selector);
          case ASSERT_URL -> String.format("✅ URL matches: %s", url);
          case SCREENSHOT -> "✅ Screenshot captured";
          case WAIT -> String.format("✅ Wait completed: %s", selector);
          case EXECUTE_SCRIPT -> "✅ Script executed";
        };

    if (executionTime != null) {
      message += String.format(" [%dms]", executionTime.toMillis());
    }

    return message;
  }
}
