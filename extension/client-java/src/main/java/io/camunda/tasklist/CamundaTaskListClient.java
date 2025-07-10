package io.camunda.tasklist;

import static io.camunda.tasklist.util.ConverterUtils.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.camunda.tasklist.CamundaTasklistClientConfiguration.DefaultProperties;
import io.camunda.tasklist.auth.Authentication;
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
import io.camunda.tasklist.generated.model.IncludeVariable;
import io.camunda.tasklist.generated.model.SaveVariablesRequest;
import io.camunda.tasklist.generated.model.TaskAssignRequest;
import io.camunda.tasklist.generated.model.TaskByVariables;
import io.camunda.tasklist.generated.model.TaskCompleteRequest;
import io.camunda.tasklist.generated.model.TaskSearchRequest;
import io.camunda.tasklist.generated.model.VariableInputDTO;
import io.camunda.tasklist.generated.model.VariablesSearchRequest;
import io.camunda.tasklist.util.ConverterUtils;
import io.camunda.zeebe.client.ZeebeClient;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

public class CamundaTaskListClient {
  private final ZeebeClient zeebeClient;
  private final DefaultProperties defaultProperties;
  private final TaskApi taskApi;
  private final FormApi formApi;
  private final VariablesApi variablesApi;

  @Deprecated
  public CamundaTaskListClient(
      CamundaTaskListClientProperties properties, ZeebeClient zeebeClient) {
    this(
        new CamundaTasklistClientConfiguration(
            Objects.requireNonNull(properties.getAuthentication(), "No authentication provided"),
            toUrl(properties.getTaskListUrl()),
            zeebeClient,
            new DefaultProperties(
                properties.isDefaultShouldReturnVariables(),
                properties.isDefaultShouldLoadTruncatedVariables(),
                properties.isUseZeebeUserTasks(),
                properties.getTenantIds())));
  }

  public CamundaTaskListClient(CamundaTasklistClientConfiguration configuration) {
    if (configuration.defaultProperties().useZeebeUserTasks()
        && configuration.zeebeClient() == null) {
      throw new IllegalStateException("ZeebeClient is required when using ZeebeUserTasks");
    }
    this.zeebeClient = configuration.zeebeClient();
    this.defaultProperties = configuration.defaultProperties();
    CloseableHttpClient httpClient = buildTasklistHttpClient(configuration.authentication());
    ApiClient apiClient = new ApiClient(httpClient);
    apiClient.setBasePath(configuration.baseUrl().toExternalForm());
    this.taskApi = new TaskApi(apiClient);
    this.formApi = new FormApi(apiClient);
    this.variablesApi = new VariablesApi(apiClient);
  }

  private static URL toUrl(String url) {
    try {
      return URI.create(url).toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException("Error while creating tasklist url", e);
    }
  }

  private static CloseableHttpClient buildTasklistHttpClient(Authentication authentication) {
    return HttpClients.custom()
        .useSystemProperties()
        .addRequestInterceptorFirst(
            (request, entity, context) ->
                authentication.getTokenHeader().forEach(request::addHeader))
        .build();
  }

  @Deprecated
  public static CamundaTaskListClientBuilder builder() {
    return new CamundaTaskListClientBuilder();
  }

  public Task unclaim(String taskId) throws TaskListException {
    Task task = getTask(taskId);
    return executeForImplementation(
        task,
        () -> {
          try {
            return ConverterUtils.toTask(taskApi.unassignTask(taskId), null);
          } catch (TaskListException | ApiException e) {
            throw new RuntimeException("Error unclaiming task " + taskId, e);
          }
        },
        () -> {
          zeebeClient.newUserTaskUnassignCommand(Long.parseLong(taskId)).send().join();
          task.setAssignee(null);
          return task;
        });
  }

  public Task claim(String taskId, String assignee) throws TaskListException {
    return claim(taskId, assignee, false);
  }

  public Task claim(String taskId, String assignee, Boolean allowOverrideAssignment)
      throws TaskListException {
    Task task = getTask(taskId);
    return executeForImplementation(
        task,
        () -> {
          try {
            return ConverterUtils.toTask(
                taskApi.assignTask(
                    taskId,
                    new TaskAssignRequest()
                        .assignee(assignee)
                        .allowOverrideAssignment(allowOverrideAssignment)),
                null);
          } catch (TaskListException | ApiException e) {
            throw new RuntimeException("Error while assigning task via tasklist api", e);
          }
        },
        () -> {
          zeebeClient
              .newUserTaskAssignCommand(Long.parseLong(taskId))
              .allowOverride(allowOverrideAssignment)
              .assignee(assignee)
              .send()
              .join();
          task.setAssignee(assignee);
          return task;
        });
  }

