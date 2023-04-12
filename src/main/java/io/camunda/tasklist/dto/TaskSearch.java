package io.camunda.tasklist.dto;

public class TaskSearch {

  private String group;
  private String assignee;
  private Boolean Assigned;
  private TaskState state;
  private String processDefinitionId;
  private String processInstanceId;
  private String taskDefinitionId;
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
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public TaskSearch setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public TaskSearch setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public String getTaskDefinitionId() {
    return taskDefinitionId;
  }

  public TaskSearch setTaskDefinitionId(String taskDefinitionId) {
    this.taskDefinitionId = taskDefinitionId;
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
