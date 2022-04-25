package io.camunda.tasklist.util;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

    private JsonUtils() {
    }

    private static ObjectMapper mapper;

    public static JsonNode toJsonNode(InputStream is) throws IOException {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        return mapper.readTree(is);
    }

}
