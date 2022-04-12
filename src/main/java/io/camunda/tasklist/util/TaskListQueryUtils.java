package io.camunda.tasklist.util;

import java.util.Map;

public class TaskListQueryUtils {
	
	private TaskListQueryUtils() {}
	
	public static String getUnclaimQuery(String taskId) {
		return "{\"operationName\":\"UnclaimTask\",\"variables\":{\"id\":\""+taskId+"\"},\"query\":\"mutation UnclaimTask($id: String!) {\\n  unclaimTask(taskId: $id) {\\n    id\\n    assignee\\n    __typename\\n  }\\n}\"}";
	}
	
	public static String getClaimQuery(String taskId) {
		return "{\"operationName\":\"ClaimTask\",\"variables\":{\"id\":\""+taskId+"\"},\"query\":\"mutation ClaimTask($id: String!) {\\n  claimTask(taskId: $id) {\\n    id\\n    assignee\\n    __typename\\n  }\\n}\"}";
	}
	
	public static String getCompleteTaskQuery(String taskId, Map<String, Object> variablesMap) {
		StringBuilder variables = new StringBuilder("[");
		for(Map.Entry<String, Object> entry : variablesMap.entrySet()) {
			variables.append("{\"name\":\"").append(entry.getKey()).append("\", \"value\":\"");
				if (entry.getValue() instanceof String) {
					variables.append("\\\"").append((String) entry.getValue()).append("\\\"");
				} else {
					variables.append((String) entry.getValue());
				}
				variables.append("\"}");
		}
		variables.append("]");
		return "{\"operationName\":\"CompleteTask\",\"variables\":{\"id\":\""+taskId+"\",\"variables\":"+variables.toString()+"},\"query\":\"mutation CompleteTask($id: String!, $variables: [VariableInput!]!) {\\n  completeTask(taskId: $id, variables: $variables) {\\n    id\\n    taskState\\n    variables {\\n      name\\n      value\\n      __typename\\n    }\\n    completionTime\\n    __typename\\n  }\\n}\"}";
	}
	
	public static String getTaskGraphQLQuery(Boolean assigned, String assigneeId, Boolean variables, Integer pageSize) {
		String assigneeFilter = ""; 
		if (assigneeId!=null && assigneeId.length()>0) {
			assigneeFilter = ", \"assignee\":\""+assigneeId+"\"";
		}
		String assignedFilter = ""; 
		if (assigned!=null) {
			assignedFilter = ", \"assignee\":"+assigned.toString();
		}
		String pageSizeFilter = ""; 
		if (pageSize!=null) {
			pageSizeFilter = ", \"pageSize\":"+pageSize;
		}
		String variablesResult="";
		if (variables) {
			variablesResult="variables {\n      id\n      name\n      previewValue\n      isValueTruncated\n      __typename\n    }\n ";
		}
		String taskQuery = "{\"operationName\":\"GetTasks\",\"variables\":{\"state\":\"CREATED\""+
				pageSizeFilter+assignedFilter+assigneeFilter+
				"},\"query\":\"query GetTasks($assignee: String, $assigned: Boolean, $state: TaskState, $pageSize: Int, $searchAfter: [String!], $searchBefore: [String!], $searchAfterOrEqual: [String!]) {\\n  tasks(\\n    query: {assignee: $assignee, assigned: $assigned, state: $state, pageSize: $pageSize, searchAfter: $searchAfter, searchBefore: $searchBefore, searchAfterOrEqual: $searchAfterOrEqual}\\n  ) {\\n    id\\n    name\\n    processName\\n    assignee\\n    creationTime\\n    taskState\\n    sortValues\\n    isFirst\\n    __typename\\n  "+variablesResult+"}\\n}\"}";
		return taskQuery;
	}
	
}
