package io.camunda.tasklist;

import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import io.camunda.tasklist.auth.AuthInterface;
import io.camunda.tasklist.dto.DateFilter;
import io.camunda.tasklist.dto.Form;
import io.camunda.tasklist.dto.Pagination;
import io.camunda.tasklist.dto.SearchType;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.dto.Variable;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.tasklist.generated.api.FormApi;
import io.camunda.tasklist.generated.api.TaskApi;
import io.camunda.tasklist.generated.invoker.ApiClient;
import io.camunda.tasklist.generated.invoker.ApiException;
import io.camunda.tasklist.generated.invoker.Configuration;
import io.camunda.tasklist.generated.model.TaskAssignRequest;
import io.camunda.tasklist.generated.model.TaskCompleteRequest;
import io.camunda.tasklist.generated.model.TaskSearchRequest;
import io.camunda.tasklist.generated.model.VariableInputDTO;
import io.camunda.tasklist.generated.model.VariableSearchResponse;
import io.camunda.tasklist.generated.model.VariablesSearchRequest;
import io.camunda.tasklist.util.ConverterUtils;

public class CamundaTaskListClient {

    private AuthInterface authentication;

    private String taskListUrl;

    private boolean defaultShouldReturnVariables;

    private int tokenExpiration;

    private ApiClient apiClient = Configuration.getDefaultApiClient();

    private TaskApi taskApi;
    private FormApi formApi;

    public Task unclaim(String taskId) throws TaskListException {
        try {
            reconnectEventually();
            return ConverterUtils.toTask(taskApi.unassignTask(taskId), null);
        } catch (TaskListException | ApiException e) {
            throw new TaskListException("Error unclaiming task " + taskId, e);
        }
    }

    public Task claim(String taskId, String assignee) throws TaskListException {
        return claim(taskId, assignee, false);
    }

    public Task claim(String taskId, String assignee, Boolean allowOverrideAssignment) throws TaskListException {
        try {
            reconnectEventually();
            return ConverterUtils.toTask(taskApi.assignTask(taskId,
                    new TaskAssignRequest().assignee(assignee).allowOverrideAssignment(allowOverrideAssignment)), null);
        } catch (TaskListException | ApiException e) {
            throw new TaskListException("Error assigning task " + taskId, e);
        }
    }

    public Task completeTask(String taskId, Map<String, Object> variablesMap) throws TaskListException {
        try {
            reconnectEventually();
            List<VariableInputDTO> variables = ConverterUtils.toVariableInput(variablesMap);
            return ConverterUtils.toTask(taskApi.completeTask(taskId, new TaskCompleteRequest().variables(variables)),
                    null);
        } catch (TaskListException | ApiException e) {
            throw new TaskListException("Error assigning task " + taskId, e);
        }
    }

    public TaskList getTasks(Boolean assigned, TaskState state, Integer pageSize) throws TaskListException {
        return getTasks(assigned, state, defaultShouldReturnVariables, new Pagination().setPageSize(pageSize));
    }

    public TaskList getTasks(Boolean assigned, TaskState state, Pagination pagination) throws TaskListException {
        return getTasks(assigned, state, defaultShouldReturnVariables, pagination);
    }

    public TaskList getTasks(Boolean assigned, TaskState state, boolean withVariables, Integer pageSize)
            throws TaskListException {
        return getTasks(null, assigned, null, state, withVariables, new Pagination().setPageSize(pageSize));
    }

    public TaskList getTasks(Boolean assigned, TaskState state, boolean withVariables, Pagination pagination)
            throws TaskListException {
        return getTasks(null, assigned, null, state, withVariables, pagination);
    }

    public TaskList getAssigneeTasks(String assigneeId, TaskState state, Integer pageSize) throws TaskListException {
        return getAssigneeTasks(assigneeId, state, defaultShouldReturnVariables,
                new Pagination().setPageSize(pageSize));
    }

