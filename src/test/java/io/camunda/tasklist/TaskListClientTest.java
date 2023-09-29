package io.camunda.tasklist;

import io.camunda.tasklist.auth.JWTAuthentication;
import io.camunda.tasklist.exception.TaskListException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("saas")
public class TaskListClientTest {

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

  @SpringBootApplication
  @PropertySource("classpath:application-saas.yaml")
  public static class TaskListClientTestApp {

    public static void main(String[] args) {
      SpringApplication.run(TaskListClientTestApp.class, args);
    }

  }

}
