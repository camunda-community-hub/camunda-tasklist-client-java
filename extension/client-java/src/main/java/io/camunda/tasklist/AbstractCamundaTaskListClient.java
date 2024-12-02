package io.camunda.tasklist;

import io.camunda.tasklist.dto.Pagination;
import io.camunda.tasklist.dto.SearchType;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.dto.Variable;
import io.camunda.tasklist.exception.TaskListException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class AbstractCamundaTaskListClient implements CamundaTaskListClient {
  private final CamundaTaskListClientDefaultBehaviourProperties defaultBehaviourProperties;

  public AbstractCamundaTaskListClient(
      CamundaTaskListClientDefaultBehaviourProperties defaultBehaviourProperties) {
    this.defaultBehaviourProperties = defaultBehaviourProperties;
  }

  @Override
  public Task getTask(String taskId) throws TaskListException {
    return getTask(taskId, defaultBehaviourProperties.defaultShouldReturnVariables());
  }

  @Override
  public List<Variable> getVariables(String taskId) throws TaskListException {
    return getVariables(taskId, defaultBehaviourProperties.defaultShouldLoadTruncatedVariables());
  }

  @Override
  public TaskList before(TaskList taskList) throws TaskListException {
    return paginate(taskList, SearchType.BEFORE);
  }

  @Override
  public TaskList beforeOrEquals(TaskList taskList) throws TaskListException {
    return paginate(taskList, SearchType.BEFORE_OR_EQUAL);
  }

  @Override
  public TaskList after(TaskList taskList) throws TaskListException {
    return paginate(taskList, SearchType.AFTER);
  }

  @Override
  public TaskList afterOrEqual(TaskList taskList) throws TaskListException {
    return paginate(taskList, SearchType.AFTER_OR_EQUAL);
  }

  @Override
  public void loadVariables(List<Task> tasks) throws TaskListException {
    loadVariables(tasks, defaultBehaviourProperties.defaultShouldLoadTruncatedVariables());
  }

  @Override
  public void loadVariables(List<Task> tasks, boolean loadTruncated) throws TaskListException {
    try {
      Map<String, Future<List<Variable>>> futures = new HashMap<>();
      Map<String, Task> taskMap = new HashMap<>();
      for (Task task : tasks) {
        taskMap.put(task.getId(), task);
        futures.put(
            task.getId(),
            CompletableFuture.supplyAsync(
                () -> {
                  try {
                    return getVariables(task.getId(), loadTruncated);
                  } catch (TaskListException e) {
                    return null;
                  }
                }));
      }
      for (Map.Entry<String, Future<List<Variable>>> varFutures : futures.entrySet()) {
        taskMap.get(varFutures.getKey()).setVariables(varFutures.getValue().get());
      }
      futures.clear();
      taskMap.clear();
    } catch (ExecutionException | InterruptedException e) {
      throw new TaskListException("Error loading task variables", e);
    }
  }

  @Override
  public TaskList getTasks(TaskSearch search) throws TaskListException {
    if (search.getWithVariables() == null) {
      search.setWithVariables(defaultBehaviourProperties.defaultShouldReturnVariables());
    }
    return getTasksInternal(search);
  }

  protected abstract TaskList getTasksInternal(TaskSearch search) throws TaskListException;

  private TaskList paginate(TaskList taskList, SearchType direction) throws TaskListException {
    if (taskList.getSearch().getPagination() == null
        || taskList.getSearch().getPagination().getPageSize() == null) {
      throw new TaskListException(
          "Before/After/AfterOrEquals search are only possible if a pageSize is set");
    }
    if (taskList.getItems() == null || taskList.getItems().isEmpty()) {
      throw new TaskListException(
          "Before/After/AfterOrEquals search are only possible if some items are present");
    }

    TaskSearch newSearch =
        taskList.getSearch().clone().setPagination(getSearchPagination(taskList, direction));
    return getTasks(newSearch);
  }

  private Pagination getSearchPagination(TaskList taskList, SearchType type) {
    return switch (type) {
      case BEFORE ->
          new Pagination.Builder()
              .pageSize(taskList.getSearch().getPagination().getPageSize())
              .before(taskList.first().getSortValues())
              .build();
      case BEFORE_OR_EQUAL ->
          new Pagination.Builder()
              .pageSize(taskList.getSearch().getPagination().getPageSize())
              .beforeOrEqual(taskList.first().getSortValues())
              .build();
      case AFTER ->
          new Pagination.Builder()
              .pageSize(taskList.getSearch().getPagination().getPageSize())
              .after(taskList.last().getSortValues())
              .build();
      default ->
          new Pagination.Builder()
              .pageSize(taskList.getSearch().getPagination().getPageSize())
              .afterOrEqual(taskList.last().getSortValues())
              .build();
    };
  }
}
