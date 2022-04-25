package io.camunda.tasklist.auth;

import com.apollographql.apollo3.ApolloClient;

import io.camunda.tasklist.exception.TaskListException;

public interface AuthInterface {
    public void authenticate(ApolloClient client) throws TaskListException;
}
