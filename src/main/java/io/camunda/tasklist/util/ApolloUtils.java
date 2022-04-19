package io.camunda.tasklist.util;

import com.apollographql.apollo3.api.Optional;

public class ApolloUtils {

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
}
