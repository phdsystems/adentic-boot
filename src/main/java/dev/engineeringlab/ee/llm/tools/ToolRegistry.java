package dev.engineeringlab.ee.llm.tools;

import java.util.List;
import java.util.Optional;

/** Stub interface for ToolRegistry - to be replaced with real implementation when available. */
public interface ToolRegistry {
  void register(Object tool);

  Optional<Object> getTool(String name);

  List<Object> getAllTools();
}
