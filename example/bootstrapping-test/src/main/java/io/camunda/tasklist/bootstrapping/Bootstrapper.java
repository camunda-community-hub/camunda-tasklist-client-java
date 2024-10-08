package io.camunda.tasklist.bootstrapping;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.auth.SimpleAuthentication;
import io.camunda.tasklist.auth.SimpleCredential;
import io.camunda.tasklist.exception.TaskListException;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;

public class Bootstrapper {
  public CamundaTaskListClient create() {
    try {
      return CamundaTaskListClient.builder()
          .taskListUrl("http://localhost:8082")
          .authentication(
              new SimpleAuthentication(
                  new SimpleCredential(
                      "demo",
                      "demo",
                      URI.create("http://localhost:8082").toURL(),
                      Duration.ofMinutes(10))))
          .build();
    } catch (TaskListException | MalformedURLException e) {
      throw new RuntimeException("Error while bootstrapping tasklist client", e);
    }
  }
}
