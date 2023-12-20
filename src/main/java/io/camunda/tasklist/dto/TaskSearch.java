package io.camunda.tasklist.dto;

import io.camunda.tasklist.exception.TaskListException;
import io.camunda.tasklist.generated.model.TaskByVariables;
import io.camunda.tasklist.generated.model.TaskByVariables.OperatorEnum;
import io.camunda.tasklist.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;

public class TaskSearch {

  private String group;
  private String assignee;
  private String candidateUser;
  private Boolean Assigned;
  private TaskState state;
  private String processDefinitionKey;
  private String processInstanceKey;
  private String taskDefinitionId;
  private List<TaskByVariables> taskVariables;
  private List<String> tenantIds;
  private Boolean withVariables;
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

  public List<TaskByVariables> getTaskVariables() {
    return taskVariables;
  }

  public TaskSearch setTaskVariables(List<TaskByVariables> taskVariables) {
    this.taskVariables = taskVariables;
    return this;
  }

  public TaskSearch addVariableFilter(String variableName, Object variableValue)
      throws TaskListException {
    return this.addVariableFilter(
        new TaskByVariables()
            .name(variableName)
            .value(JsonUtils.toJsonString(variableValue))
            .operator(OperatorEnum.EQ));
  }

  public TaskSearch addVariableFilter(TaskByVariables variableFilter) {
    if (this.taskVariables == null) {
      this.taskVariables = new ArrayList<>();
    }
    this.taskVariables.add(variableFilter);

    return this;
  }

  public List<String> getTenantIds() {
    return tenantIds;
  }

  public TaskSearch setTenantIds(List<String> tenantIds) {
    this.tenantIds = tenantIds;
    return this;
  }

  public TaskSearch addTenantId(String tenantId) {
    if (this.tenantIds == null) {
      this.tenantIds = new ArrayList<>();
    }
    this.tenantIds.add(tenantId);
    return this;
  }

  public Boolean getWithVariables() {
    return withVariables;
  }

  public boolean isWithVariables() {
    return withVariables != null && withVariables;
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
