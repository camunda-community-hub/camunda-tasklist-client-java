package io.camunda.tasklist.dto;

public class Variable {
    private String id;

    private String name;

    private String previewValue;

    private Boolean isValueTruncated;

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

    public String getPreviewValue() {
        return previewValue;
    }

    public void setPreviewValue(String previewValue) {
        this.previewValue = previewValue;
    }

    public Boolean getIsValueTruncated() {
        return isValueTruncated;
    }

    public void setIsValueTruncated(Boolean isValueTruncated) {
        this.isValueTruncated = isValueTruncated;
    }
}
