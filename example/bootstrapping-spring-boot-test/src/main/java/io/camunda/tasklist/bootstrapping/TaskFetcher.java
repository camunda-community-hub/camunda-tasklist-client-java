package io.camunda.tasklist.bootstrapping;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.TasklistClient;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.exception.TaskListException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class TaskFetcher {
  private final CamundaTaskListClient tasklistClient;

  public TaskFetcher(CamundaTaskListClient tasklistClient) {this.tasklistClient = tasklistClient;}

  @Scheduled(fixedDelay = 10000L)
  public void run() {
    try {
      tasklistClient.getTasks(new TaskSearch());
    } catch (TaskListException e) {
      throw new RuntimeException(e);
    }
  }
}
