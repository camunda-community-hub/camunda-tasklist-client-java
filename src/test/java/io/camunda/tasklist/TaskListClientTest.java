package io.camunda.tasklist;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.camunda.tasklist.auth.JWTAuthentication;
import io.camunda.tasklist.dto.*;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.tasklist.exception.TaskListRestException;
import io.camunda.tasklist.json.JsonUtils;
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

  public void setup() throws TaskListException, TaskListRestException {

    Map<String, String> variables = new HashMap<>();
    boolean created = false;
    if (findCreatedUnAssignedTasks().size() <= 0) {
      createInstance(variables);
      created = true;
    }
    if (findCreatedAssignedTasks().size() <= 0) {
      variables.put("assignee", "junit");
      createInstance(variables);
      created = true;
    }

    // TODO: improve this using job worker
    // If this is the first time running tests, then we need to wait 5 seconds to allow tasklist to index the newly
    // created tasks
    if(created) {
      try {
        TimeUnit.SECONDS.sleep(10);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
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

  @Test
  public void findCreatedAssignedTasksTest() throws TaskListException, TaskListRestException {
    setup();
    List<TaskSearchResponse> tasks = findCreatedAssignedTasks();
    assertTrue(tasks.size() > 0);
  }

  @Test
  public void findCreatedUnAssignedTasksTest() throws TaskListException, TaskListRestException {
    setup();
    List<TaskSearchResponse> tasks = findCreatedUnAssignedTasks();
    assertTrue(tasks.size() > 0);
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

  @Test
  public void assignTaskTest() throws TaskListException, TaskListRestException {
    setup();
    TaskResponse response = assignTask("junit");
    assertEquals(response.getAssignee(), "junit");
  }

  public TaskResponse unassignTask(String taskId) throws TaskListException, TaskListRestException {
    TaskResponse response = taskListRestClient.unassignTask(taskId);
    assertNotNull(response);
    return response;
  }

  @Test
  public void unassignTaskTest() throws TaskListException, TaskListRestException {
    setup();
    String taskId = findCreatedAssignedTasks().get(0).getId();
    TaskResponse response = unassignTask(taskId);
    assertNull(response.getAssignee());
  }

  @Test
  public void completeTaskTest() throws TaskListException, TaskListRestException {
    setup();
    String taskId = findCreatedAssignedTasks().get(0).getId();

    Map<String, Object> instanceVariables = new HashMap<>();
    Map<String, String> mockPersonData = new HashMap<>();
    mockPersonData.put("name", "Dave");
    mockPersonData.put("country", "USA");
    mockPersonData.put("color", "red");
    instanceVariables.put("person", mockPersonData);
    instanceVariables.put("timestamp", Calendar.getInstance());
    TaskResponse response = taskListRestClient.completeTask(taskId, instanceVariables);
    assertNotNull(response);
    assertEquals(response.getTaskState(), Constants.TASK_STATE_COMPLETED);
  }

}
