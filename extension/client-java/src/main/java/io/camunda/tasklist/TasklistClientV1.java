package io.camunda.tasklist;

import static io.camunda.tasklist.util.ConverterUtils.*;

import io.camunda.tasklist.TasklistClient.TaskSearch.DateRange;
import io.camunda.tasklist.TasklistClient.TaskSearch.Priority;
import io.camunda.tasklist.TasklistClient.TaskSearch.Sort;
import io.camunda.tasklist.TasklistClient.TaskSearch.Sort.Field;
import io.camunda.tasklist.TasklistClient.TaskSearch.Sort.Order;
import io.camunda.tasklist.TasklistClient.TaskSearch.TaskVariable;
import io.camunda.tasklist.TasklistClient.TaskSearch.TaskVariable.Operator;
import io.camunda.tasklist.TasklistClient.Variable.VariableDraft;
import io.camunda.tasklist.TasklistClient.VariableFromSearch.VariableDraftFromSearch;
import io.camunda.tasklist.exception.CompatibilityException;
import io.camunda.tasklist.generated.api.FormApi;
import io.camunda.tasklist.generated.api.TaskApi;
import io.camunda.tasklist.generated.api.VariablesApi;
import io.camunda.tasklist.generated.invoker.ApiClient;
import io.camunda.tasklist.generated.invoker.ApiException;
import io.camunda.tasklist.generated.model.DateFilter;
import io.camunda.tasklist.generated.model.DraftSearchVariableValue;
import io.camunda.tasklist.generated.model.DraftVariableValue;
import io.camunda.tasklist.generated.model.FormResponse;
import io.camunda.tasklist.generated.model.RangeValueFilter;
import io.camunda.tasklist.generated.model.SaveVariablesRequest;
import io.camunda.tasklist.generated.model.TaskAssignRequest;
import io.camunda.tasklist.generated.model.TaskByVariables;
import io.camunda.tasklist.generated.model.TaskByVariables.OperatorEnum;
import io.camunda.tasklist.generated.model.TaskCompleteRequest;
import io.camunda.tasklist.generated.model.TaskOrderBy;
import io.camunda.tasklist.generated.model.TaskOrderBy.FieldEnum;
import io.camunda.tasklist.generated.model.TaskOrderBy.OrderEnum;
import io.camunda.tasklist.generated.model.TaskResponse;
import io.camunda.tasklist.generated.model.TaskSearchRequest;
import io.camunda.tasklist.generated.model.TaskSearchRequest.ImplementationEnum;
import io.camunda.tasklist.generated.model.TaskSearchRequest.StateEnum;
import io.camunda.tasklist.generated.model.TaskSearchResponse;
import io.camunda.tasklist.generated.model.TaskSearchResponse.TaskStateEnum;
import io.camunda.tasklist.generated.model.VariableInputDTO;
import io.camunda.tasklist.generated.model.VariableResponse;
import io.camunda.tasklist.generated.model.VariableSearchResponse;
import io.camunda.tasklist.generated.model.VariablesSearchRequest;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Deprecated
public class TasklistClientV1 implements TasklistClient {
  private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern(DATE_FORMAT);

  private final FormApi formApi;
  private final TaskApi taskApi;
  private final VariablesApi variablesApi;

  public TasklistClientV1(ApiClient apiClient) {
    this.formApi = new FormApi(apiClient);
    this.taskApi = new TaskApi(apiClient);
    this.variablesApi = new VariablesApi(apiClient);
  }

  public TasklistClientV1(FormApi formApi, TaskApi taskApi, VariablesApi variablesApi) {
    this.formApi = formApi;
    this.taskApi = taskApi;
    this.variablesApi = variablesApi;
  }

  private static Form toForm(FormResponse formResponse) {
    return new Form(
        formResponse.getId(),
        formResponse.getProcessDefinitionKey(),
        formResponse.getTitle(),
        formResponse.getSchema(),
        formResponse.getVersion(),
        formResponse.getTenantId(),
        formResponse.getIsDeleted());
  }

  private static SaveVariablesRequest fromVariablesToSave(List<RequestVariable> variables) {
    SaveVariablesRequest request = new SaveVariablesRequest();
    if (variables != null) {
      request.setVariables(variables.stream().map(TasklistClientV1::fromVariable).toList());
    }
    return request;
  }

