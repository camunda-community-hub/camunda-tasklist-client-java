package io.camunda.tasklist.rest.auth;

import io.camunda.tasklist.rest.TaskListRestClient;
import io.camunda.tasklist.rest.exception.TaskListException;

public interface Authentication {

  public Boolean authenticate(TaskListRestClient taskListClient) throws TaskListException;

}
