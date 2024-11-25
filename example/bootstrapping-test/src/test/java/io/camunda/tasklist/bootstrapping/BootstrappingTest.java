package io.camunda.tasklist.bootstrapping;

import static org.junit.jupiter.api.Assertions.*;

import io.camunda.tasklist.CamundaTaskListClientV1;
import org.junit.jupiter.api.Test;

public class BootstrappingTest {
  @Test
  void shouldRun() {
    CamundaTaskListClientV1 camundaTaskListClient = new Bootstrapper().create();
    assertNotNull(camundaTaskListClient);
  }
}
