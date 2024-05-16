package io.camunda.tasklist;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CamundaTasklistClientTest {
  @Test
  public void shouldThrowIfZeebeClientNull() {
    AssertionError assertionError =
        assertThrows(
            AssertionError.class,
            () -> new CamundaTaskListClient(new CamundaTaskListClientProperties(), null));
    assertEquals("zeebeClient must not be null", assertionError.getMessage());
  }
}
