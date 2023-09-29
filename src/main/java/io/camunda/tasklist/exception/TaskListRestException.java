package io.camunda.tasklist.exception;

import io.camunda.tasklist.dto.ErrorResponse;

public class TaskListRestException extends Exception {

  private static final long serialVersionUID = -7593616210087047797L;

  public TaskListRestException(ErrorResponse errorResponse) {
    super(errorResponse.getMessage());
  }

  public TaskListRestException(ErrorResponse errorResponse, Exception e) {
    super(errorResponse.getMessage(), e);
  }

}