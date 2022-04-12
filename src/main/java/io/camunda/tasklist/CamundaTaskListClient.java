package io.camunda.tasklist;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.tasklist.auth.AuthInterface;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.tasklist.util.TaskListQueryUtils;

public class CamundaTaskListClient {

	private AuthInterface authentication;
	
	private String taskListUrl;
	
	private ObjectMapper mapper;
	
	private JsonNode toJsonNode(InputStream is) throws IOException {
		if (mapper==null) {
			mapper = new ObjectMapper();
		}
		return mapper.readTree(is);
	}
	
	public JsonNode unclaim(String taskId) throws TaskListException {
		return executeQuery(TaskListQueryUtils.getUnclaimQuery(taskId));
	}
	
	public JsonNode claim(String taskId) throws TaskListException {
		return executeQuery(TaskListQueryUtils.getClaimQuery(taskId));
	}

	public JsonNode completeTask(String taskId, Map<String, Object> variablesMap) throws TaskListException {
		return executeQuery(TaskListQueryUtils.getCompleteTaskQuery(taskId, variablesMap));
	}
	
	public JsonNode getTasks(Boolean assigned, String assigneeId, Boolean variables, Integer pageSize) throws TaskListException {
		String taskQuery = TaskListQueryUtils.getTaskGraphQLQuery(assigned, assigneeId, variables, pageSize);

		return executeQuery(taskQuery);
	}
	
	private JsonNode executeQuery(String query) throws TaskListException {
		HttpPost httpPost = new HttpPost(taskListUrl+"/graphql");
		authentication.manageAuth(httpPost);
		StringEntity queryParam = new StringEntity(query);
		httpPost.setEntity(queryParam);
		
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
	        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
	        	if (response.getCode()==200) {
		        	InputStream is = response.getEntity().getContent();
		        	return toJsonNode(is);
	        	}
	        	if (response.getCode()==401) {
	        		//force reauthentication
	        		authentication.manageAuth(httpPost, true);
	        		return executeQuery(query);
	        	}
	        	throw new TaskListException("Unexpected reponse from tasklist services with code "+response.getCode());
	        }
	    } catch (IOException e) {
	    	throw new TaskListException(e);
		} 
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
		
		public CamundaTaskListClient build() {
			CamundaTaskListClient client =  new CamundaTaskListClient();
			client.authentication = authentication;
			client.taskListUrl = taskListUrl;
			client.mapper = new ObjectMapper();
			return client;
		}
	}
}
