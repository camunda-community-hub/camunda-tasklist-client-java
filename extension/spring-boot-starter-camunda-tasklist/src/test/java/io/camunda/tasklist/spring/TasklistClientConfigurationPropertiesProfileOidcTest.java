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
      "tasklist.client.profile=oidc",
      "tasklist.client.client-id=def",
      "tasklist.client.client-secret=ghi"
    })
public class TasklistClientConfigurationPropertiesProfileOidcTest {
  @Autowired TasklistClientConfigurationProperties properties;

  @Test
  void shouldApplyProfiles() throws MalformedURLException {
    assertThat(properties.profile()).isEqualTo(oidc);
    assertThat(properties.clientId()).isEqualTo("def");
    assertThat(properties.clientSecret()).isEqualTo("ghi");
    assertThat(properties.baseUrl()).isEqualTo(URI.create("http://localhost:8082").toURL());
    assertThat(properties.enabled()).isEqualTo(true);
    assertThat(properties.authUrl())
        .isEqualTo(
            URI.create(
                    "http://localhost:18080/auth/realms/camunda-platform/protocol/openid-connect/token")
                .toURL());
  }
}
