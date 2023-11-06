package io.camunda.tasklist.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskState {
  CREATED("CREATED"), COMPLETED("COMPLETED"), CANCELED("CANCELED");

  private String rawValue;

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
