package io.camunda.tasklist.dto;

import java.util.ArrayList;
import java.util.List;

import io.camunda.tasklist.exception.TaskListException;
import io.camunda.tasklist.generated.model.IncludeVariable;
import io.camunda.tasklist.generated.model.TaskByVariables;
import io.camunda.tasklist.generated.model.TaskByVariables.OperatorEnum;
import io.camunda.tasklist.util.JsonUtils;

public class TaskSearch {
  private String candidateGroup;
  private List<String> candidateGroups;
  private String assignee;
  private String candidateUser;
  private List<String> candidateUsers;
  private Boolean assigned;
  private TaskState state;
  private String processDefinitionKey;
  private String processInstanceKey;
  private String taskDefinitionId;
  private List<TaskByVariables> taskVariables;
  private List<String> tenantIds;
  private Boolean withVariables;
  private DateFilter followUpDate;
  private DateFilter dueDate;
  private List<IncludeVariable> includeVariables;
  private Pagination pagination;

  public List<String> getCandidateGroups() {
    return candidateGroups;
  }

  public TaskSearch setCandidateGroups(List<String> candidateGroups) {
    this.candidateGroups = candidateGroups;
    return this;
  }

  public List<String> getCandidateUsers() {
    return candidateUsers;
  }

  public TaskSearch setCandidateUsers(List<String> candidateUsers) {
    this.candidateUsers = candidateUsers;
    return this;
  }

  public TaskSearch setWithVariables(Boolean withVariables) {
    this.withVariables = withVariables;
    return this;
  }

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

  public String getCandidateGroup() {
    return candidateGroup;
  }

  public TaskSearch setCandidateGroup(String candidateGroup) {
    this.candidateGroup = candidateGroup;
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
    return assigned;
  }

  public TaskSearch setAssigned(Boolean assigned) {
     this.assigned = assigned;
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

  public List<IncludeVariable> getIncludeVariables() {
    return includeVariables;
  }

  public TaskSearch setIncludeVariables(List<IncludeVariable> includeVariables) {
    this.includeVariables = includeVariables;
    return this;
  }
  
  public TaskSearch fetchVariable(String variable) {
      return fetchVariable(variable, true);
  }

  public TaskSearch fetchVariable(String variable, boolean alwaysReturnFullValue) {
    if (this.includeVariables ==null) {
        this.includeVariables = new ArrayList<>();
    }
    IncludeVariable iv = new IncludeVariable();
    iv.setName(variable);
    iv.alwaysReturnFullValue(alwaysReturnFullValue);
    this.includeVariables.add(iv);
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
