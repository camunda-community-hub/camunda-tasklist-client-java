package io.camunda.tasklist.rest.dto;

import java.util.List;

public class TaskCompleteRequest {

  List<VariableInput> variables;

  public TaskCompleteRequest() {
  }

  public List<VariableInput> getVariables() {
    return variables;
  }

  public void setVariables(List<VariableInput> variables) {
    this.variables = variables;
  }
}
