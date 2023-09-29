package io.camunda.tasklist.dto;

public class VariableInput {

  String name;
  String value;

  public VariableInput() {
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
}
