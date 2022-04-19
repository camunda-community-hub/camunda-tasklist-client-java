package io.camunda.tasklist.auth;

import com.apollographql.apollo3.api.http.HttpHeader;

import io.camunda.tasklist.exception.TaskListException;

public interface AuthInterface {
	public HttpHeader getHeader() throws TaskListException;
}
