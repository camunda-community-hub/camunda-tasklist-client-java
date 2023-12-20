package io.camunda.tasklist.dto;

import java.util.Iterator;
import java.util.List;

public class TaskList implements Iterable<Task> {

  private List<Task> items;

  private TaskSearch search;

  public List<Task> getItems() {
    return items;
  }

  public TaskList setItems(List<Task> items) {
    this.items = items;
    return this;
  }

  public Task first() {
    if (items != null && !items.isEmpty()) {
      return items.get(0);
    }
    return null;
  }

  public Task get(int index) {
    if (items != null && !items.isEmpty()) {
      return items.get(index);
    }
    return null;
  }

  public Task last() {
    if (items != null && !items.isEmpty()) {
      return items.get(items.size() - 1);
    }
    return null;
  }

  public int size() {
    if (items != null) {
      return items.size();
    }
    return 0;
  }

  public TaskSearch getSearch() {
    return search;
  }

  public TaskList setSearch(TaskSearch search) {
    this.search = search;
    return this;
  }

  @Override
  public Iterator<Task> iterator() {
    return items.iterator();
  }
}
