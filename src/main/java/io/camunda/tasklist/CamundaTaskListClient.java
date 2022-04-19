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
import com.apollographql.apollo3.api.http.HttpHeader;
import com.apollographql.apollo3.exception.ApolloHttpException;
import com.apollographql.apollo3.rx3.Rx3Apollo;

import io.camunda.tasklist.auth.AuthInterface;
import io.camunda.tasklist.client.ClaimTaskMutation;
import io.camunda.tasklist.client.ClaimTaskMutation.ClaimTask;
import io.camunda.tasklist.client.CompleteTaskMutation;
import io.camunda.tasklist.client.CompleteTaskMutation.CompleteTask;
import io.camunda.tasklist.client.GetTasksQuery;
import io.camunda.tasklist.client.GetTasksQuery.Task;
import io.camunda.tasklist.client.UnclaimTaskMutation;
import io.camunda.tasklist.client.UnclaimTaskMutation.UnclaimTask;
import io.camunda.tasklist.client.type.VariableInput;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.tasklist.util.ApolloUtils;

public class CamundaTaskListClient {

	private AuthInterface authentication;

	private ApolloClient client;

	public UnclaimTask unclaim(String taskId) throws TaskListException {
		ApolloCall<UnclaimTaskMutation.Data> unclaimCall = client.mutation(new UnclaimTaskMutation(taskId));
		ApolloResponse<UnclaimTaskMutation.Data> response = execute(unclaimCall);
		return response.data.unclaimTask;
	}

	public ClaimTask claim(String taskId) throws TaskListException {
		ApolloCall<ClaimTaskMutation.Data> claimCall = client.mutation(new ClaimTaskMutation(taskId));
		ApolloResponse<ClaimTaskMutation.Data> response = execute(claimCall);
		return response.data.claimTask;

	}

	public CompleteTask completeTask(String taskId, Map<String, Object> variablesMap) throws TaskListException {

		ApolloCall<CompleteTaskMutation.Data> completeTaskCall = client.mutation(new CompleteTaskMutation(taskId, toVariableInput(variablesMap)));
		ApolloResponse<CompleteTaskMutation.Data> response = execute(completeTaskCall);
		return response.data.completeTask;
	}

	public List<Task> getTasks(Boolean assigned, String assigneeId, Boolean variables, Integer pageSize) throws TaskListException {

		Optional<String> optAssignee = ApolloUtils.optional(assigneeId);
		Optional<Boolean> optAssigned = ApolloUtils.optional(assigned);
		Optional<Integer> optPageSize = ApolloUtils.optional(pageSize);

		ApolloCall<GetTasksQuery.Data> queryCall = client.query(new GetTasksQuery(optAssignee, optAssigned, null, optPageSize, null, null, null));
		ApolloResponse<GetTasksQuery.Data> response = execute(queryCall);

		return response.data.tasks;
	}


	private <T extends Operation.Data> ApolloResponse<T> execute(ApolloCall<T> call) throws TaskListException {
		ApolloResponse<T> result = null;
		try {
			result = Rx3Apollo.single(call).blockingGet();
		} catch (ApolloHttpException e) {
			if (e.getStatusCode() == 401) {
				client.getHttpHeaders().clear();
				client.getHttpHeaders().add(authentication.getHeader());
				try {
					result = Rx3Apollo.single(call).blockingGet();
				} catch (Exception e2) {
					throw new TaskListException(e2);
				}
			}
		}
		if (result==null) {
			return null;
		}
		if (result.hasErrors()) {
			String errorString = result.errors.stream()
					.map( e -> e.toString() )
					.collect( Collectors.joining( "," ) );
			throw new TaskListException(errorString);
		}
		return result;
	}

	private List<VariableInput> toVariableInput(Map<String, Object> variablesMap) {
		List<VariableInput> variables = new ArrayList<>();
		for(Map.Entry<String, Object> entry : variablesMap.entrySet()) {
			if (entry.getValue() instanceof String) {
				variables.add(new VariableInput(entry.getKey(), '"'+String.valueOf(entry.getValue())+'"'));
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
			CamundaTaskListClient client =  new CamundaTaskListClient();
			client.authentication = authentication;
			HttpHeader header = authentication.getHeader();
			client.client = new ApolloClient.Builder().httpServerUrl(taskListUrl+"/graphql").addHttpHeader(header.getName(), header.getValue()).build();      
			return client;
		}
	}
}
