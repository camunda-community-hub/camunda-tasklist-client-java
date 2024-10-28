package io.camunda.tasklist;

import static org.assertj.core.api.Assertions.*;

import io.camunda.tasklist.dto.TaskSearch;
import org.junit.jupiter.api.Test;

public class TaskSearchTest {
  @Test
  void shouldCloneTaskSearch() {
    TaskSearch taskSearch = new TaskSearch().fetchVariable("foo");
    TaskSearch clone = taskSearch.clone();
    assertThat(clone).isNotSameAs(taskSearch);
    assertThat(clone.getIncludeVariables()).hasSize(1);
    assertThat(clone.getIncludeVariables().get(0).getName()).isEqualTo("foo");
  }
}