    public TaskList getAssigneeTasks(String assigneeId, TaskState state, Pagination pagination)
            throws TaskListException {
        return getAssigneeTasks(assigneeId, state, defaultShouldReturnVariables, pagination);
    }

    public TaskList getAssigneeTasks(String assigneeId, TaskState state, boolean withVariables, Integer pageSize)
            throws TaskListException {
        return getTasks(null, true, assigneeId, state, withVariables, new Pagination().setPageSize(pageSize));
    }

    public TaskList getAssigneeTasks(String assigneeId, TaskState state, boolean withVariables, Pagination pagination)
            throws TaskListException {
        return getTasks(null, true, assigneeId, state, withVariables, pagination);
    }

    public TaskList getGroupTasks(String group, TaskState state, Integer pageSize) throws TaskListException {
        return getGroupTasks(group, state, defaultShouldReturnVariables, new Pagination().setPageSize(pageSize));
    }

    public TaskList getGroupTasks(String group, TaskState state, Pagination pagination) throws TaskListException {
        return getGroupTasks(group, state, defaultShouldReturnVariables, pagination);
    }

    public TaskList getGroupTasks(String group, TaskState state, boolean withVariables, Integer pageSize)
            throws TaskListException {
        return getTasks(group, null, null, state, withVariables, new Pagination().setPageSize(pageSize));
    }

    public TaskList getGroupTasks(String group, TaskState state, boolean withVariables, Pagination pagination)
            throws TaskListException {
        return getTasks(group, null, null, state, withVariables, pagination);
    }

    public Task getTask(String taskId) throws TaskListException {
        return getTask(taskId, defaultShouldReturnVariables);
    }

    public Task getTask(String taskId, boolean withVariables) throws TaskListException {

        try {
            reconnectEventually();
            List<VariableSearchResponse> variables = null;
            if (!withVariables) {
                variables = taskApi.searchTaskVariables(taskId, new VariablesSearchRequest());
            }

            return ConverterUtils.toTask(taskApi.getTaskById(taskId), variables);
        } catch (TaskListException | ApiException e) {
            throw new TaskListException("Error reading task " + taskId, e);
        }
    }

    public List<Variable> getVariables(String taskId) throws TaskListException {

        try {

            reconnectEventually();
            return ConverterUtils.toVariables(taskApi.searchTaskVariables(taskId, new VariablesSearchRequest()));
        } catch (ApiException e) {
            throw new TaskListException("Error reading task " + taskId, e);
        }
    }

    public Form getForm(String formId, String processDefinitionId) throws TaskListException {
        try {
            if (formId.startsWith("camunda-forms:bpmn:")) {
                formId = formId.substring(19, formId.length());
            }
            return ConverterUtils.toForm(formApi.getForm(formId, processDefinitionId));
        } catch (ApiException e) {
            throw new TaskListException("Error reading form " + formId, e);
        }
    }

    public TaskList before(TaskList taskList) throws TaskListException {
        return paginate(taskList, SearchType.BEFORE);
    }

    public TaskList beforeOrEquals(TaskList taskList) throws TaskListException {
        return paginate(taskList, SearchType.BEFORE_OR_EQUAL);
    }

    public TaskList after(TaskList taskList) throws TaskListException {
        return paginate(taskList, SearchType.AFTER);
    }

    public TaskList afterOrEqual(TaskList taskList) throws TaskListException {
        return paginate(taskList, SearchType.AFTER_OR_EQUAL);
    }

    private TaskList paginate(TaskList taskList, SearchType direction) throws TaskListException {
        if (taskList.getSearch().getPagination() == null
                || taskList.getSearch().getPagination().getPageSize() == null) {
            throw new TaskListException("Before/After/AfterOrEquals search are only possible if a pageSize is set");
        }
        if (taskList.getItems() == null || taskList.getItems().isEmpty()) {
            throw new TaskListException(
                    "Before/After/AfterOrEquals search are only possible if some items are present");
        }
        TaskSearch newSearch = new TaskSearch().setAssigned(taskList.getSearch().getAssigned())
                .setAssignee(taskList.getSearch().getAssignee()).setGroup(taskList.getSearch().getGroup())
                .setState(taskList.getSearch().getState()).setWithVariables(taskList.getSearch().isWithVariables())
                .setPagination(getSearchPagination(taskList, direction));

        return getTasks(newSearch);
    }

