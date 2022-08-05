package io.camunda.tasklist;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.apollographql.apollo3.ApolloCall;
import com.apollographql.apollo3.ApolloClient;
import com.apollographql.apollo3.api.ApolloResponse;
import com.apollographql.apollo3.api.Operation;
import com.apollographql.apollo3.api.Optional;
import com.apollographql.apollo3.exception.ApolloHttpException;
import com.apollographql.apollo3.rx3.Rx3Apollo;

import io.camunda.tasklist.auth.AuthInterface;
import io.camunda.tasklist.dto.Form;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.tasklist.util.ApolloUtils;
import io.generated.tasklist.client.*;

public class CamundaTaskListClient {

    private AuthInterface authentication;

    private ApolloClient apolloClient;

    private String taskListUrl;

    private boolean defaultShouldReturnVariables;
    
    private int tokenExpiration;

    public Task unclaim(String taskId) throws TaskListException {
        ApolloCall<UnclaimTaskMutation.Data> unclaimCall = apolloClient.mutation(new UnclaimTaskMutation(taskId));
        ApolloResponse<UnclaimTaskMutation.Data> response = execute(unclaimCall);
        return ApolloUtils.toTask(response.data.unclaimTask);
    }

    public Task claim(String taskId, String assignee) throws TaskListException {
        ApolloCall<ClaimTaskMutation.Data> claimCall = apolloClient.mutation(new ClaimTaskMutation(taskId, assignee));
        ApolloResponse<ClaimTaskMutation.Data> response = execute(claimCall);
        return ApolloUtils.toTask(response.data.claimTask);

    }

    public Task completeTask(String taskId, Map<String, Object> variablesMap) throws TaskListException {

        ApolloCall<CompleteTaskMutation.Data> completeTaskCall = apolloClient
                .mutation(new CompleteTaskMutation(taskId, ApolloUtils.toVariableInput(variablesMap)));
        ApolloResponse<CompleteTaskMutation.Data> response = execute(completeTaskCall);
        return ApolloUtils.toTask(response.data.completeTask);
    }

    public List<Task> getTasks(Boolean assigned, TaskState state, Integer pageSize) throws TaskListException {
        return getTasks(assigned, state, pageSize, defaultShouldReturnVariables);
    }

    public List<Task> getTasks(Boolean assigned, TaskState state, Integer pageSize, boolean getVariables)
            throws TaskListException {
        return getTasks(null, assigned, null, state, pageSize, getVariables);
    }

    public List<Task> getAssigneeTasks(String assigneeId, TaskState state, Integer pageSize) throws TaskListException {
        return getAssigneeTasks(assigneeId, state, pageSize, defaultShouldReturnVariables);
    }

    public List<Task> getAssigneeTasks(String assigneeId, TaskState state, Integer pageSize, boolean getVariables)
            throws TaskListException {
        return getTasks(null, true, assigneeId, state, pageSize, getVariables);
    }

    public List<Task> getGroupTasks(String group, TaskState state, Integer pageSize) throws TaskListException {
        return getGroupTasks(group, state, pageSize, defaultShouldReturnVariables);
    }

    public List<Task> getGroupTasks(String group, TaskState state, Integer pageSize, boolean getVariables)
            throws TaskListException {
        return getTasks(group, null, null, state, pageSize, getVariables);
    }

    public Task getTask(String taskId) throws TaskListException {
       return getTask(taskId, defaultShouldReturnVariables);
    }

    public Task getTask(String taskId, boolean getVariables) throws TaskListException {
        if (!getVariables) {
            ApolloCall<GetTaskQuery.Data> queryCall = apolloClient.query(new GetTaskQuery(taskId));
            ApolloResponse<GetTaskQuery.Data> response = execute(queryCall);
            return ApolloUtils.toTask(response.data.task);
        }
        ApolloCall<GetTaskWithVariablesQuery.Data> queryCall = apolloClient.query(new GetTaskWithVariablesQuery(taskId));
        ApolloResponse<GetTaskWithVariablesQuery.Data> response = execute(queryCall);
        return ApolloUtils.toTask(response.data.task);
    }

    public Form getForm(String formId, String processDefinitionId) throws TaskListException {
        ApolloCall<GetFormQuery.Data> queryCall = apolloClient.query(new GetFormQuery(formId, processDefinitionId));
        ApolloResponse<GetFormQuery.Data> response = execute(queryCall);
        return ApolloUtils.toForm(response.data.form);
    }

    public List<Task> getTasks(String group, Boolean assigned, String assigneeId, TaskState state, Integer pageSize,
                               boolean getVariables) throws TaskListException {

        Optional<String> optGroup = ApolloUtils.optional(group);
        Optional<String> optAssignee = ApolloUtils.optional(assigneeId);
        Optional<Boolean> optAssigned = ApolloUtils.optional(assigned);
        Optional<Integer> optPageSize = ApolloUtils.optional(pageSize);
        Optional<io.generated.tasklist.client.type.TaskState> optState = ApolloUtils.optional(state);

        if (!getVariables) {
            ApolloCall<GetTasksQuery.Data> queryCall = apolloClient.query(
                    new GetTasksQuery(optGroup, optAssignee, optAssigned, optState, optPageSize, null, null, null));
            ApolloResponse<GetTasksQuery.Data> response = execute(queryCall);

            return ApolloUtils.toTasks(response.data.tasks);
        }
        ApolloCall<GetTasksWithVariableQuery.Data> queryCall = apolloClient.query(new GetTasksWithVariableQuery(
                optGroup, optAssignee, optAssigned, optState, optPageSize, null, null, null));
        ApolloResponse<GetTasksWithVariableQuery.Data> response = execute(queryCall);

        return ApolloUtils.toTasks(response.data.tasks);
    }

    private <T extends Operation.Data> ApolloResponse<T> execute(ApolloCall<T> call) throws TaskListException {
        reconnectEventually();
        ApolloResponse<T> result = null;
        try {
            result = Rx3Apollo.single(call).blockingGet();
        } catch (ApolloHttpException e) {
            if (e.getStatusCode() == 401) {
                authentication.authenticate(this);
                try {
                    result = Rx3Apollo.single(call).blockingGet();
                } catch (Exception e2) {
                    throw new TaskListException(e2);
                }
            } else {
                throw new TaskListException(e);
            }
        }
        if (result == null) {
            return null;
        }
        if (result.hasErrors()) {
            String errorString = result.errors.stream().map(e -> e.toString()).collect(Collectors.joining(","));
            throw new TaskListException(errorString);
        }
        return result;
    }

    public ApolloClient getApolloClient() {
        return apolloClient;
    }

    public String getTaskListUrl() {
        return taskListUrl;
    }

    public void setTokenExpiration(int tokenExpiration) {
        this.tokenExpiration = tokenExpiration;
    }

    private void reconnectEventually() throws TaskListException {
        if (this.tokenExpiration>0 && this.tokenExpiration<(System.currentTimeMillis()/1000+3)) {
            authentication.authenticate(this);
        }
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
            client.apolloClient = new ApolloClient.Builder().httpServerUrl(formatUrl(taskListUrl)).addHttpHeader("", "")
                    .build();
            authentication.authenticate(client);
            return client;
        }

        private String formatUrl(String url) {
            if (url.endsWith("/graphql")) {
                return url;
            }
            if (url.endsWith("/")) {
                return url + "graphql";
            }
            return url + "/graphql";
        }
    }
}
