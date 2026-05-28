package io.camunda.tasklist.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.camunda.tasklist.exception.TaskListException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class ConverterUtilsTest {

  /** Reset the static ObjectMapper after each test so tests remain independent. */
  @AfterEach
  void resetObjectMapper() {
    ConverterUtils.setObjectMapper(null);
  }

  @Test
  void defaultObjectMapperSupportsOptionalFields() throws Exception {
    // Verifies that ConverterUtils' default ObjectMapper has Jdk8Module registered so
    // that java.util.Optional fields round-trip through toTask() without throwing.
    WithOptional source = new WithOptional();
    source.setValue(Optional.of("hello"));
    // toTask() uses getObjectMapper() for both write and read; Jdk8Module is required
    // to serialise the Optional field on the write step.
    io.camunda.tasklist.dto.Task result = ConverterUtils.toTask(source, null);
    assertThat(result).isNotNull();
  }

  @Test
  void setObjectMapperAllowsCustomConfiguration() {
    // FAIL_ON_UNKNOWN_PROPERTIES=true differs from the default (false).
    // WithOptional serialises to {"value":"hello"}; Task has no "value" field,
    // so a strict mapper must reject it — proving the custom mapper is actually used.
    ObjectMapper strict =
        new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new Jdk8Module())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    ConverterUtils.setObjectMapper(strict);

    WithOptional source = new WithOptional();
    source.setValue(Optional.of("hello"));
    assertThatThrownBy(() -> ConverterUtils.toTask(source, null))
        .isInstanceOf(TaskListException.class);
  }

  // ---------- helper DTO ----------

  static class WithOptional {
    private Optional<String> value = Optional.empty();

    @JsonProperty("value")
    public Optional<String> getValue() {
      return value;
    }

    @JsonProperty("value")
    public void setValue(Optional<String> value) {
      this.value = value;
    }
  }
}
