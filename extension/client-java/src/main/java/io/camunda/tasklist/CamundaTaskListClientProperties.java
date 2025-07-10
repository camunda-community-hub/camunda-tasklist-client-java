package io.camunda.tasklist;

import static io.camunda.tasklist.CamundaTasklistClientConfiguration.DefaultProperties.*;

import io.camunda.tasklist.auth.Authentication;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class CamundaTaskListClientProperties {
  public static final String CAMUNDA_FORMS_PREFIX = "camunda-forms:bpmn:";
  private Authentication authentication;
  private String taskListUrl;
  private boolean defaultShouldReturnVariables;
  private boolean defaultShouldLoadTruncatedVariables;
  private boolean useZeebeUserTasks;
  private List<String> tenantIds = new ArrayList<>(DEFAULT_TENANT_IDS);

  public CamundaTaskListClientProperties() {}

  public CamundaTaskListClientProperties(
      Authentication authentication,
      String taskListUrl,
      boolean defaultShouldReturnVariables,
      boolean defaultShouldLoadTruncatedVariables,
      boolean useZeebeUserTasks) {
    this.authentication = authentication;
    this.taskListUrl = taskListUrl;
    this.defaultShouldReturnVariables = defaultShouldReturnVariables;
    this.defaultShouldLoadTruncatedVariables = defaultShouldLoadTruncatedVariables;
    this.useZeebeUserTasks = useZeebeUserTasks;
  }

  public List<String> getTenantIds() {
    return tenantIds;
  }

  public void setTenantIds(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  public Authentication getAuthentication() {
    return authentication;
  }

  public void setAuthentication(Authentication authentication) {
    this.authentication = authentication;
  }

  public String getTaskListUrl() {
    return taskListUrl;
  }

  public void setTaskListUrl(String taskListUrl) {
    this.taskListUrl = taskListUrl;
  }

  public boolean isDefaultShouldReturnVariables() {
    return defaultShouldReturnVariables;
  }

  public void setDefaultShouldReturnVariables(boolean defaultShouldReturnVariables) {
    this.defaultShouldReturnVariables = defaultShouldReturnVariables;
  }

  public boolean isDefaultShouldLoadTruncatedVariables() {
    return defaultShouldLoadTruncatedVariables;
  }

  public void setDefaultShouldLoadTruncatedVariables(boolean defaultShouldLoadTruncatedVariables) {
    this.defaultShouldLoadTruncatedVariables = defaultShouldLoadTruncatedVariables;
  }

  public boolean isUseZeebeUserTasks() {
    return useZeebeUserTasks;
  }

  public void setUseZeebeUserTasks(boolean useZeebeUserTasks) {
    this.useZeebeUserTasks = useZeebeUserTasks;
  }
}
