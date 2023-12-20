package io.camunda.tasklist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TaskState {
  CREATED("CREATED"),
  COMPLETED("COMPLETED"),
  CANCELED("CANCELED");

  private final String rawValue;

  TaskState(String rawValue) {
    this.rawValue = rawValue;
  }

  public String getRawValue() {
    return rawValue;
  }

  public static TaskState fromJson(@JsonProperty("rawValue") String rawValue) {
    return valueOf(rawValue);
  }
}
