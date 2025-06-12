package io.camunda.tasklist.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.camunda.tasklist.TasklistClient;
import io.camunda.tasklist.TasklistClient.RequestVariable;
import io.camunda.tasklist.TasklistClient.TaskFromSearch;
import io.camunda.tasklist.TasklistClient.VariableFromSearch;
import io.camunda.tasklist.dto.Form;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.dto.Variable;
import io.camunda.tasklist.dto.VariableType;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.tasklist.generated.model.TaskSearchRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ConverterUtils {

  private static ObjectMapper objectMapper = null;

  private ConverterUtils() {}

  public static Variable toVariable(TasklistClient.Variable var) throws JsonProcessingException {
    return buildVariable(var.id(), var.name(), var.value());
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

  public static Variable improveVariable(VariableFromSearch var) throws JsonProcessingException {
    return buildVariable(var.id(), var.name(), var.value());
  }

  public static List<Variable> toVariables(List<VariableFromSearch> variables)
      throws TaskListException {
    try {
      List<Variable> result = null;

      if (variables != null) {
        result = new ArrayList<>();

        for (VariableFromSearch var : variables) {
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

  public static List<Task> toTasks(List<TaskFromSearch> tasks) throws TaskListException {
    List<Task> result = new ArrayList<>();
    for (TaskFromSearch task : tasks) {
      result.add(toTask(task, null));
    }
    return result;
  }

  public static List<RequestVariable> toVariableInput(Map<String, Object> variablesMap)
      throws TaskListException {
    try {
      List<RequestVariable> variables = new ArrayList<>();
      for (Map.Entry<String, Object> entry : variablesMap.entrySet()) {
        if (entry.getValue() != null) {
          variables.add(
              new RequestVariable(
                  entry.getKey(), getObjectMapper().writeValueAsString(entry.getValue())));
        }
      }
      return variables;
    } catch (JsonProcessingException e) {
      throw new TaskListException(e);
    }
  }

  public static Form toForm(TasklistClient.Form form) throws TaskListException {
    try {
      return getObjectMapper().readValue(getObjectMapper().writeValueAsString(form), Form.class);
    } catch (JsonProcessingException e) {
      throw new TaskListException(e);
    }
  }

  public static TaskSearchRequest toTaskSearchRequest(TaskSearch taskSearch)
      throws TaskListException {
    try {
      return getObjectMapper()
          .readValue(getObjectMapper().writeValueAsString(taskSearch), TaskSearchRequest.class);
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

  public static <S, T> List<T> mapIfPresent(List<S> list, Function<S, T> mapper) {
    if (list == null) {
      return null;
    }
    return list.stream().map(mapper).toList();
  }
}
