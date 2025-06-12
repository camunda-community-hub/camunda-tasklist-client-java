package io.camunda.tasklist.spring;

import io.camunda.client.CamundaClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestApp {
  public static void main(String[] args) {
    SpringApplication.run(TestApp.class, args);
  }

  @Bean
  public CamundaClient camundaClient() {
    return CamundaClient.newClient();
  }
}
