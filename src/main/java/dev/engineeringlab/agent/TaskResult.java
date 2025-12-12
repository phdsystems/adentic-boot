package dev.engineeringlab.agent;

/** Stub class for TaskResult - to be replaced with real implementation when available. */
public class TaskResult {
  private final String taskId;
  private final String output;
  private final boolean success;

  public TaskResult(String taskId, String output, boolean success) {
    this.taskId = taskId;
    this.output = output;
    this.success = success;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getOutput() {
    return output;
  }

  public boolean isSuccess() {
    return success;
  }

  public static TaskResultBuilder builder() {
    return new TaskResultBuilder();
  }

  public static class TaskResultBuilder {
    private String taskId;
    private String output;
    private boolean success = true;

    public TaskResultBuilder taskId(String taskId) {
      this.taskId = taskId;
      return this;
    }

    public TaskResultBuilder output(String output) {
      this.output = output;
      return this;
    }

    public TaskResultBuilder success(boolean success) {
      this.success = success;
      return this;
    }

    public TaskResult build() {
      return new TaskResult(taskId, output, success);
    }
  }
}
