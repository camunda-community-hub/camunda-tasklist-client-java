package io.camunda.tasklist;

import io.camunda.common.auth.Authentication;
import io.camunda.common.auth.JwtConfig;
import io.camunda.common.auth.JwtCredential;
import io.camunda.common.auth.Product;
import io.camunda.common.auth.SaaSAuthentication;
import io.camunda.common.auth.SelfManagedAuthentication;
import io.camunda.common.auth.identity.IdentityConfig;
import io.camunda.common.auth.identity.IdentityContainer;
import io.camunda.common.json.SdkObjectMapper;
import io.camunda.identity.sdk.Identity;
import io.camunda.identity.sdk.IdentityConfiguration;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.zeebe.client.ZeebeClient;
import java.time.Duration;

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

  public CamundaTaskListClientBuilder alwaysReconnect() {
    properties.setAlwaysReconnect(true);
    return this;
  }

  /**
   * Force cookie expiration after some time (default 3mn). Only usefull with SimpleAuthentication
   */
  public CamundaTaskListClientBuilder cookieExpiration(Duration cookieExpiration) {
    properties.setCookieExpiration(cookieExpiration);
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
    IdentityConfig identityConfig = new IdentityConfig();
    IdentityConfiguration identityConfiguration =
        new IdentityConfiguration(keycloakUrl, keycloakUrl, clientId, clientSecret, clientId);
    Identity identity = new Identity(identityConfiguration);
    identityConfig.addProduct(
        Product.TASKLIST, new IdentityContainer(identity, identityConfiguration));
    JwtConfig jwtConfig = new JwtConfig();
    jwtConfig.addProduct(
        Product.TASKLIST, new JwtCredential(clientId, clientSecret, clientId, keycloakUrl));
    properties.setAuthentication(
        SelfManagedAuthentication.builder()
            .withJwtConfig(jwtConfig)
            .withIdentityConfig(identityConfig)
            .build());
    return this;
  }

  public CamundaTaskListClientBuilder saaSAuthentication(String clientId, String clientSecret) {
    JwtConfig jwtConfig = new JwtConfig();
    jwtConfig.addProduct(
        Product.TASKLIST,
        new JwtCredential(
            clientId,
            clientSecret,
            "tasklist.camunda.io",
            "https://login.cloud.camunda.io/oauth/token"));
    properties.setAuthentication(
        SaaSAuthentication.builder()
            .withJwtConfig(jwtConfig)
            .withJsonMapper(new SdkObjectMapper())
            .build());
    return this;
  }

  private String formatUrl(String url) {
    if (url.endsWith("/")) {
      return url.substring(0, url.length() - 1);
    }
    return url;
  }
}
