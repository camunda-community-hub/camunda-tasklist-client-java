package io.camunda.tasklist;

import static org.assertj.core.api.Assertions.*;

import io.camunda.client.api.JsonMapper;
import io.camunda.client.impl.CamundaObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JsonMapperTest {
  @Test
  void shouldDeserializeToObjectNumber() {
    String json = "123";
    JsonMapper jsonMapper = new CamundaObjectMapper();
    Object fromJson = jsonMapper.fromJson(json, Object.class);
    assertThat(fromJson).isInstanceOf(Number.class);
  }

  @Test
  void shouldDeserializeToObjectString() {
    String json = "\"abc\"";
    JsonMapper jsonMapper = new CamundaObjectMapper();
    Object fromJson = jsonMapper.fromJson(json, Object.class);
    assertThat(fromJson).isInstanceOf(String.class);
  }

  @Test
  void shouldDeserializeToObjectNull() {
    String json = "null";
    JsonMapper jsonMapper = new CamundaObjectMapper();
    Object fromJson = jsonMapper.fromJson(json, Object.class);
    assertThat(fromJson).isNull();
  }

  @Test
  void shouldDeserializeToObjectArray() {
    String json = "[\"abc\"]";
    JsonMapper jsonMapper = new CamundaObjectMapper();
    Object fromJson = jsonMapper.fromJson(json, Object.class);
    assertThat(fromJson).isInstanceOf(List.class);
    List<String> fromJsonList = (List<String>) fromJson;
    assertThat(fromJsonList).containsExactly("abc");
  }

  @Test
  void shouldDeserializeToObjectObject() {
    String json = "{\"foo\":\"bar\"}";
    JsonMapper jsonMapper = new CamundaObjectMapper();
    Object fromJson = jsonMapper.fromJson(json, Object.class);
    assertThat(fromJson).isInstanceOf(Map.class);
    Map<String, Object> fromJsonMap = (Map<String, Object>) fromJson;
    assertThat(fromJsonMap).containsExactly(entry("foo", "bar"));
  }

  @Test
  void shouldDeserializeToObjectBoolean() {
    String json = "false";
    JsonMapper jsonMapper = new CamundaObjectMapper();
    Object fromJson = jsonMapper.fromJson(json, Object.class);
    assertThat(fromJson).isInstanceOf(Boolean.class);
  }
}
