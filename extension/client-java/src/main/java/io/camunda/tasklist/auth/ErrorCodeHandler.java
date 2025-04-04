package io.camunda.tasklist.auth;

import org.apache.hc.core5.http.ClassicHttpResponse;

public interface ErrorCodeHandler {
  RuntimeException handleError(ClassicHttpResponse response);
}
