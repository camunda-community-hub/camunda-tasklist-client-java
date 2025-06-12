package io.camunda.tasklist;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.JsonMapper;
import io.camunda.client.api.search.enums.UserTaskState;
import io.camunda.client.api.search.filter.UserTaskFilter;
import io.camunda.client.api.search.filter.UserTaskVariableFilter;
import io.camunda.client.api.search.request.SearchRequestPage;
import io.camunda.client.api.search.response.SearchResponse;
import io.camunda.client.api.search.response.UserTask;
import io.camunda.client.api.search.sort.UserTaskSort;
import io.camunda.tasklist.TasklistClient.TaskSearch.Sort;
import io.camunda.tasklist.TasklistClient.TaskSearch.TaskVariable;
import io.camunda.tasklist.exception.CompatibilityException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TasklistClientV2 implements TasklistClient {
  private static final Logger LOG = LoggerFactory.getLogger(TasklistClientV2.class);
  private final CamundaClient camundaClient;

  public TasklistClientV2(CamundaClient camundaClient) {
    this.camundaClient = camundaClient;
  }

  private static Map<String, Object> toMap(List<RequestVariable> variables, JsonMapper jsonMapper) {
    Map<String, Object> result = new HashMap<>();
    variables.forEach(
        variable -> {
          result.put(variable.name(), jsonMapper.fromJson(variable.value(), Object.class));
        });
    return result;
  }

  private static VariableFromSearch toVariableFromSearch(
      io.camunda.client.api.search.response.Variable variable) {
    return new VariableFromSearch(
        String.valueOf(variable.getVariableKey()),
        variable.getName(),
        variable.getValue(),
        false,
        variable.getValue(),
        null);
  }

  private static FormV2 toFormV2(io.camunda.client.api.search.response.Form form) {
    return new FormV2(
        form.getTenantId(),
        form.getFormId(),
        form.getSchema(),
        form.getVersion(),
        String.valueOf(form.getFormKey()));
  }

  private static Consumer<UserTaskVariableFilter> fromVariableSearchToFilter(
      VariableSearch variableSearch) {
    return filter -> {
      if (variableSearch.variableNames() != null && !variableSearch.variableNames().isEmpty()) {
        filter.name(p -> p.in(variableSearch.variableNames()));
      }
    };
  }

  private static TaskFromSearch toTaskFromSearch(UserTask userTask, List<Object> sort) {
    String processName = null; // can be found in process definition
    List<String> sortValues = sort != null ? sort.stream().map(Object::toString).toList() : null;
    Boolean isFirst = null;
    Boolean isFormEmbedded = null;
    List<VariableFromSearch> variables = null;
    Implementation implementation = Implementation.ZEEBE_USER_TASK;
    return new TaskFromSearch(
        String.valueOf(userTask.getUserTaskKey()),
        userTask.getName(),
        userTask.getElementId(),
        processName,
        userTask.getCreationDate(),
        userTask.getCompletionDate(),
        userTask.getAssignee(),
        toTaskState(userTask.getState()),
        sortValues,
        isFirst,
        String.valueOf(userTask.getFormKey()),
        userTask.getExternalFormReference(),
        isFormEmbedded,
        String.valueOf(userTask.getProcessDefinitionKey()),
        String.valueOf(userTask.getProcessInstanceKey()),
        userTask.getTenantId(),
        OffsetDateTime.parse(userTask.getDueDate()),
        OffsetDateTime.parse(userTask.getFollowUpDate()),
        userTask.getCandidateGroups(),
        userTask.getCandidateUsers(),
        variables,
        implementation,
        userTask.getPriority());
  }

  private static Task toTask(UserTask userTask) {
    String processName = null; // can be found in process definition
    Boolean isFormEmbedded = null;
    Implementation implementation = Implementation.ZEEBE_USER_TASK;
    return new Task(
        String.valueOf(userTask.getUserTaskKey()),
        userTask.getName(),
        userTask.getElementId(),
        processName,
        userTask.getCreationDate(),
        userTask.getCompletionDate(),
        userTask.getAssignee(),
        toTaskState(userTask.getState()),
        String.valueOf(userTask.getFormKey()),
        userTask.getExternalFormReference(),
        isFormEmbedded,
        String.valueOf(userTask.getProcessDefinitionKey()),
        String.valueOf(userTask.getProcessInstanceKey()),
        userTask.getTenantId(),
        OffsetDateTime.parse(userTask.getDueDate()),
        OffsetDateTime.parse(userTask.getFollowUpDate()),
        userTask.getCandidateGroups(),
        userTask.getCandidateUsers(),
        implementation,
        userTask.getPriority());
  }

  private static TasklistClient.TaskState toTaskState(UserTaskState state) {
    if (state == null) {
      return null;
    }
    return switch (state) {
      case COMPLETED -> TasklistClient.TaskState.COMPLETED;
      case FAILED -> TasklistClient.TaskState.FAILED;
      case CREATED -> TasklistClient.TaskState.CREATED;
      case CANCELED -> TasklistClient.TaskState.CANCELED;
      case UNKNOWN_ENUM_VALUE -> TasklistClient.TaskState.UNKNOWN_ENUM_VALUE;
    };
  }

  private static UserTaskState toTaskState(TasklistClient.TaskState state) {
    if (state == null) {
      return null;
    }
    return switch (state) {
      case COMPLETED -> UserTaskState.COMPLETED;
      case FAILED -> UserTaskState.FAILED;
      case CREATED -> UserTaskState.CREATED;
      case CANCELED -> UserTaskState.CANCELED;
      case UNKNOWN_ENUM_VALUE -> UserTaskState.UNKNOWN_ENUM_VALUE;
    };
  }

  private static Consumer<UserTaskSort> fromTaskSearchToSort(TaskSearch taskSearch) {
    return sort -> {
      List<Sort> sorts = taskSearch.sort();
      for (Sort s : sorts) {
        switch (s.field()) {
          case dueDate -> sort.dueDate();
          case priority -> sort.priority();
          case creationTime -> sort.creationDate();
          case followUpDate -> sort.followUpDate();
          case completionTime -> sort.completionDate();
        }
        if (s.order() == null) {
          sort.desc(); // this is the default
        } else {
          switch (s.order()) {
            case ASC -> sort.asc();
            case DESC -> sort.desc();
          }
        }
      }
    };
  }

  private static Consumer<UserTaskFilter> fromTaskSearchToFilter(
      TaskSearch taskSearch, JsonMapper jsonMapper) {
    return filter -> {
      if (taskSearch.state() != null) {
        filter.state(toTaskState(taskSearch.state()));
      }
      if (taskSearch.assigned() != null) {
        filter.assignee(p -> p.exists(taskSearch.assigned()));
      }
      if (taskSearch.assignee() != null) {
        filter.assignee(taskSearch.assignee());
      }
      if (taskSearch.assignees() != null) {
        filter.assignee(p -> p.in(taskSearch.assignees()));
      }
      if (taskSearch.taskDefinitionId() != null) {
        filter.elementId(taskSearch.taskDefinitionId());
      }
      if (taskSearch.candidateGroups() != null) {
        filter.candidateGroup(taskSearch.candidateGroup());
      }
      if (taskSearch.candidateGroups() != null) {
        filter.candidateGroup(p -> p.in(taskSearch.candidateGroups()));
      }
      if (taskSearch.candidateUser() != null) {
        filter.candidateUser(taskSearch.candidateUser());
      }
      if (taskSearch.candidateUsers() != null) {
        filter.candidateUser(p -> p.in(taskSearch.candidateUsers()));
      }
      if (taskSearch.processDefinitionKey() != null) {
        filter.processDefinitionKey(Long.valueOf(taskSearch.processDefinitionKey()));
      }
      if (taskSearch.processInstanceKey() != null) {
        filter.processInstanceKey(Long.valueOf(taskSearch.processInstanceKey()));
      }
      if (taskSearch.followUpDate() != null) {
        filter.followUpDate(
            p -> p.gte(taskSearch.followUpDate().from()).lte(taskSearch.followUpDate().to()));
      }
      if (taskSearch.dueDate() != null) {
        filter.dueDate(p -> p.gte(taskSearch.dueDate().from()).lte(taskSearch.dueDate().to()));
      }
      if (taskSearch.taskVariables() != null) {
        Map<String, Object> variables = new HashMap<>();
        for (TaskVariable taskVariable : taskSearch.taskVariables()) {
          variables.put(
              taskVariable.name(), jsonMapper.fromJson(taskVariable.value(), Object.class));
        }
        filter.localVariables(variables);
      }
      if (taskSearch.tenantIds() != null) {
        // TODO fix this as soon as the API is overhauled
        if (taskSearch.tenantIds().size() == 1) {
          filter.tenantId(taskSearch.tenantIds().get(0));
        } else {
          LOG.warn("Only search for single tenants is supported as of now");
        }
      }
      if (taskSearch.includeVariables() != null) {
        LOG.warn(
            "TaskSearch.includeVariables not implemented, please use a dedicated variable search instead");
      }
      if (taskSearch.priority() != null) {
        filter.priority(
            p -> {
              if (taskSearch.priority().gt() != null) {
                p.gte(taskSearch.priority().gt());
              }
              if (taskSearch.priority().lte() != null) {
                p.lte(taskSearch.priority().lte());
              }
              if (taskSearch.priority().eq() != null) {
                p.eq(taskSearch.priority().eq());
              }
              if (taskSearch.priority().gte() != null) {
                p.gte(taskSearch.priority().gte());
              }
              if (taskSearch.priority().lt() != null) {
                p.lte(taskSearch.priority().lt());
              }
            });
      }
    };
  }

  private static Consumer<SearchRequestPage> fromTaskSearchToPage(TaskSearch taskSearch) {
    return page -> {
      if (taskSearch.pageSize() != null) {
        page.limit(taskSearch.pageSize());
      }
      if (taskSearch.searchAfter() != null) {
        page.searchAfter(taskSearch.searchAfter().stream().map(Object.class::cast).toList());
      }
      if (taskSearch.searchBefore() != null) {
        page.searchBefore(taskSearch.searchBefore().stream().map(Object.class::cast).toList());
      }
      if (taskSearch.searchAfterOrEqual() != null) {
        LOG.warn("searchAfterOrEqual is not supported, please use searchAfter");
      }
      if (taskSearch.searchBeforeOrEqual() != null) {
        LOG.warn("searchBeforeOrEqual is not supported, please use searchBefore");
      }
    };
  }

  private static boolean isLast(UserTask item, List<UserTask> items) {
    return items.indexOf(item) == items.size() - 1;
  }

  private static boolean isFirst(UserTask item, List<UserTask> items) {
    return items.indexOf(item) == 0;
  }

  @Override
  public Form getForm(String formId, String processDefinitionKey, Long version) {
    throw new CompatibilityException(
        "Getting a form is only possible with a user task key for user tasks or the process definition key for process start forms");
  }

  @Override
  public FormV2 getFormForTask(String userTaskKey) {
    return toFormV2(
        camundaClient.newUserTaskGetFormRequest(Long.parseLong(userTaskKey)).send().join());
  }

  @Override
  public FormV2 getStartFormForProcess(String processDefinitionKey) {
    return toFormV2(
        camundaClient
            .newProcessDefinitionGetFormRequest(Long.parseLong(processDefinitionKey))
            .send()
            .join());
  }

  @Override
  public void saveDraftVariables(String taskId, List<RequestVariable> variables) {
    UserTask userTask = camundaClient.newUserTaskGetRequest(Long.parseLong(taskId)).send().join();
    camundaClient
        .newSetVariablesCommand(userTask.getElementInstanceKey())
        .variables(toMap(variables, camundaClient.getConfiguration().getJsonMapper()))
        .local(true)
        .send()
        .join();
  }

  @Override
  public List<VariableFromSearch> searchTaskVariables(
      String taskId, VariableSearch variableSearch) {
    SearchResponse<io.camunda.client.api.search.response.Variable> searchResponse =
        camundaClient
            .newUserTaskVariableSearchRequest(Long.parseLong(taskId))
            .filter(fromVariableSearchToFilter(variableSearch))
            .send()
            .join();
    return searchResponse.items().stream().map(TasklistClientV2::toVariableFromSearch).toList();
  }

  @Override
  public List<TaskFromSearch> searchTasks(TaskSearch taskSearch) {
    if (taskSearch.implementation() == Implementation.JOB_WORKER) {
      throw new CompatibilityException("Job worker user tasks are not supported with the V2 api");
    }
    SearchResponse<UserTask> searchResponse =
        camundaClient
            .newUserTaskSearchRequest()
            .page(fromTaskSearchToPage(taskSearch))
            .sort(fromTaskSearchToSort(taskSearch))
            .filter(
                fromTaskSearchToFilter(
                    taskSearch, camundaClient.getConfiguration().getJsonMapper()))
            .send()
            .join();
    List<UserTask> items = searchResponse.items();
    List<TaskFromSearch> result = new ArrayList<>();
    for (UserTask item : items) {
      if (isFirst(item, items)) {
        result.add(toTaskFromSearch(item, searchResponse.page().firstSortValues()));
      } else if (isLast(item, items)) {
        result.add(toTaskFromSearch(item, searchResponse.page().lastSortValues()));
      } else {
        result.add(toTaskFromSearch(item, null));
      }
    }
    return result;
  }

  @Override
  public Optional<Task> unassignTask(String taskId) {
    camundaClient.newUserTaskUnassignCommand(Long.parseLong(taskId)).send().join();
    return Optional.empty();
  }

  @Override
  public Optional<Task> completeTask(String taskId, List<RequestVariable> variables) {
    camundaClient
        .newUserTaskCompleteCommand(Long.parseLong(taskId))
        .variables(toMap(variables, camundaClient.getConfiguration().getJsonMapper()))
        .send()
        .join();
    return Optional.empty();
  }

  @Override
  public Optional<Task> assignTask(String taskId, TaskAssignment taskAssignment) {
    camundaClient
        .newUserTaskAssignCommand(Long.parseLong(taskId))
        .assignee(taskAssignment.assignee())
        .allowOverride(taskAssignment.allowOverrideAssignment())
        .send()
        .join();
    return Optional.empty();
  }

  @Override
  public Task getTask(String taskId) {
    UserTask task = camundaClient.newUserTaskGetRequest(Long.parseLong(taskId)).send().join();
    return toTask(task);
  }

  @Override
  public Variable getVariable(String variableId) {
    return null;
  }
}
