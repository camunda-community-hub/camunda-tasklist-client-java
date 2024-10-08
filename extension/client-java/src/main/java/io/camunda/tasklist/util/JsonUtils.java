package io.camunda.tasklist.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.tasklist.exception.TaskListException;
import java.io.IOException;

public class JsonUtils {

  private JsonUtils() {}

  private static ObjectMapper mapper;

  public static JsonNode toJsonNode(String json) throws IOException {
    if (mapper == null) {
      mapper = new ObjectMapper();
    }
    return mapper.readTree(json);
  }

  public static String toJsonString(Object object) throws TaskListException {
    if (mapper == null) {
      mapper = new ObjectMapper();
    }
    try {
      return mapper.writeValueAsString(object);
    } catch (IOException e) {
      throw new TaskListException(e);
    }
  }
}
