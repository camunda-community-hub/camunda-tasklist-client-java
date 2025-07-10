package io.camunda.tasklist;

import static io.camunda.tasklist.CamundaTasklistClientConfiguration.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.tasklist.auth.Authentication;
import io.camunda.tasklist.auth.JwtAuthentication;
import io.camunda.tasklist.auth.JwtCredential;
import io.camunda.tasklist.auth.TokenResponseHttpClientResponseHandler;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.zeebe.client.ZeebeClient;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;

@Deprecated
public class CamundaTaskListClientBuilder {
  private CamundaTaskListClientProperties properties = new CamundaTaskListClientProperties();
  private ZeebeClient zeebeClient;

  public CamundaTaskListClientBuilder authentication(Authentication authentication) {
    properties.setAuthentication(authentication);
    return this;
  }

  public CamundaTaskListClientBuilder taskListUrl(String taskListUrl) {
    properties.setTaskListUrl(formatUrl(taskListUrl));
    return this;
  }

  public CamundaTaskListClientBuilder zeebeClient(ZeebeClient zeebeClient) {
    this.zeebeClient = zeebeClient;
    return this;
  }

  /**
   * Default behaviour will be to get variables along with tasks. Default value is false. Can also
   * be overwritten in the getTasks methods
   *
   * @return the builder
   */
  public CamundaTaskListClientBuilder shouldReturnVariables() {
    properties.setDefaultShouldReturnVariables(true);
    return this;
  }

  public CamundaTaskListClientBuilder shouldLoadTruncatedVariables() {

    properties.setDefaultShouldLoadTruncatedVariables(true);
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
  public CamundaTaskListClientBuilder useZeebeUserTasks() {
    properties.setUseZeebeUserTasks(true);
    return this;
  }

  public CamundaTaskListClient build() throws TaskListException {
    return new CamundaTaskListClient(properties, zeebeClient);
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
      properties.setAuthentication(
          new JwtAuthentication(
              new JwtCredential(
                  clientId, clientSecret, audience, URI.create(authUrl).toURL(), scope),
              new TokenResponseHttpClientResponseHandler(new ObjectMapper())));
    } catch (MalformedURLException e) {
      throw new RuntimeException("Error while parsing keycloak url", e);
    }
    return this;
  }

  public CamundaTaskListClientBuilder saaSAuthentication(String clientId, String clientSecret) {
    try {
      properties.setAuthentication(
          new JwtAuthentication(
              new JwtCredential(
                  clientId,
                  clientSecret,
                  "tasklist.camunda.io",
                  URI.create("https://login.cloud.camunda.io/oauth/token").toURL(),
                  null),
              new TokenResponseHttpClientResponseHandler(new ObjectMapper())));
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