  private static TaskCompleteRequest fromVariablesToComplete(List<RequestVariable> variables) {
    TaskCompleteRequest request = new TaskCompleteRequest();
    if (variables != null) {
      request.setVariables(variables.stream().map(TasklistClientV1::fromVariable).toList());
    }
    return request;
  }

  private static VariableInputDTO fromVariable(RequestVariable variable) {
    VariableInputDTO dto = new VariableInputDTO();
    dto.setName(variable.name());
    dto.setValue(variable.value());
    return dto;
  }

  private static VariablesSearchRequest fromVariableSearch(VariableSearch variableSearch) {
    VariablesSearchRequest request = new VariablesSearchRequest();
    if (variableSearch.includeVariables() != null) {
      request.setIncludeVariables(
          variableSearch.includeVariables().stream()
              .map(TasklistClientV1::fromIncludeVariable)
              .toList());
    }
    request.setVariableNames(variableSearch.variableNames());
    return request;
  }

  private static io.camunda.tasklist.generated.model.IncludeVariable fromIncludeVariable(
      IncludeVariable includeVariable) {
    io.camunda.tasklist.generated.model.IncludeVariable request =
        new io.camunda.tasklist.generated.model.IncludeVariable();
    request.setName(includeVariable.name());
    request.setAlwaysReturnFullValue(includeVariable.alwaysReturnFullValue());
    return request;
  }

  private static List<VariableFromSearch> toVariableList(
      List<VariableSearchResponse> variableSearchResponses) {
    return variableSearchResponses.stream().map(TasklistClientV1::toVariable).toList();
  }

  private static VariableFromSearch toVariable(VariableSearchResponse variableSearchResponse) {
    return new VariableFromSearch(
        variableSearchResponse.getId(),
        variableSearchResponse.getName(),
        variableSearchResponse.getValue(),
        variableSearchResponse.getIsValueTruncated(),
        variableSearchResponse.getPreviewValue(),
        toDraft(variableSearchResponse.getDraft()));
  }

  private static Variable toVariable(VariableResponse variableSearchResponse) {
    return new Variable(
        variableSearchResponse.getId(),
        variableSearchResponse.getName(),
        variableSearchResponse.getValue(),
        toDraft(variableSearchResponse.getDraft()),
        variableSearchResponse.getTenantId());
  }

  private static VariableDraftFromSearch toDraft(DraftSearchVariableValue draft) {
    if (draft == null) {
      return null;
    }
    return new VariableDraftFromSearch(
        draft.getValue(), draft.getIsValueTruncated(), draft.getPreviewValue());
  }

  private static VariableDraft toDraft(DraftVariableValue draft) {
    if (draft == null) {
      return null;
    }
    return new VariableDraft(draft.getValue());
  }

  private static TaskSearchRequest fromTaskSearch(TaskSearch taskSearch) {
    TaskSearchRequest request = new TaskSearchRequest();
    request.setState(fromState(taskSearch.state()));
    request.setAssigned(taskSearch.assigned());
    request.setAssignee(taskSearch.assignee());
    request.setAssignees(taskSearch.assignees());
    request.setTaskDefinitionId(taskSearch.taskDefinitionId());
    request.setCandidateGroup(taskSearch.candidateGroup());
    request.setCandidateGroups(taskSearch.candidateGroups());
    request.setCandidateUser(taskSearch.candidateUser());
    request.setCandidateUsers(taskSearch.candidateUsers());
    request.setProcessDefinitionKey(taskSearch.processDefinitionKey());
    request.setProcessInstanceKey(taskSearch.processInstanceKey());
    request.setPageSize(taskSearch.pageSize());
    request.setFollowUpDate(fromDateRange(taskSearch.followUpDate()));
    request.setDueDate(fromDateRange(taskSearch.dueDate()));
    request.setTaskVariables(
        mapIfPresent(taskSearch.taskVariables(), TasklistClientV1::fromTaskVariable));
    request.setTenantIds(taskSearch.tenantIds());
    request.setSort(mapIfPresent(taskSearch.sort(), TasklistClientV1::fromSort));
    request.setSearchAfter(taskSearch.searchAfter());
    request.setSearchBefore(taskSearch.searchBefore());
    request.setSearchAfterOrEqual(taskSearch.searchAfterOrEqual());
    request.setSearchBeforeOrEqual(taskSearch.searchBeforeOrEqual());
    request.setIncludeVariables(
        mapIfPresent(taskSearch.includeVariables(), TasklistClientV1::fromIncludeVariable));
    request.setImplementation(FromImplementation(taskSearch.implementation()));
    request.setPriority(fromPriority(taskSearch.priority()));
    return request;
  }

