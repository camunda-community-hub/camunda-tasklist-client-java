package io.camunda.tasklist;

import static io.camunda.tasklist.util.ConverterUtils.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.camunda.tasklist.dto.Form;
import io.camunda.tasklist.dto.Pagination;
import io.camunda.tasklist.dto.SearchType;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.Task.Implementation;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.dto.Variable;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.tasklist.generated.api.FormApi;
import io.camunda.tasklist.generated.api.TaskApi;
import io.camunda.tasklist.generated.api.VariablesApi;
import io.camunda.tasklist.generated.invoker.ApiClient;
import io.camunda.tasklist.generated.invoker.ApiException;
import io.camunda.tasklist.generated.model.SaveVariablesRequest;
import io.camunda.tasklist.generated.model.TaskAssignRequest;
import io.camunda.tasklist.generated.model.TaskCompleteRequest;
import io.camunda.tasklist.generated.model.TaskSearchRequest;
import io.camunda.tasklist.generated.model.VariableInputDTO;
import io.camunda.tasklist.generated.model.VariablesSearchRequest;
import io.camunda.zeebe.client.ZeebeClient;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

public class CamundaTaskListClientV1 extends AbstractCamundaTaskListClient {
  private final ZeebeClient zeebeClient;
  private final TaskApi taskApi;
  private final FormApi formApi;
  private final VariablesApi variablesApi;

  public CamundaTaskListClientV1(
      CamundaTaskListClientProperties properties, ZeebeClient zeebeClient) {
    super(
        new CamundaTaskListClientDefaultBehaviourProperties(
            properties.isDefaultShouldReturnVariables(),
            properties.isDefaultShouldLoadTruncatedVariables()));
    assert properties.getTaskListUrl() != null : "taskListUrl must not be null";
    assert properties.getAuthentication() != null : "authentication must not be null";
    assert !properties.isUseZeebeUserTasks() || zeebeClient != null
        : "zeebeClient must not be null";
    CloseableHttpClient httpClient =
        HttpClients.custom()
            .useSystemProperties()
            .addRequestInterceptorFirst(
                (request, entity, context) ->
                    properties.getAuthentication().getTokenHeader().forEach(request::addHeader))
            .build();
    ApiClient apiClient = new ApiClient(httpClient);
    apiClient.setBasePath(properties.getTaskListUrl());
    this.taskApi = new TaskApi(apiClient);
    this.formApi = new FormApi(apiClient);
    this.variablesApi = new VariablesApi(apiClient);
    this.zeebeClient = zeebeClient;
  }

  public static CamundaTaskListClientBuilder builder() {
    return new CamundaTaskListClientBuilder();
  }

  @Override
  public Task unclaim(String taskId) throws TaskListException {
    try {
      return toTask(taskApi.unassignTask(taskId), null);
    } catch (Exception e) {
      throw new TaskListException("Error unclaiming task " + taskId, e);
    }
  }

  @Override
  public Task claim(String taskId, String assignee, Boolean allowOverrideAssignment)
      throws TaskListException {
    try {
      return toTask(
          taskApi.assignTask(
              taskId,
              new TaskAssignRequest()
                  .assignee(assignee)
                  .allowOverrideAssignment(allowOverrideAssignment)),
          null);
    } catch (Exception e) {
      throw new TaskListException("Error assigning task " + taskId, e);
    }
  }

  @Override
  public void completeTask(String taskId, Map<String, Object> variablesMap)
      throws TaskListException {
    try {
      Task task = getTask(taskId);
      if (task.getImplementation() == null
          || task.getImplementation().equals(Implementation.JOB_WORKER)) {
        List<VariableInputDTO> variables = toVariableInput(variablesMap);
        taskApi.completeTask(taskId, new TaskCompleteRequest().variables(variables));
      } else if (task.getImplementation().equals(Implementation.ZEEBE_USER_TASK)) {
        if (zeebeClient == null) {
          throw new IllegalStateException(
              "zeebeClient must not be null, please set useZeebeUserTasks to assert this on startup");
        }
        zeebeClient
            .newUserTaskCompleteCommand(Long.parseLong(taskId))
            .variables(variablesMap)
            .send()
            .join();
      }
    } catch (TaskListException | ApiException e) {
      throw new TaskListException("Error assigning task " + taskId, e);
    }
  }

