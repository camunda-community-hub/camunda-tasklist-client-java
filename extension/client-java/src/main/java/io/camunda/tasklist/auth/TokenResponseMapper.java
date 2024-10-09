package io.camunda.tasklist.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface TokenResponseMapper {
  TokenResponse readToken(String token);

  public class JacksonTokenResponseMapper implements TokenResponseMapper {
    private final ObjectMapper objectMapper;

    public JacksonTokenResponseMapper(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
    }

    @Override
    public TokenResponse readToken(String token) {
      try {
        return objectMapper.readValue(token, TokenResponse.class);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Error while reading token " + token, e);
      }
    }
  }
}
