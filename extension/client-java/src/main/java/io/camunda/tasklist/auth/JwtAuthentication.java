package io.camunda.tasklist.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.Asserts;

public class JwtAuthentication implements Authentication {
  private final JwtCredential jwtCredential;
  private final TokenResponseHttpClientResponseHandler responseHandler;
  private String token;
  private LocalDateTime timeout;

  public JwtAuthentication(
      JwtCredential jwtCredential, TokenResponseHttpClientResponseHandler responseHandler) {
    this.jwtCredential = jwtCredential;
    this.responseHandler = responseHandler;
  }

  public JwtAuthentication(JwtCredential jwtCredential) {
    this(jwtCredential, new TokenResponseHttpClientResponseHandler(new ObjectMapper()));
  }

  @Override
  public Map<String, String> getTokenHeader() {
    if (token == null || timeout == null || timeout.isBefore(LocalDateTime.now())) {
      TokenResponse response = retrieveToken();
      token = response.getAccessToken();
      timeout = LocalDateTime.now().plusSeconds(response.getExpiresIn()).minusSeconds(30);
    }
    return Map.of("Authorization", "Bearer " + token);
  }

  @Override
  public void resetToken() {
    this.token = null;
    this.timeout = null;
  }

  private TokenResponse retrieveToken() {
    try (CloseableHttpClient client = HttpClients.createSystem()) {
      HttpPost request = buildRequest();
      TokenResponse tokenResponse = client.execute(request, responseHandler);
      Asserts.notNull(tokenResponse.getAccessToken(), "access_token is null");
      Asserts.notNull(tokenResponse.getExpiresIn(), "expires_in is null");
      return tokenResponse;
    } catch (Exception e) {
      throw new RuntimeException("Failed to retrieve token for Tasklist authentication", e);
    }
  }

  private HttpPost buildRequest() throws URISyntaxException {
    HttpPost httpPost = new HttpPost(jwtCredential.authUrl().toURI());
    httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
    List<NameValuePair> formParams = new ArrayList<>();
    formParams.add(new BasicNameValuePair("grant_type", "client_credentials"));
    formParams.add(new BasicNameValuePair("client_id", jwtCredential.clientId()));
    formParams.add(new BasicNameValuePair("client_secret", jwtCredential.clientSecret()));
    formParams.add(new BasicNameValuePair("audience", jwtCredential.audience()));
    if (jwtCredential.scope() != null && !jwtCredential.scope().isEmpty()) {
      formParams.add(new BasicNameValuePair("scope", jwtCredential.scope()));
    }
    httpPost.setEntity(new UrlEncodedFormEntity(formParams));
    return httpPost;
  }
}
