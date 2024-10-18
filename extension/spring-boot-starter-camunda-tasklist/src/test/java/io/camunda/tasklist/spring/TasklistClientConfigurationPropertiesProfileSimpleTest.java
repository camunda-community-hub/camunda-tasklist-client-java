package io.camunda.tasklist.spring;

import static io.camunda.tasklist.spring.TasklistClientConfigurationProperties.Profile.*;
import static org.assertj.core.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"tasklist.client.profile=simple"})
public class TasklistClientConfigurationPropertiesProfileSimpleTest {
  @Autowired TasklistClientConfigurationProperties properties;

  @Test
  void shouldApplyProfiles() throws MalformedURLException {
    assertThat(properties.profile()).isEqualTo(simple);
    assertThat(properties.username()).isEqualTo("demo");
    assertThat(properties.password()).isEqualTo("demo");
    assertThat(properties.baseUrl()).isEqualTo(URI.create("http://localhost:8082").toURL());
    assertThat(properties.enabled()).isEqualTo(true);
    assertThat(properties.sessionTimeout()).isEqualTo(Duration.ofMinutes(10));
  }
}