  public void completeTask(String taskId, Map<String, Object> variablesMap)
      throws TaskListException {
    executeForImplementation(
        getTask(taskId),
        () -> {
          try {
            return taskApi.completeTask(
                taskId,
                new TaskCompleteRequest().variables(ConverterUtils.toVariableInput(variablesMap)));
          } catch (ApiException | TaskListException e) {
            throw new RuntimeException("Error while completing task via tasklist api", e);
          }
        },
        () ->
            zeebeClient
                .newUserTaskCompleteCommand(Long.parseLong(taskId))
                .variables(variablesMap)
                .send()
                .join());
  }

  private <T> T executeForImplementation(
      Task task, Supplier<T> jobWorkerAction, Supplier<T> zeebeUserTaskAction) {
    if (task.getImplementation() == null
        || task.getImplementation().equals(Implementation.JOB_WORKER)) {
      return jobWorkerAction.get();
    } else if (task.getImplementation().equals(Implementation.ZEEBE_USER_TASK)) {
      if (zeebeClient == null) {
        throw new IllegalStateException(
            "zeebeClient must not be null, please set useZeebeUserTasks to assert this on startup");
      }
      return zeebeUserTaskAction.get();
    } else {
      throw new IllegalArgumentException("Unsupported implementation: " + task.getImplementation());
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
            .setWithVariables(defaultProperties.returnVariables())
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
        new TaskSearch()
            .setCandidateGroup(group)
            .setState(state)
            .setPagination(createPagination(pageSize)));
  }

  public TaskList getGroupTasks(String group, TaskState state, Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch().setCandidateGroup(group).setState(state).setPagination(pagination));
  }

  public TaskList getGroupTasks(
      String group, TaskState state, boolean withVariables, Integer pageSize)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setCandidateGroup(group)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(createPagination(pageSize)));
  }

  public TaskList getGroupTasks(
      String group, TaskState state, boolean withVariables, Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setCandidateGroup(group)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(pagination));
  }

  public TaskList getGroupsTasks(
      List<String> groups, TaskState state, boolean withVariables, Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setCandidateGroups(groups)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(pagination));
  }

  public Task getTask(String taskId) throws TaskListException {
    return getTask(taskId, defaultProperties.returnVariables());
  }

  public Task getTask(String taskId, boolean withVariables) throws TaskListException {

    try {
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
    return getVariables(taskId, defaultProperties.loadTruncatedVariables());
  }

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
      if (formId.startsWith(CamundaTasklistConstants.CAMUNDA_FORMS_PREFIX)) {
        formId = formId.substring(CamundaTasklistConstants.CAMUNDA_FORMS_PREFIX.length());
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
        taskList.getSearch().clone().setPagination(getSearchPagination(taskList, direction));
    return getTasks(newSearch);
  }

  private Pagination getSearchPagination(TaskList taskList, SearchType type) {
    return switch (type) {
      case BEFORE ->
          new Pagination.Builder()
              .pageSize(taskList.getSearch().getPagination().getPageSize())
              .before(taskList.first().getSortValues())
              .build();
      case BEFORE_OR_EQUAL ->
          new Pagination.Builder()
              .pageSize(taskList.getSearch().getPagination().getPageSize())
              .beforeOrEqual(taskList.first().getSortValues())
              .build();
      case AFTER ->
          new Pagination.Builder()
              .pageSize(taskList.getSearch().getPagination().getPageSize())
              .after(taskList.last().getSortValues())
              .build();
      default ->
          new Pagination.Builder()
              .pageSize(taskList.getSearch().getPagination().getPageSize())
              .afterOrEqual(taskList.last().getSortValues())
              .build();
    };
  }

  public TaskList getTasks(TaskSearch search) throws TaskListException {
    if (search.getWithVariables() == null) {
      search.setWithVariables(defaultProperties.returnVariables());
    }
    if (search.getTenantIds() == null || search.getTenantIds().isEmpty()) {
      search.setTenantIds(defaultProperties.tenantIds());
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
            .setCandidateGroup(group)
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
            .setCandidateGroup(group)
            .setCandidateGroups(groups)
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
            .setIncludeVariables(includeVariables)
            .setWithVariables(withVariables)
            .setPagination(pagination));
  }

  public List<Task> getTasks(TaskSearchRequest search, boolean withVariables)
      throws TaskListException {
    try {

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
    loadVariables(tasks, defaultProperties.loadTruncatedVariables());
  }

  public void loadVariables(List<Task> tasks, boolean loadTruncated) throws TaskListException {
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
                    return getVariables(task.getId(), loadTruncated);
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

  public void saveDraftVariables(String taskId, Map<String, Object> variables)
      throws TaskListException {
    try {
      List<VariableInputDTO> convertedVariables = ConverterUtils.toVariableInput(variables);
      SaveVariablesRequest variablesInput = new SaveVariablesRequest();
      variablesInput.setVariables(convertedVariables);
      taskApi.saveDraftTaskVariables(taskId, variablesInput);
    } catch (ApiException e) {
      throw new TaskListException("Error saving draft variables for task " + taskId, e);
    }
  }
}
