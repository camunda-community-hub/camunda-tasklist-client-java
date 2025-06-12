package io.camunda.tasklist;

import io.camunda.client.CamundaClient;
import io.camunda.tasklist.auth.Authentication;
import java.net.URL;

public record CamundaTasklistClientConfiguration(
    ApiVersion apiVersion,
    Authentication authentication,
    URL baseUrl,
    CamundaClient camundaClient,
    DefaultProperties defaultProperties) {
  public record DefaultProperties(
      boolean returnVariables, boolean loadTruncatedVariables, boolean useCamundaUserTasks) {}

  public enum ApiVersion {
    v1,
    v2
  }
}
