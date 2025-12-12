package dev.engineeringlab.ee.llm.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Simple implementation of ToolRegistry - stub for standalone mode. */
public class SimpleToolRegistry implements ToolRegistry {
  private final Map<String, Object> tools = new ConcurrentHashMap<>();

  @Override
  public void register(Object tool) {
    if (tool != null) {
      tools.put(tool.getClass().getSimpleName(), tool);
    }
  }

  @Override
  public Optional<Object> getTool(String name) {
    return Optional.ofNullable(tools.get(name));
  }

  @Override
  public List<Object> getAllTools() {
    return new ArrayList<>(tools.values());
  }
}
