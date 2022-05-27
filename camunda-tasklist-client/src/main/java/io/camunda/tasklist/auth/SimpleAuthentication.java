package io.camunda.tasklist.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import com.apollographql.apollo3.ApolloClient;
import com.apollographql.apollo3.api.http.HttpHeader;

import io.camunda.tasklist.exception.TaskListException;

/**
 * To use if Tasklist is not configured with Identity and Keycloak
 * 
 * @author ChristopheDame
 *
 */
public class SimpleAuthentication implements AuthInterface {

    private String taskListUsername;

    private String taskListPassword;

    private String taskListUrl;

    public SimpleAuthentication(String taskListUsername, String taskListPassword, String taskListUrl) {
        this.taskListUsername = taskListUsername;
        this.taskListPassword = taskListPassword;
        this.taskListUrl = taskListUrl;
    }

    @Override
    public void authenticate(ApolloClient client) throws TaskListException {

        HttpPost httpPost = new HttpPost(taskListUrl + "/api/login");

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", taskListUsername));
        params.add(new BasicNameValuePair("password", taskListPassword));
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String cookie = response.getHeader("Set-Cookie").getValue();
                client.getHttpHeaders().clear();
                client.getHttpHeaders().add(new HttpHeader("Cookie", cookie));
            }
        } catch (IOException | ProtocolException e) {
            throw new TaskListException(e);
        }
    }
}
