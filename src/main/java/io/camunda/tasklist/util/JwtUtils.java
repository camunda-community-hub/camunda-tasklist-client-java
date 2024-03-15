package io.camunda.tasklist.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.tasklist.exception.TaskListException;
import java.util.Base64;

public class JwtUtils {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

  private JwtUtils() {}

  public static int getExpiration(String token) throws TaskListException {
    try {
      String[] chunks = token.split("\\.");
      String payload = new String(DECODER.decode(chunks[1]));
      JsonNode jsonPayload = MAPPER.readValue(payload, JsonNode.class);
      JsonNode exp = jsonPayload.get("exp");
      if (exp == null) {
        return 0;
      } else {
        return exp.asInt();
      }
    } catch (JsonProcessingException e) {
      throw new TaskListException("Token is not readable", e);
    }
  }
}
