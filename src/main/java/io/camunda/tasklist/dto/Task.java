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

    private TaskState taskState;

    private List<String> candidateUsers;

    private List<String> candidateGroups;

	private OffsetDateTime followUpDate;

	private OffsetDateTime dueDate;

    private String formKey;

    private String taskDefinitionId;

    private List<String> sortValues;

    private Boolean isFirst;

    private String tenantId;

    private List<Variable> variables;

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

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }
}
