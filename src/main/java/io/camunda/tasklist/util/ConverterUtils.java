package io.camunda.tasklist.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.camunda.tasklist.dto.DateFilter;
import io.camunda.tasklist.dto.Form;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.dto.Variable;
import io.camunda.tasklist.dto.VariableType;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.tasklist.generated.model.FormResponse;
import io.camunda.tasklist.generated.model.TaskSearchRequest;
import io.camunda.tasklist.generated.model.TaskSearchResponse;
import io.camunda.tasklist.generated.model.VariableInputDTO;
import io.camunda.tasklist.generated.model.VariableResponse;
import io.camunda.tasklist.generated.model.VariableSearchResponse;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConverterUtils {

  private static ObjectMapper objectMapper = null;

  private ConverterUtils() {}

  public static io.camunda.tasklist.generated.model.DateFilter toSearchDateFilter(
      DateFilter value) {
    if (value == null) return null;
    return new io.camunda.tasklist.generated.model.DateFilter()
        .from(value.getFrom() == null ? null : value.getFrom().atOffset(ZoneOffset.UTC))
        .to(value.getTo() == null ? null : value.getTo().atOffset(ZoneOffset.UTC));
  }

  public static TaskSearchRequest.StateEnum toSearchState(TaskState value) {
    return value == null ? null : TaskSearchRequest.StateEnum.fromValue(value.getRawValue());
  }

  public static Variable toVariable(VariableResponse var) throws JsonProcessingException {
    return buildVariable(var.getId(), var.getName(), var.getValue());
  }

  private static Variable buildVariable(String id, String name, String value)
      throws JsonProcessingException {
    Variable result = new Variable();
    result.setName(name);
    result.setId(id);
    JsonNode nodeValue = getObjectMapper().readTree(value);
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

  public static Variable improveVariable(VariableSearchResponse var)
      throws JsonProcessingException {
    return buildVariable(var.getId(), var.getName(), var.getValue());
  }

  public static List<Variable> toVariables(List<VariableSearchResponse> variables)
      throws TaskListException {
    try {
      List<Variable> result = null;

      if (variables != null) {
        result = new ArrayList<>();

        for (VariableSearchResponse var : variables) {
          result.add(improveVariable(var));
        }
      }
      return result;
    } catch (JsonProcessingException e) {
      throw new TaskListException(e);
    }
  }

  public static Task toTask(Object sourceTask, List<Variable> variables) throws TaskListException {
    try {
      Task task =
          getObjectMapper().readValue(getObjectMapper().writeValueAsString(sourceTask), Task.class);
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
    } catch (JsonProcessingException e) {
      throw new TaskListException(e);
    }
  }

  public static List<Task> toTasks(List<TaskSearchResponse> tasks) throws TaskListException {
    List<Task> result = new ArrayList<>();
    for (TaskSearchResponse task : tasks) {
      result.add(toTask(task, null));
    }
    return result;
  }

  public static List<VariableInputDTO> toVariableInput(Map<String, Object> variablesMap)
      throws TaskListException {
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
      throw new TaskListException(e);
    }
  }

  public static Form toForm(FormResponse apolloTask) throws TaskListException {
    try {
      return getObjectMapper()
          .readValue(getObjectMapper().writeValueAsString(apolloTask), Form.class);
    } catch (JsonProcessingException e) {
      throw new TaskListException(e);
    }
  }

  private static ObjectMapper getObjectMapper() {
    if (objectMapper == null) {
      objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    return objectMapper;
  }
}
