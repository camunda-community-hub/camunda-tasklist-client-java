package io.camunda.tasklist.bootstrapping;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.CamundaTaskListClientV1;
import io.camunda.tasklist.CamundaTasklistClientConfiguration;
import io.camunda.tasklist.CamundaTasklistClientConfiguration.DefaultProperties;
import io.camunda.tasklist.auth.SimpleAuthentication;
import io.camunda.tasklist.auth.SimpleCredential;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;

public class Bootstrapper {
  public CamundaTaskListClient create() {
    try {
      return new CamundaTaskListClientV1(
          new CamundaTasklistClientConfiguration(
              new SimpleAuthentication(
                  new SimpleCredential(
                      "demo",
                      "demo",
                      URI.create("http://localhost:8082").toURL(),
                      Duration.ofMinutes(10))),
              URI.create("http://localhost:8082").toURL(),
              null,
              new DefaultProperties(true, true, false)));
    } catch (MalformedURLException e) {
      throw new RuntimeException("Error while bootstrapping tasklist client", e);
    }
  }
}
