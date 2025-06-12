package io.camunda.tasklist.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.client.CamundaClient;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.CamundaTasklistClientConfiguration;
import io.camunda.tasklist.CamundaTasklistClientConfiguration.ApiVersion;
import io.camunda.tasklist.CamundaTasklistClientConfiguration.DefaultProperties;
import io.camunda.tasklist.auth.JwtAuthentication;
import io.camunda.tasklist.auth.JwtCredential;
import io.camunda.tasklist.auth.SimpleAuthentication;
import io.camunda.tasklist.auth.SimpleCredential;
import io.camunda.tasklist.auth.TokenResponseHttpClientResponseHandler;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

public interface TasklistClientBootstrapper {
  static CamundaClient camundaClient() {
    return CamundaClient.newClientBuilder().build();
  }

  CamundaTaskListClient createTasklistClient() throws MalformedURLException;

  class SimpleAuthTasklistClientBootstrapper implements TasklistClientBootstrapper {

    @Override
    public CamundaTaskListClient createTasklistClient() throws MalformedURLException {
      // properties you need to provide
      ApiVersion apiVersion = ApiVersion.v1;
      String username = "demo";
      String password = "demo";
      URL tasklistUrl = URI.create("http://localhost:8082").toURL();
      boolean returnVariables = false;
      boolean loadTruncatedVariables = false;
      boolean useZeebeUserTasks = true;
      // if you are using zeebe user tasks, you require a zeebe client as well
      CamundaClient camundaClient = camundaClient();
      // bootstrapping
      SimpleCredential credentials =
          new SimpleCredential(username, password, tasklistUrl, Duration.ofMinutes(10));
      SimpleAuthentication authentication = new SimpleAuthentication(credentials);
      CamundaTasklistClientConfiguration configuration =
          new CamundaTasklistClientConfiguration(
              apiVersion,
              authentication,
              tasklistUrl,
              camundaClient,
              new DefaultProperties(returnVariables, loadTruncatedVariables, useZeebeUserTasks));
      CamundaTaskListClient client = new CamundaTaskListClient(configuration);
      return client;
    }
  }

  class IdentityAuthTasklistClientBootstrapper implements TasklistClientBootstrapper {
    @Override
    public CamundaTaskListClient createTasklistClient() throws MalformedURLException {
      // properties you need to provide
      ApiVersion apiVersion = ApiVersion.v1;
      String clientId = "";
      String clientSecret = "";
      String audience = "tasklist-api";
      String scope = ""; // can be omitted if not required
      URL tasklistUrl = URI.create("http://localhost:8082").toURL();
      URL authUrl =
          URI.create(
                  "http://localhost:18080/auth/realms/camunda-platform/protocol/openid-connect/token")
              .toURL();
      boolean returnVariables = false;
      boolean loadTruncatedVariables = false;
      boolean useZeebeUserTasks = true;
      // if you are using zeebe user tasks, you require a zeebe client as well
      CamundaClient camundaClient = camundaClient();
      // bootstrapping
      JwtCredential credentials =
          new JwtCredential(clientId, clientSecret, audience, authUrl, scope);
      ObjectMapper objectMapper = new ObjectMapper();
      TokenResponseHttpClientResponseHandler clientResponseHandler =
          new TokenResponseHttpClientResponseHandler(objectMapper);
      JwtAuthentication authentication = new JwtAuthentication(credentials, clientResponseHandler);
      CamundaTasklistClientConfiguration configuration =
          new CamundaTasklistClientConfiguration(
              apiVersion,
              authentication,
              tasklistUrl,
              camundaClient,
              new DefaultProperties(returnVariables, loadTruncatedVariables, useZeebeUserTasks));
      CamundaTaskListClient client = new CamundaTaskListClient(configuration);
      return client;
    }
  }

  class SaasTasklistClientBootstrapper implements TasklistClientBootstrapper {
    @Override
    public CamundaTaskListClient createTasklistClient() throws MalformedURLException {
      // properties you need to provide
      ApiVersion apiVersion = ApiVersion.v1;
      String region = "";
      String clusterId = "";
      String clientId = "";
      String clientSecret = "";
      boolean returnVariables = false;
      boolean loadTruncatedVariables = false;
      boolean useZeebeUserTasks = true;
      // if you are using zeebe user tasks, you require a zeebe client as well
      CamundaClient camundaClient = camundaClient();
      // bootstrapping
      URL tasklistUrl =
          URI.create("https://" + region + ".tasklist.camunda.io/" + clusterId).toURL();
      URL authUrl = URI.create("https://login.cloud.camunda.io/oauth/token").toURL();
      JwtCredential credentials =
          new JwtCredential(clientId, clientSecret, "tasklist.camunda.io", authUrl, null);
      ObjectMapper objectMapper = new ObjectMapper();
      TokenResponseHttpClientResponseHandler clientResponseHandler =
          new TokenResponseHttpClientResponseHandler(objectMapper);
      JwtAuthentication authentication = new JwtAuthentication(credentials, clientResponseHandler);
      CamundaTasklistClientConfiguration configuration =
          new CamundaTasklistClientConfiguration(
              apiVersion,
              authentication,
              tasklistUrl,
              camundaClient,
              new DefaultProperties(returnVariables, loadTruncatedVariables, useZeebeUserTasks));
      CamundaTaskListClient client = new CamundaTaskListClient(configuration);
      return client;
    }
  }
}
