package io.camunda.tasklist.rest.dto.requests;

import java.util.List;

public class SaveVariablesRequest {

  List<VariableInput> variables;

  public SaveVariablesRequest() {
  }

  public List<VariableInput> getVariables() {
    return variables;
  }

  public void setVariables(List<VariableInput> variables) {
    this.variables = variables;
  }
}
