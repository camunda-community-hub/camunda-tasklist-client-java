package io.camunda.tasklist.util;

import io.camunda.tasklist.auth.Authentication;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

public class HttpClientFactory {
  public static CloseableHttpClient buildTasklistHttpClient(Authentication authentication) {
    return HttpClients.custom()
        .useSystemProperties()
        .addRequestInterceptorFirst(
            (request, entity, context) ->
                authentication.getTokenHeader().forEach(request::addHeader))
        .build();
  }
}