  private static RangeValueFilter fromPriority(Priority priority) {
    if (priority == null) {
      return null;
    }
    RangeValueFilter request = new RangeValueFilter();
    request.setEq(priority.eq());
    request.setGte(priority.gte());
    request.setGt(priority.gt());
    request.setLt(priority.lt());
    request.setLte(priority.lte());
    return request;
  }

  private static ImplementationEnum FromImplementation(Implementation implementation) {
    if (implementation == null) {
      return null;
    }
    return switch (implementation) {
      case JOB_WORKER -> ImplementationEnum.JOB_WORKER;
      case ZEEBE_USER_TASK -> ImplementationEnum.ZEEBE_USER_TASK;
    };
  }

  private static TaskOrderBy fromSort(Sort sort) {
    TaskOrderBy taskOrderBy = new TaskOrderBy();
    taskOrderBy.setField(fromField(sort.field()));
    taskOrderBy.setOrder(fromOrder(sort.order()));
    return taskOrderBy;
  }

  private static OrderEnum fromOrder(Order order) {
    if (order == null) {
      return null;
    }
    return switch (order) {
      case ASC -> OrderEnum.ASC;
      case DESC -> OrderEnum.DESC;
    };
  }

  private static FieldEnum fromField(Field field) {
    if (field == null) {
      return null;
    }
    return switch (field) {
      case dueDate -> FieldEnum.DUE_DATE;
      case priority -> FieldEnum.PRIORITY;
      case creationTime -> FieldEnum.CREATION_TIME;
      case followUpDate -> FieldEnum.FOLLOW_UP_DATE;
      case completionTime -> FieldEnum.COMPLETION_TIME;
      case name -> null;
    };
  }

  private static TaskByVariables fromTaskVariable(TaskVariable taskVariable) {
    TaskByVariables request = new TaskByVariables();
    request.setName(taskVariable.name());
    request.setValue(taskVariable.value());
    request.setOperator(fromOperator(taskVariable.operator()));
    return request;
  }

  private static OperatorEnum fromOperator(Operator operator) {
    if (operator == null) {
      return null;
    }
    return switch (operator) {
      case eq -> OperatorEnum.EQ;
    };
  }

  private static DateFilter fromDateRange(DateRange dateRange) {
    if (dateRange == null) {
      return null;
    }
    DateFilter dateFilter = new DateFilter();
    dateFilter.setFrom(dateRange.from());
    dateFilter.setTo(dateRange.to());
    return dateFilter;
  }

  private static StateEnum fromState(TaskState state) {
    if (state == null) {
      return null;
    }
    return switch (state) {
      case CREATED -> StateEnum.CREATED;
      case FAILED -> StateEnum.FAILED;
      case CANCELED -> StateEnum.CANCELED;
      case COMPLETED -> StateEnum.COMPLETED;
      case ASSIGNING, UPDATING, COMPLETING, CANCELING, UNKNOWN_ENUM_VALUE ->
          throw new IllegalArgumentException("Task state '" + state + "' does not exist in V1 API");
    };
  }

