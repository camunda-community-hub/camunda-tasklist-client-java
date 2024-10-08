package io.camunda.tasklist.auth;

import java.net.URL;

public record JwtCredential(String clientId, String clientSecret, String audience, URL authUrl) {}
