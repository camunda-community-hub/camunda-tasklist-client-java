package io.camunda.tasklist;

import io.camunda.common.auth.Authentication;
import java.time.Duration;

public class CamundaTaskListClientProperties {
  public static final String CAMUNDA_FORMS_PREFIX = "camunda-forms:bpmn:";
  private Authentication authentication;
  private String taskListUrl;
  private boolean defaultShouldReturnVariables;
  private boolean defaultShouldLoadTruncatedVariables;
  private boolean alwaysReconnect = false;
  private Duration cookieExpiration = Duration.ofMinutes(3);

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

  public boolean isAlwaysReconnect() {
    return alwaysReconnect;
  }

  public void setAlwaysReconnect(boolean alwaysReconnect) {
    this.alwaysReconnect = alwaysReconnect;
  }

  public Duration getCookieExpiration() {
    return cookieExpiration;
  }

  public void setCookieExpiration(Duration cookieExpiration) {
    this.cookieExpiration = cookieExpiration;
  }
}
