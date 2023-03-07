[![Community Extension](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)

# Camunda TaskList Client

This project is designed to simplify communication between a java backend and the Camunda 8 task list. Contributions through PR are welcome!

# How to use the client

Simply build a CamundaTaskListClient that takes an authentication and the tasklist url as parameters.

```java
SimpleAuthentication sa = new SimpleAuthentication("demo", "demo");

//shouldReturnVariables will change the default behaviour for the client to query variables along with tasks.
CamundaTaskListClient client = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8081").shouldReturnVariables().authentication(sa).build();
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
	<version>1.6.1</version>
</dependency>
```
# Troubleshooting
Depending on your project, you may encounter the "companion" error :

```
java.lang.NoSuchFieldError: Companion
	at com.apollographql.apollo3.network.http.DefaultHttpEngine$execute$2$httpRequest$2$2.contentType(OkHttpEngine.kt:56) ~[apollo-runtime-jvm-3.2.1.jar:3.2.1]
	at okhttp3.internal.http.BridgeInterceptor.intercept(BridgeInterceptor.java:53) ~[okhttp-3.14.9.jar:na]
	at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:142) ~[okhttp-3.14.9.jar:na]
	at okhttp3.internal.http.RetryAndFollowUpInterceptor.intercept(RetryAndFollowUpInterceptor.java:88) ~[okhttp-3.14.9.jar:na]
	at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:142) ~[okhttp-3.14.9.jar:na]
	at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:117) ~[okhttp-3.14.9.jar:na]
	at okhttp3.RealCall.getResponseWithInterceptorChain(RealCall.java:229) ~[okhttp-3.14.9.jar:na]
	at okhttp3.RealCall.execute(RealCall.java:81) ~[okhttp-3.14.9.jar:na]
	at com.apollographql.apollo3.network.http.DefaultHttpEngine.execute(OkHttpEngine.kt:75) ~[apollo-runtime-jvm-3.2.1.jar:3.2.1]
	at com.apollographql.apollo3.network.http.HttpNetworkTransport$EngineInterceptor.intercept(HttpNetworkTransport.kt:136) ~[apollo-runtime-jvm-3.2.1.jar:3.2.1]
	at com.apollographql.apollo3.network.http.DefaultHttpInterceptorChain.proceed(HttpInterceptor.kt:22) ~[apollo-runtime-jvm-3.2.1.jar:3.2.1]
	at com.apollographql.apollo3.network.http.HttpNetworkTransport$execute$1.invokeSuspend(HttpNetworkTransport.kt:62) ~[apollo-runtime-jvm-3.2.1.jar:3.2.1]
	at com.apollographql.apollo3.network.http.HttpNetworkTransport$execute$1.invoke(HttpNetworkTransport.kt) ~[apollo-runtime-jvm-3.2.1.jar:3.2.1]
	at com.apollographql.apollo3.network.http.HttpNetworkTransport$execute$1.invoke(HttpNetworkTransport.kt) ~[apollo-runtime-jvm-3.2.1.jar:3.2.1]
	at kotlinx.coroutines.flow.SafeFlow.collectSafely(Builders.kt:61) ~[kotlinx-coroutines-core-jvm-1.5.2.jar:na]
	at kotlinx.coroutines.flow.AbstractFlow.collect(Flow.kt:212) ~[kotlinx-coroutines-core-jvm-1.5.2.jar:na]
	at kotlinx.coroutines.flow.internal.ChannelFlowOperatorImpl.flowCollect(ChannelFlow.kt:195) ~[kotlinx-coroutines-core-jvm-1.5.2.jar:na]
	at kotlinx.coroutines.flow.internal.ChannelFlowOperator.collectTo$suspendImpl(ChannelFlow.kt:157) ~[kotlinx-coroutines-core-jvm-1.5.2.jar:na]
	at kotlinx.coroutines.flow.internal.ChannelFlowOperator.collectTo(ChannelFlow.kt) ~[kotlinx-coroutines-core-jvm-1.5.2.jar:na]
	at kotlinx.coroutines.flow.internal.ChannelFlow$collectToFun$1.invokeSuspend(ChannelFlow.kt:60) ~[kotlinx-coroutines-core-jvm-1.5.2.jar:na]
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33) ~[kotlin-stdlib-1.6.21.jar:1.6.21-release-334(1.6.21)]
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106) ~[kotlinx-coroutines-core-jvm-1.5.2.jar:na]
	at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:571) ~[kotlinx-coroutines-core-jvm-1.5.2.jar:na]
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:750) ~[kotlinx-coroutines-core-jvm-1.5.2.jar:na]
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:678) ~[kotlinx-coroutines-core-jvm-1.5.2.jar:na]
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:665) ~[kotlinx-coroutines-core-jvm-1.5.2.jar:na]
```
This means that you have a conflict with okHttp3. To resolve this, you can add the following dependency in your project :
```xml
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.9.3</version>
</dependency>
```
# Note
A similar library is available for operate there:
[camunda-operate-client-java](https://github.com/camunda-community-hub/camunda-operate-client-java)
