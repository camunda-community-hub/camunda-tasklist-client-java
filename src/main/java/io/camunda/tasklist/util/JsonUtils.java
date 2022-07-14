package io.camunda.tasklist.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

    private JsonUtils() {
    }

    private static ObjectMapper mapper;

    public static JsonNode toJsonNode(String json) throws IOException {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        return mapper.readTree(json);
    }

}
