package io.camunda.tasklist.rest.dto.responses;

public class VariableDraft {

  String value;
  Boolean isValueTruncated;
  String previewValue;

  public VariableDraft() {
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
}
