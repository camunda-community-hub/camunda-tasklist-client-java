package io.camunda.tasklist;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.camunda.tasklist.auth.JWTAuthentication;
import io.camunda.tasklist.dto.AccessTokenRequest;
import io.camunda.tasklist.dto.Constants;
import io.camunda.tasklist.dto.TaskSearchRequest;
import io.camunda.tasklist.dto.TaskSearchResponse;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.tasklist.exception.TaskListRestException;
import io.camunda.tasklist.json.JsonUtils;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.response.Topology;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskListClientTest {

  @Autowired
  JWTAuthentication jwtAuthentication;
  @Autowired
  TaskListRestClient taskListRestClient;
  @Autowired
  ZeebeClient zeebeClient;

  @Value("${tasklist.unit-test.bpmnProcessId: 'tasklistRestAPIUnitTestProcess'}")
  String bpmnProcessId;

  public void createInstance(Map<String, String> variables) {
    ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
        .bpmnProcessId(bpmnProcessId)
        .latestVersion()
        .variables(variables)
        .send()
        .join();

    Long processInstanceKey = event.getProcessInstanceKey();
    assertNotNull(processInstanceKey);
  }

  public List<TaskSearchResponse> findCreatedUnAssignedTasks() throws TaskListException, TaskListRestException {
    TaskSearchRequest taskSearchRequest = new TaskSearchRequest();
    taskSearchRequest.setState(Constants.TASK_STATE_CREATED);
    taskSearchRequest.setAssigned(false);
    //taskSearchRequest.setProcessDefinitionKey(processDefinitionKey.toString());
    List<TaskSearchResponse> response = taskListRestClient.searchTasks(taskSearchRequest);
    return response;
  }

  public List<TaskSearchResponse> findCreatedAssignedTasks() throws TaskListException, TaskListRestException {
    TaskSearchRequest taskSearchRequest = new TaskSearchRequest();
    taskSearchRequest.setState(Constants.TASK_STATE_CREATED);
    taskSearchRequest.setAssigned(true);
    //taskSearchRequest.setProcessDefinitionKey(processDefinitionKey.toString());
    List<TaskSearchResponse> response = taskListRestClient.searchTasks(taskSearchRequest);
    return response;
  }

  public void setup() throws TaskListException, TaskListRestException {
    Map<String, String> variables = new HashMap<>();
    if (findCreatedUnAssignedTasks().size() <= 0) {
      createInstance(variables);
    }
    if (findCreatedAssignedTasks().size() <= 0) {
      variables.put("assignee", "junit");
      createInstance(variables);
    }
  }

  @BeforeAll
  public void before() throws TaskListException, TaskListRestException {
    setup();
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

  @Test
  public void jsonTest() throws JsonProcessingException {
    JsonUtils<AccessTokenRequest> jsonUtils = new JsonUtils<>(AccessTokenRequest.class);
    AccessTokenRequest accessTokenRequest = new AccessTokenRequest("xxx", "xxx", "tasklist.camunda.io", "client_credentials");
    String json = jsonUtils.toJson(accessTokenRequest);
    assertNotNull(json);
    assertEquals("{\"client_id\":\"xxx\",\"client_secret\":\"xxx\",\"audience\":\"tasklist.camunda.io\",\"grant_type\":\"client_credentials\"}", json);

    AccessTokenRequest result = jsonUtils.fromJson(json);
    assertNotNull(result);
    assertEquals("xxx", result.getClient_id());
  }

}
