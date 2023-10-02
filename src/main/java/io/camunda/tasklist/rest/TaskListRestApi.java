package io.camunda.tasklist.rest;

import io.camunda.tasklist.rest.exception.TaskListException;
import io.camunda.tasklist.rest.exception.TaskListRestException;
import io.camunda.tasklist.rest.dto.*;

import java.util.List;
import java.util.Map;

public interface TaskListRestApi {

  List<TaskSearchResponse> searchTasks(TaskSearchRequest request) throws TaskListException, TaskListRestException;

  TaskResponse getTask(String taskId) throws TaskListException, TaskListRestException;

  FormResponse getForm(String processDefinitionKey, String formId) throws TaskListException, TaskListRestException;

  TaskResponse assignTask(String taskId, TaskAssignRequest request) throws TaskListException, TaskListRestException;

  TaskResponse unassignTask(String taskId) throws TaskListException, TaskListRestException;

  TaskResponse completeTask(String taskId, Map<String, Object> variablesMap) throws TaskListException, TaskListRestException;

}