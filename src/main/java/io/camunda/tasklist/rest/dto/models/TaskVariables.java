package io.camunda.tasklist.rest.dto.models;

import io.camunda.tasklist.rest.dto.emums.TaskOperator;

public class TaskVariables {

  String name;
  String value;
  String operator;

  public TaskVariables() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }
}
