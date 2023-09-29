package io.camunda.tasklist;

import io.camunda.tasklist.auth.JWTAuthentication;
import io.camunda.tasklist.exception.TaskListException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class TaskListClientTest {

  @Autowired
  JWTAuthentication jwtAuthentication;

  @Autowired
  TaskListRestClient taskListRestClient;

  @Test
  void contextLoads() {
  }

  @Test
  void authenticationTest() throws TaskListException {
    assertTrue(jwtAuthentication.authenticate(taskListRestClient));
  }

}
