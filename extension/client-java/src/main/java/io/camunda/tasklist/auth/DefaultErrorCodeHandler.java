package io.camunda.tasklist.auth;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultErrorCodeHandler implements ErrorCodeHandler {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultErrorCodeHandler.class);

  @Override
  public RuntimeException handleError(ClassicHttpResponse response) {
    String message =
        "Unsuccessful response: Code "
            + response.getCode()
            + (response.getReasonPhrase() == null ? "" : " " + response.getReasonPhrase());
    try (InputStream content = response.getEntity().getContent()) {
      if (content != null) {
        StringWriter writer = new StringWriter();
        Reader reader = new InputStreamReader(content);
        reader.transferTo(writer);
        String errorBody = writer.toString();
        return new RuntimeException(message + ", body: " + errorBody);
      }
    } catch (IOException e) {
      LOG.debug("Error while reading error response", e);
    }
    return new RuntimeException(message);
  }
}
