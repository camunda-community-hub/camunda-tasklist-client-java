package io.camunda.tasklist.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.camunda.tasklist.dto.Form;
import io.camunda.tasklist.dto.Pagination;
import io.camunda.tasklist.dto.Pagination.TaskOrderBy.Field;
import io.camunda.tasklist.dto.Pagination.TaskOrderBy.Order;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.Task.Implementation;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.dto.TaskSearch.IncludeVariable;
import io.camunda.tasklist.dto.TaskSearch.TaskByVariables;
import io.camunda.tasklist.dto.TaskSearch.TaskByVariables.Operator;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.dto.Variable;
import io.camunda.tasklist.dto.VariableType;
import io.camunda.tasklist.generated.model.DateFilter;
import io.camunda.tasklist.generated.model.FormResponse;
import io.camunda.tasklist.generated.model.TaskByVariables.OperatorEnum;
import io.camunda.tasklist.generated.model.TaskOrderBy;
import io.camunda.tasklist.generated.model.TaskOrderBy.FieldEnum;
import io.camunda.tasklist.generated.model.TaskOrderBy.OrderEnum;
import io.camunda.tasklist.generated.model.TaskResponse;
import io.camunda.tasklist.generated.model.TaskSearchRequest;
import io.camunda.tasklist.generated.model.TaskSearchResponse;
import io.camunda.tasklist.generated.model.VariableInputDTO;
import io.camunda.tasklist.generated.model.VariableResponse;
import io.camunda.tasklist.generated.model.VariableSearchResponse;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConverterUtils {
  public static String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

  private static ObjectMapper objectMapper = null;

  private ConverterUtils() {}

  public static TaskSearchRequest.StateEnum toSearchState(TaskState value) {
    return value == null ? null : TaskSearchRequest.StateEnum.fromValue(value.getRawValue());
  }

  public static List<TaskOrderBy> toSort(List<Pagination.TaskOrderBy> sort) {
    if (sort == null) {
      return null;
    }
    return sort.stream()
        .map(s -> new TaskOrderBy().field(toField(s.field())).order(toOrder(s.order())))
        .toList();
  }

  public static FieldEnum toField(Field field) {
    if (field == null) {
      return null;
    }
    switch (field) {
      case DUE_DATE -> {
        return FieldEnum.DUE_DATE;
      }
      case PRIORITY -> {
        return FieldEnum.PRIORITY;
      }
      case CREATION_TIME -> {
        return FieldEnum.CREATION_TIME;
      }
      case FOLLOW_UP_DATE -> {
        return FieldEnum.FOLLOW_UP_DATE;
      }
      case COMPLETION_TIME -> {
        return FieldEnum.COMPLETION_TIME;
      }
    }
    throw new IllegalArgumentException("Unknown field: " + field);
  }

  public static OrderEnum toOrder(Order order) {
    if (order == null) {
      return null;
    }
    switch (order) {
      case ASC -> {
        return OrderEnum.ASC;
      }
      case DESC -> {
        return OrderEnum.DESC;
      }
    }
    throw new IllegalArgumentException("Unknown order: " + order);
  }

  public static Variable toVariable(VariableResponse var) throws JsonProcessingException {
    if (var == null) {
      return null;
    }
    return buildVariable(var.getId(), var.getName(), var.getValue());
  }

  private static Variable buildVariable(String id, String name, String value) {
    Variable result = new Variable();
    result.setName(name);
    result.setId(id);
    JsonNode nodeValue = toJsonNode(value);
    if (nodeValue.canConvertToLong()) {
      result.setValue(nodeValue.asLong());
      result.setType(VariableType.NUMBER);
      return result;
    }
    if (nodeValue.isBoolean()) {
      result.setValue(nodeValue.asBoolean());
      result.setType(VariableType.BOOLEAN);
      return result;
    }
    if (nodeValue.isTextual()) {
      result.setValue(nodeValue.textValue());
      result.setType(VariableType.STRING);
      return result;
    }
    if (nodeValue.isArray()) {
      result.setValue(getObjectMapper().convertValue(nodeValue, new TypeReference<List<?>>() {}));
      result.setType(VariableType.LIST);
      return result;
    }
    result.setValue(
        getObjectMapper().convertValue(nodeValue, new TypeReference<Map<String, Object>>() {}));
    result.setType(VariableType.MAP);
    return result;
  }

  private static JsonNode toJsonNode(String value) {
    try {
      return getObjectMapper().readTree(value);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error while reading a string value to json", e);
    }
  }

  public static Variable improveVariable(VariableSearchResponse var) {
    return buildVariable(var.getId(), var.getName(), var.getValue());
  }

  public static List<Variable> toVariables(List<VariableSearchResponse> variables) {
    if (variables == null) {
      return null;
    }
    List<Variable> result = new ArrayList<>();
    for (VariableSearchResponse var : variables) {
      result.add(improveVariable(var));
    }
    return result;
  }

  public static Task toTask(TaskSearchResponse task) {
    return toTask(task, null);
  }

  public static Task toTask(TaskSearchResponse sourceTask, List<Variable> variables) {
    if (sourceTask == null) {
      return null;
    }
    Task task = new Task();
    List.of(
            mapper(sourceTask::getId, task::setId),
            mapper(sourceTask::getName, task::setName),
            mapper(sourceTask::getProcessName, task::setProcessName),
            mapper(sourceTask::getProcessDefinitionKey, task::setProcessDefinitionKey),
            mapper(sourceTask::getProcessInstanceKey, task::setProcessInstanceKey),
            mapper(sourceTask::getAssignee, task::setAssignee),
            mapper(
                sourceTask::getCreationDate,
                ConverterUtils::toOffsetDateTime,
                task::setCreationDate),
            mapper(
                sourceTask::getCompletionDate,
                ConverterUtils::toOffsetDateTime,
                task::setCompletionDate),
            mapper(sourceTask::getTaskState, ConverterUtils::toTaskState, task::setTaskState),
            mapper(sourceTask::getCandidateUsers, task::setCandidateUsers),
            mapper(sourceTask::getCandidateGroups, task::setCandidateGroups),
            mapper(sourceTask::getFollowUpDate, task::setFollowUpDate),
            mapper(sourceTask::getDueDate, task::setDueDate),
            mapper(sourceTask::getFormKey, task::setFormKey),
            mapper(sourceTask::getFormId, task::setFormId),
            mapper(sourceTask::getFormVersion, task::setFormVersion),
            mapper(sourceTask::getIsFormEmbedded, task::setFormEmbedded),
            mapper(sourceTask::getTaskDefinitionId, task::setTaskDefinitionId),
            // sortValues,
            // isFirst,
            mapper(sourceTask::getTenantId, task::setTenantId),
            // variables
            mapper(
                sourceTask::getImplementation,
                ConverterUtils::toImplementation,
                task::setImplementation))
        .forEach(Mapper::map);
    if (variables != null) {
      task.setVariables(variables);
    } else if (task.getVariables() != null && !task.getVariables().isEmpty()) {
      List<Variable> improvedList = new ArrayList<>();
      for (Variable v : task.getVariables()) {
        improvedList.add(buildVariable(v.getId(), v.getName(), (String) v.getValue()));
      }
      task.setVariables(improvedList);
    }

    return task;
  }

  private static OffsetDateTime toOffsetDateTime(String dateTime) {
    if (dateTime == null) {
      return null;
    }
    return OffsetDateTime.parse(dateTime, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
  }

  public static Task toTask(TaskResponse sourceTask) {
    return toTask(sourceTask, null);
  }

  public static Task toTask(TaskResponse sourceTask, List<Variable> variables) {
    if (sourceTask == null) {
      return null;
    }
    Task task = new Task();
    List.of(
            mapper(sourceTask::getId, task::setId),
            mapper(sourceTask::getName, task::setName),
            mapper(sourceTask::getProcessName, task::setProcessName),
            mapper(sourceTask::getProcessDefinitionKey, task::setProcessDefinitionKey),
            mapper(sourceTask::getProcessInstanceKey, task::setProcessInstanceKey),
            mapper(sourceTask::getAssignee, task::setAssignee),
            mapper(
                sourceTask::getCreationDate,
                ConverterUtils::toOffsetDateTime,
                task::setCreationDate),
            mapper(
                sourceTask::getCompletionDate,
                ConverterUtils::toOffsetDateTime,
                task::setCompletionDate),
            mapper(sourceTask::getTaskState, ConverterUtils::toTaskState, task::setTaskState),
            mapper(sourceTask::getCandidateUsers, task::setCandidateUsers),
            mapper(sourceTask::getCandidateGroups, task::setCandidateGroups),
            mapper(sourceTask::getFollowUpDate, task::setFollowUpDate),
            mapper(sourceTask::getDueDate, task::setDueDate),
            mapper(sourceTask::getFormKey, task::setFormKey),
            mapper(sourceTask::getFormId, task::setFormId),
            mapper(sourceTask::getFormVersion, task::setFormVersion),
            mapper(sourceTask::getIsFormEmbedded, task::setFormEmbedded),
            mapper(sourceTask::getTaskDefinitionId, task::setTaskDefinitionId),
            // sortValues,
            // isFirst,
            mapper(sourceTask::getTenantId, task::setTenantId),
            // variables
            mapper(
                sourceTask::getImplementation,
                ConverterUtils::toImplementation,
                task::setImplementation))
        .forEach(Mapper::map);
    if (variables != null) {
      task.setVariables(variables);
    } else if (task.getVariables() != null && !task.getVariables().isEmpty()) {
      List<Variable> improvedList = new ArrayList<>();
      for (Variable v : task.getVariables()) {
        improvedList.add(buildVariable(v.getId(), v.getName(), (String) v.getValue()));
      }
      task.setVariables(improvedList);
    }

    return task;
  }

  public static Implementation toImplementation(TaskResponse.ImplementationEnum implementation) {
    if (implementation == null) {
      return null;
    }
    switch (implementation) {
      case JOB_WORKER -> {
        return Implementation.JOB_WORKER;
      }
      case ZEEBE_USER_TASK -> {
        return Implementation.ZEEBE_USER_TASK;
      }
    }
    throw new IllegalArgumentException("Unknown implementation: " + implementation);
  }

  public static Implementation toImplementation(
      TaskSearchResponse.ImplementationEnum implementation) {
    if (implementation == null) {
      return null;
    }
    switch (implementation) {
      case JOB_WORKER -> {
        return Implementation.JOB_WORKER;
      }
      case ZEEBE_USER_TASK -> {
        return Implementation.ZEEBE_USER_TASK;
      }
    }
    throw new IllegalArgumentException("Unknown implementation: " + implementation);
  }

  public static TaskState toTaskState(TaskSearchResponse.TaskStateEnum taskState) {
    if (taskState == null) {
      return null;
    }
    switch (taskState) {
      case CREATED -> {
        return TaskState.CREATED;
      }
      case COMPLETED -> {
        return TaskState.COMPLETED;
      }
      case FAILED -> {
        return TaskState.FAILED;
      }
      case CANCELED -> {
        return TaskState.CANCELED;
      }
    }
    throw new IllegalArgumentException("Unknown task state: " + taskState);
  }

  public static TaskState toTaskState(TaskResponse.TaskStateEnum taskState) {
    if (taskState == null) {
      return null;
    }
    switch (taskState) {
      case CREATED -> {
        return TaskState.CREATED;
      }
      case COMPLETED -> {
        return TaskState.COMPLETED;
      }
      case FAILED -> {
        return TaskState.FAILED;
      }
      case CANCELED -> {
        return TaskState.CANCELED;
      }
    }
    throw new IllegalArgumentException("Unknown task state: " + taskState);
  }

  public static List<Task> toTasks(List<TaskSearchResponse> tasks) {
    if (tasks == null) {
      return null;
    }
    List<Task> result = new ArrayList<>();
    for (TaskSearchResponse task : tasks) {
      result.add(toTask(task));
    }
    return result;
  }

  public static List<VariableInputDTO> toVariableInput(Map<String, Object> variablesMap) {
    if (variablesMap == null) {
      return null;
    }
    try {
      List<VariableInputDTO> variables = new ArrayList<>();
      for (Map.Entry<String, Object> entry : variablesMap.entrySet()) {
        if (entry.getValue() != null) {
          variables.add(
              new VariableInputDTO()
                  .name(entry.getKey())
                  .value(getObjectMapper().writeValueAsString(entry.getValue())));
        }
      }
      return variables;
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error while creating variable input", e);
    }
  }

  public static Form toForm(FormResponse formResponse) {
    if (formResponse == null) {
      return null;
    }
    Form form = new Form();
    List.of(
            mapper(formResponse::getId, form::setId),
            mapper(formResponse::getProcessDefinitionKey, form::setProcessDefinitionId),
            mapper(formResponse::getSchema, form::setSchema),
            mapper(formResponse::getVersion, form::setVersion),
            mapper(formResponse::getTenantId, form::setTenantId),
            mapper(formResponse::getIsDeleted, form::setIsDeleted))
        .forEach(Mapper::map);
    return form;
  }

  public static TaskSearchRequest toTaskSearchRequest(TaskSearch taskSearch) {
    if (taskSearch == null) {
      return null;
    }
    TaskSearchRequest taskSearchRequest = new TaskSearchRequest();
    List.of(
            mapper(taskSearch::getCandidateGroup, taskSearchRequest::setCandidateGroup),
            mapper(taskSearch::getCandidateGroups, taskSearchRequest::setCandidateGroups),
            mapper(taskSearch::getAssignee, taskSearchRequest::setAssignee),
            mapper(taskSearch::getCandidateUser, taskSearchRequest::setCandidateUser),
            mapper(taskSearch::getCandidateUsers, taskSearchRequest::setCandidateUsers),
            mapper(taskSearch::getAssigned, taskSearchRequest::setAssigned),
            mapper(taskSearch::getState, ConverterUtils::toState, taskSearchRequest::setState),
            mapper(taskSearch::getProcessDefinitionKey, taskSearchRequest::setProcessDefinitionKey),
            mapper(taskSearch::getProcessInstanceKey, taskSearchRequest::setProcessInstanceKey),
            mapper(taskSearch::getTaskDefinitionId, taskSearchRequest::setTaskDefinitionId),
            mapper(
                taskSearch::getTaskVariables,
                ConverterUtils::toTaskVariables,
                taskSearchRequest::setTaskVariables),
            mapper(taskSearch::getTenantIds, taskSearchRequest::setTenantIds),
            // withVariables,
            mapper(
                taskSearch::getFollowUpDate,
                ConverterUtils::toDateFilter,
                taskSearchRequest::setFollowUpDate),
            mapper(
                taskSearch::getDueDate,
                ConverterUtils::toDateFilter,
                taskSearchRequest::setDueDate),
            mapper(
                taskSearch::getIncludeVariables,
                ConverterUtils::toIncludeVariables,
                taskSearchRequest::setIncludeVariables)
            // pagination
            )
        .forEach(Mapper::map);
    return taskSearchRequest;
  }

  public static TaskSearchRequest.StateEnum toState(TaskState taskState) {
    if (taskState == null) {
      return null;
    }
    switch (taskState) {
      case CREATED -> {
        return TaskSearchRequest.StateEnum.CREATED;
      }
      case COMPLETED -> {
        return TaskSearchRequest.StateEnum.COMPLETED;
      }
      case FAILED -> {
        return TaskSearchRequest.StateEnum.FAILED;
      }
      case CANCELED -> {
        return TaskSearchRequest.StateEnum.CANCELED;
      }
    }
    throw new IllegalArgumentException("Unknown task state: " + taskState);
  }

  public static List<io.camunda.tasklist.generated.model.TaskByVariables> toTaskVariables(
      List<TaskByVariables> taskVariables) {
    if (taskVariables == null) {
      return null;
    }
    return taskVariables.stream().map(ConverterUtils::toTaskVariable).toList();
  }

  public static List<io.camunda.tasklist.generated.model.IncludeVariable> toIncludeVariables(
      List<IncludeVariable> includeVariables) {
    if (includeVariables == null) {
      return null;
    }
    return includeVariables.stream().map(ConverterUtils::toIncludeVariable).toList();
  }

  public static io.camunda.tasklist.generated.model.IncludeVariable toIncludeVariable(
      IncludeVariable includeVariables) {
    if (includeVariables == null) {
      return null;
    }
    io.camunda.tasklist.generated.model.IncludeVariable result =
        new io.camunda.tasklist.generated.model.IncludeVariable();
    List.of(
            mapper(includeVariables::name, result::setName),
            mapper(includeVariables::alwaysReturnFullValue, result::setAlwaysReturnFullValue))
        .forEach(Mapper::map);
    return result;
  }

  public static io.camunda.tasklist.generated.model.TaskByVariables toTaskVariable(
      TaskByVariables taskVariable) {
    if (taskVariable == null) {
      return null;
    }
    io.camunda.tasklist.generated.model.TaskByVariables request =
        new io.camunda.tasklist.generated.model.TaskByVariables();
    List.of(
            mapper(taskVariable::name, request::setName),
            mapper(taskVariable::operator, ConverterUtils::toOperator, request::setOperator),
            mapper(taskVariable::value, ConverterUtils::toValue, request::setValue))
        .forEach(Mapper::map);
    return request;
  }

  public static OperatorEnum toOperator(Operator operator) {
    if (operator == null) {
      return null;
    }
    switch (operator) {
      case EQ -> {
        return OperatorEnum.EQ;
      }
    }
    throw new IllegalArgumentException("Unknown operator: " + operator);
  }

  public static String toValue(Object value) {
    try {
      return getObjectMapper().writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error while transforming a value to a json string", e);
    }
  }

  public static DateFilter toDateFilter(io.camunda.tasklist.dto.DateFilter dateFilter) {
    if (dateFilter == null) {
      return null;
    }
    DateFilter result = new DateFilter();
    List.of(mapper(dateFilter::getFrom, result::setFrom), mapper(dateFilter::getTo, result::setTo))
        .forEach(Mapper::map);
    return result;
  }

  private static <T> Mapper<T, T> mapper(Supplier<T> getter, Consumer<T> setter) {
    return new Mapper<>(getter, o -> o, setter);
  }

  private static <T, S> Mapper<S, T> mapper(
      Supplier<S> getter, Function<S, T> mapper, Consumer<T> setter) {
    return new Mapper<>(getter, mapper, setter);
  }

  private static ObjectMapper getObjectMapper() {
    if (objectMapper == null) {
      objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    return objectMapper;
  }

  private record Mapper<S, T>(Supplier<S> getter, Function<S, T> mapper, Consumer<T> setter) {

    public void map() {
      S s = getter.get();
      T t = mapper.apply(s);
      setter.accept(t);
    }
  }
}
