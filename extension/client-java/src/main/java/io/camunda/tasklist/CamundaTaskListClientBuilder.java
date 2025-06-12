package io.camunda.tasklist;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.client.CamundaClient;
import io.camunda.tasklist.CamundaTasklistClientConfiguration.ApiVersion;
import io.camunda.tasklist.CamundaTasklistClientConfiguration.DefaultProperties;
import io.camunda.tasklist.auth.Authentication;
import io.camunda.tasklist.auth.JwtAuthentication;
import io.camunda.tasklist.auth.JwtCredential;
import io.camunda.tasklist.auth.TokenResponseHttpClientResponseHandler;
import io.camunda.tasklist.exception.TaskListException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

@Deprecated
public class CamundaTaskListClientBuilder {
  private Authentication authentication;
  private URL tasklistUrl;
  private CamundaClient camundaClient;
  private ApiVersion apiVersion = ApiVersion.v2;
  private DefaultProperties defaultProperties = new DefaultProperties(false, false, false);

  public CamundaTaskListClientBuilder authentication(Authentication authentication) {
    if (authentication != null) {
      this.authentication = authentication;
    }
    return this;
  }

  @Deprecated
  public CamundaTaskListClientBuilder taskListUrl(String taskListUrl) {
    try {
      return taskListUrl(URI.create(taskListUrl).toURL());
    } catch (MalformedURLException e) {
      throw new RuntimeException("Error while creating url from '" + taskListUrl + "'", e);
    }
  }

  public CamundaTaskListClientBuilder taskListUrl(URL taskListUrl) {
    if (taskListUrl != null) {
      this.tasklistUrl = taskListUrl;
    }
    return this;
  }

  @Deprecated(forRemoval = true)
  public CamundaTaskListClientBuilder zeebeClient(CamundaClient camundaClient) {
    return camundaClient(camundaClient);
  }

  public CamundaTaskListClientBuilder camundaClient(CamundaClient camundaClient) {
    useCamundaUserTasks();
    this.camundaClient = camundaClient;
    return this;
  }

  /**
   * Default behaviour will be to get variables along with tasks. Default value is false. Can also
   * be overwritten in the getTasks methods
   *
   * @return the builder
   */
  public CamundaTaskListClientBuilder shouldReturnVariables() {
    this.defaultProperties =
        new DefaultProperties(
            true,
            defaultProperties.loadTruncatedVariables(),
            defaultProperties.useCamundaUserTasks());
    return this;
  }

  public CamundaTaskListClientBuilder shouldLoadTruncatedVariables() {
    this.defaultProperties =
        new DefaultProperties(
            defaultProperties.returnVariables(), true, defaultProperties.useCamundaUserTasks());
    return this;
  }

  @Deprecated(forRemoval = true)
  public CamundaTaskListClientBuilder alwaysReconnect() {
    return this;
  }

  /**
   * Force cookie expiration after some time (default 3mn). Only usefull with SimpleAuthentication
   */
  @Deprecated(forRemoval = true)
  public CamundaTaskListClientBuilder cookieExpiration(Duration cookieExpiration) {
    return this;
  }

  /**
   * Enable when using zeebe user tasks (only relevant for >8.5). Will require presence of a zeebe
   * client
   */
  @Deprecated(forRemoval = true)
  public CamundaTaskListClientBuilder useZeebeUserTasks() {
    return useCamundaUserTasks();
  }

  /**
   * Enable when using zeebe user tasks (only relevant for >8.5). Will require presence of a zeebe
   * client
   */
  public CamundaTaskListClientBuilder useCamundaUserTasks() {
    this.defaultProperties =
        new DefaultProperties(
            defaultProperties.returnVariables(), defaultProperties.loadTruncatedVariables(), true);
    return this;
  }

  public CamundaTaskListClientBuilder apiVersion(ApiVersion apiVersion) {
    if (apiVersion != null) {
      this.apiVersion = apiVersion;
    }
    return this;
  }

  public CamundaTaskListClient build() throws TaskListException {
    CamundaTasklistClientConfiguration configuration =
        new CamundaTasklistClientConfiguration(
            apiVersion, authentication, tasklistUrl, camundaClient, defaultProperties);
    return new CamundaTaskListClient(configuration);
  }

  public CamundaTaskListClientBuilder selfManagedAuthentication(
      String clientId, String clientSecret, String keycloakUrl) {
    return selfManagedAuthentication(clientId, clientSecret, "tasklist-api", keycloakUrl);
  }

  public CamundaTaskListClientBuilder selfManagedAuthentication(
      String clientId, String clientSecret, String audience, String keycloakUrl) {
    return selfManagedAuthentication(clientId, clientSecret, "tasklist-api", null, keycloakUrl);
  }

  public CamundaTaskListClientBuilder selfManagedAuthentication(
      String clientId, String clientSecret, String audience, String scope, String authUrl) {
    try {
      authentication =
          new JwtAuthentication(
              new JwtCredential(
                  clientId, clientSecret, audience, URI.create(authUrl).toURL(), scope),
              new TokenResponseHttpClientResponseHandler(new ObjectMapper()));
    } catch (MalformedURLException e) {
      throw new RuntimeException("Error while parsing keycloak url", e);
    }
    return this;
  }

  public CamundaTaskListClientBuilder saaSAuthentication(String clientId, String clientSecret) {
    try {
      authentication =
          new JwtAuthentication(
              new JwtCredential(
                  clientId,
                  clientSecret,
                  "tasklist.camunda.io",
                  URI.create("https://login.cloud.camunda.io/oauth/token").toURL(),
                  null),
              new TokenResponseHttpClientResponseHandler(new ObjectMapper()));
    } catch (MalformedURLException e) {
      throw new RuntimeException("Error while parsing token url", e);
    }
    return this;
  }

  private String formatUrl(String url) {
    if (url.endsWith("/")) {
      return url.substring(0, url.length() - 1);
    }
    return url;
  }
}
