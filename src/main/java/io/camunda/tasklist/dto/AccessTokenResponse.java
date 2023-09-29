package io.camunda.tasklist.dto;

import java.math.BigInteger;

public class AccessTokenResponse {

  String access_token;
  String scope;
  BigInteger expires_in;
  String token_type;

  public AccessTokenResponse() {
  }

  public AccessTokenResponse(String access_token, String scope, BigInteger expires_in, String token_type) {
    this.access_token = access_token;
    this.scope = scope;
    this.expires_in = expires_in;
    this.token_type = token_type;
  }

  public String getAccess_token() {
    return access_token;
  }

  public String getScope() {
    return scope;
  }

  public BigInteger getExpires_in() {
    return expires_in;
  }

  public String getToken_type() {
    return token_type;
  }
}
