package dev.engineeringlab.agent;

/** Stub interface for Agent - to be replaced with real implementation when available. */
public interface Agent {
  String getName();

  String getDescription();

  Object execute(Object input);
}
