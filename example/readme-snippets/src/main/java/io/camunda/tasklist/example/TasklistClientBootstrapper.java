package io.camunda.tasklist.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.CamundaTasklistClientConfiguration;
import io.camunda.tasklist.CamundaTasklistClientConfiguration.DefaultProperties;
import io.camunda.tasklist.auth.JwtAuthentication;
import io.camunda.tasklist.auth.JwtCredential;
import io.camunda.tasklist.auth.SimpleAuthentication;
import io.camunda.tasklist.auth.SimpleCredential;
import io.camunda.tasklist.auth.TokenResponseHttpClientResponseHandler;
import io.camunda.zeebe.client.ZeebeClient;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

public interface TasklistClientBootstrapper {
  static ZeebeClient zeebeClient() {
    return ZeebeClient.newClientBuilder().build();
  }

  CamundaTaskListClient createTasklistClient() throws MalformedURLException;

  class SimpleAuthTasklistClientBootstrapper implements TasklistClientBootstrapper {

    @Override
    public CamundaTaskListClient createTasklistClient() throws MalformedURLException {
      // properties you need to provide
      String username = "demo";
      String password = "demo";
      URL tasklistUrl = URI.create("http://localhost:8082").toURL();
      boolean returnVariables = false;
      boolean loadTruncatedVariables = false;
      boolean useZeebeUserTasks = true;
      // if you are using zeebe user tasks, you require a zeebe client as well
      ZeebeClient zeebeClient = zeebeClient();
      // bootstrapping
      SimpleCredential credentials =
          new SimpleCredential(username, password, tasklistUrl, Duration.ofMinutes(10));
      SimpleAuthentication authentication = new SimpleAuthentication(credentials);
      CamundaTasklistClientConfiguration configuration =
          new CamundaTasklistClientConfiguration(
              authentication,
              tasklistUrl,
              zeebeClient,
              new DefaultProperties(returnVariables, loadTruncatedVariables, useZeebeUserTasks));
      CamundaTaskListClient client = new CamundaTaskListClient(configuration);
      return client;
    }
  }

  class IdentityAuthTasklistClientBootstrapper implements TasklistClientBootstrapper {
    @Override
    public CamundaTaskListClient createTasklistClient() throws MalformedURLException {
      // properties you need to provide
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
      ZeebeClient zeebeClient = zeebeClient();
      // bootstrapping
      JwtCredential credentials =
          new JwtCredential(clientId, clientSecret, audience, authUrl, scope);
      ObjectMapper objectMapper = new ObjectMapper();
      TokenResponseHttpClientResponseHandler clientResponseHandler =
          new TokenResponseHttpClientResponseHandler(objectMapper);
      JwtAuthentication authentication = new JwtAuthentication(credentials, clientResponseHandler);
      CamundaTasklistClientConfiguration configuration =
          new CamundaTasklistClientConfiguration(
              authentication,
              tasklistUrl,
              zeebeClient,
              new DefaultProperties(returnVariables, loadTruncatedVariables, useZeebeUserTasks));
      CamundaTaskListClient client = new CamundaTaskListClient(configuration);
      return client;
    }
  }

  class SaasTasklistClientBootstrapper implements TasklistClientBootstrapper {
    @Override
    public CamundaTaskListClient createTasklistClient() throws MalformedURLException {
      // properties you need to provide
      String region = "";
      String clusterId = "";
      String clientId = "";
      String clientSecret = "";
      boolean returnVariables = false;
      boolean loadTruncatedVariables = false;
      boolean useZeebeUserTasks = true;
      // if you are using zeebe user tasks, you require a zeebe client as well
      ZeebeClient zeebeClient = zeebeClient();
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
              authentication,
              tasklistUrl,
              zeebeClient,
              new DefaultProperties(returnVariables, loadTruncatedVariables, useZeebeUserTasks));
      CamundaTaskListClient client = new CamundaTaskListClient(configuration);
      return client;
    }
  }
}
