[![Community Extension](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)

# Camunda TaskList Client

This project was intially designed to simplify communication between a java backend and the Camunda 8 task list GraphQL APIs. Since GraphQL APIs are now deprecared, this client is now targetting REST endpoints. Contributions through PR are welcome!

:information_source: 8.3+ Relesases of this client are generated against Rest endpoints.

:information_source: **8.3.3.3 changes the way to build authentication and client. Please check the following documentation**

## Use the correct authentication

Depending on your setup, you may want to use different authentication mechanisms.
In case you're using a Camunda Platform without identity enabled, you should use the **SimpleAuthentication**

```java
SimpleConfig simpleConf = new SimpleConfig();
simpleConf.addProduct(Product.TASKLIST, new SimpleCredential("user", "pwd", "http://tasklistUrl[:port]"));
Authentication auth = SimpleAuthentication.builder().withSimpleConfig(simpleConf).build();

CamundaTaskListClient client = CamundaTaskListClient.builder()
  .taskListUrl("http://tasklistUrl[:port]")
  .authentication(auth)
  .cookieExpiration(Duration.ofSeconds(5))
  .build();
```

In case you're using a Self Managed Camunda Platform with identity enabled (and Keycloak), you should use the **SelfManagedAuthentication**

```java
CamundaTaskListClient client = CamundaTaskListClient.builder()
  .taskListUrl("https://reg-x.tasklist.camunda.io/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx/")
  .selfManagedAuthentication("clientId", "clientSecret", "keycloakUrl")
  .build();
```

And finally, if you're using a SaaS environment, just use the **SaaSAuthentication**

```java
CamundaTaskListClient client = CamundaTaskListClient.builder()
  .taskListUrl("https://reg-x.tasklist.camunda.io/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx/")
  .saaSAuthentication("clientId", "clientSecret")
  .build();
```

## Build your client

Simply build a CamundaTaskListClient that takes an authentication and the tasklist url as parameters.

:information_source: Since the SelfManagedAuthentication and the SaaSAuthentication are a bit complex, two helpers have been added to the builder **saaSAuthentication(clientId, clientSecret)** and **selfManagedAuthentication(clientId, clientSecret, keycloakUrl)** as shown in previous examples. You can also build these SaaSAuthentication and SelfManagedAuthentication yourself, following the code provided in the helpers.

```java
CamundaTaskListClient client = CamundaTaskListClient.builder().taskListUrl("http://localhost:8081").shouldReturnVariables().shouldLoadTruncatedVariables().authentication(auth).build();
```

:information_source: **shouldReturnVariables()** will read variables along with tasks. This is not the recommended approach but rather a commodity. In real project implementation, we would recommend to load task variables only when required.

:information_source: **shouldLoadTruncatedVariables()** will execute a second call to read the variable if its value was truncated in the initial search.

## Make some queries
```java
//get tasks from a process instance (TaskSearch can take many more parameters)
TaskSearch ts = new TaskSearch().setProcessInstanceId("2251799818839086");
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

# use it in your project
You can import it to your maven or gradle project as a dependency

```xml
<dependency>
	<groupId>io.camunda</groupId>
	<artifactId>camunda-tasklist-client-java</artifactId>
	<version>8.5.3.3</version>
</dependency>
```


# Note
A similar library is available for operate there:
[java-client-operate](https://github.com/camunda-community-hub/spring-zeebe/tree/main/camunda-sdk-java/java-client-operate)
