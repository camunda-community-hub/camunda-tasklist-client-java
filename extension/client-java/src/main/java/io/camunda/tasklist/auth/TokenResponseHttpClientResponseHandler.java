package io.camunda.tasklist.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

public class TokenResponseHttpClientResponseHandler
    implements HttpClientResponseHandler<TokenResponse> {
  private final ObjectMapper objectMapper;
  private final ErrorCodeHandler errorCodeHandler;

  public TokenResponseHttpClientResponseHandler(
      ObjectMapper objectMapper, ErrorCodeHandler errorCodeHandler) {
    this.objectMapper = objectMapper;
    this.errorCodeHandler = errorCodeHandler;
  }

  public TokenResponseHttpClientResponseHandler(ObjectMapper objectMapper) {
    this(objectMapper, new DefaultErrorCodeHandler());
  }

  @Override
  public TokenResponse handleResponse(ClassicHttpResponse response) throws IOException {
    TokenResponse resp;
    if (200 <= response.getCode() && response.getCode() <= 299) {
      HttpEntity entity = response.getEntity();
      String tmp = new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8);
      resp = objectMapper.readValue(tmp, TokenResponse.class);
      EntityUtils.consume(entity);
      return resp;
    } else {
      throw errorCodeHandler.handleError(response);
    }
  }
}