  private static TaskFromSearch toTaskFromSearch(TaskSearchResponse taskSearchResponse) {
    return new TaskFromSearch(
        taskSearchResponse.getId(),
        taskSearchResponse.getName(),
        taskSearchResponse.getTaskDefinitionId(),
        taskSearchResponse.getProcessName(),
        Optional.ofNullable(taskSearchResponse.getCreationDate())
            .map(creationDate -> OffsetDateTime.parse(creationDate, DATE_TIME_FORMATTER))
            .orElse(null),
        Optional.ofNullable(taskSearchResponse.getCompletionDate())
            .map(completionDate -> OffsetDateTime.parse(completionDate, DATE_TIME_FORMATTER))
            .orElse(null),
        taskSearchResponse.getAssignee(),
        toTaskState(taskSearchResponse.getTaskState()),
        taskSearchResponse.getIsFirst(),
        taskSearchResponse.getFormKey(),
        taskSearchResponse.getFormId(),
        taskSearchResponse.getIsFormEmbedded(),
        taskSearchResponse.getProcessDefinitionKey(),
        taskSearchResponse.getProcessInstanceKey(),
        taskSearchResponse.getTenantId(),
        taskSearchResponse.getDueDate(),
        taskSearchResponse.getFollowUpDate(),
        taskSearchResponse.getCandidateGroups(),
        taskSearchResponse.getCandidateUsers(),
        mapIfPresent(taskSearchResponse.getVariables(), TasklistClientV1::toVariable),
        toImplementation(taskSearchResponse.getImplementation()),
        taskSearchResponse.getPriority());
  }

  private static Implementation toImplementation(
      TaskSearchResponse.ImplementationEnum implementation) {
    if (implementation == null) {
      return null;
    }
    return switch (implementation) {
      case JOB_WORKER -> Implementation.JOB_WORKER;
      case ZEEBE_USER_TASK -> Implementation.ZEEBE_USER_TASK;
    };
  }

  private static TaskState toTaskState(TaskStateEnum taskState) {
    if (taskState == null) {
      return null;
    }
    return switch (taskState) {
      case CREATED -> TaskState.CREATED;
      case FAILED -> TaskState.FAILED;
      case CANCELED -> TaskState.CANCELED;
      case COMPLETED -> TaskState.COMPLETED;
    };
  }

  private static Task toTask(TaskResponse taskSearchResponse) {
    return new Task(
        taskSearchResponse.getId(),
        taskSearchResponse.getName(),
        taskSearchResponse.getTaskDefinitionId(),
        taskSearchResponse.getProcessName(),
        Optional.ofNullable(taskSearchResponse.getCreationDate())
            .map(creationDate -> OffsetDateTime.parse(creationDate, DATE_TIME_FORMATTER))
            .orElse(null),
        Optional.ofNullable(taskSearchResponse.getCompletionDate())
            .map(completionDate -> OffsetDateTime.parse(completionDate, DATE_TIME_FORMATTER))
            .orElse(null),
        taskSearchResponse.getAssignee(),
        toTaskState(taskSearchResponse.getTaskState()),
        taskSearchResponse.getFormKey(),
        taskSearchResponse.getFormId(),
        taskSearchResponse.getIsFormEmbedded(),
        taskSearchResponse.getProcessDefinitionKey(),
        taskSearchResponse.getProcessInstanceKey(),
        taskSearchResponse.getTenantId(),
        taskSearchResponse.getDueDate(),
        taskSearchResponse.getFollowUpDate(),
        taskSearchResponse.getCandidateGroups(),
        taskSearchResponse.getCandidateUsers(),
        toImplementation(taskSearchResponse.getImplementation()),
        taskSearchResponse.getPriority());
  }

  private static Implementation toImplementation(TaskResponse.ImplementationEnum implementation) {
    if (implementation == null) {
      return null;
    }
    return switch (implementation) {
      case JOB_WORKER -> Implementation.JOB_WORKER;
      case ZEEBE_USER_TASK -> Implementation.ZEEBE_USER_TASK;
    };
  }

  private static TaskState toTaskState(TaskResponse.TaskStateEnum taskState) {
    if (taskState == null) {
      return null;
    }
    return switch (taskState) {
      case CREATED -> TaskState.CREATED;
      case FAILED -> TaskState.FAILED;
      case CANCELED -> TaskState.CANCELED;
      case COMPLETED -> TaskState.COMPLETED;
    };
  }

  private static TaskAssignRequest fromTaskAssignment(TaskAssignment taskAssignment) {
    TaskAssignRequest taskAssignmentRequest = new TaskAssignRequest();
    taskAssignmentRequest.setAssignee(taskAssignment.assignee());
    taskAssignmentRequest.setAllowOverrideAssignment(taskAssignment.allowOverrideAssignment());
    return taskAssignmentRequest;
  }

