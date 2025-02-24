package io.camunda.tasklist.exception;

public class TaskListException extends RuntimeException {

  public TaskListException() {
    super();
  }

  public TaskListException(Exception e) {
    super(e);
  }

  public TaskListException(String message) {
    super(message);
  }

  public TaskListException(String message, Exception e) {
    super(message, e);
  }
}
