package io.camunda.tasklist.auth;

import io.camunda.tasklist.TaskListRestClient;
import io.camunda.tasklist.exception.TaskListException;

public interface Authentication {

  public Boolean authenticate(TaskListRestClient taskListClient) throws TaskListException;

}
