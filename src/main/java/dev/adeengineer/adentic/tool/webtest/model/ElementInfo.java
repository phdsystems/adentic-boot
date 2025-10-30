package dev.adeengineer.adentic.tool.webtest.model;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** Information about a web page element. */
@Data
@Builder
public class ElementInfo {
  /** Element selector. */
  private String selector;

  /** Element tag name. */
  private String tagName;

  /** Element text content. */
  private String text;

  /** Element inner HTML. */
  private String innerHTML;

  /** Element attributes. */
  private Map<String, String> attributes;

  /** Whether element is visible. */
  private boolean visible;

  /** Whether element is enabled. */
  private boolean enabled;

  /** Whether element is selected/checked. */
  private boolean selected;

  /** Element position X. */
  private int x;

  /** Element position Y. */
  private int y;

  /** Element width. */
  private int width;

  /** Element height. */
  private int height;

  /** CSS classes. */
  private String className;

  /** Element ID. */
  private String id;

  @Override
  public String toString() {
    return String.format(
        "<%s> %s: '%s' (visible: %s, enabled: %s)", tagName, selector, text, visible, enabled);
  }
}
