[![Community Extension](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)

# Camunda TaskList Client

This project is designed to simplify communication between a java backend and the Camunda 8 task list. This project is still a draft.

# How to use the client

Simply build a CamundaTaskListClient that takes an authentication and the tasklist url as parameters.

```
SimpleAuthentication sa = new SimpleAuthentication("demo", "demo", "http://localhost:8081");
CamundaTaskListClient client = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8081").authentication(sa).build();
List<Task> tasks = client.getTasks(true, "demo", TaskState.CREATED, null);
for(Task task : tasks) {
    client.unclaim(task.getId());
}
tasks = client.getTasks(false, null, null, null);
for(Task task : tasks) {
    client.claim(task.getId());
}
for(Task task : tasks) {
    client.completeTask(task.getId(), Map.of("toto", "toto"));
}
```

To connect to the **SaaS** TaskList, you need to use the **SaasAuthentication** rather than the SimpleAuthentication. The SaaSAuthentication requires the ClientId and SecretId

```
SaasAuthentication sa = new SaasAuthentication("2~nB1MwkUU45FuXXX", "aBRKtreXQF3uD2MYYY");
CamundaTaskListClient client = new CamundaTaskListClient.Builder().authentication(sa)
    .taskListUrl("https://bru-2.tasklist.camunda.io/757dbc30-5127-4bed-XXXX-XXXXXXXXXXXX").build();


client.getTasks(null, null, TaskState.CREATED, 50);
```

# BUILD
To build a jar with all dependencies, please use the shadowJar goal 

```
gradlew shadowJar
```
# TODO

- Add a jwt keycloak auth
