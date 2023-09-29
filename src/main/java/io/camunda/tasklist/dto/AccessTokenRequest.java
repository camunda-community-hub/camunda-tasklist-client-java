package io.camunda.tasklist.dto;

public class AccessTokenRequest {

  String client_id;
  String client_secret;
  String audience;
  String grant_type;

  public AccessTokenRequest() {
  }

  public AccessTokenRequest(String client_id, String client_secret, String audience, String grant_type) {
    this.client_id = client_id;
    this.client_secret = client_secret;
    this.audience = audience;
    this.grant_type = grant_type;
  }

  public String getClient_id() {
    return client_id;
  }

  public String getClient_secret() {
    return client_secret;
  }

  public String getAudience() {
    return audience;
  }

  public String getGrant_type() {
    return grant_type;
  }


}
