package io.camunda.tasklist.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class Task {
  private String id;

  private String name;

  private String processName;

  private String processDefinitionKey;

  private String processInstanceKey;

  private String assignee;

  private String creationDate;

  private String completionDate;

  private TaskState taskState;

  private List<String> candidateUsers;

  private List<String> candidateGroups;

  private OffsetDateTime followUpDate;

  private OffsetDateTime dueDate;

  private String formKey;

  private String formId;

  private Long formVersion;

  private Boolean isFormEmbedded;

  private String taskDefinitionId;

  private List<String> sortValues;

  private Boolean isFirst;

  private String tenantId;

  private Integer priority;

  private List<Variable> variables;
  private Implementation implementation;

  public Boolean getFormEmbedded() {
    return isFormEmbedded;
  }

  public void setFormEmbedded(Boolean formEmbedded) {
    isFormEmbedded = formEmbedded;
  }

  public Boolean getFirst() {
    return isFirst;
  }

  public void setFirst(Boolean first) {
    isFirst = first;
  }

  public Implementation getImplementation() {
    return implementation;
  }

  public void setImplementation(Implementation implementation) {
    this.implementation = implementation;
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

  public String getProcessName() {
    return processName;
  }

  public void setProcessName(String processName) {
    this.processName = processName;
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public String getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  public String getCompletionDate() {
    return completionDate;
  }

  public void setCompletionDate(String completionDate) {
    this.completionDate = completionDate;
  }

  public TaskState getTaskState() {
    return taskState;
  }

  public void setTaskState(TaskState taskState) {
    this.taskState = taskState;
  }

  public List<String> getSortValues() {
    return sortValues;
  }

  public void setSortValues(List<String> sortValues) {
    this.sortValues = sortValues;
  }

  public Boolean getIsFirst() {
    return isFirst;
  }

  public void setIsFirst(Boolean isFirst) {
    this.isFirst = isFirst;
  }

  public List<String> getCandidateGroups() {
    return candidateGroups;
  }

  public void setCandidateGroups(List<String> candidateGroups) {
    this.candidateGroups = candidateGroups;
  }

  public String getFormKey() {
    return formKey;
  }

  public void setFormKey(String formKey) {
    this.formKey = formKey;
  }

  public String getFormId() {
    return formId;
  }

  public void setFormId(String formId) {
    this.formId = formId;
  }

  public Long getFormVersion() {
    return formVersion;
  }

  public void setFormVersion(Long formVersion) {
    this.formVersion = formVersion;
  }

  public Boolean getIsFormEmbedded() {
    return isFormEmbedded;
  }

  public void setIsFormEmbedded(Boolean isFormEmbedded) {
    this.isFormEmbedded = isFormEmbedded;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionId) {
    this.processDefinitionKey = processDefinitionId;
  }

  public String getTaskDefinitionId() {
    return taskDefinitionId;
  }

  public void setTaskDefinitionId(String taskDefinitionId) {
    this.taskDefinitionId = taskDefinitionId;
  }

  public String getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(String processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public List<String> getCandidateUsers() {
    return candidateUsers;
  }

  public void setCandidateUsers(List<String> candidateUsers) {
    this.candidateUsers = candidateUsers;
  }

  public OffsetDateTime getFollowUpDate() {
    return followUpDate;
  }

  public void setFollowUpDate(OffsetDateTime followUpDate) {
    this.followUpDate = followUpDate;
  }

  public OffsetDateTime getDueDate() {
    return dueDate;
  }

  public void setDueDate(OffsetDateTime dueDate) {
    this.dueDate = dueDate;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public List<Variable> getVariables() {
    return variables;
  }

  public void setVariables(List<Variable> variables) {
    this.variables = variables;
  }

  public enum Implementation {
    JOB_WORKER,
    ZEEBE_USER_TASK
  }
}
