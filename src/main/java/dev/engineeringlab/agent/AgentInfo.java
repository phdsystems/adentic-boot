package dev.engineeringlab.agent;

/** Stub class for AgentInfo - to be replaced with real implementation when available. */
public class AgentInfo {
  private final String name;
  private final String description;
  private final String version;

  public AgentInfo(String name, String description, String version) {
    this.name = name;
    this.description = description;
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getVersion() {
    return version;
  }

  public static AgentInfoBuilder builder() {
    return new AgentInfoBuilder();
  }

  public static class AgentInfoBuilder {
    private String name;
    private String description;
    private String version = "1.0.0";

    public AgentInfoBuilder name(String name) {
      this.name = name;
      return this;
    }

    public AgentInfoBuilder description(String description) {
      this.description = description;
      return this;
    }

    public AgentInfoBuilder version(String version) {
      this.version = version;
      return this;
    }

    public AgentInfo build() {
      return new AgentInfo(name, description, version);
    }
  }
}
