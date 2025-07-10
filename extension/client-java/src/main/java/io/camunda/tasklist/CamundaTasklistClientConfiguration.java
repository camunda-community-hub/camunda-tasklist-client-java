package io.camunda.tasklist;

import io.camunda.tasklist.auth.Authentication;
import io.camunda.zeebe.client.ZeebeClient;
import java.net.URL;
import java.util.List;

public record CamundaTasklistClientConfiguration(
    Authentication authentication,
    URL baseUrl,
    ZeebeClient zeebeClient,
    DefaultProperties defaultProperties) {
  public record DefaultProperties(
      boolean returnVariables,
      boolean loadTruncatedVariables,
      boolean useZeebeUserTasks,
      List<String> tenantIds) {}

  public static List<String> DEFAULT_TENANT_IDS = List.of("<default>");
}
