package io.camunda.tasklist.dto;

public class TaskSearch {

  private String group;
  private String assignee;
  private Boolean Assigned;
  private TaskState state;
  private boolean withVariables;
  private Pagination pagination;

  public String getGroup() {
    return group;
  }

  public TaskSearch setGroup(String group) {
    this.group = group;
    return this;
  }

  public String getAssignee() {
    return assignee;
  }

  public TaskSearch setAssignee(String assignee) {
    this.assignee = assignee;
    return this;
  }

  public Boolean getAssigned() {
    return Assigned;
  }

  public TaskSearch setAssigned(Boolean assigned) {
    Assigned = assigned;
    return this;
  }

  public TaskState getState() {
    return state;
  }

  public TaskSearch setState(TaskState state) {
    this.state = state;
    return this;
  }

  public boolean isWithVariables() {
    return withVariables;
  }

  public TaskSearch setWithVariables(boolean withVariables) {
    this.withVariables = withVariables;
    return this;
  }

  public Pagination getPagination() {
    return pagination;
  }

  public TaskSearch setPagination(Pagination pagination) {
    this.pagination = pagination;
    return this;
  }
}
