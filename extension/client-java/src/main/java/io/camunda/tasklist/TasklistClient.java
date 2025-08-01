package io.camunda.tasklist;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TasklistClient {
  // Form
  @Deprecated(since = "8.8")
  Form getForm(String formId, String processDefinitionKey, Long version);

  @Deprecated(since = "8.8")
  default Form getForm(String formId, String processDefinitionKey) {
    return getForm(formId, processDefinitionKey, null);
  }

  FormV2 getFormForTask(String userTaskKey);

  FormV2 getStartFormForProcess(String processDefinitionKey);

  // Task

  void saveDraftVariables(String taskId, List<RequestVariable> variables);

  List<VariableFromSearch> searchTaskVariables(String taskId, VariableSearch variableSearch);

  List<TaskFromSearch> searchTasks(TaskSearch taskSearch);

  Optional<Task> unassignTask(String taskId);

  Optional<Task> completeTask(String taskId, List<RequestVariable> variables);

  Optional<Task> assignTask(String taskId, TaskAssignment taskAssignment);

  Task getTask(String taskId);

  // Variables

  Variable getVariable(String variableId);

  enum Implementation {
    JOB_WORKER,
    ZEEBE_USER_TASK
  }

  enum TaskState {
    CREATED,
    COMPLETED,
    CANCELED,
    FAILED,
    ASSIGNING,
    UPDATING,
    COMPLETING,
    CANCELING,
    UNKNOWN_ENUM_VALUE
  }

  // types

  record Form(
      String id,
      String processDefinitionKey,
      String title,
      String schema,
      Long version,
      String tenantId,
      Boolean isDeleted) {}

  record FormV2(String tenantId, String formId, Object schema, Long version, String formKey) {}

  record RequestVariable(String name, String value) {}

  record VariableFromSearch(
      String id,
      String name,
      String value,
      Boolean isValueTruncated,
      String previewValue,
      VariableDraftFromSearch draft) {
    public record VariableDraftFromSearch(
        String value, Boolean isValueTruncated, String previewValue) {}
  }

  record Variable(String id, String name, String value, VariableDraft draft, String tenantId) {
    public record VariableDraft(String value) {}
  }

  record VariableSearch(List<String> variableNames, List<IncludeVariable> includeVariables) {}

  record IncludeVariable(String name, Boolean alwaysReturnFullValue) {}

  record TaskFromSearch(
      String id,
      String name,
      String taskDefinitionId,
      String processName,
      String creationDate,
      String completionDate,
      String assignee,
      TaskState taskState,
      Boolean isFirst,
      String formKey,
      String formId,
      Boolean isFormEmbedded,
      String processDefinitionKey,
      String processInstanceKey,
      String tenantId,
      OffsetDateTime dueDate,
      OffsetDateTime followUpDate,
      List<String> candidateGroups,
      List<String> candidateUsers,
      List<VariableFromSearch> variables,
      Implementation implementation,
      Integer priority) {}

  record Task(
      String id,
      String name,
      String taskDefinitionId,
      String processName,
      String creationDate,
      String completionDate,
      String assignee,
      TaskState taskState,
      String formKey,
      String formId,
      Boolean isFormEmbedded,
      String processDefinitionKey,
      String processInstanceKey,
      String tenantId,
      OffsetDateTime dueDate,
      OffsetDateTime followUpDate,
      List<String> candidateGroups,
      List<String> candidateUsers,
      Implementation implementation,
      Integer priority) {}

  record TaskSearch(
      TaskState state,
      Boolean assigned,
      String assignee,
      List<String> assignees,
      String taskDefinitionId,
      String candidateGroup,
      List<String> candidateGroups,
      String candidateUser,
      List<String> candidateUsers,
      String processDefinitionKey,
      String processInstanceKey,
      Integer pageSize,
      DateRange followUpDate,
      DateRange dueDate,
      List<TaskVariable> taskVariables,
      List<String> tenantIds,
      List<Sort> sort,
      List<String> searchAfter,
      List<String> searchAfterOrEqual,
      List<String> searchBefore,
      List<String> searchBeforeOrEqual,
      String after,
      String before,
      List<IncludeVariable> includeVariables,
      Implementation implementation,
      Priority priority) {

    public record DateRange(OffsetDateTime from, OffsetDateTime to) {}

    public record TaskVariable(String name, String value, Operator operator) {
      public enum Operator {
        eq
      }
    }

    public record Sort(Field field, Order order) {
      public enum Field {
        completionTime,
        creationTime,
        followUpDate,
        dueDate,
        priority,
        name
      }

      public enum Order {
        ASC,
        DESC
      }
    }

    public record Priority(Integer eq, Integer gte, Integer gt, Integer lt, Integer lte) {}
  }

  record TaskAssignment(String assignee, Boolean allowOverrideAssignment) {}
}
