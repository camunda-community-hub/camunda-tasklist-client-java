package io.camunda.tasklist.spring;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("tasklist.client")
public record TasklistClientConfigurationProperties(
    // generic properties
    Profile profile,
    Boolean enabled,
    URL baseUrl,
    @DefaultValue ClientDefaults defaults,
    // simple auth properties
    String username,
    String password,
    Duration sessionTimeout,
    // oidc auth properties
    String clientId,
    String clientSecret,
    URL authUrl,
    String audience,
    String scope,
    // saas auth properies
    String region,
    String clusterId) {
  public enum Profile {
    simple,
    oidc,
    saas
  }

  public record ClientDefaults(
      @DefaultValue("true") boolean returnVariables,
      @DefaultValue("true") boolean loadTruncatedVariables,
      @DefaultValue("true") boolean useZeebeUserTasks,
      @DefaultValue("<default>") List<String> tenantIds) {}
}
