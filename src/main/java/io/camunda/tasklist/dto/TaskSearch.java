package io.camunda.tasklist.dto;

public class TaskSearch {

  private String group;
  private String assignee;
  private String candidateUser;
  private Boolean Assigned;
  private TaskState state;
  private String processDefinitionKey;
  private String processInstanceKey;
  private String taskDefinitionId;
  private boolean withVariables;
  private DateFilter followUpDate;
  private DateFilter dueDate;
  private Pagination pagination;
  
  public String getCandidateUser() {
    return candidateUser;
  }

  public TaskSearch setCandidateUser(String candidateUser) {
    this.candidateUser = candidateUser;
    return this;
  }

  public DateFilter getFollowUpDate() {
    return followUpDate;
  }

  public TaskSearch setFollowUpDate(DateFilter followUpDate) {
    this.followUpDate = followUpDate;
    return this;
  }

  public DateFilter getDueDate() {
    return dueDate;
  }

  public TaskSearch setDueDate(DateFilter dueDate) {
    this.dueDate = dueDate;
    return this;
  }

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
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public TaskSearch setProcessDefinitionKey(String processDefinitionId) {
    this.processDefinitionKey = processDefinitionId;
    return this;
  }

  public String getProcessInstanceKey() {
    return processInstanceKey;
  }

  public TaskSearch setProcessInstanceKey(String processInstanceId) {
    this.processInstanceKey = processInstanceId;
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
