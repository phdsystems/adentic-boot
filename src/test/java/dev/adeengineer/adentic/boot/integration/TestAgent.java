package dev.adeengineer.adentic.boot.integration;

import dev.adeengineer.agent.Agent;
import dev.adeengineer.agent.AgentInfo;
import dev.adeengineer.agent.TaskRequest;
import dev.adeengineer.agent.TaskResult;
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
