package io.camunda.tasklist.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.apollographql.apollo3.api.Optional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.tasklist.dto.Form;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.dto.VariableType;
import io.camunda.tasklist.exception.TaskListException;
import io.generated.tasklist.client.type.VariableInput;

public class ApolloUtils {

  private static ObjectMapper objectMapper = null;

  private ApolloUtils() {
  }

  public static Optional<String> optional(String value) {
    return value == null ? null : new Optional.Present<String>(value);
  }

  public static Optional<List<String>> optional(List<String> value) {
    return value == null ? null : new Optional.Present<List<String>>(value);
  }

  public static Optional<Boolean> optional(Boolean value) {
    return value == null ? null : new Optional.Present<Boolean>(value);
  }

  public static Optional<Integer> optional(Integer value) {
    return value == null ? null : new Optional.Present<Integer>(value);
  }

  public static Optional<io.generated.tasklist.client.type.TaskState> optional(TaskState value) {
    return value == null ? null
        : new Optional.Present<io.generated.tasklist.client.type.TaskState>(io.generated.tasklist.client.type.TaskState.safeValueOf(value.getRawValue()));
  }

  public static io.camunda.tasklist.dto.Variable improveVariable(io.camunda.tasklist.dto.Variable var) throws JsonMappingException, JsonProcessingException {
    String value = (String) var.getValue();
    JsonNode nodeValue = getObjectMapper().readTree(value);
    if (nodeValue.canConvertToLong()) {
      var.setValue(nodeValue.asLong());
      var.setType(VariableType.NUMBER);
      return var;
    }
    if (nodeValue.isBoolean()) {
      var.setValue(nodeValue.asBoolean());
      var.setType(VariableType.BOOLEAN);
      return var;
    }
    if (nodeValue.isTextual()) {
      var.setValue(nodeValue.textValue());
      var.setType(VariableType.STRING);
      return var;
    }
    if (nodeValue.isArray()) {
      var.setValue(getObjectMapper().convertValue(nodeValue, new TypeReference<List<?>>() {
      }));
      var.setType(VariableType.LIST);
      return var;
    }
    var.setValue(getObjectMapper().convertValue(nodeValue, new TypeReference<Map<String, Object>>() {
    }));
    var.setType(VariableType.MAP);
    return var;

  }

  public static Task toTask(Object apolloTask) throws TaskListException {
    try {
      Task task = getObjectMapper().readValue(getObjectMapper().writeValueAsString(apolloTask), Task.class);

      if (task.getVariables() != null) {
        for (io.camunda.tasklist.dto.Variable var : task.getVariables()) {
          improveVariable(var);
        }
      }
      return task;
    } catch (JsonProcessingException e) {
      throw new TaskListException(e);
    }
  }

  public static List<Task> toTasks(List<?> apolloTasks) throws TaskListException {
    List<Task> result = new ArrayList<>();
    for (Object apolloTask : apolloTasks) {
      result.add(toTask(apolloTask));
    }
    return result;
  }

  public static List<VariableInput> toVariableInput(Map<String, Object> variablesMap) throws TaskListException {
    try {
      List<VariableInput> variables = new ArrayList<>();
      for (Map.Entry<String, Object> entry : variablesMap.entrySet()) {
        if (entry.getValue() != null) {
          variables.add(new VariableInput(entry.getKey(), getObjectMapper().writeValueAsString(entry.getValue())));
        }
      }
      return variables;
    } catch (JsonProcessingException e) {
      throw new TaskListException(e);
    }
  }

  public static Form toForm(Object apolloTask) throws TaskListException {
    try {
      return getObjectMapper().readValue(getObjectMapper().writeValueAsString(apolloTask), Form.class);
    } catch (JsonProcessingException e) {
      throw new TaskListException(e);
    }
  }

  private static ObjectMapper getObjectMapper() {
    if (objectMapper == null) {
      objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    return objectMapper;
  }
}
