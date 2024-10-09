package io.camunda.tasklist.bootstrapping;

import static org.junit.jupiter.api.Assertions.*;

import io.camunda.tasklist.CamundaTaskListClient;
import org.junit.jupiter.api.Test;

public class BootstrappingTest {
  @Test
  void shouldRun() {
    CamundaTaskListClient camundaTaskListClient = new Bootstrapper().create();
    assertNotNull(camundaTaskListClient);
  }
}
