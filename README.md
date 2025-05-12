[![Community Extension](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)

# Camunda TaskList Client

This project was intially designed to simplify communication between a java backend and the Camunda 8 task list GraphQL APIs. Since GraphQL APIs are now deprecared, this client is now targetting REST endpoints. Contributions through PR are welcome!

:information_source: 8.3+ Relesases of this client are generated against Rest endpoints.

:information_source: **8.3.3.3 changes the way to build authentication and client. Please check the following documentation**

## How to build the client

### Spring Boot

Add the dependency to your project:

```xml
<dependency>
  <groupId>io.camunda</groupId>
  <artifactId>spring-boot-starter-camunda-tasklist</artifactId>
  <version>${version.tasklist-client}</version>
</dependency>
```

Configure a Camunda Tasklist client with simple authentication:

```yaml
tasklist:
  client:
    profile: simple
```

To adjust the (meaningful) default properties, you can also override them:

```yaml
tasklist:
  client:
    profile: simple
    enabled: true
    base-url: http://localhost:8082
    session-timeout: PT10M
    username: demo
    password: demo
```


Configure a Camunda Tasklist client with identity authentication:

```yaml
tasklist:
  client:
    profile: oidc
    client-id:
    client-secret:
    scope: # optional
```

To adjust the (meaningful) default properties, you can also override them:

```yaml
tasklist:
  client:
    profile: oidc
    enabled: true
    base-url: http://localhost:8082
    auth-url: http://localhost:18080/auth/realms/camunda-platform/protocol/openid-connect/token
    audience: tasklist-api
    client-id:
    client-secret:
    scope: # optional
```

Configure a Camunda Tasklist client for Saas:

```yaml
tasklist:
  client:
    profile: saas
    region:
    cluster-id:
    client-id:
    client-secret:
```

```yaml
tasklist:
  client:
    profile: saas
    enabled: true
    base-url: https://${tasklist.client.region}.tasklist.camunda.io/${tasklist.client.cluster-id}
    auth-url: https://login.cloud.camunda.io/oauth/token
    audience: tasklist.camunda.io
    region:
    cluster-id:
    client-id:
    client-secret:
```

Configure defaults that influence the client behaviour:

```yaml
tasklist:
  client:
    defaults:
      load-truncated-variables: true
      return-variables: true
      use-zeebe-user-tasks: true
```

### Plain Java

Add the dependency to your project:

```xml
<dependency>
  <groupId>io.camunda</groupId>
  <artifactId>camunda-tasklist-client-java</artifactId>
  <version>${version.tasklist-client}</version>
</dependency>
```

Build a Camunda Tasklist client with simple authentication:

```java
// properties you need to provide
String username = "demo";
String password = "demo";
URL tasklistUrl = URI.create("http://localhost:8082").toURL();
boolean returnVariables = false;
boolean loadTruncatedVariables = false;
boolean useZeebeUserTasks = true;
// if you are using zeebe user tasks, you require a zeebe client as well
ZeebeClient zeebeClient = zeebeClient();
// bootstrapping
SimpleCredential credentials =
    new SimpleCredential(username, password, tasklistUrl, Duration.ofMinutes(10));
SimpleAuthentication authentication = new SimpleAuthentication(credentials);
CamundaTasklistClientConfiguration configuration =
    new CamundaTasklistClientConfiguration(
        authentication,
        tasklistUrl,
        zeebeClient,
        new DefaultProperties(returnVariables, loadTruncatedVariables, useZeebeUserTasks));
CamundaTaskListClient client = new CamundaTaskListClient(configuration);
```

Build a Camunda Tasklist client with identity authentication:

```java
      // properties you need to provide
String clientId = "";
String clientSecret = "";
String audience = "tasklist-api";
String scope = ""; // can be omitted if not required
URL tasklistUrl = URI.create("http://localhost:8082").toURL();
URL authUrl =
    URI.create(
           "http://localhost:18080/auth/realms/camunda-platform/protocol/openid-connect/token")
       .toURL();
boolean returnVariables = false;
boolean loadTruncatedVariables = false;
boolean useZeebeUserTasks = true;
// if you are using zeebe user tasks, you require a zeebe client as well
ZeebeClient zeebeClient = zeebeClient();
// bootstrapping
JwtCredential credentials =
    new JwtCredential(clientId, clientSecret, audience, authUrl, scope);
ObjectMapper objectMapper = new ObjectMapper();
TokenResponseHttpClientResponseHandler clientResponseHandler =
    new TokenResponseHttpClientResponseHandler(objectMapper);
JwtAuthentication authentication = new JwtAuthentication(credentials, clientResponseHandler);
CamundaTasklistClientConfiguration configuration =
    new CamundaTasklistClientConfiguration(
        authentication,
        tasklistUrl,
        zeebeClient,
        new DefaultProperties(returnVariables, loadTruncatedVariables, useZeebeUserTasks));
CamundaTaskListClient client = new CamundaTaskListClient(configuration);
```

Build a Camunda Tasklist client for Saas:

```java
      // properties you need to provide
String region = "";
String clusterId = "";
String clientId = "";
String clientSecret = "";
boolean returnVariables = false;
boolean loadTruncatedVariables = false;
boolean useZeebeUserTasks = true;
// if you are using zeebe user tasks, you require a zeebe client as well
ZeebeClient zeebeClient = zeebeClient();
// bootstrapping
URL tasklistUrl =
    URI.create("https://" + region + ".tasklist.camunda.io/" + clusterId).toURL();
URL authUrl = URI.create("https://login.cloud.camunda.io/oauth/token").toURL();
JwtCredential credentials =
    new JwtCredential(clientId, clientSecret, "tasklist.camunda.io", authUrl, null);
ObjectMapper objectMapper = new ObjectMapper();
TokenResponseHttpClientResponseHandler clientResponseHandler =
    new TokenResponseHttpClientResponseHandler(objectMapper);
JwtAuthentication authentication = new JwtAuthentication(credentials, clientResponseHandler);
CamundaTasklistClientConfiguration configuration =
    new CamundaTasklistClientConfiguration(
        authentication,
        tasklistUrl,
        zeebeClient,
        new DefaultProperties(returnVariables, loadTruncatedVariables, useZeebeUserTasks));
CamundaTaskListClient client = new CamundaTaskListClient(configuration);
```

:information_source: **shouldReturnVariables()** will read variables along with tasks. This is not the recommended approach but rather a commodity. In real project implementation, we would recommend to load task variables only when required.

:information_source: **shouldLoadTruncatedVariables()** will execute a second call to read the variable if its value was truncated in the initial search.

## Make some queries
```java
//get tasks from a process instance (TaskSearch can take many more parameters)
TaskSearch ts = new TaskSearch().setProcessInstanceKey("2251799818839086");
TaskList tasksFromInstance = client.getTasks(ts);

//get tasks from process variables
ts = new TaskSearch().addVariableFilter("riskLevels", List.of("yellow", "yellow")).addVariableFilter("age", 30);
TaskList tasksFromVariableSearch = client.getTasks(ts);

//get tasks assigned to demo
TaskList tasks = client.getAssigneeTasks("demo", TaskState.CREATED, null);
for(Task task : tasks) {
    client.unclaim(task.getId());
}
//get tasks associated with group "toto"
tasks = client.getGroupTasks("toto", null, null);

//get 10 completed tasks without their variables (last parameter) associated with group "toto", assigned (second parameter) to paul (thrid parameter)
tasks = client.getTasks("toto", true, "paul", TaskState.COMPLETED, 10, false);

//navigate after, before, afterOrEqual to previous result.
tasks = client.after(tasks);
tasks = client.before(tasks);
tasks = client.afterOrEqual(tasks);

//get unassigned tasks
tasks = client.getTasks(false, null, null);
for(Task task : tasks) {
  //assign task to paul
  client.claim(task.getId(), "paul");
}
for(Task task : tasks) {
  //complete task with variables
  client.completeTask(task.getId(), Map.of("key", "value"));
}

//get a single task
task = client.getTask("1");

//get form schema
String formKey = task.getFormKey();
String formId = formKey.substring(formKey.lastIndexOf(":")+1);
String processDefinitionId = task.getProcessDefinitionId();

Form form = client.getForm(formId, processDefinitionId);
String schema = form.getSchema();
```

# Note
A similar library is available for the Operate API of Camunda Platform here:
[camunda-operate-client-java](https://github.com/camunda-community-hub/camunda-operate-client-java)
