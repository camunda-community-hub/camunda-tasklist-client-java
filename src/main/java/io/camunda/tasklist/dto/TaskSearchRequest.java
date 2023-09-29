package io.camunda.tasklist.dto;

import java.util.List;

public class TaskSearchRequest {

  String state;
  Boolean assigned;
  String assignee;
  String taskDefinitionId;
  String candidateGroup;
  String candidateUser;
  String processDefinitionKey;
  String processInstanceKey;
  Integer pageSize;
  DateFilter followUpDate;
  DateFilter dueDate;
  List<TaskOrderBy> sort;
  List<String> searchAfter;
  List<String> searchAfterOrEqual;
  List<String> searchBefore;
  List<String> searchBeforeOrEqual;

  public TaskSearchRequest() {
    this.pageSize = 50;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public Boolean getAssigned() {
    return assigned;
  }

  public void setAssigned(Boolean assigned) {
    this.assigned = assigned;
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public String getTaskDefinitionId() {
    return taskDefinitionId;
  }

  public void setTaskDefinitionId(String taskDefinitionId) {
    this.taskDefinitionId = taskDefinitionId;
  }

  public String getCandidateGroup() {
    return candidateGroup;
  }

  public void setCandidateGroup(String candidateGroup) {
    this.candidateGroup = candidateGroup;
  }

  public String getCandidateUser() {
    return candidateUser;
  }

  public void setCandidateUser(String candidateUser) {
    this.candidateUser = candidateUser;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(String processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public Integer getPageSize() {
    return pageSize;
  }

  public void setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
  }

  public DateFilter getFollowUpDate() {
    return followUpDate;
  }

  public void setFollowUpDate(DateFilter followUpDate) {
    this.followUpDate = followUpDate;
  }

  public DateFilter getDueDate() {
    return dueDate;
  }

  public void setDueDate(DateFilter dueDate) {
    this.dueDate = dueDate;
  }

  public List<TaskOrderBy> getSort() {
    return sort;
  }

  public void setSort(List<TaskOrderBy> sort) {
    this.sort = sort;
  }

  public List<String> getSearchAfter() {
    return searchAfter;
  }

  public void setSearchAfter(List<String> searchAfter) {
    this.searchAfter = searchAfter;
  }

  public List<String> getSearchAfterOrEqual() {
    return searchAfterOrEqual;
  }

  public void setSearchAfterOrEqual(List<String> searchAfterOrEqual) {
    this.searchAfterOrEqual = searchAfterOrEqual;
  }

  public List<String> getSearchBefore() {
    return searchBefore;
  }

  public void setSearchBefore(List<String> searchBefore) {
    this.searchBefore = searchBefore;
  }

  public List<String> getSearchBeforeOrEqual() {
    return searchBeforeOrEqual;
  }

  public void setSearchBeforeOrEqual(List<String> searchBeforeOrEqual) {
    this.searchBeforeOrEqual = searchBeforeOrEqual;
  }
}
