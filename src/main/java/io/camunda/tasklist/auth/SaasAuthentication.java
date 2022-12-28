package io.camunda.tasklist.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.tasklist.util.JsonUtils;

public class SaasAuthentication extends JwtAuthentication {

  private String clientId;
  private String clientSecret;
  private String baseUrl;
  private String authUrl;

  public SaasAuthentication() {
  }

  public SaasAuthentication(String clientId, String clientSecret) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.authUrl = "https://login.cloud.camunda.io/oauth/token";
    this.baseUrl = "tasklist.camunda.io";
  }

  public SaasAuthentication(String authUrl, String baseUrl, String clientId, String clientSecret) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.baseUrl = baseUrl;
    this.authUrl = authUrl;
  }

  public SaasAuthentication clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  public SaasAuthentication clientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
    return this;
  }

  public SaasAuthentication authUrl(String authUrl) {
    this.authUrl = authUrl;
    return this;
  }

  public SaasAuthentication baseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  @Override
  public void authenticate(CamundaTaskListClient client) throws TaskListException {
    try {
      URL url = new URL(authUrl);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setUseCaches(false);
      conn.setConnectTimeout(1000 * 5);
      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("charset", "utf-8");
      String data = "{\"grant_type\":\"client_credentials\", \"audience\":\"" + baseUrl + "\", \"client_id\": \"" + clientId + "\", \"client_secret\":\""
          + clientSecret + "\"}";
      conn.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
      conn.connect();

      if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
          StringBuilder response = new StringBuilder();
          String responseLine = null;
          while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
          }
          JsonNode responseBody = JsonUtils.toJsonNode(response.toString());
          String token = responseBody.get("access_token").asText();
          setToken(client, token);
        }
      } else {
        throw new TaskListException("Error " + conn.getResponseCode() + " obtaining access token : " + conn.getResponseMessage());
      }
    } catch (IOException e) {
      throw new TaskListException(e);
    }
  }
}
