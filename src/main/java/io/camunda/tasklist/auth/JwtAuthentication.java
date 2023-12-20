package io.camunda.tasklist.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.exception.TaskListException;
import java.util.Base64;

public abstract class JwtAuthentication implements AuthInterface {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

  public int getExpiration(String token) throws TaskListException {
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

  public void setToken(CamundaTaskListClient client, String token) throws TaskListException {
    client.setBearerToken(token);
    client.setTokenExpiration(getExpiration(token));
  }
}
