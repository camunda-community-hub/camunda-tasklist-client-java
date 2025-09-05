package io.camunda.tasklist;

import static io.camunda.tasklist.util.ConverterUtils.*;
import static java.util.Optional.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.camunda.client.CamundaClient;
import io.camunda.tasklist.CamundaTasklistClientConfiguration.ApiVersion;
import io.camunda.tasklist.CamundaTasklistClientConfiguration.DefaultProperties;
import io.camunda.tasklist.TasklistClient.RequestVariable;
import io.camunda.tasklist.TasklistClient.TaskAssignment;
import io.camunda.tasklist.TasklistClient.TaskSearch.DateRange;
import io.camunda.tasklist.TasklistClient.TaskSearch.Priority;
import io.camunda.tasklist.TasklistClient.TaskSearch.Sort;
import io.camunda.tasklist.TasklistClient.TaskSearch.Sort.Field;
import io.camunda.tasklist.TasklistClient.TaskSearch.Sort.Order;
import io.camunda.tasklist.TasklistClient.TaskSearch.TaskVariable;
import io.camunda.tasklist.TasklistClient.TaskSearch.TaskVariable.Operator;
import io.camunda.tasklist.TasklistClient.VariableSearch;
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
import io.camunda.tasklist.generated.invoker.ApiClient;
import io.camunda.tasklist.generated.model.IncludeVariable;
import io.camunda.tasklist.generated.model.TaskByVariables;
import io.camunda.tasklist.generated.model.TaskByVariables.OperatorEnum;
import io.camunda.tasklist.generated.model.TaskOrderBy;
import io.camunda.tasklist.generated.model.TaskOrderBy.FieldEnum;
import io.camunda.tasklist.generated.model.TaskOrderBy.OrderEnum;
import io.camunda.tasklist.util.ConverterUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

public class CamundaTaskListClient {
  private final CamundaClient camundaClient;
  private final DefaultProperties defaultProperties;
  private final TasklistClient tasklistClient;
  private final ApiVersion apiVersion;

  public CamundaTaskListClient(CamundaTasklistClientConfiguration configuration) {
    if (configuration.defaultProperties().useCamundaUserTasks()
        && configuration.camundaClient() == null) {
      throw new IllegalStateException("CamundaClient is required when using Camunda user tasks");
    }
    this.camundaClient = configuration.camundaClient();
    this.defaultProperties = configuration.defaultProperties();
    this.tasklistClient = buildClient(configuration);
    this.apiVersion = configuration.apiVersion();
  }

  private static TasklistClient buildClient(CamundaTasklistClientConfiguration configuration) {
    return switch (configuration.apiVersion()) {
      case v1 -> new TasklistClientV1(buildApiClient(configuration));
      case v2 -> new TasklistClientV2(configuration.camundaClient());
    };
  }

