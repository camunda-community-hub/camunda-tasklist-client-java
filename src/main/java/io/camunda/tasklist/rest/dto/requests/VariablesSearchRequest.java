package io.camunda.tasklist.rest.dto.requests;

import java.util.List;

public class VariablesSearchRequest {

  List<String> variableNames;

  public VariablesSearchRequest() {
  }

  public List<String> getVariableNames() {
    return variableNames;
  }

  public void setVariableNames(List<String> variableNames) {
    this.variableNames = variableNames;
  }
}
