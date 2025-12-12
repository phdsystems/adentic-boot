package dev.engineeringlab.agent;

/** Stub class for TaskRequest - to be replaced with real implementation when available. */
public class TaskRequest {
  private final String taskId;
  private final String input;

  public TaskRequest(String taskId, String input) {
    this.taskId = taskId;
    this.input = input;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getInput() {
    return input;
  }

  public static TaskRequestBuilder builder() {
    return new TaskRequestBuilder();
  }

  public static class TaskRequestBuilder {
    private String taskId;
    private String input;

    public TaskRequestBuilder taskId(String taskId) {
      this.taskId = taskId;
      return this;
    }

    public TaskRequestBuilder input(String input) {
      this.input = input;
      return this;
    }

    public TaskRequest build() {
      return new TaskRequest(taskId, input);
    }
  }
}
