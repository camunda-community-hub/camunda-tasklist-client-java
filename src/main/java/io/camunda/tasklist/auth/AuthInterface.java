package io.camunda.tasklist.auth;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

import io.camunda.tasklist.exception.TaskListException;

public interface AuthInterface {
	public void manageAuth(HttpUriRequestBase request) throws TaskListException;
	public void manageAuth(HttpUriRequestBase request, boolean forceRefresh) throws TaskListException;
	
}