  private static ApiClient buildApiClient(CamundaTasklistClientConfiguration configuration) {
    CloseableHttpClient httpClient = buildTasklistHttpClient(configuration.authentication());
    ApiClient apiClient = new ApiClient(httpClient);
    apiClient.setBasePath(configuration.baseUrl().toExternalForm());
    return apiClient;
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

  private static TasklistClient.TaskSearch toTaskSearch(TaskSearch search) {
    return new TasklistClient.TaskSearch(
        toState(search.getState()),
        search.getAssigned(),
        search.getAssignee(),
        search.getAssignees(),
        search.getTaskDefinitionId(),
        search.getCandidateGroup(),
        search.getCandidateGroups(),
        search.getCandidateUser(),
        search.getCandidateUsers(),
        search.getProcessDefinitionKey(),
        search.getProcessInstanceKey(),
        ofNullable(search.getPagination()).map(Pagination::getPageSize).orElse(null),
        toDateRange(search.getFollowUpDate()),
        toDateRange(search.getDueDate()),
        mapIfPresent(search.getTaskVariables(), CamundaTaskListClient::toTaskVariables),
        search.getTenantIds(),
        mapIfPresent(
            ofNullable(search.getPagination()).map(Pagination::getSort).orElse(null),
            CamundaTaskListClient::toSort),
        toSearch(SearchType.AFTER, search.getPagination()),
        toSearch(SearchType.AFTER_OR_EQUAL, search.getPagination()),
        toSearch(SearchType.BEFORE, search.getPagination()),
        toSearch(SearchType.BEFORE_OR_EQUAL, search.getPagination()),
        search.getAfter(),
        search.getBefore(),
        mapIfPresent(search.getIncludeVariables(), CamundaTaskListClient::toIncludeVariable),
        toImplementation(search.getImplementation()),
        toPriority(search.getPriority()));
  }

  private static List<String> toSearch(SearchType expected, Pagination pagination) {
    if (pagination == null) {
      return null;
    }
    if (pagination.getSearchType() == expected) {
      return pagination.getSearch();
    }
    return null;
  }

  private static TasklistClient.IncludeVariable toIncludeVariable(IncludeVariable includeVariable) {
    return new TasklistClient.IncludeVariable(
        includeVariable.getName(), includeVariable.getAlwaysReturnFullValue());
  }

  private static TasklistClient.TaskSearch.Priority toPriority(TaskSearch.Priority priority) {
    if (priority == null) {
      return null;
    }
    return new Priority(
        priority.eq(), priority.gte(), priority.gt(), priority.lt(), priority.lte());
  }

  private static TasklistClient.Implementation toImplementation(Implementation implementation) {
    if (implementation == null) {
      return null;
    }
    return switch (implementation) {
      case JOB_WORKER -> TasklistClient.Implementation.JOB_WORKER;
      case ZEEBE_USER_TASK -> TasklistClient.Implementation.ZEEBE_USER_TASK;
    };
  }

  private static Sort toSort(TaskOrderBy taskOrderBy) {
    return new Sort(toField(taskOrderBy.getField()), toOrder(taskOrderBy.getOrder()));
  }

  private static Order toOrder(OrderEnum order) {
    if (order == null) {
      return null;
    }
    return switch (order) {
      case ASC -> Order.ASC;
      case DESC -> Order.DESC;
    };
  }

  private static Field toField(FieldEnum field) {
    if (field == null) {
      return null;
    }
    return switch (field) {
      case DUE_DATE -> Field.dueDate;
      case PRIORITY -> Field.priority;
      case CREATION_TIME -> Field.creationTime;
      case FOLLOW_UP_DATE -> Field.followUpDate;
      case COMPLETION_TIME -> Field.completionTime;
    };
  }

  private static TaskVariable toTaskVariables(TaskByVariables taskByVariables) {
    return new TaskVariable(
        taskByVariables.getName(),
        taskByVariables.getValue(),
        toOperator(taskByVariables.getOperator()));
  }

  private static Operator toOperator(OperatorEnum operator) {
    if (operator == null) {
      return null;
    }
    return switch (operator) {
      case EQ -> Operator.eq;
    };
  }

  private static DateRange toDateRange(DateFilter followUpDate) {
    if (followUpDate == null) {
      return null;
    }
    return new DateRange(followUpDate.getFrom(), followUpDate.getTo());
  }

  private static TasklistClient.TaskState toState(TaskState state) {
    if (state == null) {
      return null;
    }
    return switch (state) {
      case FAILED -> TasklistClient.TaskState.FAILED;
      case COMPLETED -> TasklistClient.TaskState.COMPLETED;
      case CREATED -> TasklistClient.TaskState.CREATED;
      case CANCELED -> TasklistClient.TaskState.CANCELED;
    };
  }

  public Task unclaim(String taskId) throws TaskListException {
    Task task = getTask(taskId);
    return executeForImplementation(
        task,
        () -> {
          try {
            return ConverterUtils.toTask(tasklistClient.unassignTask(taskId), null);
          } catch (TaskListException e) {
            throw new RuntimeException("Error unclaiming task " + taskId, e);
          }
        },
        () -> {
          camundaClient.newUnassignUserTaskCommand(Long.parseLong(taskId)).send().join();
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
                tasklistClient.assignTask(
                    taskId, new TaskAssignment(assignee, allowOverrideAssignment)),
                null);
          } catch (TaskListException e) {
            throw new RuntimeException("Error while assigning task via tasklist api", e);
          }
        },
        () -> {
          camundaClient
              .newAssignUserTaskCommand(Long.parseLong(taskId))
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
            return tasklistClient.completeTask(
                taskId, ConverterUtils.toVariableInput(variablesMap));
          } catch (TaskListException e) {
            throw new RuntimeException("Error while completing task via tasklist api", e);
          }
        },
        () ->
            camundaClient
                .newCompleteUserTaskCommand(Long.parseLong(taskId))
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
      if (camundaClient == null) {
        throw new IllegalStateException(
            "camundaClient must not be null, please set useCamundaUserTasks to assert this on startup");
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

      return ConverterUtils.toTask(tasklistClient.getTask(taskId), variables);
    } catch (TaskListException e) {
      throw new TaskListException("Error reading task " + taskId, e);
    }
  }

  public List<Variable> getVariables(String taskId) throws TaskListException {
    return getVariables(taskId, defaultProperties.loadTruncatedVariables());
  }

  public List<Variable> getVariables(String taskId, boolean loadTruncated)
      throws TaskListException {
    try {
      return tasklistClient.searchTaskVariables(taskId, new VariableSearch(null, null)).stream()
          .map(
              vsr -> {
                if (loadTruncated && Boolean.TRUE.equals(vsr.isValueTruncated())) {
                  try {
                    return getVariable(vsr.id());
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
    } catch (RuntimeException e) {
      throw new TaskListException("Error reading task " + taskId, e);
    }
  }

  public Variable getVariable(String variableId) throws TaskListException {
    try {
      return toVariable(tasklistClient.getVariable(variableId));
    } catch (JsonProcessingException e) {
      throw new TaskListException("Error while loading variable " + variableId, e);
    }
  }

  public Form getForm(String formId, String processDefinitionId) throws TaskListException {
    return getForm(formId, processDefinitionId, null);
  }

  public Form getForm(String formId, String processDefinitionId, Long version)
      throws TaskListException {
    if (formId.startsWith(CamundaTasklistConstants.CAMUNDA_FORMS_PREFIX)) {
      formId = formId.substring(CamundaTasklistConstants.CAMUNDA_FORMS_PREFIX.length());
    }
    return ConverterUtils.toForm(tasklistClient.getForm(formId, processDefinitionId, version));
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
      case AFTER_OR_EQUAL ->
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
    return new TaskList()
        .setItems(getTasks(toTaskSearch(search), search.getWithVariables()))
        .setSearch(search);
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

  public List<Task> getTasks(TasklistClient.TaskSearch search, boolean withVariables)
      throws TaskListException {

    List<Task> tasks = ConverterUtils.toTasks(tasklistClient.searchTasks(search));
    if (withVariables
        && (search.includeVariables() == null || search.includeVariables().isEmpty())) {
      loadVariables(tasks);
    }
    return tasks;
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
    List<RequestVariable> convertedVariables = ConverterUtils.toVariableInput(variables);
    tasklistClient.saveDraftVariables(taskId, convertedVariables);
  }
}