    private Pagination getSearchPagination(TaskList taskList, SearchType type) {
        switch (type) {
        case BEFORE:
            return new Pagination.Builder().pageSize(taskList.getSearch().getPagination().getPageSize())
                    .before(taskList.first().getSortValues()).build();
        case BEFORE_OR_EQUAL:
            return new Pagination.Builder().pageSize(taskList.getSearch().getPagination().getPageSize())
                    .beforeOrEqual(taskList.first().getSortValues()).build();
        case AFTER:
            return new Pagination.Builder().pageSize(taskList.getSearch().getPagination().getPageSize())
                    .after(taskList.last().getSortValues()).build();
        default:
            return new Pagination.Builder().pageSize(taskList.getSearch().getPagination().getPageSize())
                    .afterOrEqual(taskList.last().getSortValues()).build();
        }
    }

    public TaskList getTasks(TaskSearch search) throws TaskListException {
        return getTasks(search.getCandidateUser(), search.getGroup(), search.getAssigned(), search.getAssignee(),
                search.getState(), search.getFollowUpDate(), search.getDueDate(), search.getProcessDefinitionKey(),
                search.getProcessInstanceKey(), search.getTaskDefinitionId(), search.getTenantIds(), search.isWithVariables(),
                search.getPagination());
    }

    public TaskList getTasks(String group, Boolean assigned, String assigneeId, TaskState state, boolean withVariables,
            Pagination pagination) throws TaskListException {
        return getTasks(null, group, assigned, assigneeId, state, null, null, null, null, null, null, withVariables,
                pagination);
    }

    public TaskList getTasks(String candidateUser, String group, Boolean assigned, String assignee, TaskState state,
            DateFilter followUpDate, DateFilter dueDate, String processDefinitionId, String processInstanceId,
            String taskDefinitionId, List<String> tenantIds, boolean withVariables, Pagination pagination) throws TaskListException {

        TaskSearchRequest search = new TaskSearchRequest().candidateGroup(group).candidateUser(candidateUser)
                .assignee(assignee).state(ConverterUtils.toSearchState(state))
                .followUpDate(ConverterUtils.toSearchDateFilter(followUpDate))
                .dueDate(ConverterUtils.toSearchDateFilter(dueDate)).processDefinitionKey(processDefinitionId)
                .processInstanceKey(processInstanceId).taskDefinitionId(taskDefinitionId).tenantIds(tenantIds);
        if (pagination != null) {
            if (pagination.getSearchType() != null && pagination.getSearch() != null
                    && !pagination.getSearch().isEmpty()) {
                if (pagination.getSearchType().equals(SearchType.BEFORE)) {
                    search.searchBefore(pagination.getSearch());
                } else if (pagination.getSearchType().equals(SearchType.BEFORE_OR_EQUAL)) {
                    search.searchBeforeOrEqual(pagination.getSearch());
                } else if (pagination.getSearchType().equals(SearchType.AFTER)) {
                    search.searchAfter(pagination.getSearch());
                } else if (pagination.getSearchType().equals(SearchType.AFTER_OR_EQUAL)) {
                    search.searchAfterOrEqual(pagination.getSearch());
                }
            }
            search.pageSize(pagination.getPageSize());
            search.sort(pagination.getSort());
        }
        return new TaskList().setItems(getTasks(search, withVariables))
                .setSearch(new TaskSearch().setCandidateUser(candidateUser).setAssigned(assigned).setAssignee(assignee)
                        .setGroup(group).setProcessDefinitionKey(processDefinitionId)
                        .setProcessInstanceKey(processInstanceId).setFollowUpDate(followUpDate).setDueDate(dueDate)
                        .setTaskDefinitionId(taskDefinitionId).setState(state).setWithVariables(withVariables)
                        .setPagination(pagination));

    }

