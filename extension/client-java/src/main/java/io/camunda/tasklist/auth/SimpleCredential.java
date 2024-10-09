package io.camunda.tasklist.auth;

import java.net.URL;
import java.time.Duration;

/** Contains credential for particular product. Used for Simple authentication. */
public record SimpleCredential(
    String username, String password, URL baseUrl, Duration sessionTimeout) {}
