package io.camunda.tasklist.auth;

import java.io.IOException;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

import com.apollographql.apollo3.api.http.HttpHeader;
import com.fasterxml.jackson.databind.JsonNode;

import io.camunda.tasklist.exception.TaskListException;
import io.camunda.tasklist.util.JsonUtils;

public class SaasAuthentication implements AuthInterface {

	private String clientId;
	private String clientSecret;

	public SaasAuthentication(String clientId, String clientSecret) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}
	@Override
	public HttpHeader getHeader() throws TaskListException {

		HttpPost httpPost = new HttpPost("https://login.cloud.camunda.io/oauth/token");
		httpPost.addHeader("Content-Type", "application/json");

		String data = "{\"grant_type\":\"client_credentials\", \"audience\":\"tasklist.camunda.io\", \"client_id\": \""+clientId+"\", \"client_secret\":\""+ clientSecret+"\"}";
		httpPost.setEntity(new StringEntity(data));

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
				JsonNode responseBody = JsonUtils.toJsonNode(response.getEntity().getContent());
				String token = responseBody.get("access_token").asText();
				return new HttpHeader("Authorization", "Bearer "+token);
			}
		} catch(IOException e) {
			throw new TaskListException(e);
		}
	}
}
