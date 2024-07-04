package io.camunda.tasklist;

import static io.camunda.tasklist.util.ConverterUtils.improveVariable;
import static io.camunda.tasklist.util.ConverterUtils.toVariable;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.camunda.common.auth.Product;
import io.camunda.tasklist.dto.DateFilter;
import io.camunda.tasklist.dto.Form;
import io.camunda.tasklist.dto.Pagination;
import io.camunda.tasklist.dto.SearchType;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.Task.Implementation;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.dto.Variable;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.tasklist.generated.api.FormApi;
import io.camunda.tasklist.generated.api.TaskApi;
import io.camunda.tasklist.generated.api.VariablesApi;
import io.camunda.tasklist.generated.invoker.ApiClient;
import io.camunda.tasklist.generated.invoker.ApiException;
import io.camunda.tasklist.generated.invoker.Configuration;
import io.camunda.tasklist.generated.model.IncludeVariable;
import io.camunda.tasklist.generated.model.TaskAssignRequest;
import io.camunda.tasklist.generated.model.TaskByVariables;
import io.camunda.tasklist.generated.model.TaskCompleteRequest;
import io.camunda.tasklist.generated.model.TaskSearchRequest;
import io.camunda.tasklist.generated.model.VariableInputDTO;
import io.camunda.tasklist.generated.model.VariablesSearchRequest;
import io.camunda.tasklist.util.ConverterUtils;
import io.camunda.tasklist.util.JwtUtils;
import io.camunda.zeebe.client.ZeebeClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class CamundaTaskListClient {

  private final ApiClient apiClient = Configuration.getDefaultApiClient();
  private final TaskApi taskApi;
  private final FormApi formApi;
  private final VariablesApi variablesApi;
  private final ZeebeClient zeebeClient;
  private final CamundaTaskListClientProperties properties;
  private long tokenExpiration;

  protected CamundaTaskListClient(
      CamundaTaskListClientProperties properties, ZeebeClient zeebeClient)
      throws TaskListException {
    assert properties != null : "properties must not be null";
    assert properties.getTaskListUrl() != null : "taskListUrl must not be null";
    assert properties.getAuthentication() != null : "authentication must not be null";
    assert !properties.isUseZeebeUserTasks() || zeebeClient != null
        : "zeebeClient must not be null";
    this.properties = properties;
    this.apiClient.updateBaseUri(properties.getTaskListUrl());
    this.zeebeClient = zeebeClient;

    authenticate();
    this.taskApi = new TaskApi(this.apiClient);
    this.formApi = new FormApi(this.apiClient);
    this.variablesApi = new VariablesApi(this.apiClient);

  }

  public static CamundaTaskListClientBuilder builder() {
    return new CamundaTaskListClientBuilder();
  }

  public Task unclaim(String taskId) throws TaskListException {
    try {
      reconnectEventually();
      return ConverterUtils.toTask(taskApi.unassignTask(taskId), null);
    } catch (TaskListException | ApiException e) {
      throw new TaskListException("Error unclaiming task " + taskId, e);
    }
  }

  public Task claim(String taskId, String assignee) throws TaskListException {
    return claim(taskId, assignee, false);
  }

  public Task claim(String taskId, String assignee, Boolean allowOverrideAssignment)
      throws TaskListException {
    try {
      reconnectEventually();
      return ConverterUtils.toTask(
          taskApi.assignTask(
              taskId,
              new TaskAssignRequest()
                  .assignee(assignee)
                  .allowOverrideAssignment(allowOverrideAssignment)),
          null);
    } catch (TaskListException | ApiException e) {
      throw new TaskListException("Error assigning task " + taskId, e);
    }
  }

  public void completeTask(String taskId, Map<String, Object> variablesMap)
      throws TaskListException {
    try {
      Task task = getTask(taskId);
      if (task.getImplementation().equals(Implementation.JOB_WORKER)) {
        reconnectEventually();
        List<VariableInputDTO> variables = ConverterUtils.toVariableInput(variablesMap);
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

  public TaskList getTasks(Boolean assigned, TaskState state, Integer pageSize)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setAssigned(assigned)
            .setState(state)
            .setPagination(createPagination(pageSize)));
  }

  public TaskList getTasks(Boolean assigned, TaskState state, Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch().setAssigned(assigned).setState(state).setPagination(pagination));
  }

  public TaskList getTasks(
      Boolean assigned, TaskState state, boolean withVariables, Integer pageSize)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setAssigned(assigned)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(createPagination(pageSize)));
  }

  private Pagination createPagination(Integer pageSize) {
    return new Pagination().setPageSize(pageSize);
  }

  public TaskList getTasks(
      Boolean assigned, TaskState state, boolean withVariables, Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setAssigned(assigned)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(pagination));
  }

  public TaskList getAssigneeTasks(String assigneeId, TaskState state, Integer pageSize)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setAssignee(assigneeId)
            .setState(state)
            .setPagination(createPagination(pageSize)));
  }

  public TaskList getAssigneeTasks(String assigneeId, TaskState state, Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setAssignee(assigneeId)
            .setState(state)
            .setWithVariables(properties.isDefaultShouldReturnVariables())
            .setPagination(pagination));
  }

  public TaskList getAssigneeTasks(
      String assigneeId, TaskState state, boolean withVariables, Integer pageSize)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setAssigned(true)
            .setAssignee(assigneeId)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(createPagination(pageSize)));
  }

  public TaskList getAssigneeTasks(
      String assigneeId, TaskState state, boolean withVariables, Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setAssignee(assigneeId)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(pagination));
  }

  public TaskList getGroupTasks(String group, TaskState state, Integer pageSize)
      throws TaskListException {
    return getTasks(
        new TaskSearch().setGroup(group).setState(state).setPagination(createPagination(pageSize)));
  }

  public TaskList getGroupTasks(String group, TaskState state, Pagination pagination)
      throws TaskListException {
    return getTasks(new TaskSearch().setGroup(group).setState(state).setPagination(pagination));
  }

  public TaskList getGroupTasks(
      String group, TaskState state, boolean withVariables, Integer pageSize)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setGroup(group)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(createPagination(pageSize)));
  }

  public TaskList getGroupTasks(
      String group, TaskState state, boolean withVariables, Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setGroup(group)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(pagination));
  }

  public TaskList getGroupsTasks(
      List<String> groups, TaskState state, boolean withVariables, Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setGroups(groups)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(pagination));
  }

  public Task getTask(String taskId) throws TaskListException {
    return getTask(taskId, properties.isDefaultShouldReturnVariables());
  }

  public Task getTask(String taskId, boolean withVariables) throws TaskListException {

    try {
      reconnectEventually();
      List<Variable> variables = null;
      if (withVariables) {
        variables = getVariables(taskId);
      }

      return ConverterUtils.toTask(taskApi.getTaskById(taskId), variables);
    } catch (TaskListException | ApiException e) {
      throw new TaskListException("Error reading task " + taskId, e);
    }
  }

  public List<Variable> getVariables(String taskId) throws TaskListException {
    try {
      reconnectEventually();
      return taskApi.searchTaskVariables(taskId, new VariablesSearchRequest()).stream()
          .map(
              vsr -> {
                if (Boolean.TRUE.equals(vsr.getIsValueTruncated())) {
                  try {
                    return getVariable(vsr.getId());
                  } catch (TaskListException e) {
                    throw new RuntimeException("Error while loading full value of variable", e);
                  }
                } else {
                  try {
                    return improveVariable(vsr);
                  } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error while improving variable", e);
                  }
                }
              })
          .collect(Collectors.toList());
    } catch (ApiException | RuntimeException e) {
      throw new TaskListException("Error reading task " + taskId, e);
    }
  }

  public Variable getVariable(String variableId) throws TaskListException {
    try {
      reconnectEventually();
      return toVariable(variablesApi.getVariableById(variableId));
    } catch (ApiException | JsonProcessingException e) {
      throw new TaskListException("Error while loading variable " + variableId, e);
    }
  }

  public Form getForm(String formId, String processDefinitionId) throws TaskListException {
    return getForm(formId, processDefinitionId, null);
  }

  public Form getForm(String formId, String processDefinitionId, Long version)
      throws TaskListException {
    try {
      if (formId.startsWith(CamundaTaskListClientProperties.CAMUNDA_FORMS_PREFIX)) {
        formId = formId.substring(CamundaTaskListClientProperties.CAMUNDA_FORMS_PREFIX.length());
      }
      return ConverterUtils.toForm(formApi.getForm(formId, processDefinitionId, version));
    } catch (ApiException e) {
      throw new TaskListException("Error reading form " + formId, e);
    }
  }

  public TaskList before(TaskList taskList) throws TaskListException {
    return paginate(taskList, SearchType.BEFORE);
  }

  public TaskList beforeOrEquals(TaskList taskList) throws TaskListException {
    return paginate(taskList, SearchType.BEFORE_OR_EQUAL);
  }

  public TaskList after(TaskList taskList) throws TaskListException {
    return paginate(taskList, SearchType.AFTER);
  }

  public TaskList afterOrEqual(TaskList taskList) throws TaskListException {
    return paginate(taskList, SearchType.AFTER_OR_EQUAL);
  }

  private TaskList paginate(TaskList taskList, SearchType direction) throws TaskListException {
    if (taskList.getSearch().getPagination() == null
        || taskList.getSearch().getPagination().getPageSize() == null) {
      throw new TaskListException(
          "Before/After/AfterOrEquals search are only possible if a pageSize is set");
    }
    if (taskList.getItems() == null || taskList.getItems().isEmpty()) {
      throw new TaskListException(
          "Before/After/AfterOrEquals search are only possible if some items are present");
    }
    TaskSearch newSearch =
        new TaskSearch()
            .setAssigned(taskList.getSearch().getAssigned())
            .setAssignee(taskList.getSearch().getAssignee())
            .setGroup(taskList.getSearch().getGroup())
            .setState(taskList.getSearch().getState())
            .setWithVariables(taskList.getSearch().isWithVariables())
            .setPagination(getSearchPagination(taskList, direction));

    return getTasks(newSearch);
  }

  private Pagination getSearchPagination(TaskList taskList, SearchType type) {
    switch (type) {
      case BEFORE:
        return new Pagination.Builder()
            .pageSize(taskList.getSearch().getPagination().getPageSize())
            .before(taskList.first().getSortValues())
            .build();
      case BEFORE_OR_EQUAL:
        return new Pagination.Builder()
            .pageSize(taskList.getSearch().getPagination().getPageSize())
            .beforeOrEqual(taskList.first().getSortValues())
            .build();
      case AFTER:
        return new Pagination.Builder()
            .pageSize(taskList.getSearch().getPagination().getPageSize())
            .after(taskList.last().getSortValues())
            .build();
      default:
        return new Pagination.Builder()
            .pageSize(taskList.getSearch().getPagination().getPageSize())
            .afterOrEqual(taskList.last().getSortValues())
            .build();
    }
  }

  public TaskList getTasks(TaskSearch search) throws TaskListException {
    if (search.getWithVariables() == null) {
      search.setWithVariables(properties.isDefaultShouldReturnVariables());
    }
    Pagination pagination = search.getPagination();
    TaskSearchRequest request = ConverterUtils.toTaskSearchRequest(search);
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
      request.sort(pagination.getSort());
    }
    return new TaskList().setItems(getTasks(request, search.getWithVariables())).setSearch(search);
  }

  public TaskList getTasks(
      String group,
      Boolean assigned,
      String assigneeId,
      TaskState state,
      boolean withVariables,
      Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setGroup(group)
            .setAssigned(assigned)
            .setAssignee(assigneeId)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(pagination));
  }

  public TaskList getTasks(
      String candidateUser,
      List<String> candidateUsers,
      String group,
      List<String> groups,
      Boolean assigned,
      String assignee,
      TaskState state,
      DateFilter followUpDate,
      DateFilter dueDate,
      String processDefinitionId,
      String processInstanceId,
      String taskDefinitionId,
      List<TaskByVariables> taskVariables,
      List<String> tenantIds,
      List<IncludeVariable> includeVariables,
      boolean withVariables,
      Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setCandidateUser(candidateUser)
            .setCandidateUsers(candidateUsers)
            .setGroup(group)
            .setGroups(groups)
            .setAssigned(assigned)
            .setAssignee(assignee)
            .setState(state)
            .setFollowUpDate(followUpDate)
            .setDueDate(dueDate)
            .setProcessDefinitionKey(processDefinitionId)
            .setProcessInstanceKey(processInstanceId)
            .setTaskDefinitionId(taskDefinitionId)
            .setTaskVariables(taskVariables)
            .setTenantIds(tenantIds)
            .setIncludeVariables(
                includeVariables.stream()
                    .map(IncludeVariable::getName)
                    .collect(Collectors.toList()))
            .setWithVariables(withVariables)
            .setPagination(pagination));
  }

  public List<Task> getTasks(TaskSearchRequest search, boolean withVariables)
      throws TaskListException {
    try {
      reconnectEventually();

      List<Task> tasks = ConverterUtils.toTasks(taskApi.searchTasks(search));
      if (withVariables
          && (search.getIncludeVariables() == null || search.getIncludeVariables().isEmpty())) {
        loadVariables(tasks);
      }
      return tasks;
    } catch (ApiException e) {
      throw new TaskListException("Error searching tasks", e);
    }
  }

  public void loadVariables(List<Task> tasks) throws TaskListException {
    try {
      Map<String, Future<List<Variable>>> futures = new HashMap<>();
      Map<String, Task> taskMap = new HashMap<>();
      for (Task task : tasks) {
        taskMap.put(task.getId(), task);
        futures.put(
            task.getId(),
            CompletableFuture.supplyAsync(
                () -> {
                  try {
                    return getVariables(task.getId());
                  } catch (TaskListException e) {
                    return null;
                  }
                }));
      }
      for (Map.Entry<String, Future<List<Variable>>> varFutures : futures.entrySet()) {
        taskMap.get(varFutures.getKey()).setVariables(varFutures.getValue().get());
      }
      futures.clear();
      taskMap.clear();
    } catch (ExecutionException | InterruptedException e) {
      throw new TaskListException("Error loading task variables", e);
    }
  }

  public void setTokenExpiration(long tokenExpiration) {
    this.tokenExpiration = tokenExpiration;
  }

  private void reconnectEventually() throws TaskListException {
    if (this.properties.isAlwaysReconnect()
        || (this.tokenExpiration > 0
            && this.tokenExpiration < (System.currentTimeMillis() - 1000))) {
      // reset old Token before a new authentication
      properties.getAuthentication().resetToken(Product.TASKLIST);
      authenticate();
    }
  }

  public void authenticate() throws TaskListException {
    Map.Entry<String, String> header =
        properties.getAuthentication().getTokenHeader(Product.TASKLIST);
    if (header.getValue().startsWith("Bearer ")) {
      this.tokenExpiration = JwtUtils.getExpiration(header.getValue().substring(7)) * 1000L;
    } else if (this.properties.getCookieExpiration() != null) {
      this.tokenExpiration =
          System.currentTimeMillis() + this.properties.getCookieExpiration().toMillis();
    }
    this.apiClient.setRequestInterceptor(
        builder -> builder.header(header.getKey(), header.getValue()));
  }
}
