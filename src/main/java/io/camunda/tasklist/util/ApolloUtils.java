package io.camunda.tasklist.util;

import java.util.ArrayList;
import java.util.List;

import com.apollographql.apollo3.api.Optional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.exception.TaskListException;

public class ApolloUtils {

	private static ObjectMapper objectMapper = null;
	
	private ApolloUtils() {}
	
	public static Optional<String> optional(String value) {
		return value==null ? null : new Optional.Present<String>(value);
	}
	public static Optional<Boolean> optional(Boolean value) {
		return value==null ? null : new Optional.Present<Boolean>(value);
	}
	public static Optional<Integer> optional(Integer value) {
		return value==null ? null : new Optional.Present<Integer>(value);
	}
	public static Optional<io.camunda.tasklist.client.type.TaskState> optional(TaskState value) {
		return value==null ? null : new Optional.Present<io.camunda.tasklist.client.type.TaskState>(io.camunda.tasklist.client.type.TaskState.safeValueOf(value.getRawValue()));
	}
	
	public static Task toTask(Object apolloTask) throws TaskListException {
		try {
			return getObjectMapper().readValue(getObjectMapper().writeValueAsString(apolloTask), Task.class);
		} catch ( JsonProcessingException e) {
			throw new TaskListException(e);
		}
	}
	
	public static List<Task> toTasks(List<?> apolloTasks) throws TaskListException {
		List<Task> result = new ArrayList<>();
		for(Object apolloTask : apolloTasks) {
			result.add(toTask(apolloTask));
		}
		return result;
	}
	
	private static ObjectMapper getObjectMapper() {
		if (objectMapper==null) {
			objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}
		return objectMapper;
	}
}
