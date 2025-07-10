package io.camunda.tasklist;

import io.camunda.client.CamundaClient;
import io.camunda.tasklist.auth.Authentication;
import java.net.URL;
import java.util.List;

public record CamundaTasklistClientConfiguration(
    ApiVersion apiVersion,
    Authentication authentication,
    URL baseUrl,
    CamundaClient camundaClient,
    DefaultProperties defaultProperties) {
  public record DefaultProperties(
      boolean returnVariables,
      boolean loadTruncatedVariables,
      boolean useCamundaUserTasks,
      List<String> tenantIds) {}

  public enum ApiVersion {
    v1,
    v2
  }

  public static List<String> DEFAULT_TENANT_IDS = List.of("<default>");
}
