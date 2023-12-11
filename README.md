[![Community Extension](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)

# Camunda TaskList Client

This project was intially designed to simplify communication between a java backend and the Camunda 8 task list GraphQL APIs. Since GraphQL APIs are now deprecared, this client is now targetting REST endpoints. Contributions through PR are welcome!

:information_source: 8.3+ Relesases of this client are generated against Rest endpoints.


# How to use the client

Simply build a CamundaTaskListClient that takes an authentication and the tasklist url as parameters.

```java
SimpleAuthentication sa = new SimpleAuthentication("demo", "demo");

//shouldReturnVariables will change the default behaviour for the client to query variables along with tasks.
CamundaTaskListClient client = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8081").shouldReturnVariables().authentication(sa).build();
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



# Authentication
You can use the ***SimpleAuthentication*** to connect to a local Camunda TaskList if your setup is "simple": ***without identity and keycloak***.

To connect to the **SaaS** TaskList, you need to use the **SaasAuthentication**. The SaaSAuthentication requires the clientId and clientSecret

```java
SaasAuthentication sa = new SaasAuthentication("2~nB1MwkUU45FuXXX", "aBRKtreXQF3uD2MYYY");
CamundaTaskListClient client = new CamundaTaskListClient.Builder().authentication(sa)
    .taskListUrl("https://bru-2.tasklist.camunda.io/757dbc30-5127-4bed-XXXX-XXXXXXXXXXXX").build();


client.getTasks(false, TaskState.CREATED, 50);
```

To connect to the **Local** TaskList with **Identity & Keycloak**, you need to use the **SelfManagedAuthentication**. The SelfManagedAuthentication requires the clientId and clientSecret. You can also change the Keycloak realm and the keycloakUrl depending on your installation.

```java
SelfManagedAuthentication sma = new SelfManagedAuthentication().clientId("java").clientSecret("foTPogjlI0hidwbDZcYFWzmU8FOQwLx0").baseUrl("http://localhost:18080").keycloakRealm("camunda-platform");
CamundaTaskListClient client = new CamundaTaskListClient.Builder().shouldReturnVariables().taskListUrl("http://localhost:8082/").authentication(sma).build();
       
client.getTasks(false, TaskState.CREATED, 50);
```

# use it in your project
You can import it to your maven or gradle project as a dependency

```xml
<dependency>
	<groupId>io.camunda</groupId>
	<artifactId>camunda-tasklist-client-java</artifactId>
	<version>8.3.3</version>
</dependency>
```
# Troubleshooting



# Note
A similar library is available for operate there:
[camunda-operate-client-java](https://github.com/camunda-community-hub/camunda-operate-client-java)
