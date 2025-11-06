package dev.adeengineer.adentic.boot.integration.event;

import dev.adeengineer.agent.Agent;

/**
 * Event published when an agent is registered in the system.
 *
 * <p>Subscribers can listen for this event to perform actions when agents become available.
 */
public class AgentRegisteredEvent {

  private final Agent agent;
  private final long timestamp;

  public AgentRegisteredEvent(final Agent agent) {
    this.agent = agent;
    this.timestamp = System.currentTimeMillis();
  }

  public Agent getAgent() {
    return agent;
  }

  public String getAgentName() {
    return agent.getName();
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "AgentRegisteredEvent{"
        + "agentName='"
        + agent.getName()
        + '\''
        + ", timestamp="
        + timestamp
        + '}';
  }
}
