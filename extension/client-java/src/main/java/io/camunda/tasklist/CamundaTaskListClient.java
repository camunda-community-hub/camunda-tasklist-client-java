package io.camunda.tasklist;

import static io.camunda.tasklist.util.PaginationUtil.*;

import io.camunda.tasklist.dto.DateFilter;
import io.camunda.tasklist.dto.Form;
import io.camunda.tasklist.dto.Pagination;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.dto.TaskSearch.IncludeVariable;
import io.camunda.tasklist.dto.TaskSearch.TaskByVariables;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.dto.Variable;
import io.camunda.tasklist.exception.TaskListException;
import java.util.List;
import java.util.Map;

public interface CamundaTaskListClient {
  Task unclaim(String taskId) throws TaskListException;

  default Task claim(String taskId, String assignee) throws TaskListException {
    return claim(taskId, assignee, false);
  }

  Task claim(String taskId, String assignee, Boolean allowOverrideAssignment)
      throws TaskListException;

  void completeTask(String taskId, Map<String, Object> variablesMap) throws TaskListException;

  default TaskList getTasks(Boolean assigned, TaskState state, Integer pageSize)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setAssigned(assigned)
            .setState(state)
            .setPagination(createPagination(pageSize)));
  }

  default TaskList getTasks(Boolean assigned, TaskState state, Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch().setAssigned(assigned).setState(state).setPagination(pagination));
  }

  default TaskList getTasks(
      Boolean assigned, TaskState state, boolean withVariables, Integer pageSize)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setAssigned(assigned)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(createPagination(pageSize)));
  }

  default TaskList getTasks(
      Boolean assigned, TaskState state, boolean withVariables, Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setAssigned(assigned)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(pagination));
  }

  default TaskList getAssigneeTasks(String assigneeId, TaskState state, Integer pageSize)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setAssignee(assigneeId)
            .setState(state)
            .setPagination(createPagination(pageSize)));
  }

  default TaskList getAssigneeTasks(String assigneeId, TaskState state, Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch().setAssignee(assigneeId).setState(state).setPagination(pagination));
  }

  default TaskList getAssigneeTasks(
      String assigneeId, TaskState state, boolean withVariables, Integer pageSize)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setAssigned(true)
            .setAssignee(assigneeId)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(createPagination(pageSize)));
  }

  default TaskList getAssigneeTasks(
      String assigneeId, TaskState state, boolean withVariables, Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setAssignee(assigneeId)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(pagination));
  }

  default TaskList getGroupTasks(String group, TaskState state, Integer pageSize)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setCandidateGroup(group)
            .setState(state)
            .setPagination(createPagination(pageSize)));
  }

  default TaskList getGroupTasks(String group, TaskState state, Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch().setCandidateGroup(group).setState(state).setPagination(pagination));
  }

  default TaskList getGroupTasks(
      String group, TaskState state, boolean withVariables, Integer pageSize)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setCandidateGroup(group)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(createPagination(pageSize)));
  }

  default TaskList getGroupTasks(
      String group, TaskState state, boolean withVariables, Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setCandidateGroup(group)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(pagination));
  }

  default TaskList getGroupsTasks(
      List<String> groups, TaskState state, boolean withVariables, Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setCandidateGroups(groups)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(pagination));
  }

  Task getTask(String taskId) throws TaskListException;

  Task getTask(String taskId, boolean withVariables) throws TaskListException;

  List<Variable> getVariables(String taskId) throws TaskListException;

  List<Variable> getVariables(String taskId, boolean loadTruncated) throws TaskListException;

  Variable getVariable(String variableId) throws TaskListException;

  default Form getForm(String formId, String processDefinitionId) throws TaskListException {
    return getForm(formId, processDefinitionId, null);
  }

  Form getForm(String formId, String processDefinitionId, Long version) throws TaskListException;

  TaskList before(TaskList taskList) throws TaskListException;

  TaskList beforeOrEquals(TaskList taskList) throws TaskListException;

  TaskList after(TaskList taskList) throws TaskListException;

  TaskList afterOrEqual(TaskList taskList) throws TaskListException;

  TaskList getTasks(TaskSearch search) throws TaskListException;

  default TaskList getTasks(
      String group,
      Boolean assigned,
      String assigneeId,
      TaskState state,
      boolean withVariables,
      Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setCandidateGroup(group)
            .setAssigned(assigned)
            .setAssignee(assigneeId)
            .setState(state)
            .setWithVariables(withVariables)
            .setPagination(pagination));
  }

  default TaskList getTasks(
      String candidateUser,
      List<String> candidateUsers,
      String group,
      List<String> groups,
      Boolean assigned,
      String assignee,
      TaskState state,
      DateFilter followUpDate,
      DateFilter dueDate,
      String processDefinitionId,
      String processInstanceId,
      String taskDefinitionId,
      List<TaskByVariables> taskVariables,
      List<String> tenantIds,
      List<IncludeVariable> includeVariables,
      boolean withVariables,
      Pagination pagination)
      throws TaskListException {
    return getTasks(
        new TaskSearch()
            .setCandidateUser(candidateUser)
            .setCandidateUsers(candidateUsers)
            .setCandidateGroup(group)
            .setCandidateGroups(groups)
            .setAssigned(assigned)
            .setAssignee(assignee)
            .setState(state)
            .setFollowUpDate(followUpDate)
            .setDueDate(dueDate)
            .setProcessDefinitionKey(processDefinitionId)
            .setProcessInstanceKey(processInstanceId)
            .setTaskDefinitionId(taskDefinitionId)
            .setTaskVariables(taskVariables)
            .setTenantIds(tenantIds)
            .setIncludeVariables(includeVariables)
            .setWithVariables(withVariables)
            .setPagination(pagination));
  }

  void loadVariables(List<Task> tasks) throws TaskListException;

  void loadVariables(List<Task> tasks, boolean loadTruncated) throws TaskListException;

  void saveDraftVariables(String taskId, Map<String, Object> variables) throws TaskListException;
}
