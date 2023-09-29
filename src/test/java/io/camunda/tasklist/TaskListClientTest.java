package io.camunda.tasklist;

import io.camunda.tasklist.auth.JWTAuthentication;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.response.Topology;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class TaskListClientTest {

  @Autowired
  JWTAuthentication jwtAuthentication;
  @Autowired
  TaskListRestClient taskListRestClient;
  @Autowired
  ZeebeClient zeebeClient;
  Long processDefinitionKey;

  public void deployProcess() {
    InputStream is = getClass().getClassLoader().getResourceAsStream("bpm/tasklistRestAPIUnitTestProcess.bpmn");
    DeploymentEvent event = zeebeClient.newDeployResourceCommand()
        .addResourceStream(is, "tasklistRestAPIUnitTestProcess.bpmn")
        .send()
        .join();
    processDefinitionKey = event.getProcesses().get(0).getProcessDefinitionKey();
    assertNotNull(processDefinitionKey);
  }

  public void createInstance(Map<String, String> variables) {
    ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
        .processDefinitionKey(processDefinitionKey)
        .variables(variables)
        .send()
        .join();

    Long processInstanceKey = event.getProcessInstanceKey();
    assertNotNull(processInstanceKey);
  }

  @BeforeAll
  public void before() {

  }

  @Test
  void contextLoads() {
  }

  @Test
  void authenticationTest() throws TaskListException {
    assertTrue(jwtAuthentication.authenticate(taskListRestClient));
  }

  @Test
  public void zeebeStatus() {
    Topology topology = zeebeClient.newTopologyRequest().send().join();
    assertTrue(topology.getClusterSize() > 0);
  }

}