  private static FormV2 toFormV2(Form form) {
    return new FormV2(form.tenantId(), form.id(), form.schema(), form.version(), form.id());
  }

  @Override
  public Form getForm(String formId, String processDefinitionKey, Long version) {
    FormResponse formResponse;
    try {
      formResponse = formApi.getForm(formId, processDefinitionKey, version);
    } catch (ApiException e) {
      throw new RuntimeException("Error while getting form", e);
    }
    return toForm(formResponse);
  }

  @Override
  public FormV2 getFormForTask(String userTaskKey) {
    Task task = getTask(userTaskKey);
    return toFormV2(getForm(task.formId(), task.processDefinitionKey()));
  }

  @Override
  public FormV2 getStartFormForProcess(String processDefinitionKey) {
    throw new CompatibilityException(
        "Getting start form for process definition key is not supported in V1");
  }

  @Override
  public void saveDraftVariables(String taskId, List<RequestVariable> variables) {
    SaveVariablesRequest request = fromVariablesToSave(variables);
    try {
      taskApi.saveDraftTaskVariables(taskId, request);
    } catch (ApiException e) {
      throw new RuntimeException("Error while saving draft variables", e);
    }
  }

  @Override
  public List<VariableFromSearch> searchTaskVariables(
      String taskId, VariableSearch variableSearch) {
    VariablesSearchRequest variableSearchRequest = fromVariableSearch(variableSearch);
    List<VariableSearchResponse> variableSearchResponses;
    try {
      variableSearchResponses = taskApi.searchTaskVariables(taskId, variableSearchRequest);
    } catch (ApiException e) {
      throw new RuntimeException("Error while searching task variables", e);
    }
    return toVariableList(variableSearchResponses);
  }

  @Override
  public List<TaskFromSearch> searchTasks(TaskSearch taskSearch) {
    TaskSearchRequest taskSearchRequest = fromTaskSearch(taskSearch);
    List<TaskSearchResponse> taskSearchResponses;
    try {
      taskSearchResponses = taskApi.searchTasks(taskSearchRequest);
    } catch (ApiException e) {
      throw new RuntimeException("Error while searching tasks", e);
    }
    return taskSearchResponses.stream().map(TasklistClientV1::toTaskFromSearch).toList();
  }

  @Override
  public Optional<Task> unassignTask(String taskId) {
    TaskResponse taskResponse;
    try {
      taskResponse = taskApi.unassignTask(taskId);
    } catch (ApiException e) {
      throw new RuntimeException("Error while unassigning task", e);
    }
    return Optional.of(toTask(taskResponse));
  }

  @Override
  public Optional<Task> completeTask(String taskId, List<RequestVariable> variables) {
    TaskCompleteRequest taskCompleteRequest = fromVariablesToComplete(variables);
    TaskResponse taskResponse;
    try {
      taskResponse = taskApi.completeTask(taskId, taskCompleteRequest);
    } catch (ApiException e) {
      throw new RuntimeException("Error while completing task", e);
    }
    return Optional.of(toTask(taskResponse));
  }

  @Override
  public Optional<Task> assignTask(String taskId, TaskAssignment taskAssignment) {
    TaskAssignRequest taskAssignmentRequest = fromTaskAssignment(taskAssignment);
    TaskResponse taskResponse;
    try {
      taskResponse = taskApi.assignTask(taskId, taskAssignmentRequest);
    } catch (ApiException e) {
      throw new RuntimeException("Error while assigning task", e);
    }
    return Optional.of(toTask(taskResponse));
  }

  @Override
  public Task getTask(String taskId) {
    TaskResponse taskResponse;
    try {
      taskResponse = taskApi.getTaskById(taskId);
    } catch (ApiException e) {
      throw new RuntimeException("Error while getting task", e);
    }
    return toTask(taskResponse);
  }

  @Override
  public Variable getVariable(String variableId) {
    VariableResponse variableResponse;
    try {
      variableResponse = variablesApi.getVariableById(variableId);
    } catch (ApiException e) {
      throw new RuntimeException("Error while getting variable", e);
    }
    return toVariable(variableResponse);
  }
}
