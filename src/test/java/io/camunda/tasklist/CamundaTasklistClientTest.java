package io.camunda.tasklist;

import static org.junit.jupiter.api.Assertions.*;

import io.camunda.common.auth.Authentication;
import io.camunda.common.auth.Product;
import io.camunda.tasklist.exception.TaskListException;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.Test;

public class CamundaTasklistClientTest {
  @Test
  public void shouldThrowIfZeebeClientNullAndUseZeebeUserTasks() {
    CamundaTaskListClientProperties properties = new CamundaTaskListClientProperties();
    properties.setUseZeebeUserTasks(true);
    properties.setTaskListUrl("http://localhost:8082");
    properties.setAuthentication(new MockAuthentication());
    AssertionError assertionError =
        assertThrows(AssertionError.class, () -> new CamundaTaskListClient(properties, null));
    assertEquals("zeebeClient must not be null", assertionError.getMessage());
  }

  @Test
  public void shouldNotThrowIfZeebeClientNullAndNotUseZeebeUserTasks() throws TaskListException {
    CamundaTaskListClientProperties properties = new CamundaTaskListClientProperties();
    properties.setUseZeebeUserTasks(false);
    properties.setTaskListUrl("http://localhost:8082");
    properties.setAuthentication(new MockAuthentication());
    CamundaTaskListClient client = new CamundaTaskListClient(properties, null);
    assertNotNull(client);
  }

  private static class MockAuthentication implements Authentication {
    @Override
    public Entry<String, String> getTokenHeader(Product product) {
      return Map.entry("token", "token");
    }

    @Override
    public void resetToken(Product product) {}
  }
}
