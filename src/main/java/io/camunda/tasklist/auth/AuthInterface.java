package io.camunda.tasklist.auth;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.exception.TaskListException;

public interface AuthInterface {
  void authenticate(CamundaTaskListClient client) throws TaskListException;
}
