package dev.engineeringlab.adentic.boot.integration;

import dev.engineeringlab.agent.Agent;
import dev.engineeringlab.agent.AgentInfo;
import dev.engineeringlab.agent.TaskRequest;
import dev.engineeringlab.agent.TaskResult;
import java.util.List;

/**
 * Simple test agent for integration testing.
 *
 * <p>Returns a fixed response for all requests.
 */
public class TestAgent implements Agent {

  private final String name;
  private final String response;

  public TestAgent(final String name, final String response) {
    this.name = name;
    this.response = response;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public AgentInfo getAgentInfo() {
    return new AgentInfo(name, "Test agent: " + name, List.of("testing"), "test");
  }

  @Override
  public TaskResult executeTask(final TaskRequest request) {
    return TaskResult.success(name, response);
  }
}