  @Override
  public Task getTask(String taskId, boolean withVariables) throws TaskListException {
    try {
      List<Variable> variables = null;
      if (withVariables) {
        variables = getVariables(taskId);
      }

      return toTask(taskApi.getTaskById(taskId), variables);
    } catch (TaskListException | ApiException e) {
      throw new TaskListException("Error reading task " + taskId, e);
    }
  }

  @Override
  public List<Variable> getVariables(String taskId, boolean loadTruncated)
      throws TaskListException {
    try {
      return taskApi.searchTaskVariables(taskId, new VariablesSearchRequest()).stream()
          .map(
              vsr -> {
                if (loadTruncated && Boolean.TRUE.equals(vsr.getIsValueTruncated())) {
                  try {
                    return getVariable(vsr.getId());
                  } catch (TaskListException e) {
                    throw new RuntimeException("Error while loading full value of variable", e);
                  }
                } else {
                  try {
                    return improveVariable(vsr);
                  } catch (Exception e) {
                    throw new RuntimeException("Error while improving variable", e);
                  }
                }
              })
          .collect(Collectors.toList());
    } catch (ApiException | RuntimeException e) {
      throw new TaskListException("Error reading task " + taskId, e);
    }
  }

  @Override
  public Variable getVariable(String variableId) throws TaskListException {
    try {
      return toVariable(variablesApi.getVariableById(variableId));
    } catch (ApiException | JsonProcessingException e) {
      throw new TaskListException("Error while loading variable " + variableId, e);
    }
  }

  @Override
  public Form getForm(String formId, String processDefinitionId, Long version)
      throws TaskListException {
    try {
      if (formId.startsWith(CamundaTaskListClientProperties.CAMUNDA_FORMS_PREFIX)) {
        formId = formId.substring(CamundaTaskListClientProperties.CAMUNDA_FORMS_PREFIX.length());
      }
      return toForm(formApi.getForm(formId, processDefinitionId, version));
    } catch (ApiException e) {
      throw new TaskListException("Error reading form " + formId, e);
    }
  }

  @Override
  protected TaskList getTasksInternal(TaskSearch search) throws TaskListException {
    Pagination pagination = search.getPagination();
    TaskSearchRequest request = toTaskSearchRequest(search);
    if (pagination != null) {
      if (pagination.getSearchType() != null
          && pagination.getSearch() != null
          && !pagination.getSearch().isEmpty()) {
        if (pagination.getSearchType().equals(SearchType.BEFORE)) {
          request.searchBefore(pagination.getSearch());
        } else if (pagination.getSearchType().equals(SearchType.BEFORE_OR_EQUAL)) {
          request.searchBeforeOrEqual(pagination.getSearch());
        } else if (pagination.getSearchType().equals(SearchType.AFTER)) {
          request.searchAfter(pagination.getSearch());
        } else if (pagination.getSearchType().equals(SearchType.AFTER_OR_EQUAL)) {
          request.searchAfterOrEqual(pagination.getSearch());
        }
      }
      request.pageSize(pagination.getPageSize());
      request.sort(toSort(pagination.getSort()));
    }
    return new TaskList().setItems(getTasks(request, search.getWithVariables())).setSearch(search);
  }

  private List<Task> getTasks(TaskSearchRequest search, boolean withVariables)
      throws TaskListException {
    try {

      List<Task> tasks = toTasks(taskApi.searchTasks(search));
      if (withVariables
          && (search.getIncludeVariables() == null || search.getIncludeVariables().isEmpty())) {
        loadVariables(tasks);
      }
      return tasks;
    } catch (ApiException e) {
      throw new TaskListException("Error searching tasks", e);
    }
  }

  @Override
  public void saveDraftVariables(String taskId, Map<String, Object> variables)
      throws TaskListException {
    try {
      List<VariableInputDTO> convertedVariables = toVariableInput(variables);
      SaveVariablesRequest variablesInput = new SaveVariablesRequest();
      variablesInput.setVariables(convertedVariables);
      taskApi.saveDraftTaskVariables(taskId, variablesInput);
    } catch (ApiException e) {
      throw new TaskListException("Error saving draft variables for task " + taskId, e);
    }
  }
}