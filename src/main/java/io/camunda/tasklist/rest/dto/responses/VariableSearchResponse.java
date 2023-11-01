package io.camunda.tasklist.rest.dto.responses;

public class VariableSearchResponse {

  String id;
  String name;
  String value;
  Boolean isValueTruncated;
  String previewValue;
  VariableDraft draft;

  public VariableSearchResponse() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public Boolean getValueTruncated() {
    return isValueTruncated;
  }

  public void setValueTruncated(Boolean valueTruncated) {
    isValueTruncated = valueTruncated;
  }

  public String getPreviewValue() {
    return previewValue;
  }

  public void setPreviewValue(String previewValue) {
    this.previewValue = previewValue;
  }

  public VariableDraft getDraft() {
    return draft;
  }

  public void setDraft(VariableDraft draft) {
    this.draft = draft;
  }
}
