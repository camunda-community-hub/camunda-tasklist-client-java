package io.camunda.tasklist.rest.dto.responses;

public class VariableResponse {

  String id;
  String name;
  String value;
  VariableDraft draft;
  String tenantId;

  public VariableResponse() {
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

  public VariableDraft getDraft() {
    return draft;
  }

  public void setDraft(VariableDraft draft) {
    this.draft = draft;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
