package io.camunda.tasklist.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils<T> {

  Class<T> clazz;
  ObjectMapper objectMapper;

  public JsonUtils(Class<T> clazz) {
    this.clazz = clazz;
    this.objectMapper = getObjectMapper();
  }

  public ObjectMapper getObjectMapper() {
    if (objectMapper == null) {
      return new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    } else {
      return objectMapper;
    }
  }

  public String toJson(T object) throws JsonProcessingException {
    return objectMapper.writeValueAsString(object);
  }

  public T fromJson(String json) throws JsonProcessingException {
    JavaType javaType = objectMapper.getTypeFactory().constructType(clazz);
    return objectMapper.readValue(json, javaType);
  }

}
