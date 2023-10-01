package io.camunda.tasklist;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles("saas")
public class TaskListClientTestSaas extends TaskListClientTest {
  @SpringBootApplication
  @Deployment(resources = "classpath:/bpmn/tasklistRestAPIUnitTestProcess.bpmn")
  public static class TaskListClientTestApp {
    public static void main(String[] args) {
      SpringApplication.run(TaskListClientTestApp.class, args);
    }
  }
}
