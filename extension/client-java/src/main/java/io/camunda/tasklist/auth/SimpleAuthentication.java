package io.camunda.tasklist.auth;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleAuthentication implements Authentication {
  private static final String CSRF_HEADER = "TASKLIST-X-CSRF-TOKEN";
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final SimpleCredential simpleCredential;
  private SimpleAuthToken token;
  private LocalDateTime sessionTimeout;

  public SimpleAuthentication(SimpleCredential simpleCredential) {
    this.simpleCredential = simpleCredential;
  }

  private SimpleAuthToken retrieveToken() {
    try (CloseableHttpClient client = HttpClients.createSystem()) {
      HttpPost request = buildRequest(simpleCredential);
      SimpleAuthToken simpleAuthToken =
          client.execute(
              request,
              response -> {
                if (response.getCode() > 299) {
                  throw new RuntimeException(
                      "Unable to login, response code " + response.getCode());
                }
                String csrfTokenCandidate = null;
                Header csrfTokenHeader = response.getHeader(CSRF_HEADER);
                if (csrfTokenHeader != null) {
                  csrfTokenCandidate = csrfTokenHeader.getValue();
                }
                Header[] cookieHeaders = response.getHeaders("Set-Cookie");
                String sessionCookie = null;
                String csrfCookie = null;
                String sessionCookieName = "TASKLIST-SESSION";
                for (Header cookieHeader : cookieHeaders) {
                  if (cookieHeader.getValue().startsWith(sessionCookieName)) {
                    sessionCookie = cookieHeader.getValue();
                  }
                  if (cookieHeader.getValue().startsWith(CSRF_HEADER)) {
                    csrfCookie = cookieHeader.getValue();
                  }
                }
                return new SimpleAuthToken(sessionCookie, csrfCookie, csrfTokenCandidate);
              });
      if (simpleAuthToken.sessionCookie() == null) {
        throw new RuntimeException(
            "Unable to authenticate due to missing Set-Cookie TASKLIST-SESSION");
      }
      if (simpleAuthToken.csrfToken() == null) {
        LOG.info("No CSRF token found");
      }
      if (simpleAuthToken.csrfCookie() == null) {
        LOG.info("No CSRF cookie found");
      }
      sessionTimeout = LocalDateTime.now().plus(simpleCredential.sessionTimeout());
      return simpleAuthToken;
    } catch (Exception e) {
      throw new RuntimeException("Unable to authenticate", e);
    }
  }

  private HttpPost buildRequest(SimpleCredential simpleCredential) {
    HttpPost httpPost = new HttpPost(simpleCredential.baseUrl().toString() + "/api/login");
    List<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("username", simpleCredential.username()));
    params.add(new BasicNameValuePair("password", simpleCredential.password()));
    httpPost.setEntity(new UrlEncodedFormEntity(params));
    return httpPost;
  }

  @Override
  public Map<String, String> getTokenHeader() {
    if (token == null || sessionTimeout.isBefore(LocalDateTime.now())) {
      token = retrieveToken();
    }
    Map<String, String> headers = new HashMap<>();
    if (token.csrfToken() != null) {
      headers.put(CSRF_HEADER, token.csrfToken());
    }
    headers.put(
        "Cookie",
        Stream.of(token.sessionCookie(), token.csrfCookie())
            .filter(Objects::nonNull)
            .collect(Collectors.joining(";")));
    return headers;
  }

  @Override
  public void resetToken() {
    token = null;
  }

  private record SimpleAuthToken(String sessionCookie, String csrfCookie, String csrfToken) {}
}
