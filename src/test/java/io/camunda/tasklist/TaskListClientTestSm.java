package io.camunda.tasklist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("sm")
public class TaskListClientTestSm extends TaskListClientTest {
  @SpringBootApplication
  public static class TaskListClientTestApp {
    public static void main(String[] args) {
      SpringApplication.run(TaskListClientTestApp.class, args);
    }

  }
}
