package io.camunda.tasklist.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.camunda.tasklist.rest.auth.JWTAuthentication;
import io.camunda.tasklist.rest.exception.TaskListException;
import io.camunda.tasklist.rest.exception.TaskListRestException;
import io.camunda.tasklist.rest.json.JsonUtils;
import io.camunda.tasklist.rest.TaskListRestClient;
import io.camunda.tasklist.rest.dto.*;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.response.Topology;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
  @Value("${tasklist.unit-test.processDefinitionKey}")
  String processDefinitionKey;

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
    taskSearchRequest.setProcessDefinitionKey(processDefinitionKey);
    return taskListRestClient.searchTasks(taskSearchRequest);
  }

  public List<TaskSearchResponse> findCreatedAssignedTasks() throws TaskListException, TaskListRestException {
    TaskSearchRequest taskSearchRequest = new TaskSearchRequest();
    taskSearchRequest.setState(Constants.TASK_STATE_CREATED);
    taskSearchRequest.setAssigned(true);
    taskSearchRequest.setProcessDefinitionKey(processDefinitionKey);
    return taskListRestClient.searchTasks(taskSearchRequest);
  }

  private void waitForIndexing() {
    waitForIndexing(10);
  }

  private void waitForIndexing(Integer seconds) {
    try {
      TimeUnit.SECONDS.sleep(seconds);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
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

  public TaskResponse assignTask(String assignee) throws TaskListException, TaskListRestException {

    String taskId = findCreatedUnAssignedTasks().get(0).getId();

    TaskAssignRequest taskAssignRequest = new TaskAssignRequest();
    taskAssignRequest.setAssignee(assignee);
    taskAssignRequest.setAllowOverrideAssignment(true);
    TaskResponse response = taskListRestClient.assignTask(taskId, taskAssignRequest);
    assertNotNull(response);
    return response;
  }

  public TaskResponse unassignTask(String taskId) throws TaskListException, TaskListRestException {
    TaskResponse response = taskListRestClient.unassignTask(taskId);
    assertNotNull(response);
    return response;
  }

  @Test
  public void taskLifecycleTest() throws TaskListException, TaskListRestException {

    // Create unassigned task
    Map<String, String> variables = new HashMap<>();
    if (findCreatedUnAssignedTasks().size() <= 0) {
      createInstance(variables);
      waitForIndexing();
    }

    // Find unassigned tasks
    List<TaskSearchResponse> tasks = findCreatedUnAssignedTasks();
    assertTrue(tasks.size() > 0);

    String taskId = tasks.get(0).getId();

    // Assign task
    TaskResponse response = assignTask("junit");
    assertEquals(response.getAssignee(), "junit");

    // Find assigned tasks
    tasks = findCreatedAssignedTasks();
    assertTrue(tasks.size() > 0);

    // Un-assign task
    response = unassignTask(taskId);
    assertNull(response.getAssignee());

    // Complete Task with variables
    Map<String, Object> instanceVariables = new HashMap<>();
    Map<String, String> mockPersonData = new HashMap<>();
    mockPersonData.put("name", "Dave");
    mockPersonData.put("country", "USA");
    mockPersonData.put("color", "red");
    instanceVariables.put("person", mockPersonData);
    instanceVariables.put("timestamp", Calendar.getInstance());
    response = taskListRestClient.completeTask(taskId, instanceVariables);
    assertNotNull(response);
    assertEquals(response.getTaskState(), Constants.TASK_STATE_COMPLETED);

  }

}
