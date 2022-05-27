package io.camunda.tasklist;

import java.util.ArrayList;
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
import io.camunda.tasklist.client.ClaimTaskMutation;
import io.camunda.tasklist.client.CompleteTaskMutation;
import io.camunda.tasklist.client.GetTasksQuery;
import io.camunda.tasklist.client.GetTasksWithVariableQuery;
import io.camunda.tasklist.client.UnclaimTaskMutation;
import io.camunda.tasklist.client.type.VariableInput;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.tasklist.util.ApolloUtils;

public class CamundaTaskListClient {

    private AuthInterface authentication;

    private ApolloClient apolloClient;

    public Task unclaim(String taskId) throws TaskListException {
        ApolloCall<UnclaimTaskMutation.Data> unclaimCall = apolloClient.mutation(new UnclaimTaskMutation(taskId));
        ApolloResponse<UnclaimTaskMutation.Data> response = execute(unclaimCall);
        return ApolloUtils.toTask(response.data.unclaimTask);
    }

    public Task claim(String taskId) throws TaskListException {
        ApolloCall<ClaimTaskMutation.Data> claimCall = apolloClient.mutation(new ClaimTaskMutation(taskId));
        ApolloResponse<ClaimTaskMutation.Data> response = execute(claimCall);
        return ApolloUtils.toTask(response.data.claimTask);

    }

    public Task completeTask(String taskId, Map<String, Object> variablesMap) throws TaskListException {

        ApolloCall<CompleteTaskMutation.Data> completeTaskCall = apolloClient
                .mutation(new CompleteTaskMutation(taskId, toVariableInput(variablesMap)));
        ApolloResponse<CompleteTaskMutation.Data> response = execute(completeTaskCall);
        return ApolloUtils.toTask(response.data.completeTask);
    }

    public List<Task> getTasks(Boolean assigned, String assigneeId, TaskState state, Integer pageSize)
            throws TaskListException {

        Optional<String> optAssignee = ApolloUtils.optional(assigneeId);
        Optional<Boolean> optAssigned = ApolloUtils.optional(assigned);
        Optional<Integer> optPageSize = ApolloUtils.optional(pageSize);
        Optional<io.camunda.tasklist.client.type.TaskState> optState = ApolloUtils.optional(state);

        ApolloCall<GetTasksQuery.Data> queryCall = apolloClient
                .query(new GetTasksQuery(optAssignee, optAssigned, optState, optPageSize, null, null, null));
        ApolloResponse<GetTasksQuery.Data> response = execute(queryCall);

        return ApolloUtils.toTasks(response.data.tasks);
    }

    public List<Task> getTasksWithVariables(Boolean assigned, String assigneeId, TaskState state, Integer pageSize)
            throws TaskListException {

        Optional<String> optAssignee = ApolloUtils.optional(assigneeId);
        Optional<Boolean> optAssigned = ApolloUtils.optional(assigned);
        Optional<Integer> optPageSize = ApolloUtils.optional(pageSize);
        Optional<io.camunda.tasklist.client.type.TaskState> optState = ApolloUtils.optional(state);

        ApolloCall<GetTasksWithVariableQuery.Data> queryCall = apolloClient.query(
                new GetTasksWithVariableQuery(optAssignee, optAssigned, optState, optPageSize, null, null, null));
        ApolloResponse<GetTasksWithVariableQuery.Data> response = execute(queryCall);

        return ApolloUtils.toTasks(response.data.tasks);
    }

    private <T extends Operation.Data> ApolloResponse<T> execute(ApolloCall<T> call) throws TaskListException {
        ApolloResponse<T> result = null;
        try {
            result = Rx3Apollo.single(call).blockingGet();
        } catch (ApolloHttpException e) {
            if (e.getStatusCode() == 401) {
                authentication.authenticate(apolloClient);
                try {
                    result = Rx3Apollo.single(call).blockingGet();
                } catch (Exception e2) {
                    throw new TaskListException(e2);
                }
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

    private List<VariableInput> toVariableInput(Map<String, Object> variablesMap) {
        List<VariableInput> variables = new ArrayList<>();
        for (Map.Entry<String, Object> entry : variablesMap.entrySet()) {
            if (entry.getValue() instanceof String) {
                variables.add(new VariableInput(entry.getKey(), '"' + String.valueOf(entry.getValue()) + '"'));
            } else {
                variables.add(new VariableInput(entry.getKey(), String.valueOf(entry.getValue())));
            }
        }
        return variables;
    }

    public static class Builder {

        private AuthInterface authentication;

        private String taskListUrl;

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

        public CamundaTaskListClient build() throws TaskListException {
            CamundaTaskListClient client = new CamundaTaskListClient();
            client.authentication = authentication;
            client.apolloClient = new ApolloClient.Builder().httpServerUrl(taskListUrl + "/graphql")
                    .addHttpHeader("", "").build();
            authentication.authenticate(client.apolloClient);
            return client;
        }
    }
}
