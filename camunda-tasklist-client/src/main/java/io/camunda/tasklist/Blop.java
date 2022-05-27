package io.camunda.tasklist;

import java.util.List;
import java.util.Map;

import io.camunda.tasklist.auth.SaasAuthentication;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.exception.TaskListException;

public final class Blop {

    public static void main(String[] args) throws TaskListException {
        CamundaTaskListClient client = new CamundaTaskListClient.Builder().taskListUrl("https://bru-2.tasklist.camunda.io/9cc5bd08-03a7-4481-abd8-6d3a6cfb7d8a")
                .authentication(new SaasAuthentication("YTeShGXsSyti4z3lW6uHbSn5fT46plgH", "gLwdtHIIqdYWNZMsXZIWtz3sbzzPcNhgW6Bj5jGt~WjKKL0uh0nLxXQI_BbHVgkv")).build();
        
       

        List<Task> tasks = client.getTasks(true, "demo", TaskState.CREATED, null);
        for (Task task : tasks) {
            // client.unclaim(task.getId());
        }
        tasks = client.getTasksWithVariables(true, "demo", TaskState.COMPLETED, null);
        for (Task task : tasks) {
            client.claim(task.getId());
        }
        for (Task task : tasks) {
            client.completeTask(task.getId(), Map.of("toto", "toto"));
        }

    }
}
