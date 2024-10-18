package io.camunda.tasklist.spring;

import static io.camunda.tasklist.spring.TasklistClientConfigurationProperties.Profile.*;
import static org.assertj.core.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
      "tasklist.client.profile=saas",
      "tasklist.client.cluster-id=abc",
      "tasklist.client.region=bru-2",
      "tasklist.client.client-id=def",
      "tasklist.client.client-secret=ghi"
    })
public class TasklistClientConfigurationPropertiesProfileSaasTest {
  @Autowired TasklistClientConfigurationProperties properties;

  @Test
  void shouldApplyProfiles() throws MalformedURLException {
    assertThat(properties.profile()).isEqualTo(saas);
    assertThat(properties.clientId()).isEqualTo("def");
    assertThat(properties.clientSecret()).isEqualTo("ghi");
    assertThat(properties.baseUrl())
        .isEqualTo(URI.create("https://bru-2.tasklist.camunda.io/abc").toURL());
    assertThat(properties.enabled()).isEqualTo(true);
    assertThat(properties.authUrl())
        .isEqualTo(URI.create("https://login.cloud.camunda.io/oauth/token").toURL());
  }
}
