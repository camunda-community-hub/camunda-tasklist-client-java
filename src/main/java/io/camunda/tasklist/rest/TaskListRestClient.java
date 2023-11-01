package io.camunda.tasklist.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.tasklist.rest.auth.TaskListJWTAuthentication;
import io.camunda.tasklist.rest.dto.requests.TaskAssignRequest;
import io.camunda.tasklist.rest.dto.requests.TaskCompleteRequest;
import io.camunda.tasklist.rest.dto.requests.TaskSearchRequest;
import io.camunda.tasklist.rest.dto.requests.VariableInput;
import io.camunda.tasklist.rest.dto.responses.ErrorResponse;
import io.camunda.tasklist.rest.dto.responses.FormResponse;
import io.camunda.tasklist.rest.dto.responses.TaskResponse;
import io.camunda.tasklist.rest.dto.responses.TaskSearchResponse;
import io.camunda.tasklist.rest.exception.TaskListException;
import io.camunda.tasklist.rest.exception.TaskListRestException;
import io.camunda.tasklist.rest.json.JsonUtils;
import io.camunda.tasklist.rest.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class TaskListRestClient implements TaskListRestApi {

  TaskListJWTAuthentication authentication;
  AccessTokenResponse accessTokenResponse;
  String taskListBaseUrl;

  private final HttpClient httpClient;

  public TaskListRestClient(
      @Autowired TaskListJWTAuthentication authentication,
      @Value("${tasklist.client.taskListBaseUrl}") String taskListBaseUrl) {
    this.authentication = authentication;
    httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    this.taskListBaseUrl = taskListBaseUrl;
  }

  public TaskListJWTAuthentication getAuthentication() {
    return authentication;
  }

  public void setAuthentication(TaskListJWTAuthentication authentication) {
    this.authentication = authentication;
  }

  public AccessTokenResponse getAccessTokenResponse() {
    return accessTokenResponse;
  }

  public void setAccessTokenResponse(AccessTokenResponse accessTokenResponse) {
    this.accessTokenResponse = accessTokenResponse;
  }

  public HttpResponse<String> post(String endPoint, String body)
      throws TaskListException, TaskListRestException {
    return postOrPatch("POST", endPoint, body);
  }

  public HttpResponse<String> patch(String endPoint, String body)
      throws TaskListException, TaskListRestException {
    return postOrPatch("PATCH", endPoint, body);
  }

  private HttpResponse<String> postOrPatch(String method, String endPoint, String body)
      throws TaskListException, TaskListRestException {

    if(accessTokenResponse == null) {
      this.authentication.authenticate(this);
    }

    HttpResponse<String> response = doPostOrPatch(method, endPoint, body);

    //If we get a 401 that might mean the access token has expired. Try to refresh the token
    if(response.statusCode() == 401) {
      this.authentication.authenticate(this);
      response = doPostOrPatch(method, endPoint, body);
    }

    if(response.statusCode() == 200) {
      return response;
    } else if (response.statusCode() == 400) {

      JsonUtils<ErrorResponse> jsonUtils = new JsonUtils<>(ErrorResponse.class);
      try {
        ErrorResponse errorResponse = jsonUtils.fromJson(response.body());
        throw new TaskListRestException(errorResponse);
      } catch (JsonProcessingException e) {
        throw new TaskListException("Unable to parse error response", e);
      }

    } else {
      throw new TaskListException("Unexpected response from post", new RuntimeException(response.statusCode() + " " + response.body()));
    }
  }

  private HttpResponse<String> doPostOrPatch(String method, String endPoint, String body) throws TaskListException {
    try {

      HttpRequest request = HttpRequest.newBuilder()
          .uri(new URI(endPoint))
          .header("content-type", "application/json")
          .header("Authorization", "Bearer " + accessTokenResponse.getAccess_token())
          .timeout(Duration.ofSeconds(10))
          .method(method, HttpRequest.BodyPublishers.ofString(body))
          .build();

      return httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    } catch (URISyntaxException e) {
      throw new TaskListException("Endpoint URL must be a valid URI", e);
    } catch (IOException | InterruptedException e) {
      throw new TaskListException("Unable to complete request", e);
    }

  }

  private HttpResponse<String> get(String endPoint)
      throws TaskListException, TaskListRestException {

    if(accessTokenResponse == null) {
      this.authentication.authenticate(this);
    }

    HttpResponse<String> response = doGet(endPoint);

    //If we get a 401 that might mean the access token has expired. Try to refresh the token
    if(response.statusCode() == 401) {
      this.authentication.authenticate(this);
      response = doGet(endPoint);
    }

    if(response.statusCode() == 200) {
      return response;
    } else if (response.statusCode() == 400) {

      JsonUtils<ErrorResponse> jsonUtils = new JsonUtils<>(ErrorResponse.class);
      try {
        ErrorResponse errorResponse = jsonUtils.fromJson(response.body());
        throw new TaskListRestException(errorResponse);
      } catch (JsonProcessingException e) {
        throw new TaskListException("Unable to parse error response", e);
      }

    } else {
      throw new TaskListException("Unexpected response from post", new RuntimeException(response.statusCode() + " " + response.body()));
    }
  }

  private HttpResponse<String> doGet(String endPoint) throws TaskListException {
    try {

      HttpRequest request = HttpRequest.newBuilder()
          .uri(new URI(endPoint))
          .header("content-type", "application/json")
          .header("Authorization", "Bearer " + accessTokenResponse.getAccess_token())
          .timeout(Duration.ofSeconds(10))
          .GET()
          .build();

      return httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    } catch (URISyntaxException e) {
      throw new TaskListException("Endpoint URL must be a valid URI", e);
    } catch (IOException | InterruptedException e) {
      throw new TaskListException("Unable to complete request", e);
    }

  }

  public List<TaskSearchResponse> searchTasks(TaskSearchRequest request) throws TaskListException, TaskListRestException {
    JsonUtils<TaskSearchRequest> jsonRequest = new JsonUtils<>(TaskSearchRequest.class);
    try {

      String body = jsonRequest.toJson(request);
      String endpoint = taskListBaseUrl +  "/v1/tasks/search";
      HttpResponse<String> response = post(endpoint, body);

      // TODO: update JsonUtils if this works
      JsonUtils<List> jsonResponse = new JsonUtils<>(List.class);
      ObjectMapper objectMapper = jsonRequest.getObjectMapper();
      JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, TaskSearchResponse.class);
      return objectMapper.readValue(response.body(), javaType);

      //return jsonResponse.fromJson(response.body());

    } catch (JsonProcessingException e) {
      throw new TaskListException("Unable to parse TaskSearchRequest to json", e);
    }
  }

  public TaskResponse getTask(String taskId) throws TaskListException, TaskListRestException {

    try {

      String endpoint = taskListBaseUrl +  "/v1/tasks/" + taskId;
      HttpResponse<String> response = get(endpoint);

      JsonUtils<TaskResponse> jsonResponse = new JsonUtils<>(TaskResponse.class);
      return jsonResponse.fromJson(response.body());

    } catch (JsonProcessingException e) {
      throw new TaskListException("Unable to parse FormResponse to json", e);
    }

  }

  public FormResponse getForm(String processDefinitionKey, String formId) throws TaskListException, TaskListRestException {

    try {
      String endpoint = taskListBaseUrl +  "/v1/forms/" + formId + "?processDefinitionKey=" + processDefinitionKey;
      HttpResponse<String> response = get(endpoint);

      // TODO: update JsonUtils if this works
      JsonUtils<FormResponse> jsonResponse = new JsonUtils<>(FormResponse.class);
      return jsonResponse.fromJson(response.body());

    } catch (JsonProcessingException e) {
      throw new TaskListException("Unable to parse FormResponse to json", e);
    }
  }

  public TaskResponse assignTask(String taskId, TaskAssignRequest request) throws TaskListException, TaskListRestException {
    JsonUtils<TaskAssignRequest> jsonRequest = new JsonUtils<>(TaskAssignRequest.class);
    try {

      String body = jsonRequest.toJson(request);
      String endpoint = taskListBaseUrl + "/v1/tasks/"+taskId+"/assign";
      HttpResponse<String> response = patch(endpoint, body);
      JsonUtils<TaskResponse> jsonResponse = new JsonUtils<>(TaskResponse.class);
      return jsonResponse.fromJson(response.body());

    } catch (JsonProcessingException e) {
      throw new TaskListException("Unable to parse TaskSearchRequest to json", e);
    }
  }

  public TaskResponse unassignTask(String taskId) throws TaskListException, TaskListRestException {
    try {
      String endpoint = taskListBaseUrl + "/v1/tasks/"+taskId+"/unassign";
      HttpResponse<String> response = patch(endpoint, "");
      JsonUtils<TaskResponse> jsonResponse = new JsonUtils<>(TaskResponse.class);
      return jsonResponse.fromJson(response.body());

    } catch (JsonProcessingException e) {
      throw new TaskListException("Unable to parse TaskSearchRequest to json", e);
    }
  }

  public static List<VariableInput> convertToVariableInputList(Map<String, Object> map) throws TaskListException {
    List<VariableInput> variableInputList = new ArrayList<>();

    ObjectMapper objectMapper = new ObjectMapper();

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String name = entry.getKey();
      Object value = entry.getValue();
      VariableInput variableInput = new VariableInput();

      variableInput.setName(name);

      if (value instanceof String) {
        variableInput.setValue((String) value);
      } else {
        // Serialize the object to a JSON string
        try {
          String jsonValue = objectMapper.writeValueAsString(value);
          variableInput.setValue(jsonValue);
        } catch (Exception e) {
          throw new TaskListException("Unable to serialize instance variables", e);
        }
      }

      variableInputList.add(variableInput);
    }

    return variableInputList;
  }

  public TaskResponse completeTask(String taskId, Map<String, Object> variablesMap) throws TaskListException, TaskListRestException {

    TaskCompleteRequest taskCompleteRequest = new TaskCompleteRequest();
    taskCompleteRequest.setVariables(convertToVariableInputList(variablesMap));

    JsonUtils<TaskCompleteRequest> jsonRequest = new JsonUtils<>(TaskCompleteRequest.class);
    try {
      String body = jsonRequest.toJson(taskCompleteRequest);
      String endpoint = taskListBaseUrl + "/v1/tasks/"+taskId+"/complete";
      HttpResponse<String> response = patch(endpoint, body);
      JsonUtils<TaskResponse> jsonResponse = new JsonUtils<>(TaskResponse.class);
      return jsonResponse.fromJson(response.body());

    } catch (JsonProcessingException e) {
      throw new TaskListException("Unable to parse TaskSearchRequest to json", e);
    }
  }

}
