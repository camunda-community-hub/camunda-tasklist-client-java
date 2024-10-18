package io.camunda.tasklist.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.CamundaTaskListClientProperties;
import io.camunda.tasklist.auth.Authentication;
import io.camunda.tasklist.auth.JwtAuthentication;
import io.camunda.tasklist.auth.JwtCredential;
import io.camunda.tasklist.auth.SimpleAuthentication;
import io.camunda.tasklist.auth.SimpleCredential;
import io.camunda.tasklist.auth.TokenResponseMapper.JacksonTokenResponseMapper;
import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@EnableConfigurationProperties({TasklistClientConfigurationProperties.class})
@ConditionalOnProperty(value = "tasklist.client.enabled", matchIfMissing = true)
@Import(ObjectMapperConfiguration.class)
public class TasklistClientConfiguration {
  private final TasklistClientConfigurationProperties properties;
  private final ObjectMapper objectMapper;

  @Autowired
  public TasklistClientConfiguration(
      TasklistClientConfigurationProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
  }

  @Bean
  @ConditionalOnMissingBean
  public CamundaTaskListClient camundaTasklistClient(
      CamundaTaskListClientProperties properties,
      @Autowired(required = false) ZeebeClient zeebeClient) {
    return new CamundaTaskListClient(properties, zeebeClient);
  }

  @Bean
  @ConditionalOnMissingBean
  public CamundaTaskListClientProperties taskListClientProperties(Authentication authentication) {
    return new CamundaTaskListClientProperties(
        authentication,
        properties.baseUrl().toString(),
        properties.defaults().returnVariables(),
        properties.defaults().loadTruncatedVariables(),
        properties.defaults().useZeebeUserTasks());
  }

  @Bean
  @ConditionalOnMissingBean
  public Authentication authentication() {
    if (properties.profile() == null) {
      throw new IllegalStateException("'tasklist.client.profile' is required");
    }
    switch (properties.profile()) {
      case simple -> {
        return new SimpleAuthentication(
            new SimpleCredential(
                properties.username(),
                properties.password(),
                properties.baseUrl(),
                properties.sessionTimeout()));
      }
      case oidc, saas -> {
        return new JwtAuthentication(
            new JwtCredential(
                properties.clientId(),
                properties.clientSecret(),
                properties.audience(),
                properties.authUrl(),
                properties.scope()),
            new JacksonTokenResponseMapper(objectMapper));
      }
      default -> throw new IllegalStateException("Unsupported profile: " + properties.profile());
    }
  }
}