    public List<Task> getTasks(TaskSearchRequest search, boolean withVariables) throws TaskListException {
        try {
            reconnectEventually();

            List<Task> tasks = ConverterUtils.toTasks(taskApi.searchTasks(search));
            if (withVariables) {
                loadVariables(tasks);
            }
            return tasks;
        } catch (ApiException e) {
            throw new TaskListException("Error searching tasks", e);
        }
    }
    
    public void loadVariables(List<Task> tasks) throws TaskListException {
        try {
        Map<String, Future<List<Variable>>> futures = new HashMap<>();
        Map<String, Task> taskMap = new HashMap<>();
        for(Task task : tasks) {
            taskMap.put(task.getId(), task);
            futures.put(task.getId(), CompletableFuture.supplyAsync(() -> {
                try {
                    return getVariables(task.getId());
                } catch (TaskListException e) {
                    return null;
                }
            }));
        }
        for(Map.Entry<String, Future<List<Variable>>> varFutures : futures.entrySet()) {
            taskMap.get(varFutures.getKey()).setVariables(varFutures.getValue().get());
        }
        futures.clear();
        taskMap.clear();
        } catch(ExecutionException | InterruptedException e) {
            throw new TaskListException("Error loading task variables", e);
        }
    }

    public String getTaskListUrl() {
        return taskListUrl;
    }

    public void setTokenExpiration(int tokenExpiration) {
        this.tokenExpiration = tokenExpiration;
    }

    private void reconnectEventually() throws TaskListException {
        if (this.tokenExpiration > 0 && this.tokenExpiration < (System.currentTimeMillis() / 1000 - 3)) {
            authentication.authenticate(this);
        }
    }

    public void setAuthCookie(String cookie) {
        this.apiClient.setRequestInterceptor(new Consumer<HttpRequest.Builder>() {
            @Override
            public void accept(HttpRequest.Builder builder) {
                builder.header("cookie", cookie);
            }
        });
        this.taskApi = new TaskApi(this.apiClient);
        this.formApi = new FormApi(this.apiClient);
    }

    public void setBearerToken(String token) {
        this.apiClient.setRequestInterceptor(new Consumer<HttpRequest.Builder>() {
            @Override
            public void accept(HttpRequest.Builder builder) {
                builder.header("Authorization", "Bearer " + token);
            }
        });
        this.taskApi = new TaskApi(this.apiClient);
        this.formApi = new FormApi(this.apiClient);
    }

    public static class Builder {

        private AuthInterface authentication;

        private String taskListUrl;

        private boolean defaultShouldReturnVariables = false;

        public Builder() {

        }

        public Builder authentication(AuthInterface authentication) {
            this.authentication = authentication;
            return this;
        }

        public Builder taskListUrl(String taskListUrl) {
            this.taskListUrl = taskListUrl;
            return this;
        }

        /**
         * Default behaviour will be to get variables along with tasks. Default value is
         * false. Can also be overwritten in the getTasks methods
         * 
         * @return the builder
         */
        public Builder shouldReturnVariables() {
            this.defaultShouldReturnVariables = true;
            return this;
        }

        public CamundaTaskListClient build() throws TaskListException {
            CamundaTaskListClient client = new CamundaTaskListClient();
            client.authentication = authentication;
            client.taskListUrl = taskListUrl;
            client.defaultShouldReturnVariables = defaultShouldReturnVariables;

            client.apiClient.updateBaseUri(formatUrl(taskListUrl));

            client.taskApi = new TaskApi(client.apiClient);
            client.formApi = new FormApi(client.apiClient);

            authentication.authenticate(client);
            return client;
        }

        private String formatUrl(String url) {
            if (url.endsWith("/")) {
                return url.substring(0, url.length() - 1);
            }
            return url;
        }
    }
}
