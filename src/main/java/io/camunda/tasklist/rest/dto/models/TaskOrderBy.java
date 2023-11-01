package io.camunda.tasklist.rest.dto.models;

import io.camunda.tasklist.rest.dto.emums.TaskSort;

public class TaskOrderBy {

  String field;
  String order;

  public TaskOrderBy() {
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }
}
