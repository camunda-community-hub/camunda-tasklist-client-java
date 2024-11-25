package io.camunda.tasklist;

import io.camunda.client.CamundaClient;
import io.camunda.tasklist.dto.Form;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.dto.Variable;
import io.camunda.tasklist.exception.TaskListException;
import java.util.List;
import java.util.Map;

public class CamundaTaskListClientV2 extends AbstractCamundaTaskListClient {
  private final CamundaClient camundaClient;

  public CamundaTaskListClientV2(
      CamundaClient camundaClient,
      CamundaTaskListClientDefaultBehaviourProperties defaultBehaviourProperties) {
    super(defaultBehaviourProperties);
    this.camundaClient = camundaClient;
  }

  @Override
  protected TaskList getTasksInternal(TaskSearch search) throws TaskListException {
    return null;
  }

  @Override
  public Task unclaim(String taskId) throws TaskListException {
    throw new TaskListException("Not implemented");
  }

  @Override
  public Task claim(String taskId, String assignee, Boolean allowOverrideAssignment)
      throws TaskListException {
    throw new TaskListException("Not implemented");
  }

  @Override
  public void completeTask(String taskId, Map<String, Object> variablesMap)
      throws TaskListException {
    throw new TaskListException("Not implemented");
  }

  @Override
  public Task getTask(String taskId, boolean withVariables) throws TaskListException {
    throw new TaskListException("Not implemented");
  }

  @Override
  public List<Variable> getVariables(String taskId, boolean loadTruncated)
      throws TaskListException {
    throw new TaskListException("Not implemented");
  }

  @Override
  public Variable getVariable(String variableId) throws TaskListException {
    throw new TaskListException("Not implemented");
  }

  @Override
  public Form getForm(String formId, String processDefinitionId, Long version)
      throws TaskListException {
    throw new TaskListException("Not implemented");
  }

  @Override
  public void saveDraftVariables(String taskId, Map<String, Object> variables)
      throws TaskListException {
    throw new TaskListException("Not implemented");
  }
}
